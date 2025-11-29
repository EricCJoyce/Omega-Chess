# Omega-Chess
Notes on the creation of Omega Chess

## Docker container to compile Java to WebAssembly
Create the container.
```
sudo docker build -t emscripten-java .
```

Confirm its existence.
```
sudo docker images
```

Kill the container.
```
sudo docker image rm emscripten-java
```

## Zobrist hash generator

## Client-facing game logic module

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
