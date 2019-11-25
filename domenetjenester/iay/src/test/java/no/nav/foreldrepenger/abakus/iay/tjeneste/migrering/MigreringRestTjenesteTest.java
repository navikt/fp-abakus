package no.nav.foreldrepenger.abakus.iay.tjeneste.migrering;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYFraDtoMapper;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYTilDtoMapper;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.json.JacksonJsonConfig;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.IayGrunnlagJsonMapper;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest.GrunnlagVersjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagSakSnapshotDto;

public class MigreringRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repositoryRule.getEntityManager());
    private InntektArbeidYtelseRepository repository = new InntektArbeidYtelseRepository(repositoryRule.getEntityManager());
    private InntektArbeidYtelseTjeneste iayTjeneste = new InntektArbeidYtelseTjenesteImpl(repository);
    private KoblingTjeneste koblingTjeneste = new KoblingTjeneste(new KoblingRepository(repositoryRule.getEntityManager()), new LåsRepository(repositoryRule.getEntityManager()));
    private final ObjectMapper iayMapper = IayGrunnlagJsonMapper.getMapper();
    private MigreringRestTjeneste tjeneste = new MigreringRestTjeneste(iayTjeneste, koblingTjeneste, kodeverkRepository, repository);

    @Test
    public void skal_ikke_feile_1() throws IOException {
        URL resource = MigreringRestTjenesteTest.class.getResource("/migrering-grunnlag-1.json");
        InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt konvolutt = JacksonJsonConfig.getMapper().readValue(resource, InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt.class);

        InntektArbeidYtelseGrunnlagSakSnapshotDto dto = new InntektArbeidYtelseGrunnlagSakSnapshotDto("12341234123", YtelseType.FORELDREPENGER, new AktørIdPersonident("1234123412341"));
        dto.leggTil(konvolutt.getData(), konvolutt.erAktiv(), konvolutt.getOpplysningsperiode(), konvolutt.getOpptjeningsperiode());
        tjeneste.doMigrering(dto);
    }

    @Test
    public void skal_ikke_feile_2() throws IOException {
        URL resource = MigreringRestTjenesteTest.class.getResource("/migrering-grunnlag-2.json");
        InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt konvolutt = JacksonJsonConfig.getMapper().readValue(resource, InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt.class);

        InntektArbeidYtelseGrunnlagSakSnapshotDto dto = new InntektArbeidYtelseGrunnlagSakSnapshotDto("12341234123", YtelseType.FORELDREPENGER, new AktørIdPersonident("1234123412341"));
        dto.leggTil(konvolutt.getData(), konvolutt.erAktiv(), konvolutt.getOpplysningsperiode(), konvolutt.getOpptjeningsperiode());
        tjeneste.doMigrering(dto);
    }

    @Test
    public void skal_ikke_feile_3() throws IOException {
        URL resource = MigreringRestTjenesteTest.class.getResource("/migrering-grunnlag-3.json");
        InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt konvolutt = JacksonJsonConfig.getMapper().readValue(resource, InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt.class);

        InntektArbeidYtelseGrunnlagSakSnapshotDto dto = new InntektArbeidYtelseGrunnlagSakSnapshotDto("12341234123", YtelseType.FORELDREPENGER, new AktørIdPersonident("1234123412341"));
        dto.leggTil(konvolutt.getData(), konvolutt.erAktiv(), konvolutt.getOpplysningsperiode(), konvolutt.getOpptjeningsperiode());
        tjeneste.doMigrering(dto);
    }

    @Test
    public void skal_ikke_feile_4() throws IOException {
        URL resource = MigreringRestTjenesteTest.class.getResource("/migrering-grunnlag-4.json");
        InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt konvolutt = JacksonJsonConfig.getMapper().readValue(resource, InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt.class);

        InntektArbeidYtelseGrunnlagSakSnapshotDto dto = new InntektArbeidYtelseGrunnlagSakSnapshotDto("12341234123", YtelseType.FORELDREPENGER, new AktørIdPersonident("1234123412341"));
        dto.leggTil(konvolutt.getData(), konvolutt.erAktiv(), konvolutt.getOpplysningsperiode(), konvolutt.getOpptjeningsperiode());
        tjeneste.doMigrering(dto);
    }

    @Test
    public void skal_ikke_feile_5() throws IOException {
        URL resource = MigreringRestTjenesteTest.class.getResource("/migrering-grunnlag-5.json");
        InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt konvolutt = JacksonJsonConfig.getMapper().readValue(resource, InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt.class);

        InntektArbeidYtelseGrunnlagSakSnapshotDto dto = new InntektArbeidYtelseGrunnlagSakSnapshotDto("12341234123", YtelseType.FORELDREPENGER, new AktørIdPersonident("1234123412341"));
        dto.leggTil(konvolutt.getData(), konvolutt.erAktiv(), konvolutt.getOpplysningsperiode(), konvolutt.getOpptjeningsperiode());
        tjeneste.doMigrering(dto);
    }

    @Test
    public void skal_ikke_feile_6() throws IOException {
        URL resource = MigreringRestTjenesteTest.class.getResource("/migrering-grunnlag-6.json");
        InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt konvolutt = JacksonJsonConfig.getMapper().readValue(resource, InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt.class);

        InntektArbeidYtelseGrunnlagSakSnapshotDto dto = new InntektArbeidYtelseGrunnlagSakSnapshotDto("12341234123", YtelseType.FORELDREPENGER, new AktørIdPersonident("1234123412341"));
        dto.leggTil(konvolutt.getData(), konvolutt.erAktiv(), konvolutt.getOpplysningsperiode(), konvolutt.getOpptjeningsperiode());
        tjeneste.doMigrering(dto);
    }

    @Test
    public void skal_ikke_feile_7() throws IOException {
        URL resource = MigreringRestTjenesteTest.class.getResource("/migrering-grunnlag-7.json");
        InntektArbeidYtelseGrunnlagSakSnapshotDto dto = JacksonJsonConfig.getMapper().readValue(resource, InntektArbeidYtelseGrunnlagSakSnapshotDto.class);

        tjeneste.doMigrering(dto);

        List<InntektArbeidYtelseGrunnlag> grunnlagene = iayTjeneste.hentAlleGrunnlagFor(new AktørId(dto.getAktør().getIdent()), new Saksnummer(dto.getSaksnummer()), no.nav.foreldrepenger.abakus.kodeverk.YtelseType.FORELDREPENGER, false);

        assertThat(grunnlagene).hasSize(3);
    }

    @Test
    public void skal_ikke_feile_8() throws IOException {
        URL resource = MigreringRestTjenesteTest.class.getResource("/migrering-grunnlag-8.json");
        InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt konvolutt = JacksonJsonConfig.getMapper().readValue(resource, InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt.class);

        InntektArbeidYtelseGrunnlagSakSnapshotDto dto = new InntektArbeidYtelseGrunnlagSakSnapshotDto("12341234123", YtelseType.FORELDREPENGER, new AktørIdPersonident("1234123412341"));
        dto.leggTil(konvolutt.getData(), konvolutt.erAktiv(), konvolutt.getOpplysningsperiode(), konvolutt.getOpptjeningsperiode());
        tjeneste.doMigrering(dto);
    }

    @Test
    public void skal_ikke_feile_9() throws IOException {
        GrunnlagVersjon grunnlagVersjon = InntektArbeidYtelseGrunnlagRequest.GrunnlagVersjon.ALLE;

        URL resource = MigreringRestTjenesteTest.class.getResource("/migrering-grunnlag-9.json");
        final var koblingReferanse = new KoblingReferanse(UUID.fromString("9f2a89fc-84d1-45fb-a0ee-7b982cfa8919"));
        final var aktørId = new AktørId("1000060002501");
        final var fpsakDto = iayMapper.readValue(resource, InntektArbeidYtelseGrunnlagDto.class);
        var abakusGrunnlag = new IAYFraDtoMapper(iayTjeneste, kodeverkRepository, aktørId, koblingReferanse).mapTilGrunnlagInklusivRegisterdata(fpsakDto, true);
        final var abakusDto = new IAYTilDtoMapper(aktørId, abakusGrunnlag.getGrunnlagReferanse(), koblingReferanse).mapTilDto(abakusGrunnlag, new InntektArbeidYtelseGrunnlagRequest(new AktørIdPersonident(aktørId.getId())).medDataset(Arrays.asList(InntektArbeidYtelseGrunnlagRequest.Dataset.values())).hentGrunnlagVersjon(grunnlagVersjon));

        assertThat(abakusDto.getArbeidsforholdInformasjon().getOverstyringer()).hasSize(2);
    }
    
}
