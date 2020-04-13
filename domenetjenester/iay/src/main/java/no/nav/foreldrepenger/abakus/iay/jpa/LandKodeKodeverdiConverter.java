package no.nav.foreldrepenger.abakus.iay.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;

@Converter(autoApply = true)
public class LandKodeKodeverdiConverter implements AttributeConverter<Landkode, String> {
    @Override
    public String convertToDatabaseColumn(Landkode attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public Landkode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Landkode.fraKode(dbData);
    }
}