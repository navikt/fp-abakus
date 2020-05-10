package no.nav.foreldrepenger.abakus.domene.iay;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class InntektArbeidYtelseRepositoryTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    private InntektArbeidYtelseRepository repository = new InntektArbeidYtelseRepository(repositoryRule.getEntityManager());
    private KoblingRepository koblingRepository = new KoblingRepository(repositoryRule.getEntityManager());

    @Test
    public void skal_svare_om_er_siste() {
        final var ko = new Kobling(new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), new AktørId("1231231231223"));
        ko.setYtelseType(YtelseType.OMSORGSPENGER);
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        final var builder = OppgittOpptjeningBuilder.ny();
        builder.leggTilAnnenAktivitet(new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.VENTELØNN_VARTPENGER));

        final var grunnlagReferanse = repository.lagre(ko.getKoblingReferanse(), builder);

        assertThat(repository.erGrunnlagAktivt(grunnlagReferanse.getReferanse())).isTrue();

        final var inntektsmeldingBuilder = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("000000000")))
            .medBeløp(BigDecimal.TEN)
            .medJournalpostId("123123123")
            .medMottattDato(LocalDate.now())
            .medInnsendingstidspunkt(LocalDateTime.now());

        final var nyGrunnlagReferanse = repository.lagre(ko.getKoblingReferanse(), ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(inntektsmeldingBuilder.build()));

        assertThat(repository.erGrunnlagAktivt(grunnlagReferanse.getReferanse())).isFalse();
        assertThat(repository.erGrunnlagAktivt(nyGrunnlagReferanse.getReferanse())).isTrue();
    }

    @Test
    public void skal_kunne_lagre_overstyring_av_oppgitt_opptjening() {
        final var ko = new Kobling(new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), new AktørId("1231231231223"));
        ko.setYtelseType(YtelseType.OMSORGSPENGER);
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        final var builder = OppgittOpptjeningBuilder.ny();
        builder.leggTilAnnenAktivitet(new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.VENTELØNN_VARTPENGER));

        repository.lagreOverstyring(ko.getKoblingReferanse(), builder);

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = repository.hentInntektArbeidYtelseGrunnlagForBehandling(ko.getKoblingReferanse());

        assertThat(inntektArbeidYtelseGrunnlag).isPresent();
        assertThat(inntektArbeidYtelseGrunnlag.get().getOverstyrtOppgittOpptjening()).isPresent();
    }

    @Test
    public void skal_kunne_lagre_både_vanlig_og_overstyring_av_oppgitt_opptjening() {
        final var ko = new Kobling(new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), new AktørId("1231231231223"));
        ko.setYtelseType(YtelseType.OMSORGSPENGER);
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        final var vanlig = OppgittOpptjeningBuilder.ny();
        OppgittAnnenAktivitet annenAktivitet = new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.ETTERLØNN_SLUTTPAKKE);
        vanlig.leggTilAnnenAktivitet(annenAktivitet);

        final var overstyring = OppgittOpptjeningBuilder.ny();
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
    public void skal_kunne_lagre_overstyring_av_oppgitt_opptjening_flere_ganger() {
        final var ko = new Kobling(new Saksnummer("12341234"), new KoblingReferanse(UUID.randomUUID()), new AktørId("1231231231223"));
        ko.setYtelseType(YtelseType.OMSORGSPENGER);
        ko.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(2), LocalDate.now()));
        koblingRepository.lagre(ko);

        final var overstyring1 = OppgittOpptjeningBuilder.ny();
        OppgittAnnenAktivitet annenAktivitetoverstyring1 = new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.ETTERLØNN_SLUTTPAKKE);
        overstyring1.leggTilAnnenAktivitet(annenAktivitetoverstyring1);


        repository.lagreOverstyring(ko.getKoblingReferanse(), overstyring1);

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = repository.hentInntektArbeidYtelseGrunnlagForBehandling(ko.getKoblingReferanse());
        assertThat(inntektArbeidYtelseGrunnlag).isPresent();
        Optional<OppgittOpptjening> overstyrtOppgittOpptjening = inntektArbeidYtelseGrunnlag.get().getOverstyrtOppgittOpptjening();
        assertThat(overstyrtOppgittOpptjening).isPresent();
        assertThat(overstyrtOppgittOpptjening.get().getAnnenAktivitet()).containsExactly(annenAktivitetoverstyring1);

        final var overstyring2 = OppgittOpptjeningBuilder.ny();
        OppgittAnnenAktivitet annenAktivitetoverstyring2 = new OppgittAnnenAktivitet(IntervallEntitet.fraOgMed(LocalDate.now()), ArbeidType.VENTELØNN_VARTPENGER);
        overstyring2.leggTilAnnenAktivitet(annenAktivitetoverstyring2);
        repository.lagreOverstyring(ko.getKoblingReferanse(), overstyring2);

        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag2 = repository.hentInntektArbeidYtelseGrunnlagForBehandling(ko.getKoblingReferanse());

        assertThat(inntektArbeidYtelseGrunnlag2).isPresent();
        Optional<OppgittOpptjening> overstyrtOppgittOpptjening2 = inntektArbeidYtelseGrunnlag2.get().getOverstyrtOppgittOpptjening();
        assertThat(overstyrtOppgittOpptjening2).isPresent();
        assertThat(overstyrtOppgittOpptjening2.get().getAnnenAktivitet()).containsExactly(annenAktivitetoverstyring2);
    }
}
