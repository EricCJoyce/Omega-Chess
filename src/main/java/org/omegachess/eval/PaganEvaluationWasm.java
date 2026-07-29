package org.omegachess.eval;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.omegachess.core.GameEncoding;
import org.omegachess.core.GameState;
import org.omegachess.core.Move;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSExport;

/* Thin TeaVM/Wasm-GC entry point for Pagan.

   The module owns independent GameState objects for hypothetical search positions.
   JavaScript writes encoded states and moves into direct buffers, calls an exported operation, and reads the corresponding output buffer. */
public final class PaganEvaluationWasm
  {
    private static final int MOVE_SCORE_BYTES               = Integer.BYTES;
    private static final int MOVE_FLAGS_BYTES               = 1;
    private static final int MOVE_RECORD_BYTES              = GameState._MOVE_BYTE_SIZE + MOVE_SCORE_BYTES + MOVE_FLAGS_BYTES;

    private static final int MOVE_FLAG_NOISY                = 0x01; //  Bit zero means that a move is a capture or promotion.
    private static final int MOVE_SORTING_PROMOTION_BONUS   = 800;
    private static final int MOVE_SORTING_CHECK_BONUS       = 50;
                                                                    //  Array containing the encoded INPUT game state.
    private static final ByteBuffer INPUT_GAMESTATE_BUFFER  = directByteBuffer(GameState._GAMESTATE_BYTE_SIZE);
                                                                    //  Array containing the encoded INPUT move.
    private static final ByteBuffer INPUT_MOVE_BUFFER       = directByteBuffer(GameState._MOVE_BYTE_SIZE);
                                                                    //  Array containing the encoded OUTPUT game state.
    private static final ByteBuffer OUTPUT_GAMESTATE_BUFFER = directByteBuffer(GameState._GAMESTATE_BYTE_SIZE);
                                                                    //  Array containing up to _MAX_MOVES moves.
                                                                    //  Rather than encode the number of moves in the array itself, we return an integer.
                                                                    //  Each move is represented as a byte sub-array encoding:
                                                                    //    _MOVE_BYTE_SIZE  :  bytes encoding a single move,
                                                                    //    4                :  bytes for signed integer, which is rough score.
                                                                    //    1                :  byte (should be Boolean) indicating whether move is "quiet".
    private static final ByteBuffer OUTPUT_MOVES_BUFFER     = directByteBuffer(GameState._MAX_MOVES * MOVE_RECORD_BYTES);

    private static final FloatBuffer PACKED_WEIGHT_BUFFER   = ByteBuffer.allocateDirect(PaganSpec.PARAMETER_BYTES).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();

    private static final byte[] INPUT_GAMESTATE_BYTES       = new byte[GameState._GAMESTATE_BYTE_SIZE];
    private static final byte[] INPUT_MOVE_BYTES            = new byte[GameState._MOVE_BYTE_SIZE];
    private static final byte[] OUTPUT_GAMESTATE_BYTES      = new byte[GameState._GAMESTATE_BYTE_SIZE];

    private static final GameState GAMESTATE                = new GameState();
    private static final GameState GAMESTATE_OUT            = new GameState();
    private static final GameState MOVE_SORT_STATE          = new GameState();
    private static final Move[] MOVES                       = new Move[GameState._MAX_MOVES];
    private static final PaganEvaluator EVALUATOR           = new PaganEvaluator();

    private static boolean weightsCommitted;

    private PaganEvaluationWasm()
      {
      }

    //  TeaVM entry point.
    //  Initialization is performed through exports.
    public static void main(String[] args)
      {
      }

    //  Pass typed-array views of all direct buffers to JavaScript.
    //  Call once after loading the module and again after a memory resize.
    @JSExport
    public static void bindBuffers()
      {
        installBuffers(INPUT_GAMESTATE_BUFFER, INPUT_MOVE_BUFFER, OUTPUT_GAMESTATE_BUFFER, OUTPUT_MOVES_BUFFER, PACKED_WEIGHT_BUFFER);
        return;
      }

    //  Install exactly PaganSpec.PARAMETER_FLOATS float32 parameters after JavaScript has filled the packed-weight buffer.
    @JSExport
    public static void commitWeights()
      {
        weightsCommitted = false;
        PACKED_WEIGHT_BUFFER.clear();
        PaganWeights.loadPacked(PACKED_WEIGHT_BUFFER);
        PACKED_WEIGHT_BUFFER.clear();
        weightsCommitted = true;
        return;
      }

    //  Answer the Negamax Module's query, "Which side is to move in the GameState in the query buffer?"
    //  Return an unsigned char in {GameState._WHITE_TO_MOVE, GameState._BLACK_TO_MOVE}.
    @JSExport
    public static int sideToMove()
      {
        decodeInputGameState();
        return GAMESTATE.isWhiteToMove() ? GameState._WHITE_TO_MOVE : GameState._BLACK_TO_MOVE;
      }

    //  Answer the Negamax Module's query, "Is the GameState in the query buffer terminal?"
    @JSExport
    public static boolean isTerminal()
      {
        decodeInputGameState();
        return GAMESTATE.terminal();
      }

    //  Answer the Negamax Module's query, "Is the side to move in the GameState in the query buffer in check?"
    @JSExport
    public static boolean isSideToMoveInCheck()
      {
        decodeInputGameState();

        boolean white = GAMESTATE.isWhiteToMove();
        int king = GAMESTATE.getKingIndex(white);

        return king != GameState._NONE && GAMESTATE.inCheckBy(king, !white);
      }

    //  Answer the Negamax Module's query, "How much non-pawn material does the side to move have in the GameState in the query buffer?"
    //  This is intended for pruning policy, not Pagan's value calculation.
    //  Omega's Reinfeld values from https://www.omegachess.com/strategy
    @JSExport
    public static int nonPawnMaterial()
      {
        boolean white;
        int index;
        int total = 0;
        byte piece;

        decodeInputGameState();
        white = GAMESTATE.isWhiteToMove();

        for(index = 0; index < GameState._NONE; index++)
          {
            if((white && !GAMESTATE.isWhite(index)) || (!white && !GAMESTATE.isBlack(index)))
              continue;

            piece = GAMESTATE.pieceAt(index);

            if(piece == GameState._WHITE_KNIGHT || piece == GameState._BLACK_KNIGHT)
              total += 2;
            else if (piece == GameState._WHITE_CHAMPION || piece == GameState._BLACK_CHAMPION ||
                     piece == GameState._WHITE_WIZARD   || piece == GameState._BLACK_WIZARD   ||
                     piece == GameState._WHITE_BISHOP   || piece == GameState._BLACK_BISHOP   )
              total += 4;
            else if (piece == GameState._WHITE_ROOK || piece == GameState._BLACK_ROOK)
              total += 6;
            else if (piece == GameState._WHITE_QUEEN || piece == GameState._BLACK_QUEEN)
              total += 12;
          }

        return total;
      }

    //  Write the game state resulting from INPUT_GAMESTATE + INPUT_MOVE.
    @JSExport
    public static void makeMove()
      {
        decodeInputGameState();
        Move move = decodeInputMove();

        GAMESTATE_OUT.copyFrom(GAMESTATE);
        GAMESTATE_OUT.makeMove(move);
        encodeOutputGameState(GAMESTATE_OUT);
        return;
      }

    //  Write the game state resulting from a null move.
    @JSExport
    public static void makeNullMove()
      {
        decodeInputGameState();

        GAMESTATE_OUT.copyFrom(GAMESTATE);
        GAMESTATE_OUT.makeNullMove();
        encodeOutputGameState(GAMESTATE_OUT);
        return;
      }

    //  Return Pagan's bounded side-to-move value in [-1, +1].
    @JSExport
    public static float evaluate()
      {
        requireWeightsCommitted();
        decodeInputGameState();
        return EVALUATOR.evaluate(GAMESTATE);
      }

    //  Valid after evaluate(); useful for cross-language diagnostics.
    @JSExport
    public static float lastNetworkLogit()
      {
        return EVALUATOR.lastNetworkLogit();
      }

    //  Valid after evaluate(); currently always zero.
    @JSExport
    public static float lastHeuristicLogit()
      {
        return EVALUATOR.lastHeuristicLogit();
      }

    //  Generate legal moves and serialize one fixed-size record per move:
    //    byte 0       source square
    //    byte 1       destination square
    //    byte 2       promotion code
    //    bytes 3..6   signed little-endian move-ordering score
    //    byte 7       flags; bit zero means capture or promotion
    //  The return value is the number of records written.
    @JSExport
    public static int getMoves()
      {
        int moveCount, i, score;
        Move move;
        boolean capture, promotion, noisy;

        decodeInputGameState();
        moveCount = GAMESTATE.getMoves(MOVES);

        if(moveCount < 0 || moveCount > GameState._MAX_MOVES)
          throw new IllegalStateException("Game logic returned invalid move count: " + moveCount);

        OUTPUT_MOVES_BUFFER.clear();

        for(i = 0; i < moveCount; i++)
          {
            move = MOVES[i];

            if(move == null)
              throw new IllegalStateException("Game logic returned a null move at index " + i);

            capture = GAMESTATE.isCapture(move);
            promotion = move.promo != GameState._NO_PROMO;
            noisy = capture || promotion;

            score = capture ? StaticExchangeEvaluator.evaluate(GAMESTATE, move) : 0;

            if(promotion)
              score += MOVE_SORTING_PROMOTION_BONUS;

            if(checksOpponent(GAMESTATE, move))
              score += MOVE_SORTING_CHECK_BONUS;

            OUTPUT_MOVES_BUFFER.put((byte) move.from);
            OUTPUT_MOVES_BUFFER.put((byte) move.to);
            OUTPUT_MOVES_BUFFER.put(move.promo);
            OUTPUT_MOVES_BUFFER.putInt(score);
            OUTPUT_MOVES_BUFFER.put((byte)(noisy ? MOVE_FLAG_NOISY : 0));
          }

        OUTPUT_MOVES_BUFFER.position(0);
        return moveCount;
      }

    @JSExport
    public static boolean weightsCommitted()
      {
        return weightsCommitted;
      }

    @JSExport
    public static int parameterCount()
      {
        return PaganSpec.PARAMETER_FLOATS;
      }

    @JSExport
    public static int packedWeightBytes()
      {
        return PaganSpec.PARAMETER_BYTES;
      }

    @JSExport
    public static int featureCount()
      {
        return PaganSpec.INPUT_FLOATS;
      }

    @JSExport
    public static int architectureVersion()
      {
        return PaganSpec.LAYOUT_VERSION;
      }

    @JSExport
    public static int gameStateBytes()
      {
        return GameState._GAMESTATE_BYTE_SIZE;
      }

    @JSExport
    public static int moveBytes()
      {
        return GameState._MOVE_BYTE_SIZE;
      }

    @JSExport
    public static int moveRecordBytes()
      {
        return MOVE_RECORD_BYTES;
      }

    @JSExport
    public static int maximumMoves()
      {
        return GameState._MAX_MOVES;
      }

    private static ByteBuffer directByteBuffer(int bytes)
      {
        return ByteBuffer.allocateDirect(bytes).order(ByteOrder.LITTLE_ENDIAN);
      }

    private static void requireWeightsCommitted()
      {
        if(!weightsCommitted)
          throw new IllegalStateException("Pagan weights have not been committed");
        return;
      }

    private static void decodeInputGameState()
      {
        INPUT_GAMESTATE_BUFFER.position(0);
        INPUT_GAMESTATE_BUFFER.get(INPUT_GAMESTATE_BYTES, 0, GameState._GAMESTATE_BYTE_SIZE);
        INPUT_GAMESTATE_BUFFER.position(0);

        GameEncoding.decodeState(INPUT_GAMESTATE_BYTES, GAMESTATE);
        return;
      }

    private static Move decodeInputMove()
      {
        INPUT_MOVE_BUFFER.position(0);
        INPUT_MOVE_BUFFER.get(INPUT_MOVE_BYTES, 0, GameState._MOVE_BYTE_SIZE);
        INPUT_MOVE_BUFFER.position(0);

        return GameEncoding.decodeMove(INPUT_MOVE_BYTES);
      }

    private static void encodeOutputGameState(GameState gs)
      {
        GameEncoding.encodeState(gs, OUTPUT_GAMESTATE_BYTES);

        OUTPUT_GAMESTATE_BUFFER.position(0);
        OUTPUT_GAMESTATE_BUFFER.put(OUTPUT_GAMESTATE_BYTES, 0, GameState._GAMESTATE_BYTE_SIZE);
        OUTPUT_GAMESTATE_BUFFER.position(0);
        return;
      }

    private static boolean checksOpponent(GameState gs, Move move)
      {
        boolean defendingWhite;
        int king;

        MOVE_SORT_STATE.copyFrom(gs);
        MOVE_SORT_STATE.makeMove(move);

        defendingWhite = MOVE_SORT_STATE.isWhiteToMove();
        king = MOVE_SORT_STATE.getKingIndex(defendingWhite);

        return king != GameState._NONE && MOVE_SORT_STATE.inCheckBy(king, !defendingWhite);
      }

    /* Correctness-first SEE implementation based on legal recaptures.

       It repeatedly chooses the least valuable legal attacker of the target square, applies the recapture to a reusable GameState,
       then performs the standard backward minimax fold.
       Because it asks the existing game logic for legal moves, pins and king safety are handled automatically. */
    private static final class StaticExchangeEvaluator
      {
        private static final int SCORE_PAWN         =   10;
        private static final int SCORE_KNIGHT       =   20;
        private static final int SCORE_CHAMPION     =   40;
        private static final int SCORE_WIZARD       =   40;
        private static final int SCORE_BISHOP       =   44;
        private static final int SCORE_ROOK         =   60;
        private static final int SCORE_QUEEN        =  120;
        private static final int SCORE_KING         = 1000;

        private static final int MAX_EXCHANGE_PLIES =   64;

        private static final GameState STATE_A = new GameState();
        private static final GameState STATE_B = new GameState();
        private static final Move[] MOVE_BUFFER = new Move[GameState._MAX_MOVES];
        private static final int[] GAIN = new int[MAX_EXCHANGE_PLIES + 1];

        private StaticExchangeEvaluator()
          {
          }

        private static int evaluate(GameState gs, Move initialMove)
          {
            int target, depth;
            GameState current, next, swap;
            Move recapture;
            int occupantValue;

            if(!gs.isCapture(initialMove))
              return 0;

            target = initialMove.to;
            GAIN[0] = capturedValue(gs, initialMove) + promotionGain(initialMove);

            current = STATE_A;
            next = STATE_B;
            current.copyFrom(gs);
            current.makeMove(initialMove);

            depth = 0;

            while(depth < MAX_EXCHANGE_PLIES)
              {
                recapture = leastValuableCaptureTo(current, MOVE_BUFFER, target);

                if(recapture == null)
                  break;

                occupantValue = pieceValue(current.pieceAt(target));

                depth++;
                GAIN[depth] = occupantValue + promotionGain(recapture) - GAIN[depth - 1];

                next.copyFrom(current);
                next.makeMove(recapture);

                swap = current;
                current = next;
                next = swap;
              }

            while(depth > 0)
              {
                GAIN[depth - 1] = -Math.max(-GAIN[depth - 1], GAIN[depth]);
                depth--;
              }

            return GAIN[0];
          }

        private static Move leastValuableCaptureTo(GameState gs, Move[] moves, int target)
          {
            int count = gs.getMoves(moves);
            Move move, best = null;
            int bestValue = Integer.MAX_VALUE;
            int i, value;

            for(i = 0; i < count; i++)
              {
                move = moves[i];

                if(move == null || move.to != target)
                  continue;

                value = pieceValue(gs.pieceAt(move.from));

                if(value < bestValue)
                  {
                    bestValue = value;
                    best = move;
                  }
              }

            return best;
          }

        private static int capturedValue(GameState gs, Move move)
          {
            if(gs.isEnPassantAttack(move))
              return SCORE_PAWN;

            return pieceValue(gs.pieceAt(move.to));
          }

        private static int promotionGain(Move move)
          {
            switch(move.promo)
              {
                case GameState._PROMO_KNIGHT:
                  return SCORE_KNIGHT - SCORE_PAWN;
                case GameState._PROMO_CHAMPION:
                  return SCORE_CHAMPION - SCORE_PAWN;
                case GameState._PROMO_WIZARD:
                  return SCORE_WIZARD - SCORE_PAWN;
                case GameState._PROMO_BISHOP:
                  return SCORE_BISHOP - SCORE_PAWN;
                case GameState._PROMO_ROOK:
                  return SCORE_ROOK - SCORE_PAWN;
                case GameState._PROMO_QUEEN:
                  return SCORE_QUEEN - SCORE_PAWN;
                default:
                  return 0;
              }
          }

        private static int pieceValue(byte piece)
          {
            if(piece == GameState._WHITE_PAWN || piece == GameState._BLACK_PAWN)
              return SCORE_PAWN;
            if(piece == GameState._WHITE_KNIGHT || piece == GameState._BLACK_KNIGHT)
              return SCORE_KNIGHT;
            if(piece == GameState._WHITE_CHAMPION || piece == GameState._BLACK_CHAMPION)
              return SCORE_CHAMPION;
            if(piece == GameState._WHITE_WIZARD || piece == GameState._BLACK_WIZARD)
              return SCORE_WIZARD;
            if(piece == GameState._WHITE_BISHOP || piece == GameState._BLACK_BISHOP)
              return SCORE_BISHOP;
            if(piece == GameState._WHITE_ROOK || piece == GameState._BLACK_ROOK)
              return SCORE_ROOK;
            if(piece == GameState._WHITE_QUEEN || piece == GameState._BLACK_QUEEN)
              return SCORE_QUEEN;
            if(piece == GameState._WHITE_KING || piece == GameState._BLACK_KING)
              return SCORE_KING;
            return 0;
          }
      }

    @JSBody(params = {"inputGameState",
                      "inputMove",
                      "outputGameState",
                      "outputMoves",
                      "packedWeights"},
            script = "" +
                     "if(typeof globalThis.omegaChessInstallPaganBuffers !== 'function')" +
                     "  throw new Error('omegaChessInstallPaganBuffers is not installed');" +
                     "globalThis.omegaChessInstallPaganBuffers(inputGameState, inputMove, outputGameState, outputMoves, packedWeights);")
    private static native void installBuffers(ByteBuffer inputGameState,
                                              ByteBuffer inputMove,
                                              ByteBuffer outputGameState,
                                              ByteBuffer outputMoves,
                                              FloatBuffer packedWeights);
  }
