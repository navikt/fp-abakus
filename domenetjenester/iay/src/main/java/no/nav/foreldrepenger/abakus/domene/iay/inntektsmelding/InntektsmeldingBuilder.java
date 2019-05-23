package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.JournalpostId;
import no.nav.vedtak.konfig.Tid;

public class InntektsmeldingBuilder {
    private final InntektsmeldingEntitet kladd;

    InntektsmeldingBuilder(InntektsmeldingEntitet kladd) {
        this.kladd = kladd;
    }

    public static InntektsmeldingBuilder builder() {
        return new InntektsmeldingBuilder(new InntektsmeldingEntitet());
    }

    public InntektsmeldingBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public InntektsmeldingBuilder medArbeidsforholdId(String arbeidsforholdId) {
        if (arbeidsforholdId != null) {
            kladd.setArbeidsforholdId(ArbeidsforholdRef.ref(arbeidsforholdId));
        }
        return this;
    }

    public InntektsmeldingBuilder medBeløp(BigDecimal verdi) {
        kladd.setInntektBeløp(new Beløp(verdi));
        return this;
    }

    public InntektsmeldingBuilder medJournalpostId(JournalpostId journalpostId) {
        kladd.setJournalpostId(journalpostId);
        return this;
    }

    public InntektsmeldingBuilder medNærRelasjon(boolean nærRelasjon) {
        kladd.setNærRelasjon(nærRelasjon);
        return this;
    }

    public InntektsmeldingBuilder medStartDatoPermisjon(LocalDate startPermisjon) {
        kladd.setStartDatoPermisjon(startPermisjon);
        return this;
    }

    public InntektsmeldingBuilder medRefusjon(BigDecimal verdi, LocalDate opphører) {
        kladd.setRefusjonBeløpPerMnd(new Beløp(verdi));
        kladd.setRefusjonOpphører(opphører);
        return this;
    }

    public InntektsmeldingBuilder medRefusjon(BigDecimal verdi) {
        kladd.setRefusjonBeløpPerMnd(new Beløp(verdi));
        kladd.setRefusjonOpphører(Tid.TIDENES_ENDE);
        return this;
    }

    public InntektsmeldingBuilder medInnsendingstidspunkt(LocalDateTime innsendingstidspunkt) {
        Objects.requireNonNull(innsendingstidspunkt, "innsendingstidspunkt");
        kladd.setInnsendingstidspunkt(innsendingstidspunkt);
        return this;
    }

    public InntektsmeldingBuilder medKanalreferanse(String kanalreferanse) {
        kladd.setKanalreferanse(kanalreferanse);
        return this;
    }
    
    public InntektsmeldingBuilder medMottattDato(LocalDate mottattDato) {
        kladd.setMottattDato(mottattDato);
        return this;
    }

    public InntektsmeldingBuilder medKildesystem(String kildesystem) {
        kladd.setKildesystem(kildesystem);
        return this;
    }

    public InntektsmeldingBuilder leggTil(NaturalYtelse naturalYtelse) {
        kladd.leggTil(naturalYtelse);
        return this;
    }

    public InntektsmeldingBuilder leggTil(UtsettelsePeriode utsettelsePeriode) {
        kladd.leggTil(utsettelsePeriode);
        return this;
    }

    public InntektsmeldingBuilder leggTil(Gradering gradering) {
        kladd.leggTil(gradering);
        return this;
    }

    public InntektsmeldingBuilder leggTil(Refusjon refusjon) {
        kladd.leggTil(refusjon);
        return this;
    }

    public InntektsmeldingBuilder medInntektsmeldingaarsak(InntektsmeldingInnsendingsårsak inntektsmeldingInnsendingsårsak) {
        kladd.setInntektsmeldingInnsendingsårsak(inntektsmeldingInnsendingsårsak);
        return this;
    }

    public InntektsmeldingBuilder medInntektsmeldingaarsak(String inntektsmeldingInnsendingsårsak) {
        return medInntektsmeldingaarsak(new InntektsmeldingInnsendingsårsak(inntektsmeldingInnsendingsårsak));
    }

    public Inntektsmelding build() {
        return kladd;
    }

    public InntektsmeldingBuilder medJournalpostId(String journalpostId) {
        return medJournalpostId(new JournalpostId(journalpostId));
    }

}
