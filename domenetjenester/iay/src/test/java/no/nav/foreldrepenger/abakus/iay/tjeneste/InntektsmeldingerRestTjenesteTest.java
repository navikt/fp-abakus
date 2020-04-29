package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.abakus.iaygrunnlag.JournalpostId;
import no.nav.abakus.iaygrunnlag.Organisasjon;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingDto;
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektsmeldingInnsendingsårsakType;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingerMottattRequest;
import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.felles.metrikker.MetrikkerTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektsmeldingerTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;


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
        tjeneste = new InntektsmeldingerRestTjeneste(imTjenesten, koblingTjeneste, new InntektArbeidYtelseTjeneste(iayRepository), mock(MetrikkerTjeneste.class));
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

        UuidDto uuidDto = tjeneste.lagreInntektsmeldinger(request);

        assertThat(uuidDto).isNotNull();

        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = iayRepository.hentInntektArbeidYtelseForReferanse(new GrunnlagReferanse(uuidDto.getReferanse()));

        assertThat(grunnlagOpt).isPresent();
        assertThat(grunnlagOpt.flatMap(InntektArbeidYtelseGrunnlag::getInntektsmeldinger).map(InntektsmeldingAggregat::getInntektsmeldinger))
            .hasValueSatisfying(imer -> assertThat(imer).hasSize(1));
        assertThat(grunnlagOpt.flatMap(InntektArbeidYtelseGrunnlag::getArbeidsforholdInformasjon).map(ArbeidsforholdInformasjon::getArbeidsforholdReferanser))
            .hasValueSatisfying(imer -> assertThat(imer).hasSize(1));
    }
}
