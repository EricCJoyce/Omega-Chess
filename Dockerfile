#  Build a Docker image that can compile Java into WebAssembly.
#          sudo docker build -t emscripten-java .

FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

#  Install gradle.
RUN apt-get update && apt-get install -y --no-install-recommends gradle && rm -rf /var/lib/apt/lists/*

COPY . .
# If you have ./gradlew in the repo, prefer that:
# RUN ./gradlew teavmWasm
RUN gradle teavmWasm --no-daemon

FROM busybox:1.36
WORKDIR /out

#  TeaVM default outputDir is under buildDir; commonly build/generated/teavm
COPY --from=build /src/build/generated/teavm/ /out/
