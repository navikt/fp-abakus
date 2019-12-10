package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

public class InntektArbeidYtelseTjenesteTest {

    private Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(new OrgNummer(OrgNummer.KUNSTIG_ORG));
    
    @Test
    public void skal_kopiere_iay_grunnlag() throws Exception {

        // Arrange
        LocalDateTime innsendingstidspunkt = LocalDateTime.now().minusDays(10);
        var nå = nyInntektsmelding(innsendingstidspunkt, "1");
        var ny = nyInntektsmelding(innsendingstidspunkt.plusDays(1), "2");
        var gammel = nyInntektsmelding(innsendingstidspunkt.minusDays(1), "3");

        var iaygBuilder = InntektArbeidYtelseGrunnlagBuilder.nytt();
        iaygBuilder.setInntektsmeldinger(new InntektsmeldingAggregat(List.of(gammel, nå)));

        var iayr = Mockito.spy(new InntektArbeidYtelseRepository(Mockito.mock(EntityManager.class)));
        Mockito.doNothing().when(iayr).lagre(any(KoblingReferanse.class), any(InntektArbeidYtelseGrunnlagBuilder.class));

        Mockito.doAnswer(i -> Optional.of(iaygBuilder.build())).when(iayr).hentInntektArbeidYtelseGrunnlagForBehandling(any());
        Mockito.doReturn(Set.of(gammel, nå, ny)).when(iayr).hentAlleInntektsmeldingerFor(any(), any(), any());
        
        // Act
        var iayt = new InntektArbeidYtelseTjeneste(iayr);
        iayt.kopierGrunnlagFraEksisterendeBehandling(null, null, null, new KoblingReferanse(UUID.randomUUID()), new KoblingReferanse(UUID.randomUUID()));

        // Assert
        ArgumentCaptor<InntektArbeidYtelseGrunnlagBuilder> iaygBuilderCaptor = ArgumentCaptor.forClass(InntektArbeidYtelseGrunnlagBuilder.class); 
        Mockito.verify(iayr).lagre(any(KoblingReferanse.class), iaygBuilderCaptor.capture());
        var lagret = iaygBuilderCaptor.getValue();
        var nyIay = lagret.build(); // denne skal aldri ha vært kalt siden vi stubbet ut
        assertThat(nyIay).isNotNull();
        assertThat(nyIay.getInntektsmeldinger()).isPresent();
        var sisteInntektsmeldinger = nyIay.getInntektsmeldinger().get().getAlleInntektsmeldinger();
        assertThat(sisteInntektsmeldinger).hasSize(1);
        var sisteIms = sisteInntektsmeldinger.get(0);
        
        //Assert skal kun ha siste inntektsmelding siden alle 3 hadde samme arbeidsgiver
        assertThat(sisteIms.getInnsendingstidspunkt()).isEqualTo(ny.getInnsendingstidspunkt());
        
        
        

    }

    private Inntektsmelding nyInntektsmelding(LocalDateTime innsendingstidspunkt, String journalpostId) {
        return InntektsmeldingBuilder.builder().medInnsendingstidspunkt(innsendingstidspunkt).medJournalpostId(journalpostId).medArbeidsgiver(arbeidsgiver).build();
    }
}
