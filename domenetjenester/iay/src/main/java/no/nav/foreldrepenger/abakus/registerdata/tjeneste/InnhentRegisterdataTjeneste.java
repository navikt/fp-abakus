package no.nav.foreldrepenger.abakus.registerdata.tjeneste;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto.Aktør;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto.InnhentRegisterdataDto;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto.PeriodeDto;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class InnhentRegisterdataTjeneste {

    private IAYRegisterInnhentingTjeneste registerInnhentingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;

    InnhentRegisterdataTjeneste() {
        // CDI
    }

    @Inject
    public InnhentRegisterdataTjeneste(@FagsakYtelseTypeRef("FP") IAYRegisterInnhentingTjeneste registerInnhentingTjeneste,
                                       InntektArbeidYtelseTjeneste iayTjeneste,
                                       KoblingTjeneste koblingTjeneste) {
        this.registerInnhentingTjeneste = registerInnhentingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
    }

    public void innhent(InnhentRegisterdataDto dto) {
        Optional<Kobling> koblingOpt = koblingTjeneste.hentFor(dto.getReferanse());
        Kobling kobling;
        if (!koblingOpt.isPresent()) {
            // Lagre kobling
            PeriodeDto opplysningsperiode = dto.getOpplysningsperiode();
            kobling = new Kobling(dto.getReferanse(), new AktørId(dto.getAktørId().getId()), DatoIntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFom(), opplysningsperiode.getTom()));
        } else {
            kobling = koblingOpt.get();
        }
        // Oppdater kobling
        Aktør annenPartAktørId = dto.getAnnenPartAktørId();
        if (annenPartAktørId != null) {
            kobling.setAnnenPartAktørId(new AktørId(annenPartAktørId.getId()));
        }
        PeriodeDto opptjeningsperiode = dto.getOpptjeningsperiode();
        if (opptjeningsperiode != null) {
            kobling.setOpptjeningsperiode(DatoIntervallEntitet.fraOgMedTilOgMed(opptjeningsperiode.getFom(), opptjeningsperiode.getTom()));
        }
        // Diff & log endringer
        koblingTjeneste.lagre(kobling);

        // Trigg innhenting
        InntektArbeidYtelseAggregatBuilder builder = registerInnhentingTjeneste.innhentRegisterdata(kobling);
        iayTjeneste.lagre(kobling.getId(), builder);
    }
}
