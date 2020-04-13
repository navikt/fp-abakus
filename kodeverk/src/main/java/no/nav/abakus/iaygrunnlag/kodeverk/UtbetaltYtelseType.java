package no.nav.abakus.iaygrunnlag.kodeverk;

public interface UtbetaltYtelseType extends Kodeverdi {

    public static UtbetaltYtelseType getUtbetaltYtelseType(String kode, String kodeverk) {
        switch (kodeverk) {
            case UtbetaltYtelseFraOffentligeType.KODEVERK:
                return UtbetaltYtelseFraOffentligeType.fraKode(kode);
            case UtbetaltNæringsYtelseType.KODEVERK:
                return UtbetaltNæringsYtelseType.fraKode(kode);
            case UtbetaltPensjonTrygdType.KODEVERK:
                return UtbetaltPensjonTrygdType.fraKode(kode);
            default:
                return null;

        }
    }
}
