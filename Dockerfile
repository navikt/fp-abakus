FROM ghcr.io/navikt/fp-baseimages/chainguard:jre-25
LABEL org.opencontainers.image.source=https://github.com/navikt/fp-abakus

COPY target/classes/logback*.xml ./conf/
COPY target/lib/*.jar ./lib/
COPY target/app.jar ./
