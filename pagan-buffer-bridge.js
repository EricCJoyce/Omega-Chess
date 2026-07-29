//  Browser-side direct-buffer bridge for PaganEvaluationWasm.
//  Load this file before calling evalModule.exports.bindBuffers().
(function installPaganBridge(global)
  {
    "use strict";

    let evalModule = null;
    let views = null;

    global.omegaChessInstallPaganBuffers = function(inputGameState, inputMove, outputGameState, outputMoves, packedWeights)
      {
        views = Object.freeze({inputGameState, inputMove, outputGameState, outputMoves, packedWeights});
      };

    async function load(wasmUrl)
      {
        evalModule = await global.TeaVM.wasmGC.load(wasmUrl,
          {
            memory:
              {
                onResize: function()
                  {
                                                                    //  Direct-buffer typed-array views must be reacquired.
                    if(evalModule !== null)
                      evalModule.exports.bindBuffers();
                  }
              }
          });

        evalModule.exports.bindBuffers();
        requireViews();
        validateBufferLengths();
        return evalModule;
      }

    async function loadWeights(url)
      {
        requireModule();
        requireViews();

        const response = await fetch(url);
        if(!response.ok)
          throw new Error("Unable to fetch Pagan weights: " + response.status + " " + response.statusText);

        const bytes = await response.arrayBuffer();
        const expectedBytes = evalModule.exports.packedWeightBytes();

        if(bytes.byteLength !== expectedBytes)
          throw new Error("Pagan weight file has " + bytes.byteLength + " bytes; expected " + expectedBytes);

        if(views.packedWeights.length !== evalModule.exports.parameterCount())
          throw new Error("Packed-weight buffer length mismatch");
                                                                    //  Explicit little-endian decoding keeps the file format unambiguous.
        const source = new DataView(bytes);
        for(let i = 0; i < views.packedWeights.length; i++)
          views.packedWeights[i] = source.getFloat32(i * 4, true);

        evalModule.exports.commitWeights();

        if(!evalModule.exports.weightsCommitted())
          throw new Error("Pagan rejected the committed weights");
        return;
      }

    function validateBufferLengths()
      {
        if(views.inputGameState.length !== evalModule.exports.gameStateBytes())
          throw new Error("Input-state buffer length mismatch");

        if(views.inputMove.length !== evalModule.exports.moveBytes())
          throw new Error("Input-move buffer length mismatch");

        if(views.outputGameState.length !== evalModule.exports.gameStateBytes())
          throw new Error("Output-state buffer length mismatch");

        const expectedMoveBytes = evalModule.exports.maximumMoves() * evalModule.exports.moveRecordBytes();
        if(views.outputMoves.length !== expectedMoveBytes)
          throw new Error("Output-moves buffer length mismatch");
        return;
      }

    function requireModule()
      {
        if(evalModule === null)
          throw new Error("Pagan eval.wasm has not been loaded");
        return;
      }

    function requireViews()
      {
        if(views === null)
          throw new Error("Pagan direct buffers have not been bound");
        return;
      }

    global.OmegaChessPagan = Object.freeze({load,
                                            loadWeights,
                                            get module()
                                              {
                                                requireModule();
                                                return evalModule;
                                              },
                                            get buffers()
                                              {
                                                requireViews();
                                                return views;
                                              }
                                           });
  })(globalThis);
