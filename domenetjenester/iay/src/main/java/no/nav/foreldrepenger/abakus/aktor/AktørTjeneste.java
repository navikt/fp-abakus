package no.nav.foreldrepenger.abakus.aktor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;
import no.nav.vedtak.felles.integrasjon.pdl.Tema;


@ApplicationScoped
public class AktørTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(AktørTjeneste.class);

    private PdlKlient pdlKlient;
    private AktørConsumerMedCache aktørConsumer;


    public AktørTjeneste() {
        // for CDI proxy
    }

    @Inject
    public AktørTjeneste(PdlKlient pdlKlient,
                       AktørConsumerMedCache aktørConsumer) {
        this.pdlKlient = pdlKlient;
        this.aktørConsumer = aktørConsumer;
    }

    public Optional<AktørId> hentAktørForIdent(PersonIdent fnr, YtelseType ytelse) {
        var aid = aktørConsumer.hentAktørIdForPersonIdent(fnr.getIdent()).map(AktørId::new);
        aid.ifPresent(a -> hentAktørIdFraPDL(fnr, a.getId(), ytelse));
        return aid;
    }

    public Optional<PersonIdent> hentIdentForAktør(AktørId aktørId, YtelseType ytelse) {
        var ident = aktørConsumer.hentPersonIdentForAktørId(aktørId.getId()).map(PersonIdent::new);
        ident.ifPresent(i -> hentPersonIdentFraPDL(aktørId,i.getIdent(), ytelse));
        return ident;
    }

    private void hentAktørIdFraPDL(PersonIdent fnr, String aktørFraConsumer, YtelseType ytelse) {
        try {
            var tema = ytelse != null ? gjelderTema(ytelse) : null;
            if (tema == null)
                throw new IllegalArgumentException("Utviklerfeil mangler fagsystem mot PDL");

            var request = new HentIdenterQueryRequest();
            request.setIdent(fnr.getIdent());
            request.setGrupper(List.of(IdentGruppe.AKTORID));
            request.setHistorikk(Boolean.FALSE);

            var projection = new IdentlisteResponseProjection()
                .identer(new IdentInformasjonResponseProjection().ident().gruppe());

            var identliste = pdlKlient.hentIdenter(request, projection, tema);
            int antall = identliste.getIdenter().size();
            var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).orElse(null);
            if (antall == 1 && Objects.equals(aktørFraConsumer, aktørId)) {
                LOG.info("FPABAKUS PDL AKTØRID: like aktørid");
            } else if (antall != 1 && Objects.equals(aktørFraConsumer, aktørId)) {
                LOG.info("FPABAKUS PDL AKTØRID: ulikt antall aktørid {}", antall);
            } else {
                LOG.info("FPABAKUS PDL AKTØRID: ulike aktørid TPS og PDL, antall {}", antall);
            }
        } catch (Exception e) {
            LOG.info("FPABAKUS PDL AKTØRID hentaktørid error", e);
        }
    }

    private void hentPersonIdentFraPDL(AktørId aktørId, String identFraConsumer, YtelseType ytelse) {
        try {
            var tema = ytelse != null ? gjelderTema(ytelse) : null;
            if (tema == null)
                throw new IllegalArgumentException("Utviklerfeil mangler fagsystem mot PDL");

            var request = new HentIdenterQueryRequest();
            request.setIdent(aktørId.getId());
            request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
            request.setHistorikk(Boolean.FALSE);

            var projection = new IdentlisteResponseProjection()
                .identer(new IdentInformasjonResponseProjection().ident().gruppe());

            var identliste = pdlKlient.hentIdenter(request, projection, tema);
            int antall = identliste.getIdenter().size();
            var fnr = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).orElse(null);
            if (antall == 1 && Objects.equals(identFraConsumer, fnr)) {
                LOG.info("FPABAKUS PDL IDENT: like identer");
            } else if (antall != 1 && Objects.equals(identFraConsumer, fnr)) {
                LOG.info("FPABAKUS PDL IDENT: ulikt antall identer {}", antall);
            } else {
                LOG.info("FPABAKUS PDL IDENT: ulike identer TPS og PDL antall {}", antall);
            }
        } catch (Exception e) {
            LOG.info("FPABAKUS PDL IDENT hentident error", e);
        }
    }

    private static Tema gjelderTema(YtelseType y) {
        return Set.of(YtelseType.ENGANGSTØNAD, YtelseType.FORELDREPENGER, YtelseType.SVANGERSKAPSPENGER).contains(y) ? Tema.FOR : Tema.OMS;
    }
}
