package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Engangsstoenad;

public class YtelseBeregningsgrunnlagEngangstoenad extends YtelseBeregningsgrunnlagGrunnlag {

    YtelseBeregningsgrunnlagEngangstoenad(Engangsstoenad engangsstoenad) {
        super(YtelseType.ENGANGSSTØNAD, engangsstoenad);
    }

    @Override
    public void mapSpesialverdier(YtelseGrunnlagBuilder builder) {
        //Ingen spesialhåndtering
    }

    @Override
    public boolean harArbeidsForhold() {
        return false;
    }

    @Override
    public List<YtelseBeregningsgrunnlagArbeidsforhold> getArbeidsforhold() {
        return new ArrayList<>();
    }

    @Override
    public Arbeidskategori getArbeidskategori() {
        return Arbeidskategori.UDEFINERT;
    }
}
