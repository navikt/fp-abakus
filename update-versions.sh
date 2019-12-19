#!/usr/bin/env bash


echo VTP_IMAGE=docker.pkg.github.com/navikt/vtp/vtp > .env
echo POSTGRES_IMAGE=postgres:12 >> .env


echo ".env fil opprettet - Klart for docker-compose up"
