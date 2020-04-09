package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.RelatertYtelseStatusReverse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.AktoerId;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Meldekort;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.ObjectFactory;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Vedtak;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeResponse;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagConsumer;

@ApplicationScoped
public class MeldekortTjeneste {
    private static final String TJENESTE = "MeldekortUtbetalingsgrunnlag (Arena)";
    private static final Logger log = LoggerFactory.getLogger(MeldekortTjeneste.class);

    // SAK/status fra tjenesten (value/termnavn): AVSLU/Avsluttet, AKTIV/Aktiv, INAKT/Inaktiv - mulig ?/Lukket
    // VEDTAK/status fra tjenesten (value/termnavn): AVSLU/Avsluttet, INNST/Innstilt, IVERK/Iverksatt, REGIS/Registrert, OPPRE/Opprettet, MOTAT/Mottatt

    private MeldekortUtbetalingsgrunnlagConsumer meldekortUtbetalingsgrunnlagConsumer;
    private ObjectFactory objectFactory = new ObjectFactory();

    MeldekortTjeneste() {
        // CDI
    }

    @Inject
    public MeldekortTjeneste(MeldekortUtbetalingsgrunnlagConsumer meldekortUtbetalingsgrunnlagConsumer) {
        this.meldekortUtbetalingsgrunnlagConsumer = meldekortUtbetalingsgrunnlagConsumer;
    }

    private LocalDate oversettDatoNullable(XMLGregorianCalendar datoXML) {
        if (datoXML == null) {
            return null;
        }
        return datoXML.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }

    private YtelseType oversettType(Sak sak) {
        if (YtelseType.ARBEIDSAVKLARINGSPENGER.getKode().equals(sak.getTema().getValue())) {
            return YtelseType.ARBEIDSAVKLARINGSPENGER;
        } else if (YtelseType.DAGPENGER.getKode().equals(sak.getTema().getValue())) {
            return YtelseType.DAGPENGER;
        } else {
            return YtelseType.UDEFINERT;
        }
    }

    private YtelseStatus oversettTilstand(Sak sak, Vedtak vedtak) {
        YtelseStatus statusVedtak = RelatertYtelseStatusReverse.reverseMap(vedtak.getVedtaksstatus().getValue(), log);
        if (YtelseStatus.UNDER_BEHANDLING.equals(statusVedtak) &&
            YtelseStatus.AVSLUTTET.equals(RelatertYtelseStatusReverse.reverseMap(sak.getSaksstatus().getValue(), log))) {
            return YtelseStatus.AVSLUTTET;
        }
        return statusVedtak;
    }

    private YtelseStatus oversettTilstandUtenVedtak(Sak sak) {
        return RelatertYtelseStatusReverse.reverseMap(sak.getSaksstatus().getValue(), log);
    }

    private MeldekortUtbetalingsgrunnlagMeldekort oversettArenaMeldekort(Meldekort meldekort) {
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(oversettDatoNullable(meldekort.getMeldekortperiode().getFom()))
            .medMeldekortTom(oversettDatoNullable(meldekort.getMeldekortperiode().getTom()))
            .medDagsats(BigDecimal.valueOf(meldekort.getDagsats()))
            .medBeløp(BigDecimal.valueOf(meldekort.getBeloep()))
            .medUtbetalingsgrad(BigDecimal.valueOf(meldekort.getUtbetalingsgrad()))
            .build();
    }

    private MeldekortUtbetalingsgrunnlagSak oversettArenaVedtakMeldekort(Sak sak, Vedtak vedtak) {
        List<MeldekortUtbetalingsgrunnlagMeldekort> meldekortList = new ArrayList<>();
        for (Meldekort meldekort : vedtak.getMeldekortListe()) {
            meldekortList.add(oversettArenaMeldekort(meldekort));
        }
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .medType(oversettType(sak))
            .medTilstand(oversettTilstand(sak, vedtak))
            .medKilde(Fagsystem.ARENA)
            .medKravMottattDato(oversettDatoNullable(vedtak.getDatoKravMottatt()))
            .medSaksnummer(new Saksnummer(sak.getFagsystemSakId()))
            .medSakStatus(sak.getSaksstatus().getValue())
            .medVedtakStatus(vedtak.getVedtaksstatus().getValue())
            .medVedtattDato(oversettDatoNullable(vedtak.getVedtaksdato()))
            .medVedtaksPeriodeFom(oversettDatoNullable(vedtak.getVedtaksperiode().getFom()))
            .medVedtaksPeriodeTom(oversettDatoNullable(vedtak.getVedtaksperiode().getTom()))
            .medVedtaksDagsats(BigDecimal.valueOf(vedtak.getDagsats()))
            .leggTilMeldekort(meldekortList)
            .build();

    }

