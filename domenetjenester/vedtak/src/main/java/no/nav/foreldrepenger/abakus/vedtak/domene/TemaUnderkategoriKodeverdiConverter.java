package no.nav.foreldrepenger.abakus.vedtak.domene;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori;

@Converter(autoApply = true)
class TemaUnderkategoriKodeverdiConverter implements AttributeConverter<TemaUnderkategori, String> {
    @Override
    public String convertToDatabaseColumn(TemaUnderkategori attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public TemaUnderkategori convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TemaUnderkategori.fraKode(dbData);
    }
}