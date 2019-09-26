package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak;

import java.util.Optional;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlag;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class InfotrygdSakOgGrunnlag {
    private InfotrygdSak sak;
    private YtelseBeregningsgrunnlag grunnlag;
    private DatoIntervallEntitet periode;

    public InfotrygdSakOgGrunnlag(InfotrygdSak sak) {
        this.sak = sak;
        this.periode = sak.getPeriode();
    }

    public InfotrygdSak getSak() {
        return sak;
    }

    public Optional<YtelseBeregningsgrunnlag> getGrunnlag() {
        return Optional.ofNullable(grunnlag);
    }

    public void setGrunnlag(YtelseBeregningsgrunnlag grunnlag) {
        this.grunnlag = grunnlag;
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

}
