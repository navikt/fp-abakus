package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanse;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

/**
 * Builder for å håndtere en gitt versjon {@link VersjonType} av grunnlaget.
 * <p>
 * Holder styr på om det er en oppdatering av eksisterende informasjon, om det gjelder før eller etter skjæringstidspunktet
 * og om det er registerdata eller saksbehandlers beslutninger.
 * <p>
 * NB! Viktig at denne builderen hentes fra repository for å sikre at den er rett tilstand ved oppdatering. Hvis ikke kan data gå tapt.
 */
public class InntektArbeidYtelseAggregatBuilder {

    private final InntektArbeidYtelseAggregat kladd;
    private final VersjonType versjon;
    private final List<ArbeidsforholdReferanse> nyeInternArbeidsforholdReferanser = new ArrayList<>();

    private InntektArbeidYtelseAggregatBuilder(InntektArbeidYtelseAggregat kladd, VersjonType versjon) {
        this.kladd = kladd;
        this.versjon = versjon;
    }

    public static InntektArbeidYtelseAggregatBuilder oppdatere(Optional<InntektArbeidYtelseAggregat> oppdatere, VersjonType versjon) {
        return builderFor(oppdatere, UUID.randomUUID(), LocalDateTime.now(), versjon);
    }

    public static InntektArbeidYtelseAggregatBuilder pekeTil(InntektArbeidYtelseAggregat oppdatere, VersjonType versjon) {
        Objects.requireNonNull(oppdatere);
        return new InntektArbeidYtelseAggregatBuilder(oppdatere, versjon);
    }

    public static InntektArbeidYtelseAggregatBuilder builderFor(Optional<InntektArbeidYtelseAggregat> kopierDataFra,
                                                                UUID angittReferanse, LocalDateTime angittTidspunkt, VersjonType versjon) {
        return kopierDataFra
            .map(kopier -> new InntektArbeidYtelseAggregatBuilder(new InntektArbeidYtelseAggregat(angittReferanse, angittTidspunkt, kopier), versjon))
            .orElseGet(() -> new InntektArbeidYtelseAggregatBuilder(new InntektArbeidYtelseAggregat(angittReferanse, angittTidspunkt), versjon));
    }

