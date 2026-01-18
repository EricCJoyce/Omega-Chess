#  Build a Docker image that can compile Java into WebAssembly.
#          sudo docker build -t emscripten-java .

FROM eclipse-temurin:17-jdk AS build
WORKDIR /src

RUN apt-get update \
 && apt-get install -y --no-install-recommends gradle \
 && rm -rf /var/lib/apt/lists/*

COPY . .
RUN gradle teavmWasm --no-daemon

FROM busybox:1.36
WORKDIR /out
COPY --from=build /src/build/generated/teavm/ /out/