    private MeldekortUtbetalingsgrunnlagSak oversettArenaUtenVedtak(Sak sak) {
        MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder sakBuilder = MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .medType(oversettType(sak))
            .medTilstand(oversettTilstandUtenVedtak(sak))
            .medKilde(Fagsystem.ARENA)
            .medSaksnummer(new Saksnummer(sak.getFagsystemSakId()))
            .medSakStatus(sak.getSaksstatus().getValue())
            .medKravMottattDato(null)
            .medVedtakStatus(null);
        return sakBuilder.build();
    }

    private List<MeldekortUtbetalingsgrunnlagSak> oversettArenaSak(Sak sak) {
        List<MeldekortUtbetalingsgrunnlagSak> vedtakene = new ArrayList<>();
        if (sak.getVedtakListe().isEmpty()) {
            vedtakene.add(oversettArenaUtenVedtak(sak));
        }
        for (Vedtak vedtak : sak.getVedtakListe()) {
            vedtakene.add(oversettArenaVedtakMeldekort(sak, vedtak));
        }
        return vedtakene;
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentMeldekortListe(AktørId aktørId, LocalDate fom, LocalDate tom) {
        try {
            FinnMeldekortUtbetalingsgrunnlagListeRequest request = new FinnMeldekortUtbetalingsgrunnlagListeRequest();

            AktoerId aktoer = objectFactory.createAktoerId();
            aktoer.setAktoerId(aktørId.getId());
            request.setIdent(aktoer);
            Periode periode = objectFactory.createPeriode();
            periode.setFom(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(fom));
            if (tom != null) {
                periode.setTom(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(tom));
            }
            request.setPeriode(periode);

            Tema aapTema = objectFactory.createTema();
            aapTema.setValue(YtelseType.ARBEIDSAVKLARINGSPENGER.getKode());
            request.getTemaListe().add(aapTema);
            Tema dagTema = objectFactory.createTema();
            dagTema.setValue(YtelseType.DAGPENGER.getKode());
            request.getTemaListe().add(dagTema);

            FinnMeldekortUtbetalingsgrunnlagListeResponse response = meldekortUtbetalingsgrunnlagConsumer.finnMeldekortUtbetalingsgrunnlagListe(request);

            List<MeldekortUtbetalingsgrunnlagSak> saker = new ArrayList<>();
            if (response != null && !response.getMeldekortUtbetalingsgrunnlagListe().isEmpty()) {
                for (Sak sak : response.getMeldekortUtbetalingsgrunnlagListe()) {
                    saker.addAll(oversettArenaSak(sak));
                }
            }

            return saker;
        } catch (FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning e) {
            throw MeldekortFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        } catch (FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput e) {
            throw MeldekortFeil.FACTORY.tjenesteUgyldigInput(TJENESTE, e).toException();
        } catch (FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet e) {
            throw MeldekortFeil.FACTORY.fantIkkePersonForAktorId(TJENESTE, e).toException();
        }
    }

    private interface MeldekortFeil extends DeklarerteFeil {

        MeldekortFeil FACTORY = FeilFactory.create(MeldekortFeil.class);

        @TekniskFeil(feilkode = "FP-150919", feilmelding = "%s ikke tilgjengelig (sikkerhetsbegrensning)", logLevel = LogLevel.WARN)
        Feil tjenesteUtilgjengeligSikkerhetsbegrensning(String tjeneste, Exception exceptionMessage);

        @IntegrasjonFeil(feilkode = "FP-615298", feilmelding = "%s fant ikke person for oppgitt aktørId", logLevel = LogLevel.WARN)
        Feil fantIkkePersonForAktorId(String tjeneste, Exception exceptionMessage);

        @IntegrasjonFeil(feilkode = "FP-615299", feilmelding = "%s ugyldig input", logLevel = LogLevel.WARN)
        Feil tjenesteUgyldigInput(String tjeneste, Exception exceptionMessage);

        @TekniskFeil(feilkode = "FP-073523", feilmelding = "Teknisk feil i grensesnitt mot %s", logLevel = LogLevel.ERROR)
        Feil tekniskFeil(String tjeneste, DatatypeConfigurationException årsak);

    }
}
