package no.nav.foreldrepenger.abakus.kobling;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

@Converter(autoApply = true)
class YtelseTypeKodeverdiConverter implements AttributeConverter<YtelseType, String> {
    @Override
    public String convertToDatabaseColumn(YtelseType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public YtelseType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : YtelseType.fraKode(dbData);
    }
}