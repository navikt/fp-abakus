package no.nav.foreldrepenger.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;

@Converter(autoApply = true)
public class ArbeidskategoriKodeverdiConverter implements AttributeConverter<Arbeidskategori, String> {
    @Override
    public String convertToDatabaseColumn(Arbeidskategori attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public Arbeidskategori convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Arbeidskategori.fraKode(dbData);
    }
}
