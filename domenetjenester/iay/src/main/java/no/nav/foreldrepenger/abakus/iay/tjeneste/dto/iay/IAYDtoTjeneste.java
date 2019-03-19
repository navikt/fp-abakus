package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.abakus.domene.iay.AktivitetsAvtale;
import no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid;
import no.nav.foreldrepenger.abakus.domene.iay.AktørInntekt;
import no.nav.foreldrepenger.abakus.domene.iay.AktørYtelse;
import no.nav.foreldrepenger.abakus.domene.iay.Inntekt;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.Inntektspost;
import no.nav.foreldrepenger.abakus.domene.iay.Permisjon;
import no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.Ytelse;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.AktørDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.ReferanseDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.arbeid.AktivitetsAvtaleDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.arbeid.ArbeidDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.arbeid.PermisjonDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.arbeid.YrkesaktivitetDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.inntekt.InntekterDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.inntekt.UtbetalingDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.inntekt.UtbetalingsPostDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ytelse.YtelseDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ytelse.YtelserDto;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkDto;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;

@ApplicationScoped
public class IAYDtoTjeneste {

    public IAYDtoTjeneste() {
    }

    public GrunnlagDto mapTil(InntektArbeidYtelseGrunnlag grunnlag) {
        if (grunnlag == null) {
            return null;
        }
        GrunnlagDto dto = new GrunnlagDto();
        dto.setReferanse(new ReferanseDto(grunnlag.getReferanse().toString()));
        dto.setArbeid(mapArbeid(grunnlag.getAktørArbeidFørStp(null)));
        dto.setInntekt(mapInntekt(grunnlag.getAktørInntektFørStp(null)));
        dto.setYtelse(mapYtelser(grunnlag.getAktørYtelseFørStp(null)));
        return dto;
    }

    private List<YtelserDto> mapYtelser(Collection<AktørYtelse> ytelser) {
        return ytelser.stream().map(this::maptilYtelser).collect(Collectors.toList());
    }

    private YtelserDto maptilYtelser(AktørYtelse ay) {
        YtelserDto dto = new YtelserDto();
        dto.setAktør(new AktørDto(ay.getAktørId().getId()));
        dto.setYtelser(mapTilYtelser(ay.getYtelser()));
        return dto;
    }

    private List<YtelseDto> mapTilYtelser(Collection<Ytelse> ytelser) {
        return ytelser.stream().map(this::tilYtelse).collect(Collectors.toList());
    }

    private YtelseDto tilYtelse(Ytelse ytelse) {
        YtelseDto dto = new YtelseDto();
        dto.setPeriode(new PeriodeDto(ytelse.getPeriode().getFomDato(), ytelse.getPeriode().getTomDato()));
        dto.setSaksnummer(ytelse.getSaksnummer().getVerdi());
        dto.setStatus(ytelse.getStatus().somDto());
        dto.setType(ytelse.getRelatertYtelseType().somDto());
        dto.setFagsystem(ytelse.getKilde().somDto());
        return dto;
    }

    private List<InntekterDto> mapInntekt(Collection<AktørInntekt> aktørInntektFørStp) {
        return aktørInntektFørStp.stream().map(this::mapTilInntekt).collect(Collectors.toList());
    }

    private InntekterDto mapTilInntekt(AktørInntekt ai) {
        InntekterDto dto = new InntekterDto();
        dto.setAktør(new AktørDto(ai.getAktørId().getId()));
        List<UtbetalingDto> pensjonsgivende = tilUtbetalinger(ai.getInntektPensjonsgivende(), InntektsKilde.INNTEKT_OPPTJENING.somDto());
        List<UtbetalingDto> sammenligning = tilUtbetalinger(ai.getInntektSammenligningsgrunnlag(), InntektsKilde.INNTEKT_SAMMENLIGNING.somDto());
        List<UtbetalingDto> beregning = tilUtbetalinger(ai.getInntektBeregningsgrunnlag(), InntektsKilde.INNTEKT_BEREGNING.somDto());
        List<UtbetalingDto> sigrun = tilUtbetalinger(ai.getBeregnetSkatt(), InntektsKilde.SIGRUN.somDto());
        ArrayList<UtbetalingDto> utbetalinger = new ArrayList<>(pensjonsgivende);
        utbetalinger.addAll(sammenligning);
        utbetalinger.addAll(beregning);
        utbetalinger.addAll(sigrun);
        dto.setUtbetalinger(utbetalinger);
        return dto;
    }

    private List<UtbetalingDto> tilUtbetalinger(List<Inntekt> inntekter, KodeverkDto kilde) {
        return inntekter.stream().map(in -> tilUtbetaling(in, kilde)).collect(Collectors.toList());
    }

    private UtbetalingDto tilUtbetaling(Inntekt inntekt, KodeverkDto kilde) {
        Arbeidsgiver arbeidsgiver = inntekt.getArbeidsgiver();
        UtbetalingDto dto = new UtbetalingDto();
        dto.setKilde(kilde);
        if (arbeidsgiver != null)
            dto.setUtbetaler(mapArbeidsgiver(arbeidsgiver));
        dto.setPoster(tilPoster(inntekt.getInntektspost()));
        return dto;
    }

