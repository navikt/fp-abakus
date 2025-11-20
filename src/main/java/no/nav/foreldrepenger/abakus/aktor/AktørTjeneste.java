package no.nav.foreldrepenger.abakus.aktor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.person.Persondata;
import no.nav.vedtak.util.LRUCache;


@ApplicationScoped
public class AktørTjeneste {

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private static final LRUCache<AktørId, PersonIdent> CACHE_AKTØR_ID_TIL_IDENT = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    private static final LRUCache<PersonIdent, AktørId> CACHE_IDENT_TIL_AKTØR_ID = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);

    private Persondata pdlKlient;

    AktørTjeneste() {
        // CDI
    }

    @Inject
    public AktørTjeneste(Persondata pdlKlient) {
        this.pdlKlient = pdlKlient;
    }

    public Optional<AktørId> hentAktørForIdent(PersonIdent fnr) {
        var request = new HentIdenterQueryRequest();
        request.setIdent(fnr.getIdent());
        request.setGrupper(List.of(IdentGruppe.AKTORID));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection().identer(new IdentInformasjonResponseProjection().ident());

        try {
            var identliste = hentIdenter(request, projection);
            var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(AktørId::new);
            aktørId.ifPresent(a -> CACHE_IDENT_TIL_AKTØR_ID.put(fnr, a));
            return aktørId;
        } catch (VLException v) {
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            throw v;
        }
    }

    public Set<AktørId> hentAktørIderForIdent(PersonIdent fnr) {
        var request = new HentIdenterQueryRequest();
        request.setIdent(fnr.getIdent());
        request.setGrupper(List.of(IdentGruppe.AKTORID));
        request.setHistorikk(Boolean.TRUE);
        var projection = new IdentlisteResponseProjection().identer(new IdentInformasjonResponseProjection().ident());

        try {
            var identliste = hentIdenter(request, projection);
            return identliste.getIdenter().stream().map(IdentInformasjon::getIdent).map(AktørId::new).collect(Collectors.toSet());
        } catch (VLException v) {
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Set.of();
            }
            throw v;
        }
    }

    public Optional<PersonIdent> hentIdentForAktør(AktørId aktørId) {
        var fraCache = CACHE_AKTØR_ID_TIL_IDENT.get(aktørId);
        if (fraCache != null) {
            return Optional.of(fraCache);
        }
        var request = new HentIdenterQueryRequest();
        request.setIdent(aktørId.getId());
        request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection().identer(new IdentInformasjonResponseProjection().ident());

        final Identliste identliste;

        try {
            identliste = hentIdenter(request, projection);
            var ident = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(PersonIdent::new);
            ident.ifPresent(i -> CACHE_AKTØR_ID_TIL_IDENT.put(aktørId, i));
            return ident;
        } catch (VLException v) {
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            throw v;
        }
    }

    public Set<PersonIdent> hentPersonIdenterForAktør(AktørId aktørId) {
        var request = new HentIdenterQueryRequest();
        request.setIdent(aktørId.getId());
        request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
        request.setHistorikk(Boolean.TRUE);
        var projection = new IdentlisteResponseProjection().identer(new IdentInformasjonResponseProjection().ident());

        try {
            var identliste = hentIdenter(request, projection);
            return identliste.getIdenter().stream().map(IdentInformasjon::getIdent).map(PersonIdent::new).collect(Collectors.toSet());
        } catch (VLException v) {
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Set.of();
            }
            throw v;
        }
    }

    private Identliste hentIdenter(HentIdenterQueryRequest request, IdentlisteResponseProjection projection) {
        return pdlKlient.hentIdenter(request, projection);
    }
}
