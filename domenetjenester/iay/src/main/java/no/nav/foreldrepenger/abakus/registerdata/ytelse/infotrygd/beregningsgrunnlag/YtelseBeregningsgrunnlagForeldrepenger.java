package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Foreldrepenger;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class YtelseBeregningsgrunnlagForeldrepenger extends YtelseBeregningsgrunnlagPeriodeYtelse {

    private final LocalDate foedselsdatoBarn;
    private final LocalDate opprinneligIdentdato;
    private BigDecimal dekningsgrad;
    private BigDecimal gradering;

    YtelseBeregningsgrunnlagForeldrepenger(Foreldrepenger foreldrepenger, TemaUnderkategori tuk) {
        super(YtelseType.FORELDREPENGER, tuk, foreldrepenger);
        if (foreldrepenger.getDekningsgrad() != null) {
            dekningsgrad = new BigDecimal(foreldrepenger.getDekningsgrad());
        }
        if (foreldrepenger.getGradering() != null) {
            gradering = new BigDecimal(foreldrepenger.getGradering());
        }
        foedselsdatoBarn = DateUtil.convertToLocalDate(foreldrepenger.getFoedselsdatoBarn());
        opprinneligIdentdato = DateUtil.convertToLocalDate(foreldrepenger.getOpprinneligIdentdato());
    }

    public BigDecimal getDekningsgrad() {
        return dekningsgrad;
    }

    public BigDecimal getGradering() {
        return gradering;
    }

    public LocalDate getFoedselsdatoBarn() {
        return foedselsdatoBarn;
    }

    public LocalDate getOpprinneligIdentdato() {
        return opprinneligIdentdato;
    }

    @Override
    public void mapSpesialverdier(YtelseGrunnlagBuilder builder) {
        builder.medDekningsgradProsent(dekningsgrad);
        builder.medGraderingProsent(gradering);
        builder.medOpprinneligIdentdato(opprinneligIdentdato);
    }
}
