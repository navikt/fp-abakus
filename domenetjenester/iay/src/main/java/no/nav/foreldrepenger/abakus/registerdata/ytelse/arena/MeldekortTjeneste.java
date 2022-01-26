package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.ws.MeldekortUtbetalingsgrunnlagConsumer;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.RelatertYtelseStatusReverse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.xmlutils.DateUtil;
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
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;


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

    public List<MeldekortUtbetalingsgrunnlagSak> hentMeldekortListe(AktørId aktørId, PersonIdent ident, LocalDate fom, LocalDate tom) {
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

            var antallMK = Optional.ofNullable(response)
                .map(FinnMeldekortUtbetalingsgrunnlagListeResponse::getMeldekortUtbetalingsgrunnlagListe).orElse(List.of()).stream()
                .flatMap(l -> l.getVedtakListe().stream())
                .mapToLong(v -> v.getMeldekortListe().size())
                .sum();
            var saksnumre = Optional.ofNullable(response)
                .map(FinnMeldekortUtbetalingsgrunnlagListeResponse::getMeldekortUtbetalingsgrunnlagListe).orElse(List.of()).stream()
                    .map(Sak::getFagsystemSakId)
                        .collect(Collectors.toSet());

            validerMeldekortListeFnr(antallMK, saksnumre, ident, fom, tom);

            return saker;
        } catch (FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning e) {
            throw new TekniskException("FP-150919", "MeldekortUtbetalingsgrunnlag (Arena) ikke tilgjengelig (sikkerhetsbegrensning)", e);
        } catch (FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput e) {
            throw new IntegrasjonException("FP-615299", "MeldekortUtbetalingsgrunnlag (Arena) ugyldig input", e);
        } catch (FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet e) {
            throw new IntegrasjonException("FP-615298", "MeldekortUtbetalingsgrunnlag (Arena) fant ikke person for oppgitt aktørId", e);
        }
    }

    public void validerMeldekortListeFnr(long antallMeldekort, Set<String> saksnumre, PersonIdent ident, LocalDate fom, LocalDate tom) {
        try {
            FinnMeldekortUtbetalingsgrunnlagListeRequest request = new FinnMeldekortUtbetalingsgrunnlagListeRequest();

            var bruker = objectFactory.createBruker();
            bruker.setIdent(ident.getIdent());
            request.setIdent(bruker);
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

            var testAntallMK = Optional.ofNullable(response)
                .map(FinnMeldekortUtbetalingsgrunnlagListeResponse::getMeldekortUtbetalingsgrunnlagListe).orElse(List.of()).stream()
                .flatMap(l -> l.getVedtakListe().stream())
                .mapToLong(v -> v.getMeldekortListe().size())
                .sum();
            var testSaksnumre = Optional.ofNullable(response)
                .map(FinnMeldekortUtbetalingsgrunnlagListeResponse::getMeldekortUtbetalingsgrunnlagListe).orElse(List.of()).stream()
                .map(Sak::getFagsystemSakId)
                .collect(Collectors.toSet());

            if (testAntallMK == antallMeldekort) {
                log.info("ARENATEST MKUG FNR: Samme antall MK");
            } else {
                log.info("ARENATEST MKUG FNR: Ulikt antall MK aktør {} bruker {}", antallMeldekort, testAntallMK);
            }

            if (testSaksnumre.size() == saksnumre.size() && testSaksnumre.containsAll(saksnumre)) {
                log.info("ARENATEST MKUG FNR: Like saksnumre");
            } else {
                log.info("ARENATEST MKUG FNR: Ulike saksnumre aktør {} bruker {}", saksnumre, testSaksnumre);
            }

        } catch (FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning e) {
            log.info("ARENATEST MKUG FNR: MeldekortUtbetalingsgrunnlag (Arena) ikke tilgjengelig (sikkerhetsbegrensning)");
        } catch (FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput e) {
            log.info("ARENATEST MKUG FNR: MeldekortUtbetalingsgrunnlag (Arena) ugyldig input");
        } catch (FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet e) {
            log.info("ARENATEST MKUG FNR: MeldekortUtbetalingsgrunnlag (Arena) fant ikke person for oppgitt fnr");
        }
    }

}
