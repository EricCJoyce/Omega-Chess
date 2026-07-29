package org.omegachess.eval;

import org.omegachess.core.GameState;
import org.omegachess.core.features.FeatureEncoding;
import org.omegachess.core.features.FeatureScratch;

/* Connects Omega Chess's canonical feature encoder to Pagan's forward pass.
   Values are always from the point of view of the side to move. */
public final class PaganEvaluator
  {
    private final float[] features = new float[PaganSpec.INPUT_FLOATS];
    private final FeatureScratch featureScratch = new FeatureScratch();
    private final PaganNetwork network = new PaganNetwork();
    private final HeuristicEvaluator heuristicEvaluator = new HeuristicEvaluator();

    private float lastNetworkLogit;
    private float lastHeuristicLogit;
    private float lastValue;

    public float evaluate(GameState gs)
      {
        if(gs == null)
          throw new IllegalArgumentException("GameState argument cannot be null");

        FeatureEncoding.encode(gs, features, featureScratch);

        lastNetworkLogit = network.forwardLogit(features);
        lastHeuristicLogit = heuristicEvaluator.evaluate(gs, featureScratch);
        lastValue = (float) Math.tanh(lastNetworkLogit + lastHeuristicLogit);

        return lastValue;
      }

    //  Diagnostic access only; valid after evaluate().
    public float lastNetworkLogit()
      {
        return lastNetworkLogit;
      }

    //  Diagnostic access only; valid after evaluate().
    public float lastHeuristicLogit()
      {
        return lastHeuristicLogit;
      }

    //  Diagnostic access only; valid after evaluate().
    public float lastValue()
      {
        return lastValue;
      }

    //  Diagnostic access only; do not mutate during evaluation.
    public float[] featureBuffer()
      {
        return features;
      }
  }
