const GAME_ONGOING = 0;                                             //  (See Java code).
const GAME_OVER_WHITE_WINS = 1;                                     //  (See Java code).
const GAME_OVER_BLACK_WINS = 2;                                     //  (See Java code).
const GAME_OVER_STALEMATE = 3;                                      //  (See Java code).

function animate()
  {
    animating = true;                                               //  Set the flag: animation is in progress.

    if(animationInstruction != null)
      {
        switch(animationInstruction.action)
          {
            case 'move':         Select_A = animationInstruction.a;
                                 Select_B = animationInstruction.b;
                                 commit_mp3.play();
                                 if(Object.hasOwn(animationInstruction, 'promo'))
                                   PromotionTarget = animationInstruction.promo;
                                 else
                                   PromotionTarget = _NO_PROMO;
                                 move(animationInstruction.a, animationInstruction.b);
                                 break;
            case 'die':          Select_A = animationInstruction.a;
                                 Select_B = animationInstruction.b;
                                 if(Object.hasOwn(animationInstruction, 'promo'))
                                   PromotionTarget = animationInstruction.promo;
                                 else
                                   PromotionTarget = _NO_PROMO;
                                 commit_mp3.play();
                                 die(animationInstruction.b);
                                 break;
            case 'dieEnPassant': Select_A = animationInstruction.a;
                                 Select_B = animationInstruction.b;
                                 commit_mp3.play();
                                 dieEnPassant(gameEngine.enPassantVictim_client(animationInstruction.a, animationInstruction.b));
                                 break;
            case 'castle':       Select_A = animationInstruction.a;
                                 Select_B = animationInstruction.b;
                                 Castle_C = animationInstruction.c;
                                 Castle_D = animationInstruction.d;
                                 commit_mp3.play();
                                 castle(animationInstruction.a,
                                        animationInstruction.b,
                                        animationInstruction.c,
                                        animationInstruction.d);
                                 break;
            case 'promote':      Select_A = animationInstruction.a;
                                 Select_B = animationInstruction.b;
                                 PromotionTarget = animationInstruction.promo;
                                 promote(animationInstruction.a);
                                 break;
          }
      }
  }

