if(!Detector.webgl)
  Detector.addGetWebGLMessage();

var gameEngine;                                                     //  Compiled WebASM Module.
var gameStateBuffer = null;                                         //  Byte buffer
var gameOutputBuffer = null;                                        //  Byte buffer
var gameLogicExposedBufferBytes = 0;

const _GAMESTATE_BYTE_SIZE = 107;                                   //  Size (see Java code).
const _MOVE_BYTE_SIZE = 3;                                          //  Size (see Java code).
const _MOVEBUFFER_BYTE_SIZE = 64;                                   //  Size (see Java code).
const _MAX_MOVES = 512;                                             //  Size (see Java code).
const _ZHASH_TABLE_SIZE = 1786;                                     //  Size (see C code).
const _HASH_VALUE_BYTE_SIZE = 8;                                    //  Size of long long.
const _TRANSPO_TABLE_SIZE = 524288;                                 //  Size (see C code).
const _TRANSPO_RECORD_BYTE_SIZE = 18;                               //  Size (see C code).
const _PARAMETER_ARRAY_SIZE = 12;                                   //  Size (see C++ code).
const _NEGAMAX_NODE_STACK_CAPACITY = 32;                            //  Size (see C++ code).
const _NEGAMAX_MOVE_ARENA_CAPACITY = 8192;                          //  Size (see C++ code).
const _NEGAMAX_NODE_BYTE_SIZE = 161;                                //  Size (see C++ code).
const _NEGAMAX_MOVE_BYTE_SIZE = 4;                                  //  Size (see C++ code).
const _KILLER_MOVE_PER_PLY = 2;                                     //  Size (see C++ code).
const _KILLER_MOVE_MAX_DEPTH = 64;                                  //  Size (see C++ code).
const _STATS_BUFFER_SIZE = 16;                                      //  Size (see C++ code).
const _REPETITION_HISTORY_CAPACITY = 150;                           //  (See C++ code.)
const _REPETITION_HASH_BYTE_SIZE = 16;                              //  (See C++ code.)
const _REPETITION_PATH_CAPACITY = _NEGAMAX_NODE_STACK_CAPACITY;
const _REPETITION_PATH_PREFIX_CAPACITY = 1;                         //  (See C++ code.)
const _REPETITION_PATH_HEADER_SIZE = 1;                             //  (See C++ code.)
const _REPETITION_STATE_BYTE_SIZE = 106;                            //  (See C++ code.)

var pagan = new Player();                                           //  Create the A.I. agent.

//////////////////////////////////////////////////////////////////////  Controls
var gameStarted = false;                                            //  When the clock starts running, if there's a clock.

//////////////////////////////////////////////////////////////////////  Window dimensions
var screenWidth = window.innerWidth;
var screenHeight = window.innerHeight;

//////////////////////////////////////////////////////////////////////  CONSTANTS
const COLS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];
const ROWS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];
const  _W1 = 0,                                                                                                                         _W2 = 11;
const             _A1 = 13,   _B1 = 14,   _C1 = 15,   _D1 = 16,   _E1 = 17,   _F1 = 18,   _G1 = 19,   _H1 = 20,   _I1 = 21,   _J1 = 22;
const             _A2 = 25,   _B2 = 26,   _C2 = 27,   _D2 = 28,   _E2 = 29,   _F2 = 30,   _G2 = 31,   _H2 = 32,   _I2 = 33,   _J2 = 34;
const             _A3 = 37,   _B3 = 38,   _C3 = 39,   _D3 = 40,   _E3 = 41,   _F3 = 42,   _G3 = 43,   _H3 = 44,   _I3 = 45,   _J3 = 46;
const             _A4 = 49,   _B4 = 50,   _C4 = 51,   _D4 = 52,   _E4 = 53,   _F4 = 54,   _G4 = 55,   _H4 = 56,   _I4 = 57,   _J4 = 58;
const             _A5 = 61,   _B5 = 62,   _C5 = 63,   _D5 = 64,   _E5 = 65,   _F5 = 66,   _G5 = 67,   _H5 = 68,   _I5 = 69,   _J5 = 70;
const             _A6 = 73,   _B6 = 74,   _C6 = 75,   _D6 = 76,   _E6 = 77,   _F6 = 78,   _G6 = 79,   _H6 = 80,   _I6 = 81,   _J6 = 82;
const             _A7 = 85,   _B7 = 86,   _C7 = 87,   _D7 = 88,   _E7 = 89,   _F7 = 90,   _G7 = 91,   _H7 = 92,   _I7 = 93,   _J7 = 94;
const             _A8 = 97,   _B8 = 98,   _C8 = 99,   _D8 = 100,  _E8 = 101,  _F8 = 102,  _G8 = 103,  _H8 = 104,  _I8 = 105,  _J8 = 106;
const             _A9 = 109,  _B9 = 110,  _C9 = 111,  _D9 = 112,  _E9 = 113,  _F9 = 114,  _G9 = 115,  _H9 = 116,  _I9 = 117,  _J9 = 118;
const            _A10 = 121, _B10 = 122, _C10 = 123, _D10 = 124, _E10 = 125, _F10 = 126, _G10 = 127, _H10 = 128, _I10 = 129, _J10 = 130;
const  _W3 = 132,                                                                                                                       _W4 = 143;
const _NOTHING = 144;

