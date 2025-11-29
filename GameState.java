import java.io.*;
import java.lang.Math;
import java.lang.StringBuilder;

public class GameState
  {
    public static final int _NONE                 = 144;
    public static final char _NO_PROMO            = '0';

    public static final int GAME_ONGOING          = 0;
    public static final int GAME_OVER_WHITE_WINS  = 1;
    public static final int GAME_OVER_BLACK_WINS  = 2;
    public static final int GAME_OVER_STALEMATE   = 3;

    public static final boolean __GAMESTATE_DEBUG = false;

    private char board[];                                           //  144 characters for 144 squares
    private boolean whiteKingsideLiberty;                           //  6  booleans
    private boolean whiteQueensideLiberty;
    private boolean blackKingsideLiberty;
    private boolean blackQueensideLiberty;
    private boolean whiteHasCastled;
    private boolean blackHasCastled;
    private int previousFrom;                                       //  2 ints
    private int previousTo;
    private boolean white;                                          //  Whether white is to move

    /****************************************************************/
    //  Constructor
    public GameState()                                              //  Default board is starting position
      {
        int i;
        board = new char[_NONE];
        for(i = 0; i < _NONE; i++)
          board[i] = 'e';

        board[0]  = 'W';
        board[11] = 'W';
        board[13] = 'C';
        board[14] = 'R';
        board[15] = 'N';
        board[16] = 'I';
        board[17] = 'Q';
        board[18] = 'K';
        board[19] = 'I';
        board[20] = 'N';
        board[21] = 'R';
        board[22] = 'C';

        for(i = 25; i < 35; i++)
          board[i] = 'P';

        for(i = 109; i < 119; i++)
          board[i] = 'p';

        board[121] = 'c';
        board[122] = 'r';
        board[123] = 'n';
        board[124] = 'i';
        board[125] = 'q';
        board[126] = 'k';
        board[127] = 'i';
        board[128] = 'n';
        board[129] = 'r';
        board[130] = 'c';
        board[132] = 'w';
        board[143] = 'w';

        whiteKingsideLiberty = true;
        whiteQueensideLiberty = true;
        blackKingsideLiberty = true;
        blackQueensideLiberty = true;
        whiteHasCastled = false;
        blackHasCastled = false;
        previousFrom = _NONE;
        previousTo = _NONE;

        white = true;
      }

    /****************************************************************/
    //  Setters & getters

    /* Set this object's board according to the given array. */
    public void setBoard(char[] b)
      {
        int i;
        for(i = 0; i < _NONE; i++)
          board[i] = b[i];
        return;
      }

    public char[] getBoard()
      {
        return board;
      }

    public void setCastlingData(boolean whiteK, boolean whiteQ, boolean blackK, boolean blackQ, boolean w, boolean b)
      {
        whiteKingsideLiberty = whiteK;
        whiteQueensideLiberty = whiteQ;
        blackKingsideLiberty = blackK;
        blackQueensideLiberty = blackQ;
        whiteHasCastled = w;
        blackHasCastled = b;
        return;
      }

    public boolean whiteKingsideCastlePrivilege()
      {
        return whiteKingsideLiberty;
      }

    public boolean whiteQueensideCastlePrivilege()
      {
        return whiteQueensideLiberty;
      }

    public boolean blackKingsideCastlePrivilege()
      {
        return blackKingsideLiberty;
      }

    public boolean blackQueensideCastlePrivilege()
      {
        return blackQueensideLiberty;
      }

    public boolean hasWhiteCastled()
      {
        return whiteHasCastled;
      }

    public boolean hasBlackCastled()
      {
        return blackHasCastled;
      }

    public Move lastMove()
      {
        Move move = new Move(previousFrom, previousTo, _NO_PROMO);
        return move;
      }

    public void setPreviousMove(int prevFrom, int prevTo)
      {
        previousFrom = prevFrom;
        previousTo = prevTo;
        return;
      }

    public void setWhiteToMove(boolean w)
      {
        white = w;
        return;
      }

    public int getKingIndex(char team)
      {
        int i = 0;
        if(team == 'w')
          {
            while(i < _NONE && board[i] != 'K')
              i++;
          }
        else
          {
            while(i < _NONE && board[i] != 'k')
              i++;
          }
        return i;
      }

    /****************************************************************/
    //  Move generation and application
    public void makeMove(int from, int to)
      {
        makeMove(from, to, _NO_PROMO);
      }

    public void makeMove(int from, int to, char promo)
      {
        if(isBlackKingside(from, to))                               //  Black Kings-Castle
          {
            board[126] = 'e';
            board[127] = 'r';
            board[128] = 'k';
            board[129] = 'e';
            blackKingsideLiberty = false;                           //  Black cannot Kingside (again)
            blackQueensideLiberty = false;                          //  Black cannot Queenside
            blackHasCastled = true;                                 //  Black has castled
          }
        else if(isBlackQueenside(from, to))                         //  Black Queens-Castle
          {
            board[126] = 'e';
            board[125] = 'r';
            board[124] = 'k';
            board[123] = 'e';
            board[122] = 'e';
            blackKingsideLiberty = false;                           //  Black cannot Kingside
            blackQueensideLiberty = false;                          //  Black cannot Queenside (again)
            blackHasCastled = true;                                 //  Black has castled
          }
        else if(isWhiteKingside(from, to))                          //  White Kings-Castle
          {
            board[18] = 'e';
            board[19] = 'R';
            board[20] = 'K';
            board[21] = 'e';
            whiteKingsideLiberty = false;                           //  White cannot Kingside (again)
            whiteQueensideLiberty = false;                          //  White cannot Queenside
            whiteHasCastled = true;                                 //  White has castled
          }
        else if(isWhiteQueenside(from, to))                         //  White Queens-Castle
          {
            board[18] = 'e';
            board[17] = 'R';
            board[16] = 'K';
            board[15] = 'e';
            board[14] = 'e';
            whiteKingsideLiberty = false;                           //  White cannot Kingside
            whiteQueensideLiberty = false;                          //  White cannot Queenside (again)
            whiteHasCastled = true;                                 //  White has castled
          }
        else if(isEnPassantAttack(from, to))                        //  En-passant capture
          {
            board[ enPassantVictim(from, to) ] = 'e';
            board[to] = board[from];
            board[from] = 'e';
          }
        else
          {
            if(isKing(from) && isWhite(from))                       //  White King moved: castling rights lost
              {
                whiteKingsideLiberty = false;
                whiteQueensideLiberty = false;
              }
            else if(isRook(from) && isWhite(from) && from == 21)    //  White King's Rook moved: Kingside rights lost
              whiteKingsideLiberty = false;
            else if(isRook(from) && isWhite(from) && from == 14)    //  White Queen's Rook moved: Queenside rights lost
              whiteQueensideLiberty = false;
            else if(isKing(from) && isBlack(from))                  //  Black King moved: castling rights lost
              {
                blackKingsideLiberty = false;
                blackQueensideLiberty = false;
              }
            else if(isRook(from) && isBlack(from) && from == 129)   //  Black King's Rook moved: Kingside rights lost
              blackKingsideLiberty = false;
            else if(isRook(from) && isBlack(from) && from == 122)   //  Black Queen's Rook moved: Queenside rights lost
              blackQueensideLiberty = false;
                                                                    //  Pawn promotion
            if(isPawn(from) && promo != _NO_PROMO && (row(to) == 10 || row(to) == 1))
              {
                board[to] = promo;
                board[from] = 'e';
              }
            else                                                    //  Any other case
              {
                board[to] = board[from];
                board[from] = 'e';
              }
          }

        if(wasPawnExtendedMove(from, to))                           //  Save last move IFF last move was a pawn extended-move!
          {
            previousFrom = from;
            previousTo = to;
          }
        else
          {
            previousFrom = _NONE;
            previousTo = _NONE;
          }

        return;
      }

    public char nowToMove()
      {
        return white ? 'w' : 'b';
      }

    public char nextToMove()
      {
        return white ? 'b' : 'w';
      }

    public void switchSideToMove()
      {
        white = !white;
        return;
      }

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

                enemyStrikeCtr += attacks.length;                   //  Count up strikes
              }
          }

        if(enemyStrikeCtr > 0)                                      //  We've counted... now allocate
          {
            enemytargets = new int[enemyStrikeCtr];
            enemyStrikeCtr = 0;                                     //  Reset
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

                    enemyStrikeCtr += attacks.length;               //  Increase offset
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

    /****************************************************************/
    //  Attacks

    /* Do the given indices describe an en passant attack?
       Recall that in OMEGA CHESS, a pawn's initial move may span two or three squares. */
    public boolean isEnPassantAttack(int from, int to)
      {
        if(previousFrom < _NONE && previousTo < _NONE &&
           wasPawnExtendedMove(previousFrom, previousTo) &&
           !sameSide(from, previousTo) && isPawn(previousTo) && isPawn(from) &&
           (row(previousTo) == row(from) ||
            (row(previousTo) == row(from) + 1 && isWhite(previousTo) && isBlack(from)) ||
            (row(previousTo) == row(from) - 1 && isBlack(previousTo) && isWhite(from))) )
          {
            if((to == u(previousTo) && isWhite(from) && isEmpty(to)) || (to == u(u(previousTo)) && isWhite(from) && isEmpty(to)))
              return true;
            if((to == d(previousTo) && isBlack(from) && isEmpty(to)) || (to == d(d(previousTo)) && isBlack(from) && isEmpty(to)))
              return true;
          }
        return false;
      }

    /* Be careful with this method! It works only on the assumption that (from, to) constitute an en passant capture in the first place! */
    public int enPassantVictim(int from, int to)
      {
        if(isWhite(from))
          {
            if(isEmpty(d(to)))
              return d(d(to));
            return d(to);
          }

        if(isEmpty(u(to)))
          return u(u(to));
        return u(to);
      }

    /* Do the given values a (from) and b (to) and the current state of the board indicate a double pawn move or a triple pawn move? */
    public boolean wasPawnExtendedMove(int a, int b)
      {
        if(isPawn(b) && isWhite(b) && row(a) == 2 && row(b) > 3)
          return true;
        if(isPawn(b) && isBlack(b) && row(a) == 9 && row(b) < 8)
          return true;
        return false;
      }

    /* THIS FUNCTION FILTERS FOR CHECK!!
       All actual attacks (enemy-occupied only). */
    public Move[] getAttacks(char team)
      {
        Move[] moves;
        int movesCtr = 0;
        Move[] potentialmoves;
        int index, i, j, k;

        for(index = 0; index < _NONE; index++)
          {
            if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
              {
                if(isPawn(index))
                  potentialmoves = getPawnAttacks(index);
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
                  potentialmoves = getKingNonCastle(index);

                if(team == 'w')                                     //  White team: check for checks on the King by Black
                  {
                    for(i = 0; i < potentialmoves.length; i++)      //  For every move, make that move, then test the resultant board
                      {
                        if(!isEmpty(potentialmoves[i].to))          //  IFF square is occupied by enemy piece
                          {
                            GameState gs = new GameState();         //  Create a new GameState
                            gs.setBoard( board );                   //  Copy the board
                            gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                blackKingsideLiberty, blackQueensideLiberty,
                                                whiteHasCastled, blackHasCastled );
                            gs.setPreviousMove( previousFrom, previousTo );
                            gs.setWhiteToMove( white );
                            gs.makeMove(potentialmoves[i].from,     //  Apply the candidate move
                                        potentialmoves[i].to,
                                        potentialmoves[i].promo);

                            k = gs.getKingIndex('w');               //  Locate the king on the new board

                            if(k == _NONE)
                              System.out.println("ERROR: the white king is missing from the board!");

                            if(!gs.inCheckBy(k, 'b'))               //  If king not in check,
                              movesCtr++;                           //  then move is allowed
                          }
                      }
                  }
                else                                                //  Black team: check for checks on the King by White
                  {
                    for(i = 0; i < potentialmoves.length; i++)      //  For every move, make that move, then test the resultant board
                      {
                        if(!isEmpty(potentialmoves[i].to))          //  IFF square is occupied by enemy piece
                          {
                            GameState gs = new GameState();         //  Create a new GameState
                            gs.setBoard( board );                   //  Copy the board
                            gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                blackKingsideLiberty, blackQueensideLiberty,
                                                whiteHasCastled, blackHasCastled );
                            gs.setPreviousMove( previousFrom, previousTo );
                            gs.setWhiteToMove( white );
                            gs.makeMove(potentialmoves[i].from,     //  Apply the candidate move
                                        potentialmoves[i].to,
                                        potentialmoves[i].promo);

                            k = gs.getKingIndex('b');               //  Locate the king on the new board

                            if(k == _NONE)
                              System.out.println("ERROR: the black king is missing from the board!");

                            if(!gs.inCheckBy(k, 'w'))               //  If king not in check,
                              movesCtr++;                           //  then move is allowed
                          }
                      }
                  }
              }
          }

        if(movesCtr > 0)                                            //  If there are any moves
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            for(index = 0; index < _NONE; index++)
              {
                if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
                  {
                    if(isPawn(index))
                      potentialmoves = getPawnAttacks(index);
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
                      potentialmoves = getKingNonCastle(index);

                    if(team == 'w')                                 //  White team: check for checks on the King by Black
                      {
                        for(i = 0; i < potentialmoves.length; i++)  //  For every move, make that move, then test the resultant board
                          {
                            if(!isEmpty(potentialmoves[i].to))      //  IFF square is occupied by enemy piece
                              {
                                GameState gs = new GameState();     //  Create a new GameState
                                gs.setBoard( board );               //  Copy the board
                                gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                    blackKingsideLiberty, blackQueensideLiberty,
                                                    whiteHasCastled, blackHasCastled );
                                gs.setPreviousMove( previousFrom, previousTo );
                                gs.setWhiteToMove( white );
                                gs.makeMove(potentialmoves[i].from, //  Apply the candidate move
                                            potentialmoves[i].to,
                                            potentialmoves[i].promo);

                                k = gs.getKingIndex('w');           //  Locate the king on the new board

                                if(k == _NONE)
                                  System.out.println("ERROR: the white king is missing from the board!");

                                if(!gs.inCheckBy(k, 'b'))           //  If king not in check,
                                  {                                 //  then move is allowed
                                    moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                                    movesCtr++;
                                  }
                              }
                          }
                      }
                    else                                            //  Black team: check for checks on the King by White
                      {
                        for(i = 0; i < potentialmoves.length; i++)  //  For every move, make that move, then test the resultant board
                          {
                            if(!isEmpty(potentialmoves[i].to))      //  IFF square is occupied by enemy piece
                              {
                                GameState gs = new GameState();     //  Create a new GameState
                                gs.setBoard( board );               //  Copy the board
                                gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                    blackKingsideLiberty, blackQueensideLiberty,
                                                    whiteHasCastled, blackHasCastled );
                                gs.setPreviousMove( previousFrom, previousTo );
                                gs.setWhiteToMove( white );
                                gs.makeMove(potentialmoves[i].from, //  Apply the candidate move
                                            potentialmoves[i].to,
                                            potentialmoves[i].promo);

                                k = gs.getKingIndex('b');           //  Locate the king on the new board

                                if(k == _NONE)
                                  System.out.println("ERROR: the black king is missing from the board!");

                                if(!gs.inCheckBy(k, 'w'))           //  If king not in check,
                                  {                                 //  then move is allowed
                                    moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                                    movesCtr++;
                                  }
                              }
                          }
                      }
                  }
              }

            return moves;
          }

        return new Move[0];
      }

    /* THIS FUNCTION FILTERS FOR CHECK!!
       Same as above, but only looking at real pawn attacks for the team. */
    public Move[] getTeamPawnAttacks(char team)
      {
        Move[] moves;
        int movesCtr = 0;
        Move[] potentialmoves;
        int index, i, k;

        for(index = 0; index < _NONE; index++)
          {
            if(isPawn(index))
              {
                if( (team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)) )
                  potentialmoves = getPawnAttacks(index);
                else
                  potentialmoves = new Move[0];

                if(team == 'w')                                     //  White team: check for checks on the King by Black
                  {
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
                          movesCtr++;                               //  then move is allowed
                      }
                  }
                else                                                //  Black team: check for checks on the King by White
                  {
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
                          movesCtr++;                               //  then move is allowed
                      }
                  }
              }
          }

        if(movesCtr > 0)                                            //  If any moves at all
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            for(index = 0; index < _NONE; index++)
              {
                if(isPawn(index))
                  {
                    if( (team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)) )
                      potentialmoves = getPawnAttacks(index);
                    else
                      potentialmoves = new Move[0];

                    if(team == 'w')                                 //  White team: check for checks on the King by Black
                      {
                        for(i = 0; i < potentialmoves.length; i++)  //  For every move, make that move, then test the resultant board
                          {
                            GameState gs = new GameState();         //  Create a new GameState
                            gs.setBoard( board );                   //  Copy the board
                            gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                blackKingsideLiberty, blackQueensideLiberty,
                                                whiteHasCastled, blackHasCastled );
                            gs.setPreviousMove( previousFrom, previousTo );
                            gs.setWhiteToMove( white );
                            gs.makeMove(potentialmoves[i].from,     //  Apply the candidate move
                                        potentialmoves[i].to,
                                        potentialmoves[i].promo);

                            k = gs.getKingIndex('w');               //  Locate the king on the new board

                            if(k == _NONE)
                              System.out.println("ERROR: the white king is missing from the board!");

                            if(!gs.inCheckBy(k, 'b'))               //  If king not in check,
                              {                                     //  then move is allowed
                                moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                                movesCtr++;
                              }
                          }
                      }
                    else                                            //  Black team: check for checks on the King by White
                      {
                        for(i = 0; i < potentialmoves.length; i++)  //  For every move, make that move, then test the resultant board
                          {
                            GameState gs = new GameState();         //  Create a new GameState
                            gs.setBoard( board );                   //  Copy the board
                            gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                blackKingsideLiberty, blackQueensideLiberty,
                                                whiteHasCastled, blackHasCastled );
                            gs.setPreviousMove( previousFrom, previousTo );
                            gs.setWhiteToMove( white );
                            gs.makeMove(potentialmoves[i].from,     //  Apply the candidate move
                                        potentialmoves[i].to,
                                        potentialmoves[i].promo);

                            k = gs.getKingIndex('b');               //  Locate the king on the new board

                            if(k == _NONE)
                              System.out.println("ERROR: the black king is missing from the board!");

                            if(!gs.inCheckBy(k, 'w'))               //  If king not in check,
                              {                                     //  then move is allowed
                                moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                                movesCtr++;
                              }
                          }
                      }
                  }
              }
            return moves;
          }

        return new Move[0];
      }

    /* Returns list of *ALL* pawn attack-style moves--INCLUDING PROMOTION DUPLICATES. */
    public Move[] getPawnAttacks(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int i;
        Move[] tmp;

        if(isWhite(index))
          {
            if(!oob(ul(index)) && row(ul(index)) == row(index) + 1 && col(ul(index)) == col(index) - 1 && isBlack(ul(index)))
              {
                if(row(ul(index)) == 10)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
            if(!oob(ur(index)) && row(ur(index)) == row(index) + 1 && col(ur(index)) == col(index) + 1 && isBlack(ur(index)))
              {
                if(row(ur(index)) == 10)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
            tmp = getPawnEnPassantAttacks(index);
            movesCtr += tmp.length;
          }
        else
          {
            if(!oob(dl(index)) && row(dl(index)) == row(index) - 1 && col(dl(index)) == col(index) - 1 && isWhite(dl(index)))
              {
                if(row(dl(index)) == 1)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
            if(!oob(dr(index)) && row(dr(index)) == row(index) - 1 && col(dr(index)) == col(index) + 1 && isWhite(dr(index)))
              {
                if(row(dr(index)) == 1)
                  movesCtr += 6;
                else
                  movesCtr++;
              }
            tmp = getPawnEnPassantAttacks(index);
            movesCtr += tmp.length;
          }

        if(movesCtr > 0)                                            //  If any moves at all
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            if(isWhite(index))
              {
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
                if(!oob(ur(index)) && row(ur(index)) == row(index) + 1 && col(ur(index)) == col(index) + 1 && isBlack(ur(index)))
                  {
                    if(row(ur(index)) == 10)
                      {
                        moves[movesCtr] = new Move(index, ur(index), 'N');
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
                tmp = getPawnEnPassantAttacks(index);
                for(i = 0; i < tmp.length; i++)
                  {
                    moves[movesCtr] = new Move(index, tmp[i].to, _NO_PROMO);
                    movesCtr++;
                  }
              }
            else
              {
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
                tmp = getPawnEnPassantAttacks(index);
                for(i = 0; i < tmp.length; i++)
                  {
                    moves[movesCtr] = new Move(index, tmp[i].to, _NO_PROMO);
                    movesCtr++;
                  }
              }

            return moves;
          }

        return new Move[0];
      }

    /* Compute list of pawn attacks for all pawns of given team, regardless of the attacked squares' occupancy and without considering promotions. */
    public Move[] getPawnTargets(char team)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] pawns;
        Move[] enpass;
        int i, j;

        if(team == 'w')
          {
            pawns = getPawns(team);

            for(i = 0; i < pawns.length; i++)
              {
                if(!oob(ul(pawns[i])) && row(ul(pawns[i])) == row(pawns[i]) + 1 && col(ul(pawns[i])) == col(pawns[i]) - 1)
                  movesCtr++;
                if(!oob(ur(pawns[i])) && row(ur(pawns[i])) == row(pawns[i]) + 1 && col(ur(pawns[i])) == col(pawns[i]) + 1)
                  movesCtr++;

                enpass = getPawnEnPassantAttacks(i);
                movesCtr += enpass.length;
              }

            if(movesCtr > 0)                                        //  If any moves at all
              {
                moves = new Move[movesCtr];                         //  We've counted, now allocate
                movesCtr = 0;                                       //  Reset

                for(i = 0; i < pawns.length; i++)
                  {
                    if(!oob(ul(pawns[i])) && row(ul(pawns[i])) == row(pawns[i]) + 1 && col(ul(pawns[i])) == col(pawns[i]) - 1)
                      {
                        moves[movesCtr] = new Move(pawns[i], ul(pawns[i]), _NO_PROMO);
                        movesCtr++;
                      }
                    if(!oob(ur(pawns[i])) && row(ur(pawns[i])) == row(pawns[i]) + 1 && col(ur(pawns[i])) == col(pawns[i]) + 1)
                      {
                        moves[movesCtr] = new Move(pawns[i], ur(pawns[i]), _NO_PROMO);
                        movesCtr++;
                      }

                    enpass = getPawnEnPassantAttacks(i);
                    for(j = 0; j < enpass.length; j++)
                      {
                        moves[movesCtr] = new Move(enpass[j].from, enpass[j].to, enpass[j].promo);
                        movesCtr++;
                      }
                  }
                return moves;
              }
          }
        else
          {
            pawns = getPawns(team);

            for(i = 0; i < pawns.length; i++)
              {
                if(!oob(dl(pawns[i])) && row(dl(pawns[i])) == row(pawns[i]) - 1 && col(dl(pawns[i])) == col(pawns[i]) - 1)
                  movesCtr++;
                if(!oob(dr(pawns[i])) && row(dr(pawns[i])) == row(pawns[i]) - 1 && col(dr(pawns[i])) == col(pawns[i]) + 1)
                  movesCtr++;

                enpass = getPawnEnPassantAttacks(i);
                movesCtr += enpass.length;
              }

            if(movesCtr > 0)                                        //  If any moves at all
              {
                moves = new Move[movesCtr];                         //  We've counted, now allocate
                movesCtr = 0;                                       //  Reset

                for(i = 0; i < pawns.length; i++)
                  {
                    if(!oob(dl(pawns[i])) && row(dl(pawns[i])) == row(pawns[i]) - 1 && col(dl(pawns[i])) == col(pawns[i]) - 1)
                      {
                        moves[movesCtr] = new Move(pawns[i], dl(pawns[i]), _NO_PROMO);
                        movesCtr++;
                      }
                    if(!oob(dr(pawns[i])) && row(dr(pawns[i])) == row(pawns[i]) - 1 && col(dr(pawns[i])) == col(pawns[i]) + 1)
                      {
                        moves[movesCtr] = new Move(pawns[i], dr(pawns[i]), _NO_PROMO);
                        movesCtr++;
                      }

                    enpass = getPawnEnPassantAttacks(i);
                    for(j = 0; j < enpass.length; j++)
                      {
                        moves[movesCtr] = new Move(enpass[j].from, enpass[j].to, enpass[j].promo);
                        movesCtr++;
                      }
                  }
                return moves;
              }
          }

        return new Move[0];
      }

    /* Compute list of pawn attacks from a given index, regardless of the attacked squares' occupancy and without considering promotion. */
    public Move[] getPawnTargetsIndex(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        Move[] enpass;
        int i;

        if(isWhite(index))
          {
            if(!oob(ul(index)) && row(ul(index)) == row(index) + 1 && col(ul(index)) == col(index) - 1)
              movesCtr++;
            if(!oob(ur(index)) && row(ur(index)) == row(index) + 1 && col(ur(index)) == col(index) + 1)
              movesCtr++;

            enpass = getPawnEnPassantAttacks(index);
            movesCtr += enpass.length;

            if(movesCtr > 0)                                        //  If any moves at all
              {
                moves = new Move[movesCtr];                         //  We've counted, now allocate
                movesCtr = 0;                                       //  Reset

                if(!oob(ul(index)) && row(ul(index)) == row(index) + 1 && col(ul(index)) == col(index) - 1)
                  {
                    moves[movesCtr] = new Move(index, ul(index), _NO_PROMO);
                    movesCtr++;
                  }
                if(!oob(ur(index)) && row(ur(index)) == row(index) + 1 && col(ur(index)) == col(index) + 1)
                  {
                    moves[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                    movesCtr++;
                  }

                enpass = getPawnEnPassantAttacks(index);
                for(i = 0; i < enpass.length; i++)
                  {
                    moves[movesCtr] = new Move(enpass[i].from, enpass[i].to, enpass[i].promo);
                    movesCtr++;
                  }
                return moves;
              }
          }
        else if(isBlack(index))
         {
            if(!oob(dl(index)) && row(dl(index)) == row(index) - 1 && col(dl(index)) == col(index) - 1)
              movesCtr++;
            if(!oob(dr(index)) && row(dr(index)) == row(index) - 1 && col(dr(index)) == col(index) + 1)
              movesCtr++;

            enpass = getPawnEnPassantAttacks(index);
            movesCtr += enpass.length;

            if(movesCtr > 0)                                        //  Any moves at all
              {
                moves = new Move[movesCtr];                         //  We've counted, now allocate
                movesCtr = 0;                                       //  Reset

                if(!oob(dl(index)) && row(dl(index)) == row(index) - 1 && col(dl(index)) == col(index) - 1)
                  {
                    moves[movesCtr] = new Move(index, dl(index), _NO_PROMO);
                    movesCtr++;
                  }
                if(!oob(dr(index)) && row(dr(index)) == row(index) - 1 && col(dr(index)) == col(index) + 1)
                  {
                    moves[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                    movesCtr++;
                  }

                enpass = getPawnEnPassantAttacks(index);
                for(i = 0; i < enpass.length; i++)
                  {
                    moves[movesCtr] = new Move(enpass[i].from, enpass[i].to, enpass[i].promo);
                    movesCtr++;
                  }
                return moves;
              }
          }

        return new Move[0];
      }

    public Move[] getPawnEnPassantAttacks(int index)
      {
        Move[] moves;
        int movesCtr = 0;

        if(previousFrom < _NONE && previousTo < _NONE)
          {
            if( wasPawnExtendedMove(previousFrom, previousTo) &&
               !sameSide(index, previousTo) &&
               isPawn(previousTo) && isPawn(index) &&
               (row(previousTo) == row(index) ||
                (row(previousTo) == row(index) + 1 && isWhite(previousTo) && isBlack(index)) ||
                (row(previousTo) == row(index) - 1 && isBlack(previousTo) && isWhite(index))) )
              {
                if(col(index) - 1 == col(previousTo))               //  En-passant capture to the left
                  {
                    if(isWhite(index))
                      {
                        moves = new Move[1];

                        moves[movesCtr] = new Move(index, ul(index), _NO_PROMO);

                        movesCtr++;
                      }
                    else
                      {
                        moves = new Move[1];

                        moves[movesCtr] = new Move(index, dl(index), _NO_PROMO);

                        movesCtr++;
                      }
                    return moves;
                  }
                else if(col(index) + 1 == col(previousTo))          //  En-passant capture to the right
                  {
                    if(isWhite(index))
                      {
                        moves = new Move[1];

                        moves[movesCtr] = new Move(index, ur(index), _NO_PROMO);

                        movesCtr++;
                      }
                    else
                      {
                        moves = new Move[1];

                        moves[movesCtr] = new Move(index, dr(index), _NO_PROMO);

                        movesCtr++;
                      }
                    return moves;
                  }
              }
          }

        return new Move[0];
      }

    /****************************************************************/
    //  Coverage

    /* THIS FUNCTION FILTERS FOR CHECK!!
       All actual attacks (ally-occupied only). */
    public Move[] getCoverage(char team)
      {
        Move[] moves;
        int movesCtr = 0;
        Move[] potentialmoves;
        int index, i, j, k;

        for(index = 0; index < _NONE; index++)
          {
            if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
              {
                if(isPawn(index))
                  potentialmoves = getPawnCoverage(index);
                else if(isKnight(index))
                  potentialmoves = getKnightCoverage(index);
                else if(isChampion(index))
                  potentialmoves = getChampionCoverage(index);
                else if(isWizard(index))
                  potentialmoves = getWizardCoverage(index);
                else if(isBishop(index))
                  potentialmoves = getBishopCoverage(index);
                else if(isRook(index))
                  potentialmoves = getRookCoverage(index);
                else if(isQueen(index))
                  potentialmoves = getQueenCoverage(index);
                else
                  potentialmoves = getKingCoverage(index);

                if(team == 'w')                                     //  White team: check for checks on the King by Black
                  {
                    for(i = 0; i < potentialmoves.length; i++)      //  For every move, make that move, then test the resultant board
                      {
                        if(!isKing(potentialmoves[i].to))           //  You cannot "cover" the king
                          {
                            GameState gs = new GameState();         //  Create a new GameState
                            gs.setBoard( board );                   //  Copy the board
                            gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                blackKingsideLiberty, blackQueensideLiberty,
                                                whiteHasCastled, blackHasCastled );
                            gs.setPreviousMove( previousFrom, previousTo );
                            gs.setWhiteToMove( white );
                            gs.makeMove(potentialmoves[i].from,     //  Apply the candidate move
                                        potentialmoves[i].to,
                                        potentialmoves[i].promo);

                            k = gs.getKingIndex('w');               //  Locate the king on the new board

                            if(k == _NONE)
                              System.out.println("ERROR: the white king is missing from the board!");

                            if(!gs.inCheckBy(k, 'b'))               //  If king not in check,
                              movesCtr++;                           //  then move is allowed
                          }
                      }
                  }
                else                                                //  Black team: check for checks on the King by White
                  {
                    for(i = 0; i < potentialmoves.length; i++)      //  For every move, make that move, then test the resultant board
                      {
                        if(!isKing(potentialmoves[i].to))           //  You cannot "cover" the king
                          {
                            GameState gs = new GameState();         //  Create a new GameState
                            gs.setBoard( board );                   //  Copy the board
                            gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                blackKingsideLiberty, blackQueensideLiberty,
                                                whiteHasCastled, blackHasCastled );
                            gs.setPreviousMove( previousFrom, previousTo );
                            gs.setWhiteToMove( white );
                            gs.makeMove(potentialmoves[i].from,     //  Apply the candidate move
                                        potentialmoves[i].to,
                                        potentialmoves[i].promo);

                            k = gs.getKingIndex('b');               //  Locate the king on the new board

                            if(k == _NONE)
                              System.out.println("ERROR: the black king is missing from the board!");

                            if(!gs.inCheckBy(k, 'w'))               //  If king not in check,
                              movesCtr++;                           //  then move is allowed
                          }
                      }
                  }
              }
          }

        if(movesCtr > 0)                                            //  If there are any moves
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            for(index = 0; index < _NONE; index++)
              {
                if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
                  {
                    if(isPawn(index))
                      potentialmoves = getPawnCoverage(index);
                    else if(isKnight(index))
                      potentialmoves = getKnightCoverage(index);
                    else if(isChampion(index))
                      potentialmoves = getChampionCoverage(index);
                    else if(isWizard(index))
                      potentialmoves = getWizardCoverage(index);
                    else if(isBishop(index))
                      potentialmoves = getBishopCoverage(index);
                    else if(isRook(index))
                      potentialmoves = getRookCoverage(index);
                    else if(isQueen(index))
                      potentialmoves = getQueenCoverage(index);
                    else
                      potentialmoves = getKingCoverage(index);

                    if(team == 'w')                                 //  White team: check for checks on the King by Black
                      {
                        for(i = 0; i < potentialmoves.length; i++)  //  For every move, make that move, then test the resultant board
                          {
                            if(!isKing(potentialmoves[i].to))       //  You cannot "cover" the king
                              {
                                GameState gs = new GameState();     //  Create a new GameState
                                gs.setBoard( board );               //  Copy the board
                                gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                    blackKingsideLiberty, blackQueensideLiberty,
                                                    whiteHasCastled, blackHasCastled );
                                gs.setPreviousMove( previousFrom, previousTo );
                                gs.setWhiteToMove( white );
                                gs.makeMove(potentialmoves[i].from, //  Apply the candidate move
                                            potentialmoves[i].to,
                                            potentialmoves[i].promo);

                                k = gs.getKingIndex('w');           //  Locate the king on the new board

                                if(k == _NONE)
                                  System.out.println("ERROR: the white king is missing from the board!");

                                if(!gs.inCheckBy(k, 'b'))           //  If king not in check,
                                  {                                 //  then move is allowed
                                    moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                                    movesCtr++;
                                  }
                              }
                          }
                      }
                    else                                            //  Black team: check for checks on the King by White
                      {
                        for(i = 0; i < potentialmoves.length; i++)  //  For every move, make that move, then test the resultant board
                          {
                            if(!isKing(potentialmoves[i].to))       //  You cannot "cover" the king
                              {
                                GameState gs = new GameState();     //  Create a new GameState
                                gs.setBoard( board );               //  Copy the board
                                gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                    blackKingsideLiberty, blackQueensideLiberty,
                                                    whiteHasCastled, blackHasCastled );
                                gs.setPreviousMove( previousFrom, previousTo );
                                gs.setWhiteToMove( white );
                                gs.makeMove(potentialmoves[i].from, //  Apply the candidate move
                                            potentialmoves[i].to,
                                            potentialmoves[i].promo);

                                k = gs.getKingIndex('b');           //  Locate the king on the new board

                                if(k == _NONE)
                                  System.out.println("ERROR: the black king is missing from the board!");

                                if(!gs.inCheckBy(k, 'w'))           //  If king not in check,
                                  {                                 //  then move is allowed
                                    moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                                    movesCtr++;
                                  }
                              }
                          }
                      }
                  }
              }

            return moves;
          }

        return new Move[0];
      }

    /* THIS FUNCTION FILTERS FOR CHECK!!
       All actual attacks (ally-occupied only). */
    public Move[] getCoverageIndex(int index)
      {
        Move[] moves;
        Move[] potentialmoves;
        int potentialmovesCtr = 0;
        int movesCtr = 0;
        int i, k;

        if(!isEmpty(index))
          {
            if(isPawn(index))
              potentialmoves = getPawnCoverage(index);
            else if(isKnight(index))
              potentialmoves = getKnightCoverage(index);
            else if(isChampion(index))
              potentialmoves = getChampionCoverage(index);
            else if(isWizard(index))
              potentialmoves = getWizardCoverage(index);
            else if(isBishop(index))
              potentialmoves = getBishopCoverage(index);
            else if(isRook(index))
              potentialmoves = getRookCoverage(index);
            else if(isQueen(index))
              potentialmoves = getQueenCoverage(index);
            else
              potentialmoves = getKingCoverage(index);

            if(isWhite(index))                                      //  Piece is white, check for checks on the King by Black
              {
                for(i = 0; i < potentialmoves.length; i++)          //  For every move, make that move, then test the resultant board
                  {
                    if(!isKing(potentialmoves[i].to))               //  You cannot "cover" the king
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
                          movesCtr++;                               //  then move is allowed
                      }
                  }
                if(movesCtr > 0)                                    //  At least one move avoids check
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;                                   //  Reset

                    for(i = 0; i < potentialmoves.length; i++)      //  For every move, make that move, then test the resultant board
                      {
                        if(!isKing(potentialmoves[i].to))           //  You cannot "cover" the king
                          {
                            GameState gs = new GameState();         //  Create a new GameState
                            gs.setBoard( board );                   //  Copy the board
                            gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                blackKingsideLiberty, blackQueensideLiberty,
                                                whiteHasCastled, blackHasCastled );
                            gs.setPreviousMove( previousFrom, previousTo );
                            gs.setWhiteToMove( white );
                            gs.makeMove(potentialmoves[i].from,     //  Apply the candidate move
                                        potentialmoves[i].to,
                                        potentialmoves[i].promo);

                            k = gs.getKingIndex('w');               //  Locate the king on the new board

                            if(k == _NONE)
                              System.out.println("ERROR: the white king is missing from the board!");

                            if(!gs.inCheckBy(k, 'b'))               //  If king not in check,
                              {                                     //  then move is allowed
                                moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                                movesCtr++;
                              }
                          }
                      }

                    return moves;
                  }
              }
            else                                                    //  Piece is black, check for checks on the King by White
              {
                for(i = 0; i < potentialmoves.length; i++)          //  For every move, make that move, then test the resultant board
                  {
                    if(!isKing(potentialmoves[i].to))               //  You cannot "cover" the king
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
                          movesCtr++;                               //  then move is allowed
                      }
                  }
                if(movesCtr > 0)                                    //  At least one move avoids check
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;                                   //  Reset

                    for(i = 0; i < potentialmoves.length; i++)      //  For every move, make that move, then test the resultant board
                      {
                        if(!isKing(potentialmoves[i].to))           //  You cannot "cover" the king
                          {
                            GameState gs = new GameState();         //  Create a new GameState
                            gs.setBoard( board );                   //  Copy the board
                            gs.setCastlingData( whiteKingsideLiberty, whiteQueensideLiberty,
                                                blackKingsideLiberty, blackQueensideLiberty,
                                                whiteHasCastled, blackHasCastled );
                            gs.setPreviousMove( previousFrom, previousTo );
                            gs.setWhiteToMove( white );
                            gs.makeMove(potentialmoves[i].from,     //  Apply the candidate move
                                        potentialmoves[i].to,
                                        potentialmoves[i].promo);

                            k = gs.getKingIndex('b');               //  Locate the king on the new board

                            if(k == _NONE)
                              System.out.println("ERROR: the black king is missing from the board!");

                            if(!gs.inCheckBy(k, 'w'))               //  If king not in check,
                              {                                     //  then move is allowed
                                moves[movesCtr] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                                movesCtr++;
                              }
                          }
                      }

                    return moves;
                  }
              }
          }

        return new Move[0];
      }

    public Move[] getPawnCoverage(int index)
      {
        Move[] moves;
        int movesCtr = 0;

        if(isWhite(index))
          {
                                                                    //  You cannot "cover" the king.
            if(!oob(ul(index)) && row(ul(index)) == row(index) + 1 && col(ul(index)) == col(index) - 1 && isWhite(ul(index)) && !isKing(ul(index)))
              movesCtr++;
                                                                    //  You cannot "cover" the king.
            if(!oob(ur(index)) && row(ur(index)) == row(index) + 1 && col(ur(index)) == col(index) + 1 && isWhite(ur(index)) && !isKing(ur(index)))
              movesCtr++;

            if(movesCtr > 0)                                        //  If any moves at all
              {
                moves = new Move[movesCtr];                         //  We've counted, now allocate
                movesCtr = 0;                                       //  Reset

                                                                    //  You cannot "cover" the king.
                if(!oob(ul(index)) && row(ul(index)) == row(index) + 1 && col(ul(index)) == col(index) - 1 && isWhite(ul(index)) && !isKing(ul(index)))
                  {
                    moves[movesCtr] = new Move(index, ul(index), _NO_PROMO);
                    movesCtr++;
                  }
                                                                    //  You cannot "cover" the king.
                if(!oob(ur(index)) && row(ur(index)) == row(index) + 1 && col(ur(index)) == col(index) + 1 && isWhite(ur(index)) && !isKing(ur(index)))
                  {
                    moves[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                    movesCtr++;
                  }

                return moves;
              }
          }
        else
          {
                                                                    //  You cannot "cover" the king.
            if(!oob(dl(index)) && row(dl(index)) == row(index) - 1 && col(dl(index)) == col(index) - 1 && isBlack(dl(index)) && !isKing(dl(index)))
              movesCtr++;
                                                                    //  You cannot "cover" the king.
            if(!oob(dr(index)) && row(dr(index)) == row(index) - 1 && col(dr(index)) == col(index) + 1 && isBlack(dr(index)) && !isKing(dr(index)))
              movesCtr++;

            if(movesCtr > 0)                                        //  If any moves at all
              {
                moves = new Move[movesCtr];                         //  We've counted, now allocate
                movesCtr = 0;                                       //  Reset

                                                                    //  You cannot "cover" the king.
                if(!oob(dl(index)) && row(dl(index)) == row(index) - 1 && col(dl(index)) == col(index) - 1 && isBlack(dl(index)) && !isKing(dl(index)))
                  {
                    moves[movesCtr] = new Move(index, dl(index), _NO_PROMO);
                    movesCtr++;
                  }
                                                                    //  You cannot "cover" the king.
                if(!oob(dr(index)) && row(dr(index)) == row(index) - 1 && col(dr(index)) == col(index) + 1 && isBlack(dr(index)) && !isKing(dr(index)))
                  {
                    moves[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                    movesCtr++;
                  }

                return moves;
              }
          }

        return new Move[0];
      }

    public Move[] getKnightCoverage(int index)
      {
        Move[] moves;
        int movesCtr = 0;
                                                                    //  You cannot "cover" the king
        if(!oob(ul(u(index))) && sameSide(ul(u(index)), index) && !isKing(ul(u(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(ur(u(index))) && sameSide(ur(u(index)), index) && !isKing(ur(u(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(ur(r(index))) && sameSide(ur(r(index)), index) && !isKing(ur(r(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(ul(l(index))) && sameSide(ul(l(index)), index) && !isKing(ul(l(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(dl(d(index))) && sameSide(dl(d(index)), index) && !isKing(dl(d(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(dr(d(index))) && sameSide(dr(d(index)), index) && !isKing(dr(d(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(dr(r(index))) && sameSide(dr(r(index)), index) && !isKing(dr(r(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(dl(l(index))) && sameSide(dl(l(index)), index) && !isKing(dl(l(index))))
          movesCtr++;

        if(movesCtr > 0)                                            //  Any moves at all
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset
                                                                    //  You cannot "cover" the king
            if(!oob(ul(u(index))) && sameSide(ul(u(index)), index) && !isKing(ul(u(index))))
              {
                moves[movesCtr] = new Move(index, ul(u(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(ur(u(index))) && sameSide(ur(u(index)), index) && !isKing(ur(u(index))))
              {
                moves[movesCtr] = new Move(index, ur(u(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(ur(r(index))) && sameSide(ur(r(index)), index) && !isKing(ur(r(index))))
              {
                moves[movesCtr] = new Move(index, ur(r(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(ul(l(index))) && sameSide(ul(l(index)), index) && !isKing(ul(l(index))))
              {
                moves[movesCtr] = new Move(index, ul(l(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(dl(d(index))) && sameSide(dl(d(index)), index) && !isKing(dl(d(index))))
              {
                moves[movesCtr] = new Move(index, dl(d(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(dr(d(index))) && sameSide(dr(d(index)), index) && !isKing(dr(d(index))))
              {
                moves[movesCtr] = new Move(index, dr(d(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(dr(r(index))) && sameSide(dr(r(index)), index) && !isKing(dr(r(index))))
              {
                moves[movesCtr] = new Move(index, dr(r(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(dl(l(index))) && sameSide(dl(l(index)), index) && !isKing(dl(l(index))))
              {
                moves[movesCtr] = new Move(index, dl(l(index)), _NO_PROMO);
                movesCtr++;
              }
            return moves;
          }

        return new Move[0];
      }

    public Move[] getChampionCoverage(int index)
      {
        Move[] moves;
        int movesCtr = 0;
                                                                    //  You cannot "cover" the king
        if(!oob(u(index)) && sameSide(u(index), index) && !isKing(u(index)))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(u(u(index))) && sameSide(u(u(index)), index) && !isKing(u(u(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(d(index)) && sameSide(d(index), index) && !isKing(d(index)))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(d(d(index))) && sameSide(d(d(index)), index) && !isKing(d(d(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(l(index)) && sameSide(l(index), index) && !isKing(l(index)))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(l(l(index))) && sameSide(l(l(index)), index) && !isKing(l(l(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(r(index)) && sameSide(r(index), index) && !isKing(r(index)))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(r(r(index))) && sameSide(r(r(index)), index) && !isKing(r(r(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(ul(ul(index))) && sameSide(ul(ul(index)), index) && !isKing(ul(ul(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(ur(ur(index))) && sameSide(ur(ur(index)), index) && !isKing(ur(ur(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(dr(dr(index))) && sameSide(dr(dr(index)), index) && !isKing(dr(dr(index))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(dl(dl(index))) && sameSide(dl(dl(index)), index) && !isKing(dl(dl(index))))
          movesCtr++;

        if(movesCtr > 0)                                            //  Moves exist
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset
                                                                    //  You cannot "cover" the king
            if(!oob(u(index)) && sameSide(u(index), index) && !isKing(u(index)))
              {
                moves[movesCtr] = new Move(index, u(index), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(u(u(index))) && sameSide(u(u(index)), index) && !isKing(u(u(index))))
              {
                moves[movesCtr] = new Move(index, u(u(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(d(index)) && sameSide(d(index), index) && !isKing(d(index)))
              {
                moves[movesCtr] = new Move(index, d(index), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(d(d(index))) && sameSide(d(d(index)), index) && !isKing(d(d(index))))
              {
                moves[movesCtr] = new Move(index, d(d(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(l(index)) && sameSide(l(index), index) && !isKing(l(index)))
              {
                moves[movesCtr] = new Move(index, l(index), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(l(l(index))) && sameSide(l(l(index)), index) && !isKing(l(l(index))))
              {
                moves[movesCtr] = new Move(index, l(l(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(r(index)) && sameSide(r(index), index) && !isKing(r(index)))
              {
                moves[movesCtr] = new Move(index, r(index), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(r(r(index))) && sameSide(r(r(index)), index) && !isKing(r(r(index))))
              {
                moves[movesCtr] = new Move(index, r(r(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(ul(ul(index))) && sameSide(ul(ul(index)), index) && !isKing(ul(ul(index))))
              {
                moves[movesCtr] = new Move(index, ul(ul(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(ur(ur(index))) && sameSide(ur(ur(index)), index) && !isKing(ur(ur(index))))
              {
                moves[movesCtr] = new Move(index, ur(ur(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(dr(dr(index))) && sameSide(dr(dr(index)), index) && !isKing(dr(dr(index))))
              {
                moves[movesCtr] = new Move(index, dr(dr(index)), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(dl(dl(index))) && sameSide(dl(dl(index)), index) && !isKing(dl(dl(index))))
              {
                moves[movesCtr] = new Move(index, dl(dl(index)), _NO_PROMO);
                movesCtr++;
              }
          }

        return new Move[0];
      }

    public Move[] getWizardCoverage(int index)
      {
        Move[] moves;
        int movesCtr = 0;
                                                                    //  You cannot "cover" the king
        if(!oob(ul(index)) && sameSide(ul(index), index) && !isKing(ul(index)))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(ur(index)) && sameSide(ur(index), index) && !isKing(ur(index)))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(dr(index)) && sameSide(dr(index), index) && !isKing(dr(index)))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(dl(index)) && sameSide(dl(index), index) && !isKing(dl(index)))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(u(u(ul(index)))) && sameSide(u(u(ul(index))), index) && !isKing(u(u(ul(index)))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(u(u(ur(index)))) && sameSide(u(u(ur(index))), index) && !isKing(u(u(ur(index)))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(l(l(ul(index)))) && sameSide(l(l(ul(index))), index) && !isKing(l(l(ul(index)))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(r(r(ur(index)))) && sameSide(r(r(ur(index))), index) && !isKing(r(r(ur(index)))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(r(r(dr(index)))) && sameSide(r(r(dr(index))), index) && !isKing(r(r(dr(index)))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(l(l(dl(index)))) && sameSide(l(l(dl(index))), index) && !isKing(l(l(dl(index)))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(d(d(dr(index)))) && sameSide(d(d(dr(index))), index) && !isKing(d(d(dr(index)))))
          movesCtr++;
                                                                    //  You cannot "cover" the king
        if(!oob(d(d(dl(index)))) && sameSide(d(d(dl(index))), index) && !isKing(d(d(dl(index)))))
          movesCtr++;

        if(movesCtr > 0)                                            //  Moves exist
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset
                                                                    //  You cannot "cover" the king
            if(!oob(ul(index)) && sameSide(ul(index), index) && !isKing(ul(index)))
              {
                moves[movesCtr] = new Move(index, ul(index), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(ur(index)) && sameSide(ur(index), index) && !isKing(ur(index)))
              {
                moves[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(dr(index)) && sameSide(dr(index), index) && !isKing(dr(index)))
              {
                moves[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(dl(index)) && sameSide(dl(index), index) && !isKing(dl(index)))
              {
                moves[movesCtr] = new Move(index, dl(index), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(u(u(ul(index)))) && sameSide(u(u(ul(index))), index) && !isKing(u(u(ul(index)))))
              {
                moves[movesCtr] = new Move(index, u(u(ul(index))), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(u(u(ur(index)))) && sameSide(u(u(ur(index))), index) && !isKing(u(u(ur(index)))))
              {
                moves[movesCtr] = new Move(index, u(u(ur(index))), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(l(l(ul(index)))) && sameSide(l(l(ul(index))), index) && !isKing(l(l(ul(index)))))
              {
                moves[movesCtr] = new Move(index, l(l(ul(index))), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(r(r(ur(index)))) && sameSide(r(r(ur(index))), index) && !isKing(r(r(ur(index)))))
              {
                moves[movesCtr] = new Move(index, r(r(ur(index))), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(d(d(dl(index)))) && sameSide(d(d(dl(index))), index) && !isKing(d(d(dl(index)))))
              {
                moves[movesCtr] = new Move(index, d(d(dl(index))), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(d(d(dr(index)))) && sameSide(d(d(dr(index))), index) && !isKing(d(d(dr(index)))))
              {
                moves[movesCtr] = new Move(index, d(d(dr(index))), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(l(l(dl(index)))) && sameSide(l(l(dl(index))), index) && !isKing(l(l(dl(index)))))
              {
                moves[movesCtr] = new Move(index, l(l(dl(index))), _NO_PROMO);
                movesCtr++;
              }
                                                                    //  You cannot "cover" the king
            if(!oob(r(r(dr(index)))) && sameSide(r(r(dr(index))), index) && !isKing(r(r(dr(index)))))
              {
                moves[movesCtr] = new Move(index, r(r(dr(index))), _NO_PROMO);
                movesCtr++;
              }
            return moves;
          }

        return new Move[0];
      }

    public Move[] getBishopCoverage(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        if(isWhite(index))
          {
            flags[0] = false;
            flags[1] = true;
            flags[2] = false;
            flags[3] = false;
          }
        else
          {
            flags[0] = false;
            flags[1] = false;
            flags[2] = false;
            flags[3] = true;
          }

        tmp = ulSet(index, flags);                                  //  Up-left
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = urSet(index, flags);                                  //  Up-right
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = dlSet(index, flags);                                  //  Down-left
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = drSet(index, flags);                                  //  Down-right
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        if(movesCtr > 0)
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            tmp = ulSet(index, flags);                              //  Up-left
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = urSet(index, flags);                              //  Up-right
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = dlSet(index, flags);                              //  Down-left
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = drSet(index, flags);                              //  Down-right
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }
            return moves;
          }
        return new Move[0];
      }

    public Move[] getRookCoverage(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        if(isWhite(index))
          {
            flags[0] = false;
            flags[1] = true;
            flags[2] = false;
            flags[3] = false;
          }
        else
          {
            flags[0] = false;
            flags[1] = false;
            flags[2] = false;
            flags[3] = true;
          }

        tmp = uSet(index, flags);                                   //  Up
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = dSet(index, flags);                                   //  Down
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = lSet(index, flags);                                   //  Left
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = rSet(index, flags);                                   //  Right
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        if(movesCtr > 0)
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            tmp = uSet(index, flags);                               //  Up
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = dSet(index, flags);                               //  Down
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = lSet(index, flags);                               //  Left
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = rSet(index, flags);                               //  Right
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }
            return moves;
          }
        return new Move[0];
      }

    public Move[] getQueenCoverage(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        if(isWhite(index))
          {
            flags[0] = false;
            flags[1] = true;
            flags[2] = false;
            flags[3] = false;
          }
        else
          {
            flags[0] = false;
            flags[1] = false;
            flags[2] = false;
            flags[3] = true;
          }

        tmp = uSet(index, flags);                                   //  Up
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = urSet(index, flags);                                  //  Up-right
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = rSet(index, flags);                                   //  Right
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = drSet(index, flags);                                  //  Down-right
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = dSet(index, flags);                                   //  Down
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = dlSet(index, flags);                                  //  Down-left
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = lSet(index, flags);                                   //  Left
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        tmp = ulSet(index, flags);                                  //  Up-left
        for(i = 0; i < tmp.length; i++)
          {
            if(sameSide(tmp[i], index) && !isKing(tmp[i]))          //  You cannot "cover" the king
              movesCtr++;
          }

        if(movesCtr > 0)
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            tmp = uSet(index, flags);                               //  Up
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = urSet(index, flags);                              //  Up-right
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = rSet(index, flags);                               //  Right
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = drSet(index, flags);                              //  Downright
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = dSet(index, flags);                               //  Down
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = dlSet(index, flags);                              //  Down-left
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = lSet(index, flags);                               //  Left
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }

            tmp = ulSet(index, flags);                              //  Up-left
            for(i = 0; i < tmp.length; i++)
              {
                if(sameSide(tmp[i], index) && !isKing(tmp[i]))      //  You cannot "cover" the king
                  {
                    moves[movesCtr] = new Move(index, tmp[i], _NO_PROMO);
                    movesCtr++;
                  }
              }
            return moves;
          }
        return new Move[0];
      }

    public Move[] getKingCoverage(int index)
      {
        Move[] moves;
        int movesCtr = 0;

        if(!oob(u(index)) && sameSide(u(index), index))
          movesCtr++;
        if(!oob(ur(index)) && sameSide(ur(index), index))
          movesCtr++;
        if(!oob(r(index)) && sameSide(r(index), index))
          movesCtr++;
        if(!oob(dr(index)) && sameSide(dr(index), index))
          movesCtr++;
        if(!oob(d(index)) && sameSide(d(index), index))
          movesCtr++;
        if(!oob(dl(index)) && sameSide(dl(index), index))
          movesCtr++;
        if(!oob(l(index)) && sameSide(l(index), index))
          movesCtr++;
        if(!oob(ul(index)) && sameSide(ul(index), index))
          movesCtr++;

        if(movesCtr > 0)                                            //  Any moves at all?
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            if(!oob(u(index)) && sameSide(u(index), index))
              {
                moves[movesCtr] = new Move(index, u(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ur(index)) && sameSide(ur(index), index))
              {
                moves[movesCtr] = new Move(index, ur(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(r(index)) && sameSide(r(index), index))
              {
                moves[movesCtr] = new Move(index, r(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dr(index)) && sameSide(dr(index), index))
              {
                moves[movesCtr] = new Move(index, dr(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(d(index)) && sameSide(d(index), index))
              {
                moves[movesCtr] = new Move(index, d(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(dl(index)) && sameSide(dl(index), index))
              {
                moves[movesCtr] = new Move(index, dl(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(l(index)) && sameSide(l(index), index))
              {
                moves[movesCtr] = new Move(index, l(index), _NO_PROMO);
                movesCtr++;
              }
            if(!oob(ul(index)) && sameSide(ul(index), index))
              {
                moves[movesCtr] = new Move(index, ul(index), _NO_PROMO);
                movesCtr++;
              }

            return moves;
          }

        return new Move[0];
      }

    /****************************************************************/
    //  "Scope" is all squares theoretically reachable (allies, enemies, empties)

    public Move[] getScope(char team)
      {
        Move[] moves;
        int movesCtr = 0;
        Move[] potentialmoves;
        int index, i;

        for(index = 0; index < _NONE; index++)
          {
            if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
              {
                potentialmoves = getScopeIndex(index);
                movesCtr += potentialmoves.length;
              }
          }

        if(movesCtr > 0)                                            //  If any moves at all
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            for(index = 0; index < _NONE; index++)
              {
                if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
                  {
                    potentialmoves = getScopeIndex(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }
                  }
              }
            return moves;
          }


        return new Move[0];
      }

    public Move[] getScopeIndex(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        Move[] potentialmoves;
        int i;

        if(!isEmpty(index))
          {
            if(isPawn(index))
              {
                potentialmoves = getPawnTargetsIndex(index);
                if(potentialmoves.length > 0)
                  {
                    moves = new Move[potentialmoves.length];
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else if(isKnight(index))
              {
                potentialmoves = getKnightMoves(index);
                movesCtr += potentialmoves.length;

                potentialmoves = getKnightCoverage(index);
                movesCtr += potentialmoves.length;

                if(movesCtr > 0)                                    //  If any moves at all
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;

                    potentialmoves = getKnightMoves(index);
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;

                    potentialmoves = getKnightCoverage(index);
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else if(isChampion(index))
              {
                potentialmoves = getChampionMoves(index);
                movesCtr += potentialmoves.length;

                potentialmoves = getChampionCoverage(index);
                movesCtr += potentialmoves.length;

                if(movesCtr > 0)                                    //  If any moves at all
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;

                    potentialmoves = getChampionMoves(index);
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;

                    potentialmoves = getChampionCoverage(index);
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else if(isWizard(index))
              {
                potentialmoves = getWizardMoves(index);
                movesCtr += potentialmoves.length;

                potentialmoves = getWizardCoverage(index);
                movesCtr += potentialmoves.length;

                if(movesCtr > 0)                                    //  If any moves at all
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;

                    potentialmoves = getWizardMoves(index);
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;

                    potentialmoves = getWizardCoverage(index);
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else if(isBishop(index))
              {
                potentialmoves = getBishopScope(index);
                if(potentialmoves.length > 0)
                  {
                    moves = new Move[potentialmoves.length];
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else if(isRook(index))
              {
                potentialmoves = getRookScope(index);
                if(potentialmoves.length > 0)
                  {
                    moves = new Move[potentialmoves.length];
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else if(isQueen(index))
              {
                potentialmoves = getQueenScope(index);
                if(potentialmoves.length > 0)
                  {
                    moves = new Move[potentialmoves.length];
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else
              {
                potentialmoves = getKingNonCastle(index);
                movesCtr += potentialmoves.length;

                potentialmoves = getKingCoverage(index);
                movesCtr += potentialmoves.length;

                if(movesCtr > 0)                                    //  If any moves at all
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;

                    potentialmoves = getKingNonCastle(index);
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;

                    potentialmoves = getKingCoverage(index);
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
          }

        return new Move[0];
      }

    public Move[] getBishopScope(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        flags[0] = false;
        flags[1] = true;
        flags[2] = false;
        flags[3] = true;

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

    public Move[] getRookScope(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        flags[0] = false;
        flags[1] = true;
        flags[2] = false;
        flags[3] = true;

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

    public Move[] getQueenScope(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        flags[0] = false;
        flags[1] = true;
        flags[2] = false;
        flags[3] = true;

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

    /****************************************************************/
    //  "X-Rays" are enemy squares attackable if you could go through occupied squares

    public Move[] getXRayAttacks(char team)
      {
        Move[] moves;
        int movesCtr = 0;
        Move[] potentialmoves;
        int index, i;

        for(index = 0; index < _NONE; index++)
          {
            if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
              {
                potentialmoves = getXRayAttacksIndex(index);
                movesCtr += potentialmoves.length;
              }
          }

        if(movesCtr > 0)                                            //  Any moves at all
          {
            moves = new Move[movesCtr];                             //  We've counted, now allocate
            movesCtr = 0;                                           //  Reset

            for(index = 0; index < _NONE; index++)
              {
                if((team == 'w' && isWhite(index)) || (team == 'b' && isBlack(index)))
                  {
                    potentialmoves = getXRayAttacksIndex(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }
                  }
              }
            return moves;
          }

        return new Move[0];
      }

    public Move[] getXRayAttacksIndex(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        Move[] potentialmoves;
        int i;

        if(!isEmpty(index))
          {
            if(isPawn(index))
              {
                potentialmoves = getPawnTargetsIndex(index);
                if(potentialmoves.length > 0)
                  {
                    moves = new Move[potentialmoves.length];
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else if(isKnight(index))
              {
                potentialmoves = getKnightMoves(index);
                movesCtr += potentialmoves.length;
                potentialmoves = getKnightCoverage(index);
                movesCtr += potentialmoves.length;

                if(movesCtr > 0)                                    //  Any moves at all
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;                                   //  Reset

                    potentialmoves = getKnightMoves(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }

                    potentialmoves = getKnightCoverage(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }

                    return moves;
                  }
              }
            else if(isChampion(index))
              {
                potentialmoves = getChampionMoves(index);
                movesCtr += potentialmoves.length;
                potentialmoves = getChampionCoverage(index);
                movesCtr += potentialmoves.length;

                if(movesCtr > 0)                                    //  Any moves at all
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;                                   //  Reset

                    potentialmoves = getChampionMoves(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }

                    potentialmoves = getChampionCoverage(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }

                    return moves;
                  }
              }
            else if(isWizard(index))
              {
                potentialmoves = getWizardMoves(index);
                movesCtr += potentialmoves.length;
                potentialmoves = getWizardCoverage(index);
                movesCtr += potentialmoves.length;

                if(movesCtr > 0)                                    //  Any moves at all
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;                                   //  Reset

                    potentialmoves = getWizardMoves(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }

                    potentialmoves = getWizardCoverage(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }

                    return moves;
                  }
              }
            else if(isBishop(index))
              {
                potentialmoves = getBishopXRayAttacks(index);
                if(potentialmoves.length > 0)
                  {
                    moves = new Move[potentialmoves.length];
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else if(isRook(index))
              {
                potentialmoves = getRookXRayAttacks(index);
                if(potentialmoves.length > 0)
                  {
                    moves = new Move[potentialmoves.length];
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else if(isQueen(index))
              {
                potentialmoves = getQueenXRayAttacks(index);
                if(potentialmoves.length > 0)
                  {
                    moves = new Move[potentialmoves.length];
                    for(i = 0; i < potentialmoves.length; i++)
                      moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                    movesCtr += potentialmoves.length;
                    return moves;
                  }
              }
            else
              {
                potentialmoves = getKingNonCastle(index);
                movesCtr += potentialmoves.length;
                potentialmoves = getKingCoverage(index);
                movesCtr += potentialmoves.length;

                if(movesCtr > 0)                                    //  Any moves at all
                  {
                    moves = new Move[movesCtr];                     //  We've counted, now allocate
                    movesCtr = 0;                                   //  Reset

                    potentialmoves = getKingNonCastle(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }

                    potentialmoves = getKingCoverage(index);
                    if(potentialmoves.length > 0)
                      {
                        for(i = 0; i < potentialmoves.length; i++)
                          moves[movesCtr + i] = new Move(potentialmoves[i].from, potentialmoves[i].to, potentialmoves[i].promo);
                        movesCtr += potentialmoves.length;
                      }
                    return moves;
                  }
              }
          }

        return new Move[0];
      }

    public Move[] getBishopXRayAttacks(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        if(isWhite(index))
          {
            flags[0] = true;
            flags[1] = false;
            flags[2] = false;
            flags[3] = true;
          }
        else
          {
            flags[0] = false;
            flags[1] = true;
            flags[2] = true;
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

    public Move[] getRookXRayAttacks(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        if(isWhite(index))
          {
            flags[0] = true;
            flags[1] = false;
            flags[2] = false;
            flags[3] = true;
          }
        else
          {
            flags[0] = false;
            flags[1] = true;
            flags[2] = true;
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

    public Move[] getQueenXRayAttacks(int index)
      {
        Move[] moves;
        int movesCtr = 0;
        int[] tmp;
        int i;
        boolean[] flags = new boolean[4];                           //  [pass through white, stop and include white, pass through black, stop and include black]

        if(isWhite(index))
          {
            flags[0] = true;
            flags[1] = false;
            flags[2] = false;
            flags[3] = true;
          }
        else
          {
            flags[0] = false;
            flags[1] = true;
            flags[2] = true;
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

    /****************************************************************/
    //  End-state testing

    /*  Given a board array, return
         0 if the state is not a win for either player
         1 if the state is a win for White
         2 if the state is a win for Black
         3 if the state is a stalemate */
    public int isWin()
      {
        int i;
        int kpos = 0;
        int wMatNonK = 0, bMatNonK = 0;                             //  Counts of pieces other than Kings
        Move[] moves;

        if(white)                                                   //  Get moves for side to move
          moves = getMoves('w');
        else
          moves = getMoves('b');

        for(i = 0; i < _NONE; i++)                                  //  Count up all pieces
          {                                                         //  that are not a King
            if(!isEmpty(i) && !isKing(i))
              {
                if(isWhite(i))
                  wMatNonK++;
                else
                  bMatNonK++;
              }
          }

        if(moves.length == 0)                                       //  Game is over if side to move cannot move
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
        else if(wMatNonK == 0 && bMatNonK == 0)                     //  Game is over if only Kings remain
          return GAME_OVER_STALEMATE;

        return GAME_ONGOING;
      }

    public boolean terminal()
      {
        int win;

        win = isWin();

        return (win != GAME_ONGOING);
      }

    /****************************************************************/
    //  Identities and tests

    /*  Is the given index i vacant? */
    public boolean isEmpty(int i)
      {
        return (board[i] == 'e');
      }

    /*  Is the given index i occupied by a Black piece? */
    public boolean isBlack(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] != 'e' && board[i] == Character.toLowerCase(board[i]));
        return false;
      }

    /*  Is the given index i occupied by a White piece? */
    public boolean isWhite(int i)
      {
        if(i >= 0 && i < _NONE)
          return (board[i] != 'e' && board[i] == Character.toUpperCase(board[i]));
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

    /* Return a character indicating which team 'index' belongs to. */
    public char getTeam(int index)
      {
        if(isWhite(index))
          return 'w';
        if(isBlack(index))
          return 'b';
        return 'e';
      }

    /*  Is the given index i occupied by a Pawn? */
    public boolean isPawn(int i)
      {
        if(i < _NONE)
          return (board[i] == 'P' || board[i] == 'p');
        return false;
      }

    /*  Is the given index i occupied by a Knight? */
    public boolean isKnight(int i)
      {
        if(i < _NONE)
          return (board[i] == 'N' || board[i] == 'n');
        return false;
      }

    /*  Is the given index i occupied by a Champion? */
    public boolean isChampion(int i)
      {
        if(i < _NONE)
          return (board[i] == 'C' || board[i] == 'c');
        return false;
      }

    /*  Is the given index i occupied by a Wizard? */
    public boolean isWizard(int i)
      {
        if(i < _NONE)
          return (board[i] == 'W' || board[i] == 'w');
        return false;
      }

    /*  Is the given index i occupied by a Bishop? */
    public boolean isBishop(int i)
      {
        if(i < _NONE)
          return (board[i] == 'I' || board[i] == 'i');
        return false;
      }

    /*  Is the given index i occupied by a Rook? */
    public boolean isRook(int i)
      {
        if(i < _NONE)
          return (board[i] == 'R' || board[i] == 'r');
        return false;
      }

    /*  Is the given index i occupied by a Queen? */
    public boolean isQueen(int i)
      {
        if(i < _NONE)
          return (board[i] == 'Q' || board[i] == 'q');
        return false;
      }

    /*  Is the given index i occupied by an King? */
    public boolean isKing(int i)
      {
        if(i < _NONE)
          return (board[i] == 'K' || board[i] == 'k');
        return false;
      }

    /* An open file is defined as a column with no pawns on it */
    public boolean isOpenFile(int index)
      {
        int c = 0;

        if(col(index) > 0 && col(index) < 11)
          {
            int column[] = getCol(index);

            while(c < 10)
              {
                if(isPawn(column[c]))
                  return false;
                c++;
              }

            return true;
          }

        return false;
      }

    /* A semi-open file is defined as a column with only pawns of one team on it */
    public boolean isSemiOpenFile(int index)
      {
        int c = 0;
        boolean wPawnFound = false;
        boolean bPawnFound = false;

        if(col(index) > 0 && col(index) < 11)
          {
            int column[] = getCol(index);

            while(c < 10)
              {
                if(isPawn(column[c]))
                  {
                    if(isWhite(column[c]))
                      wPawnFound = true;
                    else
                      bPawnFound = true;
                  }
                c++;
              }

            return ((wPawnFound && !bPawnFound) || (!wPawnFound && bPawnFound));
          }

        return false;
      }

    /* Return array of indices of all white pieces */
    public int[] getWhite()
      {
        int len = 0;
        int i;

        for(i = 0; i < _NONE; i++)
          {
            if(isWhite(i))
              len++;
          }

        if(len > 0)                                                 //  If any White pieces
          {
                                                                    //  We've counted, now allocate
            int[] indices = new int[len];

            len = 0;                                                //  Reset

            for(i = 0; i < _NONE; i++)
              {
                if(isWhite(i))
                  {
                    indices[len] = i;
                    len++;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /* Return array of indices of all black pieces */
    public int[] getBlack()
      {
        int len = 0;
        int i;

        for(i = 0; i < _NONE; i++)
          {
            if(isBlack(i))
              len++;
          }

        if(len > 0)                                                 //  If any Black pieces
          {
                                                                    //  We've counted, now allocate
            int[] indices = new int[len];

            len = 0;                                                //  Reset

            for(i = 0; i < _NONE; i++)
              {
                if(isBlack(i))
                  {
                    indices[len] = i;
                    len++;
                  }
              }

            return indices;
          }

        return new int[0];
      }

    /* Return array of indices of all pawns belonging to indicated team. */
    public int[] getPawns(char team)
      {
        int len = 0;
        int i;

        for(i = 0; i < _NONE; i++)
          {
            if(team == 'w')
              {
                if(isWhite(i) && isPawn(i))
                  len++;
              }
            else
              {
                if(isBlack(i) && isPawn(i))
                  len++;
              }
          }

        if(len > 0)                                                 //  If any pawns at all
          {
            int[] indices = new int[len];                           //  We've counted, now allocate

            len = 0;                                                //  Reset

            for(i = 0; i < _NONE; i++)
              {
                if(team == 'w')
                  {
                    if(isWhite(i) && isPawn(i))
                      {
                        indices[len] = i;
                        len++;
                      }
                  }
                else
                  {
                    if(isBlack(i) && isPawn(i))
                      {
                        indices[len] = i;
                        len++;
                      }
                  }
              }

            return indices;
          }

        return new int[0];
      }

    public boolean isCastle(int from, int to)
      {
        if(isWhiteKingside(from, to) || isBlackKingside(from, to) || isWhiteQueenside(from, to) || isBlackQueenside(from, to))
          return true;
        return false;
      }

    public boolean isWhiteKingside(int from, int to)
      {
        if(isWhite(from) && isKing(from) && from == 18 && to == 20 && whiteKingsideLiberty)
          return true;
        return false;
      }

    public boolean isWhiteQueenside(int from, int to)
      {
        if(isWhite(from) && isKing(from) && from == 18 && to == 16 && whiteQueensideLiberty)
          return true;
        return false;
      }

    public boolean isBlackKingside(int from, int to)
      {
        if(isBlack(from) && isKing(from) && from == 126 && to == 128 && blackKingsideLiberty)
          return true;
        return false;
      }

    public boolean isBlackQueenside(int from, int to)
      {
        if(isBlack(from) && isKing(from) && from == 126 && to == 124 && blackQueensideLiberty)
          return true;
        return false;
      }

    public int[] darkSquares()
      {
        int[] sq = new int[51];

        sq[0] = 11;
        sq[1] = 14;   sq[1] = 16;   sq[2] = 18;   sq[3] = 20;   sq[4] = 22;
        sq[5] = 25;   sq[6] = 27;   sq[7] = 29;   sq[8] = 31;   sq[9] = 33;
        sq[10] = 38;  sq[11] = 40;  sq[12] = 42;  sq[13] = 44;  sq[14] = 46;
        sq[15] = 49;  sq[16] = 51;  sq[17] = 53;  sq[18] = 55;  sq[19] = 57;
        sq[20] = 62;  sq[21] = 64;  sq[22] = 66;  sq[23] = 68;  sq[24] = 70;
        sq[25] = 73;  sq[26] = 75;  sq[27] = 77;  sq[28] = 79;  sq[29] = 81;
        sq[30] = 86;  sq[31] = 88;  sq[32] = 90;  sq[33] = 92;  sq[34] = 94;
        sq[35] = 97;  sq[36] = 99;  sq[37] = 101; sq[38] = 103; sq[39] = 105;
        sq[40] = 110; sq[41] = 112; sq[42] = 114; sq[43] = 116; sq[44] = 118;
        sq[45] = 121; sq[46] = 123; sq[47] = 125; sq[48] = 127; sq[49] = 129;
        sq[50] = 132;

        return sq;
      }

    public int[] lightSquares()
      {
        int[] sq = new int[51];

        sq[0] = 0;
        sq[1] = 13;   sq[1] = 15;   sq[2] = 17;   sq[3] = 19;   sq[4] = 21;
        sq[5] = 26;   sq[6] = 28;   sq[7] = 30;   sq[8] = 32;   sq[9] = 34;
        sq[10] = 37;  sq[11] = 39;  sq[12] = 41;  sq[13] = 43;  sq[14] = 45;
        sq[15] = 50;  sq[16] = 52;  sq[17] = 54;  sq[18] = 56;  sq[19] = 58;
        sq[20] = 61;  sq[21] = 63;  sq[22] = 65;  sq[23] = 67;  sq[24] = 69;
        sq[25] = 74;  sq[26] = 76;  sq[27] = 78;  sq[28] = 80;  sq[29] = 82;
        sq[30] = 85;  sq[31] = 87;  sq[32] = 89;  sq[33] = 91;  sq[34] = 93;
        sq[35] = 98;  sq[36] = 100; sq[37] = 102; sq[38] = 104; sq[39] = 106;
        sq[40] = 109; sq[41] = 111; sq[42] = 113; sq[43] = 115; sq[44] = 117;
        sq[45] = 122; sq[46] = 124; sq[47] = 126; sq[48] = 128; sq[49] = 130;
        sq[50] = 143;

        return sq;
      }

    /****************************************************************/
    //  Set builders

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

    /****************************************************************/
    //  Board logic

    /*  Return the index UP from the given i */
    public int u(int i)
      {
        if(!oob(i))
          {
            if(row(i + 12) == row(i) + 1)
              return i + 12;
          }
        return _NONE;
      }

    /*  Return the index DOWN from the given i */
    public int d(int i)
      {
        if(!oob(i))
          {
            if(row(i - 12) == row(i) - 1 && row(i) != _NONE)
              return i - 12;
          }
        return _NONE;
      }

    /*  Return the index LEFT from the given i */
    public int l(int i)
      {
        if(!oob(i))
          {
            if(row(i - 1) == row(i))
              return i - 1;
          }
        return _NONE;
      }

    /*  Return the index RIGHT from the given i */
    public int r(int i)
      {
        if(!oob(i))
          {
            if(row(i + 1) == row(i))
              return i + 1;
          }
        return _NONE;
      }

    /*  Return the index UP-LEFT from the given i */
    public int ul(int i)
      {
        if(!oob(i))
          {
            if(row(i + 11) == row(i) + 1)
              return i + 11;
          }
        return _NONE;
      }

    /*  Return the index UP-RIGHT from the given i */
    public int ur(int i)
      {
        if(!oob(i))
          {
            if(row(i + 13) == row(i) + 1)
              return i + 13;
          }
        return _NONE;
      }

    /*  Return the index DOWN-LEFT from the given i */
    public int dl(int i)
      {
        if(!oob(i))
          {
            if(row(i - 13) == row(i) - 1 && row(i) != _NONE)
              return i - 13;
          }
        return _NONE;
      }

    /*  Return the index DOWN-RIGHT from the given i */
    public int dr(int i)
      {
        if(!oob(i))
          {
            if(row(i - 11) == row(i) - 1 && row(i) != _NONE)
              return i - 11;
          }
        return _NONE;
      }

    /*  Compute the COLUMN in which given index is included */
    public int col(int i)
      {
        if(i < _NONE && i >= 0)
          return i % 12;
        return _NONE;
      }

    /*  Compute the ROW in which given index is included */
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

    /* No square roots, no lookup tables: on a square game board,
       when it is possible to move diagonally, two pieces are always
       only as far apart as the greater of rise and run! */
    public int distance(int a, int b)
      {
        int ra = row(a);
        int ca = col(a);

        int rb = row(b);
        int cb = col(b);

        int difrow;
        int difcol;

        if(ra < _NONE && ca < _NONE && rb < _NONE && cb < _NONE)
          {
            difrow = Math.abs(ra - rb);
            difcol = Math.abs(ca - cb);
            return (difrow > difcol) ? difrow : difcol;
          }
        return 0;
      }

    /****************************************************************/
    //  Notation

    /*  Forsyth-Edwards Notation (FEN)
        wW0W1CRNIQKINRC11PPPPPPPPPP1BBBBBB1pppppppppp11crniqkinrc1w0w_KQkq00_x_x
        Build and return a string representation of the board. */
    public String FEN()
      {
        StringBuilder sb;
        int x, y;
        int emptyCtr = 0;
        boolean emptyFound = false;

        sb = new StringBuilder();

        if(white)
          sb.append('w');
        else
          sb.append('b');

        for(y = 0; y < 12; y++)
          {
            for(x = 0; x < 12; x++)
              {
                if(isEmpty(y * 12 + x))
                  {
                    if(!emptyFound)
                      emptyFound = true;
                    emptyCtr++;
                  }
                else
                  {
                    if(emptyFound)
                      {
                        emptyFound = false;
                        if(emptyCtr > 0)
                          {
                                                                    //  Add digit to string
                            if(emptyCtr == 12)
                              sb.append('B');
                            else if(emptyCtr == 11)
                              sb.append('A');
                            else if(emptyCtr == 10)
                              sb.append('0');
                            else
                              sb.append( (char)(emptyCtr + (int)'0') );
                            emptyCtr = 0;
                          }
                        sb.append(board[y * 12 + x]);               //  Add piece to string
                      }
                    else
                      sb.append(board[y * 12 + x]);                 //  Add piece to string
                  }
              }
            if(emptyFound)
              {
                emptyFound = false;
                if(emptyCtr > 0)
                  {
                                                                    //  Add digit to string
                    if(emptyCtr == 12)
                      sb.append('B');
                    else if(emptyCtr == 11)
                      sb.append('A');
                    else if(emptyCtr == 10)
                      sb.append('0');
                    else
                      sb.append( (char)(emptyCtr + (int)'0') );
                    emptyCtr = 0;
                  }
              }
          }

        sb.append('_');
        if(whiteKingsideLiberty)
          sb.append('K');
        if(whiteQueensideLiberty)
          sb.append('Q');
        if(blackKingsideLiberty)
          sb.append('k');
        if(blackQueensideLiberty)
          sb.append('q');

        if(whiteHasCastled)
          sb.append('1');
        else
          sb.append('0');

        if(blackHasCastled)
          sb.append('1');
        else
          sb.append('0');

        sb.append('_');
        if(previousFrom < _NONE)
          sb.append( String.valueOf(previousFrom) );
        else
          sb.append('x');

        sb.append('_');
        if(previousTo < _NONE)
          sb.append( String.valueOf(previousTo) );
        else
          sb.append('x');

        return sb.toString();
      }

    /* wW0W1CRNIQKINRC11PPPPPPPPPP1BBBBBB1pppppppppp11crniqkinrc1w0w_KQkq00_x_x */
    public void readFEN(String fen)                                 //  Build GameState from string
      {
        int i, j, k = 0, l;
        boolean encounteredBooleanDigit = false;
        String parts[] = fen.split("_");

        whiteKingsideLiberty = false;                               //  Guilty until proven innocent
        whiteQueensideLiberty = false;
        blackKingsideLiberty = false;
        blackQueensideLiberty = false;
        whiteHasCastled = false;
        blackHasCastled = false;
        previousFrom = _NONE;
        previousTo = _NONE;

        if(parts[0].charAt(0) == 'w')
          white = true;
        else
          white = false;

        for(i = 1; i < parts[0].length(); i++)
          {
            if(Character.isDigit(parts[0].charAt(i)))               //  Found a digit
              {
                if(parts[0].charAt(i) == '0')                       //  Treat '0' as 10.
                  l = 10;
                else
                  l = Integer.parseInt("" + parts[0].charAt(i));

                for(j = 0; j < l; j++)
                  {
                    board[k] = 'e';
                    k++;
                  }
              }
            else if(parts[0].charAt(i) == 'A')                      //  Treat 'A' as 11.
              {
                for(j = 0; j < 11; j++)
                  {
                    board[k] = 'e';
                    k++;
                  }
              }
            else if(parts[0].charAt(i) == 'B')                      //  Treat 'B' as 12.
              {
                for(j = 0; j < 12; j++)
                  {
                    board[k] = 'e';
                    k++;
                  }
              }
            else                                                    //  Found a char
              {
                board[k] = parts[0].charAt(i);
                k++;
              }
          }

        for(i = 0; i < parts[1].length(); i++)
          {
            if(parts[1].charAt(i) == 'K')
              whiteKingsideLiberty = true;
            else if(parts[1].charAt(i) == 'Q')
              whiteQueensideLiberty = true;
            else if(parts[1].charAt(i) == 'k')
              blackKingsideLiberty = true;
            else if(parts[1].charAt(i) == 'q')
              blackQueensideLiberty = true;
            else if(Character.isDigit(parts[1].charAt(i)))
              {
                if(!encounteredBooleanDigit)                        //  NOT yet encountered a boolean digit: we are reading for white
                  {
                    if(parts[1].charAt(i) == '0')
                      whiteHasCastled = false;
                    else if(parts[1].charAt(i) == '1')
                      whiteHasCastled = true;

                    encounteredBooleanDigit = true;
                  }
                else                                                //  HAVE encountered a boolean digit: we are reading for black
                  {
                    if(parts[1].charAt(i) == '0')
                      blackHasCastled = false;
                    else if(parts[1].charAt(i) == '1')
                      blackHasCastled = true;
                  }
              }
          }

        if(parts[2].charAt(0) != 'x')
          previousFrom = Integer.parseInt(parts[2]);

        if(parts[3].charAt(0) != 'x')
          previousFrom = Integer.parseInt(parts[3]);

        return;
      }

    /****************************************************************/
    //  Display
    public void draw()
      {
        int c = 143;
        int x, y;

        for(y = 0; y < 12; y++)
          {
            for(x = 11; x >= 0; x--)
              {
                if(oob(c - x))
                  System.out.print("   ");
                else
                  {
                    if(isEmpty(c - x))
                      System.out.print(" . ");
                    else
                      {
                        if(isBishop(c - x))
                          {
                            if(isWhite(c - x))
                              System.out.print(" B ");
                            else
                              System.out.print(" b ");
                          }
                        else
                          System.out.print(" " + board[c - x] + " ");
                      }
                  }
              }
            System.out.println();
            c -= 12;
          }
        return;
      }

    public String algebraic(int index)
      {
        StringBuilder sb;
        int c, r;

        sb = new StringBuilder();
        c = col(index);
        r = row(index);

        sb.append( (char)(c + (int)'A') );
        sb.append( String.valueOf(r) );

        return sb.toString();
      }

    public String label()                                           //  Output the game label
      {
        return "omegachess";
      }
  }
