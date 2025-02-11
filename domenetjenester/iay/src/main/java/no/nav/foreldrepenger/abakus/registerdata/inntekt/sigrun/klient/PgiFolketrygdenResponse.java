package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record PgiFolketrygdenResponse(String norskPersonidentifikator, Integer inntektsaar,
                                      List<Pgi> pensjonsgivendeInntekt) {
    public record Pgi(Skatteordning skatteordning, LocalDate datoForFastsetting,
                      Long pensjonsgivendeInntektAvLoennsinntekt,
                      Long pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel,
                      Long pensjonsgivendeInntektAvNaeringsinntekt,
                      Long pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage) {
    }

    public enum Skatteordning {
        FASTLAND, SVALBARD, KILDESKATT_PAA_LOENN
    }

    public List<Pgi> safePensjonsgivendeInntekt() {
        return Optional.ofNullable(pensjonsgivendeInntekt()).orElse(List.of());
    }

    @Override
    public String toString() {
        return "PgiFolketrygdenResponse{" + "inntektsaar=" + inntektsaar + ", pensjonsgivendeInntekt=" + pensjonsgivendeInntekt + '}';
    }
}
