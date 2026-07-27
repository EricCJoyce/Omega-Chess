package org.omegachess.core.features;

import org.omegachess.core.GameState;
import org.omegachess.core.Move;

public final class TacticalAnalyzer
  {
    private TacticalAnalyzer()
      {
      }

    public static void analyze(GameState gs, FeatureScratch scratch)
      {
        if(gs == null)
          throw new IllegalArgumentException("GameState must not be null.");

        if(scratch == null)
          throw new IllegalArgumentException("FeatureScratch must not be null.");

        scratch.clear();

        for(int index = 0; index < GameState._NONE; index++)
          {
            if(gs.oob(index) || gs.isEmpty(index))
              continue;

            if(gs.isWhite(index))
              analyzePiece(gs, index, true, scratch);
            else if(gs.isBlack(index))
              analyzePiece(gs, index, false, scratch);
          }

        deriveKingPressure(scratch);

        return;
      }

    private static void analyzePiece(GameState gs, int index, boolean white, FeatureScratch scratch)
      {
        int pieceType = pieceTypeIndex(gs, index);

        scratch.clearDestinationSeen();

        switch(pieceType)
          {
            case FeatureSpec.PIECE_PAWN:
              analyzePawn(gs, index, white, scratch);
              break;

            case FeatureSpec.PIECE_KNIGHT:
              analyzeKnight(gs, index, white, scratch);
              break;

            case FeatureSpec.PIECE_CHAMPION:
              analyzeChampion(gs, index, white, scratch);
              break;

            case FeatureSpec.PIECE_WIZARD:
              analyzeWizard(gs, index, white, scratch);
              break;

            case FeatureSpec.PIECE_BISHOP:
              analyzeBishop(gs, index, white, scratch);
              break;

            case FeatureSpec.PIECE_ROOK:
              analyzeRook(gs, index, white, scratch);
              break;

            case FeatureSpec.PIECE_QUEEN:
              analyzeQueen(gs, index, white, scratch);
              break;

            case FeatureSpec.PIECE_KING:
              analyzeKing(gs, index, white, scratch);
              break;

            default:
              throw new IllegalStateException("Unknown piece at board index " + index + ".");
          }
      }

    private static void analyzePawn(GameState gs, int index, boolean white, FeatureScratch scratch)
      {
        int len;

        if(white)
          {
            markAttack(gs, index, gs.ul(index), true, FeatureSpec.PIECE_PAWN, scratch);
            markAttack(gs, index, gs.ur(index), true, FeatureSpec.PIECE_PAWN, scratch);
          }
        else
          {
            markAttack(gs, index, gs.dl(index), false, FeatureSpec.PIECE_PAWN, scratch);
            markAttack(gs, index, gs.dr(index), false, FeatureSpec.PIECE_PAWN, scratch);
          }

        len = gs.getPawnMoves(index, scratch.mobilityMoveBuffer);
        markMobility(index, countDistinctPawnDestinations(gs, scratch.mobilityMoveBuffer, len, scratch), white, scratch);

        return;
      }

    private static void analyzeKnight(GameState gs, int index, boolean white, FeatureScratch scratch)
      {
        int len;

        markAttack(gs, index, gs.ul(gs.u(index)), white, FeatureSpec.PIECE_KNIGHT, scratch);
        markAttack(gs, index, gs.ur(gs.u(index)), white, FeatureSpec.PIECE_KNIGHT, scratch);

        markAttack(gs, index, gs.ur(gs.r(index)), white, FeatureSpec.PIECE_KNIGHT, scratch);
        markAttack(gs, index, gs.ul(gs.l(index)), white, FeatureSpec.PIECE_KNIGHT, scratch);

        markAttack(gs, index, gs.dl(gs.d(index)), white, FeatureSpec.PIECE_KNIGHT, scratch);
        markAttack(gs, index, gs.dr(gs.d(index)), white, FeatureSpec.PIECE_KNIGHT, scratch);

        markAttack(gs, index, gs.dr(gs.r(index)), white, FeatureSpec.PIECE_KNIGHT, scratch);
        markAttack(gs, index, gs.dl(gs.l(index)), white, FeatureSpec.PIECE_KNIGHT, scratch);

        len = gs.getKnightMoves(index, scratch.mobilityMoveBuffer);
        markMobility(index, len, white, scratch);

        return;
      }

    private static void analyzeChampion(GameState gs, int index, boolean white, FeatureScratch scratch)
      {
        int len;

        markAttack(gs, index, gs.u(index), white, FeatureSpec.PIECE_CHAMPION, scratch);
        markAttack(gs, index, gs.u(gs.u(index)), white, FeatureSpec.PIECE_CHAMPION, scratch);

        markAttack(gs, index, gs.d(index), white, FeatureSpec.PIECE_CHAMPION, scratch);
        markAttack(gs, index, gs.d(gs.d(index)), white, FeatureSpec.PIECE_CHAMPION, scratch);

        markAttack(gs, index, gs.l(index), white, FeatureSpec.PIECE_CHAMPION, scratch);
        markAttack(gs, index, gs.l(gs.l(index)), white, FeatureSpec.PIECE_CHAMPION, scratch);

        markAttack(gs, index, gs.r(index), white, FeatureSpec.PIECE_CHAMPION, scratch);
        markAttack(gs, index, gs.r(gs.r(index)), white, FeatureSpec.PIECE_CHAMPION, scratch);

        markAttack(gs, index, gs.ul(gs.ul(index)), white, FeatureSpec.PIECE_CHAMPION, scratch);
        markAttack(gs, index, gs.ur(gs.ur(index)), white, FeatureSpec.PIECE_CHAMPION, scratch);
        markAttack(gs, index, gs.dr(gs.dr(index)), white, FeatureSpec.PIECE_CHAMPION, scratch);
        markAttack(gs, index, gs.dl(gs.dl(index)), white, FeatureSpec.PIECE_CHAMPION, scratch);

        len = gs.getChampionMoves(index, scratch.mobilityMoveBuffer);
        markMobility(index, len, white, scratch);

        return;
      }

    private static void analyzeWizard(GameState gs, int index, boolean white, FeatureScratch scratch)
      {
        int len;

        markAttack(gs, index, gs.ul(index), white, FeatureSpec.PIECE_WIZARD, scratch);
        markAttack(gs, index, gs.ur(index), white, FeatureSpec.PIECE_WIZARD, scratch);
        markAttack(gs, index, gs.dr(index), white, FeatureSpec.PIECE_WIZARD, scratch);
        markAttack(gs, index, gs.dl(index), white, FeatureSpec.PIECE_WIZARD, scratch);

        markAttack(gs, index, gs.u(gs.u(gs.ul(index))), white, FeatureSpec.PIECE_WIZARD, scratch);
        markAttack(gs, index, gs.u(gs.u(gs.ur(index))), white, FeatureSpec.PIECE_WIZARD, scratch);

        markAttack(gs, index, gs.l(gs.l(gs.ul(index))), white, FeatureSpec.PIECE_WIZARD, scratch);
        markAttack(gs, index, gs.r(gs.r(gs.ur(index))), white, FeatureSpec.PIECE_WIZARD, scratch);

        markAttack(gs, index, gs.d(gs.d(gs.dl(index))), white, FeatureSpec.PIECE_WIZARD, scratch);
        markAttack(gs, index, gs.d(gs.d(gs.dr(index))), white, FeatureSpec.PIECE_WIZARD, scratch);

        markAttack(gs, index, gs.l(gs.l(gs.dl(index))), white, FeatureSpec.PIECE_WIZARD, scratch);
        markAttack(gs, index, gs.r(gs.r(gs.dr(index))), white, FeatureSpec.PIECE_WIZARD, scratch);

        len = gs.getWizardMoves(index, scratch.mobilityMoveBuffer);
        markMobility(index, len, white, scratch);

        return;
      }

    private static void analyzeBishop(GameState gs, int index, boolean white, FeatureScratch scratch)
      {
        int len;
        int dst;
        boolean behindFirstBlocker;

        dst = gs.ul(index);                                         //  UL
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_BISHOP, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_BISHOP, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.ul(dst);
          }
        dst = gs.ur(index);                                         //  UR
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_BISHOP, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_BISHOP, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.ur(dst);
          }
        dst = gs.dr(index);                                         //  DR
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_BISHOP, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_BISHOP, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.dr(dst);
          }
        dst = gs.dl(index);                                         //  DL
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_BISHOP, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_BISHOP, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.dl(dst);
          }

        len = gs.getBishopMoves(index, scratch.mobilityMoveBuffer);
        markMobility(index, len, white, scratch);

        return;
      }

    private static void analyzeRook(GameState gs, int index, boolean white, FeatureScratch scratch)
      {
        int len;
        int dst;
        boolean behindFirstBlocker;

        dst = gs.u(index);                                          //  U
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_ROOK, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_ROOK, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.u(dst);
          }
        dst = gs.d(index);                                          //  D
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_ROOK, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_ROOK, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.d(dst);
          }
        dst = gs.r(index);                                          //  R
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_ROOK, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_ROOK, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.r(dst);
          }
        dst = gs.l(index);                                          //  L
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_ROOK, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_ROOK, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.l(dst);
          }

        len = gs.getRookMoves(index, scratch.mobilityMoveBuffer);
        markMobility(index, len, white, scratch);

        return;
      }

    private static void analyzeQueen(GameState gs, int index, boolean white, FeatureScratch scratch)
      {
        int len;
        int dst;
        boolean behindFirstBlocker;

        dst = gs.u(index);                                          //  U
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.u(dst);
          }
        dst = gs.d(index);                                          //  D
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.d(dst);
          }
        dst = gs.r(index);                                          //  R
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.r(dst);
          }
        dst = gs.l(index);                                          //  L
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.l(dst);
          }
        dst = gs.ul(index);                                         //  UL
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.ul(dst);
          }
        dst = gs.ur(index);                                         //  UR
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.ur(dst);
          }
        dst = gs.dr(index);                                         //  DR
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.dr(dst);
          }
        dst = gs.dl(index);                                         //  DL
        behindFirstBlocker = false;
        while(!gs.oob(dst))
          {
            if(!behindFirstBlocker)
              {
                markAttack(gs, index, dst, white, FeatureSpec.PIECE_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  behindFirstBlocker = true;
              }
            else
              {
                markXray(dst, white, FeatureSpec.XRAY_QUEEN, scratch);
                if(!gs.isEmpty(dst))
                  break;
              }
            dst = gs.dl(dst);
          }

        len = gs.getQueenMoves(index, scratch.mobilityMoveBuffer);
        markMobility(index, len, white, scratch);

        return;
      }

    private static void analyzeKing(GameState gs, int index, boolean white, FeatureScratch scratch)
      {
        int src, dst, candidate;
                                                                    //  Point to white or black buffer.
        byte[] pinRays = white ? scratch.whitePinRays : scratch.blackPinRays;
        Move[] moves = scratch.kingMoveBuffer;
        GameState tmp = scratch.temporaryState;
        int len, i, k;

        //////////////////////////////////////////////////////////////  Determine attacks.
        markAttack(gs, index, gs.u(index), white, FeatureSpec.PIECE_KING, scratch);
        markAttack(gs, index, gs.ur(index), white, FeatureSpec.PIECE_KING, scratch);
        markAttack(gs, index, gs.r(index), white, FeatureSpec.PIECE_KING, scratch);
        markAttack(gs, index, gs.dr(index), white, FeatureSpec.PIECE_KING, scratch);
        markAttack(gs, index, gs.d(index), white, FeatureSpec.PIECE_KING, scratch);
        markAttack(gs, index, gs.dl(index), white, FeatureSpec.PIECE_KING, scratch);
        markAttack(gs, index, gs.l(index), white, FeatureSpec.PIECE_KING, scratch);
        markAttack(gs, index, gs.ul(index), white, FeatureSpec.PIECE_KING, scratch);

        //////////////////////////////////////////////////////////////  Determine pins.
        dst = gs.u(index);                                          //  U: queen, rook
        while(!gs.oob(dst) && gs.isEmpty(dst))
          dst = gs.u(dst);
        candidate = dst;
        if(!gs.oob(candidate) && gs.sameSide(candidate, index))
          {
            dst = gs.u(candidate);
            while(!gs.oob(dst) && gs.isEmpty(dst))
              dst = gs.u(dst);
            if(!gs.oob(dst) && gs.opposed(dst, index) && (gs.isQueen(dst) || gs.isRook(dst)))
              {
                markPinned(candidate, white, scratch);              //  Mark the candidate as pinned.
                src = gs.u(index);                                  //  Mark the pin-ray from after (excluding) the king through the slider.
                while(!gs.oob(src))
                  {
                    pinRays[src] = 1;
                    if(src == dst)
                      break;
                    src = gs.u(src);
                  }
              }
          }

        dst = gs.ur(index);                                         //  UR: queen, bishop
        while(!gs.oob(dst) && gs.isEmpty(dst))
          dst = gs.ur(dst);
        candidate = dst;
        if(!gs.oob(candidate) && gs.sameSide(candidate, index))
          {
            dst = gs.ur(candidate);
            while(!gs.oob(dst) && gs.isEmpty(dst))
              dst = gs.ur(dst);
            if(!gs.oob(dst) && gs.opposed(dst, index) && (gs.isQueen(dst) || gs.isBishop(dst)))
              {
                markPinned(candidate, white, scratch);              //  Mark the candidate as pinned.
                src = gs.ur(index);                                 //  Mark the pin-ray from after (excluding) the king through the slider.
                while(!gs.oob(src))
                  {
                    pinRays[src] = 1;
                    if(src == dst)
                      break;
                    src = gs.ur(src);
                  }
              }
          }

        dst = gs.r(index);                                          //  R: queen, rook
        while(!gs.oob(dst) && gs.isEmpty(dst))
          dst = gs.r(dst);
        candidate = dst;
        if(!gs.oob(candidate) && gs.sameSide(candidate, index))
          {
            dst = gs.r(candidate);
            while(!gs.oob(dst) && gs.isEmpty(dst))
              dst = gs.r(dst);
            if(!gs.oob(dst) && gs.opposed(dst, index) && (gs.isQueen(dst) || gs.isRook(dst)))
              {
                markPinned(candidate, white, scratch);              //  Mark the candidate as pinned.
                src = gs.r(index);                                  //  Mark the pin-ray from after (excluding) the king through the slider.
                while(!gs.oob(src))
                  {
                    pinRays[src] = 1;
                    if(src == dst)
                      break;
                    src = gs.r(src);
                  }
              }
          }

        dst = gs.dr(index);                                         //  DR: queen, bishop
        while(!gs.oob(dst) && gs.isEmpty(dst))
          dst = gs.dr(dst);
        candidate = dst;
        if(!gs.oob(candidate) && gs.sameSide(candidate, index))
          {
            dst = gs.dr(candidate);
            while(!gs.oob(dst) && gs.isEmpty(dst))
              dst = gs.dr(dst);
            if(!gs.oob(dst) && gs.opposed(dst, index) && (gs.isQueen(dst) || gs.isBishop(dst)))
              {
                markPinned(candidate, white, scratch);              //  Mark the candidate as pinned.
                src = gs.dr(index);                                 //  Mark the pin-ray from after (excluding) the king through the slider.
                while(!gs.oob(src))
                  {
                    pinRays[src] = 1;
                    if(src == dst)
                      break;
                    src = gs.dr(src);
                  }
              }
          }

        dst = gs.d(index);                                          //  D: queen, rook
        while(!gs.oob(dst) && gs.isEmpty(dst))
          dst = gs.d(dst);
        candidate = dst;
        if(!gs.oob(candidate) && gs.sameSide(candidate, index))
          {
            dst = gs.d(candidate);
            while(!gs.oob(dst) && gs.isEmpty(dst))
              dst = gs.d(dst);
            if(!gs.oob(dst) && gs.opposed(dst, index) && (gs.isQueen(dst) || gs.isRook(dst)))
              {
                markPinned(candidate, white, scratch);              //  Mark the candidate as pinned.
                src = gs.d(index);                                  //  Mark the pin-ray from after (excluding) the king through the slider.
                while(!gs.oob(src))
                  {
                    pinRays[src] = 1;
                    if(src == dst)
                      break;
                    src = gs.d(src);
                  }
              }
          }

        dst = gs.dl(index);                                         //  DL: queen, bishop
        while(!gs.oob(dst) && gs.isEmpty(dst))
          dst = gs.dl(dst);
        candidate = dst;
        if(!gs.oob(candidate) && gs.sameSide(candidate, index))
          {
            dst = gs.dl(candidate);
            while(!gs.oob(dst) && gs.isEmpty(dst))
              dst = gs.dl(dst);
            if(!gs.oob(dst) && gs.opposed(dst, index) && (gs.isQueen(dst) || gs.isBishop(dst)))
              {
                markPinned(candidate, white, scratch);              //  Mark the candidate as pinned.
                src = gs.dl(index);                                 //  Mark the pin-ray from after (excluding) the king through the slider.
                while(!gs.oob(src))
                  {
                    pinRays[src] = 1;
                    if(src == dst)
                      break;
                    src = gs.dl(src);
                  }
              }
          }

        dst = gs.l(index);                                          //  L: queen, rook
        while(!gs.oob(dst) && gs.isEmpty(dst))
          dst = gs.l(dst);
        candidate = dst;
        if(!gs.oob(candidate) && gs.sameSide(candidate, index))
          {
            dst = gs.l(candidate);
            while(!gs.oob(dst) && gs.isEmpty(dst))
              dst = gs.l(dst);
            if(!gs.oob(dst) && gs.opposed(dst, index) && (gs.isQueen(dst) || gs.isRook(dst)))
              {
                markPinned(candidate, white, scratch);              //  Mark the candidate as pinned.
                src = gs.l(index);                                  //  Mark the pin-ray from after (excluding) the king through the slider.
                while(!gs.oob(src))
                  {
                    pinRays[src] = 1;
                    if(src == dst)
                      break;
                    src = gs.l(src);
                  }
              }
          }

        dst = gs.ul(index);                                         //  UL: queen, bishop
        while(!gs.oob(dst) && gs.isEmpty(dst))
          dst = gs.ul(dst);
        candidate = dst;
        if(!gs.oob(candidate) && gs.sameSide(candidate, index))
          {
            dst = gs.ul(candidate);
            while(!gs.oob(dst) && gs.isEmpty(dst))
              dst = gs.ul(dst);
            if(!gs.oob(dst) && gs.opposed(dst, index) && (gs.isQueen(dst) || gs.isBishop(dst)))
              {
                markPinned(candidate, white, scratch);              //  Mark the candidate as pinned.
                src = gs.ul(index);                                 //  Mark the pin-ray from after (excluding) the king through the slider.
                while(!gs.oob(src))
                  {
                    pinRays[src] = 1;
                    if(src == dst)
                      break;
                    src = gs.ul(src);
                  }
              }
          }

        //////////////////////////////////////////////////////////////  Determine king escapes.
        len = gs.getKingNonCastle(index, moves);
        for(i = 0; i < len; i++)
          {
            tmp.copyFrom(gs);                                       //  Copy the game state.
            tmp.makeMove(moves[i]);                                 //  Apply the candidate move.

            k = tmp.getKingIndex(true);                             //  Locate the white king on the new board.

            if(!tmp.inCheckBy(k, !white))
              markEscape(moves[i].to, white, scratch);
          }

        //////////////////////////////////////////////////////////////  Determine king zones.
        markZone(index, white, scratch);

        if(!gs.oob(gs.u(index)))
          markZone(gs.u(index), white, scratch);
        if(!gs.oob(gs.ur(index)))
          markZone(gs.ur(index), white, scratch);
        if(!gs.oob(gs.r(index)))
          markZone(gs.r(index), white, scratch);
        if(!gs.oob(gs.dr(index)))
          markZone(gs.dr(index), white, scratch);
        if(!gs.oob(gs.d(index)))
          markZone(gs.d(index), white, scratch);
        if(!gs.oob(gs.dl(index)))
          markZone(gs.dl(index), white, scratch);
        if(!gs.oob(gs.l(index)))
          markZone(gs.l(index), white, scratch);
        if(!gs.oob(gs.ul(index)))
          markZone(gs.ul(index), white, scratch);

        //////////////////////////////////////////////////////////////  Determine king mobility.
        len = gs.getKingNonCastle(index, scratch.mobilityMoveBuffer);
        markMobility(index, len, white, scratch);

        return;
      }

    private static void markAttack(GameState gs, int source, int destination, boolean white, int pieceType, FeatureScratch scratch)
      {
        if(gs.oob(destination))
          return;
                                                                    //  Point to white or black buffer.
        byte[] pieceAttacks = white ? scratch.whitePieceAttacks : scratch.blackPieceAttacks;
        int[] attackCounts = white ? scratch.whiteAttackCounts : scratch.blackAttackCounts;
        int attackIndex = FeatureScratch.attackIndex(pieceType, destination);
        pieceAttacks[attackIndex] = 1;

        if(gs.isKing(destination) && gs.opposed(source, destination))
          markChecker(source, gs.isWhite(destination), scratch);

        if(!scratch.destinationSeen[destination])
          {
            scratch.destinationSeen[destination] = true;
            attackCounts[destination]++;
          }

        return;
      }

    private static void markRay(int rayLength, int source, boolean white, int pieceType, GameState gs, FeatureScratch scratch)
      {
        for(int i = 0; i < rayLength; i++)
          markAttack(gs, source, scratch.rayBuffer[i], white, pieceType, scratch);

        return;
      }

    private static void markXray(int destination, boolean white, int xrayType, FeatureScratch scratch)
      {
                                                                    //  Point to white or black buffer.
        byte[] pieceXrays = white ? scratch.whitePieceXrays : scratch.blackPieceXrays;
        pieceXrays[FeatureScratch.pieceXrayIndex(xrayType, destination)] = 1;
        return;
      }

    private static void markPinned(int pinnedSquare, boolean whitePinnedSide, FeatureScratch scratch)
      {
                                                                    //  Point to white or black buffer.
        byte[] pinned = whitePinnedSide ? scratch.whitePinned : scratch.blackPinned;
        pinned[pinnedSquare] = 1;
        return;
      }

    private static void markChecker(int checker, boolean whiteKingChecked, FeatureScratch scratch)
      {
                                                                    //  Point to white or black buffer.
        byte[] checkers = whiteKingChecked ? scratch.whiteKingCheckers : scratch.blackKingCheckers;

        if(checkers[checker] != 0)
          return;

        checkers[checker] = 1;

        if(whiteKingChecked)
          scratch.whiteKingCheckerCount++;
        else
          scratch.blackKingCheckerCount++;

        return;
      }

    private static void markEscape(int escape, boolean whiteKing, FeatureScratch scratch)
      {
                                                                    //  Point to white or black buffer.
        byte[] escapes = whiteKing ? scratch.whiteKingEscapes : scratch.blackKingEscapes;
        escapes[escape] = 1;
        return;
      }

    private static void markZone(int index, boolean whiteKing, FeatureScratch scratch)
      {
                                                                    //  Point to white or black buffer.
        byte[] zone = whiteKing ? scratch.whiteKingZone : scratch.blackKingZone;
        zone[index] = 1;
        return;
      }

    private static void deriveKingPressure(FeatureScratch scratch)
      {
        for(int index = 0; index < FeatureSpec.SQUARES; index++)
          {
            if(scratch.whiteKingZone[index] != 0)
              scratch.whiteKingPressure[index] = scratch.blackAttackCounts[index];

            if(scratch.blackKingZone[index] != 0)
              scratch.blackKingPressure[index] = scratch.whiteAttackCounts[index];
          }

        return;
      }

    private static void markPressure(int index, boolean whiteKing, FeatureScratch scratch)
      {
                                                                    //  Point to white or black buffer.
        int[] pressure = whiteKing ? scratch.whiteKingPressure : scratch.blackKingPressure;
                                                                    //  Note that these are SWAPPED:
                                                                    //  White cares about black's attacks.
                                                                    //  Black cares about white's attacks.
        int[] attackCount = whiteKing ? scratch.blackAttackCounts : scratch.whiteAttackCounts;
        pressure[index] = attackCount[index];
        return;
      }

    private static void markMobility(int index, int count, boolean white, FeatureScratch scratch)
      {
                                                                    //  Point to white or black buffer.
        int[] mobility = white ? scratch.whiteMobility : scratch.blackMobility;
        mobility[index] = count;
        return;
      }

    private static int countDistinctPawnDestinations(GameState gs, Move[] moves, int len, FeatureScratch scratch)
      {
        int count = 0;

        scratch.clearDestinationSeen();

        for(int i = 0; i < len; i++)
          {
            Move move = moves[i];
            if(gs.isEnPassantAttack(move))                          //  En passant has its own feature plane.
              continue;
            if(!scratch.destinationSeen[move.to])
              {
                scratch.destinationSeen[move.to] = true;
                count++;
              }
          }

        return count;
      }

    private static int pieceTypeIndex(GameState gs, int index)
      {
        if(gs.isPawn(index))
          return FeatureSpec.PIECE_PAWN;

        if(gs.isKnight(index))
          return FeatureSpec.PIECE_KNIGHT;

        if(gs.isChampion(index))
          return FeatureSpec.PIECE_CHAMPION;

        if(gs.isWizard(index))
          return FeatureSpec.PIECE_WIZARD;

        if(gs.isBishop(index))
          return FeatureSpec.PIECE_BISHOP;

        if(gs.isRook(index))
          return FeatureSpec.PIECE_ROOK;

        if(gs.isQueen(index))
          return FeatureSpec.PIECE_QUEEN;

        if(gs.isKing(index))
          return FeatureSpec.PIECE_KING;

        return -1;
      }
  }
