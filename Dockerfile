# esx_verify Dockerfile
# 
# Maintainer: Łukasz Szeremeta
# Email: l.szeremeta.dev+mmlkg@gmail.com
# https://github.com/lszeremeta
#
# Usage:
# docker build -t esx_verify .
# docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify download
# docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify download m.lar
# docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify
# docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify m.lar
#
# Replace <input_path> with directory with mml.lar and esx_mml directory.
# You can use download param to download mml.lar and esx_mml files and prepare the input directory structure.
# You can use smaller m.lar file to test the process (it's copied to the input directory automatically).
# Replace <output_path> with directory if you want to see the output files.

# Multi-stage build: 1) build 2) busybox 3) package
# Build stage
FROM maven:3.9.2-eclipse-temurin-17 as build
WORKDIR /app

# Copy the project files into the docker image (see .dockerignore)
COPY . .

# Build and rename jar, all in one layer
RUN mvn -B package --file=pom.xml \
    && mv target/esx2miz-*-jar-with-dependencies.jar esx2miz.jar

FROM busybox:1.36.0-uclibc as busybox

FROM gcr.io/distroless/java17-debian11
LABEL maintainer="Łukasz Szeremeta <l.szeremeta.dev+mmlkg@gmail.com>"

# Copy the static shell and executables into distroless image
COPY --from=busybox /bin /bin
COPY --from=build /app/esx2miz.jar /app/esx2miz.jar

WORKDIR /app

# Copy the project files into the docker image (see .dockerignore)
COPY . .

ENTRYPOINT ["/bin/sh", "process_mml.sh"]