package org.omegachess.core.features;

import java.util.Arrays;

import org.omegachess.core.GameState;
import org.omegachess.core.Move;

public final class FeatureEncoding
  {
    private FeatureEncoding()
      {
      }

    public static void encode(GameState gs, float[] buffer, FeatureScratch scratch)
      {
        validateArguments(gs, buffer, scratch);
        clear(buffer);

        writeValidMask(gs, buffer);
        writeOccupancy(gs, buffer);

        TacticalAnalyzer.analyze(gs, scratch);

        writeAttacks(gs, scratch, buffer);
        writeXrays(gs, scratch, buffer);
        writePins(gs, scratch, buffer);
        writeKingFeatures(gs, scratch, buffer);
        writeMobility(gs, scratch, buffer);
        writeDefended(gs, scratch, buffer);
        writeUndefendedAndAttacked(gs, scratch, buffer);
        writeStates(gs, scratch, buffer);
      }

    private static void writeValidMask(GameState gs, float[] buffer)
      {
        int boardIndex, tensorIndex;

        for(boardIndex = 0; boardIndex < GameState._NONE; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;
                                                                    //  Orientation does not materially change this symmetric mask,
                                                                    //  but applying it keeps the indexing convention explicit.
            tensorIndex = orientIndex(boardIndex, gs.isWhiteToMove());
            buffer[FeatureSpec.index(FeatureSpec.VALID_MASK, tensorIndex)] = 1.0f;
          }

        return;
      }

    private static void writeOccupancy(GameState gs, float[] buffer)
      {
        boolean ownPiece;
        boolean whiteToMove = gs.isWhiteToMove();

        byte piece;

        int boardIndex;
        int tensorIndex;
        int pieceType;
        int plane;

        for(boardIndex = 0; boardIndex < GameState._NONE; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;

            piece = gs.pieceAt(boardIndex);

            if(piece == GameState._EMPTY)
              continue;

            pieceType = pieceTypeIndex(piece);

            if(pieceType < 0)
              throw new IllegalStateException("Unknown piece code " + piece + " at board index " + boardIndex + ".");

            ownPiece = whiteToMove ? gs.isWhite(boardIndex) : gs.isBlack(boardIndex);
            plane = (ownPiece ? FeatureSpec.OWN_OCCUPANCY_BASE : FeatureSpec.ENEMY_OCCUPANCY_BASE) + pieceType;

            tensorIndex = orientIndex(boardIndex, whiteToMove);

            buffer[FeatureSpec.index(plane, tensorIndex)] = 1.0f;
          }

        return;
      }

    private static void writeAttacks(GameState gs, FeatureScratch scratch, float[] buffer)
      {
        boolean whiteToMove = gs.isWhiteToMove();

        byte[] ownPieceAttacks = whiteToMove ? scratch.whitePieceAttacks : scratch.blackPieceAttacks;
        byte[] enemyPieceAttacks = whiteToMove ? scratch.blackPieceAttacks : scratch.whitePieceAttacks;
        int[] ownAttackCounts = whiteToMove ? scratch.whiteAttackCounts : scratch.blackAttackCounts;
        int[] enemyAttackCounts = whiteToMove ? scratch.blackAttackCounts : scratch.whiteAttackCounts;

        for(int boardIndex = 0; boardIndex < FeatureSpec.SQUARES; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;

            int tensorIndex = orientIndex(boardIndex, whiteToMove);

            for(int pieceType = 0; pieceType < FeatureSpec.PIECE_TYPES; pieceType++)
              {
                int scratchIndex = FeatureScratch.attackIndex(pieceType, boardIndex);
                int ownPlane = FeatureSpec.OWN_ATTACK_BASE + pieceType;
                int enemyPlane = FeatureSpec.ENEMY_ATTACK_BASE + pieceType;

                buffer[FeatureSpec.index(ownPlane, tensorIndex)] = ownPieceAttacks[scratchIndex];
                buffer[FeatureSpec.index(enemyPlane, tensorIndex)] = enemyPieceAttacks[scratchIndex];
              }

            buffer[FeatureSpec.index(FeatureSpec.OWN_ATTACK_COUNT, tensorIndex)] = ownAttackCounts[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.ENEMY_ATTACK_COUNT, tensorIndex)] = enemyAttackCounts[boardIndex];
          }

        return;
      }

    private static void writeXrays(GameState gs, FeatureScratch scratch, float[] buffer)
      {
        boolean whiteToMove = gs.isWhiteToMove();
        byte[] ownXrays = whiteToMove ? scratch.whitePieceXrays : scratch.blackPieceXrays;
        byte[] enemyXrays = whiteToMove ? scratch.blackPieceXrays : scratch.whitePieceXrays;
        int boardIndex, tensorIndex, xrayType, scratchIndex;

        for(boardIndex = 0; boardIndex < FeatureSpec.SQUARES; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;

            tensorIndex = orientIndex(boardIndex, whiteToMove);

            for(xrayType = 0; xrayType < FeatureSpec.XRAY_TYPES; xrayType++)
              {
                scratchIndex = FeatureScratch.pieceXrayIndex(xrayType, boardIndex);
                buffer[FeatureSpec.index(FeatureSpec.OWN_XRAY_BASE + xrayType, tensorIndex)] = ownXrays[scratchIndex];
                buffer[FeatureSpec.index(FeatureSpec.ENEMY_XRAY_BASE + xrayType, tensorIndex)] = enemyXrays[scratchIndex];
              }
          }

        return;
      }

    private static void writePins(GameState gs, FeatureScratch scratch, float[] buffer)
      {
        int boardIndex, tensorIndex;
        boolean whiteToMove = gs.isWhiteToMove();

        byte[] ownPinned = whiteToMove ? scratch.whitePinned : scratch.blackPinned;
        byte[] enemyPinned = whiteToMove ? scratch.blackPinned : scratch.whitePinned;
        byte[] ownPinRays = whiteToMove ? scratch.whitePinRays : scratch.blackPinRays;
        byte[] enemyPinRays = whiteToMove ? scratch.blackPinRays : scratch.whitePinRays;

        for(boardIndex = 0; boardIndex < FeatureSpec.SQUARES; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;

            tensorIndex = orientIndex(boardIndex, whiteToMove);
            buffer[FeatureSpec.index(FeatureSpec.OWN_PINNED, tensorIndex)] = ownPinned[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.ENEMY_PINNED, tensorIndex)] = enemyPinned[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.OWN_PIN_RAYS, tensorIndex)] = ownPinRays[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.ENEMY_PIN_RAYS, tensorIndex)] = enemyPinRays[boardIndex];
          }

        return;
      }

    private static void writeKingFeatures(GameState gs, FeatureScratch scratch, float[] buffer)
      {
        int boardIndex, tensorIndex;
        boolean whiteToMove = gs.isWhiteToMove();
                                                                    //  Checkers and escapes concern only the side-to-move King.
        byte[] checkers = whiteToMove ? scratch.whiteKingCheckers : scratch.blackKingCheckers;
        byte[] kingEscapes = whiteToMove ? scratch.whiteKingEscapes : scratch.blackKingEscapes;
                                                                    //  Zones and pressure are represented for both sides.
        byte[] ownKingZone = whiteToMove ? scratch.whiteKingZone : scratch.blackKingZone;
        byte[] enemyKingZone = whiteToMove ? scratch.blackKingZone : scratch.whiteKingZone;
        int[] ownKingPressure = whiteToMove ? scratch.whiteKingPressure : scratch.blackKingPressure;
        int[] enemyKingPressure = whiteToMove ? scratch.blackKingPressure : scratch.whiteKingPressure;

        for(boardIndex = 0; boardIndex < FeatureSpec.SQUARES; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;

            tensorIndex = orientIndex(boardIndex, whiteToMove);

            buffer[FeatureSpec.index(FeatureSpec.CHECKERS, tensorIndex)] = checkers[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.KING_ESCAPES, tensorIndex)] = kingEscapes[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.OWN_KING_ZONE, tensorIndex)] = ownKingZone[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.ENEMY_KING_ZONE, tensorIndex)] = enemyKingZone[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.OWN_KING_PRESSURE, tensorIndex)] = ownKingPressure[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.ENEMY_KING_PRESSURE, tensorIndex)] = enemyKingPressure[boardIndex];
          }

        return;
      }

    private static void writeMobility(GameState gs, FeatureScratch scratch, float[] buffer)
      {
        int boardIndex, tensorIndex;
        boolean whiteToMove = gs.isWhiteToMove();
        int[] ownMobility = whiteToMove ? scratch.whiteMobility : scratch.blackMobility;
        int[] enemyMobility =whiteToMove ? scratch.blackMobility : scratch.whiteMobility;

        for(boardIndex = 0; boardIndex < FeatureSpec.SQUARES; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;

            tensorIndex = orientIndex(boardIndex, whiteToMove);
            buffer[FeatureSpec.index(FeatureSpec.OWN_MOBILITY, tensorIndex)] = ownMobility[boardIndex];
            buffer[FeatureSpec.index(FeatureSpec.ENEMY_MOBILITY, tensorIndex)] = enemyMobility[boardIndex];
          }

        return;
      }

    private static void writeDefended(GameState gs, FeatureScratch scratch, float[] buffer)
      {
        int boardIndex, tensorIndex;
        boolean whiteDefended, blackDefended;
        float ownValue, enemyValue;
        boolean whiteToMove = gs.isWhiteToMove();

        for(boardIndex = 0; boardIndex < FeatureSpec.SQUARES; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;

            whiteDefended = gs.isWhite(boardIndex) && scratch.whiteAttackCounts[boardIndex] > 0;
            blackDefended = gs.isBlack(boardIndex) && scratch.blackAttackCounts[boardIndex] > 0;

            ownValue = whiteToMove ? (whiteDefended ? 1.0f : 0.0f) : (blackDefended ? 1.0f : 0.0f);
            enemyValue = whiteToMove ? (blackDefended ? 1.0f : 0.0f) : (whiteDefended ? 1.0f : 0.0f);

            tensorIndex = orientIndex(boardIndex, whiteToMove);
            buffer[FeatureSpec.index(FeatureSpec.OWN_DEFENDED, tensorIndex)] = ownValue;
            buffer[FeatureSpec.index(FeatureSpec.ENEMY_DEFENDED, tensorIndex)] = enemyValue;
          }

        return;
      }

    private static void writeUndefendedAndAttacked(GameState gs, FeatureScratch scratch, float[] buffer)
      {
        int boardIndex, tensorIndex;
        boolean whiteToMove = gs.isWhiteToMove();
        boolean whiteUndefendedAndAttacked, blackUndefendedAndAttacked;
        float ownValue, enemyValue;

        for(boardIndex = 0; boardIndex < FeatureSpec.SQUARES; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;
                                                                    //  Exclude kings because other planes already encode their danger.
            whiteUndefendedAndAttacked = gs.isWhite(boardIndex) && !gs.isKing(boardIndex) &&
                                         scratch.blackAttackCounts[boardIndex] > 0 && scratch.whiteAttackCounts[boardIndex] == 0;
            blackUndefendedAndAttacked = gs.isBlack(boardIndex) && !gs.isKing(boardIndex) &&
                                         scratch.whiteAttackCounts[boardIndex] > 0 && scratch.blackAttackCounts[boardIndex] == 0;


            if(whiteToMove)
              {
                ownValue = whiteUndefendedAndAttacked ? 1.0f : 0.0f;
                enemyValue = blackUndefendedAndAttacked ? 1.0f : 0.0f;
              }
            else
              {
                ownValue = blackUndefendedAndAttacked ? 1.0f : 0.0f;
                enemyValue = whiteUndefendedAndAttacked ? 1.0f : 0.0f;
              }

            tensorIndex = orientIndex(boardIndex, whiteToMove);
            buffer[FeatureSpec.index(FeatureSpec.OWN_UNDEFENDED_ATTACKED, tensorIndex)] = ownValue;
            buffer[FeatureSpec.index(FeatureSpec.ENEMY_UNDEFENDED_ATTACKED, tensorIndex)] = enemyValue;
          }

        return;
      }

    private static void writeStates(GameState gs, FeatureScratch scratch, float[] buffer)
      {
        boolean whiteToMove = gs.isWhiteToMove();

        float ownKingside = whiteToMove ? (gs.hasWhiteKingsideLiberty() ? 1.0f : 0.0f) : (gs.hasBlackKingsideLiberty() ? 1.0f : 0.0f);
        float ownQueenside = whiteToMove ? (gs.hasWhiteQueensideLiberty() ? 1.0f : 0.0f) : (gs.hasBlackQueensideLiberty() ? 1.0f : 0.0f);

        float enemyKingside = whiteToMove ? (gs.hasBlackKingsideLiberty() ? 1.0f : 0.0f) : (gs.hasWhiteKingsideLiberty() ? 1.0f : 0.0f);
        float enemyQueenside = whiteToMove ? (gs.hasBlackQueensideLiberty() ? 1.0f : 0.0f) : (gs.hasWhiteQueensideLiberty() ? 1.0f : 0.0f);

        float moveCounter = gs.getMoveCounter() / 255.0f;
        int checkerCount = whiteToMove ? scratch.whiteKingCheckerCount : scratch.blackKingCheckerCount;
        int boardIndex, tensorIndex;

        for(boardIndex = 0; boardIndex < FeatureSpec.SQUARES; boardIndex++)
          {
            if(gs.oob(boardIndex))
              continue;

            tensorIndex = orientIndex(boardIndex, whiteToMove);
            buffer[FeatureSpec.index(FeatureSpec.OWN_CASTLE_KINGSIDE, tensorIndex)] = ownKingside;
            buffer[FeatureSpec.index(FeatureSpec.OWN_CASTLE_QUEENSIDE, tensorIndex)] = ownQueenside;

            buffer[FeatureSpec.index(FeatureSpec.ENEMY_CASTLE_KINGSIDE, tensorIndex)] = enemyKingside;
            buffer[FeatureSpec.index(FeatureSpec.ENEMY_CASTLE_QUEENSIDE, tensorIndex)] = enemyQueenside;

            buffer[FeatureSpec.index(FeatureSpec.MOVE_COUNTER, tensorIndex)] = moveCounter;
            buffer[FeatureSpec.index(FeatureSpec.CHECKER_COUNT, tensorIndex)] = checkerCount;
          }

        writeEnPassant(gs, buffer);
        return;
      }

    private static void writeEnPassant(GameState gs, float[] buffer)
      {
        boolean whiteToMove = gs.isWhiteToMove();
        Move[] moves = new Move[GameState._MAX_NUM_TARGETS];
        int src, len, i;
        int tensorIndex;

        for(src = 0; src < FeatureSpec.SQUARES; src++)
          {
            if(gs.oob(src) || !gs.isPawn(src) || (whiteToMove ? !gs.isWhite(src) : !gs.isBlack(src)))
              continue;

            len = gs.getPawnMoves(src, moves);

            for(i = 0; i < len; i++)
              {
                if(!gs.isEnPassantAttack(moves[i]))
                  continue;

                tensorIndex = orientIndex(moves[i].to, whiteToMove);
                buffer[FeatureSpec.index(FeatureSpec.EN_PASSANT, tensorIndex)] = 1.0f;
              }
          }

        return;
      }

    /* Rotate the board 180 degrees when Black is the relative "own" side. */
    private static int orientIndex(int index, boolean whiteToMove)
      {
        if(index < 0 || index >= FeatureSpec.SQUARES)
          return GameState._NONE;

        if(whiteToMove)
          return index;

        return FeatureSpec.SQUARES - 1 - index;
      }

    private static int pieceTypeIndex(byte piece)
      {
        switch(piece)
          {
            case GameState._WHITE_PAWN:
            case GameState._BLACK_PAWN:
              return FeatureSpec.PIECE_PAWN;

            case GameState._WHITE_KNIGHT:
            case GameState._BLACK_KNIGHT:
              return FeatureSpec.PIECE_KNIGHT;

            case GameState._WHITE_CHAMPION:
            case GameState._BLACK_CHAMPION:
              return FeatureSpec.PIECE_CHAMPION;

            case GameState._WHITE_WIZARD:
            case GameState._BLACK_WIZARD:
              return FeatureSpec.PIECE_WIZARD;

            case GameState._WHITE_BISHOP:
            case GameState._BLACK_BISHOP:
              return FeatureSpec.PIECE_BISHOP;

            case GameState._WHITE_ROOK:
            case GameState._BLACK_ROOK:
              return FeatureSpec.PIECE_ROOK;

            case GameState._WHITE_QUEEN:
            case GameState._BLACK_QUEEN:
              return FeatureSpec.PIECE_QUEEN;

            case GameState._WHITE_KING:
            case GameState._BLACK_KING:
              return FeatureSpec.PIECE_KING;

            default:
              return -1;
          }
      }

    private static void validateArguments(GameState gs, float[] buffer, FeatureScratch scratch)
      {
        if(gs == null)
          throw new IllegalArgumentException("GameState must not be null.");

        if(scratch == null)
          throw new IllegalArgumentException("FeatureScratch must not be null.");

        FeatureSpec.validateBuffer(buffer);

        if(GameState._NONE != FeatureSpec.SQUARES)
          throw new IllegalStateException("GameState and FeatureSpec disagree about board size.");

        return;
      }

    private static void clear(float[] buffer)
      {
        Arrays.fill(buffer, 0, FeatureSpec.FEATURE_COUNT, 0.0f);
        return;
      }
  }