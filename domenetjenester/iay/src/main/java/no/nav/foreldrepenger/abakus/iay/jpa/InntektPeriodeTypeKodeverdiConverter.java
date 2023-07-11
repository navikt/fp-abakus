package no.nav.foreldrepenger.abakus.iay.jpa;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InntektPeriodeTypeKodeverdiConverter implements AttributeConverter<InntektPeriodeType, String> {
    @Override
    public String convertToDatabaseColumn(InntektPeriodeType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public InntektPeriodeType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InntektPeriodeType.fraKode(dbData);
    }
}
