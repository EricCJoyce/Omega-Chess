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

    public float evaluate(GameState state)
      {
        if(state == null)
          throw new IllegalArgumentException("state cannot be null");

        FeatureEncoding.encode(state, features, featureScratch);
        return network.evaluate(features);
      }

    //  Diagnostic access only; do not mutate during evaluation.
    public float[] featureBuffer()
      {
        return features;
      }
  }
