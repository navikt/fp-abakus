package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Permisjon;
import no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetEntitet.AktivitetsAvtaleBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetEntitet.PermisjonBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
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
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class MapAktørArbeid {
    private class MapFraDto {

        private AktørId aktørId;
        private InntektArbeidYtelseAggregatBuilder registerData;

        MapFraDto(InntektArbeidYtelseAggregatBuilder registerData, AktørId aktørId) {
            this.registerData = registerData;
            this.aktørId = aktørId;
        }

        List<AktørArbeidBuilder> map(Collection<ArbeidDto> aktørArbeid) {
            return aktørArbeid.stream().map(this::mapAktørArbeid).collect(Collectors.toUnmodifiableList());
        }

        private AktørArbeidBuilder mapAktørArbeid(ArbeidDto dto) {
            var builder = registerData.getAktørArbeidBuilder(aktørId);
            dto.getYrkesaktiviteter().forEach(yrkesaktivitetDto -> builder.leggTilYrkesaktivitet(mapYrkesaktivitet(yrkesaktivitetDto)));
            return builder;
        }

        private YrkesaktivitetBuilder mapYrkesaktivitet(YrkesaktivitetDto dto) {
            YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty())
                .medArbeidsforholdId(mapArbeidsforholdRef(dto.getArbeidsforholdId()))
                .medArbeidsgiver(mapArbeidsgiver(dto.getArbeidsgiver()))
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

        private ArbeidsforholdRef mapArbeidsforholdRef(ArbeidsforholdRefDto arbeidsforholdId) {
            if (arbeidsforholdId == null) {
                return ArbeidsforholdRef.ref(null);
            }
            return ArbeidsforholdRef.ref(arbeidsforholdId.getAbakusReferanse());
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
            if (arbeidsgiver.getErOrganisasjon()) {
                return Arbeidsgiver.virksomhet(new OrgNummer(arbeidsgiver.getIdent()));
            }
            return Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));
        }
    }

    private class MapTilDto {
        List<ArbeidDto> map(Collection<AktørArbeid> aktørArbeid) {

            return aktørArbeid.stream()
                .map(arb -> {
                    List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>(getYrkesaktiviteter(arb.getYrkesaktiviteter()));
                    List<YrkesaktivitetDto> frilansOppdrag = getYrkesaktiviteter(arb.getFrilansOppdrag());
                    yrkesaktiviteter.addAll(frilansOppdrag);
                    return new ArbeidDto(new AktørIdPersonident(arb.getAktørId().getId()))
                        .medYrkesaktiviteter(yrkesaktiviteter);
                })
                .collect(Collectors.toList());
        }

        private List<YrkesaktivitetDto> getYrkesaktiviteter(Collection<Yrkesaktivitet> aktiviteter) {
            return aktiviteter.stream().map(a -> mapYrkesaktivitet(a)).collect(Collectors.toList());
        }

        private YrkesaktivitetDto mapYrkesaktivitet(Yrkesaktivitet a) {
            var aktivitetsAvtaler = a.getAktivitetsAvtaler().stream()
                .map(aa -> new AktivitetsAvtaleDto(aa.getFraOgMed(), aa.getTilOgMed())
                    .medSistLønnsendring(aa.getSisteLønnsendringsdato())
                    .medStillingsprosent(aa.getProsentsatsVerdi()))
                .collect(Collectors.toList());

            var ansettelsesperioder = a.getAnsettelsesPerioder().stream()
                .map(ap -> new Periode(ap.getFraOgMed(), ap.getTilOgMed()))
                .collect(Collectors.toList());

            var permisjoner = a.getPermisjon().stream()
                .map(p -> new PermisjonDto(new Periode(p.getFraOgMed(), p.getTilOgMed()),
                    new PermisjonsbeskrivelseType(p.getPermisjonsbeskrivelseType().getKode()))
                        .medProsentsats(p.getProsentsats().getVerdi()))
                .collect(Collectors.toList());

            var arbeidsforholdId = mapArbeidsforholdsId(a);

            var dto = new YrkesaktivitetDto(mapAktør(a.getArbeidsgiver()), new ArbeidType(a.getArbeidType().getKode()))
                .medAktivitetsAvtaler(aktivitetsAvtaler)
                .medAnsettelsesperiode(ansettelsesperioder)
                .medPermisjoner(permisjoner)
                .medArbeidsforholdId(arbeidsforholdId);

            return dto;
        }

        private ArbeidsforholdRefDto mapArbeidsforholdsId(Yrkesaktivitet yrkesaktivitet) {
            String internId = yrkesaktivitet.getArbeidsforholdRef().map(ArbeidsforholdRef::getReferanse).orElse(null);
            if (internId != null) {
                String eksternReferanse = tjeneste
                    .finnReferanseFor(koblingReferanse, yrkesaktivitet.getArbeidsgiver(), yrkesaktivitet.getArbeidsforholdRef().orElse(null), true)
                    .getReferanse();
                return new ArbeidsforholdRefDto(internId, eksternReferanse);
            }
            return new ArbeidsforholdRefDto(internId, null);
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiver) {
            return arbeidsgiver.erAktørId()
                ? new AktørIdPersonident(arbeidsgiver.getAktørId().getId())
                : new Organisasjon(arbeidsgiver.getOrgnr().getId());
        }

    }

    private InntektArbeidYtelseTjeneste tjeneste;
    private KoblingReferanse koblingReferanse;

    public MapAktørArbeid(InntektArbeidYtelseTjeneste tjeneste, KoblingReferanse koblingReferanse) {
        this.tjeneste = tjeneste;
        this.koblingReferanse = koblingReferanse;
    }

    public List<ArbeidDto> mapTilDto(Collection<AktørArbeid> aktørArbeid) {
        return new MapTilDto().map(aktørArbeid);
    }

    public List<AktørArbeidBuilder> mapFraDto(AktørId aktørId, InntektArbeidYtelseAggregatBuilder aggregatBuilder, Collection<ArbeidDto> aktørArbeid) {
        return new MapFraDto(aggregatBuilder, aktørId).map(aktørArbeid);
    }
}
