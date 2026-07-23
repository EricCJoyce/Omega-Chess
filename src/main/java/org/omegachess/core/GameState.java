package org.omegachess.core;

public final class GameState
  {
    public static final int _NONE                 =  144;
    public static final byte _NO_PROMO            = 0x00;
    public static final byte _PROMO_KNIGHT        = 0x01;
    public static final byte _PROMO_CHAMPION      = 0x02;
    public static final byte _PROMO_WIZARD        = 0x03;
    public static final byte _PROMO_BISHOP        = 0x04;
    public static final byte _PROMO_ROOK          = 0x05;
    public static final byte _PROMO_QUEEN         = 0x06;

    public static final byte _WHITE_PAWN          = 0x01;
    public static final byte _WHITE_KNIGHT        = 0x02;
    public static final byte _WHITE_CHAMPION      = 0x03;
    public static final byte _WHITE_WIZARD        = 0x04;
    public static final byte _WHITE_BISHOP        = 0x05;
    public static final byte _WHITE_ROOK          = 0x06;
    public static final byte _WHITE_QUEEN         = 0x07;
    public static final byte _WHITE_KING          = 0x08;

    public static final byte _BLACK_PAWN          = 0x09;
    public static final byte _BLACK_KNIGHT        = 0x0A;
    public static final byte _BLACK_CHAMPION      = 0x0B;
    public static final byte _BLACK_WIZARD        = 0x0C;
    public static final byte _BLACK_BISHOP        = 0x0D;
    public static final byte _BLACK_ROOK          = 0x0E;
    public static final byte _BLACK_QUEEN         = 0x0F;
    public static final byte _BLACK_KING          = 0x10;

    public static final byte _WHITE_TO_MOVE       = 0x00;
    public static final byte _BLACK_TO_MOVE       = 0x01;

    public static final byte GAME_ONGOING         = 0x00;
    public static final byte GAME_OVER_WHITE_WINS = 0x01;
    public static final byte GAME_OVER_BLACK_WINS = 0x02;
    public static final byte GAME_OVER_STALEMATE  = 0x03;

    public static final int _GAMESTATE_BYTE_SIZE  =  107;           //  Number of bytes needed to store a GameState structure.
    public static final int _MOVE_BYTE_SIZE       =    3;           //  Number of bytes needed to store a Move structure.
    public static final int _MAX_NUM_TARGETS      =   64;           //  A (generous) upper bound on how many distinct destinations (not distinct moves)
                                                                    //  may be available to a player from a single index.
    public static final int _MAX_MOVES            =  512;           //  A (generous) upper bound on how many moves are available to a team in a single turn.

    private byte board[];                                           //  144 characters for 144 squares.
    private boolean whiteToMove;                                    //  Whether white is to move.
    private boolean whiteKingsideLiberty;                           //  6 booleans
    private boolean whiteQueensideLiberty;
    private boolean blackKingsideLiberty;
    private boolean blackQueensideLiberty;
    private boolean whiteHasCastled;
    private boolean blackHasCastled;
    private int previousPawnMove;                                   //  Indicate which column:             {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
                                                                    //  If it was a double move, these are { 1,  2,  3,  4,  5,  6,  7,  8,  9, 10};
                                                                    //  If it was a triple move, these are {11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
    private byte moveCtr;                                           //  At 50, call it a draw.

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
      W                     W                                       */

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
      Setters  */

    public void setBoard(byte[] brd)
      {
        int i;
        for(i = 0; i < _NONE; i++)
          board[i] = brd[i];
        return;
      }

    public void setCastlingData(boolean whiteKingside, boolean whiteQueenside,
                                boolean blackKingside, boolean blackQueenside,
                                boolean whiteCastled, boolean blackCastled)
      {
        whiteKingsideLiberty = whiteKingside;
        whiteQueensideLiberty = whiteQueenside;
        blackKingsideLiberty = blackKingside;
        blackQueensideLiberty = blackQueenside;
        whiteHasCastled = whiteCastled;
        blackHasCastled = blackCastled;
        return;
      }

    public void setPreviousPawnMove(int prev)
      {
        previousPawnMove = prev;
        return;
      }

    public void setWhiteToMove(boolean w)
      {
        whiteToMove = w;
        return;
      }

    /*****************************************************************
      Moves  */

    public void makeMove(Move move)
      {
        if(isBlackKingside(move))                                   //  Black Kingside-Castle
          {
            board[126] = _EMPTY;
            board[127] = _BLACK_ROOK;
            board[128] = _BLACK_KING;
            board[129] = _EMPTY;
            blackKingsideLiberty = false;                           //  Black cannot Kingside.
            blackQueensideLiberty = false;                          //  Black cannot Queenside.
            blackHasCastled = true;                                 //  Black has castled.
            previousPawnMove = 0;                                   //  Zero this out.
          }
        else if(isBlackQueenside(move))                             //  Black Queenside-Castle
          {
            board[126] = _EMPTY;
            board[125] = _BLACK_ROOK;
            board[124] = _BLACK_KING;
            board[123] = _EMPTY;
            board[122] = _EMPTY;
            blackKingsideLiberty = false;                           //  Black cannot Kingside.
            blackQueensideLiberty = false;                          //  Black cannot Queenside.
            blackHasCastled = true;                                 //  Black has castled.
            previousPawnMove = 0;                                   //  Zero this out.
          }
        else if(isWhiteKingside(move))                              //  White Kingside-Castle
          {
            board[18] = _EMPTY;
            board[19] = _WHITE_ROOK;
            board[20] = _WHITE_KING;
            board[21] = _EMPTY;
            whiteKingsideLiberty = false;                           //  White cannot Kingside.
            whiteQueensideLiberty = false;                          //  White cannot Queenside.
            whiteHasCastled = true;                                 //  White has castled.
            previousPawnMove = 0;                                   //  Zero this out.
          }
        else if(isWhiteQueenside(move))                             //  White Queenside-Castle
          {
            board[18] = _EMPTY;
            board[17] = _WHITE_ROOK;
            board[16] = _WHITE_KING;
            board[15] = _EMPTY;
            board[14] = _EMPTY;
            whiteKingsideLiberty = false;                           //  White cannot Kingside.
            whiteQueensideLiberty = false;                          //  White cannot Queenside.
            whiteHasCastled = true;                                 //  White has castled.
            previousPawnMove = 0;                                   //  Zero this out.
          }
        else if(isEnPassantAttack(move))                            //  En-passant capture
          {
            board[ enPassantVictim(move) ] = _EMPTY;
            board[move.to] = board[move.from];
            board[move.from] = _EMPTY;

            moveCtr = 0;                                            //  Capture resets the 50-move counter.
            previousPawnMove = 0;                                   //  Zero this out.
          }
        else                                                        //  Any other type of non-castling, non-en-passant move.
          {
            if(isKing(move.from) && isWhite(move.from))             //  White King moved: castling rights lost.
              {
                whiteKingsideLiberty = false;                       //  White cannot Kingside.
                whiteQueensideLiberty = false;                      //  White cannot Queenside.
              }
                                                                    //  White King's Rook moved: Kingside rights lost.
            else if(isRook(move.from) && isWhite(move.from) && move.from == 21)
              whiteKingsideLiberty = false;                         //  White cannot Kingside.
                                                                    //  White Queen's Rook moved: Queenside rights lost.
            else if(isRook(move.from) && isWhite(move.from) && move.from == 14)
              whiteQueensideLiberty = false;                        //  White cannot Queenside.
            else if(isKing(move.from) && isBlack(move.from))        //  Black King moved: castling rights lost.
              {
                blackKingsideLiberty = false;                       //  Black cannot Kingside.
                blackQueensideLiberty = false;                      //  Black cannot Queenside.
              }
                                                                    //  Black King's Rook moved: Kingside rights lost.
            else if(isRook(move.from) && isBlack(move.from) && move.from == 129)
             blackKingsideLiberty = false;                          //  Black cannot Kingside.
                                                                    //  Black Queen's Rook moved: Queenside rights lost.
            else if(isRook(move.from) && isBlack(move.from) && move.from == 122)
              blackQueensideLiberty = false;                        //  Black cannot Queenside.

                                                                    //  Pawn promotion
            if(isPawn(move.from) && move.promo != _NO_PROMO && (row(move.to) == 10 || row(move.to) == 1))
              {
                if(isWhite(move.from))
                  {
                    switch(move.promo)
                      {
                        case _PROMO_KNIGHT:   board[move.to] = _WHITE_KNIGHT;    break;
                        case _PROMO_CHAMPION: board[move.to] = _WHITE_CHAMPION;  break;
                        case _PROMO_WIZARD:   board[move.to] = _WHITE_WIZARD;    break;
                        case _PROMO_BISHOP:   board[move.to] = _WHITE_BISHOP;    break;
                        case _PROMO_ROOK:     board[move.to] = _WHITE_ROOK;      break;
                        case _PROMO_QUEEN:    board[move.to] = _WHITE_QUEEN;     break;
                      }
                  }
                else
                  {
                    switch(move.promo)
                      {
                        case _PROMO_KNIGHT:   board[move.to] = _BLACK_KNIGHT;    break;
                        case _PROMO_CHAMPION: board[move.to] = _BLACK_CHAMPION;  break;
                        case _PROMO_WIZARD:   board[move.to] = _BLACK_WIZARD;    break;
                        case _PROMO_BISHOP:   board[move.to] = _BLACK_BISHOP;    break;
                        case _PROMO_ROOK:     board[move.to] = _BLACK_ROOK;      break;
                        case _PROMO_QUEEN:    board[move.to] = _BLACK_QUEEN;     break;
                      }
                  }
                board[move.from] = _EMPTY;
                previousPawnMove = 0;                               //  Zero this out.
                moveCtr = 0;                                        //  Pawn move resets the 50-move counter.
              }
            else                                                    //  Any other case.
              {
                previousPawnMove = 0;                               //  Zero this out... Unless it gets set below.
                if(isPawnDoubleMove(move.from, move.to))            //  Save last move IFF last move was a pawn double-move!
                  {
                    previousPawnMove = col(move.from);              //   1 --> Double move occurred in Column A.
                                                                    //   2 --> Double move occurred in Column B.
                                                                    //   3 --> Double move occurred in Column C.
                                                                    //   4 --> Double move occurred in Column D.
                                                                    //   5 --> Double move occurred in Column E.
                                                                    //   6 --> Double move occurred in Column F.
                                                                    //   7 --> Double move occurred in Column G.
                                                                    //   8 --> Double move occurred in Column H.
                                                                    //   9 --> Double move occurred in Column I.
                                                                    //  10 --> Double move occurred in Column J.
                  }
                else if(isPawnTripleMove(move.from, move.to))       //  Save last move IFF last move was a pawn triple-move!
                  {
                    previousPawnMove = 10 + col(move.from);         //  11 --> Double move occurred in Column A.
                                                                    //  12 --> Double move occurred in Column B.
                                                                    //  13 --> Double move occurred in Column C.
                                                                    //  14 --> Double move occurred in Column D.
                                                                    //  15 --> Double move occurred in Column E.
                                                                    //  16 --> Double move occurred in Column F.
                                                                    //  17 --> Double move occurred in Column G.
                                                                    //  18 --> Double move occurred in Column H.
                                                                    //  19 --> Double move occurred in Column I.
                                                                    //  20 --> Double move occurred in Column J.
                  }
                if(isPawn(move.from) || !isEmpty(move.to))          //  Pawn move or capture reset the 50-move counter.
                  moveCtr = 0;
                else                                                //  Otherwise, increase the counter.
                  moveCtr++;

                board[move.to] = board[move.from];
                board[move.from] = _EMPTY;
              }
          }

        whiteToMove = !whiteToMove;                                 //  Flip flag.

        return;
      }

    /* Does not apply to real chess, but this is convenient for tree-search. */
    public void makeNullMove()
      {
        previousPawnMove = 0;                                       //  Blank out.
        whiteToMove = !whiteToMove;                                 //  Flip flag.
        return;
      }

    /* Is the given "index" attackable by any members of the indicated team? */
    public boolean inCheckBy(int index, boolean white)
      {
        Move[] attacks = new Move[_MAX_NUM_TARGETS];
        int[] enemytargets = new int[_MAX_MOVES];
        int enemyStrikeCtr = 0;
        int i, j;

        enemyStrikeCtr = 0;
        for(i = 0; i < _NONE; i++)
          {
            if((isWhite(i) && white) || (isBlack(i) && !white))
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

                enemyStrikeCtr += attacks.length;                   //  Increase offset.
              }
          }

        i = 0;
        while(i < enemyStrikeCtr && enemytargets[i] != index)
          i++;

        return (i < enemyStrikeCtr);
      }

    /* This means, "Can I castle RIGHT NOW?" Not, "Do I still have Kingside rights?" */
    public boolean canKingsideCastle(boolean white)
      {
        if(white)
          return ( whiteKingsideLiberty && isRook(21) && isWhite(21) &&
                  !inCheckBy(18, 'b') && !inCheckBy(19, 'b') && !inCheckBy(20, 'b') && isEmpty(19) && isEmpty(20));
        else
          return ( blackKingsideLiberty && isRook(129) && isBlack(129) &&
                  !inCheckBy(126, 'w') && !inCheckBy(127, 'w') && !inCheckBy(128, 'w') && isEmpty(127) && isEmpty(128));
      }

    /* This means, "Can I castle RIGHT NOW?" Not, "Do I still have Queenside rights?" */
    public boolean canQueensideCastle(boolean white)
      {
        if(white)
          return ( whiteQueensideLiberty && isRook(14) && isWhite(14) &&
                  !inCheckBy(18, 'b') && !inCheckBy(17, 'b') && !inCheckBy(16, 'b') && isEmpty(17) && isEmpty(16) && isEmpty(15));
        else
          return ( blackQueensideLiberty && isRook(56) && isBlack(56) &&
                  !inCheckBy(126, 'w') && !inCheckBy(125, 'w') && !inCheckBy(124, 'w') && isEmpty(125) && isEmpty(124) && isEmpty(123));
      }

    /* THIS FUNCTION FILTERS FOR CHECK!! */
    public int getMoves(boolean white, Move[] buffer)
      {
        int movesCtr = 0;
        Move[] potentialmoves = new Move[_MAX_NUM_TARGETS];
        int potentialmovesCtr;
        int index, i;

        for(index = 0; index < _NONE; index++)
          {
            if((white && isWhite(index)) || (!white && isBlack(index)))
              {
                potentialmovesCtr = getMovesIndex(index, potentialmoves);
                for(i = 0; i < potentialmovesCtr; i++)
                  buffer[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                movesCtr += potentialmoves.length;
              }
          }

        return movesCtr;
      }

    /* THIS FUNCTION FILTERS FOR CHECK!! */
    public int getMovesIndex(int index, Move[] buffer)
      {
        Move[] potentialmoves = new Move[_MAX_NUM_TARGETS];
        int potentialmovesCtr = 0;

        GameState gs = new GameState();

        int movesCtr = 0;
        int i, k;

        if(!isEmpty(index))
          {
            if(isPawn(index))
              potentialmovesCtr = getPawnMoves(index, potentialmoves);
            else if(isKnight(index))
              potentialmovesCtr = getKnightMoves(index, potentialmoves);
            else if(isChampion(index))
              potentialmovesCtr = getChampionMoves(index, potentialmoves);
            else if(isWizard(index))
              potentialmovesCtr = getWizardMoves(index, potentialmoves);
            else if(isBishop(index))
              potentialmovesCtr = getBishopMoves(index, potentialmoves);
            else if(isRook(index))
              potentialmovesCtr = getRookMoves(index, potentialmoves);
            else if(isQueen(index))
              potentialmovesCtr = getQueenMoves(index, potentialmoves);
            else
              potentialmovesCtr = getKingMoves(index, potentialmoves);

            if(isWhite(index))                                      //  Piece is white, check for checks on the King by Black
              {
                for(i = 0; i < potentialmovesCtr; i++)              //  For every move, make that move, then test the resultant board
                  {
                    gs.setBoard( board );                           //  Copy the game state
                    gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                        blackKingsideLiberty, blackQueensideLiberty,
                                        whiteHasCastled, blackHasCastled );
                    gs.setPreviousPawnMove( previousPawnMove );
                    gs.setWhiteToMove( whiteToMove );

                    gs.makeMove(potentialmoves[i]);                 //  Apply the candidate move

                    k = gs.getKingIndex(true);                      //  Locate the white king on the new board

                    if(k == _NONE)
                      System.out.println("ERROR: the white king is missing from the board!");

                    if(!gs.inCheckBy(k, false))                     //  If king not in check, then move is allowed.
                      {
                        buffer[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr++;
                      }
                  }
              }
            else                                                    //  Piece is black, check for checks on the King by White
              {
                for(i = 0; i < potentialmoves.length; i++)          //  For every move, make that move, then test the resultant board
                  {
                    gs.setBoard( board );                           //  Copy the game state
                    gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                        blackKingsideLiberty, blackQueensideLiberty,
                                        whiteHasCastled, blackHasCastled );
                    gs.setPreviousPawnMove( previousPawnMove );
                    gs.setWhiteToMove( whiteToMove );

                    gs.makeMove(potentialmoves[i]);                 //  Apply the candidate move

                    k = gs.getKingIndex(false);                     //  Locate the black king on the new board

                    if(k == _NONE)
                      System.out.println("ERROR: the black king is missing from the board!");

                    if(!gs.inCheckBy(k, true))                      //  If king not in check, then move is allowed.
                      {
                        buffer[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr++;
                      }
                  }
              }
          }

        return movesCtr;
      }

    public int getPawnMoves(int index, Move[] buffer)
      {
        int movesCtr = 0;
        Move[] enpassant = new Move[2];
        int enpassantLen;
        int i, len = 0;

        if(isWhite(index))
          {
                                                                    //  White's double Pawn move
            if(row(index) == 2 && !oob(u(u(index))) && isEmpty(u(index)) && isEmpty(u(u(index))))
              {
                buffer[movesCtr] = new Move(index, u(u(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  White's triple Pawn move
            if(row(index) == 2 && !oob(u(u(u(index)))) && isEmpty(u(index)) && isEmpty(u(u(index))) && isEmpty(u(u(u(index)))))
              {
                buffer[movesCtr] = new Move(index, u(u(u(index))), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(u(index)) && isEmpty(u(index)))                 //  White Pawn forward, possibly to promotion
              {
                if(row(u(index)) == 10)
                  {
                    buffer[movesCtr] = new Move(index, u(index), _PROMO_KNIGHT);
                    buffer[movesCtr + 1] = new Move(index, u(index), _PROMO_CHAMPION);
                    buffer[movesCtr + 2] = new Move(index, u(index), _PROMO_WIZARD);
                    buffer[movesCtr + 3] = new Move(index, u(index), _PROMO_BISHOP);
                    buffer[movesCtr + 4] = new Move(index, u(index), _PROMO_ROOK);
                    buffer[movesCtr + 5] = new Move(index, u(index), _PROMO_QUEEN);
                    buffer += 6;
                  }
                else
                  {
                    buffer[movesCtr] = new Move(index, u(index), _NO_PROMO);
                    movesCtr++;
                  }
              }
                                                                    //  White Pawn attack forward-left, possibly to promotion
            if(!oob(ul(index)) && row(ul(index)) == row(index) + 1 && col(ul(index)) == col(index) - 1 && isBlack(ul(index)))
              {
                if(row(u(index)) == 10)
                  {
                    buffer[movesCtr] = new Move(index, ul(index), _PROMO_KNIGHT);
                    buffer[movesCtr + 1] = new Move(index, ul(index), _PROMO_CHAMPION);
                    buffer[movesCtr + 2] = new Move(index, ul(index), _PROMO_WIZARD);
                    buffer[movesCtr + 3] = new Move(index, ul(index), _PROMO_BISHOP);
                    buffer[movesCtr + 4] = new Move(index, ul(index), _PROMO_ROOK);
                    buffer[movesCtr + 5] = new Move(index, ul(index), _PROMO_QUEEN);
                    buffer += 6;
                  }
                else
                  {
                    buffer[movesCtr] = new Move(index, ul(index), _NO_PROMO);
                    movesCtr++;
                  }
              }
                                                                    //  White Pawn attack forward-right, possibly to promotion
            if(!oob(ur(index)) && row(ur(index)) == row(index) + 1 && col(ur(index)) == col(index) + 1 && isBlack(ur(index)))
              {
                if(row(u(index)) == 10)
                  {
                    buffer[movesCtr] = new Move(index, ur(index), _PROMO_KNIGHT);
                    buffer[movesCtr + 1] = new Move(index, ur(index), _PROMO_CHAMPION);
                    buffer[movesCtr + 2] = new Move(index, ur(index), _PROMO_WIZARD);
                    buffer[movesCtr + 3] = new Move(index, ur(index), _PROMO_BISHOP);
                    buffer[movesCtr + 4] = new Move(index, ur(index), _PROMO_ROOK);
                    buffer[movesCtr + 5] = new Move(index, ur(index), _PROMO_QUEEN);
                    buffer += 6;
                  }
                else
                  {
                    buffer[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                    movesCtr++;
                  }
              }
            enpassantLen = getPawnEnPassantAttacks(index, enpassant);
            for(i = 0; i < enpassant.length; i++)
              {
                buffer[movesCtr] = new Move(index, enpassant[i].to, _NO_PROMO);
                movesCtr++;
              }
          }
        else
          {
                                                                    //  Black's double Pawn move
            if(row(index) == 9 && !oob(d(d(index))) && isEmpty(d(index)) && isEmpty(d(d(index))))
              {
                buffer[movesCtr] = new Move(index, d(d(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  Black's triple Pawn move
            if(row(index) == 9 && !oob(d(d(d(index)))) && isEmpty(d(index)) && isEmpty(d(d(index))) && isEmpty(d(d(d(index)))))
              {
                buffer[movesCtr] = new Move(index, d(d(d(index))), _NO_PROMO);
                movesCtr++;
              }

            if(!oob(d(index)) && isEmpty(d(index)))                 //  Black Pawn forward, possibly to promotion
              {
                if(row(d(index)) == 1)
                  {
                    buffer[movesCtr] = new Move(index, d(index), _PROMO_KNIGHT);
                    buffer[movesCtr + 1] = new Move(index, d(index), _PROMO_CHAMPION);
                    buffer[movesCtr + 2] = new Move(index, d(index), _PROMO_WIZARD);
                    buffer[movesCtr + 3] = new Move(index, d(index), _PROMO_BISHOP);
                    buffer[movesCtr + 4] = new Move(index, d(index), _PROMO_ROOK);
                    buffer[movesCtr + 5] = new Move(index, d(index), _PROMO_QUEEN);
                    buffer += 6;
                  }
                else
                  {
                    buffer[movesCtr] = new Move(index, d(index), _NO_PROMO);
                    movesCtr++;
                  }
              }
                                                                    //  Black Pawn attack forward-left, possibly to promotion
            if(!oob(dl(index)) && row(dl(index)) == row(index) - 1 && col(dl(index)) == col(index) - 1 && isWhite(dl(index)))
              {
                if(row(dl(index)) == 1)
                  {
                    buffer[movesCtr] = new Move(index, dl(index), _PROMO_KNIGHT);
                    buffer[movesCtr + 1] = new Move(index, dl(index), _PROMO_CHAMPION);
                    buffer[movesCtr + 2] = new Move(index, dl(index), _PROMO_WIZARD);
                    buffer[movesCtr + 3] = new Move(index, dl(index), _PROMO_BISHOP);
                    buffer[movesCtr + 4] = new Move(index, dl(index), _PROMO_ROOK);
                    buffer[movesCtr + 5] = new Move(index, dl(index), _PROMO_QUEEN);
                    buffer += 6;
                  }
                else
                  {
                    buffer[movesCtr] = new Move(index, dl(index), _NO_PROMO);
                    movesCtr++;
                  }
              }
                                                                    //  Black Pawn attack forward-right, possibly to promotion
            if(!oob(dr(index)) && row(dr(index)) == row(index) - 1 && col(dr(index)) == col(index) + 1 && isWhite(dr(index)))
              {
                if(row(dr(index)) == 1)
                  {
                    buffer[movesCtr] = new Move(index, dr(index), _PROMO_KNIGHT);
                    buffer[movesCtr + 1] = new Move(index, dr(index), _PROMO_CHAMPION);
                    buffer[movesCtr + 2] = new Move(index, dr(index), _PROMO_WIZARD);
                    buffer[movesCtr + 3] = new Move(index, dr(index), _PROMO_BISHOP);
                    buffer[movesCtr + 4] = new Move(index, dr(index), _PROMO_ROOK);
                    buffer[movesCtr + 5] = new Move(index, dr(index), _PROMO_QUEEN);
                    buffer += 6;
                  }
                else
                  {
                    buffer[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                    movesCtr++;
                  }
              }
            enpassantLen = getPawnEnPassantAttacks(index, enpassant);
            for(i = 0; i < enpassant.length; i++)
              {
                buffer[movesCtr] = new Move(index, enpassant[i].to, _NO_PROMO);
                movesCtr++;
              }
          }

        return movesCtr;
      }

    public int getPawnEnPassantAttacks(int index, Move[] buffer)
      {
        int len = 0;

        if(previousPawnMove > 0 && isPawn(index))
          {
            switch(previousPawnMove)
              {
                //////////////////////////////////////////////////////  DOUBLE moves
                case 1:                                             //  Previous pawn double-move occurred in column A.
                  if(col(index) == 2)                               //  "index" is in the column next to column A, where the double move occurred.
                    {
                      if(isWhite(index) && row(index) == 7 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(isBlack(index) && row(index) == 4 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 2:                                             //  Previous pawn double-move occurred in column B.
                  if(col(index) == 1 || col(index) == 3)            //  "index" is in the column next to column B, where the double move occurred.
                    {
                      if(col(index) == 1 && isWhite(index) && row(index) == 7 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isWhite(index) && row(index) == 7 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 1 && isBlack(index) && row(index) == 4 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isBlack(index) && row(index) == 4 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 3:                                             //  Previous pawn double-move occurred in column C.
                  if(col(index) == 2 || col(index) == 4)            //  "index" is in the column next to column C, where the double move occurred.
                    {
                      if(col(index) == 2 && isWhite(index) && row(index) == 7 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isWhite(index) && row(index) == 7 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 2 && isBlack(index) && row(index) == 4 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isBlack(index) && row(index) == 4 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 4:                                             //  Previous pawn double-move occurred in column D.
                  if(col(index) == 3 || col(index) == 5)            //  "index" is in the column next to column D, where the double move occurred.
                    {
                      if(col(index) == 3 && isWhite(index) && row(index) == 7 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isWhite(index) && row(index) == 7 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isBlack(index) && row(index) == 4 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isBlack(index) && row(index) == 4 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 5:                                             //  Previous pawn double-move occurred in column E.
                  if(col(index) == 4 || col(index) == 6)            //  "index" is in the column next to column E, where the double move occurred.
                    {
                      if(col(index) == 4 && isWhite(index) && row(index) == 7 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isWhite(index) && row(index) == 7 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isBlack(index) && row(index) == 4 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isBlack(index) && row(index) == 4 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 6:                                             //  Previous pawn double-move occurred in column F.
                  if(col(index) == 5 || col(index) == 7)            //  "index" is in the column next to column F, where the double move occurred.
                    {
                      if(col(index) == 5 && isWhite(index) && row(index) == 7 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isWhite(index) && row(index) == 7 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isBlack(index) && row(index) == 4 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isBlack(index) && row(index) == 4 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 7:                                             //  Previous pawn double-move occurred in column G.
                  if(col(index) == 6 || col(index) == 8)            //  "index" is in the column next to column G, where the double move occurred.
                    {
                      if(col(index) == 6 && isWhite(index) && row(index) == 7 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isWhite(index) && row(index) == 7 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isBlack(index) && row(index) == 4 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isBlack(index) && row(index) == 4 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 8:                                             //  Previous pawn double-move occurred in column H.
                  if(col(index) == 7 || col(index) == 9)            //  "index" is in the column next to column H, where the double move occurred.
                    {
                      if(col(index) == 7 && isWhite(index) && row(index) == 7 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 9 && isWhite(index) && row(index) == 7 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isBlack(index) && row(index) == 4 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 9 && isBlack(index) && row(index) == 4 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 9:                                             //  Previous pawn double-move occurred in column I.
                  if(col(index) == 8 || col(index) == 10)           //  "index" is in the column next to column I, where the double move occurred.
                    {
                      if(col(index) == 8 && isWhite(index) && row(index) == 7 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 10 && isWhite(index) && row(index) == 7 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isBlack(index) && row(index) == 4 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 10 && isBlack(index) && row(index) == 4 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 10:                                            //  Previous pawn double-move occurred in column J.
                  if(col(index) == 9)                               //  "index" is in the column next to column J, where the double move occurred.
                    {
                      if(isWhite(index) && row(index) == 7 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(isBlack(index) && row(index) == 4 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;

                //////////////////////////////////////////////////////  TRIPLE moves
                case 11:                                            //  Previous pawn triple-move occurred in column A.
                  if(col(index) == 2)                               //  "index" is in the column next to column A, where the triple move occurred.
                    {
                      if(isWhite(index) && row(index) == 7 && isBlack(dl(index)) && isPawn(dl(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(isWhite(index) && row(index) == 6 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(isBlack(index) && row(index) == 4 && isWhite(ul(index)) && isPawn(ul(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(isBlack(index) && row(index) == 5 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 12:                                            //  Previous pawn triple-move occurred in column B.
                  if(col(index) == 1 || col(index) == 3)            //  "index" is in the column next to column B, where the double move occurred.
                    {
                      if(col(index) == 1 && isWhite(index) && row(index) == 7 && isBlack(dr(index)) && isPawn(dr(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isWhite(index) && row(index) == 7 && isBlack(dl(index)) && isPawn(dl(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 1 && isWhite(index) && row(index) == 6 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isWhite(index) && row(index) == 6 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 1 && isBlack(index) && row(index) == 4 && isWhite(ur(index)) && isPawn(ur(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isBlack(index) && row(index) == 4 && isWhite(ul(index)) && isPawn(ul(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 1 && isBlack(index) && row(index) == 5 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isBlack(index) && row(index) == 5 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 13:                                            //  Previous pawn triple-move occurred in column C.
                  if(col(index) == 2 || col(index) == 4)            //  "index" is in the column next to column C, where the double move occurred.
                    {
                      if(col(index) == 2 && isWhite(index) && row(index) == 7 && isBlack(dr(index)) && isPawn(dr(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isWhite(index) && row(index) == 7 && isBlack(dl(index)) && isPawn(dl(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 2 && isWhite(index) && row(index) == 6 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isWhite(index) && row(index) == 6 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 2 && isBlack(index) && row(index) == 4 && isWhite(ur(index)) && isPawn(ur(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isBlack(index) && row(index) == 4 && isWhite(ul(index)) && isPawn(ul(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 2 && isBlack(index) && row(index) == 5 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isBlack(index) && row(index) == 5 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 14:                                            //  Previous pawn triple-move occurred in column D.
                  if(col(index) == 3 || col(index) == 5)            //  "index" is in the column next to column D, where the double move occurred.
                    {
                      if(col(index) == 3 && isWhite(index) && row(index) == 7 && isBlack(dr(index)) && isPawn(dr(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isWhite(index) && row(index) == 7 && isBlack(dl(index)) && isPawn(dl(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isWhite(index) && row(index) == 6 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isWhite(index) && row(index) == 6 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isBlack(index) && row(index) == 4 && isWhite(ur(index)) && isPawn(ur(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isBlack(index) && row(index) == 4 && isWhite(ul(index)) && isPawn(ul(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 3 && isBlack(index) && row(index) == 5 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isBlack(index) && row(index) == 5 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 15:                                            //  Previous pawn triple-move occurred in column E.
                  if(col(index) == 4 || col(index) == 6)            //  "index" is in the column next to column E, where the double move occurred.
                    {
                      if(col(index) == 4 && isWhite(index) && row(index) == 7 && isBlack(dr(index)) && isPawn(dr(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isWhite(index) && row(index) == 7 && isBlack(dl(index)) && isPawn(dl(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isWhite(index) && row(index) == 6 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isWhite(index) && row(index) == 6 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isBlack(index) && row(index) == 4 && isWhite(ur(index)) && isPawn(ur(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isBlack(index) && row(index) == 4 && isWhite(ul(index)) && isPawn(ul(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 4 && isBlack(index) && row(index) == 5 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isBlack(index) && row(index) == 5 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 16:                                            //  Previous pawn triple-move occurred in column F.
                  if(col(index) == 5 || col(index) == 7)            //  "index" is in the column next to column F, where the double move occurred.
                    {
                      if(col(index) == 5 && isWhite(index) && row(index) == 7 && isBlack(dr(index)) && isPawn(dr(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isWhite(index) && row(index) == 7 && isBlack(dl(index)) && isPawn(dl(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isWhite(index) && row(index) == 6 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isWhite(index) && row(index) == 6 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isBlack(index) && row(index) == 4 && isWhite(ur(index)) && isPawn(ur(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isBlack(index) && row(index) == 4 && isWhite(ul(index)) && isPawn(ul(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 5 && isBlack(index) && row(index) == 5 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isBlack(index) && row(index) == 5 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 17:                                            //  Previous pawn triple-move occurred in column G.
                  if(col(index) == 6 || col(index) == 8)            //  "index" is in the column next to column G, where the double move occurred.
                    {
                      if(col(index) == 6 && isWhite(index) && row(index) == 7 && isBlack(dr(index)) && isPawn(dr(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isWhite(index) && row(index) == 7 && isBlack(dl(index)) && isPawn(dl(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isWhite(index) && row(index) == 6 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isWhite(index) && row(index) == 6 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isBlack(index) && row(index) == 4 && isWhite(ur(index)) && isPawn(ur(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isBlack(index) && row(index) == 4 && isWhite(ul(index)) && isPawn(ul(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 6 && isBlack(index) && row(index) == 5 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isBlack(index) && row(index) == 5 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 18:                                            //  Previous pawn triple-move occurred in column H.
                  if(col(index) == 7 || col(index) == 9)            //  "index" is in the column next to column H, where the double move occurred.
                    {
                      if(col(index) == 7 && isWhite(index) && row(index) == 7 && isBlack(dr(index)) && isPawn(dr(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 9 && isWhite(index) && row(index) == 7 && isBlack(dl(index)) && isPawn(dl(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isWhite(index) && row(index) == 6 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 9 && isWhite(index) && row(index) == 6 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isBlack(index) && row(index) == 4 && isWhite(ur(index)) && isPawn(ur(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 9 && isBlack(index) && row(index) == 4 && isWhite(ul(index)) && isPawn(ul(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 7 && isBlack(index) && row(index) == 5 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 9 && isBlack(index) && row(index) == 5 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 19:                                            //  Previous pawn triple-move occurred in column I.
                  if(col(index) == 8 || col(index) == 10)           //  "index" is in the column next to column I, where the double move occurred.
                    {
                      if(col(index) == 8 && isWhite(index) && row(index) == 7 && isBlack(dr(index)) && isPawn(dr(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 10 && isWhite(index) && row(index) == 7 && isBlack(dl(index)) && isPawn(dl(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isWhite(index) && row(index) == 6 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 10 && isWhite(index) && row(index) == 6 && isBlack(l(index)) && isPawn(l(index)) && isEmpty(ul(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ul(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isBlack(index) && row(index) == 4 && isWhite(ur(index)) && isPawn(ur(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 10 && isBlack(index) && row(index) == 4 && isWhite(ul(index)) && isPawn(ul(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 8 && isBlack(index) && row(index) == 5 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(col(index) == 10 && isBlack(index) && row(index) == 5 && isWhite(l(index)) && isPawn(l(index)) && isEmpty(dl(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dl(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
                case 20:                                            //  Previous pawn triple-move occurred in column J.
                  if(col(index) == 9)                               //  "index" is in the column next to column J, where the double move occurred.
                    {
                      if(isWhite(index) && row(index) == 7 && isBlack(dr(index)) && isPawn(dr(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(isWhite(index) && row(index) == 6 && isBlack(r(index)) && isPawn(r(index)) && isEmpty(ur(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = ur(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(isBlack(index) && row(index) == 4 && isWhite(ur(index)) && isPawn(ur(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                      else if(isBlack(index) && row(index) == 5 && isWhite(r(index)) && isPawn(r(index)) && isEmpty(dr(index)))
                        {
                          buffer[movesCtr].from = index;
                          buffer[movesCtr].to = dr(index);
                          buffer[movesCtr].promo = _NO_PROMO;
                          movesCtr++;
                        }
                    }
                  break;
              }
          }

        return len;
      }

    public int getKnightMoves(int index, Move[] buffer)
      {
        int movesCtr = 0;

        if(!oob(ul(u(index))) && !sameSide(ul(u(index)), index))
          {
            buffer[movesCtr] = new Move(index, ul(u(index)), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(ur(u(index))) && !sameSide(ur(u(index)), index))
          {
            buffer[movesCtr] = new Move(index, ur(u(index)), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(ur(r(index))) && !sameSide(ur(r(index)), index))
          {
            buffer[movesCtr] = new Move(index, ur(r(index)), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(ul(l(index))) && !sameSide(ul(l(index)), index))
          {
            buffer[movesCtr] = new Move(index, ul(l(index)), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(dl(d(index))) && !sameSide(dl(d(index)), index))
          {
            buffer[movesCtr] = new Move(index, dl(d(index)), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dr(d(index))) && !sameSide(dr(d(index)), index))
          {
            buffer[movesCtr] = new Move(index, dr(d(index)), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(dr(r(index))) && !sameSide(dr(r(index)), index))
          {
            buffer[movesCtr] = new Move(index, dr(r(index)), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dl(l(index))) && !sameSide(dl(l(index)), index))
          {
            buffer[movesCtr] = new Move(index, dl(l(index)), _NO_PROMO);
            movesCtr++;
          }

        return movesCtr;
      }

    public int getChampionMoves(int index, Move[] buffer)
      {
        int movesCtr = 0;

        if(!oob(u(index)) && !sameSide(u(index), index))
          {
            buffer[movesCtr] = new Move(index, u(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(u(u(index))) && !sameSide(u(u(index)), index))
          {
            buffer[movesCtr] = new Move(index, u(u(index)), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(d(index)) && !sameSide(d(index), index))
          {
            buffer[movesCtr] = new Move(index, d(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(d(d(index))) && !sameSide(d(d(index)), index))
          {
            buffer[movesCtr] = new Move(index, d(d(index)), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(l(index)) && !sameSide(l(index), index))
          {
            buffer[movesCtr] = new Move(index, l(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(l(l(index))) && !sameSide(l(l(index)), index))
          {
            buffer[movesCtr] = new Move(index, l(l(index)), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(r(index)) && !sameSide(r(index), index))
          {
            buffer[movesCtr] = new Move(index, r(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(r(r(index))) && !sameSide(r(r(index)), index))
          {
            buffer[movesCtr] = new Move(index, r(r(index)), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(ul(ul(index))) && !sameSide(ul(ul(index)), index))
          {
            buffer[movesCtr] = new Move(index, ul(ul(index)), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(ur(ur(index))) && !sameSide(ur(ur(index)), index))
          {
            buffer[movesCtr] = new Move(index, ur(ur(index)), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dr(dr(index))) && !sameSide(dr(dr(index)), index))
          {
            buffer[movesCtr] = new Move(index, dr(dr(index)), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dl(dl(index))) && !sameSide(dl(dl(index)), index))
          {
            buffer[movesCtr] = new Move(index, dl(dl(index)), _NO_PROMO);
            movesCtr++;
          }

        return movesCtr;
      }

    public int getWizardMoves(int index, Move[] buffer)
      {
        int movesCtr = 0;

        if(!oob(ul(index)) && !sameSide(ul(index), index))
          {
            buffer[movesCtr] = new Move(index, ul(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(ur(index)) && !sameSide(ur(index), index))
          {
            buffer[movesCtr] = new Move(index, ur(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dr(index)) && !sameSide(dr(index), index))
          {
            buffer[movesCtr] = new Move(index, dr(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dl(index)) && !sameSide(dl(index), index))
          {
            buffer[movesCtr] = new Move(index, dl(index), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(u(u(ul(index)))) && !sameSide(u(u(ul(index))), index))
          {
            buffer[movesCtr] = new Move(index, u(u(ul(index))), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(u(u(ur(index)))) && !sameSide(u(u(ur(index))), index))
          {
            buffer[movesCtr] = new Move(index, u(u(ur(index))), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(l(l(ul(index)))) && !sameSide(l(l(ul(index))), index))
          {
            buffer[movesCtr] = new Move(index, l(l(ul(index))), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(r(r(ur(index)))) && !sameSide(r(r(ur(index))), index))
          {
            buffer[movesCtr] = new Move(index, r(r(ur(index))), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(d(d(dl(index)))) && !sameSide(d(d(dl(index))), index))
          {
            buffer[movesCtr] = new Move(index, d(d(dl(index))), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(d(d(dr(index)))) && !sameSide(d(d(dr(index))), index))
          {
            buffer[movesCtr] = new Move(index, d(d(dr(index))), _NO_PROMO);
            movesCtr++;
          }

        if(!oob(l(l(dl(index)))) && !sameSide(l(l(dl(index))), index))
          {
            buffer[movesCtr] = new Move(index, l(l(dl(index))), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(r(r(dr(index)))) && !sameSide(r(r(dr(index))), index))
          {
            buffer[movesCtr] = new Move(index, r(r(dr(index))), _NO_PROMO);
            movesCtr++;
          }

        return movesCtr;
      }

    public int getBishopMoves(int index, Move[] buffer)
      {
        int movesCtr = 0;
        int[] tmp = new int[12];
        int len, i;
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

        len = ulSet(index, flags, tmp);                             //  Up-left
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = urSet(index, flags, tmp);                             //  Up-right
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = dlSet(index, flags, tmp);                             //  Down-left
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = drSet(index, flags, tmp);                             //  Down-right
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        return movesCtr;
      }

    public int getRookMoves(int index, Move[] buffer)
      {
        int movesCtr = 0;
        int[] tmp = new int[10];
        int len, i;
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

        len = uSet(index, flags, tmp);                              //  Up
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = dSet(index, flags, tmp);                              //  Down
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = lSet(index, flags, tmp);                              //  Left
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = rSet(index, flags, tmp);                              //  Right
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        return movesCtr;
      }

    public int getQueenMoves(int index, Move[] buffer)
      {
        int movesCtr = 0;
        int[] tmp = new int[12];
        int len, i;
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

        len = uSet(index, flags, tmp);                              //  Up
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = urSet(index, flags, tmp);                             //  Up-right
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = rSet(index, flags, tmp);                              //  Right
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = drSet(index, flags, tmp);                             //  Down-right
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = dSet(index, flags, tmp);                              //  Down
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = dlSet(index, flags, tmp);                             //  Down-left
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = lSet(index, flags, tmp);                              //  Left
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        len = ulSet(index, flags, tmp);                             //  Up-left
        for(i = 0; i < len; i++)
          {
            buffer[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
            movesCtr++;
          }

        return movesCtr;
      }

    public int getKingMoves(int index, Move[] buffer)
      {
        int movesCtr = 0;

        if(!oob(u(index)) && !sameSide(u(index), index))
          {
            buffer[movesCtr] = new Move(index, u(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(ur(index)) && !sameSide(ur(index), index))
          {
            buffer[movesCtr] = new Move(index, ur(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(r(index)) && !sameSide(r(index), index))
          {
            buffer[movesCtr] = new Move(index, r(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dr(index)) && !sameSide(dr(index), index))
          {
            buffer[movesCtr] = new Move(index, dr(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(d(index)) && !sameSide(d(index), index))
          {
            buffer[movesCtr] = new Move(index, d(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dl(index)) && !sameSide(dl(index), index))
          {
            buffer[movesCtr] = new Move(index, dl(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(l(index)) && !sameSide(l(index), index))
          {
            buffer[movesCtr] = new Move(index, l(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(ul(index)) && !sameSide(ul(index), index))
          {
            buffer[movesCtr] = new Move(index, ul(index), _NO_PROMO);
            movesCtr++;
          }
        if(isWhite(index))
          {
            if(canKingsideCastle(true))
              {
                buffer[movesCtr] = new Move(18, 20, _NO_PROMO);
                movesCtr++;
              }
            if(canQueensideCastle(true))
              {
                buffer[movesCtr] = new Move(18, 16, _NO_PROMO);
                movesCtr++;
              }
          }
        else
          {
            if(canKingsideCastle(false))
              {
                buffer[movesCtr] = new Move(126, 128, _NO_PROMO);
                movesCtr++;
              }
            if(canQueensideCastle(false))
              {
                buffer[movesCtr] = new Move(126, 124, _NO_PROMO);
                movesCtr++;
              }
          }

        return movesCtr;
      }

    public int getKingNonCastle(int index, Move[] buffer)
      {
        int movesCtr = 0;

        if(!oob(u(index)) && !sameSide(u(index), index))
          {
            buffer[movesCtr] = new Move(index, u(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(ur(index)) && !sameSide(ur(index), index))
          {
            buffer[movesCtr] = new Move(index, ur(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(r(index)) && !sameSide(r(index), index))
          {
            buffer[movesCtr] = new Move(index, r(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dr(index)) && !sameSide(dr(index), index))
          {
            buffer[movesCtr] = new Move(index, dr(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(d(index)) && !sameSide(d(index), index))
          {
            buffer[movesCtr] = new Move(index, d(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(dl(index)) && !sameSide(dl(index), index))
          {
            buffer[movesCtr] = new Move(index, dl(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(l(index)) && !sameSide(l(index), index))
          {
            buffer[movesCtr] = new Move(index, l(index), _NO_PROMO);
            movesCtr++;
          }
        if(!oob(ul(index)) && !sameSide(ul(index), index))
          {
            buffer[movesCtr] = new Move(index, ul(index), _NO_PROMO);
            movesCtr++;
          }

        return movesCtr;
      }

    /*****************************************************************
      Terminal testing  */

    public int isWin()
      {
        int i;
        int kpos = 0;
        int wMatNonK = 0, bMatNonK = 0;                             //  Counts of pieces other than Kings.
        Move[] moves = new Move[_MAX_MOVES];
        int moveLen;

        moveLen = getMoves(whiteToMove, moves);                     //  Get moves for side to move.

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

        if(moveLen == 0)                                            //  Game is over if side to move cannot move.
          {
            if(whiteToMove)
              {
                while(kpos < _NONE && board[kpos] != _WHITE_KING)
                  kpos++;
              }
            else
              {
                while(kpos < _NONE && board[kpos] != _BLACK_KING)
                  kpos++;
              }

            if(kpos == _NONE)                                       //  Kings should never be off the board!
              System.out.println("ERROR: King not found!");

            if(whiteToMove)
              {
                if(inCheckBy(kpos, false))
                  return GAME_OVER_BLACK_WINS;
                return GAME_OVER_STALEMATE;
              }
            else
              {
                if(inCheckBy(kpos, true))
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

    /*  Is the given index i occupied by a White piece? */
    public boolean isWhite(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] >= _WHITE_PAWN && board[i] <= _WHITE_KING);
        return false;
      }

    /*  Is the given index i occupied by a Black piece? */
    public boolean isBlack(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] >= _BLACK_PAWN && board[i] <= _BLACK_KING);
        return false;
      }

    /*  Is index i the same as index j
        in terms of both being White or both being Black or both being Empty? */
    public boolean sameSide(int i, int j)
      {
        return ((isWhite(i) && isWhite(j)) || (isBlack(i) && isBlack(j)));
      }

    /*  More specific than same(), this function asks, "Are i and j on opposite teams?" */
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

    public int getKingIndex(boolean white)
      {
        int i;

        while(i < _NONE)
          {
            if((white && board[i] == _WHITE_KING) || (!white && board[i] == _BLACK_KING))
              break;
            i++;
          }

        return i;
      }

    /*****************************************************************
      Set-builders  */

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int uSet(int index, boolean[] flags, int[] indices)
      {
        int len = 0;
        int dst = u(index);

        while(!oob(dst))
          {
            if(isEmpty(dst))
              {
                indices[len] = dst;
                len++;
                dst = u(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    indices[len] = dst;
                    len++;
                    dst = u(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    indices[len] = dst;
                    len++;
                    dst = u(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
          }

        return len;
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int dSet(int index, boolean[] flags, int[] indices)
      {
        int len = 0;
        int dst = d(index);

        while(!oob(dst))
          {
            if(isEmpty(dst))
              {
                indices[len] = dst;
                len++;
                dst = d(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    indices[len] = dst;
                    len++;
                    dst = d(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    indices[len] = dst;
                    len++;
                    dst = d(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
          }

        return len;
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int lSet(int index, boolean[] flags, int[] indices)
      {
        int len = 0;
        int dst = l(index);

        while(!oob(dst))
          {
            if(isEmpty(dst))
              {
                indices[len] = dst;
                len++;
                dst = l(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    indices[len] = dst;
                    len++;
                    dst = l(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    indices[len] = dst;
                    len++;
                    dst = l(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
          }

        return len;
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int rSet(int index, boolean[] flags, int[] indices)
      {
        int len = 0;
        int dst = r(index);

        while(!oob(dst))
          {
            if(isEmpty(dst))
              {
                indices[len] = dst;
                len++;
                dst = r(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    indices[len] = dst;
                    len++;
                    dst = r(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    indices[len] = dst;
                    len++;
                    dst = r(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
          }

        return len;
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int urSet(int index, boolean[] flags, int[] indices)
      {
        int len = 0;
        int dst = ur(index);

        while(!oob(dst))
          {
            if(isEmpty(dst))
              {
                indices[len] = dst;
                len++;
                dst = ur(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    indices[len] = dst;
                    len++;
                    dst = ur(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    indices[len] = dst;
                    len++;
                    dst = ur(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
          }

        return len;
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int drSet(int index, boolean[] flags, int[] indices)
      {
        int len = 0;
        int dst = dr(index);

        while(!oob(dst))
          {
            if(isEmpty(dst))
              {
                indices[len] = dst;
                len++;
                dst = dr(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    indices[len] = dst;
                    len++;
                    dst = dr(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    indices[len] = dst;
                    len++;
                    dst = dr(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
          }

        return len;
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int dlSet(int index, boolean[] flags, int[] indices)
      {
        int len = 0;
        int dst = dl(index);

        while(!oob(dst))
          {
            if(isEmpty(dst))
              {
                indices[len] = dst;
                len++;
                dst = dl(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    indices[len] = dst;
                    len++;
                    dst = dl(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    indices[len] = dst;
                    len++;
                    dst = dl(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
          }

        return len;
      }

    /* flags[0, 1, 2, 3] = [pass through white, stop and include white, pass through black, stop and include black] */
    public int ulSet(int index, boolean[] flags, int[] indices)
      {
        int len = 0;
        int dst = ul(index);

        while(!oob(dst))
          {
            if(isEmpty(dst))
              {
                indices[len] = dst;
                len++;
                dst = ul(dst);
              }
            else if(isWhite(dst))
              {
                if(flags[0])                                        //  Pass through White
                  {
                    indices[len] = dst;
                    len++;
                    dst = ul(dst);
                  }
                else if(flags[1])                                   //  Stop and include White
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
            else if(isBlack(dst))
              {
                if(flags[2])                                        //  Pass through Black
                  {
                    indices[len] = dst;
                    len++;
                    dst = ul(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    break;
                  }
                else
                  break;
              }
          }

        return len;
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
  }