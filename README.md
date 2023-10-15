FP-ABAKUS
================
[![Bygg og deploy](https://github.com/navikt/fp-abakus/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/navikt/fp-abakus/actions/workflows/build.yml)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=navikt_fp-abakus)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=coverage)](https://sonarcloud.io/summary/new_code?id=navikt_fp-abakus)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-abakus)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=bugs)](https://sonarcloud.io/dashboard?id=navikt_fp-abakus)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=navikt_fp-abakus)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=navikt_fp-abakus)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=navikt_fp-abakus)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=sqale_index)](https://sonarcloud.io/dashboard?id=navikt_fp-abakus)

### Abakus kontrakt
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/fp-abakus)](https://github.com/navikt/fp-abakus/releases)
![GitHub](https://img.shields.io/github/license/navikt/fp-abakus)

Dette er repository for kildkode som dekker innhenting og etablering av grunnlag for IAY(inntekt, arbeid & ytelse). Grunnlaget består av registrerte
inntekter, arbeidsgivere og arbeidsforhold, etablerte ytelser, oppgitt opptjening, og saksbehandlers merknader og evt. bekreftede/skjønnsmessig
fastsatte vurderinger.

### Struktur

Dette er dekker IAY(inntekt, arbeid & ytelse) Foreldrepenger & Svangerskapspenger (Folketrygdloven kapittel 14), Pleiepenger og Omsorgspenger (
Folketrygdloven kapittel 9). Dette benyttes som underlag for opptjening, hvilke arbeidsaktiviteter bruker har hatt, og beregningsgrunnlag i saksflyt (
ikke del av denne tjenesten)

Hvert grunnlag er immutable, men består av ett eller flere 'aggregater' (DDD terminologi) med hver sin livssyklus (eks. inntektsmeldigner kommer fra
arbeidsgivere, registeropplysninger fra ulike systemer i Nav, Skatt, A-ordningen). Hver endring lagres separat og deduplisert ifht. aggregatene (dvs.
dersom et aggregat ikke endrer seg blir det ikke duplisert, men lages en peker fra grunnlaget til den versjonen som inkluderes).

### Utviklingshåndbok

[Utviklingoppsett](https://confluence.adeo.no/display/LVF/60+Utviklingsoppsett)
[Utviklerhåndbok, Kodestandard, osv](https://confluence.adeo.no/pages/viewpage.action?pageId=190254327)

### Miljøoversikt

[Miljøer](https://confluence.adeo.no/pages/viewpage.action?pageId=193202159)

### Linker

[Foreldrepengeprosjektet på Confluence](http://confluence.adeo.no/display/MODNAV/Foreldrepengeprosjektet)

### Sikkerhet

Det er mulig å kalle tjenesten med bruk av følgende tokens

- Azure CC
- Azure OBO med følgende rettigheter:
    - fpsak-saksbehandler
    - fpsak-veileder
    - k9-saksbehandler
    - k9-veileder
    - abakus-drift
- STS (fases ut)
