package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.jboss.weld.exceptions.IllegalArgumentException;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.JournalpostId;
import no.nav.vedtak.konfig.Tid;

public class InntektsmeldingBuilder {
    private final Inntektsmelding kladd;
    private boolean erBygget;
    private EksternArbeidsforholdRef eksternArbeidsforholdId;

    InntektsmeldingBuilder(Inntektsmelding kladd) {
        this.kladd = kladd;
    }

    public static InntektsmeldingBuilder builder() {
        return new InntektsmeldingBuilder(new Inntektsmelding());
    }

    public static InntektsmeldingBuilder kopi(Inntektsmelding inntektsmelding) {
        return new InntektsmeldingBuilder(new Inntektsmelding(inntektsmelding));
    }

    public InntektsmeldingBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        precondition();
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return kladd.getArbeidsgiver();
    }

    public Optional<EksternArbeidsforholdRef> getEksternArbeidsforholdRef() {
        return Optional.ofNullable(eksternArbeidsforholdId);
    }

    public Optional<InternArbeidsforholdRef> getInternArbeidsforholdRef() {
        return Optional.ofNullable(kladd.getArbeidsforholdRef());
    }

    public InntektsmeldingBuilder medArbeidsforholdId(EksternArbeidsforholdRef arbeidsforholdId) {
        precondition();
        this.eksternArbeidsforholdId = arbeidsforholdId;
        return this;
    }

    public InntektsmeldingBuilder medArbeidsforholdId(InternArbeidsforholdRef arbeidsforholdId) {
        precondition();
        if (arbeidsforholdId != null) {
            // magic - hvis har ekstern referanse må også intern referanse være spesifikk
            if (arbeidsforholdId.getReferanse() == null && eksternArbeidsforholdId != null && eksternArbeidsforholdId.gjelderForSpesifiktArbeidsforhold()) {
                throw new IllegalArgumentException(
                    "Begge referanser gjelde spesifikke arbeidsforhold. " + " Ekstern: " + eksternArbeidsforholdId + ", Intern: " + arbeidsforholdId);
            }
            kladd.setArbeidsforholdId(arbeidsforholdId);
        }
        return this;
    }
    public InntektsmeldingBuilder medBeløp(BigDecimal verdi) {
        precondition();
        kladd.setInntektBeløp(verdi == null ? null : new Beløp(verdi));
        return this;
    }

    public InntektsmeldingBuilder medJournalpostId(JournalpostId journalpostId) {
        precondition();
        kladd.setJournalpostId(journalpostId);
        return this;
    }

    public InntektsmeldingBuilder medNærRelasjon(boolean nærRelasjon) {
        precondition();
        kladd.setNærRelasjon(nærRelasjon);
        return this;
    }

    public InntektsmeldingBuilder medStartDatoPermisjon(LocalDate startPermisjon) {
        precondition();
        kladd.setStartDatoPermisjon(startPermisjon);
        return this;
    }

    public InntektsmeldingBuilder medRefusjon(BigDecimal verdi, LocalDate opphører) {
        precondition();
        kladd.setRefusjonBeløpPerMnd(verdi == null ? null : new Beløp(verdi));
        kladd.setRefusjonOpphører(opphører);
        return this;
    }

    public InntektsmeldingBuilder medRefusjon(BigDecimal verdi) {
        precondition();
        kladd.setRefusjonBeløpPerMnd(verdi == null ? null : new Beløp(verdi));
        kladd.setRefusjonOpphører(Tid.TIDENES_ENDE);
        return this;
    }

    public InntektsmeldingBuilder medInnsendingstidspunkt(LocalDateTime innsendingstidspunkt) {
        precondition();
        Objects.requireNonNull(innsendingstidspunkt, "innsendingstidspunkt");
        kladd.setInnsendingstidspunkt(innsendingstidspunkt);
        return this;
    }

    public InntektsmeldingBuilder medKanalreferanse(String kanalreferanse) {
        precondition();
        kladd.setKanalreferanse(kanalreferanse);
        return this;
    }

    public InntektsmeldingBuilder medMottattDato(LocalDate mottattDato) {
        precondition();
        kladd.setMottattDato(mottattDato);
        return this;
    }

    public InntektsmeldingBuilder medKildesystem(String kildesystem) {
        precondition();
        kladd.setKildesystem(kildesystem);
        return this;
    }

    public InntektsmeldingBuilder leggTil(NaturalYtelse naturalYtelse) {
        precondition();
        kladd.leggTil(naturalYtelse);
        return this;
    }

    public InntektsmeldingBuilder leggTil(UtsettelsePeriode utsettelsePeriode) {
        precondition();
        kladd.leggTil(utsettelsePeriode);
        return this;
    }

    public InntektsmeldingBuilder leggTil(Gradering gradering) {
        precondition();
        kladd.leggTil(gradering);
        return this;
    }

    public InntektsmeldingBuilder leggTil(Refusjon refusjon) {
        precondition();
        kladd.leggTil(refusjon);
        return this;
    }

    public InntektsmeldingBuilder medInntektsmeldingaarsak(InntektsmeldingInnsendingsårsak inntektsmeldingInnsendingsårsak) {
        precondition();
        kladd.setInntektsmeldingInnsendingsårsak(inntektsmeldingInnsendingsårsak);
        return this;
    }

    public Inntektsmelding build() {
        Objects.requireNonNull(kladd.getArbeidsgiver(), "arbeidsgiver mangler");
        Objects.requireNonNull(kladd.getInnsendingstidspunkt(), "innsendingstidspunkt mangler");
        Objects.requireNonNull(kladd.getJournalpostId(), "journalpostId");

        var internRef = getInternArbeidsforholdRef();
        if (internRef.isPresent()) {
            // magic - hvis har ekstern referanse må også intern referanse være spesifikk
            if ((eksternArbeidsforholdId != null && eksternArbeidsforholdId.gjelderForSpesifiktArbeidsforhold()) && internRef.get().getReferanse() == null) {
                throw new IllegalArgumentException(
                    "Begge referanser må gjelde spesifikke arbeidsforhold. " + " Ekstern: " + eksternArbeidsforholdId + ", Intern: " + internRef);
            }
        }
        erBygget = true;
        return kladd;
    }

    private void precondition() {
        if(erBygget) {
            throw new IllegalStateException("Inntektsmelding objekt er allerede bygget, kan ikke modifisere nå. Returnerer kun : " + kladd);
        }
    }

    public InntektsmeldingBuilder medJournalpostId(String journalpostId) {
        return medJournalpostId(journalpostId == null ? null : new JournalpostId(journalpostId));
    }

}
