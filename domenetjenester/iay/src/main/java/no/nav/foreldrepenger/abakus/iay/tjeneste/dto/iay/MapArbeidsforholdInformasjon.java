package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.BekreftetPermisjon;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyringBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyrtePerioderEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanseEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.BekreftetPermisjonStatus;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdOverstyringDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdReferanseDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

class MapArbeidsforholdInformasjon {
    static class MapFraDto {
        private InntektArbeidYtelseGrunnlagBuilder grunnlagBuilder;

        public MapFraDto(InntektArbeidYtelseGrunnlagBuilder builder) {
            this.grunnlagBuilder = builder;
        }

        ArbeidsforholdInformasjonBuilder map(ArbeidsforholdInformasjon dto) {
            var eksisterende = grunnlagBuilder.getArbeidsforholdInformasjon();
            var builder = ArbeidsforholdInformasjonBuilder.builder(eksisterende);
            dto.getOverstyringer().stream().map(ov -> mapArbeidsforholdOverstyring(ov, builder)).forEach(builder::leggTil);
            dto.getReferanser().stream().map(this::mapArbeidsforholdReferanse).forEach(r -> builder.leggTilNyReferanse(r));
            return builder;
        }

        private ArbeidsforholdReferanseEntitet mapArbeidsforholdReferanse(ArbeidsforholdReferanseDto r) {
            var internRef = InternArbeidsforholdRef.ref(r.getArbeidsforholdReferanse().getAbakusReferanse());
            var eksternRef = EksternArbeidsforholdRef.ref(r.getArbeidsforholdReferanse().getEksternReferanse());
            var arbeidsgiver = mapArbeidsgiver(r.getArbeidsgiver());
            var ref = new ArbeidsforholdReferanseEntitet(arbeidsgiver, internRef, eksternRef);
            return ref;
        }

        private ArbeidsforholdOverstyringBuilder mapArbeidsforholdOverstyring(ArbeidsforholdOverstyringDto ov, ArbeidsforholdInformasjonBuilder builder) {
            var arbeidsgiverRef = mapArbeidsforholdRef(ov.getArbeidsforholdRef());
            var nyArbeidsgiverRef = mapArbeidsforholdRef(ov.getNyArbeidsforholdRef());
            var arbeidsgiver = mapArbeidsgiver(ov.getArbeidsgiver());

            var overstyringBuilder = builder.getOverstyringBuilderFor(arbeidsgiver, arbeidsgiverRef);

            overstyringBuilder.medBeskrivelse(ov.getBegrunnelse())
                .medNyArbeidsforholdRef(nyArbeidsgiverRef)
                .medHandling(new ArbeidsforholdHandlingType(ov.getHandling().getKode()))
                .medAngittArbeidsgiverNavn(ov.getAngittArbeidsgiverNavn())
                .medAngittStillingsprosent(new Stillingsprosent(ov.getStillingsprosent()));

            ov.getBekreftetPermisjon().ifPresent(bp -> {
                var bekreftetPermisjon = new BekreftetPermisjon(bp.getPeriode().getFom(), bp.getPeriode().getTom(), map(bp.getBekreftetPermisjonStatus()));
                overstyringBuilder.medBekreftetPermisjon(bekreftetPermisjon);
            });

            // overstyrte perioder
            ov.getArbeidsforholdOverstyrtePerioder().stream().forEach(p -> overstyringBuilder.leggTilOverstyrtPeriode(p.getFom(), p.getTom()));

            return overstyringBuilder;
        }

        private BekreftetPermisjonStatus map(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.BekreftetPermisjonStatus status) {
            return new BekreftetPermisjonStatus(status.getKode());
        }

        private ArbeidsforholdRef mapArbeidsforholdRef(ArbeidsforholdRefDto arbeidsforholdId) {
            if (arbeidsforholdId == null) {
                return ArbeidsforholdRef.ref(null);
            }
            // FIXME : Abakus må lagre intern<->ekstern referanse
            return ArbeidsforholdRef.ref(arbeidsforholdId.getAbakusReferanse());
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
            if(entitet==null) return null;
            
            var arbeidsforholdInformasjon = new ArbeidsforholdInformasjon();
            var overstyringer = entitet.getOverstyringer().stream()
                .map(ao -> {
                    var dto = new ArbeidsforholdOverstyringDto(mapAktør(ao.getArbeidsgiver()),
                        mapArbeidsforholdsId(entitet, ao.getArbeidsgiver(), ao.getArbeidsforholdRef()))
                            .medBegrunnelse(ao.getBegrunnelse())
                            .medBekreftetPermisjon(mapBekreftetPermisjon(ao.getBekreftetPermisjon()))
                            .medHandling(map(ao.getHandling()))
                            .medNavn(ao.getArbeidsgiverNavn())
                            .medStillingsprosent(ao.getStillingsprosent() == null ? null : ao.getStillingsprosent().getVerdi())
                            .medNyArbeidsforholdRef(
                                ao.getNyArbeidsforholdRef() == null ? null : mapArbeidsforholdsId(entitet, ao.getArbeidsgiver(), ao.getNyArbeidsforholdRef()))
                            .medArbeidsforholdOverstyrtePerioder(map(ao.getArbeidsforholdOverstyrtePerioder()));
                    return dto;
                })
                .collect(Collectors.toList());

            var referanser = entitet.getArbeidsforholdReferanser().stream()
                .map(ar -> this.mapArbeidsforholdReferanse(ar))
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
                    .collect(Collectors.toList());
        }

        private no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType map(ArbeidsforholdHandlingType handling) {
            if (handling == null) {
                return null;
            }
            return new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType(handling.getKode());
        }

        private no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeidsforhold.v1.BekreftetPermisjon mapBekreftetPermisjon(Optional<BekreftetPermisjon> entitet) {
            if(entitet.isEmpty()) {
                return null;
            }
            var bekreftetPermisjon = entitet.get();
            var periode = mapPeriode(bekreftetPermisjon.getPeriode());
            return new no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeidsforhold.v1.BekreftetPermisjon(periode,
                new no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.BekreftetPermisjonStatus(bekreftetPermisjon.getStatus().getKode()));
        }

        private Periode mapPeriode(DatoIntervallEntitet periode) {
            return new Periode(periode.getFomDato(), periode.getTomDato());
        }

        private ArbeidsforholdReferanseDto mapArbeidsforholdReferanse(ArbeidsforholdReferanseEntitet ref) {
            var arbeidsgiver = mapAktør(ref.getArbeidsgiver());
            var internReferanse = ref.getInternReferanse().getReferanse();
            var eksternReferanse = ref.getEksternReferanse().getReferanse();
            return new ArbeidsforholdReferanseDto(arbeidsgiver, new ArbeidsforholdRefDto(internReferanse, eksternReferanse));
        }

        private ArbeidsforholdRefDto mapArbeidsforholdsId(no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon arbeidsforholdInformasjon,
                                                          Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref) {
            if(ref==null) {
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
                EksternArbeidsforholdRef eksternReferanse = arbeidsforholdInformasjon.finnEkstern(arbeidsgiver, InternArbeidsforholdRef.ref(internId));
                return new ArbeidsforholdRefDto(internId, eksternReferanse.getReferanse());
            }
            return new ArbeidsforholdRefDto(internId, null);
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiver) {
            return arbeidsgiver.erAktørId()
                ? new AktørIdPersonident(arbeidsgiver.getAktørId().getId())
                : new Organisasjon(arbeidsgiver.getOrgnr().getId());
        }
    }
}