function move(a, b)
  {
    animationTarget = 0;
    while(animationTarget < gamePieces.length && gamePieces[animationTarget].chessposition != a)
      animationTarget++;

    if(animationTarget < gamePieces.length)
      {
        gamePieces[animationTarget].chessposition = b;              //  Update internal position.

        var start_x = convIndexToX(a);
        var start_y = convIndexToY(a);

        var end_x = convIndexToX(b);
        var end_y = convIndexToY(b);

        var mid_x = midpoint(start_x, end_x);
        var mid_y = midpoint(start_y, end_y);

        animate_startPos = {x: start_x, y: start_y, z:gamePieces[animationTarget].ownZ};
        animate_midPos   = {x: mid_x,   y: mid_y,   z:GAMEPIECE_MOVEMENT_ZENITH};
        animate_endPos   = {x: end_x,   y: end_y,   z:gamePieces[animationTarget].ownZ};

        var tweenHead = new TWEEN.Tween(animate_startPos).to(animate_midPos, 600);
        tweenHead.easing(TWEEN.Easing.Cubic.Out);
        tweenHead.onUpdate(function()
          {
            gamePieces[animationTarget].position.x = animate_startPos.x;
            gamePieces[animationTarget].position.y = animate_startPos.y;
            gamePieces[animationTarget].position.z = animate_startPos.z;
          });

        var tweenTail = new TWEEN.Tween(animate_midPos).to(animate_endPos, 600);
        tweenTail.easing(TWEEN.Easing.Cubic.In);
        tweenTail.onUpdate(function()
          {
            gamePieces[animationTarget].position.x = animate_midPos.x;
            gamePieces[animationTarget].position.y = animate_midPos.y;
            gamePieces[animationTarget].position.z = animate_midPos.z;
          });
        tweenTail.onComplete(function()
          {                                                         //  Test for promotion.
            if(pagan.team == 'Black')
              {
                                                                    //  Test whether the DESTINATION is black's home row AND
                                                                    //  whether the SOURCE is a white pawn (because we have not yet updated the game state.)
                if(gameEngine.row_client(Select_B) == 10 && gameEngine.isWhite_client(Select_A) && gameEngine.isPawn_client(Select_A))
                  addHUD();                                         //  Human, as White, to promote.
                                                                    //  THERFORE **DO NOT** UPDATE THE GAME-ENGINE YET!!!

                                                                    //  Test whether the DESTINATION is white's home row AND
                                                                    //  whether the SOURCE is a black pawn (because we have not yet updated the game state.)
                else if(gameEngine.row_client(Select_B) == 1 && gameEngine.isBlack_client(Select_A) && gameEngine.isPawn_client(Select_A))
                  promoteAI(Select_A, Select_B, PromotionTarget);   //  A.I., as Black, to promote.
                                                                    //  The A.I. promotion routine updates the game state at the end of promotion.
                else
                  {
                    pagan.commitRealMove(a, b, _NO_PROMO);          //  Update the game state (and OBSERVE the update).
                    swapTurns();                                    //  Nobody's promoting now, swap turns.
                  }
              }
            else
              {
                                                                    //  Test whether the DESTINATION is black's home row AND
                                                                    //  whether the SOURCE is a white pawn (because we have not yet updated the game state.)
                if(gameEngine.row_client(Select_B) == 10 && gameEngine.isWhite_client(Select_A) && gameEngine.isPawn_client(Select_A))
                  promoteAI(Select_A, Select_B, PromotionTarget);   //  A.I., as White, to promote.
                                                                    //  The A.I. promotion routine updates the game state at the end of promotion.

                                                                    //  Test whether the DESTINATION is white's home row AND
                                                                    //  whether the SOURCE is a black pawn (because we have not yet updated the game state.)
                else if(gameEngine.row_client(Select_B) == 1 && gameEngine.isBlack_client(Select_A) && gameEngine.isPawn_client(Select_A))
                  addHUD();                                         //  Human, as Black, to promote.
                                                                    //  THERFORE **DO NOT** UPDATE THE GAME-ENGINE YET!!!
                else
                  {
                    pagan.commitRealMove(a, b, _NO_PROMO);          //  Update the game state (and OBSERVE the update).
                    swapTurns();                                    //  Nobody's promoting now, swap turns.
                  }
              }
          });

        tweenHead.chain(tweenTail);
        tweenHead.start();
      }
  }

function die(a)
  {
    animationTarget = 0;
    while(animationTarget < gamePieces.length && gamePieces[animationTarget].chessposition != a)
      animationTarget++;

    if(animationTarget < gamePieces.length)
      {
        gamePieces[animationTarget].chessposition = _NOTHING;       //  Update internal position.

        animate_startScale = {x: gamePieces[animationTarget].scale.x,
                              y: gamePieces[animationTarget].scale.y,
                              z: gamePieces[animationTarget].scale.z};
        animate_endScale   = {x: 1, y: 1, z: 1};
        var tween = new TWEEN.Tween(animate_startScale).to(animate_endScale, 500);
        tween.onUpdate(function()
          {
            gamePieces[animationTarget].scale.x = animate_startScale.x;
            gamePieces[animationTarget].scale.y = animate_startScale.y;
            gamePieces[animationTarget].scale.z = animate_startScale.z;
          });
        tween.onComplete(function()
          {
            removeDeadMesh();
          });
        tween.start();
      }
  }

/* Received index "a" is the index of the CAPTURED PIECE. */
function dieEnPassant(a)
  {
    animationTarget = 0;
    while(animationTarget < gamePieces.length && gamePieces[animationTarget].chessposition != a)
      animationTarget++;

    if(animationTarget < gamePieces.length)
      {
        gamePieces[animationTarget].chessposition = _NOTHING;       //  Update internal position.

        animate_startScale = {x: gamePieces[animationTarget].scale.x,
                              y: gamePieces[animationTarget].scale.y,
                              z: gamePieces[animationTarget].scale.z};
        animate_endScale   = {x: 1, y: 1, z: 1};
        var tween = new TWEEN.Tween(animate_startScale).to(animate_endScale, 500);
        tween.onUpdate(function()
          {
            gamePieces[animationTarget].scale.x = animate_startScale.x;
            gamePieces[animationTarget].scale.y = animate_startScale.y;
            gamePieces[animationTarget].scale.z = animate_startScale.z;
          });
        tween.onComplete(function()
          {
            removeDeadMesh();
          });
        tween.start();
      }
  }

