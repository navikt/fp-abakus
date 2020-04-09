package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import javax.enterprise.inject.spi.CDI;

import no.nav.foreldrepenger.abakus.domene.iay.NæringsinntektType;
import no.nav.foreldrepenger.abakus.domene.iay.OffentligYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.PensjonTrygdType;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseInntektspostType;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;

final class KodeverkMapper {
    private static KodeverkRepository repository = null;

    private KodeverkMapper() {

    }

    static KodeverkRepository repository() {
        if (repository == null) {
            repository = CDI.current().select(KodeverkRepository.class).get();
        }
        return repository;
    }

    static no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseType mapYtelseTypeTilDto(YtelseInntektspostType ytelseType) {
        if (ytelseType == null || ytelseType.getKode().equals("-")) {
            return null;
        }
        switch (ytelseType.getKodeverk()) {
            case no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType.KODEVERK:
                return new no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType(ytelseType.getKode());
            case no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType.KODEVERK:
                return new no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType(ytelseType.getKode());
            case no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltPensjonTrygdType.KODEVERK:
                return new no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltPensjonTrygdType(ytelseType.getKode());
            default:
                throw new IllegalArgumentException("Ukjent YtelseType: " + ytelseType + ", kan ikke mappes til "
                    + no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseType.class.getName());
        }

    }

    public static YtelseInntektspostType mapUtbetaltYtelseTypeTilGrunnlag(no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseType type) {
        if (type == null)
            return OffentligYtelseType.UDEFINERT;
        String kode = type.getKode();
        switch (type.getKodeverk()) {
            case no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType.KODEVERK:
                return repository().finn(OffentligYtelseType.class, kode);
            case no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType.KODEVERK:
                return repository().finn(NæringsinntektType.class, kode);
            case no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltPensjonTrygdType.KODEVERK:
                return repository().finn(PensjonTrygdType.class, kode);
            default:
                throw new IllegalArgumentException("Ukjent UtbetaltYtelseType: " + type);
        }
    }

}