//////////////////////////////////////////////////////////////////////  Three.js
var VIEW_ANGLE = 45;
var ASPECT = screenWidth / screenHeight;
var NEAR = 0.1;
var FAR = 10000;
var container = document.getElementById('container');               //  Contains the game
var renderer, camera, scene;
var CAMERA_X, CAMERA_Y, CAMERA_Z;
const PREFERRED_M = -0.3079928;                                     //  IMPLEMENTATION-SPECIFIC:
const PREFERRED_C = 670.0;                                          //  CAMERA_Z = M(minimum-screen-dimension) + C
                                                                    //  Keep the board at a comfortable but ample visual distance.
//////////////////////////////////////////////////////////////////////  Board squares
var boardSquares = [];                                              //  Array of PlaneGeometry objects
var normalMaterials = [];                                           //  Array of texture maps
var selectedMaterials = [];                                         //  Array of texture maps
var targetedMaterials = [];                                         //  Array of texture maps

const BOARD_SQ_WIDTH = 12, BOARD_SQ_HEIGHT = 12;                    //  IMPLEMENTATION-SPECIFIC
const SQ_WIDTH = 22, SQ_HEIGHT = 22, SQ_W_SEG = 1, SQ_H_SEG = 1;
const SQ_OFFSET = 23;
const LIVE_SQUARES = [_W1,                                                  _W2,
                          _A1, _B1, _C1, _D1, _E1, _F1, _G1, _H1, _I1, _J1,
                          _A2, _B2, _C2, _D2, _E2, _F2, _G2, _H2, _I2, _J2,
                          _A3, _B3, _C3, _D3, _E3, _F3, _G3, _H3, _I3, _J3,
                          _A4, _B4, _C4, _D4, _E4, _F4, _G4, _H4, _I4, _J4,
                          _A5, _B5, _C5, _D5, _E5, _F5, _G5, _H5, _I5, _J5,
                          _A6, _B6, _C6, _D6, _E6, _F6, _G6, _H6, _I6, _J6,
                          _A7, _B7, _C7, _D7, _E7, _F7, _G7, _H7, _I7, _J7,
                          _A8, _B8, _C8, _D8, _E8, _F8, _G8, _H8, _I8, _J8,
                          _A9, _B9, _C9, _D9, _E9, _F9, _G9, _H9, _I9, _J9,
                          _A10,_B10,_C10,_D10,_E10,_F10,_G10,_H10,_I10,_J10,
                      _W3,                                                  _W4];

//////////////////////////////////////////////////////////////////////  Board pieces
var pawnModelLoader, pawnGeometry;                                  //  Template loaders
var knightModelLoader, knightGeometry;
var wizardModelLoader, wizardGeometry;
var championModelLoader, championGeometry;
var bishopModelLoader, bishopGeometry;
var rookModelLoader, rookGeometry;
var queenModelLoader, queenGeometry;
var kingModelLoader, kingGeometry;
var gamePieces = [];

//////////////////////////////////////////////////////////////////////  Piece materials
var whiteMaterial, blackMaterial;

//////////////////////////////////////////////////////////////////////  Sounds
var select_mp3, deselect_mp3, commit_mp3, promote_mp3, chime_mp3, error_mp3, switch_mp3;

//////////////////////////////////////////////////////////////////////  Lights
var ambientLight;
var directionalLight1, directionalLight2;

//////////////////////////////////////////////////////////////////////  Game control
var MasterControl = false;                                          //  Shuts on/off all interactivity
var animating = false;
var animationInstruction = null;

var gameOver = false;                                               //  Has it ended?

var HumansTurn = true;
var CurrentTurn = 'White';                                          //  Whose turn to play
var Select_A = _NOTHING;
var Select_B = _NOTHING;
var Castle_C = _NOTHING;                                            //  IMPLEMENTATION-SPECIFIC
var Castle_D = _NOTHING;                                            //  Used to store the castling A, B after the Tween
var PromotionTarget = null;                                         //  Tracks the choice of pawn promotion

//////////////////////////////////////////////////////////////////////  Game clock
var previousSecond = Date.now();                                    //  Track the last millisecond

//////////////////////////////////////////////////////////////////////  Game logic
var Options = [];                                                   //  Array of indices.

//////////////////////////////////////////////////////////////////////  Game piece animation control
const GAMEPIECE_MOVEMENT_ZENITH = 100;
var animationTarget;                                                //  Maintains Mesh index when tweening
var animate_startPos;
var animate_midPos;
var animate_endPos;
var animate_startScale;
var animate_endScale;

