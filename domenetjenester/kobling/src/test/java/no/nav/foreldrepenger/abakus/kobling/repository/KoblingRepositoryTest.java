package no.nav.foreldrepenger.abakus.kobling.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class KoblingRepositoryTest extends EntityManagerAwareTest {

    private KoblingRepository koblingRepository;

    @BeforeEach
    void setUp() {
        koblingRepository = new KoblingRepository(getEntityManager());
    }

    @Test
    void lagre_ny_kobling_ok() {
        var koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        var nyKobling = new Kobling(YtelseType.FORELDREPENGER, new Saksnummer("23234234"), koblingReferanse, new AktørId(1232123343423L));
        assertDoesNotThrow(() -> koblingRepository.lagre(nyKobling));
    }

    @Test
    void lagre_ny_kobling_med_lik_referanse_nok() {
        var referanse = UUID.randomUUID();
        var koblingReferanse = new KoblingReferanse(referanse);
        var nyKobling = new Kobling(YtelseType.FORELDREPENGER, new Saksnummer("23234234"), koblingReferanse, new AktørId(1232123343423L));
        assertDoesNotThrow(() -> koblingRepository.lagre(nyKobling));

        var nyereKobling = new Kobling(YtelseType.FORELDREPENGER, new Saksnummer("23234234"), koblingReferanse, new AktørId(1232123343423L));
        nyereKobling.deaktiver();
        var ex = assertThrows(IllegalStateException.class, () -> koblingRepository.lagre(nyereKobling));

        assertThat(ex.getMessage()).startsWith("Utviklerfeil: Kan ikke lagre en ny kobling for eksisterende kobling referanse.");
    }

    @Test
    void lagre_ny_kobling_som_er_deaktivert_nok() {
        var referanse = UUID.randomUUID();
        var koblingReferanse = new KoblingReferanse(referanse);
        var nyKobling = new Kobling(YtelseType.FORELDREPENGER, new Saksnummer("23234234"), koblingReferanse, new AktørId(1232123343423L));
        assertDoesNotThrow(() -> koblingRepository.lagre(nyKobling));
        var kobling = koblingRepository.hentForKoblingReferanse(koblingReferanse, true).orElseThrow();
        kobling.deaktiver();
        assertDoesNotThrow(() -> koblingRepository.lagre(kobling)); // her bør den første endringer ikke feile

        var nyereKobling = koblingRepository.hentForKoblingReferanse(koblingReferanse, true).orElseThrow();
        nyereKobling.setYtelseType(YtelseType.SVANGERSKAPSPENGER);
        var ex = assertThrows(IllegalStateException.class, () -> koblingRepository.lagre(nyereKobling));
        assertThat(ex.getMessage()).startsWith("Etterspør kobling:").contains(referanse.toString()).endsWith("men denne er ikke aktiv") ;
    }
}
