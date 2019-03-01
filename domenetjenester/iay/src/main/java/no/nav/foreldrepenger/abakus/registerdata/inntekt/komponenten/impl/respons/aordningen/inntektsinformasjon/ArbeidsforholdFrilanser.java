package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

import java.time.LocalDate;

public class ArbeidsforholdFrilanser {
    private Double antallTimerPerUkeSomEnFullStillingTilsvarer;
    private String arbeidstidsordning;
    private String avloenningstype;
    private LocalDate sisteDatoForStillingsprosentendring;
    private LocalDate sisteLoennsendring;
    private LocalDate frilansPeriodeFom;
    private LocalDate frilansPeriodeTom;
    private Double stillingsprosent;
    private String yrke;
    private String arbeidsforholdID;
    private String arbeidsforholdIDnav;
    private String arbeidsforholdstype;
    private Aktoer arbeidsgiver;
    private Aktoer arbeidstaker;


    public Double getAntallTimerPerUkeSomEnFullStillingTilsvarer() {
        return this.antallTimerPerUkeSomEnFullStillingTilsvarer;
    }

    public String getArbeidstidsordning() {
        return this.arbeidstidsordning;
    }

    public String getAvloenningstype() {
        return this.avloenningstype;
    }

    public LocalDate getSisteDatoForStillingsprosentendring() {
        return this.sisteDatoForStillingsprosentendring;
    }

    public LocalDate getSisteLoennsendring() {
        return this.sisteLoennsendring;
    }

    public LocalDate getFrilansPeriodeFom() {
        return this.frilansPeriodeFom;
    }

    public LocalDate getFrilansPeriodeTom() {
        return this.frilansPeriodeTom;
    }

    public Double getStillingsprosent() {
        return this.stillingsprosent;
    }

    public String getYrke() {
        return this.yrke;
    }

    public String getArbeidsforholdID() {
        return this.arbeidsforholdID;
    }

    public String getArbeidsforholdIDnav() {
        return this.arbeidsforholdIDnav;
    }

    public String getArbeidsforholdstype() {
        return this.arbeidsforholdstype;
    }

    public Aktoer getArbeidsgiver() {
        return this.arbeidsgiver;
    }

    public Aktoer getArbeidstaker() {
        return this.arbeidstaker;
    }

    public void setAntallTimerPerUkeSomEnFullStillingTilsvarer(Double antallTimerPerUkeSomEnFullStillingTilsvarer) {
        this.antallTimerPerUkeSomEnFullStillingTilsvarer = antallTimerPerUkeSomEnFullStillingTilsvarer;
    }

    public void setArbeidstidsordning(String arbeidstidsordning) {
        this.arbeidstidsordning = arbeidstidsordning;
    }

    public void setAvloenningstype(String avloenningstype) {
        this.avloenningstype = avloenningstype;
    }

    public void setSisteDatoForStillingsprosentendring(LocalDate sisteDatoForStillingsprosentendring) {
        this.sisteDatoForStillingsprosentendring = sisteDatoForStillingsprosentendring;
    }

    public void setSisteLoennsendring(LocalDate sisteLoennsendring) {
        this.sisteLoennsendring = sisteLoennsendring;
    }

    public void setFrilansPeriodeFom(LocalDate frilansPeriodeFom) {
        this.frilansPeriodeFom = frilansPeriodeFom;
    }

    public void setFrilansPeriodeTom(LocalDate frilansPeriodeTom) {
        this.frilansPeriodeTom = frilansPeriodeTom;
    }

    public void setStillingsprosent(Double stillingsprosent) {
        this.stillingsprosent = stillingsprosent;
    }

    public void setYrke(String yrke) {
        this.yrke = yrke;
    }

    public void setArbeidsforholdID(String arbeidsforholdID) {
        this.arbeidsforholdID = arbeidsforholdID;
    }

    public void setArbeidsforholdIDnav(String arbeidsforholdIDnav) {
        this.arbeidsforholdIDnav = arbeidsforholdIDnav;
    }

    public void setArbeidsforholdstype(String arbeidsforholdstype) {
        this.arbeidsforholdstype = arbeidsforholdstype;
    }

    public void setArbeidsgiver(Aktoer arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public void setArbeidstaker(Aktoer arbeidstaker) {
        this.arbeidstaker = arbeidstaker;
    }

    public ArbeidsforholdFrilanser() {
    }

    public ArbeidsforholdFrilanser(Double antallTimerPerUkeSomEnFullStillingTilsvarer, String arbeidstidsordning, String avloenningstype, LocalDate sisteDatoForStillingsprosentendring, LocalDate sisteLoennsendring, LocalDate frilansPeriodeFom, LocalDate frilansPeriodeTom, Double stillingsprosent, String yrke, String arbeidsforholdID, String arbeidsforholdIDnav, String arbeidsforholdstype, Aktoer arbeidsgiver, Aktoer arbeidstaker) {
        this.antallTimerPerUkeSomEnFullStillingTilsvarer = antallTimerPerUkeSomEnFullStillingTilsvarer;
        this.arbeidstidsordning = arbeidstidsordning;
        this.avloenningstype = avloenningstype;
        this.sisteDatoForStillingsprosentendring = sisteDatoForStillingsprosentendring;
        this.sisteLoennsendring = sisteLoennsendring;
        this.frilansPeriodeFom = frilansPeriodeFom;
        this.frilansPeriodeTom = frilansPeriodeTom;
        this.stillingsprosent = stillingsprosent;
        this.yrke = yrke;
        this.arbeidsforholdID = arbeidsforholdID;
        this.arbeidsforholdIDnav = arbeidsforholdIDnav;
        this.arbeidsforholdstype = arbeidsforholdstype;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidstaker = arbeidstaker;
    }
}