function removeDeadMesh()
  {
    var i;
    var markedForDeath = [];
    for(i = 0; i < gamePieces.length; i++)
      {
        if(gamePieces[i].chessposition == _NOTHING)
          markedForDeath.push(i);
      }
    while(markedForDeath.length > 0)
      {
        scene.remove(gamePieces[markedForDeath[0]]);
        gamePieces.splice(markedForDeath[0], 1);
        markedForDeath.shift();
      }
    move(Select_A, Select_B);
  }

function castle(a, b, c, d)
  {
    Castle_C = c;                                                   //  Save the castling move
    Castle_D = d;

    animationTarget = 0;
    while(animationTarget < gamePieces.length && gamePieces[animationTarget].chessposition != a)
      animationTarget++;

    if(animationTarget < gamePieces.length)
      {
        gamePieces[animationTarget].chessposition = b;              //  Update internal position.

        var start_x = convIndexToX(a);
        var start_y = convIndexToY(a);

        var end_x = convIndexToX(b);
        var end_y = convIndexToY(b);

        var mid_x = midpoint(start_x, end_x);
        var mid_y = midpoint(start_y, end_y);

        animate_startPos = {x: start_x, y: start_y, z:0};
        animate_midPos   = {x: mid_x,   y: mid_y,   z:GAMEPIECE_MOVEMENT_ZENITH};
        animate_endPos   = {x: end_x,   y: end_y,   z:0};

        var tweenHead = new TWEEN.Tween(animate_startPos).to(animate_midPos, 600);
        tweenHead.easing(TWEEN.Easing.Cubic.Out);
        tweenHead.onUpdate(function()
          {
            gamePieces[animationTarget].position.x = animate_startPos.x;
            gamePieces[animationTarget].position.y = animate_startPos.y;
            gamePieces[animationTarget].position.z = animate_startPos.z;
          });

        var tweenTail = new TWEEN.Tween(animate_midPos).to(animate_endPos, 600);
        tweenTail.easing(TWEEN.Easing.Cubic.In);
        tweenTail.onUpdate(function()
          {
            gamePieces[animationTarget].position.x = animate_midPos.x;
            gamePieces[animationTarget].position.y = animate_midPos.y;
            gamePieces[animationTarget].position.z = animate_midPos.z;
          });
        tweenTail.onComplete(function()
          {
            commit_mp3.play();
            completeCastle(Select_A, Select_B, Castle_C, Castle_D)
          });

        tweenHead.chain(tweenTail);
        tweenHead.start();
      }
  }

function completeCastle(a, b, c, d)
  {
    animationTarget = 0;
    while(animationTarget < gamePieces.length && gamePieces[animationTarget].chessposition != c)
      animationTarget++;

    if(animationTarget < gamePieces.length)
      {
        gamePieces[animationTarget].chessposition = d;              //  Update internal position.

        var start_x = convIndexToX(c);
        var start_y = convIndexToY(c);

        var end_x = convIndexToX(d);
        var end_y = convIndexToY(d);

        var mid_x = midpoint(start_x, end_x);
        var mid_y = midpoint(start_y, end_y);

        animate_startPos = {x: start_x, y: start_y, z:0};
        animate_midPos   = {x: mid_x,   y: mid_y,   z:GAMEPIECE_MOVEMENT_ZENITH};
        animate_endPos   = {x: end_x,   y: end_y,   z:0};

        var tweenHead = new TWEEN.Tween(animate_startPos).to(animate_midPos, 600);
        tweenHead.easing(TWEEN.Easing.Cubic.Out);
        tweenHead.onUpdate(function()
          {
            gamePieces[animationTarget].position.x = animate_startPos.x;
            gamePieces[animationTarget].position.y = animate_startPos.y;
            gamePieces[animationTarget].position.z = animate_startPos.z;
          });

        var tweenTail = new TWEEN.Tween(animate_midPos).to(animate_endPos, 600);
        tweenTail.easing(TWEEN.Easing.Cubic.In);
        tweenTail.onUpdate(function()
          {
            gamePieces[animationTarget].position.x = animate_midPos.x;
            gamePieces[animationTarget].position.y = animate_midPos.y;
            gamePieces[animationTarget].position.z = animate_midPos.z;
          });
        tweenTail.onComplete(function()
          {
            gameEngine.makeMove_client(a, b, _NO_PROMO);
            swapTurns();                                            //  Nobody's promoting now, swap turns.
          });

        tweenHead.chain(tweenTail);
        tweenHead.start();
      }
  }

