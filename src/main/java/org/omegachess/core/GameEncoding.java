package org.omegachess.core;

/* Byte [       0] = Side to move and castling data: [7][6][5][4][3][2][1][0]
                                                      ^  ^  ^  ^  ^  ^  ^  ^
                                                      |  |  |  |  |  |  |  +--- reserved.
                                                      |  |  |  |  |  |  +------ ON: black has castled.
                                                      |  |  |  |  |  +--------- ON: black has queenside privilege.
                                                      |  |  |  |  +------------ ON: black has kingside privilege.
                                                      |  |  |  +--------------- ON: white has castled.
                                                      |  |  +------------------ ON: white has queenside privilege.
                                                      |  +--------------------- ON: white has kingside privilege.
                                                      +------------------------ ON: white to move; OFF: black to move.
   Bytes[  1, 104] = Encoding of the 104 playable squares.
   Byte [     105] = Previous pawn special-move indicator.
   Byte [     106] = Move counter.                    */
public final class GameEncoding
  {
    private static final int FLAGS_INDEX              = 0;
    private static final int BOARD_START_INDEX        = 1;
    private static final int PREVIOUS_PAWN_MOVE_INDEX = 105;
    private static final int MOVE_COUNTER_INDEX       = 106;

    private static final int WHITE_TO_MOVE_MASK       = 0x80;
    private static final int WHITE_KINGSIDE_MASK      = 0x40;
    private static final int WHITE_QUEENSIDE_MASK     = 0x20;
    private static final int WHITE_CASTLED_MASK       = 0x10;
    private static final int BLACK_KINGSIDE_MASK      = 0x08;
    private static final int BLACK_QUEENSIDE_MASK     = 0x04;
    private static final int BLACK_CASTLED_MASK       = 0x02;
    private static final int RESERVED_MASK            = 0x01;

    /* Static utility class. */
    private GameEncoding()
      {
      }

    /* Encode a GameState into a newly allocated 107-byte array. */
    public static byte[] encodeState(GameState state)
      {
        byte[] encoded = new byte[GameState._GAMESTATE_BYTE_SIZE];
        encodeState(state, encoded);
        return encoded;
      }

    /* Encode a GameState into a caller-owned buffer. */
    public static void encodeState(GameState state, byte[] encoded)
      {
        int r, c, index;
        int previousPawnMove, moveCounter;
        int flags = 0;
        int output;

        requireState(state);
        requireStateBuffer(encoded);

        if(state.isWhiteToMove())
          flags |= WHITE_TO_MOVE_MASK;

        if(state.hasWhiteKingsideLiberty())
          flags |= WHITE_KINGSIDE_MASK;

        if(state.hasWhiteQueensideLiberty())
          flags |= WHITE_QUEENSIDE_MASK;

        if(state.hasWhiteCastled())
          flags |= WHITE_CASTLED_MASK;

        if(state.hasBlackKingsideLiberty())
          flags |= BLACK_KINGSIDE_MASK;

        if(state.hasBlackQueensideLiberty())
          flags |= BLACK_QUEENSIDE_MASK;

        if(state.hasBlackCastled())
          flags |= BLACK_CASTLED_MASK;
                                                                    //  Flags may be between 0 and 254.
                                                                    //  Values above 127 appear negative as Java bytes,
                                                                    //  but the stored bit pattern remains correct.
        encoded[FLAGS_INDEX] = (byte)flags;

        output = BOARD_START_INDEX;
                                                                    //  White-side extension squares.
        encoded[output++] = state.pieceAt(0);
        encoded[output++] = state.pieceAt(11);
                                                                    //  Central 10 x 10 board.
        for(r = 1; r <= 10; r++)
          {
            for(c = 1; c <= 10; c++)
              {
                index = r * 12 + c;
                encoded[output++] = state.pieceAt(index);
              }
          }
                                                                    //  Black-side extension squares
        encoded[output++] = state.pieceAt(132);
        encoded[output++] = state.pieceAt(143);

        previousPawnMove = state.getPreviousPawnMove();
        moveCounter = state.getMoveCounter();
        encoded[PREVIOUS_PAWN_MOVE_INDEX] = (byte)previousPawnMove;
        encoded[MOVE_COUNTER_INDEX] = (byte)moveCounter;

        return;
      }

    /* Decode a 107-byte state into a newly allocated GameState. */
    public static GameState decodeState(byte[] encoded)
      {
        GameState state = new GameState();
        decodeState(encoded, state);
        return state;
      }

    /* Decode a 107-byte state into an existing GameState. */
    public static void decodeState(byte[] encoded, GameState state)
      {
        int flags;
        requireStateBuffer(encoded);
        requireState(state);
        flags = encoded[FLAGS_INDEX] & 0xFF;                        //  Convert the signed Java byte into an unsigned integer before applying masks.

        boolean whiteToMove = (flags & WHITE_TO_MOVE_MASK) != 0;
        boolean whiteKingside = (flags & WHITE_KINGSIDE_MASK) != 0;
        boolean whiteQueenside = (flags & WHITE_QUEENSIDE_MASK) != 0;
        boolean whiteCastled = (flags & WHITE_CASTLED_MASK) != 0;
        boolean blackKingside = (flags & BLACK_KINGSIDE_MASK) != 0;
        boolean blackQueenside = (flags & BLACK_QUEENSIDE_MASK) != 0;
        boolean blackCastled = (flags & BLACK_CASTLED_MASK) != 0;
        int previousPawnMove = encoded[PREVIOUS_PAWN_MOVE_INDEX] & 0xFF;
        int moveCounter = encoded[MOVE_COUNTER_INDEX] & 0xFF;

        if(previousPawnMove > 20)                                   //  Force blank.
          previousPawnMove = 0;

        if(moveCounter > 100)                                       //  Force draw.
          moveCounter = 100;

        state.clearForDecoding();                                   //  Clear all 144 internal squares, including the non-playable sentinel border.

        int input = BOARD_START_INDEX;

        state.setPieceFromEncoding(0, checkedPiece(encoded[input++]));
        state.setPieceFromEncoding(11, checkedPiece(encoded[input++]));

        for(int row = 1; row <= 10; row++)
          {
            for(int col = 1; col <= 10; col++)
              {
                int boardIndex = row * 12 + col;
                state.setPieceFromEncoding(boardIndex, checkedPiece(encoded[input++]));
              }
          }

        state.setPieceFromEncoding(132, checkedPiece(encoded[input++]));
        state.setPieceFromEncoding(143, checkedPiece(encoded[input++]));

        if(input != PREVIOUS_PAWN_MOVE_INDEX)
          {
            throw new IllegalStateException(
                "Internal board-decoding length mismatch"
            );
          }

        state.setMetadataFromEncoding(whiteToMove, whiteKingside, whiteQueenside, whiteCastled,
                                                   blackKingside, blackQueenside, blackCastled,
                                      previousPawnMove, moveCounter);
        return;
      }

    /* Encode one move into three bytes:
         byte 0: source square
         byte 1: destination square
         byte 2: promotion code          */
    public static byte[] encodeMove(Move move)
      {
        if(move == null)
          throw new IllegalArgumentException("Move cannot be null");

        if(move.from < 0 || move.from >= GameState._NONE)
          throw new IllegalArgumentException("Invalid move source: " + move.from);

        if(move.to < 0 || move.to >= GameState._NONE)
          throw new IllegalArgumentException("Invalid move destination: " + move.to);

        if(move.promo < GameState._NO_PROMO || move.promo > GameState._PROMO_QUEEN)
          throw new IllegalArgumentException("Invalid promotion code: " + move.promo);

        byte[] encoded = new byte[GameState._MOVE_BYTE_SIZE];
                                                                    //  Square indices 128-143 become negative Java bytes,
                                                                    //  but the underlying eight bits remain intact.
        encoded[0] = (byte)move.from;
        encoded[1] = (byte)move.to;
        encoded[2] = move.promo;

        return encoded;
      }

    public static Move decodeMove(byte[] encoded)
      {
        if(encoded == null || encoded.length != GameState._MOVE_BYTE_SIZE)
          throw new IllegalArgumentException("An encoded move must contain exactly " + GameState._MOVE_BYTE_SIZE + " bytes");

        int from = encoded[0] & 0xFF;
        int to = encoded[1] & 0xFF;
        byte promo = encoded[2];

        if(from >= GameState._NONE)
          throw new IllegalArgumentException("Invalid encoded move source: " + from);

        if(to >= GameState._NONE)
          throw new IllegalArgumentException("Invalid encoded move destination: " + to);

        if(promo < GameState._NO_PROMO || promo > GameState._PROMO_QUEEN)
          throw new IllegalArgumentException("Invalid encoded promotion code: " + (promo & 0xFF));

        return new Move(from, to, promo);
      }

    public static byte[] encodeMoves(Move[] moves, int count)
      {
        int i, output = 0;

        if(moves == null)
          throw new IllegalArgumentException("Move array cannot be null");

        if(count < 0 || count > moves.length)
          throw new IllegalArgumentException("Invalid move count: " + count);

        byte[] encoded = new byte[count * GameState._MOVE_BYTE_SIZE];

        for(i = 0; i < count; i++)
          {
            Move move = moves[i];

            if(move == null)
              throw new IllegalArgumentException("Move " + i + " is null");

            if(move.from < 0 || move.from >= GameState._NONE)
              throw new IllegalArgumentException("Invalid source square in move " + i + ": " + move.from);

            if(move.to < 0 || move.to >= GameState._NONE)
              throw new IllegalArgumentException("Invalid destination square in move "+ i + ": " + move.to);

            if(move.promo < GameState._NO_PROMO || move.promo > GameState._PROMO_QUEEN)
              throw new IllegalArgumentException("Invalid promotion code in move " + i + ": " + (move.promo & 0xFF));

            encoded[output++] = (byte)move.from;
            encoded[output++] = (byte)move.to;
            encoded[output++] = move.promo;
          }

        return encoded;
      }

    public static int decodeMoves(byte[] encoded, Move[] moves)
      {
        int input = 0, count, i;
        int from, to;
        byte promo;

        if(encoded == null)
          throw new IllegalArgumentException("Encoded move array cannot be null");

        if(moves == null)
          throw new IllegalArgumentException("Move output array cannot be null");

        if(encoded.length % GameState._MOVE_BYTE_SIZE != 0)
          throw new IllegalArgumentException("Encoded move array length must be divisible by " + GameState._MOVE_BYTE_SIZE);

        count = encoded.length / GameState._MOVE_BYTE_SIZE;

        if(count > moves.length)
          throw new IllegalArgumentException("Move output buffer is too small");

        for(i = 0; i < count; i++)
          {
            from = encoded[input++] & 0xFF;
            to = encoded[input++] & 0xFF;
            promo = encoded[input++];

            if(from >= GameState._NONE || to >= GameState._NONE)
              throw new IllegalArgumentException("Invalid encoded move at index " + i);

            moves[i] = new Move(from, to, promo);
          }

        return count;
      }

    private static byte checkedPiece(byte encodedPiece)
      {
        int piece = encodedPiece & 0xFF;

        if(piece > GameState._BLACK_KING)
          throw new IllegalArgumentException("Invalid encoded piece value: " + piece);

        return encodedPiece;
      }

    private static void requireState(GameState state)
      {
        if(state == null)
          throw new IllegalArgumentException("GameState cannot be null");
      }

    private static void requireStateBuffer(byte[] encoded)
      {
        if(encoded == null || encoded.length != GameState._GAMESTATE_BYTE_SIZE)
          throw new IllegalArgumentException("An encoded GameState must contain exactly " + GameState._GAMESTATE_BYTE_SIZE + " bytes");
      }
  }