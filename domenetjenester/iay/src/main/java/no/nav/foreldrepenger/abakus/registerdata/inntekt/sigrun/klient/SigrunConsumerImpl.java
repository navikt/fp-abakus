package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import static java.util.Arrays.asList;

import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden.PgiFolketrygdenResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden.SigrunPgiFolketrygdenResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;

@ApplicationScoped
public class SigrunConsumerImpl implements SigrunConsumer {

    private static final String TEKNISK_NAVN = "skatteoppgjoersdato";

    private static final MonthDay TIDLIGSTE_SJEKK_FJOR = MonthDay.of(5, 1);

    private static final Year FØRSTE_PGI = Year.of(2017);
    private static final boolean IS_PROD = Environment.current().isProd();
    private final SigrunRestClient client;


    public SigrunConsumerImpl() {
        this.client = new SigrunRestClient(RestClient.client());

    }

    // Testformål
    SigrunConsumerImpl(SigrunRestClient restClient) {
        this.client = restClient;

    }

    @Override
    public SigrunResponse beregnetskatt(Long aktørId, IntervallEntitet opplysningsperiode) {
        var årTilListeMedSkatt = ferdiglignedeBeregnetSkattÅr(aktørId, opplysningsperiode).stream()
            .collect(Collectors.toMap(år -> år, år -> client.hentBeregnetSkattForAktørOgÅr(aktørId, år.toString())));
        return new SigrunResponse(årTilListeMedSkatt);
    }

    @Override
    public SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag(Long aktørId, IntervallEntitet opplysningsperiode) {
        Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap = hentÅrsListeForSummertskattegrunnlag(aktørId, opplysningsperiode).stream()
            .collect(Collectors.toMap(år -> år, år -> client.hentSummertskattegrunnlag(aktørId, år.toString())));
        return new SigrunSummertSkattegrunnlagResponse(summertskattegrunnlagMap);
    }

    @Override
    public SigrunPgiFolketrygdenResponse pgiFolketrygden(String fnr, IntervallEntitet opplysningsperiode) {
        var funnet = hentÅrsListeForPgiFolketrygden(fnr, opplysningsperiode).stream()
            .collect(Collectors.toMap(år -> år, år -> client.hentPgiForFolketrygden(fnr, år.toString())));
        return new SigrunPgiFolketrygdenResponse(funnet);
    }

    @Override
    public List<PgiFolketrygdenResponse> pensjonsgivendeInntektForFolketrygden(String fnr, IntervallEntitet opplysningsperiode) {
        var senesteÅr = utledSenesteÅr(opplysningsperiode);
        List<PgiFolketrygdenResponse> svarene = new ArrayList<>();
        var svarSenesteÅr = svarForSenesteÅr(fnr, senesteÅr);
        svarSenesteÅr.ifPresent(svarene::add);
        utledTidligereÅr(opplysningsperiode, senesteÅr, svarSenesteÅr.isPresent())
            .forEach(år -> Optional.ofNullable(client.hentPensjonsgivendeInntektForFolketrygden(fnr, år)).ifPresent(svarene::add));
        return svarene;
    }