//////////////////////////////////////////////////////////////////////  Load-targets
var elementsLoaded = 0;                                             //  Track objects to load
const ELEMENTS_TO_LOAD = 340;                                       //  104 squares: normal, selected, targeted + 8 meshes
                                                                    //  (DefaultLoadingManager excludes JSON meshes)
                                                                    //  +7 audio files
                                                                    //  +1 game logic WebASM module
                                                                    //  +1 evaluation WebASM module
                                                                    //  +1 network weights file
                                                                    //  +1 tree-search WebASM module
                                                                    //  +1 Zobrist hasher
                                                                    //  +4 tech details (en, pl, es, de)
                                                                    //  +4 control panels (en, pl, es, de)
THREE.DefaultLoadingManager.onProgress = function(item, loaded, total)
  {
    elementsLoaded++;
    loadTotalReached();                                             //  Test for load complete
  };

init3D();
initScene();
initEvents();
initSounds();

//////////////////////////////////////////////////////////////////////
//   I N I T s
function init3D()
  {
    scene = new THREE.Scene();
    camera = new THREE.PerspectiveCamera(VIEW_ANGLE, ASPECT, NEAR, FAR);
    renderer = new THREE.WebGLRenderer( {alpha: true} );

    CAMERA_X = 0;
    CAMERA_Y = 0;
    CAMERA_Z = PREFERRED_M * Math.min(screenWidth, screenHeight) + PREFERRED_C;

    scene.add(camera);
    camera.position.set(CAMERA_X, CAMERA_Y, CAMERA_Z);
    renderer.setSize(screenWidth, screenHeight);
    container.append(renderer.domElement);

    document.querySelectorAll('canvas')[0].id = "interactivelayer"; //  Distinguish this canvas from the "flowfield" canvas

    var canvas = document.getElementById('interactivelayer');       //  Add event listener for context loss:
                                                                    //  put yourself back together
    canvas.addEventListener("webglcontextlost", restoreLostContext, false)

    renderer.render(scene, camera);
  }

function initScene()
  {
    initLights();
    initBoard();
    initPieces();
  }

function initLights()
  {
    ambientLight = new THREE.AmbientLight(0xc0c0c0);

    directionalLight1 = new THREE.DirectionalLight(0xffffff, 0.3);
    directionalLight1.position.set(1, -1, 1).normalize();

    directionalLight2 = new THREE.DirectionalLight(0xffffff, 0.1);
    directionalLight2.position.set(1, 1, -1).normalize();

    scene.add(ambientLight);
    scene.add(directionalLight1);
    scene.add(directionalLight2);
  }

function initBoard()
  {
    var i;
    var row_ctr = 0;
    var col_ctr = 0;

    for(i = _W1; i < _NOTHING; i++)
      {
        if(i == _W1)
          {
            normalmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W1.png')
              });
            selectedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W1s.png')
              });
            targetedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W1t.png')
              });

            normalMaterials.push(normalmaterial);
            selectedMaterials.push(selectedmaterial);
            targetedMaterials.push(targetedmaterial);
          }
        else if(i == _W2)
          {
            normalmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W2.png')
              });
            selectedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W2s.png')
              });
            targetedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W2t.png')
              });

            normalMaterials.push(normalmaterial);
            selectedMaterials.push(selectedmaterial);
            targetedMaterials.push(targetedmaterial);
          }
        else if(i == _W3)
          {
            normalmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W3.png')
              });
            selectedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W3s.png')
              });
            targetedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W3t.png')
              });

            normalMaterials.push(normalmaterial);
            selectedMaterials.push(selectedmaterial);
            targetedMaterials.push(targetedmaterial);
          }
        else if(i == _W4)
          {
            normalmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W4.png')
              });
            selectedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W4s.png')
              });
            targetedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/W4t.png')
              });

            normalMaterials.push(normalmaterial);
            selectedMaterials.push(selectedmaterial);
            targetedMaterials.push(targetedmaterial);
          }
        else if(i % 12 > 0 && i % 12 < 11 && i >= _A1 && i <= _J10)
          {
            normalmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/' + COLS[col_ctr - 1] + ROWS[row_ctr - 1] + '.png')
              });
            selectedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/' + COLS[col_ctr - 1] + ROWS[row_ctr - 1] + 's.png')
              });
            targetedmaterial = new THREE.MeshLambertMaterial({
                map: THREE.ImageUtils.loadTexture('obj/img/board/' + COLS[col_ctr - 1] + ROWS[row_ctr - 1] + 't.png')
              });

            normalMaterials.push(normalmaterial);
            selectedMaterials.push(selectedmaterial);
            targetedMaterials.push(targetedmaterial);
          }

        col_ctr++;
        if(col_ctr > BOARD_SQ_WIDTH - 1)
          {
            col_ctr = 0;
            row_ctr++;
          }
      }
  }

