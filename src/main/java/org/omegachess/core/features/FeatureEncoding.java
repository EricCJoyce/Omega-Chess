package org.omegachess.core.features;

public final class FeatureEncoding
  {
    public static void encode(GameState state, float[] output, FeatureScratch scratch)
      {
        FeatureSpec.clear(output);
        BoardGeometry.writeValidMask(output);
        TacticalAnalyzer.analyze(state, scratch);

        writeOccupancy(state, output);
        writeAttacks(scratch, output);
        writeXrays(scratch, output);
        writePins(scratch, output);
        writeKingFeatures(state, scratch, output);
        writeMobility(scratch, output);
        writeDefendedAndUndefended(state, scratch, output);
        writeRuleState(state, output);
      }
  }