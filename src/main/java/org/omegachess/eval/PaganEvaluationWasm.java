package org.omegachess.eval;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.omegachess.core.GameState;
import org.omegachess.core.Move;
import org.omegachess.core.GameEncoding;
import org.omegachess.core.GameLogic;

import org.omegachess.core.features.FeatureSpec;
import org.omegachess.core.features.FeatureEncoding;
import org.omegachess.core.features.FeatureScratch;
import org.omegachess.core.features.TacticalAnalyzer;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSExport;

/* Thin TeaVM/Wasm-GC entry point for Pagan.

   JavaScript copies negamax's query-state bytes into INPUT_STATE_BUFFER, then calls evaluate().
   The returned value is in [-1,+1] from the point of view of the side to move in that encoded state.

   The packed weight buffer is a practical startup path that avoids emitting hundreds of thousands of float literals into Java class files.
   A later exporter can either write pagan-weights.bin or replace PaganWeights with generated embedded data while preserving the inference code. */
public final class PaganEvaluationWasm
  {
    private static final ByteBuffer INPUT_STATE_BUFFER = ByteBuffer.allocateDirect(GameState._GAMESTATE_BYTE_SIZE);
    private static final FloatBuffer PACKED_WEIGHT_BUFFER = ByteBuffer.allocateDirect(PaganSpec.PARAMETER_FLOATS * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();

    private static final byte[] STATE_BYTES = new byte[GameState._GAMESTATE_BYTE_SIZE];
    private static final GameState STATE = new GameState();
    private static final PaganEvaluator EVALUATOR = new PaganEvaluator();

    private static boolean weightsCommitted;

    private PaganEvaluationWasm()
      {
      }

    //  TeaVM entry point. Initialization is performed lazily by exports.
    public static void main(String[] args)
      {
        return;
      }

    @JSExport
    public static void bindBuffers()
      {
        installBuffers(INPUT_STATE_BUFFER, PACKED_WEIGHT_BUFFER);
        return;
      }

    //  Commit exactly PaganSpec.PARAMETER_FLOATS values after JavaScript has copied a packed float32 checkpoint into PACKED_WEIGHT_BUFFER.
    @JSExport
    public static void commitWeights()
      {
        PACKED_WEIGHT_BUFFER.position(0);
        PaganWeights.loadPacked(PACKED_WEIGHT_BUFFER);
        PACKED_WEIGHT_BUFFER.position(0);
        weightsCommitted = true;
        return;
      }

    @JSExport
    public static float evaluate()
      {
        decodeInputState();
        return EVALUATOR.evaluate(STATE);
      }

    @JSExport
    public static int sideToMove()
      {
        decodeInputState();
        return STATE.isWhiteToMove() ? GameState._WHITE_TO_MOVE : GameState._BLACK_TO_MOVE;
      }

    @JSExport
    public static boolean isTerminal()
      {
        decodeInputState();
        return STATE.terminal();
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
    public static int featureCount()
      {
        return PaganSpec.INPUT_FLOATS;
      }

    @JSExport
    public static int architectureVersion()
      {
        return PaganSpec.LAYOUT_VERSION;
      }

    private static void decodeInputState()
      {
        INPUT_STATE_BUFFER.position(0);
        INPUT_STATE_BUFFER.get(STATE_BYTES, 0, GameState._GAMESTATE_BYTE_SIZE);
        INPUT_STATE_BUFFER.position(0);
        GameEncoding.decodeState(STATE_BYTES, STATE);
        return;
      }

    @JSBody(params = {"inputState", "packedWeights"},
            script = "" +
                     "if(typeof globalThis.omegaChessInstallPaganBuffers !== 'function')" +
                     "  throw new Error('omegaChessInstallPaganBuffers is not installed');" +
                     "" +
                     "globalThis.omegaChessInstallPaganBuffers(inputState, packedWeights);")
    private static native void installBuffers(ByteBuffer inputState, FloatBuffer packedWeights);
  }
