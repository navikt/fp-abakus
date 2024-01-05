#!/usr/bin/env bash

echo VTP_IMAGE=europe-north1-docker.pkg.dev/nais-management-233d/teamforeldrepenger/navikt/vtp > .env
echo POSTGRES_IMAGE=postgres:16 >> .env

echo ".env fil opprettet - Klart for docker-compose up"
