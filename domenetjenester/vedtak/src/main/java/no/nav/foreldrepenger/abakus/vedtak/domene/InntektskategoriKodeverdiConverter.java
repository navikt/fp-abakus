package no.nav.foreldrepenger.abakus.vedtak.domene;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;

@Converter(autoApply = true)
public class InntektskategoriKodeverdiConverter implements AttributeConverter<Inntektskategori, String> {
    @Override
    public String convertToDatabaseColumn(Inntektskategori attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public Inntektskategori convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Inntektskategori.fraKode(dbData);
    }
}
