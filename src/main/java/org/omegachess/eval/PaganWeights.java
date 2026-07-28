package org.omegachess.eval;

import java.nio.FloatBuffer;
import java.util.Arrays;

/* Pagan v1 parameters in PyTorch-compatible order.

   The arrays are zero-filled placeholders until an exported checkpoint is installed.
   They may later be replaced by generated Java initializers, or populated once at startup from a packed float32 buffer. */
public final class PaganWeights
  {
                                                                    //  [output channel][input plane]
    public static final float[] STEM_WEIGHT = new float[PaganSpec.STEM_WEIGHT_FLOATS];
                                                                    //  [output channel]
    public static final float[] STEM_BIAS = new float[PaganSpec.STEM_BIAS_FLOATS];

                                                                    //  [block][channel][kernel row][kernel column]
    public static final float[] DEPTHWISE1_WEIGHT = new float[PaganSpec.BLOCK_DEPTHWISE_WEIGHT_FLOATS];
                                                                    //  [block][channel]
    public static final float[] DEPTHWISE1_BIAS = new float[PaganSpec.BLOCK_DEPTHWISE_BIAS_FLOATS];

                                                                    //  [block][output channel][input channel]
    public static final float[] POINTWISE1_WEIGHT = new float[PaganSpec.BLOCK_POINTWISE_WEIGHT_FLOATS];
                                                                    //  [block][output channel]
    public static final float[] POINTWISE1_BIAS = new float[PaganSpec.BLOCK_POINTWISE_BIAS_FLOATS];

                                                                    //  [block][channel][kernel row][kernel column]
    public static final float[] DEPTHWISE2_WEIGHT = new float[PaganSpec.BLOCK_DEPTHWISE_WEIGHT_FLOATS];
                                                                    //  [block][channel]
    public static final float[] DEPTHWISE2_BIAS = new float[PaganSpec.BLOCK_DEPTHWISE_BIAS_FLOATS];

                                                                    //  [block][output channel][input channel]
    public static final float[] POINTWISE2_WEIGHT = new float[PaganSpec.BLOCK_POINTWISE_WEIGHT_FLOATS];
                                                                    //  [block][output channel]
    public static final float[] POINTWISE2_BIAS = new float[PaganSpec.BLOCK_POINTWISE_BIAS_FLOATS];

                                                                    //  [hidden unit][flattened channel-row-column input]
    public static final float[] HIDDEN_WEIGHT = new float[PaganSpec.HIDDEN_WEIGHT_FLOATS];
                                                                    //  [hidden unit]
    public static final float[] HIDDEN_BIAS = new float[PaganSpec.HIDDEN_BIAS_FLOATS];

                                                                    //  [hidden unit]
    public static final float[] VALUE_WEIGHT = new float[PaganSpec.VALUE_WEIGHT_FLOATS];
                                                                    //  scalar
    public static final float[] VALUE_BIAS = new float[PaganSpec.VALUE_BIAS_FLOATS];

    private PaganWeights()
      {
      }

    /* Load all parameters from a packed float32 buffer.
       Packed order:
         stem.weight, stem.bias,
         blocks.0.depthwise1.weight, blocks.0.depthwise1.bias,
         blocks.0.pointwise1.weight, blocks.0.pointwise1.bias,
         blocks.0.depthwise2.weight, blocks.0.depthwise2.bias,
         blocks.0.pointwise2.weight, blocks.0.pointwise2.bias,
         ... repeated for blocks 1..3,
         hidden.weight, hidden.bias, value_head.weight, value_head.bias. */
    public static void loadPacked(FloatBuffer source)
      {
        int block;

        if(source == null)
          throw new IllegalArgumentException("source cannot be null");
        if(source.remaining() != PaganSpec.PARAMETER_FLOATS)
          throw new IllegalArgumentException("Expected " + PaganSpec.PARAMETER_FLOATS + " packed floats, found " + source.remaining());

        source.get(STEM_WEIGHT);
        source.get(STEM_BIAS);

        for(block = 0; block < PaganSpec.RESIDUAL_BLOCKS; block++)
          {
            getBlockDepthwise(source, DEPTHWISE1_WEIGHT, block);
            getBlockBias(source, DEPTHWISE1_BIAS, block);
            getBlockPointwise(source, POINTWISE1_WEIGHT, block);
            getBlockBias(source, POINTWISE1_BIAS, block);
            getBlockDepthwise(source, DEPTHWISE2_WEIGHT, block);
            getBlockBias(source, DEPTHWISE2_BIAS, block);
            getBlockPointwise(source, POINTWISE2_WEIGHT, block);
            getBlockBias(source, POINTWISE2_BIAS, block);
          }

        source.get(HIDDEN_WEIGHT);
        source.get(HIDDEN_BIAS);
        source.get(VALUE_WEIGHT);
        source.get(VALUE_BIAS);
        return;
      }

    public static void writePacked(FloatBuffer destination)
      {
        int block;

        if(destination == null)
          throw new IllegalArgumentException("destination cannot be null");

        if(destination.remaining() != PaganSpec.PARAMETER_FLOATS)
          throw new IllegalArgumentException("Expected room for " + PaganSpec.PARAMETER_FLOATS + " packed floats, found " + destination.remaining());

        destination.put(STEM_WEIGHT);
        destination.put(STEM_BIAS);

        for(block = 0; block < PaganSpec.RESIDUAL_BLOCKS; block++)
          {
            putBlockDepthwise(destination, DEPTHWISE1_WEIGHT, block);
            putBlockBias(destination, DEPTHWISE1_BIAS, block);
            putBlockPointwise(destination, POINTWISE1_WEIGHT, block);
            putBlockBias(destination, POINTWISE1_BIAS, block);
            putBlockDepthwise(destination, DEPTHWISE2_WEIGHT, block);
            putBlockBias(destination, DEPTHWISE2_BIAS, block);
            putBlockPointwise(destination, POINTWISE2_WEIGHT, block);
            putBlockBias(destination, POINTWISE2_BIAS, block);
          }

        destination.put(HIDDEN_WEIGHT);
        destination.put(HIDDEN_BIAS);
        destination.put(VALUE_WEIGHT);
        destination.put(VALUE_BIAS);
        return;
      }

    public static void clear()
      {
        Arrays.fill(STEM_WEIGHT, 0.0f);
        Arrays.fill(STEM_BIAS, 0.0f);
        Arrays.fill(DEPTHWISE1_WEIGHT, 0.0f);
        Arrays.fill(DEPTHWISE1_BIAS, 0.0f);
        Arrays.fill(POINTWISE1_WEIGHT, 0.0f);
        Arrays.fill(POINTWISE1_BIAS, 0.0f);
        Arrays.fill(DEPTHWISE2_WEIGHT, 0.0f);
        Arrays.fill(DEPTHWISE2_BIAS, 0.0f);
        Arrays.fill(POINTWISE2_WEIGHT, 0.0f);
        Arrays.fill(POINTWISE2_BIAS, 0.0f);
        Arrays.fill(HIDDEN_WEIGHT, 0.0f);
        Arrays.fill(HIDDEN_BIAS, 0.0f);
        Arrays.fill(VALUE_WEIGHT, 0.0f);
        Arrays.fill(VALUE_BIAS, 0.0f);
        return;
      }

    private static void getBlockDepthwise(FloatBuffer source, float[] destination, int block)
      {
        int count = PaganSpec.CHANNELS * 3 * 3;
        source.get(destination, block * count, count);
        return;
      }

    private static void getBlockPointwise(FloatBuffer source, float[] destination, int block)
      {
        int count = PaganSpec.CHANNELS * PaganSpec.CHANNELS;
        source.get(destination, block * count, count);
        return;
      }

    private static void getBlockBias(FloatBuffer source, float[] destination, int block)
      {
        source.get(destination, block * PaganSpec.CHANNELS, PaganSpec.CHANNELS);
        return;
      }

    private static void putBlockDepthwise(FloatBuffer destination, float[] source, int block)
      {
        int count = PaganSpec.CHANNELS * 3 * 3;
        destination.put(source, block * count, count);
        return;
      }

    private static void putBlockPointwise(FloatBuffer destination, float[] source, int block)
      {
        int count = PaganSpec.CHANNELS * PaganSpec.CHANNELS;
        destination.put(source, block * count, count);
        return;
      }

    private static void putBlockBias(FloatBuffer destination, float[] source, int block)
      {
        destination.put(source, block * PaganSpec.CHANNELS, PaganSpec.CHANNELS);
        return;
      }
  }
