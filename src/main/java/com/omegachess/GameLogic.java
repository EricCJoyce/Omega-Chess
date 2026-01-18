package com.omegachess;

import org.teavm.jso.export.JSExport;

public final class GameLogicModule
  {
    private GameLogicModule() {}

    private static final int MOVES_MAX = 64;
    private static final int MOVES_BYTES = MOVES_MAX * 3;

    // Choose a safe temporary cap until you finalize encoding.
    private static final int GAMESTATE_CAP = 256;

    private static int GAMESTATE_BYTE_SIZE = 0; // set by client once known
    private static final byte[] currentState = new byte[GAMESTATE_CAP];
    private static final byte[] movesBuffer = new byte[MOVES_BYTES];
    private static int movesCount = 0;

    // ---- Buffer plumbing (ABI helpers) ----
    @JSExport public static void setGameStateSize(int size)
      {
        if(size < 0)
          size = 0;
        if(size > GAMESTATE_CAP)
          size = GAMESTATE_CAP;
        GAMESTATE_BYTE_SIZE = size;
      }

    @JSExport public static int getGameStateSize()
      {
        return GAMESTATE_BYTE_SIZE;
      }

    @JSExport public static int getMovesMax()
      {
        return MOVES_MAX;
      }

    @JSExport public static int getMovesCount()
      {
        return movesCount;
      }

    @JSExport public static void clearMoves()
      {
        movesCount = 0;
      }

    @JSExport public static void writeCurrentStateByte(int i, int value)
      {
        if(i < 0 || i >= GAMESTATE_CAP)
          return;
        currentState[i] = (byte)(value & 0xFF);
      }

    @JSExport public static int readCurrentStateByte(int i)
      {
        if(i < 0 || i >= GAMESTATE_CAP)
          return 0;
        return currentState[i] & 0xFF;
      }

    @JSExport public static int readMovesByte(int i)
      {
        if(i < 0 || i >= MOVES_BYTES)
          return 0;
        return movesBuffer[i] & 0xFF;
      }

    // Placeholder pointer-style calls (kept for compatibility with your desired API)
    // Once we implement true shared-memory views, these will return real offsets.
    @JSExport public static int getCurrentState() { return 0; }
    @JSExport public static int getMovesBuffer() { return 0; }

    // ---- Your requested query API (stubs for now) ----
    @JSExport public static int sideToMove_client() { return 0; }

    @JSExport public static int isWhite_client(int index) { return 0; }
    @JSExport public static int isBlack_client(int index) { return 0; }
    @JSExport public static int isEmpty_client(int index) { return 0; }

    @JSExport public static int isPawn_client(int index) { return 0; }
    @JSExport public static int isKnight_client(int index) { return 0; }
    @JSExport public static int isChampion_client(int index) { return 0; }
    @JSExport public static int isWizard_client(int index) { return 0; }
    @JSExport public static int isBishop_client(int index) { return 0; }
    @JSExport public static int isRook_client(int index) { return 0; }
    @JSExport public static int isQueen_client(int index) { return 0; }
    @JSExport public static int isKing_client(int index) { return 0; }

    @JSExport public static int whiteKingsidePrivilege_client() { return 0; }
    @JSExport public static int whiteQueensidePrivilege_client() { return 0; }
    @JSExport public static int whiteCastled_client() { return 0; }
    @JSExport public static int blackKingsidePrivilege_client() { return 0; }
    @JSExport public static int blackQueensidePrivilege_client() { return 0; }
    @JSExport public static int blackCastled_client() { return 0; }

    // Writes destinations (or full 3-byte move records) into movesBuffer and sets movesCount.
    @JSExport public static int getMovesIndex_client(int fromIndex)
      {
        // TODO: implement real move-gen.
        // For now, empty result:
        movesCount = 0;
        return movesCount;
      }

    // promotion is int (0 = none, else ASCII code or your enum)
    @JSExport public static void makeMove_client(int from, int to, int promotion)
      {
        // TODO: apply move to currentState
      }

    @JSExport public static int isTerminal_client() { return 0; }

    // 0 ongoing, 1 white win, 2 black win, 3 stalemate (your choice)
    @JSExport public static int isWin_client() { return 0; }

    // Debug draw: for now write a textual dump into movesBuffer or console via a later bridge.
    @JSExport public static void draw()
      {
        // TODO: Either:
        // (A) write ASCII board text into movesBuffer and set movesCount, OR
        // (B) later, for WASM-GC backend, call JS print functions.
      }
  }

