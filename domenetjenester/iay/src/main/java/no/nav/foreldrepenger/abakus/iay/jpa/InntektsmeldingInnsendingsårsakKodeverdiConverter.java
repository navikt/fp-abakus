package no.nav.foreldrepenger.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType;

@Converter(autoApply = true)
public class InntektsmeldingInnsendingsårsakKodeverdiConverter
        implements AttributeConverter<InntektsmeldingInnsendingsårsakType, String> {
    @Override
    public String convertToDatabaseColumn(InntektsmeldingInnsendingsårsakType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public InntektsmeldingInnsendingsårsakType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InntektsmeldingInnsendingsårsakType.fraKode(dbData);
    }
}
