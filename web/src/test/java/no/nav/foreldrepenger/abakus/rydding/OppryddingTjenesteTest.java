package no.nav.foreldrepenger.abakus.rydding;

import static java.util.stream.LongStream.rangeClosed;
import static no.nav.foreldrepenger.abakus.rydding.OppryddingTjeneste.MAX_PARTITION_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
class OppryddingTjenesteTest {

    @Mock
    private OppryddingIAYAggregatRepository oppryddingIAYAggregatRepository;
    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;

    private OppryddingTjeneste oppryddingTjeneste;

    @BeforeEach
    void setUp() {
        oppryddingTjeneste = new OppryddingTjeneste(oppryddingIAYAggregatRepository, prosessTaskTjeneste);
    }

    @Test
    void testFjernAlleIayAggregatUtenReferanse_noAggregates_ok() {
        when(oppryddingIAYAggregatRepository.hentAlleIayAggregatUtenReferanse()).thenReturn(List.of());

        oppryddingTjeneste.fjernAlleIayAggregatUtenReferanse();

        verify(oppryddingIAYAggregatRepository, times(1)).hentAlleIayAggregatUtenReferanse();
        verifyNoInteractions(prosessTaskTjeneste);
    }

    @Test
    void testFjernAlleIayAggregatUtenReferanse_withAggregates_ok() {
        var aggregates = List.of(1L, 2L, 3L);
        when(oppryddingIAYAggregatRepository.hentAlleIayAggregatUtenReferanse()).thenReturn(aggregates);

        oppryddingTjeneste.fjernAlleIayAggregatUtenReferanse();

        verify(oppryddingIAYAggregatRepository).hentAlleIayAggregatUtenReferanse();
        verify(prosessTaskTjeneste, times(1)).lagre(any(ProsessTaskData.class));
    }

    @Test
    void testPartisjonerOverFlereTask_ok() {
        var aggregates = rangeClosed(1, MAX_PARTITION_SIZE + 50).boxed().toList();
        when(oppryddingIAYAggregatRepository.hentAlleIayAggregatUtenReferanse()).thenReturn(aggregates);

        oppryddingTjeneste.fjernAlleIayAggregatUtenReferanse();

        assertThat(aggregates).hasSize(MAX_PARTITION_SIZE + 50);
        verify(prosessTaskTjeneste, times(2)).lagre(any(ProsessTaskData.class));
    }

    @Test
    void testOpprettFjernIayAggregatTask_ok() {
        var aggregates = rangeClosed(1, MAX_PARTITION_SIZE).boxed().toList();
        when(oppryddingIAYAggregatRepository.hentAlleIayAggregatUtenReferanse()).thenReturn(aggregates);

        oppryddingTjeneste.fjernAlleIayAggregatUtenReferanse();

        assertThat(aggregates).hasSize(MAX_PARTITION_SIZE);
        verify(prosessTaskTjeneste).lagre(any(ProsessTaskData.class));
    }

}