/* The move (or capture) had to be completed before the HUD could pop.
   This means that the MESH INTERNALS have been updated, but the gameEngine has NOT YET BEEN UPDATED! */
function promoteHuman(a, b, p)
  {
    animationTarget = 0;
    while(animationTarget < gamePieces.length && gamePieces[animationTarget].chessposition != b)
      animationTarget++;

    if(animationTarget < gamePieces.length)
      {
        gamePieces[animationTarget].chessposition = _NOTHING;       //  Update internal position.

        animate_startScale = {x: gamePieces[animationTarget].scale.x,
                              y: gamePieces[animationTarget].scale.y,
                              z: gamePieces[animationTarget].scale.z};
        animate_endScale   = {x: 1, y: 1, z: 1};
        var tween = new TWEEN.Tween(animate_startScale).to(animate_endScale, 500);
        tween.onUpdate(function()
          {
            gamePieces[animationTarget].scale.x = animate_startScale.x;
            gamePieces[animationTarget].scale.y = animate_startScale.y;
            gamePieces[animationTarget].scale.z = animate_startScale.z;
          });
        tween.onComplete(function()
          {
            removePromotedPawnHuman(a, b, p);
          });
        tween.start();
      }
  }

function removePromotedPawnHuman(a, b, p)
  {
    var i;
    var markedForDeath = [];
    for(i = 0; i < gamePieces.length; i++)
      {
        if(gamePieces[i].chessposition == _NOTHING)
          markedForDeath.push(i);
      }
    while(markedForDeath.length > 0)
      {
        scene.remove(gamePieces[markedForDeath[0]]);
        gamePieces.splice(markedForDeath[0], 1);
        markedForDeath.shift();
      }

    if(gameEngine.isWhite_client(Select_A))                         //  VALID: gameEngine gets updated AT THE END OF ALL THIS SHIT.
      completePromotionHuman(a, b, p);
    else
      completePromotionHuman(a, b, p);
  }

