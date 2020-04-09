package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;

@Converter(autoApply = true)
public class PermisjonsbeskrivelseTypeKodeverdiConverter implements AttributeConverter<PermisjonsbeskrivelseType, String> {
    @Override
    public String convertToDatabaseColumn(PermisjonsbeskrivelseType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public PermisjonsbeskrivelseType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PermisjonsbeskrivelseType.fraKode(dbData);
    }
}