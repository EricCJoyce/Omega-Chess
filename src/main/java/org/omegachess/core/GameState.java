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

    public static final byte _EMPTY               = 0x00;
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

    private final byte[] board = new byte[_NONE];                   //  144 characters for 144 squares.
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
      Copy  */

    public void copyFrom(GameState src)
      {
        System.arraycopy(src.board, 0, board, 0, _NONE);
        whiteToMove = src.whiteToMove;
        whiteKingsideLiberty = src.whiteKingsideLiberty;
        whiteQueensideLiberty = src.whiteQueensideLiberty;
        blackKingsideLiberty = src.blackKingsideLiberty;
        blackQueensideLiberty = src.blackQueensideLiberty;
        whiteHasCastled = src.whiteHasCastled;
        blackHasCastled = src.blackHasCastled;
        previousPawnMove = src.previousPawnMove;
        moveCtr = src.moveCtr;

        return;
      }

    /*****************************************************************
      Moves  */

    public void makeMove(Move move)
      {
        int enPassantVic = enPassantVictim(move);

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
        else if(enPassantVic != _NONE)                              //  En-passant capture
          {
            board[ enPassantVic ] = _EMPTY;
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
                                                                    //  Capture of a rook entails loss of castling rights.
                if(isWhite(move.to) && isRook(move.to) && move.to == 21)
                  whiteKingsideLiberty = false;
                else if(isWhite(move.to) && isRook(move.to) && move.to == 14)
                  whiteQueensideLiberty = false;
                else if(isBlack(move.to) && isRook(move.to) && move.to == 129)
                  blackKingsideLiberty = false;
                else if(isBlack(move.to) && isRook(move.to) && move.to == 122)
                  blackQueensideLiberty = false;

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
                                                                    //  Capture of a rook entails loss of castling rights.
                if(isWhite(move.to) && isRook(move.to) && move.to == 21)
                  whiteKingsideLiberty = false;
                else if(isWhite(move.to) && isRook(move.to) && move.to == 14)
                  whiteQueensideLiberty = false;
                else if(isBlack(move.to) && isRook(move.to) && move.to == 129)
                  blackKingsideLiberty = false;
                else if(isBlack(move.to) && isRook(move.to) && move.to == 122)
                  blackQueensideLiberty = false;

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
        int attacksLen = 0;
        int[] enemytargets = new int[_MAX_MOVES];
        int enemyStrikeCtr = 0;
        int i, j;

        enemyStrikeCtr = 0;
        for(i = 0; i < _NONE; i++)
          {
            if((isWhite(i) && white) || (isBlack(i) && !white))
              {
                if(isPawn(i))
                  attacksLen = getPawnAttacks(i, attacks);
                else if(isKnight(i))
                  attacksLen = getKnightMoves(i, attacks);
                else if(isChampion(i))
                  attacksLen = getChampionMoves(i, attacks);
                else if(isWizard(i))
                  attacksLen = getWizardMoves(i, attacks);
                else if(isBishop(i))
                  attacksLen = getBishopMoves(i, attacks);
                else if(isRook(i))
                  attacksLen = getRookMoves(i, attacks);
                else if(isQueen(i))
                  attacksLen = getQueenMoves(i, attacks);
                else
                  attacksLen = getKingNonCastle(i, attacks);

                for(j = 0; j < attacksLen; j++)
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
                  !inCheckBy(18, false) && !inCheckBy(19, false) && !inCheckBy(20, false) && isEmpty(19) && isEmpty(20));
        else
          return ( blackKingsideLiberty && isRook(129) && isBlack(129) &&
                  !inCheckBy(126, true) && !inCheckBy(127, true) && !inCheckBy(128, true) && isEmpty(127) && isEmpty(128));
      }

    /* This means, "Can I castle RIGHT NOW?" Not, "Do I still have Queenside rights?" */
    public boolean canQueensideCastle(boolean white)
      {
        if(white)
          return ( whiteQueensideLiberty && isRook(14) && isWhite(14) &&
                  !inCheckBy(18, false) && !inCheckBy(17, false) && !inCheckBy(16, false) && isEmpty(17) && isEmpty(16) && isEmpty(15));
        else
          return ( blackQueensideLiberty && isRook(122) && isBlack(122) &&
                  !inCheckBy(126, true) && !inCheckBy(125, true) && !inCheckBy(124, true) && isEmpty(125) && isEmpty(124) && isEmpty(123));
      }

    /* Does the given move describe a kingside castle by white on the current board? */
    public boolean isWhiteKingside(Move move)
      {
        return (isWhite(move.from) && isKing(move.from) && move.from == 18 && move.to == 20);
      }

    /* Does the given move describe a queenside castle by white on the current board? */
    public boolean isWhiteQueenside(Move move)
      {
        return (isWhite(move.from) && isKing(move.from) && move.from == 18 && move.to == 16);
      }

    /* Does the given move describe a kingside castle by black on the current board? */
    public boolean isBlackKingside(Move move)
      {
        return (isBlack(move.from) && isKing(move.from) && move.from == 126 && move.to == 128);
      }

    /* Does the given move describe a queenside castle by black on the current board? */
    public boolean isBlackQueenside(Move move)
      {
        return (isBlack(move.from) && isKing(move.from) && move.from == 126 && move.to == 124);
      }

    /* Return the column in [1, 10] or _NONE in which the previous pawn-special move occurred. */
    private int previousPawnFile()
      {
        if(previousPawnMove < 1 || previousPawnMove > 20)
          return _NONE;

        return ((previousPawnMove - 1) % 10) + 1;
      }

    /* Given the flag "previousPawnMove", did the previous pawn move advance 2 or 3 rows? */
    private int previousPawnAdvance()
      {
        if(previousPawnMove >= 1 && previousPawnMove <= 10)
          return 2;

        if(previousPawnMove >= 11 && previousPawnMove <= 20)
          return 3;

        return 0;
      }

    /* Translate the Move-based query to an index-based query. */
    public int enPassantVictim(Move move)
      {
        if(move == null || move.promo != _NO_PROMO)
          return _NONE;

        return enPassantVictim(move.from, move.to);
      }

    private int enPassantVictim(int from, int to)
      {
        int advance = previousPawnAdvance();
        boolean capturingWhite, destinationWasPassed;
        int forward, previousPawnStartRow, destinationRow, victimFile, victimRow, victim;
        byte expectedVictim;

        if(advance == 0)
          return _NONE;

        if(oob(from) || oob(to))
          return _NONE;

        if(!isPawn(from))
          return _NONE;

        if(!isEmpty(to))                                            //  En-passant capture always lands on an empty square.
          return _NONE;

        capturingWhite = isWhite(from);

        if(capturingWhite != whiteToMove)                           //  Only the actual side to move may exercise the current en-passant capture privilege.
          return _NONE;

        forward = capturingWhite ? 1 : -1;

        if(row(to) != row(from) + forward)                          //  The capturing pawn must move one row forward and one file sideways,
          return _NONE;                                             //  exactly like an ordinary pawn capture.

        if(Math.abs(col(to) - col(from)) != 1)
          return _NONE;

        victimFile = previousPawnFile();

        if(col(to) != victimFile)                                   //  The capturing pawn must land on the file of the pawn that made the previous
          return _NONE;                                             //  double or triple move.
                                                                    //  The capturing side is known, so the previous mover was the opposing side.
        previousPawnStartRow = capturingWhite ? 9 : 2;              //  White pawns start on row 2; black pawns start on row 9.

        if(capturingWhite)
          victimRow = previousPawnStartRow - advance;
        else
          victimRow = previousPawnStartRow + advance;

        destinationRow = row(to);
                                                                    //  The destination must be one of the squares passed through by the previous pawn.
                                                                    //  White moves upward: startRow < passedRow < victimRow
                                                                    //  Black moves downward: startRow > passedRow > victimRow
        if(capturingWhite)
          destinationWasPassed = destinationRow < previousPawnStartRow && destinationRow > victimRow;
        else
          destinationWasPassed = destinationRow > previousPawnStartRow && destinationRow < victimRow;

        if(!destinationWasPassed)
          return _NONE;

        victim = victimRow * 12 + victimFile;
                                                                    //  Verify that the expected enemy pawn is actually sitting
                                                                    //  on the computed destination of the previous move.
        expectedVictim = capturingWhite ? _BLACK_PAWN : _WHITE_PAWN;
        if(board[victim] != expectedVictim)
          return _NONE;

        return victim;
      }

    /* Does the given move describe an en-passant capture on the current board? */
    public boolean isEnPassantAttack(Move move)
      {
        return enPassantVictim(move) != _NONE;
      }

    /* Do the given indices describe a pawn double move, on the given board? */
    public boolean isPawnDoubleMove(int from, int to)
      {
        if(!isPawn(from))
          return false;

        if(col(from) != col(to))
          return false;

        if(isWhite(from))
          return row(from) == 2 && row(to) == 4;

        return row(from) == 9 && row(to) == 7;
      }

    /* Do the given indices describe a pawn triple move, on the given board? */
    public boolean isPawnTripleMove(int from, int to)
      {
        if(!isPawn(from))
          return false;

        if(col(from) != col(to))
          return false;

        if(isWhite(from))
          return row(from) == 2 && row(to) == 5;

        return row(from) == 9 && row(to) == 6;
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
                movesCtr += potentialmovesCtr;
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
                    gs.copyFrom(this);                              //  Copy the game state
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
                for(i = 0; i < potentialmovesCtr; i++)              //  For every move, make that move, then test the resultant board
                  {
                    gs.copyFrom(this);                              //  Copy the game state
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
                    movesCtr += 6;
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
                    movesCtr += 6;
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
                    movesCtr += 6;
                  }
                else
                  {
                    buffer[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                    movesCtr++;
                  }
              }
            enpassantLen = getPawnEnPassantAttacks(index, enpassant);
            for(i = 0; i < enpassantLen; i++)
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
                    movesCtr += 6;
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
                    movesCtr += 6;
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
                    movesCtr += 6;
                  }
                else
                  {
                    buffer[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                    movesCtr++;
                  }
              }
            enpassantLen = getPawnEnPassantAttacks(index, enpassant);
            for(i = 0; i < enpassantLen; i++)
              {
                buffer[movesCtr] = new Move(index, enpassant[i].to, _NO_PROMO);
                movesCtr++;
              }
          }

        return movesCtr;
      }

    /* "buffer" must contain at least two entries. */
    public int getPawnEnPassantAttacks(int index, Move[] buffer)
      {
        int movesCtr = 0;
        int leftDestination, rightDestination;

        if(!isPawn(index))
          return 0;

        if(isWhite(index))
          {
            leftDestination = ul(index);
            rightDestination = ur(index);
          }
        else
          {
            leftDestination = dl(index);
            rightDestination = dr(index);
          }

        if(!oob(leftDestination) && enPassantVictim(index, leftDestination) != _NONE)
          {
            buffer[movesCtr] = new Move(index, leftDestination, _NO_PROMO);
            movesCtr++;
          }

        if(!oob(rightDestination) && enPassantVictim(index, rightDestination) != _NONE)
          {
            buffer[movesCtr] = new Move(index, rightDestination, _NO_PROMO);
            movesCtr++;
          }

        return movesCtr;
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
        else if(moveCtr >= 100)                                     //  Game is over if the move counter reaches 100.
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

    public boolean isWhiteToMove()
      {
        return whiteToMove;
      }

    /*  Is the given index i vacant? */
    public boolean isEmpty(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] == 0x00);
        return false;
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
        if(i >= 0 && i < _NONE)
          return (board[i] == _WHITE_PAWN || board[i] == _BLACK_PAWN);
        return false;
      }

    /*  Is the given index i occupied by a Knight? */
    public boolean isKnight(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] == _WHITE_KNIGHT || board[i] == _BLACK_KNIGHT);
        return false;
      }

    /*  Is the given index i occupied by a Champion? */
    public boolean isChampion(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] == _WHITE_CHAMPION || board[i] == _BLACK_CHAMPION);
        return false;
      }

    /*  Is the given index i occupied by a Wizard? */
    public boolean isWizard(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] == _WHITE_WIZARD || board[i] == _BLACK_WIZARD);
        return false;
      }

    /*  Is the given index i occupied by a Bishop? */
    public boolean isBishop(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] == _WHITE_BISHOP || board[i] == _BLACK_BISHOP);
        return false;
      }

    /*  Is the given index i occupied by a Rook? */
    public boolean isRook(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] == _WHITE_ROOK || board[i] == _BLACK_ROOK);
        return false;
      }

    /*  Is the given index i occupied by a Queen? */
    public boolean isQueen(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] == _WHITE_QUEEN || board[i] == _BLACK_QUEEN);
        return false;
      }

    /*  Is the given index i occupied by an King? */
    public boolean isKing(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] == _WHITE_KING || board[i] == _BLACK_KING);
        return false;
      }

    public int getKingIndex(boolean white)
      {
        int i = 0;

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
                    indices[len] = dst;
                    len++;
                    dst = u(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    len++;
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
                    indices[len] = dst;
                    len++;
                    dst = d(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    len++;
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
                    indices[len] = dst;
                    len++;
                    dst = l(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    len++;
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
                    indices[len] = dst;
                    len++;
                    dst = r(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    len++;
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
                    indices[len] = dst;
                    len++;
                    dst = ur(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    len++;
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
                    indices[len] = dst;
                    len++;
                    dst = dr(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    len++;
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
                    indices[len] = dst;
                    len++;
                    dst = dl(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    len++;
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
                    indices[len] = dst;
                    len++;
                    dst = ul(dst);
                  }
                else if(flags[3])                                   //  Stop and include Black
                  {
                    indices[len] = dst;
                    len++;
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
        if(i >= 0 && i < _NONE)
          return i % 12;
        return _NONE;
      }

    /*  Compute the ROW in which given index is included. */
    public int row(int i)
      {
        if(i >= 0 && i < _NONE)
          return (i - (i % 12)) / 12;
        return _NONE;
      }

    /* Is the given index out of bounds or == _NONE? */
    public boolean oob(int i)
      {
        if(i >= 0 && i < _NONE)
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