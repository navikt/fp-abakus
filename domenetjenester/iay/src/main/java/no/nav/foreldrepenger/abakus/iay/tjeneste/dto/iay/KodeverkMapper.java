package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;

import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.domene.iay.NæringsinntektType;
import no.nav.foreldrepenger.abakus.domene.iay.OffentligYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.PensjonTrygdType;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseInntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.NaturalYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.UtsettelseÅrsak;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.vedtak.domene.TemaUnderkategori;

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
        var kodeverk = (no.nav.abakus.iaygrunnlag.kodeverk.Kodeverk) type;
        String kode = kodeverk.getKode();
        switch (kodeverk.getKodeverk()) {
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

    public static TemaUnderkategori getTemaUnderkategori(no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori kode) {
        return kode == null || kode.getKode().equals("-")
            ? TemaUnderkategori.UDEFINERT
            : TemaUnderkategori.fraKode(kode.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori getBehandlingsTemaUnderkategori(TemaUnderkategori kode) {
        return kode == null || TemaUnderkategori.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori(kode.getKode());
    }

    public static Fagsystem mapFagsystemFraDto(no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem dto) {
        return dto == null
            ? Fagsystem.UDEFINERT
            : Fagsystem.fraKode(dto.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem mapFagsystemTilDto(Fagsystem kode) {
        return kode == null || Fagsystem.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem(kode.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType mapInntektPeriodeTypeTilDto(InntektPeriodeType hyppighet) {
        return hyppighet == null || InntektPeriodeType.UDEFINERT.equals(hyppighet)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType(hyppighet.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori mapArbeidskategoriTilDto(Arbeidskategori kode) {
        return kode == null || Arbeidskategori.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori(kode.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType mapPermisjonbeskrivelseTypeTilDto(PermisjonsbeskrivelseType kode) {
        return kode == null || PermisjonsbeskrivelseType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType(kode.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType mapInntektsKildeTilDto(InntektsKilde kode) {
        return kode == null || InntektsKilde.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType(kode.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType mapInntektspostTypeTilDto(InntektspostType kode) {
        return kode == null || InntektspostType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType(kode.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType mapArbeidsforholdHandlingTypeTilDto(ArbeidsforholdHandlingType kode) {
        return kode == null || ArbeidsforholdHandlingType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType(kode.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType mapInntektsmeldingInnsendingsårsak(InntektsmeldingInnsendingsårsak kode) {
        return kode == null || InntektsmeldingInnsendingsårsak.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType(kode.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.NaturalytelseType mapNaturalYtelseTilDto(NaturalYtelseType kode) {
        return kode == null || NaturalYtelseType.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.NaturalytelseType(kode.getKode());
    }

    public static no.nav.abakus.iaygrunnlag.kodeverk.UtsettelseÅrsakType mapUtsettelseÅrsakTilDto(UtsettelseÅrsak kode) {
        return kode == null || UtsettelseÅrsak.UDEFINERT.equals(kode)
            ? null
            : new no.nav.abakus.iaygrunnlag.kodeverk.UtsettelseÅrsakType(kode.getKode());
    }

    public static InntektsKilde mapInntektsKildeFraDto(no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType dto) {
        return dto == null
            ? InntektsKilde.UDEFINERT
            : repository().finn(InntektsKilde.class, dto.getKode());
    }

    public static ArbeidsforholdHandlingType mapArbeidsforholdHandlingTypeFraDto(no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType dto) {
        return dto == null
            ? ArbeidsforholdHandlingType.UDEFINERT
            : repository().finn(ArbeidsforholdHandlingType.class, dto.getKode());
    }

    public static NaturalYtelseType mapNaturalYtelseFraDto(no.nav.abakus.iaygrunnlag.kodeverk.NaturalytelseType dto) {
        return dto == null
            ? NaturalYtelseType.UDEFINERT
            : repository().finn(NaturalYtelseType.class, dto.getKode());
    }

    public static no.nav.foreldrepenger.abakus.kodeverk.YtelseType mapYtelseTypeFraDto(no.nav.abakus.iaygrunnlag.kodeverk.YtelseType dto) {
        return dto == null
            ? no.nav.foreldrepenger.abakus.kodeverk.YtelseType.UDEFINERT
            : no.nav.foreldrepenger.abakus.kodeverk.YtelseType.fraKode(dto.getKode());
    }

    public static YtelseStatus mapYtelseStatusFraDto(no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus dto) {
        return dto == null
            ? YtelseStatus.UDEFINERT
            : YtelseStatus.fraKode(dto.getKode());
    }

    public static InntektspostType mapInntektspostTypeFraDto(no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType dto) {
        return dto == null
            ? InntektspostType.UDEFINERT
            : repository().finn(InntektspostType.class, dto.getKode());
    }

    public static PermisjonsbeskrivelseType mapPermisjonbeskrivelseTypeFraDto(no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType dto) {
        return dto == null
            ? PermisjonsbeskrivelseType.UDEFINERT
            : repository().finn(PermisjonsbeskrivelseType.class, dto.getKode());
    }

    public static Arbeidskategori mapArbeidskategoriFraDto(no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori dto) {
        return dto == null
            ? Arbeidskategori.UDEFINERT
            : repository().finn(Arbeidskategori.class, dto.getKode());
    }

    public static InntektPeriodeType mapInntektPeriodeTypeFraDto(no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType dto) {
        return dto == null
            ? InntektPeriodeType.UDEFINERT
            : repository().finn(InntektPeriodeType.class, dto.getKode());
    }

    public static InntektsmeldingInnsendingsårsak mapInntektsmeldingInnsendingsårsakFraDto(no.nav.abakus.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType dto) {
        return dto == null
            ? InntektsmeldingInnsendingsårsak.UDEFINERT
            : repository().finn(InntektsmeldingInnsendingsårsak.class, dto.getKode());
    }

    public static UtsettelseÅrsak mapUtsettelseÅrsakFraDto(no.nav.abakus.iaygrunnlag.kodeverk.UtsettelseÅrsakType dto) {
        return dto == null
            ? UtsettelseÅrsak.UDEFINERT
            : repository().finn(UtsettelseÅrsak.class, dto.getKode());
    }

}
