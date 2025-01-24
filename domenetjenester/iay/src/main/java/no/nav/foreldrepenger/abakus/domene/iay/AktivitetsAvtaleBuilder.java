package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class AktivitetsAvtaleBuilder {
    private final AktivitetsAvtale aktivitetsAvtaleEntitet;
    private boolean oppdatering;

    AktivitetsAvtaleBuilder(AktivitetsAvtale aktivitetsAvtaleEntitet, boolean oppdatering) {
        this.aktivitetsAvtaleEntitet = aktivitetsAvtaleEntitet;
        this.oppdatering = oppdatering;
    }

    public static AktivitetsAvtaleBuilder ny() {
        return new AktivitetsAvtaleBuilder(new AktivitetsAvtale(), false);
    }

    static AktivitetsAvtaleBuilder oppdater(Optional<AktivitetsAvtale> aktivitetsAvtale) {
        return new AktivitetsAvtaleBuilder(
                aktivitetsAvtale.orElseGet(AktivitetsAvtale::new), aktivitetsAvtale.isPresent());
    }

    public AktivitetsAvtaleBuilder medProsentsats(Stillingsprosent prosentsats) {
        this.aktivitetsAvtaleEntitet.setProsentsats(prosentsats);
        return this;
    }

    public AktivitetsAvtaleBuilder medProsentsats(BigDecimal prosentsats) {
        this.aktivitetsAvtaleEntitet.setProsentsats(
                prosentsats == null ? Stillingsprosent.nullProsent() : Stillingsprosent.arbeid(prosentsats));
        return this;
    }

    public AktivitetsAvtaleBuilder medPeriode(IntervallEntitet periode) {
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
