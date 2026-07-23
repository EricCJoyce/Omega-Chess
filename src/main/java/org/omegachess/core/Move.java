package org.omegachess.core;

public final class Move
  {
    public final int from;
    public final int to;
    public final byte promo;

    public Move(int from, int to, byte promo)
      {
        this.from = from;
        this.to = to;
        this.promo = promo;
      }
  }