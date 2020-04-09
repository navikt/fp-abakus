package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.VirksomhetType;


@Converter(autoApply = true)
public class VirksomhetTypeKodeverdiConverter implements AttributeConverter<VirksomhetType, String> {
    @Override
    public String convertToDatabaseColumn(VirksomhetType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public VirksomhetType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : VirksomhetType.fraKode(dbData);
    }
}