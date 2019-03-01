package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.inntekt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.Aktoer;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.Tilleggsinformasjon;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "inntektType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Loennsinntekt.class, name = Loennsinntekt.INNTEKT_TYPE),
    @JsonSubTypes.Type(value = Naeringsinntekt.class, name = Naeringsinntekt.INNTEKT_TYPE),
    @JsonSubTypes.Type(value = PensjonEllerTrygd.class, name = PensjonEllerTrygd.INNTEKT_TYPE),
    @JsonSubTypes.Type(value = YtelseFraOffentlige.class, name = YtelseFraOffentlige.INNTEKT_TYPE),
})
public abstract class Inntekt {
    private final InntektType inntektType;
    private String arbeidsforholdREF;
    private BigDecimal beloep;
    private String fordel;
    private String inntektskilde;
    private String inntektsperiodetype;
    private String inntektsstatus;
    private YearMonth leveringstidspunkt;
    private String opptjeningsland;
    private LocalDate opptjeningsperiodeFom;
    private LocalDate opptjeningsperiodeTom;
    private String skattemessigBosattLand;
    private YearMonth utbetaltIMaaned;
    private Aktoer opplysningspliktig;
    private Aktoer virksomhet;
    private Tilleggsinformasjon tilleggsinformasjon;
    private Aktoer inntektsmottaker;
    private Boolean inngaarIGrunnlagForTrekk;
    private Boolean utloeserArbeidsgiveravgift;
    private String informasjonsstatus;
    private String beskrivelse;
    private String skatteOgAvgiftsregel;

    public Inntekt(InntektType inntektType) {
        this.inntektType = inntektType;
    }

    public InntektType getInntektType() {
        return this.inntektType;
    }

    public String getArbeidsforholdREF() {
        return this.arbeidsforholdREF;
    }

    public void setArbeidsforholdREF(String arbeidsforholdREF) {
        this.arbeidsforholdREF = arbeidsforholdREF;
    }

    public BigDecimal getBeloep() {
        return this.beloep;
    }

    public void setBeloep(BigDecimal beloep) {
        this.beloep = beloep;
    }

    public String getFordel() {
        return this.fordel;
    }

    public void setFordel(String fordel) {
        this.fordel = fordel;
    }

    public String getInntektskilde() {
        return this.inntektskilde;
    }

    public void setInntektskilde(String inntektskilde) {
        this.inntektskilde = inntektskilde;
    }

    public String getInntektsperiodetype() {
        return this.inntektsperiodetype;
    }

    public void setInntektsperiodetype(String inntektsperiodetype) {
        this.inntektsperiodetype = inntektsperiodetype;
    }

    public String getInntektsstatus() {
        return this.inntektsstatus;
    }

    public void setInntektsstatus(String inntektsstatus) {
        this.inntektsstatus = inntektsstatus;
    }

    public YearMonth getLeveringstidspunkt() {
        return this.leveringstidspunkt;
    }

    public void setLeveringstidspunkt(YearMonth leveringstidspunkt) {
        this.leveringstidspunkt = leveringstidspunkt;
    }

    public String getOpptjeningsland() {
        return this.opptjeningsland;
    }

    public void setOpptjeningsland(String opptjeningsland) {
        this.opptjeningsland = opptjeningsland;
    }

    public LocalDate getOpptjeningsperiodeFom() {
        return this.opptjeningsperiodeFom;
    }

    public void setOpptjeningsperiodeFom(LocalDate opptjeningsperiodeFom) {
        this.opptjeningsperiodeFom = opptjeningsperiodeFom;
    }

    public LocalDate getOpptjeningsperiodeTom() {
        return this.opptjeningsperiodeTom;
    }

    public void setOpptjeningsperiodeTom(LocalDate opptjeningsperiodeTom) {
        this.opptjeningsperiodeTom = opptjeningsperiodeTom;
    }

    public String getSkattemessigBosattLand() {
        return this.skattemessigBosattLand;
    }

    public void setSkattemessigBosattLand(String skattemessigBosattLand) {
        this.skattemessigBosattLand = skattemessigBosattLand;
    }

    public YearMonth getUtbetaltIMaaned() {
        return this.utbetaltIMaaned;
    }

    public void setUtbetaltIMaaned(YearMonth utbetaltIMaaned) {
        this.utbetaltIMaaned = utbetaltIMaaned;
    }

    public Aktoer getOpplysningspliktig() {
        return this.opplysningspliktig;
    }

    public void setOpplysningspliktig(Aktoer opplysningspliktig) {
        this.opplysningspliktig = opplysningspliktig;
    }

    public Aktoer getVirksomhet() {
        return this.virksomhet;
    }

    public void setVirksomhet(Aktoer virksomhet) {
        this.virksomhet = virksomhet;
    }

    public Tilleggsinformasjon getTilleggsinformasjon() {
        return this.tilleggsinformasjon;
    }

    public void setTilleggsinformasjon(Tilleggsinformasjon tilleggsinformasjon) {
        this.tilleggsinformasjon = tilleggsinformasjon;
    }

    public Aktoer getInntektsmottaker() {
        return this.inntektsmottaker;
    }

    public void setInntektsmottaker(Aktoer inntektsmottaker) {
        this.inntektsmottaker = inntektsmottaker;
    }

    public Boolean getInngaarIGrunnlagForTrekk() {
        return this.inngaarIGrunnlagForTrekk;
    }

    public void setInngaarIGrunnlagForTrekk(Boolean inngaarIGrunnlagForTrekk) {
        this.inngaarIGrunnlagForTrekk = inngaarIGrunnlagForTrekk;
    }

    public Boolean getUtloeserArbeidsgiveravgift() {
        return this.utloeserArbeidsgiveravgift;
    }

    public void setUtloeserArbeidsgiveravgift(Boolean utloeserArbeidsgiveravgift) {
        this.utloeserArbeidsgiveravgift = utloeserArbeidsgiveravgift;
    }

    public String getInformasjonsstatus() {
        return this.informasjonsstatus;
    }

    public void setInformasjonsstatus(String informasjonsstatus) {
        this.informasjonsstatus = informasjonsstatus;
    }

    public String getBeskrivelse() {
        return this.beskrivelse;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public String getSkatteOgAvgiftsregel() {
        return this.skatteOgAvgiftsregel;
    }

    public void setSkatteOgAvgiftsregel(String skatteOgAvgiftsregel) {
        this.skatteOgAvgiftsregel = skatteOgAvgiftsregel;
    }

}