    public Optional<PgiFolketrygdenResponse> svarForSenesteÅr(String fnr, Year senesteÅr) {
        if (Year.now().minusYears(1).equals(senesteÅr) && MonthDay.now().isBefore(TIDLIGSTE_SJEKK_FJOR)) {
            return Optional.empty();
        }
        try {
            var svar = client.hentPensjonsgivendeInntektForFolketrygden(fnr, senesteÅr);
            return Optional.ofNullable(svar);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Year utledSenesteÅr(IntervallEntitet opplysningsperiode) {
        var ifjor = Year.now().minusYears(1);
        var oppgitt = opplysningsperiode != null ? Year.from(opplysningsperiode.getTomDato()) : ifjor;
        return oppgitt.isAfter(ifjor) ? ifjor : oppgitt;
    }

    private List<Year> utledTidligereÅr(IntervallEntitet opplysningsperiode, Year senesteÅr, boolean harDataSenesteÅr) {
        var tidligsteÅr = opplysningsperiode != null ? Year.from(opplysningsperiode.getFomDato()) : senesteÅr.minusYears(2);
        var fraTidligsteÅr = harDataSenesteÅr ? tidligsteÅr : tidligsteÅr.minusYears(1);
        if (fraTidligsteÅr.isBefore(FØRSTE_PGI)) {
            fraTidligsteÅr = FØRSTE_PGI;
        }
        List<Year> årene = new ArrayList<>();
        while (fraTidligsteÅr.isBefore(senesteÅr)) {
            årene.add(fraTidligsteÅr);
            fraTidligsteÅr = fraTidligsteÅr.plusYears(1);
        }
        return årene.stream().sorted(Comparator.reverseOrder()).toList();
    }

    @Override
    public boolean erÅretFerdiglignet(Long aktørId, Year år) {
        if (IS_PROD && Year.now().minusYears(1).equals(år) && MonthDay.now().isBefore(TIDLIGSTE_SJEKK_FJOR)) {
            return false;
        }
        return client.hentBeregnetSkattForAktørOgÅr(aktørId, år.toString()).stream().anyMatch(l -> l.tekniskNavn().equals(TEKNISK_NAVN));
    }

    private List<Year> ferdiglignedeBeregnetSkattÅr(Long aktørId, IntervallEntitet opplysningsperiode) {
        if (opplysningsperiode != null) {
            return beregnetSkattÅrslisteFraOpplysningsperiode(opplysningsperiode);
        } else {
            Year iFjor = Year.now().minusYears(1L);
            if (erÅretFerdiglignet(aktørId, iFjor)) {
                return asList(iFjor, iFjor.minusYears(1L), iFjor.minusYears(2L));
            } else {
                Year iForifjor = iFjor.minusYears(1L);
                return asList(iForifjor, iForifjor.minusYears(1L), iForifjor.minusYears(2L));
            }
        }
    }

    private List<Year> beregnetSkattÅrslisteFraOpplysningsperiode(IntervallEntitet opplysningsperiode) {
        var fomÅr = opplysningsperiode.getFomDato().getYear();
        var tomÅr = opplysningsperiode.getTomDato().getYear();
        var år = fomÅr;
        var årsListe = new ArrayList<Year>();
        while (år <= tomÅr) {
            årsListe.add(Year.of(år));
            år++;
        }
        return årsListe;
    }

    List<Year> hentÅrsListeForSummertskattegrunnlag(Long aktørId, IntervallEntitet opplysningsperiode) {
        Year iFjor = Year.now().minusYears(1L);
        if (opplysningsperiode != null) {
            return summertSkattegrunnlagÅrslisteFraOpplysningsperiode(opplysningsperiode);
        } else {
            //filteret(SummertSkattegrunnlagForeldrepenger) i Sigrun er ikke impl. tidligere enn 2018
            if (iFjor.equals(Year.of(2018))) {
                return List.of(iFjor);
            } else if (iFjor.equals(Year.of(2019))) {
                return List.of(iFjor, iFjor.minusYears(1L));
            }
            return ferdiglignedeBeregnetSkattÅr(aktørId, opplysningsperiode);
        }
    }

    private ArrayList<Year> summertSkattegrunnlagÅrslisteFraOpplysningsperiode(IntervallEntitet opplysningsperiode) {
        var fomÅr = opplysningsperiode.getFomDato().getYear();
        var tomÅr = opplysningsperiode.getTomDato().getYear();
        var år = fomÅr;
        var årsListe = new ArrayList<Year>();
        while (år <= tomÅr) {
            //filteret(SummertSkattegrunnlagForeldrepenger) i Sigrun er ikke impl. tidligere enn 2018
            if (år >= 2018) {
                årsListe.add(Year.of(år));
            }
            år++;
        }
        return årsListe;
    }

    List<Year> hentÅrsListeForPgiFolketrygden(String fnr, IntervallEntitet opplysningsperiode) {
        if (opplysningsperiode != null) {
            return pgiFolketrygdenÅrslisteFraOpplysningsperiode(opplysningsperiode);
        } else {
            return ferdiglignedePgiFolketrygdenÅr(fnr);
        }
    }

    private List<Year> pgiFolketrygdenÅrslisteFraOpplysningsperiode(IntervallEntitet opplysningsperiode) {
        var fomÅr = opplysningsperiode.getFomDato().getYear();
        var tomÅr = opplysningsperiode.getTomDato().getYear();
        var år = fomÅr;
        var årsListe = new HashSet<Year>();
        while (år <= tomÅr) {
            // PGI-FT fom 2017
            if (år >= 2017) {
                årsListe.add(Year.of(år));
            }
            år++;
        }
        return new ArrayList<>(årsListe);
    }

    private List<Year> ferdiglignedePgiFolketrygdenÅr(String fnr) {
        Year iFjor = Year.now().minusYears(1L);
        if (!client.hentPgiForFolketrygden(fnr, iFjor.toString()).isEmpty()) {
            return asList(iFjor, iFjor.minusYears(1L), iFjor.minusYears(2L));
        } else {
            Year iForifjor = iFjor.minusYears(1L);
            return asList(iForifjor, iForifjor.minusYears(1L), iForifjor.minusYears(2L));
        }
    }


}
