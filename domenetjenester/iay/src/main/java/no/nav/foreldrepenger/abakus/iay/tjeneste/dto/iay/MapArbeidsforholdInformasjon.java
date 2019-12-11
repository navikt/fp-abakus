package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import no.nav.abakus.iaygrunnlag.*;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdInformasjon;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdOverstyringDto;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdReferanseDto;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.BekreftetPermisjon;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyringBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyrtePerioderEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.BekreftetPermisjonStatus;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.typer.*;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class MapArbeidsforholdInformasjon {

    private static final Comparator<ArbeidsforholdReferanseDto> COMP_ARBEIDSFORHOLD_REFERANSE = Comparator
        .comparing((ArbeidsforholdReferanseDto ref) -> ref.getArbeidsgiver().getIdent())
        .thenComparing(ref -> ref.getArbeidsforholdReferanse() == null ? null : ref.getArbeidsforholdReferanse().getAbakusReferanse(),
            Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<ArbeidsforholdOverstyringDto> COMP_ARBEIDSFORHOLD_OVERSTYRING = Comparator
        .comparing((ArbeidsforholdOverstyringDto ov) -> ov.getArbeidsgiver().getIdent())
        .thenComparing(ov -> ov.getArbeidsforholdRef() == null ? null : ov.getArbeidsforholdRef().getAbakusReferanse(),
            Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<Periode> COMP_PERIODE = Comparator
        .comparing(Periode::getFom, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Periode::getTom, Comparator.nullsLast(Comparator.naturalOrder()));

    static class MapFraDto {
        private KodeverkRepository kodeverkRepository;
        private InntektArbeidYtelseGrunnlagBuilder grunnlagBuilder;

        public MapFraDto(KodeverkRepository kodeverkRepository, InntektArbeidYtelseGrunnlagBuilder builder) {
            this.kodeverkRepository = kodeverkRepository;
            this.grunnlagBuilder = builder;
        }

        ArbeidsforholdInformasjonBuilder map(ArbeidsforholdInformasjon dto) {
            var eksisterende = grunnlagBuilder.getArbeidsforholdInformasjon();
            var builder = ArbeidsforholdInformasjonBuilder.builder(eksisterende);
            if (dto != null) {
                builder.fjernAlleOverstyringer();
                dto.getOverstyringer().stream().map(this::mapArbeidsforholdOverstyring).forEach(builder::leggTil);
                dto.getReferanser().stream().map(this::mapArbeidsforholdReferanse).forEach(builder::leggTilNyReferanse);
            }
            return builder;
        }

        private ArbeidsforholdReferanse mapArbeidsforholdReferanse(ArbeidsforholdReferanseDto r) {
            var internRef = InternArbeidsforholdRef.ref(r.getArbeidsforholdReferanse().getAbakusReferanse());
            var eksternRef = EksternArbeidsforholdRef.ref(r.getArbeidsforholdReferanse().getEksternReferanse());
            var arbeidsgiver = mapArbeidsgiver(r.getArbeidsgiver());
            return new ArbeidsforholdReferanse(arbeidsgiver, internRef, eksternRef);
        }

        private ArbeidsforholdOverstyringBuilder mapArbeidsforholdOverstyring(ArbeidsforholdOverstyringDto ov) {
            var arbeidsgiverRef = mapArbeidsforholdRef(ov.getArbeidsforholdRef());
            var nyArbeidsgiverRef = mapArbeidsforholdRef(ov.getNyArbeidsforholdRef());
            var arbeidsgiver = mapArbeidsgiver(ov.getArbeidsgiver());

            var overstyringBuilder = ArbeidsforholdOverstyringBuilder.oppdatere(Optional.empty());

            overstyringBuilder.medBeskrivelse(ov.getBegrunnelse())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbeidsgiverRef)
                .medNyArbeidsforholdRef(nyArbeidsgiverRef)
                .medHandling(KodeverkMapper.mapArbeidsforholdHandlingTypeFraDto(ov.getHandling()))
                .medAngittArbeidsgiverNavn(ov.getAngittArbeidsgiverNavn())
                .medAngittStillingsprosent(new Stillingsprosent(ov.getStillingsprosent()));

            ov.getBekreftetPermisjon().ifPresent(bp -> {
                BekreftetPermisjonStatus bekreftetPermisjonStatus = KodeverkMapper.getBekreftetPermisjonStatus(bp.getBekreftetPermisjonStatus());
                BekreftetPermisjonStatus status = kodeverkRepository.finn(BekreftetPermisjonStatus.class, bekreftetPermisjonStatus.getKode());
                var bekreftetPermisjon = new BekreftetPermisjon(bp.getPeriode().getFom(), bp.getPeriode().getTom(), status);
                overstyringBuilder.medBekreftetPermisjon(bekreftetPermisjon);
            });

            // overstyrte perioder
            ov.getArbeidsforholdOverstyrtePerioder().forEach(p -> overstyringBuilder.leggTilOverstyrtPeriode(p.getFom(), p.getTom()));

            return overstyringBuilder;
        }

        private InternArbeidsforholdRef mapArbeidsforholdRef(ArbeidsforholdRefDto arbeidsforholdId) {
            if (arbeidsforholdId == null) {
                return InternArbeidsforholdRef.nullRef();
            }
            // FIXME : Abakus må lagre intern<->ekstern referanse
            return InternArbeidsforholdRef.ref(arbeidsforholdId.getAbakusReferanse());
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
            if (arbeidsgiver.getErOrganisasjon()) {
                return Arbeidsgiver.virksomhet(new OrgNummer(arbeidsgiver.getIdent()));
            }
            return Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));
        }
    }

    static class MapTilDto {

        ArbeidsforholdInformasjon map(no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon entitet) {
            if (entitet == null)
                return null;

            var arbeidsforholdInformasjon = new ArbeidsforholdInformasjon();
            var overstyringer = entitet.getOverstyringer().stream()
                .map(ao -> {
                    var dto = new ArbeidsforholdOverstyringDto(mapAktør(ao.getArbeidsgiver()),
                        mapArbeidsforholdsId(entitet, ao.getArbeidsgiver(), ao.getArbeidsforholdRef()))
                        .medBegrunnelse(ao.getBegrunnelse())
                        .medBekreftetPermisjon(mapBekreftetPermisjon(ao.getBekreftetPermisjon()))
                        .medHandling(KodeverkMapper.mapArbeidsforholdHandlingTypeTilDto(ao.getHandling()))
                        .medNavn(ao.getArbeidsgiverNavn())
                        .medStillingsprosent(ao.getStillingsprosent() == null ? null : ao.getStillingsprosent().getVerdi())
                        .medNyArbeidsforholdRef(
                            ao.getNyArbeidsforholdRef() == null ? null : mapArbeidsforholdsId(entitet, ao.getArbeidsgiver(), ao.getNyArbeidsforholdRef()))
                        .medArbeidsforholdOverstyrtePerioder(map(ao.getArbeidsforholdOverstyrtePerioder()));
                    return dto;
                })
                .sorted(COMP_ARBEIDSFORHOLD_OVERSTYRING)
                .collect(Collectors.toList());

            var referanser = entitet.getArbeidsforholdReferanser().stream()
                .map(this::mapArbeidsforholdReferanse)
                .sorted(COMP_ARBEIDSFORHOLD_REFERANSE)
                .collect(Collectors.toList());

            return arbeidsforholdInformasjon
                .medOverstyringer(overstyringer)
                .medReferanser(referanser);
        }

        private List<Periode> map(List<ArbeidsforholdOverstyrtePerioderEntitet> perioder) {
            return perioder == null ? null
                : perioder.stream()
                .map(ArbeidsforholdOverstyrtePerioderEntitet::getOverstyrtePeriode)
                .map(this::mapPeriode)
                .sorted(COMP_PERIODE)
                .collect(Collectors.toList());
        }

        private no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.BekreftetPermisjon mapBekreftetPermisjon(Optional<BekreftetPermisjon> entitet) {
            if (entitet.isEmpty()) {
                return null;
            }
            var bekreftetPermisjon = entitet.get();
            var periode = mapPeriode(bekreftetPermisjon.getPeriode());
            var bekreftetPermisjonStatus = KodeverkMapper.mapBekreftetPermisjonStatus(bekreftetPermisjon.getStatus());
            return new no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.BekreftetPermisjon(periode, bekreftetPermisjonStatus);
        }

        private Periode mapPeriode(DatoIntervallEntitet periode) {
            return new Periode(periode.getFomDato(), periode.getTomDato());
        }

        private ArbeidsforholdReferanseDto mapArbeidsforholdReferanse(ArbeidsforholdReferanse ref) {
            var arbeidsgiver = mapAktør(ref.getArbeidsgiver());
            var internReferanse = ref.getInternReferanse().getReferanse();
            var eksternReferanse = ref.getEksternReferanse().getReferanse();
            return new ArbeidsforholdReferanseDto(arbeidsgiver, new ArbeidsforholdRefDto(internReferanse, eksternReferanse));
        }

        private ArbeidsforholdRefDto mapArbeidsforholdsId(no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon arbeidsforholdInformasjon,
                                                          Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef ref) {
            if (ref == null) {
                return null;
            }
            /*
             * TODO: Fjern denne kommentaren når migrering fra fpsak er overstått.
             * Merk motsatt mapping av fpsak under migrering. FPSAK holder kun AAREGISTER referanse (og således vil sende samme referanse for
             * intern/ekstern (abakus/aaregister)
             * I Abakus er disse skilt. ArbeidsforholdRef holder intern abakus referanse (ikke AAREGISTER referanse som i FPSAK)
             */
            String internId = ref.getReferanse();
            if (internId != null) {
                EksternArbeidsforholdRef eksternReferanse = arbeidsforholdInformasjon.finnEksternRaw(arbeidsgiver, ref);
                if (eksternReferanse == null || eksternReferanse.getReferanse() == null) {
                    throw new IllegalStateException("Mangler eksternReferanse for internReferanse=" + ref);
                }
                return new ArbeidsforholdRefDto(internId, eksternReferanse.getReferanse());
            }
            return null;
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiver) {
            return arbeidsgiver.erAktørId()
                ? new AktørIdPersonident(arbeidsgiver.getAktørId().getId())
                : new Organisasjon(arbeidsgiver.getOrgnr().getId());
        }
    }
}
