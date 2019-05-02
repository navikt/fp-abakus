package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.abakus.domene.iay.ArbeidsgiverEntitet;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Beløp;

public interface Inntektsmelding {
    /**
     * Arbeidsgiveren som har sendt inn inntektsmeldingen
     *
     * @return {@link ArbeidsgiverEntitet}
     */
    ArbeidsgiverEntitet getArbeidsgiver();

    String getKanalreferanse();

    /**
     * Liste over perioder med graderinger
     *
     * @return {@link Gradering}
     */
    List<Gradering> getGraderinger();

    /**
     * Liste over naturalytelser
     *
     * @return {@link NaturalYtelse}
     */
    List<NaturalYtelse> getNaturalYtelser();

    /**
     * Liste over utsettelse perioder
     *
     * @return {@link UtsettelsePeriode}
     */
    List<UtsettelsePeriode> getUtsettelsePerioder();

    /**
     * Arbeidsgivers arbeidsforhold referanse
     *
     * @return {@link ArbeidsforholdRef}
     */
    ArbeidsforholdRef getArbeidsforholdRef();

    /**
     * Gjelder for et spesifikt arbeidsforhold
     *
     * @return {@link Boolean}
     */
    boolean gjelderForEtSpesifiktArbeidsforhold();

    /**
     * Startdato for permisjonen
     *
     * @return {@link LocalDate}
     */
    LocalDate getStartDatoPermisjon();

    /**
     * Referanse til {@link no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument} som benyttes for å markere
     * hvilke dokument som er gjeldende i behandlingen
     *
     * @return {@link Long}
     */
    Long getMottattDokumentId();

    /**
     * Er det nær relasjon mellom søker og arbeidsgiver
     *
     * @return {@link Boolean}
     */
    boolean getErNærRelasjon();

    /**
     * Oppgitt årsinntekt fra arbeidsgiver
     *
     * @return {@link BigDecimal}
     */
    Beløp getInntektBeløp();

    /**
     * Beløpet arbeidsgiver ønsker refundert
     *
     * @return {@link BigDecimal}
     */
    Beløp getRefusjonBeløpPerMnd();

    /**
     * Dersom refusjonen opphører i stønadsperioden angis siste dag det søkes om refusjon for.
     *
     * @return {@link LocalDate}
     */
    LocalDate getRefusjonOpphører();

    /**
     * Liste over endringer i refusjonsbeløp
     *
     * @return {@Link Refusjon}
     */
    List<Refusjon> getEndringerRefusjon();


    InntektsmeldingInnsendingsårsak getInntektsmeldingInnsendingsårsak();

    LocalDateTime getInnsendingstidspunkt();

    String getKildesystem();

    default boolean gjelderSammeArbeidsforhold(Inntektsmelding annen) {
        return getArbeidsgiver().equals(annen.getArbeidsgiver())
            && getArbeidsforholdRef().gjelderFor(annen.getArbeidsforholdRef());
    }

}
