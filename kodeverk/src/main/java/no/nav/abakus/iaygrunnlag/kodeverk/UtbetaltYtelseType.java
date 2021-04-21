package no.nav.abakus.iaygrunnlag.kodeverk;

public interface UtbetaltYtelseType extends Kodeverdi {

    public static UtbetaltYtelseType getUtbetaltYtelseType(String kode, String kodeverk) {
        return switch (kodeverk) {
            case UtbetaltYtelseFraOffentligeType.KODEVERK -> UtbetaltYtelseFraOffentligeType.fraKode(kode);
            case UtbetaltNæringsYtelseType.KODEVERK -> UtbetaltNæringsYtelseType.fraKode(kode);
            case UtbetaltPensjonTrygdType.KODEVERK -> UtbetaltPensjonTrygdType.fraKode(kode);
            default -> null;
        };
    }

    public boolean erUdefinert();


}
