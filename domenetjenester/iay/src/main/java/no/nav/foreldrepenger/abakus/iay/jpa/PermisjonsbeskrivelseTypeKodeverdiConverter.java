package no.nav.foreldrepenger.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;

@Converter(autoApply = true)
public class PermisjonsbeskrivelseTypeKodeverdiConverter
        implements AttributeConverter<PermisjonsbeskrivelseType, String> {
    @Override
    public String convertToDatabaseColumn(PermisjonsbeskrivelseType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public PermisjonsbeskrivelseType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PermisjonsbeskrivelseType.fraKode(dbData);
    }
}
