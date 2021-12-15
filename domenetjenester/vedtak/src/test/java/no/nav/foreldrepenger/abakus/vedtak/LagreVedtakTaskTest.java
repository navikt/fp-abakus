package no.nav.foreldrepenger.abakus.vedtak;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelse;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ExtractFromYtelseV1;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskDataBuilder;

public class LagreVedtakTaskTest {

    @RegisterExtension
    public static JpaExtension extension = new JpaExtension();

    private VedtakYtelseRepository repository = new VedtakYtelseRepository(extension.getEntityManager());
    private ExtractFromYtelseV1 extractor = new ExtractFromYtelseV1(repository);
    private LagreVedtakTask task = new LagreVedtakTask(repository, extractor);

    @Test
    public void skal_feile_ved_valideringsfeil() {
        String payload = "{\n" +
            "  \"version\": \"1.0\",\n" +
            "  \"aktør\": {\n" +
            "    \"verdi\": \"1957590366736\"\n" +
            "  },\n" +
            "  \"vedtattTidspunkt\": \"2019-06-11T00:00:00\",\n" +
            "  \"type\": {\n" +
            "    \"kodeverk\": \"FAGSAK_YTELSE_TYPE\",\n" +
            "    \"kode\": \"SVP\"\n" +
            "  },\n" +
            "  \"saksnummer\": \"139015437\",\n" +
            "  \"vedtakReferanse\": \"3028155d-c556-4a8a-a38d-a526b1129bf2\",\n" +
            "  \"status\": {\n" +
            "    \"kodeverk\": \"YTELSE_STATUS\",\n" +
            "    \"kode\": \"UBEH\"\n" +
            "  },\n" +
            "  \"fagsystem\": {\n" +
            "    \"kodeverk\": \"FAGSYSTEM\",\n" +
            "    \"kode\": \"FPSAK\"\n" +
            "  },\n" +
            "  \"periode\": {\n" +
            "    \"fom\": null,\n" +
            "    \"tom\": null\n" +
            "  },\n" +
            "  \"anvist\": [\n" +
            "    {\n" +
            "      \"periode\": {\n" +
            "        \"fom\": null,\n" +
            "        \"tom\": null\n" +
            "      },\n" +
            "      \"beløp\": {\n" +
            "        \"verdi\": 1234\n" +
            "      },\n" +
            "      \"dagsats\": {\n" +
            "        \"verdi\": 1234\n" +
            "      },\n" +
            "      \"utbetalingsgrad\": {\n" +
            "        \"verdi\": 100\n" +
            "      },\n" +
            "      \"andeler\": [\n" +
            "        {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"ident\": \"999999999\",\n" +
            "            \"identType\": \"ORGNUMMER\"\n" +
            "          },\n" +
            "          \"arbeidsforholdId\": \"joeisjf843jr3\",\n" +
            "          \"dagsats\": {\n" +
            "            \"verdi\": 1234\n" +
            "          },\n" +
            "          \"utbetalingsgrad\": {\n" +
            "            \"verdi\": 100\n" +
            "          },\n" +
            "          \"refusjonsgrad\": {\n" +
            "            \"verdi\": 100\n" +
            "          },\n" +
            "          \"inntektskategori\": {\n" +
            "            \"kode\": \"ARBEIDSTAKER\",\n" +
            "            \"kodeverk\": \"INNTEKTSKATEGORI\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";

        ProsessTaskData data = ProsessTaskDataBuilder.forProsessTask(LagreVedtakTask.class)
            .medPayload(payload)
            .medProperty(LagreVedtakTask.KEY, UUID.randomUUID().toString())
            .build();

        assertThrows(IllegalArgumentException.class, () ->task.doTask(data));
    }


