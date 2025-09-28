package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;

public record InntektsInformasjon(List<Månedsinntekt> månedsinntekter, InntektskildeType kilde) {

    public List<Månedsinntekt> getMånedsinntekterUtenomYtelser() {
        return månedsinntekter().stream().filter(it -> !it.isYtelse()).toList();
    }

    public List<Månedsinntekt> getYtelsesTrygdEllerPensjonInntektSummert() {
        // Grupperer og summerer pr måned/type/ytelse og lager ny liste med summerte beløp
        return månedsinntekter().stream()
            .filter(Månedsinntekt::isYtelse)
            .collect(Collectors.groupingBy(Månedsinntekt::getNøkkel, Collectors.reducing(BigDecimal.ZERO, Månedsinntekt::beløp, BigDecimal::add)))
            .entrySet().stream()
            .map(e -> new Månedsinntekt(e.getKey().type(), e.getKey().måned(), e.getValue(), e.getKey().beskrivelse(), null, null))
            .toList();
    }

}
