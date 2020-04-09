package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;

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