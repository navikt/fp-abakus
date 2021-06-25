package no.nav.foreldrepenger.abakus.domene.iay;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Fravær;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.cdi.CdiAwareExtension;

@ExtendWith(CdiAwareExtension.class)
public class InntektArbeidYtelseRepositoryTest {

    @RegisterExtension
    public static JpaExtension jpaExtension = new JpaExtension();

    private InntektArbeidYtelseRepository repository;
    private KoblingRepository koblingRepository;

    @BeforeEach
    public void setup() {
        repository = new InntektArbeidYtelseRepository(jpaExtension.getEntityManager());
        koblingRepository = new KoblingRepository(jpaExtension.getEntityManager());
    }

    @Test
    public void skal_svare_om_er_siste() {
        var ko = new Kobling(YtelseType.OMSORGSPENGER, new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), new AktørId("1231231231223"));
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        var builder = OppgittOpptjeningBuilder.ny();
        builder.leggTilAnnenAktivitet(new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.VENTELØNN_VARTPENGER));

        var grunnlagReferanse = repository.lagre(ko.getKoblingReferanse(), builder);

        assertThat(repository.erGrunnlagAktivt(grunnlagReferanse.getReferanse())).isTrue();

        var inntektsmeldingBuilder = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medBeløp(BigDecimal.TEN)
            .medJournalpostId("123123123")
            .medMottattDato(LocalDate.now())
            .medInnsendingstidspunkt(LocalDateTime.now());

        var nyGrunnlagReferanse = repository.lagre(ko.getKoblingReferanse(), ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(inntektsmeldingBuilder.build()));

        assertThat(repository.erGrunnlagAktivt(grunnlagReferanse.getReferanse())).isFalse();
        assertThat(repository.erGrunnlagAktivt(nyGrunnlagReferanse.getReferanse())).isTrue();
    }

    @Test
    public void skal_oppdatere_sigrun() {
        var aktør = new AktørId("1231231231223");
        var periodeFom = LocalDate.of(2020,1,1);
        var periodeTom = LocalDate.of(2020,12,31);
        var ko = new Kobling(YtelseType.FORELDREPENGER, new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), aktør);
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        var grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.nytt();
        var gb = grunnlagBuilder.getRegisterBuilder();

        var aib = gb.getAktørInntektBuilder(aktør);
        var ib = aib.getInntektBuilder(InntektskildeType.SIGRUN, null);
        var ipb = ib.getInntektspostBuilder().medInntektspostType(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE).medBeløp(BigDecimal.TEN).medPeriode(periodeFom, periodeTom);
        var ipba = ib.getInntektspostBuilder().medInntektspostType(InntektspostType.LØNN).medBeløp(BigDecimal.TEN).medPeriode(periodeFom, periodeTom);
        ib.leggTilInntektspost(ipb);ib.leggTilInntektspost(ipba);
        aib.leggTilInntekt(ib);
        gb.leggTilAktørInntekt(aib);

        repository.lagre(ko.getKoblingReferanse(), grunnlagBuilder);

        var grunnlagBuilder2 = InntektArbeidYtelseGrunnlagBuilder.oppdatere(repository.hentInntektArbeidYtelseGrunnlagForBehandling(ko.getKoblingReferanse()));
        var gb2 = grunnlagBuilder2.getRegisterBuilder();
        var aib2 = gb2.getAktørInntektBuilder(aktør);
        var ib2 = aib2.getInntektBuilder(InntektskildeType.SIGRUN, null);
        ib2.tilbakestillInntektsposterForPerioder(Set.of(IntervallEntitet.fraOgMedTilOgMed(periodeFom, periodeTom)));
        var ipb2 = ib2.getInntektspostBuilder().medInntektspostType(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE).medBeløp(BigDecimal.ONE).medPeriode(periodeFom, periodeTom);
        var ipba2 = ib.getInntektspostBuilder().medInntektspostType(InntektspostType.LØNN).medBeløp(BigDecimal.TEN).medPeriode(periodeFom, periodeTom);

        ib2.leggTilInntektspost(ipba2);ib2.leggTilInntektspost(ipb2);
        aib2.leggTilInntekt(ib2);
        gb2.leggTilAktørInntekt(aib2);

        repository.lagre(ko.getKoblingReferanse(), grunnlagBuilder2);

        var g3 = repository.hentInntektArbeidYtelseGrunnlagForBehandling(ko.getKoblingReferanse()).orElseThrow();

        assertThat(g3.getRegisterVersjon().flatMap(a -> a.getAktørInntekt().stream().filter(ai -> ai.getAktørId().equals(aktør)).findFirst())
            .flatMap(ai -> ai.getInntekt().stream().findFirst())
            .flatMap(i -> i.getAlleInntektsposter().stream().filter(ip -> ip.getInntektspostType().equals(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE)).findFirst())
            .map(Inntektspost::getBeløp).orElse(Beløp.ZERO)).isEqualTo(new Beløp(BigDecimal.ONE));
    }

    @Test
    public void skal_kunne_lagre_overstyring_av_oppgitt_opptjening() {
        var ko = new Kobling(YtelseType.OMSORGSPENGER, new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), new AktørId("1231231231223"));
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        var builder = OppgittOpptjeningBuilder.ny();
        builder.leggTilAnnenAktivitet(new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.VENTELØNN_VARTPENGER));

        repository.lagreOverstyring(ko.getKoblingReferanse(), builder);

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = repository.hentInntektArbeidYtelseGrunnlagForBehandling(ko.getKoblingReferanse());

        assertThat(inntektArbeidYtelseGrunnlag).isPresent();
        assertThat(inntektArbeidYtelseGrunnlag.get().getOverstyrtOppgittOpptjening()).isPresent();
    }

    @Test
    public void skal_kunne_lagre_både_vanlig_og_overstyring_av_oppgitt_opptjening() {
        var ko = new Kobling(YtelseType.OMSORGSPENGER, new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), new AktørId("1231231231223"));
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        var vanlig = OppgittOpptjeningBuilder.ny();
        OppgittAnnenAktivitet annenAktivitet = new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.ETTERLØNN_SLUTTPAKKE);
        vanlig.leggTilAnnenAktivitet(annenAktivitet);

        var overstyring = OppgittOpptjeningBuilder.ny();
        OppgittAnnenAktivitet overstrytAnnenAktivitet = new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.VENTELØNN_VARTPENGER);
        overstyring.leggTilAnnenAktivitet(overstrytAnnenAktivitet);

        repository.lagre(ko.getKoblingReferanse(), vanlig);
        repository.lagreOverstyring(ko.getKoblingReferanse(), overstyring);

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = repository.hentInntektArbeidYtelseGrunnlagForBehandling(ko.getKoblingReferanse());

        assertThat(inntektArbeidYtelseGrunnlag).isPresent();
        Optional<OppgittOpptjening> overstyrtOppgittOpptjening = inntektArbeidYtelseGrunnlag.get().getOverstyrtOppgittOpptjening();
        Optional<OppgittOpptjening> oppgittOpptjening = inntektArbeidYtelseGrunnlag.get().getOppgittOpptjening();
        assertThat(overstyrtOppgittOpptjening).isPresent();
        assertThat(oppgittOpptjening).isPresent();
        assertThat(overstyrtOppgittOpptjening.get().getAnnenAktivitet()).containsExactly(overstrytAnnenAktivitet);
        assertThat(oppgittOpptjening.get().getAnnenAktivitet()).containsExactly(annenAktivitet);
    }

    @Test
    public void skal_kunne_hente_oppgitt_opptjening() {
        var ko = new Kobling(YtelseType.OMSORGSPENGER, new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), new AktørId("1231231231223"));
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        var vanlig = OppgittOpptjeningBuilder.ny();
        OppgittAnnenAktivitet annenAktivitet = new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.ETTERLØNN_SLUTTPAKKE);
        vanlig.leggTilAnnenAktivitet(annenAktivitet);

        var overstyring = OppgittOpptjeningBuilder.ny();
        OppgittAnnenAktivitet overstrytAnnenAktivitet = new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.VENTELØNN_VARTPENGER);
        overstyring.leggTilAnnenAktivitet(overstrytAnnenAktivitet);

        repository.lagre(ko.getKoblingReferanse(), vanlig);
        repository.lagreOverstyring(ko.getKoblingReferanse(), overstyring);

        var oppgittOpptjeningVanlig = repository.hentOppgittOpptjeningFor(vanlig.getEksternReferanse());
        var oppgittOpptjeningOverstyring = repository.hentOppgittOpptjeningFor(overstyring.getEksternReferanse());

        assertThat(oppgittOpptjeningVanlig).isNotEmpty();
        assertThat(oppgittOpptjeningOverstyring).isNotEmpty();
    }


    @Test
    public void skal_kunne_lagre_overstyring_av_oppgitt_opptjening_flere_ganger() {
        var ko = new Kobling(YtelseType.OMSORGSPENGER, new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), new AktørId("1231231231223"));
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        var overstyring1 = OppgittOpptjeningBuilder.ny();
        OppgittAnnenAktivitet annenAktivitetoverstyring1 = new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.ETTERLØNN_SLUTTPAKKE);
        overstyring1.leggTilAnnenAktivitet(annenAktivitetoverstyring1);


        repository.lagreOverstyring(ko.getKoblingReferanse(), overstyring1);

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = repository.hentInntektArbeidYtelseGrunnlagForBehandling(ko.getKoblingReferanse());
        assertThat(inntektArbeidYtelseGrunnlag).isPresent();
        Optional<OppgittOpptjening> overstyrtOppgittOpptjening = inntektArbeidYtelseGrunnlag.get().getOverstyrtOppgittOpptjening();
        assertThat(overstyrtOppgittOpptjening).isPresent();
        assertThat(overstyrtOppgittOpptjening.get().getAnnenAktivitet()).containsExactly(annenAktivitetoverstyring1);

        var overstyring2 = OppgittOpptjeningBuilder.ny();
        OppgittAnnenAktivitet annenAktivitetoverstyring2 = new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.VENTELØNN_VARTPENGER);
        overstyring2.leggTilAnnenAktivitet(annenAktivitetoverstyring2);
        repository.lagreOverstyring(ko.getKoblingReferanse(), overstyring2);

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag2 = repository.hentInntektArbeidYtelseGrunnlagForBehandling(ko.getKoblingReferanse());

        assertThat(inntektArbeidYtelseGrunnlag2).isPresent();
        Optional<OppgittOpptjening> overstyrtOppgittOpptjening2 = inntektArbeidYtelseGrunnlag2.get().getOverstyrtOppgittOpptjening();
        assertThat(overstyrtOppgittOpptjening2).isPresent();
        assertThat(overstyrtOppgittOpptjening2.get().getAnnenAktivitet()).containsExactly(annenAktivitetoverstyring2);
    }

    @Test
    public void skal_ta_vare_på_utdatert_inntektsmeldinger_basert_på_kanalreferanse() {
        var aktørId = new AktørId("1231231231223");
        var koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = new Saksnummer("12341234");
        var ko = new Kobling(YtelseType.OMSORGSPENGER, saksnummer, koblingReferanse, aktørId);
        LocalDateTime now = LocalDateTime.now();
        LocalDate idag = now.toLocalDate();
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(idag.minusYears(2), idag));
        koblingRepository.lagre(ko);

        var inntektsmelding1 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medJournalpostId("1")
            .medInnsendingstidspunkt(now.minusDays(10))
            .medBeløp(BigDecimal.TEN)
            .medKanalreferanse("AR123")
            .leggTil(new Fravær(idag.minusDays(30), idag.minusDays(25), null))
            .medRefusjon(BigDecimal.TEN)
            .build();
        var inntektsmelding2 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medInnsendingstidspunkt(now.minusDays(9))
            .medJournalpostId("2")
            .medBeløp(BigDecimal.ONE)
            .medKanalreferanse("AR124")
            .leggTil(new Fravær(idag.minusDays(26), idag.minusDays(25), null))
            .medRefusjon(BigDecimal.ONE)
            .build();
        var inntektsmelding3 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medInnsendingstidspunkt(now.minusDays(9))
            .medJournalpostId("3")
            .medBeløp(BigDecimal.ONE)
            .medKanalreferanse("AR125")
            .leggTil(new Fravær(idag.minusDays(30), idag.minusDays(25), null))
            .medRefusjon(BigDecimal.ONE)
            .build();
        var inntektsmelding4 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medInnsendingstidspunkt(now.minusDays(10))
            .medJournalpostId("4")
            .medBeløp(BigDecimal.ONE)
            .medKanalreferanse("AR126")
            .leggTil(new Fravær(idag, idag.plusDays(5), null))
            .medRefusjon(BigDecimal.ONE)
            .build();

        repository.lagre(ko.getKoblingReferanse(), ArbeidsforholdInformasjonBuilder.oppdatere(new ArbeidsforholdInformasjon()), List.of(inntektsmelding1, inntektsmelding2, inntektsmelding3, inntektsmelding4, inntektsmelding1));

        var grunnlag = repository.hentAlleInntektArbeidYtelseGrunnlagFor(aktørId, saksnummer, YtelseType.OMSORGSPENGER, false);

        assertThat(grunnlag).hasSize(4);

        var inntektsmeldings = grunnlag.stream()
            .map(InntektArbeidYtelseGrunnlag::getInntektsmeldinger)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(InntektsmeldingAggregat::getInntektsmeldinger).flatMap(Collection::stream).collect(Collectors.toList());
        assertThat(inntektsmeldings).hasSize(4);

        var aktivtGrunnlag = repository.hentInntektArbeidYtelseForBehandling(koblingReferanse);

        assertThat(aktivtGrunnlag.getInntektsmeldinger()).isPresent();
        assertThat(aktivtGrunnlag.getInntektsmeldinger().get().getInntektsmeldinger()).hasSize(1);
        assertThat(aktivtGrunnlag.getInntektsmeldinger().get().getInntektsmeldinger()).contains(inntektsmelding4);

    }

    @Test
    public void skal_kun_hente_aktivt_grunnlag() {
        var aktørId = new AktørId("1231231231223");
        var koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = new Saksnummer("12341234");
        var ko = new Kobling(YtelseType.OMSORGSPENGER, saksnummer, koblingReferanse, aktørId);
        LocalDateTime now = LocalDateTime.now();
        LocalDate idag = now.toLocalDate();
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(idag.minusYears(2), idag));
        koblingRepository.lagre(ko);

        var inntektsmelding1 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medJournalpostId("1")
            .medInnsendingstidspunkt(now.minusDays(10))
            .medBeløp(BigDecimal.TEN)
            .medKanalreferanse("AR123")
            .leggTil(new Fravær(idag.minusDays(30), idag.minusDays(25), null))
            .medRefusjon(BigDecimal.TEN)
            .build();
        var inntektsmelding2 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medInnsendingstidspunkt(now.minusDays(9))
            .medJournalpostId("2")
            .medBeløp(BigDecimal.ONE)
            .medKanalreferanse("AR124")
            .leggTil(new Fravær(idag.minusDays(26), idag.minusDays(25), null))
            .medRefusjon(BigDecimal.ONE)
            .build();
        var inntektsmelding3 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medInnsendingstidspunkt(now.minusDays(9))
            .medJournalpostId("3")
            .medBeløp(BigDecimal.ONE)
            .medKanalreferanse("AR125")
            .leggTil(new Fravær(idag.minusDays(30), idag.minusDays(25), null))
            .medRefusjon(BigDecimal.ONE)
            .build();
        var inntektsmelding4 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medInnsendingstidspunkt(now.minusDays(10))
            .medJournalpostId("4")
            .medBeløp(BigDecimal.ONE)
            .medKanalreferanse("AR126")
            .leggTil(new Fravær(idag, idag.plusDays(5), null))
            .medRefusjon(BigDecimal.ONE)
            .build();

        repository.lagre(ko.getKoblingReferanse(), ArbeidsforholdInformasjonBuilder.oppdatere(new ArbeidsforholdInformasjon()), List.of(inntektsmelding1, inntektsmelding2, inntektsmelding3, inntektsmelding4, inntektsmelding1));

        var grunnlag = repository.hentAlleInntektArbeidYtelseGrunnlagFor(aktørId, saksnummer, YtelseType.OMSORGSPENGER, true);

        assertThat(grunnlag).hasSize(1);

        var inntektsmeldings = grunnlag.stream()
            .map(InntektArbeidYtelseGrunnlag::getInntektsmeldinger)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(InntektsmeldingAggregat::getInntektsmeldinger).flatMap(Collection::stream).collect(Collectors.toList());
        assertThat(inntektsmeldings).hasSize(1);

        var aktivtGrunnlag = repository.hentInntektArbeidYtelseForBehandling(koblingReferanse);

        assertThat(aktivtGrunnlag.getInntektsmeldinger()).isPresent();
        assertThat(aktivtGrunnlag.getInntektsmeldinger().get().getInntektsmeldinger()).hasSize(1);
        assertThat(aktivtGrunnlag.getInntektsmeldinger().get().getInntektsmeldinger()).contains(inntektsmelding4);

    }

    @Test
    public void skal_ta_vare_på_utdatert_inntektsmeldinger_basert_på_innsendingstidspunkt_mangler_kanalreferanse() {
        var aktørId = new AktørId("1231231231223");
        var koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        var saksnummer = new Saksnummer("12341234");
        var ko = new Kobling(YtelseType.OMSORGSPENGER, saksnummer, koblingReferanse, aktørId);
        LocalDateTime now = LocalDateTime.now();
        LocalDate idag = now.toLocalDate();
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(idag.minusYears(2), idag));
        koblingRepository.lagre(ko);

        var inntektsmelding1 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medJournalpostId("1")
            .medInnsendingstidspunkt(now.minusDays(10))
            .medBeløp(BigDecimal.TEN)
            .medKanalreferanse("AR123")
            .leggTil(new Fravær(idag.minusDays(30), idag.minusDays(25), null))
            .medRefusjon(BigDecimal.TEN)
            .build();
        var inntektsmelding2 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medInnsendingstidspunkt(now.minusDays(9))
            .medJournalpostId("2")
            .medBeløp(BigDecimal.ONE)
            .leggTil(new Fravær(idag.minusDays(26), idag.minusDays(25), null))
            .medRefusjon(BigDecimal.ONE)
            .build();
        var inntektsmelding3 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medInnsendingstidspunkt(now.minusDays(9))
            .medJournalpostId("3")
            .medBeløp(BigDecimal.ONE)
            .leggTil(new Fravær(idag.minusDays(30), idag.minusDays(25), null))
            .medRefusjon(BigDecimal.ONE)
            .build();
        var inntektsmelding4 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medArbeidsforholdId(InternArbeidsforholdRef.nullRef())
            .medArbeidsforholdId(EksternArbeidsforholdRef.nullRef())
            .medInnsendingstidspunkt(now.minusDays(10))
            .medJournalpostId("4")
            .medBeløp(BigDecimal.ONE)
            .leggTil(new Fravær(idag, idag.plusDays(5), null))
            .medRefusjon(BigDecimal.ONE)
            .build();

        repository.lagre(ko.getKoblingReferanse(), ArbeidsforholdInformasjonBuilder.oppdatere(new ArbeidsforholdInformasjon()), List.of(inntektsmelding1, inntektsmelding2, inntektsmelding3, inntektsmelding4, inntektsmelding1));

        var grunnlag = repository.hentAlleInntektArbeidYtelseGrunnlagFor(aktørId, saksnummer, YtelseType.OMSORGSPENGER, false);

        assertThat(grunnlag).hasSize(4);

        var inntektsmeldings = grunnlag.stream()
            .map(InntektArbeidYtelseGrunnlag::getInntektsmeldinger)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(InntektsmeldingAggregat::getInntektsmeldinger).flatMap(Collection::stream).collect(Collectors.toList());
        assertThat(inntektsmeldings).hasSize(4);

        var aktivtGrunnlag = repository.hentInntektArbeidYtelseForBehandling(koblingReferanse);

        assertThat(aktivtGrunnlag.getInntektsmeldinger()).isPresent();
        assertThat(aktivtGrunnlag.getInntektsmeldinger().get().getInntektsmeldinger()).hasSize(1);
        assertThat(aktivtGrunnlag.getInntektsmeldinger().get().getInntektsmeldinger()).contains(inntektsmelding3);

    }
}
