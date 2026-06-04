# fp-abakus

Service collecting, storing and sharing IAY aggregates (income, employments and benefits).

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

| Topic             | Details                                                                    |
|-------------------|----------------------------------------------------------------------------|
| Role              | Central application for case processing from søknad to vedtak/dismissal    |
| Consumers         | `fp-sak` and external apps outside the foreldrepenger sphere               |
| Tech stack        | Standard fp Java backend using `fp-prosesstask`                            |
| Data              | PostgreSQL; FSS deployment; long-term storage of IAY aggregates and vedtak |

Data sources / REST integrations besides PDL:
- Aa-register: employment data
- EREG: Company registration data (source Brreg)
- Inntekt: Monthly income - proxy for Skatteetaten Inntekt API 
- Sigrun: Yearly income - proxy for Skatteetaten Pensjonsgivende inntekt for folketrygden API
- Infotrygd replica: Legacy Sykepenger benefit data
- Spøkelse: Sykepenger benefit data
- Arena: Legacy Arbeidsavklaringspenger and Dagpenger - through `fp-ws-proxy`
- Kelvin: Arbeidsavklaringspenger
- Dp-sak: Dagpenger
- `VedtakYtelse`: Stored vedtak from `fp-sak` may become input for later benefit calculations

`VedtakConsumer` receives vedtak from `fp-sak` and `k9-sak` for local storage and later sharing.

## Domain model

- `Kobling`: linking `fp-sak` sak and behandling to stored data
- `InntektArbeidYtelseGrunnlag`: immutable aggregate of sub-aggregates for sub-domains with independent lifecycles 
- `InntektArbeidYtelseAggregat`: IAY sub-aggregate for register data - three parts for income, employment and benefits
- `OppgittOpptjening`: IAY sub-aggregate for user-provided data from søknad (benefit application)
- `InntektsmeldingAggregat`: IAY sub-aggregate for data from employer's inntektsmelding
- `ArbeidsforholdInformasjon`: IAY sub-aggregate for mapping from source arbeidsforhold identificator to internal identifiers + selections on how to include arbeidsforhold in a behandling
- `VedtakYtelse`: separate immutable aggregate with its own lifecycle. Used as input to `InntektArbeidYtelseAggregat` and external sharing 

## Entry points

- `RegisterdataRestTjeneste`: start asynchronous retrieval and storing of register data. Will call back to `fp-sak` when done
- `InntektsmeldingerRestTjeneste`: store and retrieve `Inntektsmelding`
- `OppgittOpptjeningRestTjeneste`: storing user-provided data from søknad
- `GrunnlagRestTjeneste`: services for retrieving and managing `InntektArbeidYtelseGrunnlag`
- `KoblingRestTjeneste`: deactivtes a kobling when a behandling is closed, preventing further update.
- `ArbeidsforholdRestTjeneste`: Aa-register employment data outside the IAY aggregate 
- `YtelseRestTjeneste`: internal sharing of `VedtakYtelse`
- `EksternDelingAvYtelserRestTjeneste`: external sharing of `VedtakYtelse` to consumers outside the foreldrepenger sphere

## Kontrakter sub-build

`kontrakter/` contains `kontrakt`, `kontrakt-vedtak`, and `kodeverk`. They are released separately as `abakus-kontrakt` and consumed by `fp-sak`, `fptilbake` and others.

## Verification

- Verify integration impact via `navikt/fp-autotest`.
- Relevant suites: `fpsak`, `verdikjede`.
- Preferred path: use the `run-integration-tests` skill 
