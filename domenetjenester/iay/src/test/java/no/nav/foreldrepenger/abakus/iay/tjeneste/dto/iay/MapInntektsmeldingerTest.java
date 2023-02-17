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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.RefusjonskravDatoerDto;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyringBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

public class MapInntektsmeldingerTest {

    public static final String SAKSNUMMER = "1234123412345";
    private static final YtelseType ytelseType = YtelseType.FORELDREPENGER;

    @RegisterExtension
    public static JpaExtension jpaExtension = new JpaExtension();

    private final KoblingRepository repository = new KoblingRepository(jpaExtension.getEntityManager());
    private final KoblingTjeneste koblingTjeneste = new KoblingTjeneste(repository, new LåsRepository(jpaExtension.getEntityManager()));
    private final InntektArbeidYtelseRepository iayRepository = new InntektArbeidYtelseRepository(jpaExtension.getEntityManager());
    private final InntektArbeidYtelseTjeneste iayTjeneste = new InntektArbeidYtelseTjeneste(iayRepository);

    @Test
    public void skal_hente_alle_inntektsmeldinger_for_fagsak_uten_duplikater() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        KoblingReferanse koblingReferanse2 = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        YtelseType foreldrepenger = YtelseType.FORELDREPENGER;
        Kobling kobling1 = new Kobling(foreldrepenger, saksnummer, koblingReferanse, aktørId);
        Kobling kobling2 = new Kobling(foreldrepenger, saksnummer, koblingReferanse2, aktørId);
        koblingTjeneste.lagre(kobling1);
        koblingTjeneste.lagre(kobling2);
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer("910909088")))
            .medBeløp(BigDecimal.TEN)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        ArbeidsforholdInformasjonBuilder arbeidsforholdInformasjon = ArbeidsforholdInformasjonBuilder.builder(Optional.empty());
        iayRepository.lagre(koblingReferanse, arbeidsforholdInformasjon, List.of(im));
        iayRepository.lagre(koblingReferanse2, arbeidsforholdInformasjon, List.of(im));

        // Act
        Map<Inntektsmelding, ArbeidsforholdInformasjon> alleIm = iayTjeneste.hentArbeidsforholdinfoInntektsmeldingerMapFor(aktørId, saksnummer,
            foreldrepenger);
        InntektsmeldingerDto inntektsmeldingerDto = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(alleIm);

        // Assert
        assertThat(inntektsmeldingerDto.getInntektsmeldinger().size()).isEqualTo(1);
    }

    @Test
    public void skal_hente_alle_inntektsmeldinger_for_fagsak_uten_duplikater_med_flere_versjoner_av_arbeidsforholdInformasjon() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        YtelseType foreldrepenger = YtelseType.FORELDREPENGER;
        Kobling kobling1 = new Kobling(foreldrepenger, saksnummer, koblingReferanse, aktørId);
        koblingTjeneste.lagre(kobling1);
        UUID internArbeidsforholdRef = UUID.randomUUID();
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(new OrgNummer("910909088"));
        InternArbeidsforholdRef ref = InternArbeidsforholdRef.ref(internArbeidsforholdRef);
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medArbeidsforholdId(ref)
            .medBeløp(BigDecimal.TEN)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        EksternArbeidsforholdRef eksternRef = EksternArbeidsforholdRef.ref("EksternRef");
        ArbeidsforholdInformasjonBuilder arbeidsforholdInfo1 = ArbeidsforholdInformasjonBuilder.builder(Optional.empty());
        arbeidsforholdInfo1.leggTilNyReferanse(new ArbeidsforholdReferanse(virksomhet, ref, eksternRef));
        iayRepository.lagre(koblingReferanse, arbeidsforholdInfo1, List.of(im));
        ArbeidsforholdInformasjonBuilder arbeidsforholdInfo2 = ArbeidsforholdInformasjonBuilder.builder(Optional.of(arbeidsforholdInfo1.build()))
            .leggTil(ArbeidsforholdOverstyringBuilder.oppdatere(Optional.empty())
                .medArbeidsforholdRef(ref)
                .medArbeidsgiver(virksomhet)
                .medHandling(ArbeidsforholdHandlingType.BASERT_PÅ_INNTEKTSMELDING));
        InntektArbeidYtelseGrunnlagBuilder grBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(
            iayRepository.hentInntektArbeidYtelseForBehandling(koblingReferanse)).medInformasjon(arbeidsforholdInfo2.build());
        iayRepository.lagre(koblingReferanse, grBuilder);

        // Act
        Map<Inntektsmelding, ArbeidsforholdInformasjon> alleIm = iayTjeneste.hentArbeidsforholdinfoInntektsmeldingerMapFor(aktørId, saksnummer,
            foreldrepenger);
        InntektsmeldingerDto inntektsmeldingerDto = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(alleIm);

        // Assert
        assertThat(inntektsmeldingerDto.getInntektsmeldinger().size()).isEqualTo(1);
    }


    @Test
    public void skal_mappe_en_inntektsmelding_til_første_refusjonsdato_og_første_innsendelsesdato() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        YtelseType foreldrepenger = YtelseType.FORELDREPENGER;
        Kobling kobling1 = new Kobling(foreldrepenger, saksnummer, koblingReferanse, aktørId);
        koblingTjeneste.lagre(kobling1);
        LocalDateTime innsendingstidspunkt = LocalDateTime.now();
        LocalDate startPermisjon = LocalDate.now().minusDays(10);
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(new OrgNummer("910909088"));
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medBeløp(BigDecimal.TEN)
            .medRefusjon(BigDecimal.TEN)
            .medStartDatoPermisjon(startPermisjon)
            .medInnsendingstidspunkt(innsendingstidspunkt)
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        iayRepository.lagre(koblingReferanse, ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(im));

        // Act
        Set<Inntektsmelding> alleIm = iayTjeneste.hentAlleInntektsmeldingerFor(aktørId, saksnummer, foreldrepenger);
        Kobling nyesteKobling = koblingTjeneste.hentSisteFor(aktørId, saksnummer, foreldrepenger).orElseThrow();
        RefusjonskravDatoerDto refusjonskravDatoerDto = MapInntektsmeldinger.mapRefusjonskravdatoer(alleIm,
            iayTjeneste.hentAggregat(nyesteKobling.getKoblingReferanse()));

        // Assert
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().size()).isEqualTo(1);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getArbeidsgiver().getIdent()).isEqualTo(virksomhet.getIdentifikator());
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteDagMedRefusjonskrav()).isEqualTo(startPermisjon);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteInnsendingAvRefusjonskrav()).isEqualTo(
            innsendingstidspunkt.toLocalDate());
    }

    @Test
    public void skal_mappe_en_inntektsmelding_til_første_refusjonsdato_og_første_innsendelsesdato_når_startdato_permisjon_er_null() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");

        Kobling kobling1 = new Kobling(ytelseType, saksnummer, koblingReferanse, aktørId);
        koblingTjeneste.lagre(kobling1);
        LocalDateTime innsendingstidspunkt = LocalDateTime.now();
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(new OrgNummer("910909088"));
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medBeløp(BigDecimal.TEN)
            .medRefusjon(BigDecimal.TEN)
            .medInnsendingstidspunkt(innsendingstidspunkt)
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        iayRepository.lagre(koblingReferanse, ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(im));

        // Act
        Set<Inntektsmelding> alleIm = iayTjeneste.hentAlleInntektsmeldingerFor(aktørId, saksnummer, ytelseType);
        Kobling nyesteKobling = koblingTjeneste.hentSisteFor(aktørId, saksnummer, ytelseType).orElseThrow();
        RefusjonskravDatoerDto refusjonskravDatoerDto = MapInntektsmeldinger.mapRefusjonskravdatoer(alleIm,
            iayTjeneste.hentAggregat(nyesteKobling.getKoblingReferanse()));

        // Assert
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().size()).isEqualTo(1);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getArbeidsgiver().getIdent()).isEqualTo(virksomhet.getIdentifikator());
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteDagMedRefusjonskrav()).isNull();
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteInnsendingAvRefusjonskrav()).isEqualTo(
            innsendingstidspunkt.toLocalDate());
    }

    @Test
    public void skal_mappe_ikkje_mappe_refusjonsdatoer_når_siste_inntektsmelding_ikkje_har_refusjonskrav() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        Kobling kobling1 = new Kobling(ytelseType, saksnummer, koblingReferanse, aktørId);
        koblingTjeneste.lagre(kobling1);
        LocalDateTime innsendingstidspunkt = LocalDateTime.now();
        LocalDate startPermisjon = LocalDate.now().minusDays(10);
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(new OrgNummer("910909088"));
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medBeløp(BigDecimal.TEN)
            .medRefusjon(BigDecimal.TEN)
            .medStartDatoPermisjon(startPermisjon)
            .medInnsendingstidspunkt(innsendingstidspunkt)
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        Inntektsmelding im2 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medBeløp(BigDecimal.ZERO)
            .medRefusjon(BigDecimal.ZERO)
            .medStartDatoPermisjon(startPermisjon)
            .medInnsendingstidspunkt(innsendingstidspunkt.plusDays(10))
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id2")
            .build();
        iayRepository.lagre(koblingReferanse, ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(im));
        iayRepository.lagre(koblingReferanse, ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(im2));

        // Act
        Set<Inntektsmelding> alleIm = iayTjeneste.hentAlleInntektsmeldingerFor(aktørId, saksnummer, ytelseType);
        Kobling nyesteKobling = koblingTjeneste.hentSisteFor(aktørId, saksnummer, ytelseType).orElseThrow();
        RefusjonskravDatoerDto refusjonskravDatoerDto = MapInntektsmeldinger.mapRefusjonskravdatoer(alleIm,
            iayTjeneste.hentAggregat(nyesteKobling.getKoblingReferanse()));

        // Assert
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().size()).isEqualTo(0);
    }

    @Test
    public void skal_mappe_til_refusjonskravdatoer_for_flere_inntektsmeldinger_med_refusjonskrav() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        Kobling kobling1 = new Kobling(ytelseType, saksnummer, koblingReferanse, aktørId);
        koblingTjeneste.lagre(kobling1);
        LocalDateTime innsendingstidspunkt = LocalDateTime.now();
        LocalDate startPermisjon = LocalDate.now().minusDays(10);
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(new OrgNummer("910909088"));
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medBeløp(BigDecimal.TEN)
            .medRefusjon(BigDecimal.TEN)
            .medStartDatoPermisjon(startPermisjon)
            .medInnsendingstidspunkt(innsendingstidspunkt)
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        Inntektsmelding im2 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medBeløp(BigDecimal.ZERO)
            .medRefusjon(BigDecimal.TEN)
            .medStartDatoPermisjon(startPermisjon)
            .medInnsendingstidspunkt(innsendingstidspunkt.plusDays(10))
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id2")
            .build();
        iayRepository.lagre(koblingReferanse, ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(im));
        iayRepository.lagre(koblingReferanse, ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(im2));

        // Act
        Set<Inntektsmelding> alleIm = iayTjeneste.hentAlleInntektsmeldingerFor(aktørId, saksnummer, ytelseType);
        Kobling nyesteKobling = koblingTjeneste.hentSisteFor(aktørId, saksnummer, ytelseType).orElseThrow();
        RefusjonskravDatoerDto refusjonskravDatoerDto = MapInntektsmeldinger.mapRefusjonskravdatoer(alleIm,
            iayTjeneste.hentAggregat(nyesteKobling.getKoblingReferanse()));

        // Assert
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().size()).isEqualTo(1);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getArbeidsgiver().getIdent()).isEqualTo(virksomhet.getIdentifikator());
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteDagMedRefusjonskrav()).isEqualTo(startPermisjon);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteInnsendingAvRefusjonskrav()).isEqualTo(
            innsendingstidspunkt.toLocalDate());

    }

    @Test
    public void skal_mappe_til_refusjonskravdatoer_for_flere_arbeidsgivere_med_refusjonskrav() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        Kobling kobling1 = new Kobling(ytelseType, saksnummer, koblingReferanse, aktørId);
        koblingTjeneste.lagre(kobling1);
        LocalDateTime innsendingstidspunkt = LocalDateTime.now();
        LocalDate startPermisjon = LocalDate.now().minusDays(10);
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(new OrgNummer("910909088"));
        Arbeidsgiver virksomhet2 = Arbeidsgiver.virksomhet(new OrgNummer("995428563"));
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medBeløp(BigDecimal.TEN)
            .medRefusjon(BigDecimal.TEN)
            .medStartDatoPermisjon(startPermisjon)
            .medInnsendingstidspunkt(innsendingstidspunkt)
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        LocalDateTime innsendingstidspunkt2 = innsendingstidspunkt.plusDays(10);
        Inntektsmelding im2 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet2)
            .medBeløp(BigDecimal.ZERO)
            .medRefusjon(BigDecimal.TEN)
            .medStartDatoPermisjon(startPermisjon)
            .medInnsendingstidspunkt(innsendingstidspunkt2)
            .medMottattDato(innsendingstidspunkt2.toLocalDate())
            .medJournalpostId("journalpost_id2")
            .build();
        iayRepository.lagre(koblingReferanse, ArbeidsforholdInformasjonBuilder.builder(Optional.empty()), List.of(im, im2));

        // Act
        Set<Inntektsmelding> alleIm = iayTjeneste.hentAlleInntektsmeldingerFor(aktørId, saksnummer, ytelseType);
        Kobling nyesteKobling = koblingTjeneste.hentSisteFor(aktørId, saksnummer, ytelseType).orElseThrow();
        RefusjonskravDatoerDto refusjonskravDatoerDto = MapInntektsmeldinger.mapRefusjonskravdatoer(alleIm,
            iayTjeneste.hentAggregat(nyesteKobling.getKoblingReferanse()));

        // Assert
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().size()).isEqualTo(2);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getArbeidsgiver().getIdent()).isEqualTo(virksomhet2.getIdentifikator());
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteDagMedRefusjonskrav()).isEqualTo(startPermisjon);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteInnsendingAvRefusjonskrav()).isEqualTo(
            innsendingstidspunkt2.toLocalDate());

        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(1).getArbeidsgiver().getIdent()).isEqualTo(virksomhet.getIdentifikator());
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(1).getFørsteDagMedRefusjonskrav()).isEqualTo(startPermisjon);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(1).getFørsteInnsendingAvRefusjonskrav()).isEqualTo(
            innsendingstidspunkt.toLocalDate());

    }

    @Test
    public void skal_mappe_til_refusjonskravdatoer_for_flere_arbeidsforhold_med_refusjonskrav() {
        // Arrange
        Saksnummer saksnummer = new Saksnummer(SAKSNUMMER);
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        AktørId aktørId = new AktørId("1234123412341");
        Kobling kobling1 = new Kobling(ytelseType, saksnummer, koblingReferanse, aktørId);
        koblingTjeneste.lagre(kobling1);
        LocalDateTime innsendingstidspunkt = LocalDateTime.now();
        LocalDate startPermisjon = LocalDate.now().minusDays(10);
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(new OrgNummer("910909088"));
        ArbeidsforholdInformasjonBuilder builder = ArbeidsforholdInformasjonBuilder.builder(Optional.empty());
        InternArbeidsforholdRef ref1 = builder.finnEllerOpprett(virksomhet, EksternArbeidsforholdRef.ref("rgjg98jj3j43"));
        Inntektsmelding im = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medArbeidsforholdId(ref1)
            .medBeløp(BigDecimal.TEN)
            .medRefusjon(BigDecimal.TEN)
            .medStartDatoPermisjon(startPermisjon)
            .medInnsendingstidspunkt(innsendingstidspunkt)
            .medMottattDato(LocalDate.now())
            .medJournalpostId("journalpost_id")
            .build();
        LocalDateTime innsendingstidspunkt2 = innsendingstidspunkt.plusDays(10);
        InternArbeidsforholdRef ref2 = builder.finnEllerOpprett(virksomhet, EksternArbeidsforholdRef.ref("28r9283yr92"));
        Inntektsmelding im2 = InntektsmeldingBuilder.builder()
            .medArbeidsgiver(virksomhet)
            .medArbeidsforholdId(ref2)
            .medBeløp(BigDecimal.ZERO)
            .medRefusjon(BigDecimal.TEN)
            .medStartDatoPermisjon(startPermisjon)
            .medInnsendingstidspunkt(innsendingstidspunkt2)
            .medMottattDato(innsendingstidspunkt2.toLocalDate())
            .medJournalpostId("journalpost_id2")
            .build();
        iayRepository.lagre(koblingReferanse, builder, List.of(im, im2));

        // Act
        Set<Inntektsmelding> alleIm = iayTjeneste.hentAlleInntektsmeldingerFor(aktørId, saksnummer, ytelseType);
        Kobling nyesteKobling = koblingTjeneste.hentSisteFor(aktørId, saksnummer, ytelseType).orElseThrow();
        RefusjonskravDatoerDto refusjonskravDatoerDto = MapInntektsmeldinger.mapRefusjonskravdatoer(alleIm,
            iayTjeneste.hentAggregat(nyesteKobling.getKoblingReferanse()));

        // Assert
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().size()).isEqualTo(1);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getArbeidsgiver().getIdent()).isEqualTo(virksomhet.getIdentifikator());
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteDagMedRefusjonskrav()).isEqualTo(startPermisjon);
        assertThat(refusjonskravDatoerDto.getRefusjonskravDatoer().get(0).getFørsteInnsendingAvRefusjonskrav()).isEqualTo(
            innsendingstidspunkt.toLocalDate());

    }

}
