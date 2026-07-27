package org.omegachess.core.features;

import java.util.Arrays;

import org.omegachess.core.GameState;
import org.omegachess.core.Move;


/* Hold preallocated working arrays. */
public final class FeatureScratch
  {
    public final byte[] whitePieceAttacks = new byte[FeatureSpec.PIECE_TYPES * FeatureSpec.SQUARES];
    public final byte[] blackPieceAttacks = new byte[FeatureSpec.PIECE_TYPES * FeatureSpec.SQUARES];

    public final int[] whiteAttackCounts = new int[FeatureSpec.SQUARES];
    public final int[] blackAttackCounts = new int[FeatureSpec.SQUARES];

    public final boolean[] destinationSeen = new boolean[FeatureSpec.SQUARES];
    public final int[] rayBuffer = new int[FeatureSpec.SQUARES];

    public final byte[] whitePieceXrays = new byte[FeatureSpec.XRAY_TYPES * FeatureSpec.SQUARES];
    public final byte[] blackPieceXrays = new byte[FeatureSpec.XRAY_TYPES * FeatureSpec.SQUARES];
                                                                    //  Color refers to the PINNED SIDE--not the attacking slider.
    public final byte[] whitePinned = new byte[FeatureSpec.SQUARES];
    public final byte[] blackPinned = new byte[FeatureSpec.SQUARES];
    public final byte[] whitePinRays = new byte[FeatureSpec.SQUARES];
    public final byte[] blackPinRays = new byte[FeatureSpec.SQUARES];
                                                                    //  Color refers to the CHECKED KING--not the pieces giving check to the king.
    public final byte[] whiteKingCheckers = new byte[FeatureSpec.SQUARES];
    public final byte[] blackKingCheckers = new byte[FeatureSpec.SQUARES];
    public int whiteKingCheckerCount;
    public int blackKingCheckerCount;

    public final byte[] whiteKingEscapes = new byte[FeatureSpec.SQUARES];
    public final byte[] blackKingEscapes = new byte[FeatureSpec.SQUARES];

    public final byte[] whiteKingZone = new byte[FeatureSpec.SQUARES];
    public final byte[] blackKingZone = new byte[FeatureSpec.SQUARES];

    public final Move[] kingMoveBuffer = new Move[8];
    public final GameState temporaryState = new GameState();

    public final int[] whiteKingPressure = new int[FeatureSpec.SQUARES];
    public final int[] blackKingPressure = new int[FeatureSpec.SQUARES];

    public final int[] whiteMobility = new int[FeatureSpec.SQUARES];
    public final int[] blackMobility = new int[FeatureSpec.SQUARES];
    public final Move[] mobilityMoveBuffer = new Move[GameState._MAX_NUM_TARGETS];

    public void clear()
      {
        Arrays.fill(whitePieceAttacks, (byte)0);
        Arrays.fill(blackPieceAttacks, (byte)0);
        Arrays.fill(whiteAttackCounts, 0);
        Arrays.fill(blackAttackCounts, 0);
        Arrays.fill(whitePieceXrays, (byte)0);
        Arrays.fill(blackPieceXrays, (byte)0);
        Arrays.fill(whitePinned, (byte)0);
        Arrays.fill(blackPinned, (byte)0);
        Arrays.fill(whitePinRays, (byte)0);
        Arrays.fill(blackPinRays, (byte)0);
        Arrays.fill(whiteKingCheckers, (byte)0);
        Arrays.fill(blackKingCheckers, (byte)0);
        Arrays.fill(whiteKingEscapes, (byte)0);
        Arrays.fill(blackKingEscapes, (byte)0);
        Arrays.fill(whiteKingZone, (byte)0);
        Arrays.fill(blackKingZone, (byte)0);
        Arrays.fill(whiteKingPressure, 0);
        Arrays.fill(blackKingPressure, 0);
        Arrays.fill(whiteMobility, 0);
        Arrays.fill(blackMobility, 0);

        return;
      }

    public static int attackIndex(int pieceType, int boardIndex)
      {
        return pieceType * FeatureSpec.SQUARES + boardIndex;
      }

    public static int pieceXrayIndex(int xrayType, int boardIndex)
      {
        return xrayType * FeatureSpec.SQUARES + boardIndex;
      }

    public void clearDestinationSeen()
      {
        Arrays.fill(destinationSeen, false);
        return;
      }
  }