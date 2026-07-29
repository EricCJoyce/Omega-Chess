package org.omegachess.wasm;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.omegachess.core.GameEncoding;
import org.omegachess.core.GameLogic;
import org.omegachess.core.GameState;
import org.omegachess.core.Move;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSBuffer;
import org.teavm.jso.JSBufferType;
import org.teavm.jso.JSExport;

public final class GameLogicWasm
  {
    private static final GameState CURRENT_STATE = new GameState(); //  Java object used by game logic.
                                                                    //  107-byte linear-memory encoding visible to JavaScript.
    private static final ByteBuffer CURRENT_STATE_BUFFER = ByteBuffer.allocateDirect(GameState._GAMESTATE_BYTE_SIZE);

    private static final ByteBuffer MOVES_BUFFER = ByteBuffer.allocateDirect(GameState._MAX_NUM_TARGETS);
    private static final byte[] STATE_SCRATCH = new byte[GameState._GAMESTATE_BYTE_SIZE];
    private static final Move[] INDEX_MOVE_SCRATCH = new Move[GameState._MAX_NUM_TARGETS];
    private static final Move[] LEGAL_MOVE_SCRATCH = new Move[GameState._MAX_MOVES];

    private static final boolean[] TARGET_SEEN = new boolean[GameState._NONE];

    @JSBody(params = { "currentState", "movesBuffer" }, script = "globalThis.omegaChessInstallBuffers(currentState, movesBuffer);")
    private static native void installBuffers(@JSBuffer(JSBufferType.UINT8)Buffer currentState,
                                              @JSBuffer(JSBufferType.UINT8)Buffer movesBuffer);

    private GameLogicWasm()
      {
      }

    public static void main(String[] args)
      {
      }

    @JSExport
    public static byte[] startpos_client()
      {
        GameState state = GameLogic.createStartingPosition();
        return GameEncoding.encodeState(state);
      }

    @JSExport
    public static byte[] legalMoves(byte[] encodedState)
      {
        GameState state = GameEncoding.decodeState(encodedState);
        int count = GameLogic.legalMoves(state, LEGAL_MOVE_SCRATCH);
        return GameEncoding.encodeMoves(LEGAL_MOVE_SCRATCH, count);
      }

    @JSExport
    public static void bindBuffers_client()
      {
        writeCurrentStateBuffer();
        installBuffers(CURRENT_STATE_BUFFER, MOVES_BUFFER);
        return;
      }

    @JSExport
    public static void loadCurrentState_client()
      {
        CURRENT_STATE_BUFFER.position(0);
        CURRENT_STATE_BUFFER.get(STATE_SCRATCH, 0, GameState._GAMESTATE_BYTE_SIZE);
        CURRENT_STATE_BUFFER.position(0);
        GameEncoding.decodeState(STATE_SCRATCH, CURRENT_STATE);
        return;
      }

    @JSExport
    public static int sideToMove_client()
      {
        return CURRENT_STATE.isWhiteToMove() ? GameState._WHITE_TO_MOVE : GameState._BLACK_TO_MOVE;
      }

    @JSExport
    public static boolean isWhite_client(int index)
      {
        return CURRENT_STATE.isWhite(index);
      }

    @JSExport
    public static boolean isBlack_client(int index)
      {
        return CURRENT_STATE.isBlack(index);
      }

    @JSExport
    public static boolean isEmpty_client(int index)
      {
        return CURRENT_STATE.isEmpty(index);
      }

    @JSExport
    public static boolean isPawn_client(int index)
      {
        return CURRENT_STATE.isPawn(index);
      }

    @JSExport
    public static boolean isKnight_client(int index)
      {
        return CURRENT_STATE.isKnight(index);
      }

    @JSExport
    public static boolean isChampion_client(int index)
      {
        return CURRENT_STATE.isChampion(index);
      }

    @JSExport
    public static boolean isWizard_client(int index)
      {
        return CURRENT_STATE.isWizard(index);
      }

    @JSExport
    public static boolean isBishop_client(int index)
      {
        return CURRENT_STATE.isBishop(index);
      }

    @JSExport
    public static boolean isRook_client(int index)
      {
        return CURRENT_STATE.isRook(index);
      }

    @JSExport
    public static boolean isQueen_client(int index)
      {
        return CURRENT_STATE.isQueen(index);
      }

    @JSExport
    public static boolean isKing_client(int index)
      {
        return CURRENT_STATE.isKing(index);
      }

    @JSExport
    public static boolean whiteKingsidePrivilege_client()
      {
        return CURRENT_STATE.hasWhiteKingsideLiberty();
      }

    @JSExport
    public static boolean whiteQueensidePrivilege_client()
      {
        return CURRENT_STATE.hasWhiteQueensideLiberty();
      }

    @JSExport
    public static boolean whiteCastled_client()
      {
        return CURRENT_STATE.hasWhiteCastled();
      }

    @JSExport
    public static boolean blackKingsidePrivilege_client()
      {
        return CURRENT_STATE.hasBlackKingsideLiberty();
      }

    @JSExport
    public static boolean blackQueensidePrivilege_client()
      {
        return CURRENT_STATE.hasBlackQueensideLiberty();
      }

    @JSExport
    public static boolean blackCastled_client()
      {
        return CURRENT_STATE.hasBlackCastled();
      }

    @JSExport
    public static boolean isWhiteKingsideCastle_client(int from, int to)
      {
        return CURRENT_STATE.isWhiteKingside(from, to);
      }

    @JSExport
    public static boolean isWhiteQueensideCastle_client(int from, int to)
      {
        return CURRENT_STATE.isWhiteQueenside(from, to);
      }

    @JSExport
    public static boolean isBlackKingsideCastle_client(int from, int to)
      {
        return CURRENT_STATE.isBlackKingside(from, to);
      }

    @JSExport
    public static boolean isBlackQueensideCastle_client(int from, int to)
      {
        return CURRENT_STATE.isBlackQueenside(from, to);
      }

    @JSExport
    public static boolean isEnPassantAttack_client(int from, int to)
      {
        return CURRENT_STATE.isEnPassantAttack(from, to);
      }

    @JSExport
    public static int enPassantVictim_client(int from, int to)
      {
        return CURRENT_STATE.enPassantVictim(from, to);
      }

    @JSExport
    public static int row_client(int index)
      {
        return CURRENT_STATE.row(index);
      }

    @JSExport
    public static int getMovesIndex_client(int index)
      {
        int i;
        int moveCount, targetCount, target;

        for(i = 0; i < TARGET_SEEN.length; i++)
          TARGET_SEEN[i] = false;

        moveCount = CURRENT_STATE.getMovesIndex(index, INDEX_MOVE_SCRATCH);
        targetCount = 0;

        for(i = 0; i < moveCount; i++)
          {
            target = INDEX_MOVE_SCRATCH[i].to;

            if(!TARGET_SEEN[target])
              {
                TARGET_SEEN[target] = true;
                MOVES_BUFFER.put(targetCount, (byte)target);
                targetCount++;
              }
          }

        return targetCount;
      }

    @JSExport
    public static void makeMove_client(int from, int to, int promo)
      {
        if(promo < GameState._NO_PROMO || promo > GameState._PROMO_QUEEN)
          throw new IllegalArgumentException("Invalid promotion code: " + promo);

        Move requested = new Move(from, to, (byte) promo);
        boolean applied = GameLogic.applyLegalMove(CURRENT_STATE, requested, LEGAL_MOVE_SCRATCH);

        if(!applied)
          throw new IllegalArgumentException("Illegal Omega Chess move: "+ from + " -> " + to + " promo " + promo);

        writeCurrentStateBuffer();
      }

    private static void writeCurrentStateBuffer()
      {
        GameEncoding.encodeState(CURRENT_STATE, STATE_SCRATCH);
        CURRENT_STATE_BUFFER.position(0);
        CURRENT_STATE_BUFFER.put(STATE_SCRATCH, 0, GameState._GAMESTATE_BYTE_SIZE);
        CURRENT_STATE_BUFFER.position(0);
        return;
      }

    @JSExport
    public static boolean isTerminal_client()
      {
        return CURRENT_STATE.terminal();
      }

    @JSExport
    public static int isWin_client()
      {
        return CURRENT_STATE.isWin();
      }

    @JSExport
    public static void draw()
      {
        int i, prevPawn;

        System.out.print(CURRENT_STATE.symbol(132) + " ");          //  Topmost row, the two wizard corners.
        for(i = 133; i < 143; i++)
          System.out.print("  ");
        System.out.print(CURRENT_STATE.symbol(143) + " \n");

        System.out.print("  ");                                     //  Topmost full row.
        for(i = 121; i < 131; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print("  ");
        for(i = 109; i < 119; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print("  ");
        for(i = 97; i < 107; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print("  ");
        for(i = 85; i < 95; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print("  ");
        for(i = 73; i < 83; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print("  ");
        for(i = 61; i < 71; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print("  ");
        for(i = 49; i < 59; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print("  ");
        for(i = 37; i < 47; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print("  ");
        for(i = 25; i < 35; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print("  ");                                     //  Bottommost full row.
        for(i = 13; i < 23; i++)
          System.out.print(CURRENT_STATE.symbol(i) + "  ");
        System.out.print("  \n");

        System.out.print(CURRENT_STATE.symbol(0) + " ");            //  Bottommost row, the two wizard corners.
        for(i = 1; i < 11; i++)
          System.out.print("  ");
        System.out.print(CURRENT_STATE.symbol(11) + " \n");

        if(CURRENT_STATE.isWhiteToMove())
          System.out.println("White to move");
        else
          System.out.println("Black to move");

        prevPawn = CURRENT_STATE.getPreviousPawnMove();
        if(prevPawn > 0)
          {
            if(prevPawn <= 10)
              System.out.print("Previous pawn double move on column ");
            else
              System.out.print("Previous pawn triple move on column ");
            switch(prevPawn)
              {
                case 1:
                case 11:   System.out.println("A");  break;
                case 2:
                case 12:   System.out.println("B");  break;
                case 3:
                case 13:   System.out.println("C");  break;
                case 4:
                case 14:   System.out.println("D");  break;
                case 5:
                case 15:   System.out.println("E");  break;
                case 6:
                case 16:   System.out.println("F");  break;
                case 7:
                case 17:   System.out.println("G");  break;
                case 8:
                case 18:   System.out.println("H");  break;
                case 9:
                case 19:   System.out.println("I");  break;
                case 10:
                case 20:   System.out.println("J");  break;
              }
          }

        System.out.println("Move counter: " + CURRENT_STATE.getMoveCounter());

        return;
      }
  }