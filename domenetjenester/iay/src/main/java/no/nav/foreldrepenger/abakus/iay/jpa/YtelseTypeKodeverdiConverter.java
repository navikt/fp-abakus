package no.nav.foreldrepenger.abakus.iay.jpa;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class YtelseTypeKodeverdiConverter implements AttributeConverter<YtelseType, String> {
    @Override
    public String convertToDatabaseColumn(YtelseType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public YtelseType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : YtelseType.fraKode(dbData);
    }
}
