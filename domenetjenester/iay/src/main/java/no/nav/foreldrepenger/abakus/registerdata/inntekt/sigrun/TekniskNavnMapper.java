package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.util.List;
import java.util.Map;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;

public final class TekniskNavnMapper {

    static final String PERSONINNTEKT_LØNN = "personinntektLoenn";
    static final String PERSONINNTEKT_BARE_PENSJONSDEL = "personinntektBarePensjonsdel";
    static final String PERSONINNTEKT_NÆRING = "personinntektNaering";
    static final String PERSONINNTEKT_FISKE_FANGST_FAMILIEBARNEHAGE = "personinntektFiskeFangstFamiliebarnehage";
    static final String SVALBARD_LØNN_LØNNSTREKKORDNINGEN = "svalbardLoennLoennstrekkordningen";
    static final String SVALBARD_PERSONINNTEKT_NÆRING = "svalbardPersoninntektNaering";
    static final String LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN = "loennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen";
    static final String SKATTEOPPGJØRSDATO  = "skatteoppgjoersdato";

    private static final Map<String, InntektspostType> SIGRUN_TIL_INNTEKTSPOST = Map.of(
        PERSONINNTEKT_LØNN, InntektspostType.LØNN,
        PERSONINNTEKT_BARE_PENSJONSDEL, InntektspostType.LØNN,
        PERSONINNTEKT_NÆRING, InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE,
        PERSONINNTEKT_FISKE_FANGST_FAMILIEBARNEHAGE, InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE,
        SVALBARD_LØNN_LØNNSTREKKORDNINGEN, InntektspostType.LØNN,
        SVALBARD_PERSONINNTEKT_NÆRING, InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE,
        LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN, InntektspostType.LØNN
    );

    private static final List<String> SIGRUN_TIL_NULL = List.of(SKATTEOPPGJØRSDATO);

    public static InntektspostType fraSigrunNavn(String tekniskNavn) {
        if (tekniskNavn == null || SIGRUN_TIL_NULL.contains(tekniskNavn)) {
            return null;
        }
        return SIGRUN_TIL_INNTEKTSPOST.get(tekniskNavn);
    }

}
