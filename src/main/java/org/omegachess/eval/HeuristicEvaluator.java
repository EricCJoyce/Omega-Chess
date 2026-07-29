package org.omegachess.eval;

import org.omegachess.core.GameState;
import org.omegachess.core.features.FeatureScratch;

/* Values are always from the point of view of the side to move. */
public final class HeuristicEvaluator
  {
    public static float MATERIAL = 0.0f;
    public static float MOBILITY = 0.0f;
    public static float KING_SAFETY = 0.0f;
    public static float PAWN_STRUCTURE = 0.0f;
    public static float TEMPO = 0.0f;

    private final float[] features = new float[PaganSpec.INPUT_FLOATS];
    private final FeatureScratch featureScratch = new FeatureScratch();
    private final PaganNetwork network = new PaganNetwork();

    public float evaluate(GameState gs, FeatureScratch scratch)
      {
        float score = 0.0f;

        if(gs == null)
          throw new IllegalArgumentException("GameState argument cannot be null");
        if(scratch == null)
          throw new IllegalArgumentException("FeatureScratch argument cannot be null");

        score += MATERIAL * material(gs);
        score += MOBILITY * mobility(gs);
        score += KING_SAFETY * kingSafety(gs);
        score += PAWN_STRUCTURE * pawnStructure(gs);
        score += TEMPO * tempo(gs);

        return score;
      }

    private static float material(GameState gs)
      {
        return 0.0f;
      }

    private static float mobility(GameState gs)
      {
        return 0.0f;
      }

    private static float kingSafety(GameState gs)
      {
        return 0.0f;
      }

    private static float pawnStructure(GameState gs)
      {
        return 0.0f;
      }

    private static float tempo(GameState gs)
      {
        return 0.0f;
      }
  }