package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class PermisjonBuilder {
    private final PermisjonEntitet permisjonEntitet;

    PermisjonBuilder(PermisjonEntitet permisjonEntitet) {
        this.permisjonEntitet = permisjonEntitet;
    }

    static PermisjonBuilder ny() {
        return new PermisjonBuilder(new PermisjonEntitet());
    }

    public PermisjonBuilder medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
        this.permisjonEntitet.setPermisjonsbeskrivelseType(permisjonsbeskrivelseType);
        return this;
    }

    public PermisjonBuilder medProsentsats(BigDecimal prosentsats) {
        this.permisjonEntitet.setProsentsats(new Stillingsprosent(prosentsats));
        return this;
    }

    public PermisjonBuilder medPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
        this.permisjonEntitet.setPeriode(fraOgMed, tilOgMed);
        return this;
    }

    public Permisjon build() {
        if (permisjonEntitet.hasValues()) {
            return permisjonEntitet;
        }
        throw new IllegalStateException();
    }

    public PermisjonBuilder medPermisjonsbeskrivelseType(String kode) {
        return medPermisjonsbeskrivelseType(new PermisjonsbeskrivelseType(kode));
    }
}