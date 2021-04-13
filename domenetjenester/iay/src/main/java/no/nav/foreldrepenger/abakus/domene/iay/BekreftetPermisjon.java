package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import no.nav.abakus.iaygrunnlag.kodeverk.BekreftetPermisjonStatus;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.BekreftetPermisjonStatusKodeverdiConverter;

@Embeddable
public class BekreftetPermisjon {

    @Convert(converter = BekreftetPermisjonStatusKodeverdiConverter.class)
    @Column(name = "BEKREFTET_PERMISJON_STATUS", nullable = false, updatable = false)
    private BekreftetPermisjonStatus status = BekreftetPermisjonStatus.UDEFINERT;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "bekreftet_permisjon_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "bekreftet_permisjon_tom"))
    })
    private IntervallEntitet periode;

    public BekreftetPermisjon() {
    }

    public BekreftetPermisjon(LocalDate permisjonFom, LocalDate permisjonTom, BekreftetPermisjonStatus status) {
        this.periode = IntervallEntitet.fraOgMedTilOgMed(permisjonFom, permisjonTom);
        this.status = status;
    }

    public BekreftetPermisjon(BekreftetPermisjon bekreftetPermisjon) {
        this.periode = bekreftetPermisjon.getPeriode();
        this.status = bekreftetPermisjon.getStatus();
    }

    public BekreftetPermisjonStatus getStatus() {
        return status;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof BekreftetPermisjon))
            return false;
        var that = (BekreftetPermisjon) o;
        return Objects.equals(periode, that.periode)
            && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, status);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "periode=" + periode +
            ", status=" + status +
            '>';
    }

}
