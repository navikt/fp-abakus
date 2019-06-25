package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;

import no.nav.foreldrepenger.abakus.domene.iay.NæringsinntektType;
import no.nav.foreldrepenger.abakus.domene.iay.OffentligYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.PensjonTrygdType;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.NaturalYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.UtsettelseÅrsak;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.BekreftetPermisjonStatus;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;

final class KodeverkMapper {
    private static final Map<String, String> YTELSETYPE_FPSAK_TIL_ABAKUS;
    private static final Map<String, String> YTELSETYPE_ABAKUS_TIL_FPSAK;
    private static KodeverkRepository repository = null;

    static {
        YTELSETYPE_FPSAK_TIL_ABAKUS = Map.of(
            "FORELDREPENGER", "FP",
            "ENGANGSSTØNAD", "ES",
            "SVANGERSKAPSPENGER", "SVP");

        YTELSETYPE_ABAKUS_TIL_FPSAK = YTELSETYPE_FPSAK_TIL_ABAKUS.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private KodeverkMapper() {

    }

    static KodeverkRepository repository() {
        if (repository == null) {
            repository = CDI.current().select(KodeverkRepository.class).get();
        }
        return repository;
    }

    static String getFpsakYtelseTypeFraAbakus(String kode) {
        return YTELSETYPE_ABAKUS_TIL_FPSAK.get(kode);
    }

    static String getAbakusYtelseTypeFraFpsak(String kode) {
        return YTELSETYPE_FPSAK_TIL_ABAKUS.get(kode);
    }

    static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseType mapYtelseTypeTilDto(YtelseType ytelseType) {
        if (ytelseType == null || ytelseType.getKode().equals("-")) {
            return null;
        }
        switch (ytelseType.getKodeverk()) {
            case no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType.KODEVERK:
                return new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType(ytelseType.getKode());
            case no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType.KODEVERK:
                return new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType(ytelseType.getKode());
            case no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltPensjonTrygdType.KODEVERK:
                return new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltPensjonTrygdType(ytelseType.getKode());
            default:
                throw new IllegalArgumentException("Ukjent YtelseType: " + ytelseType + ", kan ikke mappes til "
                    + no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseType.class.getName());
        }

    }

    public static YtelseType mapUtbetaltYtelseTypeTilGrunnlag(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseType type) {
        if (type == null) return OffentligYtelseType.UDEFINERT;
        var kodeverk = (no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Kodeverk) type;
        String kode = kodeverk.getKode();
        switch (kodeverk.getKodeverk()) {
            case no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType.KODEVERK:
                return repository().finn(OffentligYtelseType.class, kode);
            case no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType.KODEVERK:
                return repository().finn(NæringsinntektType.class, kode);
            case no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltPensjonTrygdType.KODEVERK:
                return repository().finn(PensjonTrygdType.class, kode);
            default:
                throw new IllegalArgumentException("Ukjent UtbetaltYtelseType: " + type);
        }
    }

    public static TemaUnderkategori getTemaUnderkategori(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.TemaUnderkategori kode) {
        return kode == null || kode.getKode().equals("-")
            ? TemaUnderkategori.UDEFINERT
            : repository().finn(TemaUnderkategori.class, kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.TemaUnderkategori getBehandlingsTemaUnderkategori(TemaUnderkategori kode) {
        return kode == null || TemaUnderkategori.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.TemaUnderkategori(kode.getKode());
    }

    public static BekreftetPermisjonStatus getBekreftetPermisjonStatus(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.BekreftetPermisjonStatus kode) {
        return kode == null || kode.getKode().equals("-")
            ? BekreftetPermisjonStatus.UDEFINERT
            : repository().finn(BekreftetPermisjonStatus.class, kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.BekreftetPermisjonStatus mapBekreftetPermisjonStatus(BekreftetPermisjonStatus status) {
        return status == null || BekreftetPermisjonStatus.UDEFINERT.equals(status)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.BekreftetPermisjonStatus(status.getKode());
    }

    public static Fagsystem mapFagsystemFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Fagsystem dto) {
        return dto == null
            ? Fagsystem.UDEFINERT
            : repository().finn(Fagsystem.class, dto.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Fagsystem mapFagsystemTilDto(Fagsystem kode) {
        return kode == null || Fagsystem.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Fagsystem(kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektPeriodeType mapInntektPeriodeTypeTilDto(InntektPeriodeType hyppighet) {
        return hyppighet == null || InntektPeriodeType.UDEFINERT.equals(hyppighet)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektPeriodeType(hyppighet.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Arbeidskategori mapArbeidskategoriTilDto(Arbeidskategori kode) {
        return kode == null || Arbeidskategori.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Arbeidskategori(kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidType mapArbeidTypeTilDto(ArbeidType arbeidType) {
        return arbeidType == null || ArbeidType.UDEFINERT.equals(arbeidType)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidType(arbeidType.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType mapPermisjonbeskrivelseTypeTilDto(PermisjonsbeskrivelseType kode) {
        return kode == null || PermisjonsbeskrivelseType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType(kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektskildeType mapInntektsKildeTilDto(InntektsKilde kode) {
        return kode == null || InntektsKilde.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektskildeType(kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektspostType mapInntektspostTypeTilDto(InntektspostType kode) {
        return kode == null || InntektspostType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektspostType(kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType mapSkatteOgAvgiftsregelTilDto(SkatteOgAvgiftsregelType kode) {
        return kode == null || SkatteOgAvgiftsregelType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType(kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType mapArbeidsforholdHandlingTypeTilDto(ArbeidsforholdHandlingType kode) {
        return kode == null || ArbeidsforholdHandlingType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType(kode.getKode());
    }

    public static ArbeidType mapArbeidType(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidType dto) {
        return dto == null
            ? ArbeidType.UDEFINERT
            : repository().finn(ArbeidType.class, dto.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType mapInntektsmeldingInnsendingsårsak(InntektsmeldingInnsendingsårsak kode) {
        return kode == null || InntektsmeldingInnsendingsårsak.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType(kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.NaturalytelseType mapNaturalYtelseTilDto(NaturalYtelseType kode) {
        return kode == null || NaturalYtelseType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.NaturalytelseType(kode.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtsettelseÅrsakType mapUtsettelseÅrsakTilDto(UtsettelseÅrsak kode) {
        return kode == null || UtsettelseÅrsak.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtsettelseÅrsakType(kode.getKode());
    }

    public static InntektsKilde mapInntektsKildeFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektskildeType dto) {
        return dto == null
            ? InntektsKilde.UDEFINERT
            : repository().finn(InntektsKilde.class, dto.getKode());
    }

    public static ArbeidsforholdHandlingType mapArbeidsforholdHandlingTypeFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType dto) {
        return dto == null
            ? ArbeidsforholdHandlingType.UDEFINERT
            : repository().finn(ArbeidsforholdHandlingType.class, dto.getKode());
    }

    public static NaturalYtelseType mapNaturalYtelseFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.NaturalytelseType dto) {
        return dto == null
            ? NaturalYtelseType.UDEFINERT
            : repository().finn(NaturalYtelseType.class, dto.getKode());
    }

    public static VirksomhetType mapVirksomhetTypeFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.VirksomhetType dto) {
        return dto == null
            ? VirksomhetType.UDEFINERT
            : repository().finn(VirksomhetType.class, dto.getKode());
    }

    public static no.nav.foreldrepenger.abakus.kodeverk.YtelseType mapYtelseTypeFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseType dto) {
        return dto == null
            ? no.nav.foreldrepenger.abakus.kodeverk.YtelseType.UDEFINERT
            : repository().finn(no.nav.foreldrepenger.abakus.kodeverk.YtelseType.class, dto.getKode());
    }

    public static YtelseStatus mapYtelseStatusFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseStatus dto) {
        return dto == null
            ? YtelseStatus.UDEFINERT
            : repository().finn(YtelseStatus.class, dto.getKode());
    }

    public static SkatteOgAvgiftsregelType mapSkatteOgAvgiftsregelFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType dto) {
        return dto == null
            ? SkatteOgAvgiftsregelType.UDEFINERT
            : repository().finn(SkatteOgAvgiftsregelType.class, dto.getKode());
    }

    public static InntektspostType mapInntektspostTypeFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektspostType dto) {
        return dto == null
            ? InntektspostType.UDEFINERT
            : repository().finn(InntektspostType.class, dto.getKode());
    }

    public static PermisjonsbeskrivelseType mapPermisjonbeskrivelseTypeFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType dto) {
        return dto == null
            ? PermisjonsbeskrivelseType.UDEFINERT
            : repository().finn(PermisjonsbeskrivelseType.class, dto.getKode());
    }

    public static Arbeidskategori mapArbeidskategoriFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Arbeidskategori dto) {
        return dto == null
            ? Arbeidskategori.UDEFINERT
            : repository().finn(Arbeidskategori.class, dto.getKode());
    }

    public static InntektPeriodeType mapInntektPeriodeTypeFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektPeriodeType dto) {
        return dto == null
            ? InntektPeriodeType.UDEFINERT
            : repository().finn(InntektPeriodeType.class, dto.getKode());
    }

    public static InntektsmeldingInnsendingsårsak mapInntektsmeldingInnsendingsårsakFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType dto) {
        return dto == null
            ? InntektsmeldingInnsendingsårsak.UDEFINERT
            : repository().finn(InntektsmeldingInnsendingsårsak.class, dto.getKode());
    }

    public static UtsettelseÅrsak mapUtsettelseÅrsakFraDto(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtsettelseÅrsakType dto) {
        return dto == null
            ? UtsettelseÅrsak.UDEFINERT
            : repository().finn(UtsettelseÅrsak.class, dto.getKode());
    }

    public static no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.VirksomhetType mapVirksomhetTypeTilDto(VirksomhetType kode) {
        return kode == null || VirksomhetType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.VirksomhetType(kode.getKode());
    }

}