function completePromotionHuman(a, b, p)
  {
    promote_mp3.play();

    if(pagan.team == 'Black')                                       //  A.I. is black; human is white.
      {
        switch(p)
          {
            case _PROMO_KNIGHT:   gamePieces.push(new THREE.Mesh(knightGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].rotation.y = -90 * (Math.PI/180);
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Knight';
                                  break;
            case _PROMO_CHAMPION: gamePieces.push(new THREE.Mesh(championGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Champion';
                                  break;
            case _PROMO_WIZARD:   gamePieces.push(new THREE.Mesh(wizardGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Wizard';
                                  break;
            case _PROMO_BISHOP:   gamePieces.push(new THREE.Mesh(bishopGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Bishop';
                                  break;
            case _PROMO_ROOK:     gamePieces.push(new THREE.Mesh(rookGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Rook';
                                  break;
            case _PROMO_QUEEN:    gamePieces.push(new THREE.Mesh(queenGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Queen';
                                  break;
          }
      }
    else                                                            //  A.I. is white; human is black.
      {
        switch(p)
          {
            case _PROMO_KNIGHT:   gamePieces.push(new THREE.Mesh(knightGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].rotation.y = 90 * (Math.PI/180);
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Knight';
                                  break;
            case _PROMO_CHAMPION: gamePieces.push(new THREE.Mesh(championGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Champion';
                                  break;
            case _PROMO_WIZARD:   gamePieces.push(new THREE.Mesh(wizardGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].rotation.y = Math.PI;
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Wizard';
                                  break;
            case _PROMO_BISHOP:   gamePieces.push(new THREE.Mesh(bishopGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Bishop';
                                  break;
            case _PROMO_ROOK:     gamePieces.push(new THREE.Mesh(rookGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Rook';
                                  break;
            case _PROMO_QUEEN:    gamePieces.push(new THREE.Mesh(queenGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Queen';
                                  break;
          }
      }

    pagan.commitRealMove(a, b, p);                                  //  Update the game state (and OBSERVE the update).

    if(gamePieces[gamePieces.length - 1].chessrank == 'Champion')
      gamePieces[gamePieces.length - 1].ownZ = 8;
    else if(gamePieces[gamePieces.length - 1].chessrank == 'Wizard')
      gamePieces[gamePieces.length - 1].ownZ = 0;
    else
      gamePieces[gamePieces.length - 1].ownZ = 0;

    gamePieces[gamePieces.length - 1].rotation.x = 90 * (Math.PI/180);
    gamePieces[gamePieces.length - 1].position.x = convIndexToX(b);
    gamePieces[gamePieces.length - 1].position.y = convIndexToY(b);
    gamePieces[gamePieces.length - 1].position.z = gamePieces[gamePieces.length - 1].ownZ;
    gamePieces[gamePieces.length - 1].chessposition = b;

    scene.add(gamePieces[gamePieces.length - 1]);
    animationTarget = gamePieces.length - 1;
    animate_startScale = {x: gamePieces[animationTarget].scale.x,
                          y: gamePieces[animationTarget].scale.y,
                          z: gamePieces[animationTarget].scale.z};
    if(gamePieces[gamePieces.length - 1].chessrank == 'Champion')
      animate_endScale = {x: 13, y: 13, z: 13};
    else if(gamePieces[gamePieces.length - 1].chessrank == 'Wizard')
      animate_endScale = {x: 5, y: 5, z: 5};
    else
      animate_endScale = {x: 25, y: 25, z: 25};

    var tween = new TWEEN.Tween(animate_startScale).to(animate_endScale, 500);
    tween.onUpdate(function()
      {
        gamePieces[animationTarget].scale.x = animate_startScale.x;
        gamePieces[animationTarget].scale.y = animate_startScale.y;
        gamePieces[animationTarget].scale.z = animate_startScale.z;
      });
    tween.onComplete(function()
      {
        swapTurns();
      });
    tween.start();
  }

/* The A.I. needs no HUD, obviously, but this sequence needs to keep in sync with the human-version.
   The MESH INTERNALS have been updated, but the gameEngine has NOT YET BEEN UPDATED! */
function promoteAI(a, b, p)
  {
    animationTarget = 0;
    while(animationTarget < gamePieces.length && gamePieces[animationTarget].chessposition != b)
      animationTarget++;

    if(animationTarget < gamePieces.length)
      {
        gamePieces[animationTarget].chessposition = _NOTHING;       //  Update internal position.

        animate_startScale = {x: gamePieces[animationTarget].scale.x,
                              y: gamePieces[animationTarget].scale.y,
                              z: gamePieces[animationTarget].scale.z};
        animate_endScale   = {x: 1, y: 1, z: 1};
        var tween = new TWEEN.Tween(animate_startScale).to(animate_endScale, 500);
        tween.onUpdate(function()
          {
            gamePieces[animationTarget].scale.x = animate_startScale.x;
            gamePieces[animationTarget].scale.y = animate_startScale.y;
            gamePieces[animationTarget].scale.z = animate_startScale.z;
          });
        tween.onComplete(function()
          {
            removePromotedPawnAI(a, b, p);
          });
        tween.start();
      }
  }

function removePromotedPawnAI(a, b, p)
  {
    var i;
    var markedForDeath = [];
    for(i = 0; i < gamePieces.length; i++)
      {
        if(gamePieces[i].chessposition == _NOTHING)
          markedForDeath.push(i);
      }
    while(markedForDeath.length > 0)
      {
        scene.remove(gamePieces[markedForDeath[0]]);
        gamePieces.splice(markedForDeath[0], 1);
        markedForDeath.shift();
      }

    if(gameEngine.isWhite_client(Select_A))                         //  VALID: gameEngine gets updated AT THE END OF ALL THIS SHIT.
      completePromotionAI(a, b, p);
    else
      completePromotionAI(a, b, p);
  }

function completePromotionAI(a, b, p)
  {
    promote_mp3.play();

    if(pagan.team == 'Black')                                       //  A.I. is black.
      {
        switch(p)
          {
            case _PROMO_KNIGHT:   gamePieces.push(new THREE.Mesh(knightGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].rotation.y = 90 * (Math.PI/180);
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Knight';
                                  break;
            case _PROMO_CHAMPION: gamePieces.push(new THREE.Mesh(championGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Champion';
                                  break;
            case _PROMO_WIZARD:   gamePieces.push(new THREE.Mesh(wizardGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].rotation.y = Math.PI;
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Wizard';
                                  break;
            case _PROMO_BISHOP:   gamePieces.push(new THREE.Mesh(bishopGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Bishop';
                                  break;
            case _PROMO_ROOK:     gamePieces.push(new THREE.Mesh(rookGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Rook';
                                  break;
            case _PROMO_QUEEN:    gamePieces.push(new THREE.Mesh(queenGeometry, blackMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'Black';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Queen';
                                  break;
          }
      }
    else                                                            //  A.I. is white.
      {
        switch(p)
          {
            case _PROMO_KNIGHT:   gamePieces.push(new THREE.Mesh(knightGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].rotation.y = -90 * (Math.PI/180);
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Knight';
                                  break;
            case _PROMO_CHAMPION: gamePieces.push(new THREE.Mesh(championGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Champion';
                                  break;
            case _PROMO_WIZARD:   gamePieces.push(new THREE.Mesh(wizardGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Wizard';
                                  break;
            case _PROMO_BISHOP:   gamePieces.push(new THREE.Mesh(bishopGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Bishop';
                                  break;
            case _PROMO_ROOK:     gamePieces.push(new THREE.Mesh(rookGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Rook';
                                  break;
            case _PROMO_QUEEN:    gamePieces.push(new THREE.Mesh(queenGeometry, whiteMaterial));
                                  gamePieces[gamePieces.length - 1].chessteam = 'White';
                                  gamePieces[gamePieces.length - 1].chessrank = 'Queen';
                                  break;
          }
      }

    pagan.commitRealMove(a, b, p);                                  //  Update the game state (and OBSERVE the update).

    if(gamePieces[gamePieces.length - 1].chessrank == 'Champion')
      gamePieces[gamePieces.length - 1].ownZ = 8;
    else if(gamePieces[gamePieces.length - 1].chessrank == 'Wizard')
      gamePieces[gamePieces.length - 1].ownZ = 0;
    else
      gamePieces[gamePieces.length - 1].ownZ = 0;
    gamePieces[gamePieces.length - 1].rotation.x = 90 * (Math.PI/180);
    gamePieces[gamePieces.length - 1].position.x = convIndexToX(b);
    gamePieces[gamePieces.length - 1].position.y = convIndexToY(b);
    gamePieces[gamePieces.length - 1].position.z = gamePieces[gamePieces.length - 1].ownZ;
    gamePieces[gamePieces.length - 1].chessposition = b;

    scene.add(gamePieces[gamePieces.length - 1]);
    animationTarget = gamePieces.length - 1;
    animate_startScale = {x: gamePieces[animationTarget].scale.x,
                          y: gamePieces[animationTarget].scale.y,
                          z: gamePieces[animationTarget].scale.z};
    if(gamePieces[gamePieces.length - 1].chessrank == 'Champion')
      animate_endScale = {x: 13, y: 13, z: 13};
    else if(gamePieces[gamePieces.length - 1].chessrank == 'Wizard')
      animate_endScale = {x: 5, y: 5, z: 5};
    else
      animate_endScale = {x: 25, y: 25, z: 25};

    var tween = new TWEEN.Tween(animate_startScale).to(animate_endScale, 500);
    tween.onUpdate(function()
      {
        gamePieces[animationTarget].scale.x = animate_startScale.x;
        gamePieces[animationTarget].scale.y = animate_startScale.y;
        gamePieces[animationTarget].scale.z = animate_startScale.z;
      });
    tween.onComplete(function()
      {
        swapTurns();
      });
    tween.start();
  }

function swapTurns()
  {
    var winFlag;
    var i;

    switch(PromotionTarget)
      {
        case _PROMO_KNIGHT:   console.log(Select_A + ', ' + Select_B + ': Knight');    break;
        case _PROMO_CHAMPION: console.log(Select_A + ', ' + Select_B + ': Champion');  break;
        case _PROMO_WIZARD:   console.log(Select_A + ', ' + Select_B + ': Wizard');    break;
        case _PROMO_BISHOP:   console.log(Select_A + ', ' + Select_B + ': Bishop');    break;
        case _PROMO_ROOK:     console.log(Select_A + ', ' + Select_B + ': Rook');      break;
        case _PROMO_QUEEN:    console.log(Select_A + ', ' + Select_B + ': Queen');     break;
        default:              console.log(Select_A + ', ' + Select_B);
      }

    Select_A = _NOTHING;                                            //  Reset.
    Select_B = _NOTHING;
    Castle_C = _NOTHING;
    Castle_D = _NOTHING;
    PromotionTarget = _NO_PROMO;

    winFlag = gameEngine.isWin_client();                            //  Is the game state now terminal?
    if(winFlag != GAME_ONGOING)                                     //  The game state is now terminal.
      {
        artworkForThinking(false);                                  //  Pull "thinking" artwork.
        nodeCounter(false);                                         //  Pull the node counter.

        gameOver = true;                                            //  Signal that the game is over.
        chime_mp3.play();                                           //  Play the sound.

        if(winFlag == GAME_OVER_WHITE_WINS)
          {
            i = 0;                                                  //  Highlight the checkmated king.
            while(!(gameEngine.isBlack_client(i) && gameEngine.isKing_client(i)))
              i++;
            selectedSq(i);

            switch(currentLang)
              {
                case 'Spanish': alert(alertStringScrub('\xA1Jaque mate!'));  break;
                case 'German': alert(alertStringScrub('Schachmatt!'));  break;
                case 'Polish': alert(alertStringScrub('Mat!'));  break;
                default: alert(alertStringScrub('Checkmate!'));
              }
          }
        else if(winFlag == GAME_OVER_BLACK_WINS)
          {
            i = 0;                                                  //  Highlight the checkmated king.
            while(!(gameEngine.isWhite_client(i) && gameEngine.isKing_client(i)))
              i++;
            selectedSq(i);

            switch(currentLang)
              {
                case 'Spanish': alert(alertStringScrub('\xA1Jaque mate!'));  break;
                case 'German': alert(alertStringScrub('Schachmatt!'));  break;
                case 'Polish': alert(alertStringScrub('Mat!'));  break;
                default: alert(alertStringScrub('Checkmate!'));
              }
          }
        else
          {
            switch(currentLang)
              {
                case 'Spanish': alert(alertStringScrub('\xA1Estancamiento!'));  break;
                case 'German': alert(alertStringScrub('Patt!'));  break;
                case 'Polish': alert(alertStringScrub('Pat!'));  break;
                default: alert(alertStringScrub('Stalemate!'));
              }
          }
      }
    else                                                            //  The game is still on going.
      {
        if(CurrentTurn == 'White')
          {
            CurrentTurn = 'Black';
            if(pagan.team == 'White')
              HumansTurn = true;
            else
              HumansTurn = false;
          }
        else
          {
            CurrentTurn = 'White';
            if(pagan.team == 'Black')
              HumansTurn = true;
            else
              HumansTurn = false;
          }

        animationInstruction = null;                                //  Blank out animation instruction.
        animating = false;                                          //  We are no longer animating.
                                                                    //  Restore control to the human player.
        if((CurrentTurn == 'White' && pagan.team == 'Black') || (CurrentTurn == 'Black' && pagan.team == 'White'))
          {
            artworkForThinking(false);                              //  Pull "thinking" artwork.
            nodeCounter(false);                                     //  Pull the node counter.
            pagan.nodeCtr = 0;                                      //  Reset node counter.
            MasterControl = true;
          }
        else
          {
            artworkForThinking(true);                               //  Show the "thinking" artwork.
            updateNodeCounter(pagan.nodeCtr);                       //  Show the A.I.'s node count.
            nodeCounter(true);                                      //  Show the node counter.
          }

        gameEngine.draw();                                          //  Output to the console.
      }

    return;
  }