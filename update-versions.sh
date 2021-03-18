#!/usr/bin/env bash

echo VTP_IMAGE=ghcr.io/navikt/vtp > .env
echo POSTGRES_IMAGE=postgres:12 >> .env
echo AZURE_MOCK_IMAGE=docker.pkg.github.com/navikt/azure-mock/azure-mock:latest >> .env

echo ".env fil opprettet - Klart for docker-compose up"
