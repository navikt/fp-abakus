package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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