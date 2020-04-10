package no.nav.foreldrepenger.abakus.iay.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.OrganisasjonType;

@Converter(autoApply = true)
public class OrganisasjonstypeKodeverdiConverter implements AttributeConverter<OrganisasjonType, String> {
    @Override
    public String convertToDatabaseColumn(OrganisasjonType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public OrganisasjonType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : OrganisasjonType.fraKode(dbData);
    }
}