function buildBoard()
  {
    var i;
    var x_offset = -80 - SQ_OFFSET - SQ_OFFSET;
    var y_offset = -80 - SQ_OFFSET - SQ_OFFSET;
    var target_x = 0;
    var target_y = 0;
    var row_ctr = 0;
    var col_ctr = 0;

    var planegeom = new THREE.PlaneGeometry(SQ_WIDTH, SQ_HEIGHT, SQ_W_SEG, SQ_H_SEG);

    for(i = _W1; i < _NOTHING; i++)
      {
        if(LIVE_SQUARES.indexOf(i) >= 0)
          {
            var plane = new THREE.Mesh(planegeom, normalMaterials[LIVE_SQUARES.indexOf(i)]);
            plane.name = i;
            plane.position.set(target_x + x_offset, target_y + y_offset, 0);
            boardSquares.push(plane);
            scene.add(boardSquares[boardSquares.length - 1]);
          }

        target_x += SQ_OFFSET;
        col_ctr++;
        if(target_x > (SQ_OFFSET * (BOARD_SQ_WIDTH - 1)))
          {
            target_x = 0;
            target_y += SQ_OFFSET;
            col_ctr = 0;
            row_ctr++;
          }
      }
    return;
  }

function initPieces()
  {
    whiteMaterial = new THREE.MeshLambertMaterial(
      {
        color: 0xEDF5FF,
        emissive: 0x000000
      });
    blackMaterial = new THREE.MeshPhongMaterial(
      {
        color: 0x383838,
        emissive: 0x000000,
        specular: 0xB9B9B9
      });

    pawnModelLoader = new THREE.JSONLoader();
    pawnModelLoader.load('obj/collada/Pawn.json', function(geometry)
      {
        pawnGeometry = geometry;
        elementsLoaded++;
        loadTotalReached();
      });

    knightModelLoader = new THREE.JSONLoader();
    knightModelLoader.load('obj/collada/Knight.json', function(geometry)
      {
        knightGeometry = geometry;
        elementsLoaded++;
        loadTotalReached();
      });

    wizardModelLoader = new THREE.JSONLoader();
    wizardModelLoader.load('obj/collada/Wizard.json', function(geometry)
      {
        wizardGeometry = geometry;
        elementsLoaded++;
        loadTotalReached();
      });

    championModelLoader = new THREE.JSONLoader();
    knightModelLoader.load('obj/collada/Champion.json', function(geometry)
      {
        championGeometry = geometry;
        elementsLoaded++;
        loadTotalReached();
      });

    bishopModelLoader = new THREE.JSONLoader();
    bishopModelLoader.load('obj/collada/Bishop.json', function(geometry)
      {
        bishopGeometry = geometry;
        elementsLoaded++;
        loadTotalReached();
      });

    rookModelLoader = new THREE.JSONLoader();
    rookModelLoader.load('obj/collada/Rook.json', function(geometry)
      {
        rookGeometry = geometry;
        elementsLoaded++;
        loadTotalReached();
      });

    queenModelLoader = new THREE.JSONLoader();
    queenModelLoader.load('obj/collada/Queen.json', function(geometry)
      {
        queenGeometry = geometry;
        elementsLoaded++;
        loadTotalReached();
      });

    kingModelLoader = new THREE.JSONLoader();
    kingModelLoader.load('obj/collada/King.json', function(geometry)
      {
        kingGeometry = geometry;
        elementsLoaded++;
        loadTotalReached();
      });
  }

