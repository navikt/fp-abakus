# fp-abakus

IAY (Inntekt, Arbeid & Ytelse) grunnlag service for **foreldrepenger** and
**svangerskapspenger** (Folketrygdloven kap. 14). Fetches and maintains
register-based data for inntekter, arbeidsforhold, and ytelser.
Also stores vedtak (LagretVedtak / VedtakYtelse) from fp-sak and other benefit systems
for use as ytelse in later cases. 
Data sharing outside teamforeldrepenger through ekstern Kafka and REST APIs. 

## Context (read first)

- **fp-context** (https://github.com/navikt/fp-context) — team-wide domain,
  architecture, conventions, workflow. Treat as source of truth.
- **Copilot Space**: navikt / **TeamForeldrepenger** — attaches fp-context + key repos.
- Defer to fp-context for: domain/Folketrygdloven kap. 14, backend tech / stack,
  Java code style, testing conventions, workflow/PR rules, CI/CD, runtime/NAIS,
  security, external integrations, data content.

## Role in the value chain

| Upstream | fp-abakus | Downstream |
|---|---|---|
| Aareg, Inntekt, Infotrygd (register data) | IAY-grunnlag | fp-sak (vilkår, opptjening) |
| Vedtak from other benefit systems via Kafka | LagretVedtak (ytelseshistorikk) | fp-kalkulus (beregningsgrunnlag) |
| fp-sak / consumers (kobling creation) | Inntektsmeldinger, OppgittOpptjening | |

## Domain model

Each IAY-grunnlag is **immutable** and composed of multiple aggregates (DDD)
with independent lifecycles (inntektsmeldinger from employers, register data
from various Nav/external systems). Each change is stored separately and
deduplicated — unchanged aggregates are not duplicated, only a new pointer
from the grunnlag to the included version.

LagretVedtak is a separate and immutable aggregate with independent lifecycle from grunnlag. It 
receives vedtak from fp-sak and other systems (k9-sak).

## Repo structure

| Area | Purpose |
|---|---|
| `iay` | IAY services (InntektArbeidYtelseTjeneste, InntektsmeldingerTjeneste, OppgittOpptjeningTjeneste) |
| `registerdata` | Data fetching from Aareg / Inntekt / Infotrygd / ytelser |
| `kobling` | Links consumer behandling to IAY-grunnlag |
| `vedtak` | LagretVedtak — receives/stores/publishes VedtakYtelse from benefit systems as ytelseshistorikk |
| `aktor` | AktørID handling |
| `app` | REST endpoints, healthcheck, metrics, maintenance |
| `domene`, `typer`, `felles` | Shared types and domain primitives |
| `prosesstask` | Async task config (nav-prosesstask) |
| `rydding`, `lonnskomp` | Cleanup / lønnskompensasjon |

## Kontrakter (sub-build)

`kontrakter/` contains `kontrakt`, `kontrakt-vedtak`, and `kodeverk` — released
separately as `abakus-kontrakt` (version controlled by `abakus-kontrakt.version`
in pom). Consumed by fp-sak, fp-kalkulus, and other systems that deliver vedtak.

## Integration testing

See [fp-autotest](https://github.com/navikt/fp-autotest).

## Tech

Standard backend tech stack. Uses PostgreSQL on-prem.
