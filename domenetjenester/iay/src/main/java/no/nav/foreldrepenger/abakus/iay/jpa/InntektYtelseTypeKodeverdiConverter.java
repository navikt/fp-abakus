package no.nav.foreldrepenger.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektYtelseType;


@Converter(autoApply = true)
public class InntektYtelseTypeKodeverdiConverter implements AttributeConverter<InntektYtelseType, String> {

    private static final String UDEFINERT_YTELSE = "-";

    @Override
    public String convertToDatabaseColumn(InntektYtelseType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public InntektYtelseType convertToEntityAttribute(String dbData) {
        return dbData == null || UDEFINERT_YTELSE.equals(dbData) ? null : InntektYtelseType.fraKode(dbData);
    }
}
