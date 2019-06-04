package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Embeddable
public class BekreftetPermisjon {

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "PERMISJON_BRUK")
    private boolean bruk;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fomDato", column = @Column(name = "PERMISJON_FOM")),
            @AttributeOverride(name = "tomDato", column = @Column(name = "PERMISJON_TOM"))
    })
    private DatoIntervallEntitet periode;

    BekreftetPermisjon() {
    }

    public BekreftetPermisjon(LocalDate permisjonFom, LocalDate permisjonTom, boolean bruk){
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(permisjonFom, permisjonTom);
        this.bruk = bruk;
    }
        
    public BekreftetPermisjon(BekreftetPermisjon bekreftetPermisjonEntitet) {
        this.periode = bekreftetPermisjonEntitet.getBekreftetPermisjonPeriode();
        this.bruk = bekreftetPermisjonEntitet.getBruk();
    }

    public DatoIntervallEntitet getBekreftetPermisjonPeriode() {
        return periode;
    }

    public boolean getBruk() {
        return bruk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BekreftetPermisjon that = (BekreftetPermisjon) o;
        return Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "periode=" + periode +
            ", bruk=" + bruk +
            '>';
    }

}
