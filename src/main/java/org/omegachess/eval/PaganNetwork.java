package org.omegachess.eval;

/* Allocation-free forward pass for Pagan v1.
   This reproduces:
     ReLU(stem 1x1)
     four depthwise-separable residual blocks
     flatten(CHW)
     ReLU(hidden dense)
     tanh(value head)

   One PaganNetwork instance is intentionally not thread-safe or reentrant. */
public final class PaganNetwork
  {
    private final float[] activation = new float[PaganSpec.ACTIVATION_FLOATS];
    private final float[] workA = new float[PaganSpec.ACTIVATION_FLOATS];
    private final float[] workB = new float[PaganSpec.ACTIVATION_FLOATS];
    private final float[] hidden = new float[PaganSpec.HIDDEN_UNITS];

    public float evaluate(float[] input)
      {
        int block;

        if(input == null || input.length != PaganSpec.INPUT_FLOATS)
          throw new IllegalArgumentException("Pagan input must contain exactly " + PaganSpec.INPUT_FLOATS + " floats");

        stem(input, activation);

        for(block = 0; block < PaganSpec.RESIDUAL_BLOCKS; block++)
          residualBlock(block);

        hiddenLayer();
        return valueHead();
      }

    private void stem(float[] input, float[] output)
      {
        int inputPlane, outputChannel, weightBase;
        float bias, sum;
        int square;

        for(outputChannel = 0; outputChannel < PaganSpec.CHANNELS; outputChannel++)
          {
            weightBase = outputChannel * PaganSpec.INPUT_PLANES;
            bias = PaganWeights.STEM_BIAS[outputChannel];

            for(square = 0; square < PaganSpec.SQUARES; square++)
              {
                sum = bias;

                for(inputPlane = 0; inputPlane < PaganSpec.INPUT_PLANES; inputPlane++)
                  sum += input[inputPlane * PaganSpec.SQUARES + square] * PaganWeights.STEM_WEIGHT[weightBase + inputPlane];

                output[outputChannel * PaganSpec.SQUARES + square] = relu(sum);
              }
          }

        return;
      }

    private void residualBlock(int block)
      {
        int i;

        depthwise3x3(activation, workA, PaganWeights.DEPTHWISE1_WEIGHT, PaganWeights.DEPTHWISE1_BIAS, block, true);
        pointwise1x1(workA, workB, PaganWeights.POINTWISE1_WEIGHT, PaganWeights.POINTWISE1_BIAS, block, true);
        depthwise3x3(workB, workA, PaganWeights.DEPTHWISE2_WEIGHT, PaganWeights.DEPTHWISE2_BIAS, block, true);
        pointwise1x1(workA, workB, PaganWeights.POINTWISE2_WEIGHT, PaganWeights.POINTWISE2_BIAS, block, false);

        for(i = 0; i < activation.length; i++)
          activation[i] = relu(activation[i] + workB[i]);

        return;
      }

    private static void depthwise3x3(float[] input, float[] output, float[] weights, float[] biases, int block, boolean applyRelu)
      {
        int channel, channelWeightBase;
        int blockWeightBase = block * PaganSpec.CHANNELS * 3 * 3;
        int blockBiasBase = block * PaganSpec.CHANNELS;
        float bias, sum;
        int row, col;
        int kernelRow, kernelCol;
        int inputRow, inputCol, inputIndex, weightIndex, outputIndex;

        for(channel = 0; channel < PaganSpec.CHANNELS; channel++)
          {
            channelWeightBase = blockWeightBase + channel * 9;
            bias = biases[blockBiasBase + channel];

            for(row = 0; row < PaganSpec.HEIGHT; row++)
              {
                for(col = 0; col < PaganSpec.WIDTH; col++)
                  {
                    sum = bias;

                    for(kernelRow = 0; kernelRow < 3; kernelRow++)
                      {
                        inputRow = row + kernelRow - 1;
                        if(inputRow < 0 || inputRow >= PaganSpec.HEIGHT)
                          continue;

                        for(kernelCol = 0; kernelCol < 3; kernelCol++)
                          {
                            inputCol = col + kernelCol - 1;
                            if(inputCol < 0 || inputCol >= PaganSpec.WIDTH)
                              continue;

                            inputIndex = PaganSpec.activationIndex(channel, inputRow, inputCol);
                            weightIndex = channelWeightBase + kernelRow * 3 + kernelCol;

                            sum += input[inputIndex] * weights[weightIndex];
                          }
                      }

                    outputIndex = PaganSpec.activationIndex(channel, row, col);
                    output[outputIndex] = applyRelu ? relu(sum) : sum;
                  }
              }
          }

        return;
      }

    private static void pointwise1x1(float[] input, float[] output, float[] weights, float[] biases, int block, boolean applyRelu)
      {
        int blockWeightBase = block * PaganSpec.CHANNELS * PaganSpec.CHANNELS;
        int blockBiasBase = block * PaganSpec.CHANNELS;
        int outputChannel, outputWeightBase, inputChannel;
        int square;
        float bias, sum;

        for(outputChannel = 0; outputChannel < PaganSpec.CHANNELS; outputChannel++)
          {
            outputWeightBase = blockWeightBase + outputChannel * PaganSpec.CHANNELS;
            bias = biases[blockBiasBase + outputChannel];

            for(square = 0; square < PaganSpec.SQUARES; square++)
              {
                sum = bias;

                for(inputChannel = 0; inputChannel < PaganSpec.CHANNELS; inputChannel++)
                  sum += input[inputChannel * PaganSpec.SQUARES + square] * weights[outputWeightBase + inputChannel];

                output[outputChannel * PaganSpec.SQUARES + square] = applyRelu ? relu(sum) : sum;
              }
          }

        return;
      }

    private void hiddenLayer()
      {
        int outputUnit, weightBase, inputUnit;
        float sum;

        for(outputUnit = 0; outputUnit < PaganSpec.HIDDEN_UNITS; outputUnit++)
          {
            weightBase = outputUnit * PaganSpec.ACTIVATION_FLOATS;
            sum = PaganWeights.HIDDEN_BIAS[outputUnit];

            for(inputUnit = 0; inputUnit < PaganSpec.ACTIVATION_FLOATS; inputUnit++)
              sum += activation[inputUnit] * PaganWeights.HIDDEN_WEIGHT[weightBase + inputUnit];

            hidden[outputUnit] = relu(sum);
          }

        return;
      }

    private float valueHead()
      {
        int i;
        float sum = PaganWeights.VALUE_BIAS[0];

        for(i = 0; i < PaganSpec.HIDDEN_UNITS; i++)
          sum += hidden[i] * PaganWeights.VALUE_WEIGHT[i];

        return (float) Math.tanh(sum);
      }

    private static float relu(float value)
      {
        return value > 0.0f ? value : 0.0f;
      }
  }
