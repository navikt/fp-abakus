package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;


@Converter(autoApply = true)
public class VirksomhetTypeKodeverdiConverter implements AttributeConverter<ArbeidType, String> {
    @Override
    public String convertToDatabaseColumn(ArbeidType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public ArbeidType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ArbeidType.fraKode(dbData);
    }
}