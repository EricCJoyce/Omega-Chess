package org.omegachess.core;

public final class Move
  {
    public int from;
    public int to;
    public byte promo;

    public Move(int a, int b, byte p)
      {
        from = a;
        to = b;
        promo = p;
      }
  }