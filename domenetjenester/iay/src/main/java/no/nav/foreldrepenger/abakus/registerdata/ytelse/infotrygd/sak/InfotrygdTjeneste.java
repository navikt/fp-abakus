package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak;

import static no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.RelatertYtelseTema.ENSLIG_FORSORGER_TEMA;
import static no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.RelatertYtelseTema.FORELDREPENGER_TEMA;
import static no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.RelatertYtelseTema.PÅRØRENDE_SYKDOM_TEMA;
import static no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.RelatertYtelseTema.SYKEPENGER_TEMA;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeResponse;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakConsumer;

@ApplicationScoped
public class InfotrygdTjeneste {
    public static final String TJENESTE = "InfotrygdSak";
    private static final String INFOTRYGD_NEDE_EXCEPTION_TEXT = "Basene i Infotrygd er ikke tilgjengelige";
    private static final Logger log = LoggerFactory.getLogger(InfotrygdTjeneste.class);
    private InfotrygdSakConsumer infotrygdSakConsumer;
    private KodeverkRepository kodeverkRepository;

    InfotrygdTjeneste() {
        // CDI
    }

    @Inject
    public InfotrygdTjeneste(InfotrygdSakConsumer infotrygdSakConsumer, KodeverkRepository kodeverkRepository) {
        this.infotrygdSakConsumer = infotrygdSakConsumer;
        this.kodeverkRepository = kodeverkRepository;
    }

