package no.nav.foreldrepenger.abakus.iay.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.LønnsinntektBeskrivelse;


@Converter(autoApply = true)
public class LønnsbeskrivelseKodeverdiConverter implements AttributeConverter<LønnsinntektBeskrivelse, String> {
    @Override
    public String convertToDatabaseColumn(LønnsinntektBeskrivelse attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public LønnsinntektBeskrivelse convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LønnsinntektBeskrivelse.fraKode(dbData);
    }
}
