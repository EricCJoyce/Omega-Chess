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
    public static final int _MAX_NUM_TARGETS      = 64;             //  A (generous) upper bound on how many distinct destinations (not distinct moves)
                                                                    //  may be available to a player from a single index.
    public static final int _MAX_MOVES            = 128;            //  A (generous) upper bound on how many moves are available to a team in a single turn.

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
      Moves  */

    /* Is the given "index" attackable by any members of "team"? ("team" is in {'w', 'b'}.) */
    public boolean inCheckBy(int index, char team)
      {
        Move[] attacks;
        int[] enemytargets;
        boolean ret = false;
        int enemyStrikeCtr = 0;
        int i, j;

        for(i = 0; i < _NONE; i++)
          {
            if((isWhite(i) && team == 'w') || (isBlack(i) && team == 'b'))
              {
                if(isPawn(i))
                  attacks = getPawnAttacks(i);
                else if(isKnight(i))
                  attacks = getKnightMoves(i);
                else if(isChampion(i))
                  attacks = getChampionMoves(i);
                else if(isWizard(i))
                  attacks = getWizardMoves(i);
                else if(isBishop(i))
                  attacks = getBishopMoves(i);
                else if(isRook(i))
                  attacks = getRookMoves(i);
                else if(isQueen(i))
                  attacks = getQueenMoves(i);
                else
                  attacks = getKingNonCastle(i);

                enemyStrikeCtr += attacks.length;                   //  Count up strikes.
              }
          }

        if(enemyStrikeCtr > 0)                                      //  We've counted... now allocate.
          {
            enemytargets = new int[enemyStrikeCtr];
            enemyStrikeCtr = 0;                                     //  Reset.
            for(i = 0; i < _NONE; i++)
              {
                if((isWhite(i) && team == 'w') || (isBlack(i) && team == 'b'))
                  {
                    if(isPawn(i))
                      attacks = getPawnAttacks(i);
                    else if(isKnight(i))
                      attacks = getKnightMoves(i);
                    else if(isChampion(i))
                      attacks = getChampionMoves(i);
                    else if(isWizard(i))
                      attacks = getWizardMoves(i);
                    else if(isBishop(i))
                      attacks = getBishopMoves(i);
                    else if(isRook(i))
                      attacks = getRookMoves(i);
                    else if(isQueen(i))
                      attacks = getQueenMoves(i);
                    else
                      attacks = getKingNonCastle(i);

                    for(j = 0; j < attacks.length; j++)
                      enemytargets[enemyStrikeCtr + j] = attacks[j].to;

                    enemyStrikeCtr += attacks.length;               //  Increase offset.
                  }
              }

            i = 0;
            while(i < enemyStrikeCtr && enemytargets[i] != index)
              i++;

            if(i < enemyStrikeCtr)
              ret = true;
          }

        return ret;
      }

    /* This means, "Can I castle RIGHT NOW?" Not, "Do I still have Kingside rights?" */
    public boolean canKingsideCastle(char team)
      {
        boolean c = false;
        if(team == 'b')
          {
            if(blackKingsideLiberty)
              {
                if(isRook(129) && isBlack(129))
                  {
                    if(!inCheckBy(126, 'w') && !inCheckBy(127, 'w') && !inCheckBy(128, 'w') && isEmpty(127) && isEmpty(128))
                    c = true;
                  }
              }
          }
        else
          {
            if(whiteKingsideLiberty)
              {
                if(isRook(21) && isWhite(21))
                  {
                    if(!inCheckBy(18, 'b') && !inCheckBy(19, 'b') && !inCheckBy(20, 'b') && isEmpty(19) && isEmpty(20))
                    c = true;
                  }
              }
          }
        return c;
      }

    /* This means, "Can I castle RIGHT NOW?" Not, "Do I still have Queenside rights?" */
    public boolean canQueensideCastle(char team)
      {
        boolean c = false;
        if(team == 'b')
          {
            if(blackQueensideLiberty)
              {
                if(isRook(56) && isBlack(56))
                  {
                    if(!inCheckBy(126, 'w') && !inCheckBy(125, 'w') && !inCheckBy(124, 'w') && isEmpty(125) && isEmpty(124) && isEmpty(123))
                    c = true;
                  }
              }
          }
        else
          {
            if(whiteQueensideLiberty)
              {
                if(isRook(14) && isWhite(14))
                  {
                    if(!inCheckBy(18, 'b') && !inCheckBy(17, 'b') && !inCheckBy(16, 'b') && isEmpty(17) && isEmpty(16) && isEmpty(15))
                    c = true;
                  }
              }
          }
        return c;
      }

    /* THIS FUNCTION FILTERS FOR CHECK!! */
    public Move[] getMoves(char team)
      {
        int movesCtr = 0;
        int index, i;

        for(index = 0; index < _NONE; index++)
          {
            if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
              {
                Move[] potentialmoves = getMovesIndex(index);
                movesCtr += potentialmoves.length;                  //  Increase count
              }
          }

        if(movesCtr > 0)
          {
            Move[] moves = new Move[movesCtr];                      //  Now that we've counted, allocate
            movesCtr = 0;                                           //  Reset
            for(index = 0; index < _NONE; index++)
              {
                if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
                  {
                    Move[] potentialmoves = getMovesIndex(index);
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                  }
              }
            return moves;
          }
        return new Move[0];
      }

    /* THIS FUNCTION FILTERS FOR CHECK!! */
    public Move[] getMovesIndex(int index)
      {
        Move[] moves;
        Move[] potentialmoves;
        char[] tmpBoard;
        boolean tmpWhiteKingsideLiberty, tmpWhiteQueensideLiberty, tmpWhiteHasCastled;
        boolean tmpBlackKingsideLiberty, tmpBlackQueensideLiberty, tmpBlackHasCastled;
        int potentialmovesCtr = 0;
        int movesCtr = 0;
        int i, k;

        if(!isEmpty(index))
          {
            if(isPawn(index))
              potentialmoves = getPawnMoves(index);
            else if(isKnight(index))
              potentialmoves = getKnightMoves(index);
            else if(isChampion(index))
              potentialmoves = getChampionMoves(index);
            else if(isWizard(index))
              potentialmoves = getWizardMoves(index);
            else if(isBishop(index))
              potentialmoves = getBishopMoves(index);
            else if(isRook(index))
              potentialmoves = getRookMoves(index);
            else if(isQueen(index))
              potentialmoves = getQueenMoves(index);
            else
              potentialmoves = getKingMoves(index);

            if(isWhite(index))                                      //  Piece is white, check for checks on the King by Black
              {
                for(i = 0; i < potentialmoves.length; i++)          //  For every move, make that move, then test the resultant board
                  {
                  	tmpBoard = new char[];
                    GameState gs = new GameState();                 //  Create a new GameState
                    gs.setBoard( board );                           //  Copy the board
                    gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                        blackKingsideLiberty, blackQueensideLiberty,
                                        whiteHasCastled, blackHasCastled );
                    gs.setPreviousMove( previousFrom, previousTo );
                    gs.setWhiteToMove( white );
                    gs.makeMove(potentialmoves[i].from,             //  Apply the candidate move
                                potentialmoves[i].to,
                                potentialmoves[i].promo);

                    k = gs.getKingIndex('w');                       //  Locate the king on the new board

                    if(k == _NONE)
                      System.out.println("ERROR: the white king is missing from the board!");

                    if(!gs.inCheckBy(k, 'b'))                       //  If king not in check,
                      movesCtr++;                                   //  then move is allowed
                  }
                if(movesCtr > 0)                                    //  At least one move avoids check
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;                                   //  Reset

                    for(i = 0; i < potentialmoves.length; i++)      //  For every move, make that move, then test the resultant board
                      {
                        GameState gs = new GameState();             //  Create a new GameState
                        gs.setBoard( board );                       //  Copy the board
                        gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                            blackKingsideLiberty, blackQueensideLiberty,
                                            whiteHasCastled, blackHasCastled );
                        gs.setPreviousMove( previousFrom, previousTo );
                        gs.setWhiteToMove( white );
                        gs.makeMove(potentialmoves[i].from,         //  Apply the candidate move
                                    potentialmoves[i].to,
                                    potentialmoves[i].promo);

                        k = gs.getKingIndex('w');                   //  Locate the king on the new board

                        if(k == _NONE)
                          System.out.println("ERROR: the white king is missing from the board!");

                        if(!gs.inCheckBy(k, 'b'))                   //  If king not in check,
                          {                                         //  then move is allowed
                            moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                            movesCtr++;
                          }
                      }

                    return moves;
                  }
              }
            else                                                    //  Piece is black, check for checks on the King by White
              {
                for(i = 0; i < potentialmoves.length; i++)          //  For every move, make that move, then test the resultant board
                  {
                    GameState gs = new GameState();                 //  Create a new GameState
                    gs.setBoard( board );                           //  Copy the board
                    gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                        blackKingsideLiberty, blackQueensideLiberty,
                                        whiteHasCastled, blackHasCastled );
                    gs.setPreviousMove( previousFrom, previousTo );
                    gs.setWhiteToMove( white );
                    gs.makeMove(potentialmoves[i].from,             //  Apply the candidate move
                                potentialmoves[i].to,
                                potentialmoves[i].promo);

                    k = gs.getKingIndex('b');                       //  Locate the king on the new board

                    if(k == _NONE)
                      System.out.println("ERROR: the black king is missing from the board!");

                    if(!gs.inCheckBy(k, 'w'))                       //  If king not in check,
                      movesCtr++;                                   //  then move is allowed
                  }
                if(movesCtr > 0)                                    //  At least one move avoids check
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;                                   //  Reset

                    for(i = 0; i < potentialmoves.length; i++)      //  For every move, make that move, then test the resultant board
                      {
                        GameState gs = new GameState();             //  Create a new GameState
                        gs.setBoard( board );                       //  Copy the board
                        gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                            blackKingsideLiberty, blackQueensideLiberty,
                                            whiteHasCastled, blackHasCastled );
                        gs.setPreviousMove( previousFrom, previousTo );
                        gs.setWhiteToMove( white );
                        gs.makeMove(potentialmoves[i].from,         //  Apply the candidate move
                                    potentialmoves[i].to,
                                    potentialmoves[i].promo);

                        k = gs.getKingIndex('b');                   //  Locate the king on the new board

                        if(k == _NONE)
                          System.out.println("ERROR: the black king is missing from the board!");

                        if(!gs.inCheckBy(k, 'w'))                   //  If king not in check,
                          {                                         //  then move is allowed
                            moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                            movesCtr++;
                          }
                      }

                    return moves;
                  }
              }
          }

        return new Move[0];
      }

    public Move[] getPawnMoves(int index)
      {
        Move[] moves;
        Move[] enpassant;
        int movesCtr = 0;
        int i, len = 0;

        if(isWhite(index))
          {
                                                                    //  White's double Pawn move
            if(row(index) == 2 && !oob(u(u(index))) && isEmpty(u(index)) && isEmpty(u(u(index))))
              movesCtr++;
                                                                    //  White's triple Pawn move
            if(row(index) == 2 && !oob(u(u(u(index)))) && isEmpty(u(index)) && isEmpty(u(u(index))) && isEmpty(u(u(u(index)))))
              movesCtr++;

            if(!oob(u(index)) && isEmpty(u(index)))                 //  White Pawn forward, possibly to promotion
              {
                if(row(u(index)) == 10)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
                                                                    //  White Pawn attack forward-left, possibly to promotion
            if(!oob(ul(index)) && row(ul(index)) == row(index) + 1 && col(ul(index)) == col(index) - 1 && isBlack(ul(index)))
              {
                if(row(ul(index)) == 10)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
                                                                    //  White Pawn attack forward-right, possibly to promotion
            if(!oob(ur(index)) && row(ur(index)) == row(index) + 1 && col(ur(index)) == col(index) + 1 && isBlack(ur(index)))
              {
                if(row(ur(index)) == 10)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
            enpassant = getPawnEnPassantAttacks(index);
            movesCtr += enpassant.length;

            if(movesCtr > 0)                                        //  At least one move
              {
                moves = new Move[movesCtr];                         //  We've counted, now allocate
                movesCtr = 0;                                       //  Reset
                                                                    //  White's double Pawn move
                if(row(index) == 2 && !oob(u(u(index))) && isEmpty(u(index)) && isEmpty(u(u(index))))
                  {
                    moves[movesCtr] = new Move(index, u(u(index)), _NO_PROMO);
                    movesCtr++;
                  }
                                                                    //  White's triple Pawn move
                if(row(index) == 2 && !oob(u(u(u(index)))) && isEmpty(u(index)) && isEmpty(u(u(index))) && isEmpty(u(u(u(index)))))
                  {
                    moves[movesCtr] = new Move(index, u(u(u(index))), _NO_PROMO);
                    movesCtr++;
                  }

                if(!oob(u(index)) && isEmpty(u(index)))             //  White Pawn forward, possibly to promotion
                  {
                    if(row(u(index)) == 10)
                      {
                        moves[movesCtr] = new Move(index, u(index), 'N');
                        moves[movesCtr + 1] = new Move(index, u(index), 'C');
                        moves[movesCtr + 2] = new Move(index, u(index), 'W');
                        moves[movesCtr + 3] = new Move(index, u(index), 'I');
                        moves[movesCtr + 4] = new Move(index, u(index), 'R');
                        moves[movesCtr + 5] = new Move(index, u(index), 'Q');
                        movesCtr += 6;
                      }
                    else
                      {
                        moves[movesCtr] = new Move(index, u(index), _NO_PROMO);
                        movesCtr++;
                      }
                  }
                                                                    //  White Pawn attack forward-left, possibly to promotion
                if(!oob(ul(index)) && row(ul(index)) == row(index) + 1 && col(ul(index)) == col(index) - 1 && isBlack(ul(index)))
                  {
                    if(row(ul(index)) == 10)
                      {
                        moves[movesCtr] = new Move(index, ul(index), 'N');
                        moves[movesCtr + 1] = new Move(index, ul(index), 'C');
                        moves[movesCtr + 2] = new Move(index, ul(index), 'W');
                        moves[movesCtr + 3] = new Move(index, ul(index), 'I');
                        moves[movesCtr + 4] = new Move(index, ul(index), 'R');
                        moves[movesCtr + 5] = new Move(index, ul(index), 'Q');
                        movesCtr += 6;
                      }
                    else
                      {
                        moves[movesCtr] = new Move(index, ul(index), _NO_PROMO);
                        movesCtr++;
                      }
                  }
                                                                    //  White Pawn attack forward-right, possibly to promotion
                if(!oob(ur(index)) && row(ur(index)) == row(index) + 1 && col(ur(index)) == col(index) + 1 && isBlack(ur(index)))
                  {
                    if(row(ur(index)) == 10)
                      {
                        moves[movesCtr]= new Move(index, ur(index), 'N');
                        moves[movesCtr + 1] = new Move(index, ur(index), 'C');
                        moves[movesCtr + 2] = new Move(index, ur(index), 'W');
                        moves[movesCtr + 3] = new Move(index, ur(index), 'I');
                        moves[movesCtr + 4] = new Move(index, ur(index), 'R');
                        moves[movesCtr + 5] = new Move(index, ur(index), 'Q');
                        movesCtr += 6;
                      }
                    else
                      {
                        moves[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                        movesCtr++;
                      }
                  }

                enpassant = getPawnEnPassantAttacks(index);
                for(i = 0; i < enpassant.length; i++)
                  {
                    moves[movesCtr] = new Move(index, enpassant[i].to, _NO_PROMO);
                    movesCtr++;
                  }
                return moves;
              }
          }
        else
          {
                                                                    //  Black's double Pawn move
            if(row(index) == 9 && !oob(d(d(index))) && isEmpty(d(index)) && isEmpty(d(d(index))))
              movesCtr++;
                                                                    //  Black's triple Pawn move
            if(row(index) == 9 && !oob(d(d(d(index)))) && isEmpty(d(index)) && isEmpty(d(d(index))) && isEmpty(d(d(d(index)))))
              movesCtr++;

            if(!oob(d(index)) && isEmpty(d(index)))                 //  Black Pawn forward, possibly to promotion
              {
                if(row(d(index)) == 1)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
                                                                    //  Black Pawn attack forward-left, possibly to promotion
            if(!oob(dl(index)) && row(dl(index)) == row(index) - 1 && col(dl(index)) == col(index) - 1 && isWhite(dl(index)))
              {
                if(row(dl(index)) == 1)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
                                                                    //  Black Pawn attack forward-right, possibly to promotion
            if(!oob(dr(index)) && row(dr(index)) == row(index) - 1 && col(dr(index)) == col(index) + 1 && isWhite(dr(index)))
              {
                if(row(dr(index)) == 1)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
            enpassant = getPawnEnPassantAttacks(index);
            movesCtr += enpassant.length;

            if(movesCtr > 0)                                        //  At least one move
              {
                moves = new Move[movesCtr];                         //  We've counted, now allocate
                movesCtr = 0;                                       //  Reset
                                                                    //  Black's double Pawn move
                if(row(index) == 9 && !oob(d(d(index))) && isEmpty(d(index)) && isEmpty(d(d(index))))
                  {
                    moves[movesCtr] = new Move(index, d(d(index)), _NO_PROMO);
                    movesCtr++;
                  }
                                                                    //  Black's triple Pawn move
                if(row(index) == 9 && !oob(d(d(d(index)))) && isEmpty(d(index)) && isEmpty(d(d(index))) && isEmpty(d(d(d(index)))))
                  {
                    moves[movesCtr] = new Move(index, d(d(d(index))), _NO_PROMO);
                    movesCtr++;
                  }

                if(!oob(d(index)) && isEmpty(d(index)))             //  Black Pawn forward, possibly to promotion
                  {
                    if(row(d(index)) == 1)
                      {
                        moves[movesCtr] = new Move(index, d(index), 'n');
                        moves[movesCtr + 1] = new Move(index, d(index), 'c');
                        moves[movesCtr + 2] = new Move(index, d(index), 'w');
                        moves[movesCtr + 3] = new Move(index, d(index), 'i');
                        moves[movesCtr + 4] = new Move(index, d(index), 'r');
                        moves[movesCtr + 5] = new Move(index, d(index), 'q');
                        movesCtr += 6;
                      }
                    else
                      {
                        moves[movesCtr] = new Move(index, d(index), _NO_PROMO);
                        movesCtr++;
                      }
                  }
                                                                    //  Black Pawn attack forward-left, possibly to promotion
                if(!oob(dl(index)) && row(dl(index)) == row(index) - 1 && col(dl(index)) == col(index) - 1 && isWhite(dl(index)))
                  {
                    if(row(dl(index)) == 1)
                      {
                        moves[movesCtr] = new Move(index, dl(index), 'n');
                        moves[movesCtr + 1] = new Move(index, dl(index), 'c');
                        moves[movesCtr + 2] = new Move(index, dl(index), 'w');
                        moves[movesCtr + 3] = new Move(index, dl(index), 'i');
                        moves[movesCtr + 4] = new Move(index, dl(index), 'r');
                        moves[movesCtr + 5] = new Move(index, dl(index), 'q');
                        movesCtr += 6;
                      }
                    else
                      {
                        moves[movesCtr] = new Move(index, dl(index), _NO_PROMO);
                        movesCtr++;
                      }
                  }
                                                                    //  Black Pawn attack forward-right, possibly to promotion
                if(!oob(dr(index)) && row(dr(index)) == row(index) - 1 && col(dr(index)) == col(index) + 1 && isWhite(dr(index)))
                  {
                    if(row(dr(index)) == 1)
                      {
                        moves[movesCtr] = new Move(index, dr(index), 'n');
                        moves[movesCtr + 1] = new Move(index, dr(index), 'c');
                        moves[movesCtr + 2] = new Move(index, dr(index), 'w');
                        moves[movesCtr + 3] = new Move(index, dr(index), 'i');
                        moves[movesCtr + 4] = new Move(index, dr(index), 'r');
                        moves[movesCtr + 5] = new Move(index, dr(index), 'q');
                        movesCtr += 6;
                      }
                    else
                      {
                        moves[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                        movesCtr++;
                      }
                  }

                enpassant = getPawnEnPassantAttacks(index);
                for(i = 0; i < enpassant.length; i++)
                  {
                    moves[movesCtr] = new Move(index, enpassant[i].to, _NO_PROMO);
                    movesCtr++;
                  }
                return moves;
              }
          }
        return new Move[0];
      }

    public Move[] getKnightMoves(int index)
      {
        Move[] moves;
        int movesCtr = 0;

        if(!oob(ul(u(index))) && !sameSide(ul(u(index)), index))
          movesCtr++;
        if(!oob(ur(u(index))) && !sameSide(ur(u(index)), index))
          movesCtr++;

        if(!oob(ur(r(index))) && !sameSide(ur(r(index)), index))
          movesCtr++;
        if(!oob(ul(l(index))) && !sameSide(ul(l(index)), index))
          movesCtr++;

        if(!oob(dl(d(index))) && !sameSide(dl(d(index)), index))
          movesCtr++;
        if(!oob(dr(d(index))) && !sameSide(dr(d(index)), index))
          movesCtr++;

        if(!oob(dr(r(index))) && !sameSide(dr(r(index)), index))
          movesCtr++;
        if(!oob(dl(l(index))) && !sameSide(dl(l(index)), index))
          movesCtr++;

        if(movesCtr > 0)                                            //  Moves exist
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            if(!oob(ul(u(index))) && !sameSide(ul(u(index)), index))
              {
                moves[movesCtr] = new Move(index, ul(u(index)), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ur(u(index))) && !sameSide(ur(u(index)), index))
              {
                moves[movesCtr] = new Move(index, ur(u(index)), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(ur(r(index))) && !sameSide(ur(r(index)), index))
              {
                moves[movesCtr] = new Move(index, ur(r(index)), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ul(l(index))) && !sameSide(ul(l(index)), index))
              {
                moves[movesCtr] = new Move(index, ul(l(index)), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(dl(d(index))) && !sameSide(dl(d(index)), index))
              {
                moves[movesCtr] = new Move(index, dl(d(index)), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dr(d(index))) && !sameSide(dr(d(index)), index))
              {
                moves[movesCtr] = new Move(index, dr(d(index)), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(dr(r(index))) && !sameSide(dr(r(index)), index))
              {
                moves[movesCtr] = new Move(index, dr(r(index)), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dl(l(index))) && !sameSide(dl(l(index)), index))
              {
                moves[movesCtr] = new Move(index, dl(l(index)), _NO_PROMO);
                movesCtr++;
              }

            return moves;
          }

        return new Move[0];
      }

    public Move[] getChampionMoves(int index)
      {
        Move[] moves;
        int movesCtr = 0;

        if(!oob(u(index)) && !sameSide(u(index), index))
          movesCtr++;
        if(!oob(u(u(index))) && !sameSide(u(u(index)), index))
          movesCtr++;

        if(!oob(d(index)) && !sameSide(d(index), index))
          movesCtr++;
        if(!oob(d(d(index))) && !sameSide(d(d(index)), index))
          movesCtr++;

        if(!oob(l(index)) && !sameSide(l(index), index))
          movesCtr++;
        if(!oob(l(l(index))) && !sameSide(l(l(index)), index))
          movesCtr++;

        if(!oob(r(index)) && !sameSide(r(index), index))
          movesCtr++;
        if(!oob(r(r(index))) && !sameSide(r(r(index)), index))
          movesCtr++;

        if(!oob(ul(ul(index))) && !sameSide(ul(ul(index)), index))
          movesCtr++;
        if(!oob(ur(ur(index))) && !sameSide(ur(ur(index)), index))
          movesCtr++;
        if(!oob(dr(dr(index))) && !sameSide(dr(dr(index)), index))
          movesCtr++;
        if(!oob(dl(dl(index))) && !sameSide(dl(dl(index)), index))
          movesCtr++;

        if(movesCtr > 0)                                            //  Moves exist
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            if(!oob(u(index)) && !sameSide(u(index), index))
              {
                moves[movesCtr] = new Move(index, u(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(u(u(index))) && !sameSide(u(u(index)), index))
              {
                moves[movesCtr] = new Move(index, u(u(index)), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(d(index)) && !sameSide(d(index), index))
              {
                moves[movesCtr] = new Move(index, d(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(d(d(index))) && !sameSide(d(d(index)), index))
              {
                moves[movesCtr] = new Move(index, d(d(index)), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(l(index)) && !sameSide(l(index), index))
              {
                moves[movesCtr] = new Move(index, l(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(l(l(index))) && !sameSide(l(l(index)), index))
              {
                moves[movesCtr] = new Move(index, l(l(index)), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(r(index)) && !sameSide(r(index), index))
              {
                moves[movesCtr] = new Move(index, r(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(r(r(index))) && !sameSide(r(r(index)), index))
              {
                moves[movesCtr] = new Move(index, r(r(index)), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(ul(ul(index))) && !sameSide(ul(ul(index)), index))
              {
                moves[movesCtr] = new Move(index, ul(ul(index)), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ur(ur(index))) && !sameSide(ur(ur(index)), index))
              {
                moves[movesCtr] = new Move(index, ur(ur(index)), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dr(dr(index))) && !sameSide(dr(dr(index)), index))
              {
                moves[movesCtr] = new Move(index, dr(dr(index)), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dl(dl(index))) && !sameSide(dl(dl(index)), index))
              {
                moves[movesCtr] = new Move(index, dl(dl(index)), _NO_PROMO);
                movesCtr++;
              }

            return moves;
          }

        return new Move[0];
      }

    public Move[] getWizardMoves(int index)
      {
        Move[] moves;
        int movesCtr = 0;

        if(!oob(ul(index)) && !sameSide(ul(index), index))
          movesCtr++;
        if(!oob(ur(index)) && !sameSide(ur(index), index))
          movesCtr++;
        if(!oob(dr(index)) && !sameSide(dr(index), index))
          movesCtr++;
        if(!oob(dl(index)) && !sameSide(dl(index), index))
          movesCtr++;

        if(!oob(u(u(ul(index)))) && !sameSide(u(u(ul(index))), index))
          movesCtr++;
        if(!oob(u(u(ur(index)))) && !sameSide(u(u(ur(index))), index))
          movesCtr++;

        if(!oob(l(l(ul(index)))) && !sameSide(l(l(ul(index))), index))
          movesCtr++;
        if(!oob(r(r(ur(index)))) && !sameSide(r(r(ur(index))), index))
          movesCtr++;

        if(!oob(r(r(dr(index)))) && !sameSide(r(r(dr(index))), index))
          movesCtr++;
        if(!oob(l(l(dl(index)))) && !sameSide(l(l(dl(index))), index))
          movesCtr++;

        if(!oob(d(d(dr(index)))) && !sameSide(d(d(dr(index))), index))
          movesCtr++;
        if(!oob(d(d(dl(index)))) && !sameSide(d(d(dl(index))), index))
          movesCtr++;

        if(movesCtr > 0)                                            //  Moves exist
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            if(!oob(ul(index)) && !sameSide(ul(index), index))
              {
                moves[movesCtr] = new Move(index, ul(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ur(index)) && !sameSide(ur(index), index))
              {
                moves[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dr(index)) && !sameSide(dr(index), index))
              {
                moves[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dl(index)) && !sameSide(dl(index), index))
              {
                moves[movesCtr] = new Move(index, dl(index), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(u(u(ul(index)))) && !sameSide(u(u(ul(index))), index))
              {
                moves[movesCtr] = new Move(index, u(u(ul(index))), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(u(u(ur(index)))) && !sameSide(u(u(ur(index))), index))
              {
                moves[movesCtr] = new Move(index, u(u(ur(index))), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(l(l(ul(index)))) && !sameSide(l(l(ul(index))), index))
              {
                moves[movesCtr] = new Move(index, l(l(ul(index))), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(r(r(ur(index)))) && !sameSide(r(r(ur(index))), index))
              {
                moves[movesCtr] = new Move(index, r(r(ur(index))), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(d(d(dl(index)))) && !sameSide(d(d(dl(index))), index))
              {
                moves[movesCtr] = new Move(index, d(d(dl(index))), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(d(d(dr(index)))) && !sameSide(d(d(dr(index))), index))
              {
                moves[movesCtr] = new Move(index, d(d(dr(index))), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(l(l(dl(index)))) && !sameSide(l(l(dl(index))), index))
              {
                moves[movesCtr] = new Move(index, l(l(dl(index))), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(r(r(dr(index)))) && !sameSide(r(r(dr(index))), index))
              {
                moves[movesCtr] = new Move(index, r(r(dr(index))), _NO_PROMO);
                movesCtr++;
              }
            return moves;
          }

        return new Move[0];
      }

    public Move[] getBishopMoves(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        if(isWhite(index))
          {
            flags[0] = false;
            flags[1] = false;
            flags[2] = false;
            flags[3] = true;
          }
        else
          {
            flags[0] = false;
            flags[1] = true;
            flags[2] = false;
            flags[3] = false;
          }

        tmp = ulSet(index, flags);                                  //  Up-left
        movesCtr += tmp.length;

        tmp = urSet(index, flags);                                  //  Up-right
        movesCtr += tmp.length;

        tmp = dlSet(index, flags);                                  //  Down-left
        movesCtr += tmp.length;

        tmp = drSet(index, flags);                                  //  Down-right
        movesCtr += tmp.length;

        if(movesCtr > 0)
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            tmp = ulSet(index, flags);                              //  Up-left
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = urSet(index, flags);                              //  Up-right
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = dlSet(index, flags);                              //  Down-left
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = drSet(index, flags);                              //  Down-right
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }
            return moves;
          }
        return new Move[0];
      }

    public Move[] getRookMoves(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        if(isWhite(index))
          {
            flags[0] = false;
            flags[1] = false;
            flags[2] = false;
            flags[3] = true;
          }
        else
          {
            flags[0] = false;
            flags[1] = true;
            flags[2] = false;
            flags[3] = false;
          }

        tmp = uSet(index, flags);                                   //  Up
        movesCtr += tmp.length;

        tmp = dSet(index, flags);                                   //  Down
        movesCtr += tmp.length;

        tmp = lSet(index, flags);                                   //  Left
        movesCtr += tmp.length;

        tmp = rSet(index, flags);                                   //  Right
        movesCtr += tmp.length;

        if(movesCtr > 0)
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            tmp = uSet(index, flags);                               //  Up
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = dSet(index, flags);                               //  Down
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = lSet(index, flags);                               //  Left
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = rSet(index, flags);                               //  Right
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }
            return moves;
          }
        return new Move[0];
      }

    public Move[] getQueenMoves(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        if(isWhite(index))
          {
            flags[0] = false;
            flags[1] = false;
            flags[2] = false;
            flags[3] = true;
          }
        else
          {
            flags[0] = false;
            flags[1] = true;
            flags[2] = false;
            flags[3] = false;
          }

        tmp = uSet(index, flags);                                   //  Up
        movesCtr += tmp.length;

        tmp = urSet(index, flags);                                  //  Up-right
        movesCtr += tmp.length;

        tmp = rSet(index, flags);                                   //  Right
        movesCtr += tmp.length;

        tmp = drSet(index, flags);                                  //  Downright
        movesCtr += tmp.length;

        tmp = dSet(index, flags);                                   //  Down
        movesCtr += tmp.length;

        tmp = dlSet(index, flags);                                  //  Down-left
        movesCtr += tmp.length;

        tmp = lSet(index, flags);                                   //  Left
        movesCtr += tmp.length;

        tmp = ulSet(index, flags);                                  //  Up-left
        movesCtr += tmp.length;

        if(movesCtr > 0)
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            tmp = uSet(index, flags);                               //  Up
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = urSet(index, flags);                              //  Up-right
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = rSet(index, flags);                               //  Right
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = drSet(index, flags);                              //  Down-right
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = dSet(index, flags);                               //  Down
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = dlSet(index, flags);                              //  Down-left
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = lSet(index, flags);                               //  Left
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            tmp = ulSet(index, flags);                              //  Up-left
            for(i = 0; i < tmp.length; i++)
              {
                moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                movesCtr++;
              }

            return moves;
          }
        return new Move[0];
      }

    public Move[] getKingMoves(int index)
      {
        Move[] moves;
        int movesCtr = 0;

        if(!oob(u(index)) && !sameSide(u(index), index))
          movesCtr++;
        if(!oob(ur(index)) && !sameSide(ur(index), index))
          movesCtr++;
        if(!oob(r(index)) && !sameSide(r(index), index))
          movesCtr++;
        if(!oob(dr(index)) && !sameSide(dr(index), index))
          movesCtr++;
        if(!oob(d(index)) && !sameSide(d(index), index))
          movesCtr++;
        if(!oob(dl(index)) && !sameSide(dl(index), index))
          movesCtr++;
        if(!oob(l(index)) && !sameSide(l(index), index))
          movesCtr++;
        if(!oob(ul(index)) && !sameSide(ul(index), index))
          movesCtr++;
        if(isWhite(index))
          {
            if(canKingsideCastle('w'))
              movesCtr++;
            if(canQueensideCastle('w'))
              movesCtr++;
          }
        else
          {
            if(canKingsideCastle('b'))
              movesCtr++;
            if(canQueensideCastle('b'))
              movesCtr++;
          }

        if(movesCtr > 0)                                            //  Any moves at all?
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            if(!oob(u(index)) && !sameSide(u(index), index))
              {
                moves[movesCtr] = new Move(index, u(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ur(index)) && !sameSide(ur(index), index))
              {
                moves[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(r(index)) && !sameSide(r(index), index))
              {
                moves[movesCtr] = new Move(index, r(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dr(index)) && !sameSide(dr(index), index))
              {
                moves[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(d(index)) && !sameSide(d(index), index))
              {
                moves[movesCtr] = new Move(index, d(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dl(index)) && !sameSide(dl(index), index))
              {
                moves[movesCtr] = new Move(index, dl(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(l(index)) && !sameSide(l(index), index))
              {
                moves[movesCtr] = new Move(index, l(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ul(index)) && !sameSide(ul(index), index))
              {
                moves[movesCtr] = new Move(index, ul(index), _NO_PROMO);
                movesCtr++;
              }
            if(isWhite(index))
              {
                if(canKingsideCastle('w'))
                  {
                    moves[movesCtr] = new Move(18, 20, _NO_PROMO);
                    movesCtr++;
                  }
                if(canQueensideCastle('w'))
                  {
                    moves[movesCtr] = new Move(18, 16, _NO_PROMO);
                    movesCtr++;
                  }
              }
            else
              {
                if(canKingsideCastle('b'))
                  {
                    moves[movesCtr] = new Move(126, 128, _NO_PROMO);
                    movesCtr++;
                  }
                if(canQueensideCastle('b'))
                  {
                    moves[movesCtr] = new Move(126, 124, _NO_PROMO);
                    movesCtr++;
                  }
              }
            return moves;
          }

        return new Move[0];
      }

    public Move[] getKingNonCastle(int index)
      {
        Move[] moves;
        int movesCtr = 0;

        if(!oob(u(index)) && !sameSide(u(index), index))
          movesCtr++;
        if(!oob(ur(index)) && !sameSide(ur(index), index))
          movesCtr++;
        if(!oob(r(index)) && !sameSide(r(index), index))
          movesCtr++;
        if(!oob(dr(index)) && !sameSide(dr(index), index))
          movesCtr++;
        if(!oob(d(index)) && !sameSide(d(index), index))
          movesCtr++;
        if(!oob(dl(index)) && !sameSide(dl(index), index))
          movesCtr++;
        if(!oob(l(index)) && !sameSide(l(index), index))
          movesCtr++;
        if(!oob(ul(index)) && !sameSide(ul(index), index))
          movesCtr++;

        if(movesCtr > 0)                                            //  Any moves at all?
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            if(!oob(u(index)) && !sameSide(u(index), index))
              {
                moves[movesCtr] = new Move(index, u(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ur(index)) && !sameSide(ur(index), index))
              {
                moves[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(r(index)) && !sameSide(r(index), index))
              {
                moves[movesCtr] = new Move(index, r(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dr(index)) && !sameSide(dr(index), index))
              {
                moves[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(d(index)) && !sameSide(d(index), index))
              {
                moves[movesCtr] = new Move(index, d(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dl(index)) && !sameSide(dl(index), index))
              {
                moves[movesCtr] = new Move(index, dl(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(l(index)) && !sameSide(l(index), index))
              {
                moves[movesCtr] = new Move(index, l(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ul(index)) && !sameSide(ul(index), index))
              {
                moves[movesCtr] = new Move(index, ul(index), _NO_PROMO);
                movesCtr++;
              }

            return moves;
          }

        return new Move[0];
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