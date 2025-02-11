package no.nav.foreldrepenger.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;

@Converter(autoApply = true)
public class InntektspostTypeKodeverdiConverter implements AttributeConverter<InntektspostType, String> {
    @Override
    public String convertToDatabaseColumn(InntektspostType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public InntektspostType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InntektspostType.fraKode(dbData);
    }
}
