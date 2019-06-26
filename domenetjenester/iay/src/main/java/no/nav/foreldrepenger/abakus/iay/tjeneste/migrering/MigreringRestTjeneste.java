package no.nav.foreldrepenger.abakus.iay.tjeneste.migrering;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYFraDtoMapper;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.json.JacksonJsonConfig;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.PersonIdent;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagSakSnapshotDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Api(tags = "migrering")
@Path("/migrering/v1")
@ApplicationScoped
@Transaction
public class MigreringRestTjeneste {

    private static final Logger log = LoggerFactory.getLogger(MigreringRestTjeneste.class);

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private KodeverkRepository kodeverkRepository;
    private InntektArbeidYtelseRepository repository;

    public MigreringRestTjeneste() {
    }

    @Inject
    public MigreringRestTjeneste(InntektArbeidYtelseTjeneste iayTjeneste,
                                 KoblingTjeneste koblingTjeneste,
                                 KodeverkRepository kodeverkRepository,
                                 InntektArbeidYtelseRepository repository) {
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.kodeverkRepository = kodeverkRepository;
        this.repository = repository;
    }

    @PUT
    @Path("/sak")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Tar i mot alle grunnlag på alle behandlinger på en gitt sak")
    @BeskyttetRessurs(action = CREATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response migrerSak(@NotNull
                              @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class)
                              @Valid InntektArbeidYtelseGrunnlagSakSnapshotDto sakSnapshot) throws JsonProcessingException {

        doMigrering(sakSnapshot);

        log.info("Migrert sak={} med {} grunnlag", sakSnapshot.getSaksnummer(), sakSnapshot.getGrunnlag().size());
        return Response.ok().build();
    }

    /**
     * Synling for testing
     *
     * @param sakSnapshot
     * @throws JsonProcessingException
     */
    void doMigrering(InntektArbeidYtelseGrunnlagSakSnapshotDto sakSnapshot) throws JsonProcessingException {
        var aktørId = new AktørId(sakSnapshot.getAktør().getIdent());
        iayTjeneste.slettAltForSak(aktørId, new Saksnummer(sakSnapshot.getSaksnummer()), kodeverkRepository.finn(YtelseType.class, sakSnapshot.getYtelseType().getKode()));

        for (InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt konvolutt : sakSnapshot.getGrunnlag()) {
            try {
                log.info("Migrerer grunnlag={}", konvolutt.getData().getGrunnlagReferanse());
                var kobling = finnEllerOpprett(konvolutt, sakSnapshot);

                var koblingReferanse = kobling.getKoblingReferanse();
                var dtoMapper = new IAYFraDtoMapper(iayTjeneste, kodeverkRepository, aktørId, koblingReferanse);
                var grunnlag = dtoMapper.mapTilGrunnlagInklusivRegisterdata(konvolutt.getData());

                var aktiv = konvolutt.erAktiv() != null ? konvolutt.erAktiv() : false;
                repository.lagreMigrertGrunnlag(grunnlag, koblingReferanse, aktiv);
                log.info("Migrert grunnlag={}", grunnlag.getGrunnlagReferanse());
            } catch (Exception e) {
                log.info("Feilet migrering av sak={} for grunnlag med json='{}'", sakSnapshot.getSaksnummer(), JacksonJsonConfig.getMapper().writeValueAsString(konvolutt));
                throw new IllegalStateException(e);
            }
        }
    }

    @GET
    @Path("/status")
    @ApiOperation(value = "Gir status / stats på migrering")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response status() {

        Map<String, Object> responsMap = new HashMap<>();

        List<Saksnummer> value = koblingTjeneste.hentAlleSaksnummer();
        responsMap.put("saksnummer", value);
        responsMap.put("antallSaker", value.size());
        responsMap.put("iay", repository.hentStats());

        return Response.ok(responsMap).build();
    }

    private Kobling finnEllerOpprett(InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt konvolutt, InntektArbeidYtelseGrunnlagSakSnapshotDto sakDto) {
        var grunnlag = konvolutt.getData();
        var referanse = new KoblingReferanse(grunnlag.getKoblingReferanse());
        Optional<Kobling> koblingOpt = koblingTjeneste.hentFor(referanse);
        Kobling kobling;
        if (koblingOpt.isEmpty()) {
            // Lagre kobling
            var aktørId = new AktørId(sakDto.getAktør().getIdent());
            kobling = new Kobling(new Saksnummer(sakDto.getSaksnummer()), referanse, aktørId);
        } else {
            kobling = koblingOpt.get();
        }
        if (YtelseType.UDEFINERT.equals(kobling.getYtelseType())) {
            var ytelseType = mapTilYtelseType(sakDto.getYtelseType());
            if (ytelseType != null) {
                kobling.setYtelseType(ytelseType);
            }
        }
        // Oppdater kobling
        var annenPartAktør = sakDto.getAnnenPartAktør();
        if (annenPartAktør != null) {
            kobling.setAnnenPartAktørId(new AktørId(annenPartAktør.getIdent()));
        }
        var opplysningsperiode = konvolutt.getOpplysningsperiode();
        if (opplysningsperiode != null) {
            kobling.setOpplysningsperiode(DatoIntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFom(), opplysningsperiode.getTom()));
        }
        var opptjeningsperiode = konvolutt.getOpptjeningsperiode();
        if (opptjeningsperiode != null) {
            kobling.setOpptjeningsperiode(DatoIntervallEntitet.fraOgMedTilOgMed(opptjeningsperiode.getFom(), opptjeningsperiode.getTom()));
        }
        // Diff & log endringer
        koblingTjeneste.lagre(kobling);
        return kobling;
    }

    private YtelseType mapTilYtelseType(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseType ytelseType) {
        return kodeverkRepository.finn(YtelseType.class, ytelseType.getKode());
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {
        public AbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (InntektArbeidYtelseGrunnlagSakSnapshotDto) obj;
            AbacDataAttributter opprett = AbacDataAttributter.opprett();
            req.getGrunnlag().stream()
                .map(InntektArbeidYtelseGrunnlagSakSnapshotDto.Konvolutt::getData)
                .map(InntektArbeidYtelseGrunnlagDto::getPerson)
                .map(PersonIdent::getIdent)
                .forEach(personIdent -> opprett.leggTil(StandardAbacAttributtType.AKTØR_ID, personIdent));
            // Legger alle her inn som aktørId da dette feltet kun skal inneholde aktørIder
            return opprett;
        }
    }
}
