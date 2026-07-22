#  Build a Docker image that can compile Java into WebAssembly.
#          sudo docker build -t java-wasm .

FROM maven:3.9-eclipse-temurin-21

WORKDIR /project

ENTRYPOINT ["mvn", "--batch-mode", "--no-transfer-progress"]
