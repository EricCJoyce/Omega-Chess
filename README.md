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