    public List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom) {
        FinnSakListeResponse response = finnSakListeFull(fnr, fom);
        List<InfotrygdSak> sakene = mapInfotrygdResponseToInfotrygdSak(response);
        log.info("Infotrygd antall saker/vedtak: {} fom {}", sakene.size(), fom);
        return sakene;
    }

    private List<InfotrygdSak> mapInfotrygdResponseToInfotrygdSak(FinnSakListeResponse response) {
        if (response != null) {
            return response.getVedtakListe()
                .stream()
                .map(this::konverterInfotrygdVedtak)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private FinnSakListeResponse finnSakListeFull(String fnr, LocalDate fom) {
        FinnSakListeRequest request = new FinnSakListeRequest();
        Periode periode = new Periode();
        try {
            periode.setFom(DateUtil.convertToXMLGregorianCalendar(fom));
            periode.setTom(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(9999, Month.DECEMBER, 31)));
            request.setPeriode(periode);
            request.setPersonident(fnr);
            return infotrygdSakConsumer.finnSakListe(request);
        } catch (FinnSakListePersonIkkeFunnet e) { // $NON-NLS-1$ //NOSONAR
            // Skal ut ifra erfaringer fra fundamentet ikke gjøres noe med, fordi dette er normalt.
            InfotrygdTjenesteFeil.FACTORY.personIkkeFunnet(e).log(log);
        } catch (FinnSakListeUgyldigInput e) {
            throw InfotrygdTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, e).toException();
        } catch (FinnSakListeSikkerhetsbegrensning e) {
            throw InfotrygdTjenesteFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        } catch (IntegrasjonException e) {
            if (e.getFeil().getFeilmelding().contains(INFOTRYGD_NEDE_EXCEPTION_TEXT)) {
                throw InfotrygdTjenesteFeil.FACTORY.nedetid(TJENESTE, e).toException();
            } else {
                throw e;
            }
        }
        return new FinnSakListeResponse();
    }

    private InfotrygdSak konverterInfotrygdVedtak(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak vedtak) {
        LocalDate opphoerFomDato = DateUtil.convertToLocalDate(vedtak.getOpphoerFom());
        return konverterFraInfotrygdSak(vedtak, true, opphoerFomDato).build();
    }

    private InfotrygdSak.InfotrygdSakBuilder konverterFraInfotrygdSak(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak sak,
                                                                      boolean erVedtak, LocalDate opphoerFomDato) {
        LocalDate registrert = DateUtil.convertToLocalDate(sak.getRegistrert());
        LocalDate iverksatt = DateUtil.convertToLocalDate(sak.getIverksatt());

        TemaUnderkategori temaUnderkategori = TemaUnderkategori.UDEFINERT;
        YtelseStatus relatertYtelseTilstand = YtelseStatus.AVSLUTTET;

        if (sak.getBehandlingstema() != null && sak.getBehandlingstema().getValue() != null) {
            temaUnderkategori = kodeverkRepository.finn(TemaUnderkategori.class, sak.getBehandlingstema().getValue());
        }
        if (sak.getStatus() != null && sak.getStatus().getValue() != null) {
            RelatertYtelseStatus status = kodeverkRepository.finnForKodeverkEiersKode(RelatertYtelseStatus.class, sak.getStatus().getValue(),
                RelatertYtelseStatus.AVSLUTTET_IT);
            relatertYtelseTilstand = getYtelseTilstand(erVedtak, status);
        }
        YtelseType ytelseType = utledYtelseType(sak.getTema().getValue(), temaUnderkategori);
        if (YtelseType.ENGANGSSTØNAD.equals(ytelseType)) {
            opphoerFomDato = iverksatt != null ? iverksatt : registrert;
        }

        return InfotrygdSak.InfotrygdSakBuilder.ny()
            .medIverksatt(iverksatt)
            .medRegistrert(registrert)
            .medOpphørFom(opphoerFomDato)
            .medYtelseType(ytelseType)
            .medTemaUnderkategori(temaUnderkategori)
            .medRelatertYtelseTilstand(relatertYtelseTilstand);
    }

    private YtelseType utledYtelseType(String ytelseTema, TemaUnderkategori behandlingsTema) {
        if (ENSLIG_FORSORGER_TEMA.getKode().equals(ytelseTema)) {
            return YtelseType.ENSLIG_FORSØRGER;
        } else if (FORELDREPENGER_TEMA.getKode().equals(ytelseTema)) {
            if (TemaUnderkategori.erGjelderSvangerskapspenger(behandlingsTema.getKode())) {
                return YtelseType.SVANGERSKAPSPENGER;
            } else if (TemaUnderkategori.erGjelderForeldrepenger(behandlingsTema.getKode())) {
                return YtelseType.FORELDREPENGER;
            } else if (TemaUnderkategori.erGjelderEngangsstonad(behandlingsTema.getKode())) {
                return YtelseType.ENGANGSSTØNAD;
            } else {
                throw new IllegalStateException("Mangler mapping for RelatertYtelseTema(FA), TemaUnderkategori: " + behandlingsTema.getKode());
            }
        } else if (SYKEPENGER_TEMA.getKode().equals(ytelseTema)) {
            return YtelseType.SYKEPENGER;
        } else if (PÅRØRENDE_SYKDOM_TEMA.getKode().equals(ytelseTema)) {
            // TODO : her må mappes ulike varianter av Pleiepenger
            return YtelseType.PÅRØRENDESYKDOM;
        } else {
            // ignore andre temaer foreløpig
            return YtelseType.UDEFINERT;
        }
    }

    private boolean erLøpendeVedtak(boolean erVedtak, RelatertYtelseStatus status) {
        return erVedtak && RelatertYtelseStatus.erLøpendeVedtak(status.getKode());
    }

    private boolean erÅpenSak(boolean erVedtak, RelatertYtelseStatus status) {
        return !erVedtak && RelatertYtelseStatus.erÅpenSakStatus(status.getKode());
    }

    private boolean erIkkeStartet(RelatertYtelseStatus status) {
        return RelatertYtelseStatus.erIkkeStartetStatus(status.getKode());
    }

    private YtelseStatus getYtelseTilstand(boolean erVedtak, RelatertYtelseStatus status) {
        if (erLøpendeVedtak(erVedtak, status)) {
            return YtelseStatus.LØPENDE;
        } else if (erÅpenSak(erVedtak, status)) {
            return YtelseStatus.UNDER_BEHANDLING;
        } else if (erIkkeStartet(status)) {
            return YtelseStatus.OPPRETTET;
        }
        return YtelseStatus.AVSLUTTET;
    }

}
