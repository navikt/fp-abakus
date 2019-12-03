package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak;

import static no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.RelatertYtelseTema.ENSLIG_FORSORGER_TEMA;
import static no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.RelatertYtelseTema.FORELDREPENGER_TEMA;
import static no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.RelatertYtelseTema.PÅRØRENDE_SYKDOM_TEMA;
import static no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.RelatertYtelseTema.SYKEPENGER_TEMA;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.TemaUnderkategoriReverse;
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

    private static final Map<String, YtelseStatus> STATUS_VERDI_MAP = Map.ofEntries(
        Map.entry(RelatertYtelseStatus.LØPENDE_VEDTAK.getKode(), YtelseStatus.LØPENDE),
        Map.entry(RelatertYtelseStatus.AVSLUTTET_IT.getKode(), YtelseStatus.AVSLUTTET),
        Map.entry(RelatertYtelseStatus.IKKE_STARTET.getKode(), YtelseStatus.OPPRETTET),
        Map.entry("xx", YtelseStatus.UDEFINERT),
        Map.entry("??", YtelseStatus.UDEFINERT)
    );

    private InfotrygdSakConsumer infotrygdSakConsumer;


    InfotrygdTjeneste() {
        // CDI
    }

    @Inject
    public InfotrygdTjeneste(InfotrygdSakConsumer infotrygdSakConsumer) {
        this.infotrygdSakConsumer = infotrygdSakConsumer;
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
            temaUnderkategori = TemaUnderkategoriReverse.reverseMap(sak.getBehandlingstema().getValue());
        }
        if (sak.getStatus() != null && sak.getStatus().getValue() != null) {
            relatertYtelseTilstand = getYtelseTilstand(erVedtak, sak.getStatus().getValue());
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

    private YtelseStatus getYtelseTilstand(boolean erVedtak, String status) {
        return STATUS_VERDI_MAP.getOrDefault(status, erVedtak ? YtelseStatus.AVSLUTTET : YtelseStatus.UNDER_BEHANDLING);
    }

}