function initPiece(symbol, index, geometry)
  {
    if(symbol == symbol.toUpperCase())                              //  White team
      {
        switch(symbol)
          {
            case 'P': gamePieces.push(new THREE.Mesh(geometry, whiteMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'White';
                      gamePieces[gamePieces.length - 1].chessrank = 'Pawn';
                      break;
            case 'N': gamePieces.push(new THREE.Mesh(geometry, whiteMaterial));
                      gamePieces[gamePieces.length - 1].rotation.y = -90 * (Math.PI/180);
                      gamePieces[gamePieces.length - 1].chessteam = 'White';
                      gamePieces[gamePieces.length - 1].chessrank = 'Knight';
                      break;
            case 'C': gamePieces.push(new THREE.Mesh(geometry, whiteMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'White';
                      gamePieces[gamePieces.length - 1].chessrank = 'Champion';
                      break;
            case 'W': gamePieces.push(new THREE.Mesh(geometry, whiteMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'White';
                      gamePieces[gamePieces.length - 1].chessrank = 'Wizard';
                      break;
            case 'B': gamePieces.push(new THREE.Mesh(geometry, whiteMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'White';
                      gamePieces[gamePieces.length - 1].chessrank = 'Bishop';
                      break;
            case 'R': gamePieces.push(new THREE.Mesh(geometry, whiteMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'White';
                      if(index == _A1)
                        gamePieces[gamePieces.length - 1].chessrank = 'QRook';
                      else
                        gamePieces[gamePieces.length - 1].chessrank = 'KRook';
                      break;
            case 'Q': gamePieces.push(new THREE.Mesh(geometry, whiteMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'White';
                      gamePieces[gamePieces.length - 1].chessrank = 'Queen';
                      break;
            case 'K': gamePieces.push(new THREE.Mesh(geometry, whiteMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'White';
                      gamePieces[gamePieces.length - 1].chessrank = 'King';
                      break;
          }
      }
    else                                                            //  Black team
      {
        switch(symbol)
          {
            case 'p': gamePieces.push(new THREE.Mesh(geometry, blackMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'Black';
                      gamePieces[gamePieces.length - 1].chessrank = 'Pawn';
                      break;
            case 'n': gamePieces.push(new THREE.Mesh(geometry, blackMaterial));
                      gamePieces[gamePieces.length - 1].rotation.y = 90 * (Math.PI/180);
                      gamePieces[gamePieces.length - 1].chessteam = 'Black';
                      gamePieces[gamePieces.length - 1].chessrank = 'Knight';
                      break;
            case 'c': gamePieces.push(new THREE.Mesh(geometry, blackMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'Black';
                      gamePieces[gamePieces.length - 1].chessrank = 'Champion';
                      break;
            case 'w': gamePieces.push(new THREE.Mesh(geometry, blackMaterial));
                      gamePieces[gamePieces.length - 1].rotation.y = Math.PI;
                      gamePieces[gamePieces.length - 1].chessteam = 'Black';
                      gamePieces[gamePieces.length - 1].chessrank = 'Wizard';
                      break;
            case 'b': gamePieces.push(new THREE.Mesh(geometry, blackMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'Black';
                      gamePieces[gamePieces.length - 1].chessrank = 'Bishop';
                      break;
            case 'r': gamePieces.push(new THREE.Mesh(geometry, blackMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'Black';
                      if(index == _A8)
                        gamePieces[gamePieces.length - 1].chessrank = 'QRook';
                      else
                        gamePieces[gamePieces.length - 1].chessrank = 'KRook';
                      break;
            case 'q': gamePieces.push(new THREE.Mesh(geometry, blackMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'Black';
                      gamePieces[gamePieces.length - 1].chessrank = 'Queen';
                      break;
            case 'k': gamePieces.push(new THREE.Mesh(geometry, blackMaterial));
                      gamePieces[gamePieces.length - 1].chessteam = 'Black';
                      gamePieces[gamePieces.length - 1].chessrank = 'King';
                      break;
          }
      }
    if(gamePieces[gamePieces.length - 1].chessrank == 'Champion')
      {
        gamePieces[gamePieces.length - 1].scale.x = 13;
        gamePieces[gamePieces.length - 1].scale.y = 13;
        gamePieces[gamePieces.length - 1].scale.z = 13;
        gamePieces[gamePieces.length - 1].ownZ = 8;
      }
    else if(gamePieces[gamePieces.length - 1].chessrank == 'Wizard')
      {
        gamePieces[gamePieces.length - 1].scale.x = 5;
        gamePieces[gamePieces.length - 1].scale.y = 5;
        gamePieces[gamePieces.length - 1].scale.z = 5;
        gamePieces[gamePieces.length - 1].ownZ = 0;
      }
    else
      {
        gamePieces[gamePieces.length - 1].scale.x = 25;
        gamePieces[gamePieces.length - 1].scale.y = 25;
        gamePieces[gamePieces.length - 1].scale.z = 25;
        gamePieces[gamePieces.length - 1].ownZ = 0;
      }
    gamePieces[gamePieces.length - 1].rotation.x = 90 * (Math.PI/180);
    gamePieces[gamePieces.length - 1].position.x = convIndexToX(index);
    gamePieces[gamePieces.length - 1].position.y = convIndexToY(index);
    gamePieces[gamePieces.length - 1].position.z = gamePieces[gamePieces.length - 1].ownZ;
    gamePieces[gamePieces.length - 1].chessposition = index;

    scene.add(gamePieces[gamePieces.length - 1]);
    return;
  }

function initSounds()
  {
    select_mp3 = new Audio();
    select_mp3.addEventListener('canplaythrough', () => { elementsLoaded++; loadTotalReached(); }, { once: true });
    select_mp3.src = 'obj/mp3/select.mp3';

    deselect_mp3 = new Audio();
    deselect_mp3.addEventListener('canplaythrough', () => { elementsLoaded++; loadTotalReached(); }, { once: true });
    deselect_mp3.src = 'obj/mp3/deselect.mp3';

    commit_mp3 = new Audio();
    commit_mp3.addEventListener('canplaythrough', () => { elementsLoaded++; loadTotalReached(); }, { once: true });
    commit_mp3.src = 'obj/mp3/commit.mp3';

    promote_mp3 = new Audio();
    promote_mp3.addEventListener('canplaythrough', () => { elementsLoaded++; loadTotalReached(); }, { once: true });
    promote_mp3.src = 'obj/mp3/promote.mp3';

    chime_mp3 = new Audio();
    chime_mp3.addEventListener('canplaythrough', () => { elementsLoaded++; loadTotalReached(); }, { once: true });
    chime_mp3.src = 'obj/mp3/chime.mp3';

    error_mp3 = new Audio();
    error_mp3.addEventListener('canplaythrough', () => { elementsLoaded++; loadTotalReached(); }, { once: true });
    error_mp3.src = 'obj/mp3/error.mp3';

    switch_mp3 = new Audio();
    switch_mp3.addEventListener('canplaythrough', () => { elementsLoaded++; loadTotalReached(); }, { once: true });
    switch_mp3.src = 'obj/mp3/switch.mp3';
  }

function initEvents()
  {
    window.addEventListener('resize', onWindowResize, false);
    window.addEventListener('mousedown', onClick, false);
    document.addEventListener('touchstart', onTouch, false);
  }

//////////////////////////////////////////////////////////////////////
//   L O A D I N G
function loadTotalReached()
  {
    var element = document.getElementById('loadingbanner');
    var ldBanner = document.getElementById('percentLoaded');
    var maxPlySlider;

    if(elementsLoaded == ELEMENTS_TO_LOAD)
      {
        element.parentNode.removeChild(element);                    //  Remove "Loading . . ." banner

        buildBoard();
        begin();

        reqSess();                                                  //  Load initial byte array into GameState buffer.

        maxPlySlider = document.getElementById('plies-slider');     //  Set the maximum, according to the WASM.
        maxPlySlider.max = pagan.negamaxEngine.instance.exports.getMaxPly();
      }
    else
      ldBanner.innerHTML = Math.round(elementsLoaded / ELEMENTS_TO_LOAD * 100) + ' %';
  }

globalThis.omegaChessInstallBuffers = function(currentStateView, movesBufferView)
  {
    gameStateBuffer = currentStateView;
    gameOutputBuffer = movesBufferView;
  };

//////////////////////////////////////////////////////////////////////
//   L A U N C H
function begin()
  {
    var tech = document.getElementById('tech-details');
    var theGui = document.getElementById('control-panel');

    resetCameraPositionAngle(36);                                   //  Default camera angle (the nicest in my opinion)
    render();                                                       //  Begin animation

    switch(currentLang)                                             //  Load interface components
      {
        case 'Polish':  tech.innerHTML = techDetails_pl;
                        theGui.innerHTML = gui_pl;
                        break;
        case 'Spanish': tech.innerHTML = techDetails_es;
                        theGui.innerHTML = gui_es;
                        break;
        case 'German':  tech.innerHTML = techDetails_de;
                        theGui.innerHTML = gui_de;
                        break;
        default:        tech.innerHTML = techDetails_en;
                        theGui.innerHTML = gui_en;
                        break;
      }
                                                                    //  If fullscreen is not available on this
    if(!fullscreenAvailable)                                        //  device, then hide the switch
      document.getElementById('fullscreen-row').style.display = "none";

    setPanelToggleReveal(true);                                     //  Enable panel toggle

    MasterControl = true;                                           //  BEGIN !!
  }

//////////////////////////////////////////////////////////////////////
//   R E S T O R E   W E B G L   C O N T E X T
function restoreLostContext(e)
  {
    e.preventDefault();                                             //  Prevent default action

    if(DEBUG_VERBOSE)
      console.log('WebGL context crashed');

    //cancelRequestAnimationFrame(requestId);
  }

//////////////////////////////////////////////////////////////////////
//   B O A R D   S Q U A R E   U T I L s
function convIndexToX(i)
  {                                                                 //  IMPLEMENTATION-SPECIFIC piece fudge
    return ((i % BOARD_SQ_WIDTH) * SQ_OFFSET) - 80 - SQ_OFFSET - SQ_OFFSET;
  }

function convIndexToY(i)
  {                                                                 //  IMPLEMENTATION-SPECIFIC piece fudge
    return (((i - (i % BOARD_SQ_WIDTH)) / BOARD_SQ_WIDTH) * SQ_OFFSET) - 80 - SQ_OFFSET - SQ_OFFSET;
  }

function midpoint(a, b)
  {
    return (a + b) / 2;
  }

//////////////////////////////////////////////////////////////////////
//   B O A R D   S Q U A R E   M A T E R I A L S
function normalSq(i)
  {
    i = typeof i !== 'undefined' ? i : _NOTHING;
    var ctr, j;

    if(i == _NOTHING)
      {
        ctr = _W1;
        j = _W4;
      }
    else
      {
        ctr = i;
        j = i;
      }

    for(; ctr <= j; ctr++)
      {
        if(LIVE_SQUARES.indexOf(ctr) >= 0)
          boardSquares[ LIVE_SQUARES.indexOf(ctr) ].material = normalMaterials[ LIVE_SQUARES.indexOf(ctr) ];
      }
  }

function selectedSq(i)
  {
    i = typeof i !== 'undefined' ? i : _NOTHING;
    var ctr, j;

    if(i == _NOTHING)
      {
        ctr = _W1;
        j = _W4;
      }
    else
      {
        ctr = i;
        j = i;
      }

    for(; ctr <= j; ctr++)
      {
        if(LIVE_SQUARES.indexOf(ctr) >= 0)
          boardSquares[ LIVE_SQUARES.indexOf(ctr) ].material = selectedMaterials[ LIVE_SQUARES.indexOf(ctr) ];
      }
  }

function targetedSq(i)
  {
    i = typeof i !== 'undefined' ? i : _NOTHING;
    var ctr, j;

    if(i == _NOTHING)
      {
        ctr = _W1;
        j = _W4;
      }
    else
      {
        ctr = i;
        j = i;
      }

    for(; ctr <= j; ctr++)
      {
        if(LIVE_SQUARES.indexOf(ctr) >= 0)
          boardSquares[ LIVE_SQUARES.indexOf(ctr) ].material = targetedMaterials[ LIVE_SQUARES.indexOf(ctr) ];
      }
  }

//////////////////////////////////////////////////////////////////////
//   C L I C K S
function validTarget(j)
  {
    var i = 0;
    while(i < Options.length && Options[i] != j)
      i++;
    if(i < Options.length)
      return true;
    return false;
  }

function onClick(e)
  {
    if(!gameOver)
      {
        var vector = new THREE.Vector3((e.clientX / screenWidth) * 2 - 1,
                                     - (e.clientY / screenHeight) * 2 + 1,
                                       1);
        vector.unproject(camera);

        var ray = new THREE.Raycaster(camera.position, vector.sub(camera.position).normalize());
        var intersects = ray.intersectObjects(boardSquares);

        selection(intersects);
      }
  }

function onTouch(e)
  {
    if(!gameOver && e.touches.length == 1)
      {
        var vector = new THREE.Vector3((e.touches[0].pageX / screenWidth) * 2 - 1,
                                     - (e.touches[0].pageY / screenHeight) * 2 + 1,
                                       1);
        vector.unproject(camera);

        var ray = new THREE.Raycaster(camera.position, vector.sub(camera.position).normalize());
        var intersects = ray.intersectObjects(boardSquares);

        selection(intersects);
      }
  }

function selection(intersects)
  {
    var firsthit;
    if(intersects.length > 0 && MasterControl && HumansTurn && !panelOpen)
      {
        firsthit = intersects[0].object.name;                       //  'name' of first square hit by the ray.

        if(CurrentTurn == 'White')                                  //  WHITE
          {
            if(Select_A == _NOTHING)
              {
                if(gameEngine.isWhite_client(firsthit))
                  {
                    Select_A = firsthit;                            //  Set Select_A.
                    select_mp3.play();                              //  Play the select sound.
                    selectedSq(Select_A);                           //  Change the square's material.
                    getMoves();                                     //  Query the moves for this piece.

                    if(!gameStarted)                                //  Officially start the game.
                      pullGUIComponents();
                  }
              }
            else if(Select_A == firsthit)
              {
                deselect_mp3.play();                                //  Play the deselect sound.
                normalSq();                                         //  Change (all) squares' material.
                Select_A = _NOTHING;                                //  Reset Select_A.
                Options = [];                                       //  Empty array.
              }
            else if(validTarget(firsthit))
              {
                MasterControl = false;                              //  Disable control right away.
                HumansTurn = false;
                Select_B = firsthit;
                Options = [];                                       //  Empty array.
                normalSq();                                         //  Reset all square colors.
                                                                    //  White is capturing a piece: set up a capture.
                if(gameEngine.isBlack_client(Select_B))
                  animationInstruction = {a:Select_A, b:Select_B, action:'die'};
                                                                    //  White is King's-side castling: set up a castle.
                else if(Select_A == _F1 && Select_B == _H1 &&
                        gameEngine.isWhite_client(_F1) && gameEngine.isKing_client(_F1) &&
                        !gameEngine.whiteCastled_client() && gameEngine.whiteKingsidePrivilege_client())
                  animationInstruction = {a:Select_A, b:Select_B, c:_I1, d:_G1, action:'castle'};
                                                                    //  White is Queen's-side castling: set up a castle.
                else if(Select_A == _F1 && Select_B == _D1 &&
                        gameEngine.isWhite_client(_F1) && gameEngine.isKing_client(_F1) &&
                        !gameEngine.whiteCastled_client() && gameEngine.whiteQueensidePrivilege_client())
                  animationInstruction = {a:Select_A, b:Select_B, c:_B1, d:_E1, action:'castle'};
                                                                    //  White is capturing en passant: set up en-passant capture.
                else if(gameEngine.isEnPassantAttack_client(Select_A, Select_B))
                  animationInstruction = {a:Select_A, b:Select_B, action:'dieEnPassant'};
                else                                                //  White is moving: set up the move.
                  animationInstruction = {a:Select_A, b:Select_B, action:'move'};

                animate();
              }
          }
        else                                                        //  BLACK
          {
            if(Select_A == _NOTHING)
              {
                if(gameEngine.isBlack_client(firsthit))
                  {
                    Select_A = firsthit;                            //  Set Select_A.
                    select_mp3.play();                              //  Play the select sound.
                    selectedSq(Select_A);                           //  Change the square's material.
                    getMoves();                                     //  Query the moves for this piece.

                    if(!gameStarted)                                //  Officially start the game.
                      pullGUIComponents();
                  }
              }
            else if(Select_A == firsthit)
              {
                deselect_mp3.play();                                //  Play the deselect sound.
                normalSq();                                         //  Change (all) squares' material.
                Select_A = _NOTHING;                                //  Reset Select_A.
                Options = [];                                       //  Empty array.
              }
            else if(validTarget(firsthit))
              {
                MasterControl = false;                              //  Disable control right away.
                HumansTurn = false;
                Select_B = firsthit;
                Options = [];                                       //  Empty array.
                normalSq();                                         //  Reset all square colors.
                                                                    //  Black is capturing a piece: set up a capture.
                if(gameEngine.isWhite_client(Select_B))
                  animationInstruction = {a:Select_A, b:Select_B, action:'die'};
                                                                    //  Black is King's-side castling: set up a castle.
                else if(Select_A == _F10 && Select_B == _H10 &&
                        gameEngine.isBlack_client(_F10) && gameEngine.isKing_client(_F10) &&
                        !gameEngine.blackCastled_client() && gameEngine.blackKingsidePrivilege_client())
                  animationInstruction = {a:Select_A, b:Select_B, c:_I10, d:_G10, action:'castle'};
                                                                    //  Black is Queen's-side castling: set up a castle.
                else if(Select_A == _F10 && Select_B == _D10 &&
                        gameEngine.isBlack_client(_F10) && gameEngine.isKing_client(_F10) &&
                        !gameEngine.blackCastled_client() && gameEngine.blackQueensidePrivilege_client())
                  animationInstruction = {a:Select_A, b:Select_B, c:_B10, d:_E10, action:'castle'};
                                                                    //  Black is capturing en passant: set up en-passant capture.
                else if(gameEngine.isEnPassantAttack_client(Select_A, Select_B))
                  animationInstruction = {a:Select_A, b:Select_B, action:'dieEnPassant'};
                else                                                //  Black is moving: set up the move.
                  animationInstruction = {a:Select_A, b:Select_B, action:'move'};

                animate();                                          //  Update the game state, perform the animation.
              }
          }
      }
  }

function getMoves()
  {
    //console.log('getMoves ' + Select_A)
    var len = gameEngine.getMovesIndex_client(Select_A);
    var i;

    for(i = 0; i < len; i++)
      {
        Options.push( gameOutputBuffer[i] );
        targetedSq( gameOutputBuffer[i] );
      }

    return;
  }

//////////////////////////////////////////////////////////////////////
//   S C R E E N   E V E N T S
function onWindowResize(e)
  {
    screenWidth = window.innerWidth;
    screenHeight = window.innerHeight;

    ASPECT = screenWidth / screenHeight;
    camera.aspect = ASPECT;
    camera.updateProjectionMatrix();
    renderer.setSize(screenWidth, screenHeight);

    CAMERA_Z = PREFERRED_M * Math.min(screenWidth, screenHeight) + PREFERRED_C;
    resetCameraPositionAngle(angle);                                //  Set this to itself just so that it can redraw
  }

function toggleFullscreen(b)
  {
    if(b)
      THREEx.FullScreen.request();
    else
      THREEx.FullScreen.cancel();
  }

//////////////////////////////////////////////////////////////////////
//   C A M E R A   M O V E M E N T
function resetCameraPositionAngle(a)
  {
    angle = a;

    if(pagan.team == 'Black')
      {
        camera.position.z = Math.cos(-a * (Math.PI / 180)) * CAMERA_Z;
        camera.position.y = CAMERA_Y + Math.sin(-a * (Math.PI / 180)) * CAMERA_Z;
        camera.rotation.x = a * (Math.PI / 180);
      }
    else
      {
        camera.position.z = Math.cos(-a * (Math.PI / 180)) * CAMERA_Z;
        camera.position.y = CAMERA_Y + Math.sin(a * (Math.PI / 180)) * CAMERA_Z;
        camera.rotation.x = -a * (Math.PI / 180);
      }
  }
