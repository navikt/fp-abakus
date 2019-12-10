package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.TemaUnderkategoriReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdTjenesteFeil;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.vedtak.domene.TemaUnderkategori;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Engangsstoenad;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Foreldrepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Grunnlag;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PaaroerendeSykdom;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Sykepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag.InfotrygdBeregningsgrunnlagConsumer;


@ApplicationScoped
public class InfotrygdBeregningsgrunnlagTjenesteImpl implements InfotrygdBeregningsgrunnlagTjeneste {
    private static final String TJENESTE = "InfotrygdBeregningsgrunnlag";
    private static final Logger log = LoggerFactory.getLogger(InfotrygdBeregningsgrunnlagTjenesteImpl.class);

    private InfotrygdBeregningsgrunnlagConsumer infotrygdBeregningsgrunnlagConsumer;
    private AktørConsumer tpsTjeneste;

    @Inject
    public InfotrygdBeregningsgrunnlagTjenesteImpl(InfotrygdBeregningsgrunnlagConsumer infotrygdBeregningsgrunnlagConsumer,
                                                   AktørConsumer tpsTjeneste) {
        this.infotrygdBeregningsgrunnlagConsumer = infotrygdBeregningsgrunnlagConsumer;
        this.tpsTjeneste = tpsTjeneste;
    }

    InfotrygdBeregningsgrunnlagTjenesteImpl() {
        // CDI
    }

    @Override
    public List<YtelseBeregningsgrunnlag> hentGrunnlagListeFull(AktørId aktørId, LocalDate fom) {
        PersonIdent personIdent = tpsTjeneste.hentPersonIdentForAktørId(aktørId.getId()).map(PersonIdent::new).orElseThrow();
        FinnGrunnlagListeResponse finnGrunnlagListeResponse = finnGrunnlagListeFull(personIdent.getIdent(), fom);
        return convertNy(finnGrunnlagListeResponse);
    }

    @Override
    public List<YtelseBeregningsgrunnlag> hentGrunnlagListeFull(String fnr, LocalDate fom) {
        FinnGrunnlagListeResponse finnGrunnlagListeResponse = finnGrunnlagListeFull(fnr, fom);
        return convertNy(finnGrunnlagListeResponse);
    }

    private FinnGrunnlagListeResponse finnGrunnlagListeFull(String fnr, LocalDate fom) {
        FinnGrunnlagListeRequest finnGrunnlagListeRequest = new FinnGrunnlagListeRequest();
        try {
            finnGrunnlagListeRequest.setFom(DateUtil.convertToXMLGregorianCalendar(fom));
            finnGrunnlagListeRequest.setTom(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(9999, Month.DECEMBER, 31)));
            finnGrunnlagListeRequest.setPersonident(fnr);
            return infotrygdBeregningsgrunnlagConsumer.finnBeregningsgrunnlagListe(finnGrunnlagListeRequest);

        } catch (FinnGrunnlagListeSikkerhetsbegrensning e) {
            throw InfotrygdTjenesteFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        } catch (FinnGrunnlagListeUgyldigInput e) {
            throw InfotrygdTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, e).toException();
        } catch (FinnGrunnlagListePersonIkkeFunnet e) {//$NON-NLS-1$ //NOSONAR
            // Skal ut ifra erfaringer fra fundamentet ikke gjøres noe med, fordi dette er normalt.
            InfotrygdTjenesteFeil.FACTORY.personIkkeFunnet(e).log(log);
        }
        return new FinnGrunnlagListeResponse();
    }

    private List<YtelseBeregningsgrunnlag> convertNy(FinnGrunnlagListeResponse finnGrunnlagListeResponse) {
        List<YtelseBeregningsgrunnlag> alleGrunnlag = new ArrayList<>();

        if (finnGrunnlagListeResponse == null) {
            return alleGrunnlag;
        }

        for (Foreldrepenger fp : finnGrunnlagListeResponse.getForeldrepengerListe()) {
            TemaUnderkategori tuk = getBehandlingsTema(fp);
            alleGrunnlag.add(new YtelseBeregningsgrunnlagForeldrepenger(fp, tuk));
        }

        for (Engangsstoenad engangsstoenad : finnGrunnlagListeResponse.getEngangstoenadListe()) {
            TemaUnderkategori tuk = getBehandlingsTema(engangsstoenad);
            alleGrunnlag.add(new YtelseBeregningsgrunnlagEngangstønad(engangsstoenad, tuk));
        }

        for (Sykepenger sykep : finnGrunnlagListeResponse.getSykepengerListe()) {
            TemaUnderkategori tuk = getBehandlingsTema(sykep);
            alleGrunnlag.add(new YtelseBeregningsgrunnlagSykepenger(sykep, tuk));
        }

        for (PaaroerendeSykdom paaroerendeSykdom : finnGrunnlagListeResponse.getPaaroerendeSykdomListe()) {
            TemaUnderkategori tuk = getBehandlingsTema(paaroerendeSykdom);
            alleGrunnlag.add(new YtelseBeregningsgrunnlagPårørendeSykdom(paaroerendeSykdom, tuk));
        }

        return alleGrunnlag;
    }

    private TemaUnderkategori getBehandlingsTema(Grunnlag grunnlag) {
        return TemaUnderkategoriReverse.reverseMap(grunnlag.getBehandlingstema().getValue());
    }

}
