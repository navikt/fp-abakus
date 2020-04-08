package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.BekreftetPermisjonStatus;


@Converter(autoApply = true)
public class BekreftetPermisjonStatusKodeverdiConverter implements AttributeConverter<BekreftetPermisjonStatus, String> {
    @Override
    public String convertToDatabaseColumn(BekreftetPermisjonStatus attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public BekreftetPermisjonStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BekreftetPermisjonStatus.fraKode(dbData);
    }
}