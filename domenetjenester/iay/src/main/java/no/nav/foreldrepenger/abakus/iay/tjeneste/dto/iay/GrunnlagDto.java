package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.List;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.ReferanseDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.arbeid.ArbeidDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.inntekt.InntekterDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ytelse.YtelserDto;

public class GrunnlagDto {

    private ReferanseDto referanse;
    private List<ArbeidDto> arbeid;
    private List<InntekterDto> inntekt;
    private List<YtelserDto> ytelse;

    public GrunnlagDto() {
    }

    public ReferanseDto getReferanse() {
        return referanse;
    }

    public void setReferanse(ReferanseDto referanse) {
        this.referanse = referanse;
    }

    public List<ArbeidDto> getArbeid() {
        return arbeid;
    }

    public void setArbeid(List<ArbeidDto> arbeid) {
        this.arbeid = arbeid;
    }

    public List<InntekterDto> getInntekt() {
        return inntekt;
    }

    public void setInntekt(List<InntekterDto> inntekt) {
        this.inntekt = inntekt;
    }

    public List<YtelserDto> getYtelse() {
        return ytelse;
    }

    public void setYtelse(List<YtelserDto> ytelse) {
        this.ytelse = ytelse;
    }
}
