package no.nav.foreldrepenger.abakus.kobling;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

@ApplicationScoped
public class KoblingTjeneste {

    private KoblingRepository repository;
    private LåsRepository låsRepository;

    KoblingTjeneste() {
    }

    @Inject
    public KoblingTjeneste(KoblingRepository repository, LåsRepository låsRepository) {
        this.repository = repository;
        this.låsRepository = låsRepository;
    }

    public Kobling finnEllerOpprett(YtelseType ytelseType, KoblingReferanse referanse, AktørId aktørId, Saksnummer saksnummer) {
        Kobling kobling = repository.hentForKoblingReferanse(referanse, true)
            .orElseGet(() -> new Kobling(ytelseType, saksnummer, referanse, aktørId));
        repository.lagre(kobling);
        return kobling;
    }

    public Optional<Kobling> hentFor(KoblingReferanse referanse) {
        return repository.hentForKoblingReferanse(referanse);
    }

    public Optional<Kobling> hentSisteFor(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType) {
        return repository.hentSisteKoblingReferanseFor(aktørId, saksnummer, ytelseType);
    }

    public void lagre(Kobling kobling) {
        repository.lagre(kobling);
    }

    public Kobling hent(Long koblingId) {
        return repository.hentForKoblingId(koblingId);
    }

    public KoblingLås taSkrivesLås(KoblingReferanse referanse) {
        return repository.hentForKoblingReferanse(referanse, true).map(Kobling::getId).map(KoblingLås::new).orElse(null);
    }

    public void oppdaterLåsVersjon(KoblingLås lås) {
        låsRepository.oppdaterLåsVersjon(lås);
    }

    public List<Saksnummer> hentAlleSaksnummer() {
        return repository.hentAlleSaksnummer();
    }
}
