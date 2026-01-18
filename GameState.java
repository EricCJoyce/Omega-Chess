public class GameState
  {
    public static final int _NONE                 = 144;
    public static final char _NO_PROMO            = 0x00;
    public static final char _PROMO_KNIGHT        = 0x01;
    public static final char _PROMO_CHAMPION      = 0x02;
    public static final char _PROMO_WIZARD        = 0x03;
    public static final char _PROMO_BISHOP        = 0x04;
    public static final char _PROMO_ROOK          = 0x05;
    public static final char _PROMO_QUEEN         = 0x06;

    public static final char _WHITE_PAWN          = 0x01;
    public static final char _WHITE_KNIGHT        = 0x02;
    public static final char _WHITE_CHAMPION      = 0x03;
    public static final char _WHITE_WIZARD        = 0x04;
    public static final char _WHITE_BISHOP        = 0x05;
    public static final char _WHITE_ROOK          = 0x06;
    public static final char _WHITE_QUEEN         = 0x07;
    public static final char _WHITE_KING          = 0x08;

    public static final char _BLACK_PAWN          = 0x09;
    public static final char _BLACK_KNIGHT        = 0x0A;
    public static final char _BLACK_CHAMPION      = 0x0B;
    public static final char _BLACK_WIZARD        = 0x0C;
    public static final char _BLACK_BISHOP        = 0x0D;
    public static final char _BLACK_ROOK          = 0x0E;
    public static final char _BLACK_QUEEN         = 0x0F;
    public static final char _BLACK_KING          = 0x10;

    public static final char _WHITE_TO_MOVE       = 0x00;
    public static final char _BLACK_TO_MOVE       = 0x01;

    public static final char GAME_ONGOING         = 0x00;
    public static final char GAME_OVER_WHITE_WINS = 0x01;
    public static final char GAME_OVER_BLACK_WINS = 0x02;
    public static final char GAME_OVER_STALEMATE  = 0x03;

    public static final int _GAMESTATE_BYTE_SIZE  = 67;             //  Number of bytes needed to store a GameState structure.
    public static final int _MOVE_BYTE_SIZE       = 3;              //  Number of bytes needed to store a Move structure.
    public static final int _MAX_NUM_TARGETS      = 32;             //  A (generous) upper bound on how many distinct destinations (not distinct moves)
                                                                    //  may be available to a player from a single index.
    public static final int _MAX_MOVES            = 64;             //  A (generous) upper bound on how many moves are available to a team in a single turn.

    private char board[];                                           //  144 characters for 144 squares.
    private boolean whiteKingsideLiberty;                           //  6 booleans
    private boolean whiteQueensideLiberty;
    private boolean blackKingsideLiberty;
    private boolean blackQueensideLiberty;
    private boolean whiteHasCastled;
    private boolean blackHasCastled;
    private int previousPawnMove;                                   //  Indicate which column:             {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
                                                                    //  If it was a double move, these are { 1,  2,  3,  4,  5,  6,  7,  8,  9, 10};
                                                                    //  If it was a triple move, these are {11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
    private char moveCtr;                                           //  At 50, call it a draw.
    private boolean whiteToMove;                                    //  Whether white is to move.

    /*****************************************************************
      Constructor

                          w                     w
                            c r n b q k b n r c
                            p p p p p p p p p p
                            . . . . . . . . . .
                            . . . . . . . . . .
                            . . . . . . . . . .
                            . . . . . . . . . .
                            . . . . . . . . . .
                            . . . . . . . . . .
                            P P P P P P P P P P
                            C R N B Q K B N R C
                          W                     W                   */

    public GameState()                                              //  Default board is starting position.
      {
        int i;
        board = new char[_NONE];
        for(i = 0; i < _NONE; i++)
          board[i] = 0x00;

        board[0]  = _WHITE_WIZARD;
        board[11] = _WHITE_WIZARD;
        board[13] = _WHITE_CHAMPION;
        board[14] = _WHITE_ROOK;
        board[15] = _WHITE_KNIGHT;
        board[16] = _WHITE_BISHOP;
        board[17] = _WHITE_QUEEN;
        board[18] = _WHITE_KING;
        board[19] = _WHITE_BISHOP;
        board[20] = _WHITE_KNIGHT;
        board[21] = _WHITE_ROOK;
        board[22] = _WHITE_CHAMPION;

        for(i = 25; i < 35; i++)
          board[i] = _WHITE_PAWN;

        for(i = 109; i < 119; i++)
          board[i] = _BLACK_PAWN;

        board[121] = _BLACK_CHAMPION;
        board[122] = _BLACK_ROOK;
        board[123] = _BLACK_KNIGHT;
        board[124] = _BLACK_BISHOP;
        board[125] = _BLACK_QUEEN;
        board[126] = _BLACK_KING;
        board[127] = _BLACK_BISHOP;
        board[128] = _BLACK_KNIGHT;
        board[129] = _BLACK_ROOK;
        board[130] = _BLACK_CHAMPION;
        board[132] = _BLACK_WIZARD;
        board[143] = _BLACK_WIZARD;

        whiteKingsideLiberty = true;
        whiteQueensideLiberty = true;
        blackKingsideLiberty = true;
        blackQueensideLiberty = true;
        whiteHasCastled = false;
        blackHasCastled = false;
        previousPawnMove = 0x00;

        moveCtr = 0x00;
        whiteToMove = true;
      }

    /*****************************************************************
      Terminal testing  */

    public int isWin()
      {
        int i;
        int kpos = 0;
        int wMatNonK = 0, bMatNonK = 0;                             //  Counts of pieces other than Kings.
        Move[] moves;

        if(white)                                                   //  Get moves for side to move.
          moves = getMoves('w');
        else
          moves = getMoves('b');

        for(i = 0; i < _NONE; i++)                                  //  Count up all pieces that are not a King.
          {
            if(!isEmpty(i) && !isKing(i))
              {
                if(isWhite(i))
                  wMatNonK++;
                else
                  bMatNonK++;
              }
          }

        if(moves.length == 0)                                       //  Game is over if side to move cannot move.
          {
            if(white)
              {
                while(kpos < _NONE && board[kpos] != 'K')
                  kpos++;
              }
            else
              {
                while(kpos < _NONE && board[kpos] != 'k')
                  kpos++;
              }

            if(kpos == _NONE)                                       //  Kings should never be off the board!
              System.out.println("ERROR: King not found!");

            if(white)
              {
                if(inCheckBy(kpos, 'b'))
                  return GAME_OVER_BLACK_WINS;
                return GAME_OVER_STALEMATE;
              }
            else
              {
                if(inCheckBy(kpos, 'w'))
                  return GAME_OVER_WHITE_WINS;
                return GAME_OVER_STALEMATE;
              }
          }
        else if(wMatNonK == 0 && bMatNonK == 0)                     //  Game is over if only Kings remain.
          return GAME_OVER_STALEMATE;

        return GAME_ONGOING;
      }

    public boolean terminal()
      {
        int win;
        win = isWin();
        return (win != GAME_ONGOING);
      }

    /*****************************************************************
      Identity testing  */

    /*  Is the given index i vacant? */
    public boolean isEmpty(int i)
      {
        return (board[i] == 0x00);
      }

    /*  Is the given index i occupied by a Black piece? */
    public boolean isBlack(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] >= _BLACK_PAWN && board[i] <= _BLACK_KING);
        return false;
      }

    /*  Is the given index i occupied by a White piece? */
    public boolean isWhite(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] >= _WHITE_PAWN && board[i] <= _WHITE_KING);
        return false;
      }

    /*  Is index i the same as index j
        in terms of both being White or both being Black or both being Empty? */
    public boolean sameSide(int i, int j)
      {
        return ((isWhite(i) && isWhite(j)) || (isBlack(i) && isBlack(j)));
      }

    /*  More specific than same(), this function asks,
        "Are i and j on opposite teams?" */
    public boolean opposed(int i, int j)
      {
        return ((isWhite(i) && isBlack(j)) || (isBlack(i) && isWhite(j)));
      }

    /*  Is the given index i occupied by a Pawn? */
    public boolean isPawn(int i)
      {
        if(i < _NONE)
          return (board[i] == _WHITE_PAWN || board[i] == _BLACK_PAWN);
        return false;
      }

    /*  Is the given index i occupied by a Knight? */
    public boolean isKnight(int i)
      {
        if(i < _NONE)
          return (board[i] == _WHITE_KNIGHT || board[i] == _BLACK_KNIGHT);
        return false;
      }

    /*  Is the given index i occupied by a Champion? */
    public boolean isChampion(int i)
      {
        if(i < _NONE)
          return (board[i] == _WHITE_CHAMPION || board[i] == _BLACK_CHAMPION);
        return false;
      }

    /*  Is the given index i occupied by a Wizard? */
    public boolean isWizard(int i)
      {
        if(i < _NONE)
          return (board[i] == _WHITE_WIZARD || board[i] == _BLACK_WIZARD);
        return false;
      }

    /*  Is the given index i occupied by a Bishop? */
    public boolean isBishop(int i)
      {
        if(i < _NONE)
          return (board[i] == _WHITE_BISHOP || board[i] == _BLACK_BISHOP);
        return false;
      }

    /*  Is the given index i occupied by a Rook? */
    public boolean isRook(int i)
      {
        if(i < _NONE)
          return (board[i] == _WHITE_ROOK || board[i] == _BLACK_ROOK);
        return false;
      }

    /*  Is the given index i occupied by a Queen? */
    public boolean isQueen(int i)
      {
        if(i < _NONE)
          return (board[i] == _WHITE_QUEEN || board[i] == _BLACK_KING);
        return false;
      }

    /*  Is the given index i occupied by an King? */
    public boolean isKing(int i)
      {
        if(i < _NONE)
          return (board[i] == _WHITE_KING || board[i] == _BLACK_KING);
        return false;
      }

    /*****************************************************************
      Set-builders  */

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int[] uSet(int index, boolean[] flags)
      {
        int len = 0;
        int dst = u(index);

        while(!oob(dst))                                            //  First pass: count up.
          {
            if(isEmpty(dst))
              {
                len++;
                dst = u(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    len++;
                    dst = u(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    len++;
                    dst = u(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
          }

        if(len > 0)
          {
            int indices[] = new int[len];
            len = 0;                                                //  Reset
            dst = u(index);

            while(!oob(dst))                                        //  Second pass: fill in.
              {
                if(isEmpty(dst))
                  {
                    indices[len] = dst;
                    len++;
                    dst = u(dst);
                  }
                else if(isWhite(dst))
                  {
                    if(flags[0])                                    //  Pass through White
                      {
                        indices[len] = dst;
                        len++;
                        dst = u(dst);
                      }
                    else if(flags[1])                               //  Stop and include White
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
                else if(isBlack(dst))
                  {
                    if(flags[2])                                    //  Pass through Black
                      {
                        indices[len] = dst;
                        len++;
                        dst = u(dst);
                      }
                    else if(flags[3])                               //  Stop and include Black
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int[] dSet(int index, boolean[] flags)
      {
        int len = 0;
        int dst = d(index);

        while(!oob(dst))                                            //  First pass: count up.
          {
            if(isEmpty(dst))
              {
                len++;
                dst = d(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    len++;
                    dst = d(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    len++;
                    dst = d(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
          }

        if(len > 0)
          {
            int indices[] = new int[len];
            len = 0;                                                //  Reset
            dst = d(index);

            while(!oob(dst))                                        //  Second pass: fill in.
              {
                if(isEmpty(dst))
                  {
                    indices[len] = dst;
                    len++;
                    dst = d(dst);
                  }
                else if(isWhite(dst))
                  {
                    if(flags[0])                                    //  Pass through White
                      {
                        indices[len] = dst;
                        len++;
                        dst = d(dst);
                      }
                    else if(flags[1])                               //  Stop and include White
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
                else if(isBlack(dst))
                  {
                    if(flags[2])                                    //  Pass through Black
                      {
                        indices[len] = dst;
                        len++;
                        dst = d(dst);
                      }
                    else if(flags[3])                               //  Stop and include Black
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int[] lSet(int index, boolean[] flags)
      {
        int len = 0;
        int dst = l(index);

        while(!oob(dst))                                            //  First pass: count up.
          {
            if(isEmpty(dst))
              {
                len++;
                dst = l(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    len++;
                    dst = l(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    len++;
                    dst = l(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
          }

        if(len > 0)
          {
            int indices[] = new int[len];
            len = 0;                                                //  Reset
            dst = l(index);

            while(!oob(dst))                                        //  Second pass: fill in.
              {
                if(isEmpty(dst))
                  {
                    indices[len] = dst;
                    len++;
                    dst = l(dst);
                  }
                else if(isWhite(dst))
                  {
                    if(flags[0])                                    //  Pass through White
                      {
                        indices[len] = dst;
                        len++;
                        dst = l(dst);
                      }
                    else if(flags[1])                               //  Stop and include White
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
                else if(isBlack(dst))
                  {
                    if(flags[2])                                    //  Pass through Black
                      {
                        indices[len] = dst;
                        len++;
                        dst = l(dst);
                      }
                    else if(flags[3])                               //  Stop and include Black
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int[] rSet(int index, boolean[] flags)
      {
        int len = 0;
        int dst = r(index);

        while(!oob(dst))                                            //  First pass: count up.
          {
            if(isEmpty(dst))
              {
                len++;
                dst = r(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    len++;
                    dst = r(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    len++;
                    dst = r(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
          }

        if(len > 0)
          {
            int indices[] = new int[len];
            len = 0;                                                //  Reset
            dst = r(index);

            while(!oob(dst))                                        //  Second pass: fill in.
              {
                if(isEmpty(dst))
                  {
                    indices[len] = dst;
                    len++;
                    dst = r(dst);
                  }
                else if(isWhite(dst))
                  {
                    if(flags[0])                                    //  Pass through White
                      {
                        indices[len] = dst;
                        len++;
                        dst = r(dst);
                      }
                    else if(flags[1])                               //  Stop and include White
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
                else if(isBlack(dst))
                  {
                    if(flags[2])                                    //  Pass through Black
                      {
                        indices[len] = dst;
                        len++;
                        dst = r(dst);
                      }
                    else if(flags[3])                               //  Stop and include Black
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int[] urSet(int index, boolean[] flags)
      {
        int len = 0;
        int dst = ur(index);

        while(!oob(dst))                                            //  First pass: count up.
          {
            if(isEmpty(dst))
              {
                len++;
                dst = ur(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    len++;
                    dst = ur(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    len++;
                    dst = ur(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
          }

        if(len > 0)
          {
            int indices[] = new int[len];
            len = 0;                                                //  Reset
            dst = ur(index);

            while(!oob(dst))                                        //  Second pass: fill in.
              {
                if(isEmpty(dst))
                  {
                    indices[len] = dst;
                    len++;
                    dst = ur(dst);
                  }
                else if(isWhite(dst))
                  {
                    if(flags[0])                                    //  Pass through White
                      {
                        indices[len] = dst;
                        len++;
                        dst = ur(dst);
                      }
                    else if(flags[1])                               //  Stop and include White
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
                else if(isBlack(dst))
                  {
                    if(flags[2])                                    //  Pass through Black
                      {
                        indices[len] = dst;
                        len++;
                        dst = ur(dst);
                      }
                    else if(flags[3])                               //  Stop and include Black
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int[] drSet(int index, boolean[] flags)
      {
        int len = 0;
        int dst = dr(index);

        while(!oob(dst))                                            //  First pass: count up.
          {
            if(isEmpty(dst))
              {
                len++;
                dst = dr(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    len++;
                    dst = dr(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    len++;
                    dst = dr(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
          }

        if(len > 0)
          {
            int indices[] = new int[len];
            len = 0;                                                //  Reset
            dst = dr(index);

            while(!oob(dst))                                        //  Second pass: fill in.
              {
                if(isEmpty(dst))
                  {
                    indices[len] = dst;
                    len++;
                    dst = dr(dst);
                  }
                else if(isWhite(dst))
                  {
                    if(flags[0])                                    //  Pass through White
                      {
                        indices[len] = dst;
                        len++;
                        dst = dr(dst);
                      }
                    else if(flags[1])                               //  Stop and include White
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
                else if(isBlack(dst))
                  {
                    if(flags[2])                                    //  Pass through Black
                      {
                        indices[len] = dst;
                        len++;
                        dst = dr(dst);
                      }
                    else if(flags[3])                               //  Stop and include Black
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int[] dlSet(int index, boolean[] flags)
      {
        int len = 0;
        int dst = dl(index);

        while(!oob(dst))                                            //  First pass: count up.
          {
            if(isEmpty(dst))
              {
                len++;
                dst = dl(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    len++;
                    dst = dl(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    len++;
                    dst = dl(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
          }

        if(len > 0)
          {
            int indices[] = new int[len];
            len = 0;                                                //  Reset
            dst = dl(index);

            while(!oob(dst))                                        //  Second pass: fill in.
              {
                if(isEmpty(dst))
                  {
                    indices[len] = dst;
                    len++;
                    dst = dl(dst);
                  }
                else if(isWhite(dst))
                  {
                    if(flags[0])                                    //  Pass through White
                      {
                        indices[len] = dst;
                        len++;
                        dst = dl(dst);
                      }
                    else if(flags[1])                               //  Stop and include White
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
                else if(isBlack(dst))
                  {
                    if(flags[2])                                    //  Pass through Black
                      {
                        indices[len] = dst;
                        len++;
                        dst = dl(dst);
                      }
                    else if(flags[3])                               //  Stop and include Black
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int[] ulSet(int index, boolean[] flags)
      {
        int len = 0;
        int dst = ul(index);

        while(!oob(dst))                                            //  First pass: count up.
          {
            if(isEmpty(dst))
              {
                len++;
                dst = ul(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    len++;
                    dst = ul(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    len++;
                    dst = ul(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    len++;
                    break;
                  }
                else
                  break;
              }
          }

        if(len > 0)
          {
            int indices[] = new int[len];
            len = 0;                                                //  Reset
            dst = ul(index);

            while(!oob(dst))                                        //  Second pass: fill in.
              {
                if(isEmpty(dst))
                  {
                    indices[len] = dst;
                    len++;
                    dst = ul(dst);
                  }
                else if(isWhite(dst))
                  {
                    if(flags[0])                                    //  Pass through White
                      {
                        indices[len] = dst;
                        len++;
                        dst = ul(dst);
                      }
                    else if(flags[1])                               //  Stop and include White
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
                else if(isBlack(dst))
                  {
                    if(flags[2])                                    //  Pass through Black
                      {
                        indices[len] = dst;
                        len++;
                        dst = ul(dst);
                      }
                    else if(flags[3])                               //  Stop and include Black
                      {
                        indices[len] = dst;
                        break;
                      }
                    else
                      break;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /*****************************************************************
      Board logic  */

    /*  Return the index UP from the given i. */
    public int u(int i)
      {
        if(!oob(i))
          {
            if(row(i + 12) == row(i) + 1)
              return i + 12;
          }
        return _NONE;
      }

    /*  Return the index DOWN from the given i. */
    public int d(int i)
      {
        if(!oob(i))
          {
            if(row(i - 12) == row(i) - 1 && row(i) != _NONE)
              return i - 12;
          }
        return _NONE;
      }

    /*  Return the index LEFT from the given i. */
    public int l(int i)
      {
        if(!oob(i))
          {
            if(row(i - 1) == row(i))
              return i - 1;
          }
        return _NONE;
      }

    /*  Return the index RIGHT from the given i. */
    public int r(int i)
      {
        if(!oob(i))
          {
            if(row(i + 1) == row(i))
              return i + 1;
          }
        return _NONE;
      }

    /*  Return the index UP-LEFT from the given i. */
    public int ul(int i)
      {
        if(!oob(i))
          {
            if(row(i + 11) == row(i) + 1)
              return i + 11;
          }
        return _NONE;
      }

    /*  Return the index UP-RIGHT from the given i. */
    public int ur(int i)
      {
        if(!oob(i))
          {
            if(row(i + 13) == row(i) + 1)
              return i + 13;
          }
        return _NONE;
      }

    /*  Return the index DOWN-LEFT from the given i. */
    public int dl(int i)
      {
        if(!oob(i))
          {
            if(row(i - 13) == row(i) - 1 && row(i) != _NONE)
              return i - 13;
          }
        return _NONE;
      }

    /*  Return the index DOWN-RIGHT from the given i. */
    public int dr(int i)
      {
        if(!oob(i))
          {
            if(row(i - 11) == row(i) - 1 && row(i) != _NONE)
              return i - 11;
          }
        return _NONE;
      }

    /*  Compute the COLUMN in which given index is included. */
    public int col(int i)
      {
        if(i < _NONE && i >= 0)
          return i % 12;
        return _NONE;
      }

    /*  Compute the ROW in which given index is included. */
    public int row(int i)
      {
        if(i < _NONE && i >= 0)
          return (i - (i % 12)) / 12;
        return _NONE;
      }

    /* Is the given index out of bounds or == _NONE? */
    public boolean oob(int i)
      {
        if(i < _NONE && i >= 0)
          {
            if(row(i) == 0  && col(i) > 0 && col(i) < 11)
              return true;
            if(row(i) == 11 && col(i) > 0 && col(i) < 11)
              return true;
            if(col(i) == 0  && row(i) > 0 && row(i) < 11)
              return true;
            if(col(i) == 11 && row(i) > 0 && row(i) < 11)
              return true;

            return false;
          }

        return true;
      }

    public int[] getCol(int index)
      {
        int i, j;

        if(col(index) == 0 || col(index) == 11)
          {
            int indices[] = new int[2];
            if(col(index) == 0)
              {
                indices[0] = 0;
                indices[1] = 132;
              }
            else
              {
                indices[0] = 11;
                indices[1] = 143;
              }
            return indices;
          }
        else
          {
            int indices[] = new int[10];
            if(index < _NONE)
              {
                j = col(index) + 12;
                for(i = 0; i < 10; i++)
                  indices[i] = j + i * 12;
              }
            else
              {
                for(i = 0; i < 10; i++)
                  indices[i] = _NONE;
              }
            return indices;
          }
      }

    public int[] getRow(int index)
      {
        int i, j;

        if(row(index) == 0 || row(index) == 11)
          {
            int indices[] = new int[2];
            if(row(index) == 0)
              {
                indices[0] = 0;
                indices[1] = 11;
              }
            else
              {
                indices[0] = 132;
                indices[1] = 143;
              }
            return indices;
          }
        else
          {
            int indices[] = new int[10];
            if(index < _NONE)
              {
                j = row(index) * 12 + 1;
                for(i = 0; i < 10; i++)
                  indices[i] = j + i;
              }
            else
              {
                for(i = 0; i < 10; i++)
                  indices[i] = _NONE;
              }
            return indices;
          }
      }
  }