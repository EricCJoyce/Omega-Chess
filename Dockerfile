#  Build a Docker image that can compile Java into WebAssembly.
#          sudo docker build -t java-wasm .
# syntax=docker/dockerfile:1.7

# Build stage
# Contains the JDK, Maven, TeaVM dependencies, and the Java compiler.
FROM maven:3.9.16-eclipse-temurin-21 AS builder

WORKDIR /build

#  Copy the Maven configuration first, allowing Docker to cache downloaded dependencies until pom.xml changes.
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    mvn \
      --batch-mode \
      --no-transfer-progress \
      -DskipTests \
      dependency:go-offline

# Now copy the Java source.
COPY src ./src

#  Compile Java to bytecode, then TeaVM bytecode to WebAssembly GC.
RUN --mount=type=cache,target=/root/.m2 \
    mvn \
      --batch-mode \
      --no-transfer-progress \
      -DskipTests \
      clean package

#  Verify and collect only the browser artifacts.
RUN set -eux; \
    WASM_DIR="target/generated/wasm/teavm"; \
    test -f "${WASM_DIR}/classes.wasm"; \
    test -f "${WASM_DIR}/classes.wasm-runtime.js"; \
    mkdir -p /output; \
    cp "${WASM_DIR}/classes.wasm" \
       /output/omega_chess.wasm; \
    cp "${WASM_DIR}/classes.wasm-runtime.js" \
       /output/omega_chess.wasm-runtime.js

#  Artifact-only stage
#  This is not a runtime container. Docker can copy these files directly into a local directory with --output.
FROM scratch AS artifact

COPY --from=builder /output/ /
