# fp-abakus — Agent Instructions

For team-wide context, see [fp-context](https://github.com/navikt/fp-context)
and the **TeamForeldrepenger** Copilot Space.

## Integration testing

Lives in [fp-autotest](https://github.com/navikt/fp-autotest).

**Suites covering fp-abakus:** `fpsak`, `verdikjede`

```bash
cd ~/git/fp-autotest
mvn test -P fpsak                    # full suite
mvn test -P fpsak -Dtest=Fodsel      # single class
mvn test -P verdikjede               # end-to-end
```

Test catalog and aksjonspunkt mapping are maintained in fp-autotest
(`TEST_CATALOG.md`, `AKSJONSPUNKT_MAPPING.md`) — do not duplicate here.

## Local build for autotest

| Item | Value         |
|---|---------------|
| Docker tag | `fp-abakus`   |
| .env var | `FPABAKUS_IMAGE` |
| Compose service | `fpabakus`    |

Use the `run-integration-tests` skill in fp-autotest for the full
build → deploy → test loop.
