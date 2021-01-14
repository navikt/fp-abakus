package no.nav.foreldrepenger.abakus.lonnskomp.domene;

import java.util.Objects;

import javax.persistence.AttributeConverter;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

@Entity(name = "LonnskompFilterEntitet")
@Table(name = "LONNSKOMP_FILTER")
public class LønnskompensasjonFilter extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_LONNSKOMP_FILTER")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer", nullable = false, updatable = false)))
    private Saksnummer saksnummer;

    @Convert(converter = LocalInntektsKildeKodeverdiConverter.class)
    @Column(name = "kilde", nullable = false, updatable = false)
    private InntektskildeType inntektskildeType;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public LønnskompensasjonFilter() {
        // hibernate
    }

    public LønnskompensasjonFilter(Saksnummer saksnummer, InntektskildeType inntektskildeType) {
        this.saksnummer = saksnummer;
        this.inntektskildeType = inntektskildeType;
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { this.saksnummer, this.inntektskildeType };
        return IndexKeyComposer.createKey(keyParts);
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public InntektskildeType getInntektskildeType() {
        return inntektskildeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LønnskompensasjonFilter that = (LønnskompensasjonFilter) o;
        return Objects.equals(saksnummer, that.saksnummer) && inntektskildeType == that.inntektskildeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(saksnummer, inntektskildeType);
    }

    @Converter(autoApply = true)
    private static class LocalInntektsKildeKodeverdiConverter implements AttributeConverter<InntektskildeType, String> {
        @Override
        public String convertToDatabaseColumn(InntektskildeType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public InntektskildeType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : InntektskildeType.fraKode(dbData);
        }
    }
}
