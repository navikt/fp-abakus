package no.nav.foreldrepenger.abakus.iay.jpa;

import no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class TemaUnderkategoriKodeverdiConverter implements AttributeConverter<TemaUnderkategori, String> {
    @Override
    public String convertToDatabaseColumn(TemaUnderkategori attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public TemaUnderkategori convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TemaUnderkategori.fraKode(dbData);
    }
}
