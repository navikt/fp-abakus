package no.nav.foreldrepenger.abakus.domene.virksomhet;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.abakus.iaygrunnlag.kodeverk.OrganisasjonType;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class VirksomhetRepositoryImpl implements VirksomhetRepository {

    private final Virksomhet KUNSTIG_VIRKSOMHET = new Virksomhet.Builder()
        .medNavn("Kunstig virksomhet")
        .medOrganisasjonstype(OrganisasjonType.KUNSTIG)
        .medOrgnr(OrgNummer.KUNSTIG_ORG)
        .medRegistrert(LocalDate.of(1978, 01, 01))
        .medOppstart(LocalDate.of(1978, 01, 01))
        .build();
    
    private LRUCache<String, Virksomhet> cache;

    public VirksomhetRepositoryImpl() {
        this.cache = new LRUCache<>(1000, 60 * 1000 * 15);
    }

    @Override
    public Optional<Virksomhet> hent(String orgnr) {
        if (Objects.equals(KUNSTIG_VIRKSOMHET.getOrgnr(), orgnr)) {
            return Optional.of(KUNSTIG_VIRKSOMHET);
        }
        return Optional.ofNullable(cache.get(orgnr));
    }

    @Override
    public void lagre(Virksomhet virksomhet) {
        Objects.requireNonNull(virksomhet, "virksomhet kan ikke være null");
        cache.put(virksomhet.getOrgnr(), virksomhet);
    }

}
