package no.nav.foreldrepenger.abakus.vedtak.domene;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;

@Converter(autoApply = true)
public class FagsystemKodeverdiConverter implements AttributeConverter<Fagsystem, String> {
    @Override
    public String convertToDatabaseColumn(Fagsystem attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public Fagsystem convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Fagsystem.fraKode(dbData);
    }
}