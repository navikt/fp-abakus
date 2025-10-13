FROM ghcr.io/navikt/fp-baseimages/chainguard:jre-25

LABEL org.opencontainers.image.source=https://github.com/navikt/fp-abakus

COPY web/target/classes/logback*.xml ./conf/
COPY web/target/lib/*.jar ./lib/
COPY web/target/app.jar ./
