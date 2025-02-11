package no.nav.foreldrepenger.abakus.vedtak.domene;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;

@Converter(autoApply = true)
class YtelseStatusKodeverdiConverter implements AttributeConverter<YtelseStatus, String> {
    @Override
    public String convertToDatabaseColumn(YtelseStatus attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public YtelseStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : YtelseStatus.fraKode(dbData);
    }
}
