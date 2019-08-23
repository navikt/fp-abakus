package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.typer.AntallTimer;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class AktivitetsAvtaleBuilder {
    private final AktivitetsAvtaleEntitet aktivitetsAvtaleEntitet;
    private boolean oppdatering = false;

    AktivitetsAvtaleBuilder(AktivitetsAvtale aktivitetsAvtaleEntitet, boolean oppdatering) {
        this.aktivitetsAvtaleEntitet = (AktivitetsAvtaleEntitet) aktivitetsAvtaleEntitet; // NOSONAR
        this.oppdatering = oppdatering;
    }

    public static AktivitetsAvtaleBuilder ny() {
        return new AktivitetsAvtaleBuilder(new AktivitetsAvtaleEntitet(), false);
    }

    static AktivitetsAvtaleBuilder oppdater(Optional<AktivitetsAvtale> aktivitetsAvtale) {
        return new AktivitetsAvtaleBuilder(aktivitetsAvtale.orElse(new AktivitetsAvtaleEntitet()), aktivitetsAvtale.isPresent());
    }

    public AktivitetsAvtaleBuilder medProsentsats(Stillingsprosent prosentsats) {
        this.aktivitetsAvtaleEntitet.setProsentsats(prosentsats);
        return this;
    }

    public AktivitetsAvtaleBuilder medProsentsats(BigDecimal prosentsats) {
        this.aktivitetsAvtaleEntitet.setProsentsats(prosentsats == null ? null : new Stillingsprosent(prosentsats));
        return this;
    }
    
    public AktivitetsAvtaleBuilder medAntallTimer(BigDecimal antallTimer) {
        this.aktivitetsAvtaleEntitet.setAntallTimer(antallTimer == null ? null : new AntallTimer(antallTimer));
        return this;
    }

    public AktivitetsAvtaleBuilder medAntallTimerFulltid(BigDecimal antallTimerFulltid) {
        this.aktivitetsAvtaleEntitet.setAntallTimerFulltid(antallTimerFulltid == null ? null : new AntallTimer(antallTimerFulltid));
        return this;
    }

    public AktivitetsAvtaleBuilder medPeriode(DatoIntervallEntitet periode) {
        this.aktivitetsAvtaleEntitet.setPeriode(periode);
        return this;
    }

    public AktivitetsAvtaleBuilder medBeskrivelse(String begrunnelse) {
        this.aktivitetsAvtaleEntitet.setBeskrivelse(begrunnelse);
        return this;
    }

    public AktivitetsAvtale build() {
        if (aktivitetsAvtaleEntitet.hasValues()) {
            return aktivitetsAvtaleEntitet;
        }
        throw new IllegalStateException();
    }

    public boolean isOppdatering() {
        return oppdatering;
    }

    public AktivitetsAvtaleBuilder medSisteLønnsendringsdato(LocalDate sisteLønnsendringsdato) {
        this.aktivitetsAvtaleEntitet.sisteLønnsendringsdato(sisteLønnsendringsdato);
        return this;
    }
}