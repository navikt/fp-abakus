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
import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
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
}
