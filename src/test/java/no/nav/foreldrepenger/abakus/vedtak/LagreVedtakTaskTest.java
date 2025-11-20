package no.nav.foreldrepenger.abakus.vedtak;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ExtractFromYtelseV1;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskDataBuilder;

class LagreVedtakTaskTest {

    @RegisterExtension
    public static JpaExtension extension = new JpaExtension();

    private VedtakYtelseRepository repository = new VedtakYtelseRepository(extension.getEntityManager());
    private ExtractFromYtelseV1 extractor = new ExtractFromYtelseV1(repository);
    private LagreVedtakTask task = new LagreVedtakTask(repository, extractor);

    @Test
    void skal_feile_ved_valideringsfeil() {
        String payload = """
            {
              "version": "1.0",
              "aktør": {
                "verdi": "1957590366736"
              },
              "vedtattTidspunkt": "2019-06-11T00:00:00",
              "ytelse": "SVANGERSKAPSPENGER",
              "saksnummer": "139015437",
              "vedtakReferanse": "3028155d-c556-4a8a-a38d-a526b1129bf2",
              "ytelseStatus": "UNDER_BEHANDLING",
              "kildesystem": "FPSAK",
              "periode": {
                "fom": null,
                "tom": null
              },
              "anvist": [
                {
                  "periode": {
                    "fom": null,
                    "tom": null
                  },
                  "beløp": {
                    "verdi": 1234
                  },
                  "dagsats": {
                    "verdi": 1234
                  },
                  "utbetalingsgrad": {
                    "verdi": 100
                  },
                  "andeler": [
                    {
                      "arbeidsgiver": {
                        "ident": "999999999",
                        "identType": "ORGNUMMER"
                      },
                      "arbeidsforholdId": "joeisjf843jr3",
                      "dagsats": {
                        "verdi": 1234
                      },
                      "utbetalingsgrad": {
                        "verdi": 100
                      },
                      "refusjonsgrad": {
                        "verdi": 100
                      },
                      "inntektklasse" : "ARBEIDSTAKER"
                    }
                  ]
                }
              ]
            }
            """;

        ProsessTaskData data = ProsessTaskDataBuilder.forProsessTask(LagreVedtakTask.class)
            .medPayload(payload)
            .medProperty(LagreVedtakTask.KEY, UUID.randomUUID().toString())
            .build();

        assertThrows(IllegalArgumentException.class, () -> task.doTask(data));
    }


    @Test
    void skal_ikke_feile_uten_valideringsfeil() {
        String payload = """
            {
              "version" : "1.0",
              "aktør" : {
                "verdi" : "1293528970663"
              },
              "vedtattTidspunkt" : "2021-12-15T11:06:01.623",
              "ytelse" : "PLEIEPENGER_SYKT_BARN",
              "saksnummer" : "1DMNZBU",
              "vedtakReferanse" : "e10b4624-d205-4bf8-a065-c41ed74be90b",
              "ytelseStatus" : "LØPENDE",
              "kildesystem" : "K9SAK",
              "periode" : {
                "fom" : "2021-11-01",
                "tom" : "2021-11-19"
              },
              "tilleggsopplysninger" : "{\\n  \\"pleietrengende\\" : \\"2569674469455\\",\\n  \\"innleggelsesPerioder\\" : [ ]\\n}",
              "anvist" : [ {
                "periode" : {
                  "fom" : "2021-11-01",
                  "tom" : "2021-11-05"
                },
                "beløp" : null,
                "dagsats" : {
                  "verdi" : 1846.00
                },
                "utbetalingsgrad" : {
                  "verdi" : 100.00
                },
                "andeler" : [ {
                  "arbeidsgiver" : {
                    "identType" : "ORGNUMMER",
                    "ident" : "972674818"
                  },
                  "arbeidsgiverIdent" : {
                    "ident" : "972674818"
                  },
                  "arbeidsforholdId" : "1",
                  "dagsats" : {
                    "verdi" : 1846.00
                  },
                  "utbetalingsgrad" : {
                    "verdi" : 100.00
                  },
                  "refusjonsgrad" : {
                    "verdi" : 100.00
                  },
                  "inntektklasse" : "ARBEIDSTAKER"
                } ]
              }, {
                "periode" : {
                  "fom" : "2021-11-08",
                  "tom" : "2021-11-12"
                },
                "beløp" : null,
                "dagsats" : {
                  "verdi" : 1846.00
                },
                "utbetalingsgrad" : {
                  "verdi" : 100.00
                },
                "andeler" : [ {
                  "arbeidsgiver" : {
                    "identType" : "ORGNUMMER",
                    "ident" : "972674818"
                  },
                  "arbeidsgiverIdent" : {
                    "ident" : "972674818"
                  },
                  "arbeidsforholdId" : "1",
                  "dagsats" : {
                    "verdi" : 1846.00
                  },
                  "utbetalingsgrad" : {
                    "verdi" : 100.00
                  },
                  "refusjonsgrad" : {
                    "verdi" : 100.00
                  },
                  "inntektklasse" : "ARBEIDSTAKER"
                } ]
              }, {
                "periode" : {
                  "fom" : "2021-11-15",
                  "tom" : "2021-11-19"
                },
                "beløp" : null,
                "dagsats" : {
                  "verdi" : 1846.00
                },
                "utbetalingsgrad" : {
                  "verdi" : 100.00
                },
               "andeler" : [ {
                  "arbeidsgiver" : {
                    "identType" : "ORGNUMMER",
                    "ident" : "972674818"
                  },
                  "arbeidsforholdId" : "1",
                  "dagsats" : {
                    "verdi" : 1846.00
                  },
                  "utbetalingsgrad" : {
                    "verdi" : 100.00
                  },
                  "refusjonsgrad" : {
                    "verdi" : 100.00
                  },
                  "inntektklasse" : "ARBEIDSTAKER"
                } ]
              } ]
            }
            """;

        ProsessTaskData data = ProsessTaskDataBuilder.forProsessTask(LagreVedtakTask.class)
            .medPayload(payload)
            .medProperty(LagreVedtakTask.KEY, UUID.randomUUID().toString())
            .build();

        task.doTask(data);

        extension.getEntityManager().flush();

        var vedtakYtelser = repository.hentYtelserForIPeriode(new AktørId("1293528970663"), LocalDate.of(2021, 11, 1), LocalDate.of(2021, 11, 19));

        assertThat(vedtakYtelser.size()).isEqualTo(1);
        assertThat(vedtakYtelser.get(0).getYtelseAnvist().size()).isEqualTo(3);
        assertThat(vedtakYtelser.get(0).getYtelseAnvist().iterator().next().getAndeler().size()).isEqualTo(1);

    }
}
