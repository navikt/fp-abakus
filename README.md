![](https://github.com/navikt/fp-abakus/workflows/Bygg%20og%20deploy/badge.svg) 
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-abakus) 
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=navikt_fp-abakus)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=navikt_fp-abakus)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-abakus&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=navikt_fp-abakus)

ABAKUS
===============
Dette er repository for kildkode som dekker innhenting og etablering av grunnlag for IAY(inntekt, arbeid & ytelse).  Grunnlaget består av registrerte inntekter, arbeidsgivere og arbeidsforhold, etablerte ytelser, oppgitt opptjening, og saksbehandlers merknader og evt. bekreftede/skjønnsmessig fastsatte vurderinger.

### Struktur
Dette er dekker IAY(inntekt, arbeid & ytelse) Foreldrepenger & Svangerskapspenger (Folketrygdloven kapittel 14), og etablering av beregningsgrunnlag (kapittel 8).

Hvert grunnlag er immutable, men består av ett eller flere 'aggregater' (DDD terminologi) med hver sin livssyklus (eks. inntektsmeldigner kommer fra arbeidsgivere, registeropplysninger fra ulike systemer i Nav, Skatt, A-ordningen).  Hver endring lagres separat og deduplisert ifht. aggregatene (dvs. dersom et aggregat ikke endrer seg blir det ikke duplisert, men lages en peker fra grunnlaget til den versjonen som inkluderes).

### Utviklingshåndbok
[Utviklingoppsett](https://confluence.adeo.no/display/LVF/60+Utviklingsoppsett)
[Utviklerhåndbok, Kodestandard, osv](https://confluence.adeo.no/pages/viewpage.action?pageId=190254327)

### Miljøoversikt
[Miljøer](https://confluence.adeo.no/pages/viewpage.action?pageId=193202159)


### Linker
[Foreldrepengeprosjektet på Confluence](http://confluence.adeo.no/display/MODNAV/Foreldrepengeprosjektet)

