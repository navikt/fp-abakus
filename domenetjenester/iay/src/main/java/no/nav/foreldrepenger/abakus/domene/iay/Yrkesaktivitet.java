package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.List;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

public interface Yrkesaktivitet {

    /**
     * Kategorisering av aktivitet som er enten pensjonsgivende inntekt eller likestilt med pensjonsgivende inntekt
     * <p>
     * Fra aa-reg
     * <ul>
     * <li>{@value ArbeidType#ORDINÆRT_ARBEIDSFORHOLD}</li>
     * <li>{@value ArbeidType#MARITIMT_ARBEIDSFORHOLD}</li>
     * <li>{@value ArbeidType#FORENKLET_OPPGJØRSORDNING}</li>
     * </ul>
     * <p>
     * Fra inntektskomponenten
     * <ul>
     * <li>{@value ArbeidType#FRILANSER_OPPDRAGSTAKER_MED_MER}</li>
     * </ul>
     * <p>
     * De resterende kommer fra søknaden
     *
     * @return {@link ArbeidType}
     */
    ArbeidType getArbeidType();

    /**
     * Unik identifikator for arbeidsforholdet til aktøren i bedriften. Selve nøkkelen er ikke unik, men er unik for arbeidstaker hos arbeidsgiver.
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return referanse
     */
    InternArbeidsforholdRef getArbeidsforholdRef();

    /**
     * Liste over fremtidige / historiske permisjoner hos arbeidsgiver.
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return liste med permisjoner
     */
    Collection<Permisjon> getPermisjon();

    /**
     * Aktivitet som gjelder arbeid
     *
     * @return liste med aktiviteter for arbeid
     */
    Collection<AktivitetsAvtale> getAktivitetsAvtalerForArbeid();

    /**
     * Alle aktivitetsavtaler
     */
    Collection<AktivitetsAvtale> getAlleAktivitetsAvtaler();
    
    /**
     * ArbeidsgiverEntitet
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return {@link ArbeidsgiverEntitet}
     */
    Arbeidsgiver getArbeidsgiver();

    /**
     * Navn på utenlands arbeidsgiver
     *
     * @return Navn
     */
    String getNavnArbeidsgiverUtland();

    /**
     * Gir hele ansettelsesperioden for et arbeidsforhold.
     * <p>
     * NB! Gjelder kun arbeidsforhold.
     *
     * @return perioden
     */
    List<AktivitetsAvtale> getAnsettelsesPerioder();

    boolean erArbeidsforhold();

    Long getId();
}
