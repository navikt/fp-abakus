package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.PersonIdent;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.ArbeidDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.PermisjonDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.YrkesaktivitetDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class MapAktørArbeid {

    private static final Comparator<YrkesaktivitetDto> COMP_YRKESAKTIVITET = Comparator
        .comparing((YrkesaktivitetDto dto) -> dto.getArbeidsgiver().map(Aktør::getIdent).orElse(null), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getArbeidsforholdId() == null ? null : dto.getArbeidsforholdId().getAbakusReferanse(),
            Comparator.nullsFirst(Comparator.naturalOrder()));

    private static final Comparator<AktivitetsAvtaleDto> COMP_AKTIVITETSAVTALE = Comparator
        .comparing((AktivitetsAvtaleDto dto) -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<PermisjonDto> COMP_PERMISJON = Comparator
        .comparing((PermisjonDto dto) -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));;

    static class MapFraDto {

        @SuppressWarnings("unused")
        private AktørId søkerAktørId;

        private InntektArbeidYtelseAggregatBuilder registerData;

        MapFraDto(AktørId søkerAktørId, InntektArbeidYtelseAggregatBuilder registerData) {
            this.registerData = registerData;
            this.søkerAktørId = søkerAktørId;
        }

        List<AktørArbeidBuilder> map(Collection<ArbeidDto> dtos) {
            if (dtos == null || dtos.isEmpty()) {
                return Collections.emptyList();
            }
            return dtos.stream().map(this::mapAktørArbeid).collect(Collectors.toUnmodifiableList());
        }

        private AktørArbeidBuilder mapAktørArbeid(ArbeidDto dto) {
            var builder = registerData.getAktørArbeidBuilder(tilAktørId(dto.getPerson()));
            dto.getYrkesaktiviteter().forEach(yrkesaktivitetDto -> builder.leggTilYrkesaktivitet(mapYrkesaktivitet(yrkesaktivitetDto)));
            return builder;
        }

        /** Returnerer person sin aktørId. Denne trenger ikke være samme som søkers aktørid men kan f.eks. være annen part i en sak. */
        private AktørId tilAktørId(PersonIdent person) {
            if (!(person instanceof AktørIdPersonident)) {
                throw new IllegalArgumentException("Støtter kun " + AktørIdPersonident.class.getSimpleName() + " her");
            }
            return new AktørId(person.getIdent());
        }

        private YrkesaktivitetBuilder mapYrkesaktivitet(YrkesaktivitetDto dto) {
            var arbeidsgiver = dto.getArbeidsgiver().map(this::mapArbeidsgiver).orElse(null);
            var internArbeidsforholdRef = arbeidsgiver == null ? null : mapArbeidsforholdRef(arbeidsgiver, dto.getArbeidsforholdId());

            YrkesaktivitetBuilder yrkesaktivitetBuilder = YrkesaktivitetBuilder.oppdatere(Optional.empty())
                .medArbeidsforholdId(internArbeidsforholdRef)
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsgiverNavn(dto.getNavnArbeidsgiverUtland())
                .medArbeidType(KodeverkMapper.mapArbeidType(dto.getType()));

            dto.getAktivitetsAvtaler()
                .forEach(aktivitetsAvtaleDto -> yrkesaktivitetBuilder.leggTilAktivitetsAvtale(mapAktivitetsAvtale(aktivitetsAvtaleDto)));

            dto.getPermisjoner()
                .forEach(permisjonDto -> yrkesaktivitetBuilder.leggTilPermisjon(mapPermisjon(permisjonDto, yrkesaktivitetBuilder.getPermisjonBuilder())));

            return yrkesaktivitetBuilder;
        }

        private Permisjon mapPermisjon(PermisjonDto dto, PermisjonBuilder permisjonBuilder) {
            return permisjonBuilder
                .medPeriode(dto.getPeriode().getFom(), dto.getPeriode().getTom())
                .medPermisjonsbeskrivelseType(KodeverkMapper.mapPermisjonbeskrivelseTypeFraDto(dto.getType()).getKode())
                .medProsentsats(dto.getProsentsats())
                .build();
        }

        private AktivitetsAvtaleBuilder mapAktivitetsAvtale(AktivitetsAvtaleDto dto) {
            return AktivitetsAvtaleBuilder.ny()
                .medBeskrivelse(dto.getBeskrivelse())
                .medPeriode(mapPeriode(dto.getPeriode()))
                .medProsentsats(dto.getStillingsprosent())
                .medAntallTimer(dto.getAntallTimer())
                .medAntallTimerFulltid(dto.getAntallTimerFulltid())
                .medSisteLønnsendringsdato(dto.getSistLønnsendring());
        }

        private DatoIntervallEntitet mapPeriode(Periode periode) {
            return DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
        }

        private InternArbeidsforholdRef mapArbeidsforholdRef(@SuppressWarnings("unused") Arbeidsgiver arbeidsgiver, ArbeidsforholdRefDto arbeidsforholdId) {
            if (arbeidsforholdId == null) {
                return InternArbeidsforholdRef.nullRef();
            }
            // intern referanse == abakus referanse.
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

        private ArbeidsforholdInformasjon arbeidsforholdInformasjon;

        public MapTilDto(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
            this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
        }

        List<ArbeidDto> map(Collection<AktørArbeid> aktørArbeid) {
            if (aktørArbeid == null || aktørArbeid.isEmpty()) {
                return Collections.emptyList();
            }
            return aktørArbeid.stream().map(this::map).collect(Collectors.toList());
        }

        private ArbeidDto map(AktørArbeid arb) {
            List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>(getYrkesaktiviteter(arb.hentAlleYrkesaktiviter()));

            var aktiviteter = yrkesaktiviteter.stream().filter(this::erGyldigYrkesaktivitet).sorted(COMP_YRKESAKTIVITET).collect(Collectors.toList());

            var dto = new ArbeidDto(new AktørIdPersonident(arb.getAktørId().getId()))
                .medYrkesaktiviteter(aktiviteter);
            return dto;
        }

        private boolean erGyldigYrkesaktivitet(YrkesaktivitetDto yrkesaktivitet) {
            return !yrkesaktivitet.getAktivitetsAvtaler().isEmpty() || !yrkesaktivitet.getPermisjoner().isEmpty();
        }

        private List<YrkesaktivitetDto> getYrkesaktiviteter(Collection<Yrkesaktivitet> aktiviteter) {
            return aktiviteter.stream().map(this::mapYrkesaktivitet).collect(Collectors.toList());
        }

        private AktivitetsAvtaleDto map(AktivitetsAvtale aa) {
            var avtale = new AktivitetsAvtaleDto(aa.getFraOgMed(), aa.getTilOgMed())
                .medBeskrivelse(aa.getBeskrivelse())
                .medSistLønnsendring(aa.getSisteLønnsendringsdato())
                .medAntallTimer(aa.getAntallTimer() == null ? null : aa.getAntallTimer().getSkalertVerdi())
                .medAntallTimerFulltid(aa.getAntallTimerFulltid() == null ? null : aa.getAntallTimerFulltid().getSkalertVerdi())
                .medStillingsprosent(aa.getProsentsats() == null ? null : aa.getProsentsats().getVerdi());
            return avtale;
        }

        private PermisjonDto map(Permisjon p) {
            var permisjonsbeskrivelseType = KodeverkMapper.mapPermisjonbeskrivelseTypeTilDto(p.getPermisjonsbeskrivelseType());
            var permisjon = new PermisjonDto(new Periode(p.getFraOgMed(), p.getTilOgMed()), permisjonsbeskrivelseType)
                .medProsentsats(p.getProsentsats() == null ? null : p.getProsentsats().getVerdi());
            return permisjon;
        }

        private YrkesaktivitetDto mapYrkesaktivitet(Yrkesaktivitet a) {
            var aktivitetsAvtaler = a.getAlleAktivitetsAvtaler().stream().map(this::map).sorted(COMP_AKTIVITETSAVTALE).collect(Collectors.toList());
            var permisjoner = a.getPermisjon().stream().map(this::map).sorted(COMP_PERMISJON).collect(Collectors.toList());

            var arbeidsforholdId = mapArbeidsforholdsId(a.getArbeidsgiver(), a);

            var arbeidType = KodeverkMapper.mapArbeidTypeTilDto(a.getArbeidType());
            var dto = new YrkesaktivitetDto(arbeidType)
                .medArbeidsgiver(mapAktør(a.getArbeidsgiver()))
                .medAktivitetsAvtaler(aktivitetsAvtaler)
                .medPermisjoner(permisjoner)
                .medArbeidsforholdId(arbeidsforholdId)
                .medNavnArbeidsgiverUtland(a.getNavnArbeidsgiverUtland());

            return dto;
        }

        private ArbeidsforholdRefDto mapArbeidsforholdsId(Arbeidsgiver arbeidsgiver, Yrkesaktivitet yrkesaktivitet) {
            var internRef = yrkesaktivitet.getArbeidsforholdRef();
            if (internRef == null || internRef.getReferanse() == null) {
                return null;
            }
            var eksternRef = arbeidsforholdInformasjon == null ? null : arbeidsforholdInformasjon.finnEkstern(arbeidsgiver, internRef);

            if (eksternRef == null || eksternRef.getReferanse() == null) {
                throw new java.lang.IllegalStateException("Mapping til Abakus: Savner eksternRef for internRef: " + internRef);
            }

            return new ArbeidsforholdRefDto(internRef.getReferanse(), eksternRef.getReferanse(),
                no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Fagsystem.AAREGISTERET);
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiver) {
            if (arbeidsgiver == null) {
                return null; // arbeidType='NÆRING' har null arbeidsgiver
            }
            return arbeidsgiver.erAktørId()
                ? new AktørIdPersonident(arbeidsgiver.getAktørId().getId())
                : new Organisasjon(arbeidsgiver.getOrgnr().getId());
        }

    }

}
