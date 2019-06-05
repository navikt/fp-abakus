package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.AktivitetsAvtale;
import no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Permisjon;
import no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetEntitet.AktivitetsAvtaleBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetEntitet.PermisjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.ArbeidDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.PermisjonDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.YrkesaktivitetDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class MapAktørArbeid {

    static class MapFraDto {

        private AktørId aktørId;
        private InntektArbeidYtelseAggregatBuilder registerData;

        MapFraDto(AktørId aktørId, InntektArbeidYtelseAggregatBuilder registerData) {
            this.registerData = registerData;
            this.aktørId = aktørId;
        }

        List<AktørArbeidBuilder> map(Collection<ArbeidDto> dtos) {
            if(dtos==null || dtos.isEmpty()) {
                return Collections.emptyList();
            }
            return dtos.stream().map(this::mapAktørArbeid).collect(Collectors.toUnmodifiableList());
        }

        private AktørArbeidBuilder mapAktørArbeid(ArbeidDto dto) {
            var builder = registerData.getAktørArbeidBuilder(aktørId);
            dto.getYrkesaktiviteter().forEach(yrkesaktivitetDto -> builder.leggTilYrkesaktivitet(mapYrkesaktivitet(yrkesaktivitetDto)));
            return builder;
        }

        private YrkesaktivitetBuilder mapYrkesaktivitet(YrkesaktivitetDto dto) {
            var arbeidsgiver = mapArbeidsgiver(dto.getArbeidsgiver());
            var internArbeidsforholdRef = mapArbeidsforholdRef(arbeidsgiver, dto.getArbeidsforholdId());
            YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty())
                .medArbeidsforholdId(internArbeidsforholdRef)
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsgiverNavn(dto.getNavnArbeidsgiverUtland())
                .medArbeidType((dto.getType().getKode()));

            dto.getAktivitetsAvtaler()
                .forEach(aktivitetsAvtaleDto -> yrkesaktivitetBuilder.leggTilAktivitetsAvtale(mapAktivitetsAvtale(aktivitetsAvtaleDto)));

            dto.getPermisjoner()
                .forEach(permisjonDto -> yrkesaktivitetBuilder.leggTilPermisjon(mapPermisjon(permisjonDto, yrkesaktivitetBuilder.getPermisjonBuilder())));

            return yrkesaktivitetBuilder;
        }

        private Permisjon mapPermisjon(PermisjonDto dto, PermisjonBuilder permisjonBuilder) {
            return permisjonBuilder
                .medPeriode(dto.getPeriode().getFom(), dto.getPeriode().getTom())
                .medPermisjonsbeskrivelseType(dto.getType().getKode())
                .medProsentsats(dto.getProsentsats())
                .build();
        }

        private AktivitetsAvtaleBuilder mapAktivitetsAvtale(AktivitetsAvtaleDto dto) {
            return AktivitetsAvtaleBuilder.ny()
                .medBeskrivelse(dto.getBeskrivelse())
                .medPeriode(mapPeriode(dto.getPeriode()))
                .medProsentsats(dto.getStillingsprosent())
                .medSisteLønnsendringsdato(dto.getSistLønnsendring());
        }

        private DatoIntervallEntitet mapPeriode(Periode periode) {
            return DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
        }

        private ArbeidsforholdRef mapArbeidsforholdRef(@SuppressWarnings("unused") Arbeidsgiver arbeidsgiver, ArbeidsforholdRefDto arbeidsforholdId) {
            if (arbeidsforholdId == null) {
                return ArbeidsforholdRef.ref(null);
            }
            // FIXME : Abakus skal lage ny intern referanse
            // det som kommer i arbeidsforholdId er aaregistererferans
            return ArbeidsforholdRef.ref(arbeidsforholdId.getEksternReferanse());
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
            if (arbeidsgiver.getErOrganisasjon()) {
                return Arbeidsgiver.virksomhet(new OrgNummer(arbeidsgiver.getIdent()));
            }
            return Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));
        }
    }

    static class MapTilDto {

        private ArbeidsforholdInformasjon arbeidsforholdInformasjon;

        public MapTilDto(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
            this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
        }

        List<ArbeidDto> map(Collection<AktørArbeid> aktørArbeid) {
            if(aktørArbeid==null || aktørArbeid.isEmpty()) {
                return Collections.emptyList();
            }
            return aktørArbeid.stream().map(this::map).collect(Collectors.toList());
        }

        private ArbeidDto map(AktørArbeid arb) {
            List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>(getYrkesaktiviteter(arb.getYrkesaktiviteter()));
            List<YrkesaktivitetDto> frilansOppdrag = getYrkesaktiviteter(arb.getFrilansOppdrag());
            yrkesaktiviteter.addAll(frilansOppdrag);
            var dto = new ArbeidDto(new AktørIdPersonident(arb.getAktørId().getId()))
                .medYrkesaktiviteter(yrkesaktiviteter);
            return dto;
        }

        private List<YrkesaktivitetDto> getYrkesaktiviteter(Collection<Yrkesaktivitet> aktiviteter) {
            return aktiviteter.stream().map(this::mapYrkesaktivitet).collect(Collectors.toList());
        }

        private AktivitetsAvtaleDto map(AktivitetsAvtale aa) {
            var avtale = new AktivitetsAvtaleDto(aa.getFraOgMed(), aa.getTilOgMed())
                .medBeskrivelse(aa.getBeskrivelse())
                .medSistLønnsendring(aa.getSisteLønnsendringsdato())
                .medStillingsprosent(aa.getProsentsats() != null ? aa.getProsentsats().getVerdi() : null);
            return avtale;
        }

        private PermisjonDto map(Permisjon p) {
            var permisjon = new PermisjonDto(new Periode(p.getFraOgMed(), p.getTilOgMed()),
                new PermisjonsbeskrivelseType(p.getPermisjonsbeskrivelseType().getKode()))
                    .medProsentsats(p.getProsentsats().getVerdi());
            return permisjon;
        }

        private YrkesaktivitetDto mapYrkesaktivitet(Yrkesaktivitet a) {
            var aktivitetsAvtaler = a.getAlleAktivitetsAvtaler().stream().map(this::map).collect(Collectors.toList());

            var permisjoner = a.getPermisjon().stream().map(this::map).collect(Collectors.toList());

            var arbeidsforholdId = mapArbeidsforholdsId(a.getArbeidsgiver(), a);

            var arbeidType = new ArbeidType(a.getArbeidType().getKode());
            var dto = new YrkesaktivitetDto(mapAktør(a.getArbeidsgiver()), arbeidType)
                .medAktivitetsAvtaler(aktivitetsAvtaler)
                .medPermisjoner(permisjoner)
                .medArbeidsforholdId(arbeidsforholdId)
                .medNavnArbeidsgiverUtland(a.getNavnArbeidsgiverUtland());

            return dto;
        }

        private ArbeidsforholdRefDto mapArbeidsforholdsId(Arbeidsgiver arbeidsgiver, Yrkesaktivitet yrkesaktivitet) {
            var internRef = yrkesaktivitet.getArbeidsforholdRef();
            var eksternRef = arbeidsforholdInformasjon == null
                ? null
                : arbeidsforholdInformasjon.finnEkstern(arbeidsgiver, InternArbeidsforholdRef.ref(internRef.getReferanse()));
            return new ArbeidsforholdRefDto(internRef.getReferanse(), eksternRef.getReferanse(), Fagsystem.AAREGISTERET);
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiver) {
            return arbeidsgiver.erAktørId()
                ? new AktørIdPersonident(arbeidsgiver.getAktørId().getId())
                : new Organisasjon(arbeidsgiver.getOrgnr().getId());
        }

    }

}
