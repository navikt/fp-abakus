package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregatEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Gradering;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.NaturalYtelse;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Refusjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.UtsettelsePeriode;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.JournalpostId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.GraderingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingSomIkkeKommerDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.NaturalytelseDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.RefusjonDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.UtsettelsePeriodeDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.NaturalytelseType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtsettelseÅrsakType;

public class MapInntektsmeldinger {

    private class MapTilDto {

        public InntektsmeldingerDto map(InntektsmeldingAggregat iayAggregat, List<InntektsmeldingSomIkkeKommer> inntektsmeldingerSomIkkeKommer) {
            var dto = new InntektsmeldingerDto();

            var inntektsmeldinger = iayAggregat.getInntektsmeldinger().stream().map(this::mapInntektsmelding).collect(Collectors.toList());
            dto.medInntektsmeldinger(inntektsmeldinger);

            if (inntektsmeldingerSomIkkeKommer != null) {
                var ikkeKommer = inntektsmeldingerSomIkkeKommer.stream().map(this::mapInntektsmeldingSomIkkekommer).collect(Collectors.toList());
                dto.medInntektsmeldingerSomIkkeKommer(ikkeKommer);
            }
            return dto;
        }

        private InntektsmeldingSomIkkeKommerDto mapInntektsmeldingSomIkkekommer(InntektsmeldingSomIkkeKommer ik) {
            var arbeidsforholdsId = mapArbeidsforholdsId(ik.getArbeidsgiver(), ik.getRef());
            var arbeidsgiverEntitet = mapAktør(ik.getArbeidsgiver());
            return new InntektsmeldingSomIkkeKommerDto(arbeidsgiverEntitet, arbeidsforholdsId);
        }

        private InntektsmeldingDto mapInntektsmelding(Inntektsmelding im) {
            var ArbeidsgiverEntitet = mapAktør(im.getArbeidsgiver());
            var journalpostId = new JournalpostId(im.getJournalpostId().getVerdi());
            var innsendingstidspunkt = im.getInnsendingstidspunkt();
            var arbeidsforholdId = mapArbeidsforholdsId(im.getArbeidsgiver(), im.getArbeidsforholdRef());
            var innsendingsårsak = new InntektsmeldingInnsendingsårsakType(im.getInntektsmeldingInnsendingsårsak().getKode());
            
            var inntektsmeldingDto = new InntektsmeldingDto(ArbeidsgiverEntitet, journalpostId, innsendingstidspunkt, im.getMottattDato())
                .medArbeidsforholdRef(arbeidsforholdId)
                .medInnsendingsårsak(innsendingsårsak)
                .medInntektBeløp(im.getInntektBeløp().getVerdi())
                .medKanalreferanse(im.getKanalreferanse())
                .medKildesystem(im.getKildesystem())
                .medRefusjonOpphører(im.getRefusjonOpphører())
                .medRefusjonsBeløpPerMnd(im.getRefusjonBeløpPerMnd().getVerdi())
                .medStartDatoPermisjon(im.getStartDatoPermisjon())
                .medNærRelasjon(im.getErNærRelasjon());

            inntektsmeldingDto.medEndringerRefusjon(im.getEndringerRefusjon().stream().map(this::mapEndringRefusjon).collect(Collectors.toList()));

            inntektsmeldingDto.medGraderinger(im.getGraderinger().stream().map(this::mapGradering).collect(Collectors.toList()));

            inntektsmeldingDto.medNaturalytelser(im.getNaturalYtelser().stream().map(this::mapNaturalytelse).collect(Collectors.toList()));

            inntektsmeldingDto.medUtsettelsePerioder(im.getUtsettelsePerioder().stream().map(this::mapUtsettelsePeriode).collect(Collectors.toList()));

            return inntektsmeldingDto;
        }

        private RefusjonDto mapEndringRefusjon(Refusjon refusjon) {
            return new RefusjonDto(refusjon.getFom(), refusjon.getRefusjonsbeløp().getVerdi());
        }

        private GraderingDto mapGradering(Gradering gradering) {
            var periode = gradering.getPeriode();
            var arbeidstidProsent = gradering.getArbeidstidProsent();
            return new GraderingDto(new Periode(periode.getFomDato(), periode.getTomDato()), arbeidstidProsent.getVerdi());
        }

        private NaturalytelseDto mapNaturalytelse(NaturalYtelse naturalYtelse) {
            var periode = naturalYtelse.getPeriode();
            var type = new NaturalytelseType(naturalYtelse.getType().getKode());
            var beløpPerMnd = naturalYtelse.getBeloepPerMnd().getVerdi();
            return new NaturalytelseDto(new Periode(periode.getFomDato(), periode.getTomDato()), type, beløpPerMnd);
        }

        private UtsettelsePeriodeDto mapUtsettelsePeriode(UtsettelsePeriode utsettelsePeriode) {
            var periode = utsettelsePeriode.getPeriode();
            var utsettelseÅrsak = new UtsettelseÅrsakType(utsettelsePeriode.getÅrsak().getKode());
            return new UtsettelsePeriodeDto(new Periode(periode.getFomDato(), periode.getTomDato()), utsettelseÅrsak);
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiverEntitet) {
            return arbeidsgiverEntitet.erAktørId()
                ? new AktørIdPersonident(arbeidsgiverEntitet.getAktørId().getId())
                : new Organisasjon(arbeidsgiverEntitet.getOrgnr().getId());
        }

