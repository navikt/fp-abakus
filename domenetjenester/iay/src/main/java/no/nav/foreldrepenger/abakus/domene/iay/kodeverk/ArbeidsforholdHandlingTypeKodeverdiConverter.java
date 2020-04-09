package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType;

@Converter(autoApply = true)
public class ArbeidsforholdHandlingTypeKodeverdiConverter implements AttributeConverter<ArbeidsforholdHandlingType, String> {
    @Override
    public String convertToDatabaseColumn(ArbeidsforholdHandlingType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public ArbeidsforholdHandlingType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ArbeidsforholdHandlingType.fraKode(dbData);
    }
}