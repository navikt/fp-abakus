package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PaaroerendeSykdom;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class YtelseBeregningsgrunnlagPårørendeSykdom extends YtelseBeregningsgrunnlagPeriodeYtelse {

    private final LocalDate foedselsdatoPleietrengende;

    YtelseBeregningsgrunnlagPårørendeSykdom(PaaroerendeSykdom paaroerendeSykdom, TemaUnderkategori tuk, KodeverkRepository kodeverkRepository) {
        super(YtelseType.PÅRØRENDESYKDOM, tuk, paaroerendeSykdom, kodeverkRepository);
        foedselsdatoPleietrengende = DateUtil.convertToLocalDate(paaroerendeSykdom.getFoedselsdatoPleietrengende());
    }

    public LocalDate getFoedselsdatoPleietrengende() {
        return foedselsdatoPleietrengende;
    }

    @Override
    public void mapSpesialverdier(YtelseGrunnlagBuilder builder) {
        //Ingen spesialhåndtering
    }
}