        private ArbeidsforholdRefDto mapArbeidsforholdsId(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef arbeidsforhold) {
            String internId = arbeidsforhold.getReferanse();
            if (internId != null) {
                String eksternReferanse = tjeneste
                    .finnReferanseFor(koblingReferanse, arbeidsgiverEntitet, arbeidsforhold, true)
                    .getReferanse();
                return new ArbeidsforholdRefDto(internId, eksternReferanse);
            }
            return new ArbeidsforholdRefDto(internId, null);
        }
    }

    private class MapFraDto {

        public InntektsmeldingAggregatEntitet map(InntektsmeldingerDto inntektsmeldinger) {
            var inntektsmeldingAggregat = new InntektsmeldingAggregatEntitet();
            inntektsmeldinger.getInntektsmeldinger().stream().map(this::mapInntektsmelding).forEach(inntektsmeldingAggregat::leggTil);
            return inntektsmeldingAggregat;
        }

        private Inntektsmelding mapInntektsmelding(InntektsmeldingDto dto) {
            var arbeidsforholdId = dto.getArbeidsforholdRef() != null ? dto.getArbeidsforholdRef().getAbakusReferanse() : null;
            var journalpostId = dto.getJournalpostId().getId();
            var innsendingstidspunkt = dto.getInnsendingstidspunkt().toLocalDateTime();
            var innsendingsårsak = dto.getInnsendingsårsak().getKode();

            return InntektsmeldingBuilder.builder()
                .medJournalpostId(journalpostId)
                .medArbeidsgiver(mapArbeidsgiver(dto.getArbeidsgiver()))
                .medInnsendingstidspunkt(innsendingstidspunkt)
                .medBeløp(dto.getInntektBeløp())
                .medArbeidsforholdId(arbeidsforholdId)
                .medStartDatoPermisjon(dto.getStartDatoPermisjon())
                .medRefusjon(dto.getRefusjonsBeløpPerMnd())
                .medKanalreferanse(dto.getKanalreferanse())
                .medInntektsmeldingaarsak(innsendingsårsak)
                .medNærRelasjon(dto.isNærRelasjon())
                .medKildesystem(dto.getKildesystem())
                .medMottattDato(dto.getMottattDato())
                .build();
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiverDto) {
            if (arbeidsgiverDto == null) {
                return null;
            }
            String identifikator = arbeidsgiverDto.getIdent();
            if (arbeidsgiverDto.getErOrganisasjon()) {
                return Arbeidsgiver.virksomhet(new OrgNummer(identifikator));
            }
            if (arbeidsgiverDto.getErPerson()) {
                return Arbeidsgiver.person(new AktørId(identifikator));
            }
            throw new IllegalArgumentException();
        }
    }

    private class MapFraInntektsmeldingSomIkkeKommerDto {

        public List<InntektsmeldingSomIkkeKommer> map(InntektsmeldingerDto inntektsmeldinger) {
            if (inntektsmeldinger == null) {
                return Collections.emptyList();
            }
            return inntektsmeldinger.getInntektsmeldingerSomIkkeKommer().stream().map(this::mapInntektsmeldingSomIkkeKommer).collect(Collectors.toList());
        }

        private InntektsmeldingSomIkkeKommer mapInntektsmeldingSomIkkeKommer(InntektsmeldingSomIkkeKommerDto ik) {
            ArbeidsforholdRef ref = ArbeidsforholdRef.ref(ik.getArbeidsforholdId().getAbakusReferanse());
            var arbeidsgiver = mapArbeidsgiver(ik.getArbeidsgiver());
            return new InntektsmeldingSomIkkeKommer(arbeidsgiver, ref);
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
            if (arbeidsgiver.getErOrganisasjon()) {
                return Arbeidsgiver.virksomhet(new OrgNummer(arbeidsgiver.getIdent()));
            }
            return Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));
        }
    }

    private InntektArbeidYtelseTjeneste tjeneste;

    private KoblingReferanse koblingReferanse;

    public MapInntektsmeldinger(InntektArbeidYtelseTjeneste tjeneste, KoblingReferanse koblingReferanse) {
        this.tjeneste = tjeneste;
        this.koblingReferanse = koblingReferanse;
    }

    public InntektsmeldingerDto mapTilDto(InntektsmeldingAggregat ia, List<InntektsmeldingSomIkkeKommer> inntektsmeldingerSomIkkeKommer) {
        return new MapTilDto().map(ia, inntektsmeldingerSomIkkeKommer);
    }

    public InntektsmeldingAggregatEntitet mapFraDto(InntektsmeldingerDto inntektsmeldinger) {
        return new MapFraDto().map(inntektsmeldinger);
    }

    public List<InntektsmeldingSomIkkeKommer> mapFraDtoInntektsmeldingSomIkkeKommer(InntektsmeldingerDto inntektsmeldinger) {
        return new MapFraInntektsmeldingSomIkkeKommerDto().map(inntektsmeldinger);
    }

}
