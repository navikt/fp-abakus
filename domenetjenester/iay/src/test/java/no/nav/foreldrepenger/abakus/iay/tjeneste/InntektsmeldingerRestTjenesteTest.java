package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektsmeldingerTjeneste;
import no.nav.foreldrepenger.abakus.iay.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.JournalpostId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.UuidDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektsmeldingerMottattRequest;

public class InntektsmeldingerRestTjenesteTest {

    public static final String SAKSNUMMER = "1234123412345";
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private final KoblingRepository repository = new KoblingRepository(repositoryRule.getEntityManager());
    private final KoblingTjeneste koblingTjeneste = new KoblingTjeneste(repository, new LåsRepository(repositoryRule.getEntityManager()));
    private final InntektArbeidYtelseRepository iayRepository = new InntektArbeidYtelseRepository(repositoryRule.getEntityManager());

    private InntektsmeldingerTjeneste imTjenesten;
    private InntektsmeldingerRestTjeneste tjeneste;

    @Before
    public void setUp() throws Exception {
        imTjenesten = new InntektsmeldingerTjeneste(iayRepository);
        tjeneste = new InntektsmeldingerRestTjeneste(imTjenesten, koblingTjeneste);
    }

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
        InntektsmeldingerDto ims = imTjenesten.hentAlleInntektsmeldingerForSak(aktørId, saksnummer, foreldrepenger);

        // Assert
        assertThat(ims.getInntektsmeldinger().size()).isEqualTo(1);
    }

    @Test
    public void skal_lagre_kobling_og_inntektsmelding() {
        InntektsmeldingerDto im = new InntektsmeldingerDto()
            .medInntektsmeldinger(List.of(new InntektsmeldingDto(new Organisasjon("999999999"), new JournalpostId(UUID.randomUUID().toString()), LocalDateTime.now(), LocalDate.now())
                .medStartDatoPermisjon(LocalDate.now())
                .medInntektBeløp(1)
                .medInnsendingsårsak(InntektsmeldingInnsendingsårsakType.NY)
                .medArbeidsforholdRef(new ArbeidsforholdRefDto(UUID.randomUUID().toString(), "ALTINN-01", Fagsystem.AAREGISTERET))
                .medKanalreferanse("KANAL")
                .medKildesystem("Altinn")));
        InntektsmeldingerMottattRequest request = new InntektsmeldingerMottattRequest(SAKSNUMMER, UUID.randomUUID(), new AktørIdPersonident("1234123412341"), im);

        UuidDto uuidDto = tjeneste.mottaInntektsmeldinger(request);

        assertThat(uuidDto).isNotNull();

        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = iayRepository.hentInntektArbeidYtelseForReferanse(new GrunnlagReferanse(uuidDto.getReferanse()));

        assertThat(grunnlagOpt).isPresent();
        assertThat(grunnlagOpt.flatMap(InntektArbeidYtelseGrunnlag::getInntektsmeldinger).map(InntektsmeldingAggregat::getInntektsmeldinger))
            .hasValueSatisfying(imer -> assertThat(imer).hasSize(1));
        assertThat(grunnlagOpt.flatMap(InntektArbeidYtelseGrunnlag::getArbeidsforholdInformasjon).map(ArbeidsforholdInformasjon::getArbeidsforholdReferanser))
            .hasValueSatisfying(imer -> assertThat(imer).hasSize(1));
    }
}
