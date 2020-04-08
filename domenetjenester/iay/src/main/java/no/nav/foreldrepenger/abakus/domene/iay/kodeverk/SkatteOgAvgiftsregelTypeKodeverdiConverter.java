package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;


@Converter(autoApply = true)
public class SkatteOgAvgiftsregelTypeKodeverdiConverter implements AttributeConverter<SkatteOgAvgiftsregelType, String> {
    @Override
    public String convertToDatabaseColumn(SkatteOgAvgiftsregelType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public SkatteOgAvgiftsregelType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SkatteOgAvgiftsregelType.fraKode(dbData);
    }
}