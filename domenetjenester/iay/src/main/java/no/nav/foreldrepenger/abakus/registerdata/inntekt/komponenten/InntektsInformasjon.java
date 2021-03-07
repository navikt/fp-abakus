package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt.YtelseNøkkel;

public class InntektsInformasjon {

    private List<Månedsinntekt> månedsinntekter;
    private List<FrilansArbeidsforhold> frilansArbeidsforhold = new ArrayList<>();
    private InntektskildeType kilde;

    public InntektsInformasjon(List<Månedsinntekt> månedsinntekter, List<FrilansArbeidsforhold> frilansArbeidsforhold, InntektskildeType kilde) {
        this.månedsinntekter = new ArrayList<>();
        this.månedsinntekter.addAll(månedsinntekter);
        this.frilansArbeidsforhold = frilansArbeidsforhold;
        this.kilde = kilde;
    }

    public List<Månedsinntekt> getMånedsinntekter() {
        return Collections.unmodifiableList(månedsinntekter);
    }

    public void leggTilMånedsinntekter(List<Månedsinntekt> inntekter) {
        this.månedsinntekter.addAll(inntekter);
    }

    public Map<ArbeidsforholdIdentifikator, List<FrilansArbeidsforhold>> getFrilansArbeidsforhold() {
        return frilansArbeidsforhold.stream().collect(Collectors.groupingBy(FrilansArbeidsforhold::getIdentifikator));
    }

    public List<Månedsinntekt> getMånedsinntekterUtenomYtelser() {
        return getMånedsinntekter().stream()
            .filter(it -> !it.isYtelse())
            .collect(Collectors.toList());
    }

    public List<Månedsinntekt> getYtelsesTrygdEllerPensjonInntektSummert() {
        Map<YtelseNøkkel, List<Månedsinntekt>> ytelseNøkkel = månedsinntekter.stream().filter(Månedsinntekt::isYtelse)
            .collect(Collectors.groupingBy(Månedsinntekt::getNøkkel));

        List<Månedsinntekt> summert = new ArrayList<>();

        for (Map.Entry<YtelseNøkkel, List<Månedsinntekt>> entry : ytelseNøkkel.entrySet()) {
            BigDecimal sum = entry.getValue().stream().map(Månedsinntekt::getBeløp).reduce(BigDecimal.ZERO, BigDecimal::add);
            Månedsinntekt.Builder builder = new Månedsinntekt.Builder();
            YtelseNøkkel nøkkel = entry.getKey();
            builder.medBeløp(sum);
            builder.medYtelse(true);
            builder.medYtelseKode(nøkkel.getYtelseKode());
            builder.medNæringsinntektKode(nøkkel.getNæringsinntektKode());
            builder.medPensjonEllerTrygdKode(nøkkel.getPensjonKode());
            builder.medMåned(nøkkel.getMåned());
            summert.add(builder.build());
        }
        return summert;
    }

    public InntektskildeType getKilde() {
        return kilde;
    }

}