    /**
     * Legger til inntekter for en gitt aktør hvis det ikke er en oppdatering av eksisterende.
     * Ved oppdatering eksisterer koblingen for denne aktøren allerede så en kopi av forrige innslag manipuleres før lagring.
     *
     * @param aktørInntekt {@link AktørInntektBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørInntekt(AktørInntektBuilder aktørInntekt) {
        if (!aktørInntekt.getErOppdatering()) {
            // Hvis ny så skal den legges til, hvis ikke ligger den allerede der og blir manipulert.
            this.kladd.leggTilAktørInntekt(aktørInntekt.build());
        }
        return this;
    }

    /**
     * Legger til aktiviteter for en gitt aktør hvis det ikke er en oppdatering av eksisterende.
     * Ved oppdatering eksisterer koblingen for denne aktøren allerede så en kopi av forrige innslag manipuleres før lagring.
     *
     * @param aktørArbeid {@link AktørArbeidBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørArbeid(AktørArbeidBuilder aktørArbeid) {
        if (!aktørArbeid.getErOppdatering()) {
            // Hvis ny så skal den legges til, hvis ikke ligger den allerede der og blir manipulert.
            this.kladd.leggTilAktørArbeid(aktørArbeid.build());
        }
        return this;
    }

    /**
     * Legger til tilstøtende ytelser for en gitt aktør hvis det ikke er en oppdatering av eksisterende.
     * Ved oppdatering eksisterer koblingen for denne aktøren allerede så en kopi av forrige innslag manipuleres før lagring.
     *
     * @param aktørYtelse {@link AktørYtelseBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørYtelse(AktørYtelseBuilder aktørYtelse) {
        if (!aktørYtelse.getErOppdatering() && aktørYtelse.harVerdi()) {
            // Hvis ny så skal den legges til, hvis ikke ligger den allerede der og blir manipulert.
            this.kladd.leggTilAktørYtelse(aktørYtelse.build());
        }
        return this;
    }

    public void medNyInternArbeidsforholdRef(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef nyRef, EksternArbeidsforholdRef eksternReferanse) {
        nyeInternArbeidsforholdReferanser.add(new ArbeidsforholdReferanse(arbeidsgiver, nyRef, eksternReferanse));
    }

    public InternArbeidsforholdRef medNyInternArbeidsforholdRef(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef eksternReferanse) {
        if (eksternReferanse == null || eksternReferanse.getReferanse() == null) {
            return InternArbeidsforholdRef.nullRef();
        }
        InternArbeidsforholdRef nyRef = InternArbeidsforholdRef.nyRef();
        nyeInternArbeidsforholdReferanser.add(new ArbeidsforholdReferanse(arbeidsgiver, nyRef, eksternReferanse));
        return nyRef;
    }

    public List<ArbeidsforholdReferanse> getNyeInternArbeidsforholdReferanser() {
        return nyeInternArbeidsforholdReferanser;
    }

    /**
     * Oppretter builder for aktiviteter for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @param aktørId aktøren
     * @return builder {@link AktørArbeidBuilder}
     */
    public AktørArbeidBuilder getAktørArbeidBuilder(AktørId aktørId) {
        Optional<AktørArbeid> aktørArbeid = kladd.getAktørArbeid().stream().filter(aa -> aktørId.equals(aa.getAktørId())).findFirst();
        return AktørArbeidBuilder.oppdatere(aktørArbeid).medAktørId(aktørId);
    }

    /**
     * Oppretter builder for inntekter for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @param aktørId aktøren
     * @return builder {@link AktørInntektBuilder}
     */
    public AktørInntektBuilder getAktørInntektBuilder(AktørId aktørId) {
        Optional<AktørInntekt> aktørInntekt = kladd.getAktørInntekt().stream().filter(aa -> aktørId.equals(aa.getAktørId())).findFirst();
        final AktørInntektBuilder oppdatere = AktørInntektBuilder.oppdatere(aktørInntekt);
        oppdatere.medAktørId(aktørId);
        return oppdatere;
    }

    /**
     * Oppretter builder for tilstøtende ytelser for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @param aktørId aktøren
     * @return builder {@link AktørYtelseBuilder}
     */
    public AktørYtelseBuilder getAktørYtelseBuilder(AktørId aktørId) {
        Optional<AktørYtelse> aktørYtelse = kladd.getAktørYtelse().stream().filter(ay -> aktørId.equals(ay.getAktørId())).findFirst();
        return AktørYtelseBuilder.oppdatere(aktørYtelse).medAktørId(aktørId);
    }

    public InntektArbeidYtelseAggregat build() {
        return this.kladd;
    }

    VersjonType getVersjon() {
        return versjon;
    }

    void oppdaterArbeidsforholdReferanseEtterErstatting(AktørId søker, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef gammelRef,
                                                        InternArbeidsforholdRef nyRef) {
        final AktørArbeidBuilder builder = getAktørArbeidBuilder(søker);
        if (builder.getErOppdatering()) {
            if (eksistererIkkeFraFør(arbeidsgiver, gammelRef, builder)) {
                final YrkesaktivitetBuilder yrkesaktivitetBuilder = builder.getYrkesaktivitetBuilderForNøkkelAvType(
                    Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(gammelRef, arbeidsgiver),
                    ArbeidType.AA_REGISTER_TYPER);
                if (yrkesaktivitetBuilder.getErOppdatering()) {
                    yrkesaktivitetBuilder.medArbeidsforholdId(nyRef);
                    builder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
                    leggTilAktørArbeid(builder);
                }
            }
        }
    }

