package no.nav.foreldrepenger.abakus.iay.jpa;

import no.nav.abakus.iaygrunnlag.kodeverk.UtsettelseÅrsakType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter(autoApply = true)
public class UtsettelseÅrsakTypeKodeverdiConverter implements AttributeConverter<UtsettelseÅrsakType, String> {
    @Override
    public String convertToDatabaseColumn(UtsettelseÅrsakType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public UtsettelseÅrsakType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : UtsettelseÅrsakType.fraKode(dbData);
    }
}
