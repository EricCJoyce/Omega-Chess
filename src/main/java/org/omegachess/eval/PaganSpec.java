package org.omegachess.eval;

/* Fixed architecture and tensor layout for Pagan v1.

   Input layout is plane-row-column (CHW):
     index = (plane * HEIGHT + row) * WIDTH + column.

   Activation layout is channel-row-column (CHW):
     index = (channel * HEIGHT + row) * WIDTH + column. */
public final class PaganSpec
  {
    public static final String CHECKPOINT_FORMAT          = "pagan-value-network-v1";
    public static final String FEATURE_VERSION            = "pagan-features-v1";
    public static final int LAYOUT_VERSION                = 1;

    public static final int INPUT_PLANES                  = 64;
    public static final int HEIGHT                        = 12;
    public static final int WIDTH                         = 12;
    public static final int SQUARES                       = HEIGHT * WIDTH;
    public static final int INPUT_FLOATS                  = INPUT_PLANES * SQUARES;

    public static final int CHANNELS                      = 32;
    public static final int RESIDUAL_BLOCKS               = 4;
    public static final int HIDDEN_UNITS                  = 64;
    public static final int ACTIVATION_FLOATS             = CHANNELS * SQUARES;

    public static final int STEM_WEIGHT_FLOATS            = CHANNELS * INPUT_PLANES;
    public static final int STEM_BIAS_FLOATS              = CHANNELS;

    public static final int BLOCK_DEPTHWISE_WEIGHT_FLOATS = RESIDUAL_BLOCKS * CHANNELS * 3 * 3;
    public static final int BLOCK_DEPTHWISE_BIAS_FLOATS   = RESIDUAL_BLOCKS * CHANNELS;
    public static final int BLOCK_POINTWISE_WEIGHT_FLOATS = RESIDUAL_BLOCKS * CHANNELS * CHANNELS;
    public static final int BLOCK_POINTWISE_BIAS_FLOATS   = RESIDUAL_BLOCKS * CHANNELS;

    public static final int HIDDEN_WEIGHT_FLOATS          = HIDDEN_UNITS * ACTIVATION_FLOATS;
    public static final int HIDDEN_BIAS_FLOATS            = HIDDEN_UNITS;
    public static final int VALUE_WEIGHT_FLOATS           = HIDDEN_UNITS;
    public static final int VALUE_BIAS_FLOATS             = 1;

    public static final int PARAMETER_FLOATS              = STEM_WEIGHT_FLOATS +
                                                            STEM_BIAS_FLOATS +
                                                            BLOCK_DEPTHWISE_WEIGHT_FLOATS +
                                                            BLOCK_DEPTHWISE_BIAS_FLOATS +
                                                            BLOCK_POINTWISE_WEIGHT_FLOATS +
                                                            BLOCK_POINTWISE_BIAS_FLOATS +
                                                            BLOCK_DEPTHWISE_WEIGHT_FLOATS +
                                                            BLOCK_DEPTHWISE_BIAS_FLOATS +
                                                            BLOCK_POINTWISE_WEIGHT_FLOATS +
                                                            BLOCK_POINTWISE_BIAS_FLOATS +
                                                            HIDDEN_WEIGHT_FLOATS +
                                                            HIDDEN_BIAS_FLOATS +
                                                            VALUE_WEIGHT_FLOATS +
                                                            VALUE_BIAS_FLOATS;
    public static final int PARAMETER_BYTES               = PARAMETER_FLOATS * Float.BYTES;

    public static final int MATERIAL_PAWN                 = 10;
    public static final int MATERIAL_KNIGHT               = 20;
    public static final int MATERIAL_CHAMPION             = 39;
    public static final int MATERIAL_WIZARD               = 38;
    public static final int MATERIAL_BISHOP               = 40;
    public static final int MATERIAL_ROOK                 = 60;
    public static final int MATERIAL_QUEEN                = 120;
    public static final int SEE_KING                      = 1000;

    public static final float KING_PRESSURE_WEIGHT        = 1.0f;
    public static final float KING_CHECK_WEIGHT           = 4.0f;
    public static final float KING_ESCAPE_WEIGHT          = 1.0f;

    public static final float[] PASSED_PAWN_BONUS         = {0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 7.0f, 11.0f, 16.0f};
    public static final float PAWN_DOUBLED                = 2.0f;
    public static final float PAWN_ISOLATED               = 2.0f;
    public static final float PAWN_CONNECTED              = 1.0f;

    private PaganSpec()
      {
      }

    public static int inputIndex(int plane, int row, int column)
      {
        return (plane * HEIGHT + row) * WIDTH + column;
      }

    public static int activationIndex(int channel, int row, int column)
      {
        return (channel * HEIGHT + row) * WIDTH + column;
      }
  }
