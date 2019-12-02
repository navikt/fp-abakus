package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;

public class MapInntektsmeldingerTest {

    public static final String SAKSNUMMER = "1234123412345";
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private final KoblingRepository repository = new KoblingRepository(repositoryRule.getEntityManager());
    private final KoblingTjeneste koblingTjeneste = new KoblingTjeneste(repository, new LåsRepository(repositoryRule.getEntityManager()));
    private final InntektArbeidYtelseRepository iayRepository = new InntektArbeidYtelseRepository(repositoryRule.getEntityManager());
    private final InntektArbeidYtelseTjeneste iayTjeneste = new InntektArbeidYtelseTjeneste(iayRepository);

    @Test
    public void skal_hente_alle_inntektsmeldinger_for_fagsak_uten_duplikater() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        KoblingReferanse koblingReferanse2 = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        YtelseType foreldrepenger = YtelseType.FORELDREPENGER;
        Kobling kobling1 = new Kobling(saksnummer, koblingReferanse, aktørId);
        kobling1.setYtelseType(foreldrepenger);
        Kobling kobling2 = new Kobling(saksnummer, koblingReferanse2, aktørId);
        kobling2.setYtelseType(foreldrepenger);
        koblingTjeneste.lagre(kobling1);
        koblingTjeneste.lagre(kobling2);
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("910909088")))
            .medBeløp(BigDecimal.TEN)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        iayRepository.lagre(koblingReferanse, ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(im));
        iayRepository.lagre(koblingReferanse2, ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(im));

        // Act
        Map<ArbeidsforholdInformasjon, Set<Inntektsmelding>> alleIm = iayTjeneste.hentArbeidsforholdinfoInntektsmeldingerMapFor(aktørId, saksnummer, foreldrepenger);
        InntektsmeldingerDto inntektsmeldingerDto = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(alleIm);

        // Assert
        assertThat(inntektsmeldingerDto.getInntektsmeldinger().size()).isEqualTo(1);
    }

}
