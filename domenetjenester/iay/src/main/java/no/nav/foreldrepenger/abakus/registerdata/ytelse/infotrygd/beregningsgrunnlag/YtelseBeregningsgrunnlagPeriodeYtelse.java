package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.vedtak.domene.TemaUnderkategori;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PeriodeYtelse;

public abstract class YtelseBeregningsgrunnlagPeriodeYtelse extends YtelseBeregningsgrunnlag {

    private static final Logger log = LoggerFactory.getLogger(YtelseBeregningsgrunnlagPeriodeYtelse.class);

    private static final Map<String, Arbeidskategori> ARBEIDSKATEGORI_MAP = Map.ofEntries(
        Map.entry("00", Arbeidskategori.FISKER),
        Map.entry("01", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("02", Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE),
        Map.entry("03", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE),
        Map.entry("04", Arbeidskategori.SJØMANN),
        Map.entry("05", Arbeidskategori.JORDBRUKER),
        Map.entry("06", Arbeidskategori.DAGPENGER),
        Map.entry("07", Arbeidskategori.INAKTIV),
        Map.entry("08", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("09", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("10", Arbeidskategori.SJØMANN),
        Map.entry("11", Arbeidskategori.SJØMANN),
        Map.entry("12", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("13", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER),
        Map.entry("14", Arbeidskategori.UGYLDIG),
        Map.entry("15", Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE),
        Map.entry("16", Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE),
        Map.entry("17", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FISKER),
        Map.entry("18", Arbeidskategori.UGYLDIG),
        Map.entry("19", Arbeidskategori.FRILANSER),
        Map.entry("20", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER),
        Map.entry("21", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("22", Arbeidskategori.SJØMANN),
        Map.entry("23", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER),
        Map.entry("24", Arbeidskategori.FRILANSER),
        Map.entry("25", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER),
        Map.entry("26", Arbeidskategori.DAGMAMMA),
        Map.entry("27", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("30", Arbeidskategori.UGYLDIG),
        Map.entry("99", Arbeidskategori.UGYLDIG));


    private List<YtelseBeregningsgrunnlagArbeidsforhold> arbeidsforhold;
    private Arbeidskategori arbeidskategori;

    YtelseBeregningsgrunnlagPeriodeYtelse(YtelseType type, TemaUnderkategori tuk, PeriodeYtelse periodeYtelse) {
        super(type, tuk, periodeYtelse);
        if (periodeYtelse.getArbeidskategori() == null) {
            this.arbeidskategori = Arbeidskategori.UGYLDIG;
        } else {
            var kategori = periodeYtelse.getArbeidskategori().getValue();
            if (ARBEIDSKATEGORI_MAP.get(kategori) == null) {
                log.warn("Infotrygd ga ukjent kode for arbeidskategori {}", kategori);
            }
            this.arbeidskategori = ARBEIDSKATEGORI_MAP.getOrDefault(kategori, Arbeidskategori.UGYLDIG);
        }
        lagArbeidsforholdListe(periodeYtelse.getArbeidsforholdListe());
    }

    private void lagArbeidsforholdListe(List<Arbeidsforhold> arbeidsforholdListe) {
        ArrayList<YtelseBeregningsgrunnlagArbeidsforhold> afh = new ArrayList<>();
        for (Arbeidsforhold arbf : arbeidsforholdListe) {
            afh.add(new YtelseBeregningsgrunnlagArbeidsforhold(arbf));
        }
        arbeidsforhold = Collections.unmodifiableList(afh);
    }

    @Override
    public List<YtelseBeregningsgrunnlagArbeidsforhold> getArbeidsforhold() {
        return arbeidsforhold;
    }

    @Override
    public boolean harArbeidsForhold() {
        return (getArbeidsforhold() != null && !getArbeidsforhold().isEmpty());
    }


    @Override
    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }
}
