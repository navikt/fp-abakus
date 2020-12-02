package no.nav.foreldrepenger.abakus.aktor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;
import no.nav.vedtak.felles.integrasjon.pdl.Tema;
import no.nav.vedtak.util.LRUCache;


@ApplicationScoped
public class AktørTjeneste {

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private LRUCache<AktørId, PersonIdent> cacheAktørIdTilIdent;
    private LRUCache<PersonIdent, AktørId> cacheIdentTilAktørId;

    private PdlKlient pdlKlient;


    public AktørTjeneste() {
        // for CDI proxy
    }

    @Inject
    public AktørTjeneste(PdlKlient pdlKlient) {
        this.pdlKlient = pdlKlient;
        this.cacheAktørIdTilIdent = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
        this.cacheIdentTilAktørId = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public Optional<AktørId> hentAktørForIdent(PersonIdent fnr, YtelseType ytelse) {
        var tema = ytelse != null ? gjelderTema(ytelse) : null;
        if (tema == null)
            throw new IllegalArgumentException("Utviklerfeil mangler fagsystem mot PDL");

        var request = new HentIdenterQueryRequest();
        request.setIdent(fnr.getIdent());
        request.setGrupper(List.of(IdentGruppe.AKTORID));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection()
            .identer(new IdentInformasjonResponseProjection().ident());

        try {
            var identliste = pdlKlient.hentIdenter(request, projection, Tema.FOR);
            var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(AktørId::new);
            aktørId.ifPresent(a -> cacheIdentTilAktørId.put(fnr, a));
            return aktørId;
        } catch (VLException v) {
            if (PdlKlient.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            throw v;
        }
    }

    public Optional<PersonIdent> hentIdentForAktør(AktørId aktørId, YtelseType ytelse) {
        var tema = ytelse != null ? gjelderTema(ytelse) : null;
        if (tema == null)
            throw new IllegalArgumentException("Utviklerfeil mangler fagsystem mot PDL");

        var fraCache = cacheAktørIdTilIdent.get(aktørId);
        if (fraCache != null) {
            return Optional.of(fraCache);
        }
        var request = new HentIdenterQueryRequest();
        request.setIdent(aktørId.getId());
        request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection()
            .identer(new IdentInformasjonResponseProjection().ident());

        final Identliste identliste;

        try {
            identliste = pdlKlient.hentIdenter(request, projection, Tema.FOR);
            var ident = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(PersonIdent::new);
            ident.ifPresent(i -> cacheAktørIdTilIdent.put(aktørId, i));
            return ident;
        } catch (VLException v) {
            if (PdlKlient.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            throw v;
        }
    }

    private static Tema gjelderTema(YtelseType y) {
        return Set.of(YtelseType.ENGANGSTØNAD, YtelseType.FORELDREPENGER, YtelseType.SVANGERSKAPSPENGER).contains(y) ? Tema.FOR : Tema.OMS;
    }
}
