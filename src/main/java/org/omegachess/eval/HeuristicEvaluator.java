package org.omegachess.eval;

import org.omegachess.core.GameState;
import org.omegachess.core.Move;
import org.omegachess.core.features.FeatureScratch;
import org.omegachess.core.features.FeatureSpec;

/* Values are always from the point of view of the side to move. */
public final class HeuristicEvaluator
  {
    public static float MATERIAL_WEIGHT       = 0.0025f;
    public static float MOBILITY_WEIGHT       = 0.0015f;
    public static float KING_SAFETY_WEIGHT    = 0.0100f;
    public static float PAWN_STRUCTURE_WEIGHT = 0.0050f;
    public static float SEE_WEIGHT            = 0.0020f;

    private final GameState seeState = new GameState();
    private final Move[] seeCandidateBuffer = new Move[GameState._NONE];
    private final Move[] seeExchangeBuffer = new Move[GameState._NONE];
    private final float[] seeGains = new float[GameState._NONE];

    public float evaluate(GameState gs, FeatureScratch scratch)
      {
        float score = 0.0f;

        if(gs == null)
          throw new IllegalArgumentException("GameState argument cannot be null");
        if(scratch == null)
          throw new IllegalArgumentException("FeatureScratch argument cannot be null");

        score += MATERIAL_WEIGHT * material(gs);
        score += MOBILITY_WEIGHT * mobility(gs, scratch);
        score += KING_SAFETY_WEIGHT * kingSafety(gs, scratch);
        score += PAWN_STRUCTURE_WEIGHT * pawnStructure(gs);
        score += SEE_WEIGHT * seeScore(gs);

        return score;
      }

    private static float material(GameState gs)
      {
        float val, h = 0.0f;
        int i;
        boolean whiteToMove = gs.isWhiteToMove();

        for(i = 0; i < GameState._NONE; i++)
          {
            val = 0.0f;
            if(!gs.oob(i) && !gs.isEmpty(i))
              {
                if(gs.isPawn(i))
                  val = PaganSpec.MATERIAL_PAWN;
                else if(gs.isKnight(i))
                  val = PaganSpec.MATERIAL_KNIGHT;
                else if(gs.isChampion(i))
                  val = PaganSpec.MATERIAL_CHAMPION;
                else if(gs.isWizard(i))
                  val = PaganSpec.MATERIAL_WIZARD;
                else if(gs.isBishop(i))
                  val = PaganSpec.MATERIAL_BISHOP;
                else if(gs.isRook(i))
                  val = PaganSpec.MATERIAL_ROOK;
                else if(gs.isQueen(i))
                  val = PaganSpec.MATERIAL_QUEEN;
              }
            if(val > 0.0f)
              {
                if(gs.isWhite(i) == whiteToMove)
                  h += val;
                else
                  h -= val;
              }
          }

        return h;
      }

    private static float mobility(GameState gs, FeatureScratch scratch)
      {
        int white = 0, black = 0;
        int i;

        for(i = 0; i < GameState._NONE; i++)
          {
            if(gs.oob(i))
              continue;

            white += scratch.whiteMobility[i];
            black += scratch.blackMobility[i];
          }

        if(gs.isWhiteToMove())
          return (float)(white - black);

        return (float)(black - white);
      }

    private static float kingSafety(GameState gs, FeatureScratch scratch)
      {
        float whiteDanger = kingDanger(true, scratch);
        float blackDanger = kingDanger(false, scratch);

        if(gs.isWhiteToMove())
          return blackDanger - whiteDanger;

        return whiteDanger - blackDanger;
      }

    private static float kingDanger(boolean white, FeatureScratch scratch)
      {
        int pressure = 0, escapes = 0;
        int i;

        int[] kingPressure = white ? scratch.whiteKingPressure : scratch.blackKingPressure;
        byte[] kingEscapes = white ? scratch.whiteKingEscapes : scratch.blackKingEscapes;
        int checkerCount = white ? scratch.whiteKingCheckerCount : scratch.blackKingCheckerCount;

        for(i = 0; i < FeatureSpec.SQUARES; i++)
          {
            pressure += kingPressure[i];

            if(kingEscapes[i] != 0)
              escapes++;
          }

        return PaganSpec.KING_PRESSURE_WEIGHT * pressure + PaganSpec.KING_CHECK_WEIGHT * checkerCount - PaganSpec.KING_ESCAPE_WEIGHT * escapes;
      }

    private static float pawnStructure(GameState gs)
      {
        float white = 0.0f, black = 0.0f;
        float score;
        boolean whitePawn;
        int i;

        for(i = 0; i < GameState._NONE; i++)
          {
            if(gs.oob(i) || !gs.isPawn(i))
              continue;

            whitePawn = gs.isWhite(i);
            score = 0.0f;

            if(isPassedPawn(gs, i))
              score += passedPawnValue(gs, i, whitePawn);

            if(isDoubledPawn(gs, i, whitePawn))
              score -= PaganSpec.PAWN_DOUBLED;

            if(isIsolatedPawn(gs, i, whitePawn))
              score -= PaganSpec.PAWN_ISOLATED;

            if(isConnectedPawn(gs, i, whitePawn))
              score += PaganSpec.PAWN_CONNECTED;

            if(whitePawn)
              white += score;
            else
              black += score;
          }

        return gs.isWhiteToMove() ? white - black : black - white;
      }

    private static boolean isPassedPawn(GameState gs, int index)
      {
        boolean white = gs.isWhite(index);
        int forward = white ? gs.u(index) : gs.d(index);
        int left, right;

        while(!gs.oob(forward))
          {
            if(gs.isPawn(forward) && gs.opposed(index, forward))
              return false;

            left = gs.l(forward);
            if(!gs.oob(left) && gs.isPawn(left) && gs.opposed(index, left))
              return false;

            right = gs.r(forward);
            if(!gs.oob(right) && gs.isPawn(right) && gs.opposed(index, right))
              return false;

            forward = white ? gs.u(forward) : gs.d(forward);
          }

        return true;
      }

    private static float passedPawnValue(GameState gs, int index, boolean white)
      {
        int row = gs.row(index);

        if(white)
          return PaganSpec.PASSED_PAWN_BONUS[row - 2];
        else
          return PaganSpec.PASSED_PAWN_BONUS[9 - row];
      }

    private static boolean isDoubledPawn(GameState gs, int index, boolean white)
      {
        int square;
                                                                    //  Count a doubled pawn only when another friendly pawn lies behind it.
                                                                    //  This makes N pawns on one file produce exactly N - 1 doubled-pawn penalties.
        square = white ? gs.d(index) : gs.u(index);

        while(!gs.oob(square))
          {
            if(gs.isPawn(square) && (white ? gs.isWhite(square) : gs.isBlack(square)))
              return true;

            square = white ? gs.d(square) : gs.u(square);
          }

        return false;
      }

    private static boolean isIsolatedPawn(GameState gs, int index, boolean white)
      {
        int left = gs.l(index);
        int right = gs.r(index);

        if(!gs.oob(left) && hasFriendlyPawnOnFile(gs, left, white))
          return false;

        if(!gs.oob(right) && hasFriendlyPawnOnFile(gs, right, white))
          return false;

        return true;
      }

    private static boolean hasFriendlyPawnOnFile(GameState gs, int index, boolean white)
      {
        int square;

        if(gs.isPawn(index) && (white ? gs.isWhite(index) : gs.isBlack(index)))
          return true;

        square = gs.u(index);
        while(!gs.oob(square))
          {
            if(gs.isPawn(square) && (white ? gs.isWhite(square) : gs.isBlack(square)))
              return true;

            square = gs.u(square);
          }

        square = gs.d(index);
        while(!gs.oob(square))
          {
            if(gs.isPawn(square) && (white ? gs.isWhite(square) : gs.isBlack(square)))
              return true;

            square = gs.d(square);
          }

        return false;
      }

    private static boolean isConnectedPawn(GameState gs, int index, boolean white)
      {
        int leftSupport, rightSupport;

        if(white)
          {
            leftSupport = gs.dl(index);
            rightSupport = gs.dr(index);
          }
        else
          {
            leftSupport = gs.ul(index);
            rightSupport = gs.ur(index);
          }

        if(!gs.oob(leftSupport) && gs.isPawn(leftSupport) && (white ? gs.isWhite(leftSupport) : gs.isBlack(leftSupport)))
          return true;

        if(!gs.oob(rightSupport) && gs.isPawn(rightSupport) && (white ? gs.isWhite(rightSupport) : gs.isBlack(rightSupport)))
          return true;

        return false;
      }

    private float seeScore(GameState gs)
      {
        float whiteBest = bestSeeForSide(gs, true);
        float blackBest = bestSeeForSide(gs, false);

        if(gs.isWhiteToMove())
          return whiteBest - blackBest;

        return blackBest - whiteBest;
      }

    private float bestSeeForSide(GameState gs, boolean white)
      {
        float best = 0.0f;
        float value;
        int len;
        int target, i;

        for(target = 0; target < GameState._NONE; target++)
          {
            if(gs.oob(target) || gs.isEmpty(target))
              continue;
                                                                    //  The king is never a material victim.
            if(gs.isKing(target))                                   //  Check/checkmate is handled by the game/search logic.
              continue;
            if(gs.isWhite(target) == white)                         //  Only enemy pieces can be capture targets.
              continue;

            len = gs.attackersOfSquare(target, white, seeCandidateBuffer);

            for(i = 0; i < len; i++)
              {
                if(seeCandidateBuffer[i] == null)
                  continue;

                value = see(seeCandidateBuffer[i], gs);

                if(value > best)
                  best = value;
              }
          }
                                                                    //  Best begins at zero, so unfavorable captures are ignored:
        return best;                                                //  the player is always free not to make them.
      }

    private float see(Move move, GameState src)
      {
        GameState gs = seeState;
        Move[] buffer = seeExchangeBuffer;
        float[] gains = seeGains;

        int gainsLen = 0;
        int target = move.to;
        int len, chosen;
        boolean white;
        float leastVal, val, victimVal;
        int i, j;

        gs.copyFrom(src);
                                                                    //  Remember the color of the initial captor explicitly.
        white = gs.isWhite(move.from);                              //  This permits SEE to examine captures for either side, not merely the actual side to move.

        if(gs.isEnPassantAttack(move))
          gains[0] = PaganSpec.MATERIAL_PAWN;
        else
          gains[0] = seeValue(gs, target);

        gains[0] += promotionGain(move);

        gs.makeMove(move);

        white = !white;                                             //  The first recapture belongs to the other side.

        while(true)
          {
            len = gs.attackersOfSquare(target, white, buffer);

            if(len == 0)
              break;

            chosen = -1;
            leastVal = PaganSpec.SEE_KING * 2.0f;

            for(i = 0; i < len; i++)
              {
                if(buffer[i] == null)
                  continue;

                val = seeValue(gs, buffer[i].from);

                if(val < leastVal)
                  {
                    leastVal = val;
                    chosen = i;
                  }
              }

            if(chosen < 0)
              break;

            victimVal = seeValue(gs, target);

            gainsLen++;
            gains[gainsLen] = victimVal + promotionGain(buffer[chosen]) - gains[gainsLen - 1];

            gs.makeMove(buffer[chosen]);

            white = !white;
          }

        for(j = gainsLen - 1; j >= 0; j--)
          gains[j] = -Math.max(-gains[j], gains[j + 1]);

        return gains[0];
      }

    private static float promotionGain(Move move)
      {
        switch(move.promo)
          {
            case GameState._PROMO_KNIGHT:   return PaganSpec.MATERIAL_KNIGHT   - PaganSpec.MATERIAL_PAWN;
            case GameState._PROMO_CHAMPION: return PaganSpec.MATERIAL_CHAMPION - PaganSpec.MATERIAL_PAWN;
            case GameState._PROMO_WIZARD:   return PaganSpec.MATERIAL_WIZARD   - PaganSpec.MATERIAL_PAWN;
            case GameState._PROMO_BISHOP:   return PaganSpec.MATERIAL_BISHOP   - PaganSpec.MATERIAL_PAWN;
            case GameState._PROMO_ROOK:     return PaganSpec.MATERIAL_ROOK     - PaganSpec.MATERIAL_PAWN;
            case GameState._PROMO_QUEEN:    return PaganSpec.MATERIAL_QUEEN    - PaganSpec.MATERIAL_PAWN;
            default:                        return 0.0f;
          }
      }

    private static float seeValue(GameState gs, int index)
      {
        if(gs.oob(index) || gs.isEmpty(index))
          return 0.0f;

        if(gs.isPawn(index))
          return PaganSpec.MATERIAL_PAWN;
        if(gs.isKnight(index))
          return PaganSpec.MATERIAL_KNIGHT;
        if(gs.isChampion(index))
          return PaganSpec.MATERIAL_CHAMPION;
        if(gs.isWizard(index))
          return PaganSpec.MATERIAL_WIZARD;
        if(gs.isBishop(index))
          return PaganSpec.MATERIAL_BISHOP;
        if(gs.isRook(index))
          return PaganSpec.MATERIAL_ROOK;
        if(gs.isQueen(index))
          return PaganSpec.MATERIAL_QUEEN;
        if(gs.isKing(index))                                        //  Large sentinel: king should always be the last possible captor chosen in an exchange.
          return PaganSpec.SEE_KING;

        return 0.0f;
      }
  }