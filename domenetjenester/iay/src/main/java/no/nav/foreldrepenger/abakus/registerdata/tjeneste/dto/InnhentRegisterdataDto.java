package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class InnhentRegisterdataDto implements AbacDto {

    private static final String URL_PATTERN = "/^(?:(?:https?|ftp):\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\." +
        "\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})" +
        "(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d" +
        "|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:" +
        "[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$/i\n";

    @Valid
    @NotNull
    private ReferanseDto referanse;
    @Valid
    @NotNull
    private YtelseTypeKodeverk ytelseType;
    @NotNull
    @Valid
    private Aktør aktørId;
    @Valid
    private Aktør annenPartAktørId;
    @NotNull
    @Valid
    private PeriodeDto opplysningsperiode;
    @Valid
    private PeriodeDto opptjeningsperiode;

    @Valid
    @Pattern(regexp = URL_PATTERN)
    private String callbackUrl;

    public InnhentRegisterdataDto() {
    }

    public String getReferanse() {
        return referanse.getReferanse();
    }

    public void setReferanse(ReferanseDto referanse) {
        this.referanse = referanse;
    }

    public Aktør getAktørId() {
        return aktørId;
    }

    public void setAktørId(Aktør aktørId) {
        this.aktørId = aktørId;
    }

    public Aktør getAnnenPartAktørId() {
        return annenPartAktørId;
    }

    public void setAnnenPartAktørId(Aktør annenPartAktørId) {
        this.annenPartAktørId = annenPartAktørId;
    }

    public PeriodeDto getOpplysningsperiode() {
        return opplysningsperiode;
    }

    public void setOpplysningsperiode(PeriodeDto opplysningsperiode) {
        this.opplysningsperiode = opplysningsperiode;
    }

    public PeriodeDto getOpptjeningsperiode() {
        return opptjeningsperiode;
    }

    public void setOpptjeningsperiode(PeriodeDto opptjeningsperiode) {
        this.opptjeningsperiode = opptjeningsperiode;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public YtelseTypeKodeverk getYtelseType() {
        return ytelseType;
    }

    public void setYtelseType(YtelseTypeKodeverk ytelseType) {
        this.ytelseType = ytelseType;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter opprett = AbacDataAttributter.opprett();
        if (annenPartAktørId != null) {
            opprett.leggTil(annenPartAktørId.abacAttributter());
        }
        return opprett.leggTil(aktørId.abacAttributter());
    }
}
