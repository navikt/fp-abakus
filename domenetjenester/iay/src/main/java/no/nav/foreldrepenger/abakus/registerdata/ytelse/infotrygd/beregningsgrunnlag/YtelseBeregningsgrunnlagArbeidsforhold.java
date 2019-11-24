package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Arbeidsforhold;

public class YtelseBeregningsgrunnlagArbeidsforhold {

    private static final String DAGLIG_KILDE = "D";
    private static final String UKENTLIG_KILDE = "U";
    private static final String BIUKENTLIG_KILDE = "F";
    private static final String MÅNEDLIG_KILDE = "M";
    private static final String ÅRLIG_KILDE = "Å";
    private static final String FASTSATT25PAVVIK_KILDE = "X";
    private static final String PREMIEGRUNNLAG_KILDE = "Y";

    private static final Map<String, InntektPeriodeType> INNTEKT_PERIODE_TYPE_MAP = Map.of(
        DAGLIG_KILDE, InntektPeriodeType.DAGLIG,
        UKENTLIG_KILDE, InntektPeriodeType.UKENTLIG,
        BIUKENTLIG_KILDE, InntektPeriodeType.BIUKENTLIG,
        MÅNEDLIG_KILDE, InntektPeriodeType.MÅNEDLIG,
        ÅRLIG_KILDE, InntektPeriodeType.ÅRLIG,
        FASTSATT25PAVVIK_KILDE, InntektPeriodeType.FASTSATT25PAVVIK,
        PREMIEGRUNNLAG_KILDE, InntektPeriodeType.PREMIEGRUNNLAG
    );

    private static final Logger log = LoggerFactory.getLogger(YtelseBeregningsgrunnlagArbeidsforhold.class);

    private final BigDecimal inntektForPerioden;
    private final String orgnr;
    private InntektPeriodeType inntektPeriodeType;


    YtelseBeregningsgrunnlagArbeidsforhold(Arbeidsforhold arbeidsforhold) {
        inntektForPerioden = arbeidsforhold.getInntektForPerioden();
        orgnr = arbeidsforhold.getOrgnr();
        if (arbeidsforhold.getInntektsPeriode() != null) {
            this.inntektPeriodeType = oversettFraKilde(arbeidsforhold.getInntektsPeriode().getValue());
        }
    }

    public BigDecimal getInntektForPerioden() {
        return inntektForPerioden;
    }

    public String getOrgnr() {
        return orgnr;
    }

    public boolean harGyldigOrgnr() {
        return OrganisasjonsNummerValidator.erGyldig(orgnr);
    }

    public InntektPeriodeType getInntektPeriodeType() {
        return inntektPeriodeType;
    }

    private InntektPeriodeType oversettFraKilde(String kode) {
        if (INNTEKT_PERIODE_TYPE_MAP.get(kode) == null) {
            log.warn("Infotrygd ga ukjent kode for inntektsperiode {}", kode);
        }
        return INNTEKT_PERIODE_TYPE_MAP.getOrDefault(kode, InntektPeriodeType.UDEFINERT);
    }
}
