# [Omega Chess](https://www.ericjoycefilm.com/wastesoftime/boardgames/omegachess/index.php?lang=en)
Notes on the creation of Omega Chess

## Docker container to compile Java to WebAssembly
Create the container.
```
sudo docker build -t java-wasm .
```

Confirm its existence.
```
sudo docker images
```

Kill the container.
```
sudo docker image rm java-wasm
```

## Zobrist hash generator

## Client-facing game logic module

```
sudo docker run --rm -v "$PWD":/project -v "$HOME/.m2":/root/.m2 java-wasm clean package
```

```
target/
 +---classes/
 |    +---org/
 |         +---omegachess/
 |              +---core/
 |              |    +---GameEncoding.class
 |              |    +---GameLogic.class
 |              |    +---GameState.class
 |              |    +---Move.class
 |              +---wasm/
 |                   +---GameLogicWasm.class
 +---generated/
 |    +---wasm/
 |         +---classes.wasm-runtime.js
 |         +---teavm/
 |              +---gamelogic.wasm
 +---generated-sources/
 |    +---annotations/
 +---maven-archiver/
 |    +---pom.properties
 +---maven-status/
 |    +---maven-compiler-plugin/
 |         +---compile/
 |              +---default-compile/
 |                   +---createdFiles.lst
 |                   +---inputFiles.lst
 +---omega-chess-wasm-1.0.0-SNAPSHOT.jar
```

## Citation
If this code was helpful to you, please cite this repository.

```
@misc{omegachess,
  title={Omega Chess in Java},
  author={Eric C. Joyce},
  year={2025},
  publisher={Github},
  journal={GitHub repository},
  howpublished={\url{https://github.com/EricCJoyce/Omega-Chess}}
}
```