    private boolean eksistererIkkeFraFør(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef gammelRef, AktørArbeidBuilder builder) {
        return !builder.getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(gammelRef, arbeidsgiver),
            ArbeidType.AA_REGISTER_TYPER).getErOppdatering();
    }

    public static class AktørArbeidBuilder {
        private final AktørArbeid kladd;
        private final boolean oppdatering;

        private AktørArbeidBuilder(AktørArbeid aktørArbeid, boolean oppdatering) {
            this.kladd = aktørArbeid;
            this.oppdatering = oppdatering;
        }

        static AktørArbeidBuilder ny() {
            return new AktørArbeidBuilder(new AktørArbeid(), false);
        }

        static AktørArbeidBuilder oppdatere(AktørArbeid oppdatere) {
            return new AktørArbeidBuilder(oppdatere, true);
        }

        public static AktørArbeidBuilder oppdatere(Optional<AktørArbeid> oppdatere) {
            return oppdatere.map(AktørArbeidBuilder::oppdatere).orElseGet(AktørArbeidBuilder::ny);
        }

        public AktørArbeidBuilder medAktørId(AktørId aktørId) {
            this.kladd.setAktørId(aktørId);
            return this;
        }

        public YrkesaktivitetBuilder getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel nøkkel, ArbeidType arbeidType) {
            return kladd.getYrkesaktivitetBuilderForNøkkel(nøkkel, arbeidType);
        }

        public YrkesaktivitetBuilder getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel nøkkel, Set<ArbeidType> arbeidType) {
            return kladd.getYrkesaktivitetBuilderForNøkkel(nøkkel, arbeidType);
        }

        public YrkesaktivitetBuilder getYrkesaktivitetBuilderForType(ArbeidType type) {
            return kladd.getYrkesaktivitetBuilderForType(type);
        }

        public AktørArbeidBuilder leggTilYrkesaktivitet(YrkesaktivitetBuilder yrkesaktivitet) {
            if (!yrkesaktivitet.getErOppdatering()) {
                kladd.leggTilYrkesaktivitet(yrkesaktivitet.build());
            }
            return this;
        }

        public AktørArbeidBuilder leggTilYrkesaktivitet(Yrkesaktivitet yrkesaktivitet) {
            kladd.leggTilYrkesaktivitet(yrkesaktivitet);
            return this;
        }

        public AktørArbeid build() {
            if (kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException();
        }

        public boolean getErOppdatering() {
            return oppdatering;
        }

        public AktørArbeid getKladd() {
            return kladd;
        }

        public void fjernYrkesaktivitetHvisFinnes(YrkesaktivitetBuilder builder) {
            kladd.fjernYrkesaktivitetForBuilder(builder);
        }

        public void tilbakestillYrkesaktiviteter() {
            kladd.tilbakestillYrkesaktiviteter();
        }
    }

    public static class AktørInntektBuilder {
        private final AktørInntekt aktørInntekt;
        private final boolean oppdatering;

        private AktørInntektBuilder(AktørInntekt aktørInntekt, boolean oppdatering) {
            this.aktørInntekt = aktørInntekt;
            this.oppdatering = oppdatering;
        }

        static AktørInntektBuilder ny() {
            return new AktørInntektBuilder(new AktørInntekt(), false);
        }

        static AktørInntektBuilder oppdatere(AktørInntekt oppdatere) {
            return new AktørInntektBuilder(oppdatere, true);
        }

        public static AktørInntektBuilder oppdatere(Optional<AktørInntekt> oppdatere) {
            return oppdatere.map(AktørInntektBuilder::oppdatere).orElseGet(AktørInntektBuilder::ny);
        }

        private void medAktørId(AktørId aktørId) {
            this.aktørInntekt.setAktørId(aktørId);
        }

        public InntektBuilder getInntektBuilder(InntektskildeType inntektskildeType, Opptjeningsnøkkel opptjeningsnøkkel) {
            return aktørInntekt.getInntektBuilder(inntektskildeType, opptjeningsnøkkel);
        }

        public InntektBuilder getInntektBuilderForYtelser(InntektskildeType inntektskildeType) {
            return aktørInntekt.getInntektBuilderForYtelser(inntektskildeType);
        }

        public AktørInntektBuilder leggTilInntekt(InntektBuilder inntektBuilder) {
            if (!inntektBuilder.getErOppdatering()) {
                Inntekt inntektTmpEntitet = inntektBuilder.build();
                aktørInntekt.leggTilInntekt(inntektTmpEntitet);
            }
            return this;
        }

        public AktørInntekt build() {
            if (aktørInntekt.hasValues()) {
                return aktørInntekt;
            }
            throw new IllegalStateException();
        }

        AktørInntekt getKladd() {
            return aktørInntekt;
        }

        boolean getErOppdatering() {
            return oppdatering;
        }

        public AktørInntektBuilder fjernInntekterFraKilde(InntektskildeType inntektskildeType) {
            aktørInntekt.fjernInntekterFraKilde(inntektskildeType);
            return this;
        }
    }

    public static class AktørYtelseBuilder {
        private final AktørYtelse kladd;
        private final boolean oppdatering;

        private AktørYtelseBuilder(AktørYtelse aktørYtelse, boolean oppdatering) {
            this.kladd = aktørYtelse;
            this.oppdatering = oppdatering;
        }

        static AktørYtelseBuilder ny() {
            return new AktørYtelseBuilder(new AktørYtelse(), false);
        }

        static AktørYtelseBuilder oppdatere(AktørYtelse oppdatere) {
            return new AktørYtelseBuilder(oppdatere, true);
        }

        public static AktørYtelseBuilder oppdatere(Optional<AktørYtelse> oppdatere) {
            return oppdatere.map(AktørYtelseBuilder::oppdatere).orElseGet(AktørYtelseBuilder::ny);
        }

        boolean getErOppdatering() {
            return oppdatering;
        }

        public AktørYtelseBuilder medAktørId(AktørId aktørId) {
            this.kladd.setAktørId(aktørId);
            return this;
        }

        public YtelseBuilder getYtelselseBuilderForType(Fagsystem fagsystem, YtelseType type, Saksnummer sakId) {
            return kladd.getYtelseBuilderForType(fagsystem, type, sakId);
        }

        public YtelseBuilder getYtelselseBuilderForType(Fagsystem fagsystem, YtelseType type, Saksnummer sakId, IntervallEntitet periode,
                                                        Optional<LocalDate> tidligsteAnvistFom) {
            return kladd.getYtelseBuilderForType(fagsystem, type, sakId, periode, tidligsteAnvistFom);
        }

        public YtelseBuilder getYtelselseBuilderForType(Fagsystem fagsystem, YtelseType type, TemaUnderkategori typeKategori, IntervallEntitet periode,
                                                        Optional<LocalDate> tidligsteAnvistFom) {
            return kladd.getYtelseBuilderForType(fagsystem, type, typeKategori, periode, tidligsteAnvistFom);
        }

        public void tilbakestillYtelser() {
            kladd.tilbakestillYtelser();
        }

        public AktørYtelseBuilder leggTilYtelse(YtelseBuilder ytelseBuilder) {
            if (!ytelseBuilder.getErOppdatering()) {
                Ytelse ytelse = ytelseBuilder.build();
                this.kladd.leggTilYtelse(ytelse);
            }
            return this;
        }

        boolean harVerdi() {
            return kladd.hasValues();
        }

        public AktørYtelse build() {
            if (this.kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException("Har ikke innhold");
        }

        @Override
        public String toString() {
            return "AktørYtelseBuilder{" +
                "kladd=" + kladd +
                ", oppdatering=" + oppdatering +
                '}';
        }

    }

}
