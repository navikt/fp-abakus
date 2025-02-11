package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.PgiFolketrygdenResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunPgiFolketrygdenMapper;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestClient;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.YEARS;


@ApplicationScoped
public class SigrunTjeneste {
    private static final MonthDay TIDLIGSTE_SJEKK_FJOR = MonthDay.of(Month.MAY, 1);

    private static final Year FØRSTE_PGI = Year.of(2017);
    private SigrunRestClient sigrunConsumer;

    SigrunTjeneste() {
        //CDI
    }

    @Inject
    public SigrunTjeneste(SigrunRestClient sigrunConsumer) {
        this.sigrunConsumer = sigrunConsumer;
    }


    public Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> hentPensjonsgivende(PersonIdent fnr, IntervallEntitet opplysningsperiodeSkattegrunnlag) {
        var svarene = pensjonsgivendeInntektForFolketrygden(fnr.getIdent(), opplysningsperiodeSkattegrunnlag);
        return SigrunPgiFolketrygdenMapper.mapFraPgiResponseTilIntern(svarene).entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<PgiFolketrygdenResponse> pensjonsgivendeInntektForFolketrygden(String fnr, IntervallEntitet opplysningsperiode) {
        var senesteDato = utledSeneste(opplysningsperiode);
        List<PgiFolketrygdenResponse> svarene = new ArrayList<>();
        var svarSenesteÅr = svarForSenesteÅr(fnr, Year.from(senesteDato));
        svarSenesteÅr.ifPresent(svarene::add);
        utledTidligereÅr(opplysningsperiode, senesteDato, svarSenesteÅr.isPresent())
            .forEach(år -> sigrunConsumer.hentPensjonsgivendeInntektForFolketrygden(fnr, år).ifPresent(svarene::add));
        return svarene;
    }

    private LocalDate utledSeneste(IntervallEntitet opplysningsperiode) {
        var ifjor = LocalDate.now().minusYears(1);
        var oppgitt = opplysningsperiode != null ? opplysningsperiode.getTomDato() : ifjor;
        // Ikke senere år enn i fjor
        return oppgitt.getYear() > ifjor.getYear() ? ifjor : oppgitt;
    }

    public Optional<PgiFolketrygdenResponse> svarForSenesteÅr(String fnr, Year senesteÅr) {
        // Venter ikke svar før i fjor og ikke før etter TIDLIGSTE_SJEKK_FJOR
        var ifjor = Year.now().minusYears(1);
        if (senesteÅr.isAfter(ifjor) || (ifjor.equals(senesteÅr) && MonthDay.now().isBefore(TIDLIGSTE_SJEKK_FJOR))) {
            return Optional.empty();
        }
        try {
            return sigrunConsumer.hentPensjonsgivendeInntektForFolketrygden(fnr, senesteÅr);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private List<Year> utledTidligereÅr(IntervallEntitet opplysningsperiode, LocalDate senesteDato, boolean harDataSenesteÅr) {
        var senesteÅr = Year.from(senesteDato);
        long periodeLengde = opplysningsperiode != null ? periodeLengde(opplysningsperiode, senesteDato) : 2L;
        var tidligsteÅr = opplysningsperiode != null ? Year.from(opplysningsperiode.getFomDato()) : senesteÅr.minusYears(2);
        var fraTidligsteÅr = harDataSenesteÅr || periodeLengde > 2L ? tidligsteÅr : tidligsteÅr.minusYears(1);
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

    private long periodeLengde(IntervallEntitet opplysningsperiode, LocalDate senesteDato) {
        var lengde = YEARS.between(opplysningsperiode.getFomDato(), senesteDato);
        return lengde >= 2 ? lengde : 2;
    }

}
