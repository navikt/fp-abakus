package no.nav.foreldrepenger.abakus.iay.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;

@Converter(autoApply = true)
public class ArbeidTypeKodeverdiConverter implements AttributeConverter<ArbeidType, String> {
    @Override
    public String convertToDatabaseColumn(ArbeidType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public ArbeidType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ArbeidType.fraKode(dbData);
    }
}