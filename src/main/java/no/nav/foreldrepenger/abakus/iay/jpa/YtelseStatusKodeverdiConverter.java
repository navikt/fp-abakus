package no.nav.foreldrepenger.abakus.iay.jpa;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class YtelseStatusKodeverdiConverter implements AttributeConverter<YtelseStatus, String> {
    @Override
    public String convertToDatabaseColumn(YtelseStatus attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public YtelseStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : YtelseStatus.fraKode(dbData);
    }
}
