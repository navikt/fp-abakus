#!/usr/bin/env bash


export VTP_VERSION="repo-fra-laptop-tunnel:14129/vtp:"$(git ls-remote --tags https://github.com/navikt/vtp.git | sort -t '/' -k 3 -V | tail -2 | head -1 | sed 's/.*refs\/tags\/\([a-z.0-9_]*$\)/\1/g')
echo VTP_IMAGE=${VTP_VERSION} >> .env

echo POSTGRES_IMAGE=postgres:12 >> .env


echo ".env fil opprettet - Klart for docker-compose up abakus [oracle]"