    @Test
    public void skal_ikke_feile_uten_valideringsfeil() {
        String payload = "{\n" +
            "  \"version\" : \"1.0\",\n" +
            "  \"aktør\" : {\n" +
            "    \"verdi\" : \"1293528970663\"\n" +
            "  },\n" +
            "  \"vedtattTidspunkt\" : \"2021-12-15T11:06:01.623\",\n" +
            "  \"type\" : {\n" +
            "    \"kode\" : \"PSB\",\n" +
            "    \"kodeverk\" : \"FAGSAK_YTELSE_TYPE\"\n" +
            "  },\n" +
            "  \"saksnummer\" : \"1DMNZBU\",\n" +
            "  \"vedtakReferanse\" : \"e10b4624-d205-4bf8-a065-c41ed74be90b\",\n" +
            "  \"status\" : {\n" +
            "    \"kode\" : \"LOP\",\n" +
            "    \"kodeverk\" : \"YTELSE_STATUS\"\n" +
            "  },\n" +
            "  \"fagsystem\" : {\n" +
            "    \"kode\" : \"K9SAK\",\n" +
            "    \"kodeverk\" : \"FAGSYSTEM\"\n" +
            "  },\n" +
            "  \"periode\" : {\n" +
            "    \"fom\" : \"2021-11-01\",\n" +
            "    \"tom\" : \"2021-11-19\"\n" +
            "  },\n" +
            "  \"tilleggsopplysninger\" : \"{\\n  \\\"pleietrengende\\\" : \\\"2569674469455\\\",\\n  \\\"innleggelsesPerioder\\\" : [ ]\\n}\",\n" +
            "  \"anvist\" : [ {\n" +
            "    \"periode\" : {\n" +
            "      \"fom\" : \"2021-11-01\",\n" +
            "      \"tom\" : \"2021-11-05\"\n" +
            "    },\n" +
            "    \"beløp\" : null,\n" +
            "    \"dagsats\" : {\n" +
            "      \"verdi\" : 1846.00\n" +
            "    },\n" +
            "    \"utbetalingsgrad\" : {\n" +
            "      \"verdi\" : 100.00\n" +
            "    },\n" +
            "    \"andeler\" : [ {\n" +
            "      \"arbeidsgiver\" : {\n" +
            "        \"identType\" : \"ORGNUMMER\",\n" +
            "        \"ident\" : \"972674818\"\n" +
            "      },\n" +
            "      \"arbeidsforholdId\" : \"1\",\n" +
            "      \"dagsats\" : {\n" +
            "        \"verdi\" : 1846.00\n" +
            "      },\n" +
            "      \"utbetalingsgrad\" : {\n" +
            "        \"verdi\" : 100.00\n" +
            "      },\n" +
            "      \"refusjonsgrad\" : {\n" +
            "        \"verdi\" : 100.00\n" +
            "      },\n" +
            "      \"inntektskategori\" : {\n" +
            "        \"kode\" : \"ARBEIDSTAKER\",\n" +
            "        \"kodeverk\" : \"INNTEKTSKATEGORI\"\n" +
            "      }\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"periode\" : {\n" +
            "      \"fom\" : \"2021-11-08\",\n" +
            "      \"tom\" : \"2021-11-12\"\n" +
            "    },\n" +
            "    \"beløp\" : null,\n" +
            "    \"dagsats\" : {\n" +
            "      \"verdi\" : 1846.00\n" +
            "    },\n" +
            "    \"utbetalingsgrad\" : {\n" +
            "      \"verdi\" : 100.00\n" +
            "    },\n" +
            "    \"andeler\" : [ {\n" +
            "      \"arbeidsgiver\" : {\n" +
            "        \"identType\" : \"ORGNUMMER\",\n" +
            "        \"ident\" : \"972674818\"\n" +
            "      },\n" +
            "      \"arbeidsforholdId\" : \"1\",\n" +
            "      \"dagsats\" : {\n" +
            "        \"verdi\" : 1846.00\n" +
            "      },\n" +
            "      \"utbetalingsgrad\" : {\n" +
            "        \"verdi\" : 100.00\n" +
            "      },\n" +
            "      \"refusjonsgrad\" : {\n" +
            "        \"verdi\" : 100.00\n" +
            "      },\n" +
            "      \"inntektskategori\" : {\n" +
            "        \"kode\" : \"ARBEIDSTAKER\",\n" +
            "        \"kodeverk\" : \"INNTEKTSKATEGORI\"\n" +
            "      }\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"periode\" : {\n" +
            "      \"fom\" : \"2021-11-15\",\n" +
            "      \"tom\" : \"2021-11-19\"\n" +
            "    },\n" +
            "    \"beløp\" : null,\n" +
            "    \"dagsats\" : {\n" +
            "      \"verdi\" : 1846.00\n" +
            "    },\n" +
            "    \"utbetalingsgrad\" : {\n" +
            "      \"verdi\" : 100.00\n" +
            "    },\n" +
            "    \"andeler\" : [ {\n" +
            "      \"arbeidsgiver\" : {\n" +
            "        \"identType\" : \"ORGNUMMER\",\n" +
            "        \"ident\" : \"972674818\"\n" +
            "      },\n" +
            "      \"arbeidsforholdId\" : \"1\",\n" +
            "      \"dagsats\" : {\n" +
            "        \"verdi\" : 1846.00\n" +
            "      },\n" +
            "      \"utbetalingsgrad\" : {\n" +
            "        \"verdi\" : 100.00\n" +
            "      },\n" +
            "      \"refusjonsgrad\" : {\n" +
            "        \"verdi\" : 100.00\n" +
            "      },\n" +
            "      \"inntektskategori\" : {\n" +
            "        \"kode\" : \"ARBEIDSTAKER\",\n" +
            "        \"kodeverk\" : \"INNTEKTSKATEGORI\"\n" +
            "      }\n" +
            "    } ]\n" +
            "  } ]\n" +
            "}";

        ProsessTaskData data = ProsessTaskDataBuilder.forProsessTask(LagreVedtakTask.class)
            .medPayload(payload)
            .medProperty(LagreVedtakTask.KEY, UUID.randomUUID().toString())
            .build();

        task.doTask(data);

        extension.getEntityManager().flush();

        var vedtakYtelser = repository.hentYtelserForIPeriode(new AktørId("1293528970663"),
            LocalDate.of(2021, 11, 1),
            LocalDate.of(2021, 11, 19));

        assertThat(vedtakYtelser.size()).isEqualTo(1);
        assertThat(vedtakYtelser.get(0).getYtelseAnvist().size()).isEqualTo(3);
        assertThat(vedtakYtelser.get(0).getYtelseAnvist().iterator().next().getAndeler().size()).isEqualTo(1);


    }
}