    private ArbeidsgiverDto mapArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return new ArbeidsgiverDto(arbeidsgiver.getIdentifikator(), mapArbeidsgiverType(arbeidsgiver));
    }

    private List<UtbetalingsPostDto> tilPoster(Collection<Inntektspost> inntektspost) {
        return inntektspost.stream().map(this::tilPost).collect(Collectors.toList());
    }

    private UtbetalingsPostDto tilPost(Inntektspost inntektspost) {
        UtbetalingsPostDto dto = new UtbetalingsPostDto();
        dto.setType(inntektspost.getInntektspostType().somDto());
        dto.setSkattAvgiftType(inntektspost.getSkatteOgAvgiftsregelType().somDto());
        dto.setBeløp(inntektspost.getBeløp().getVerdi());
        dto.setPeriode(new PeriodeDto(inntektspost.getFraOgMed(), inntektspost.getTilOgMed()));
        return dto;
    }

    private List<ArbeidDto> mapArbeid(Collection<AktørArbeid> aktørArbeid) {
        return aktørArbeid.stream().map(this::mapTilArbeid).collect(Collectors.toList());
    }

    private ArbeidDto mapTilArbeid(AktørArbeid aa) {
        ArbeidDto dto = new ArbeidDto();
        dto.setAktør(new AktørDto(aa.getAktørId().getId()));
        dto.setYrkesaktiviteter(tilYrkesaktiviteter(aa.getYrkesaktiviteter()));
        return dto;
    }

    private List<YrkesaktivitetDto> tilYrkesaktiviteter(Collection<Yrkesaktivitet> yrkesaktiviteter) {
        return yrkesaktiviteter.stream().map(this::tilDto).collect(Collectors.toList());
    }

    private YrkesaktivitetDto tilDto(Yrkesaktivitet yrkesaktivitet) {
        Arbeidsgiver arbeidsgiver = yrkesaktivitet.getArbeidsgiver();
        ArbeidsforholdRefDto arbeidsforholdId = new ArbeidsforholdRefDto(yrkesaktivitet.getArbeidsforholdRef()
            .map(ArbeidsforholdRef::getReferanse).orElse(null));

        YrkesaktivitetDto dto = new YrkesaktivitetDto();
        dto.setAnsettelsesperiode(mapAnsettelsesPeriode(yrkesaktivitet.getAnsettelsesPerioder()));
        dto.setArbeidsgiver(mapArbeidsgiver(arbeidsgiver));
        dto.setType(yrkesaktivitet.getArbeidType().somDto());
        dto.setArbeidsforholdId(arbeidsforholdId);
        dto.setPermisjoner(mapPermisjoner(yrkesaktivitet.getPermisjon()));
        dto.setAktivitetsAvtaler(mapAktivitetsAvtaler(yrkesaktivitet.getAktivitetsAvtaler()));
        return dto;
    }

    private List<AktivitetsAvtaleDto> mapAktivitetsAvtaler(Collection<AktivitetsAvtale> aktivitetsAvtaler) {
        return aktivitetsAvtaler.stream().map(this::mapAktivitetsAvtale).collect(Collectors.toList());
    }

    private AktivitetsAvtaleDto mapAktivitetsAvtale(AktivitetsAvtale aktivitetsAvtale) {
        AktivitetsAvtaleDto dto = new AktivitetsAvtaleDto();
        dto.setPeriode(new PeriodeDto(aktivitetsAvtale.getFraOgMed(), aktivitetsAvtale.getTilOgMed()));
        dto.setAntallTimer(aktivitetsAvtale.getAntallTimer().getVerdi());
        dto.setSistLønnsendring(aktivitetsAvtale.getSisteLønnsendringsdato());
        dto.setStillingsprosent(aktivitetsAvtale.getProsentsats().getVerdi());
        return dto;
    }

    private List<PermisjonDto> mapPermisjoner(Collection<Permisjon> permisjon) {
        return permisjon.stream().map(this::mapPermisjon).collect(Collectors.toList());
    }

    private PermisjonDto mapPermisjon(Permisjon permisjon) {
        PermisjonDto dto = new PermisjonDto();
        dto.setPeriode(new PeriodeDto(permisjon.getFraOgMed(), permisjon.getTilOgMed()));
        dto.setType(permisjon.getPermisjonsbeskrivelseType().somDto());
        dto.setProsentsats(permisjon.getProsentsats().getVerdi());
        return dto;
    }

    private ArbeidsgiverType mapArbeidsgiverType(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return ArbeidsgiverType.UKJENT;
        }
        if (arbeidsgiver.getErVirksomhet()) {
            return ArbeidsgiverType.VIRKSOMHET;
        }
        return ArbeidsgiverType.PRIVAT;
    }

    private List<PeriodeDto> mapAnsettelsesPeriode(List<AktivitetsAvtale> ansettelsesPerioder) {
        return ansettelsesPerioder.stream().map(aa -> new PeriodeDto(aa.getFraOgMed(), aa.getTilOgMed())).collect(Collectors.toList());
    }
}
