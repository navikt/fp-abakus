package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.UtsettelseÅrsakType;


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