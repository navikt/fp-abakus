package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

public class MeldekortUtbetalingsgrunnlagSak {

    private List<MeldekortUtbetalingsgrunnlagMeldekort> meldekortene;
    private YtelseType type;
    private YtelseStatus tilstand;
    private Fagsystem kilde;
    private Saksnummer saksnummer;
    private String sakStatus;
    private String vedtakStatus;
    private LocalDate kravMottattDato;
    private LocalDate vedtattDato;
    private LocalDate vedtaksPeriodeFom;
    private LocalDate vedtaksPeriodeTom;
    private Beløp vedtaksDagsats;

    private MeldekortUtbetalingsgrunnlagSak() { // NOSONAR
    }

    public YtelseType getYtelseType() {
        return type;
    }

    public YtelseStatus getYtelseTilstand() {
        return tilstand;
    }

    public Fagsystem getKilde() {
        return kilde;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public String getSakStatus() {
        return sakStatus;
    }

    public String getVedtakStatus() {
        return vedtakStatus;
    }

    public LocalDate getKravMottattDato() {
        return kravMottattDato;
    }

    public LocalDate getVedtattDato() {
        return vedtattDato;
    }

    public LocalDate getVedtaksPeriodeFom() {
        return vedtaksPeriodeFom;
    }

    public LocalDate getVedtaksPeriodeTom() {
        return vedtaksPeriodeTom;
    }

    public Beløp getVedtaksDagsats() {
        return vedtaksDagsats;
    }

    public List<MeldekortUtbetalingsgrunnlagMeldekort> getMeldekortene() {
        return meldekortene;
    }

    public static class MeldekortSakBuilder {
        private final MeldekortUtbetalingsgrunnlagSak sak;

        MeldekortSakBuilder(MeldekortUtbetalingsgrunnlagSak sak) {
            this.sak = sak;
            sak.meldekortene = new ArrayList<>();
        }

        public static MeldekortSakBuilder ny() {
            return new MeldekortSakBuilder(new MeldekortUtbetalingsgrunnlagSak());
        }

        public MeldekortSakBuilder medType(YtelseType type) {
            this.sak.type = type;
            return this;
        }

        public MeldekortSakBuilder medTilstand(YtelseStatus tilstand) {
            this.sak.tilstand = tilstand;
            return this;
        }

        public MeldekortSakBuilder medKilde(Fagsystem kilde) {
            this.sak.kilde = kilde;
            return this;
        }

        public MeldekortSakBuilder medSaksnummer(Saksnummer saksnummer) {
            this.sak.saksnummer = saksnummer;
            return this;
        }

        public MeldekortSakBuilder medSakStatus(String sakStatus) {
            this.sak.sakStatus = sakStatus;
            return this;
        }

        public MeldekortSakBuilder medVedtakStatus(String vedtakStatus) {
            this.sak.vedtakStatus = vedtakStatus;
            return this;
        }

        public MeldekortSakBuilder medKravMottattDato(LocalDate kravMottattDato) {
            this.sak.kravMottattDato = kravMottattDato;
            return this;
        }

        public MeldekortSakBuilder medVedtattDato(LocalDate vedtattDato) {
            this.sak.vedtattDato = vedtattDato;
            return this;
        }

        public MeldekortSakBuilder medVedtaksPeriodeFom(LocalDate vedtaksPeriodeFom) {
            this.sak.vedtaksPeriodeFom = vedtaksPeriodeFom;
            return this;
        }

        public MeldekortSakBuilder medVedtaksPeriodeTom(LocalDate vedtaksPeriodeTom) {
            this.sak.vedtaksPeriodeTom = vedtaksPeriodeTom;
            return this;
        }

        public MeldekortSakBuilder medVedtaksDagsats(Beløp vedtaksDagsats) {
            this.sak.vedtaksDagsats = vedtaksDagsats;
            return this;
        }
        public MeldekortSakBuilder medVedtaksDagsats(BigDecimal vedtaksDagsats) {
            this.sak.vedtaksDagsats = new Beløp(vedtaksDagsats);
            return this;
        }

        public MeldekortSakBuilder leggTilMeldekort(List<MeldekortUtbetalingsgrunnlagMeldekort> meldekortene) {
            this.sak.meldekortene.addAll(meldekortene);
            return this;
        }


        public MeldekortUtbetalingsgrunnlagSak build() {
            return this.sak;
        }

    }
}
