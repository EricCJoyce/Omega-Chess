package org.omegachess.core.features;

/* hold preallocated working arrays. */
public final class FeatureScratch
  {
    public final int[] ownAttackCounts = new int[144];
    public final int[] enemyAttackCounts = new int[144];

    public final byte[] ownPieceAttacks = new byte[8 * 144];
    public final byte[] enemyPieceAttacks = new byte[8 * 144];

    public final byte[] ownXrays = new byte[3 * 144];
    public final byte[] enemyXrays = new byte[3 * 144];

    //  Pins, mobility, checkers, and temporary board data.
  }