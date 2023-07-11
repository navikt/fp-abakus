package no.nav.foreldrepenger.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;

@Converter(autoApply = true)
public class InntektsKildeKodeverdiConverter implements AttributeConverter<InntektskildeType, String> {
    @Override
    public String convertToDatabaseColumn(InntektskildeType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public InntektskildeType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InntektskildeType.fraKode(dbData);
    }
}
