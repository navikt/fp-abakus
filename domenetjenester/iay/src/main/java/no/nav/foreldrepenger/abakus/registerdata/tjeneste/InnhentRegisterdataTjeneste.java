package no.nav.foreldrepenger.abakus.registerdata.tjeneste;

import static no.nav.foreldrepenger.abakus.registerdata.callback.CallbackTask.EKSISTERENDE_GRUNNLAG_REF;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.RegisterdataInnhentingTask;
import no.nav.foreldrepenger.abakus.registerdata.callback.CallbackTask;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

@ApplicationScoped
public class InnhentRegisterdataTjeneste {

    private Instance<IAYRegisterInnhentingTjeneste> innhentTjenester;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private ProsessTaskRepository prosessTaskRepository;
    private KodeverkRepository kodeverkRepository;

    InnhentRegisterdataTjeneste() {
        // CDI
    }

    @Inject
    public InnhentRegisterdataTjeneste(@Any Instance<IAYRegisterInnhentingTjeneste> innhentingTjeneste,
                                       InntektArbeidYtelseTjeneste iayTjeneste,
                                       KoblingTjeneste koblingTjeneste,
                                       ProsessTaskRepository prosessTaskRepository,
                                       KodeverkRepository kodeverkRepository) {
        this.innhentTjenester = innhentingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
        this.kodeverkRepository = kodeverkRepository;
    }

    public Optional<GrunnlagReferanse> innhent(InnhentRegisterdataRequest dto) {
        Kobling kobling = oppdaterKobling(dto);

        // Trigg innhenting
        final var innhentingTjeneste = finnInnhenter(mapTilYtelseType(dto));
        InntektArbeidYtelseAggregatBuilder builder = innhentingTjeneste.innhentRegisterdata(kobling);
        iayTjeneste.lagre(kobling.getKoblingReferanse(), builder);

        Optional<InntektArbeidYtelseGrunnlag> grunnlag = iayTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse());
        return grunnlag.map(InntektArbeidYtelseGrunnlag::getGrunnlagReferanse);
    }

    private IAYRegisterInnhentingTjeneste finnInnhenter(YtelseType ytelseType) {
        final var tjenester = innhentTjenester.select(new YtelseTypeRef.FagsakYtelseTypeRefLiteral(ytelseType.getKode()));
        if (tjenester.isAmbiguous() || tjenester.isUnsatisfied()) {
            throw new IllegalArgumentException("Finner ikke IAYRegisterInnhenter. Støtter ikke ytelsetype " + ytelseType);
        }
        return tjenester.get();
    }

    private Kobling oppdaterKobling(InnhentRegisterdataRequest dto) {
        KoblingReferanse referanse = new KoblingReferanse(dto.getReferanse());
        Optional<Kobling> koblingOpt = koblingTjeneste.hentFor(referanse);
        Kobling kobling;
        if (koblingOpt.isEmpty()) {
            // Lagre kobling
            AktørId aktørId = new AktørId(dto.getAktør().getIdent());
            kobling = new Kobling(new Saksnummer(dto.getSaksnummer()), referanse, aktørId);
        } else {
            kobling = koblingOpt.get();
        }

        if (YtelseType.UDEFINERT.equals(kobling.getYtelseType())) {
            var ytelseType = mapTilYtelseType(dto);
            if (ytelseType != null) {
                kobling.setYtelseType(ytelseType);
            }
        }
        // Oppdater kobling
        Aktør annenPartAktør = dto.getAnnenPartAktør();
        if (annenPartAktør != null) {
            kobling.setAnnenPartAktørId(new AktørId(annenPartAktør.getIdent()));
        }
        Periode opplysningsperiode = dto.getOpplysningsperiode();
        if (opplysningsperiode != null) {
            kobling.setOpplysningsperiode(DatoIntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFom(), opplysningsperiode.getTom()));
        }
        Periode opptjeningsperiode = dto.getOpptjeningsperiode();
        if (opptjeningsperiode != null) {
            kobling.setOpptjeningsperiode(DatoIntervallEntitet.fraOgMedTilOgMed(opptjeningsperiode.getFom(), opptjeningsperiode.getTom()));
        }
        // Diff & log endringer
        koblingTjeneste.lagre(kobling);
        return kobling;
    }

    private YtelseType mapTilYtelseType(InnhentRegisterdataRequest dto) {
        return kodeverkRepository.finn(YtelseType.class, dto.getYtelseType().getKode());
    }

    public String triggAsyncInnhent(InnhentRegisterdataRequest dto) {
        Kobling kobling = oppdaterKobling(dto);

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        ProsessTaskData innhentingTask = new ProsessTaskData(RegisterdataInnhentingTask.TASKTYPE);
        ProsessTaskData callbackTask = new ProsessTaskData(CallbackTask.TASKTYPE);
        innhentingTask.setKobling(kobling.getId(), kobling.getAktørId().getId());
        callbackTask.setKobling(kobling.getId(), kobling.getAktørId().getId());

        Optional<GrunnlagReferanse> eksisterendeGrunnlagRef = hentSisteReferanseFor(kobling.getKoblingReferanse());
        eksisterendeGrunnlagRef.ifPresent(ref -> callbackTask.setProperty(EKSISTERENDE_GRUNNLAG_REF, ref.toString()));

        if (dto.getCallbackUrl() != null) {
            innhentingTask.setCallbackUrl(dto.getCallbackUrl());
            callbackTask.setCallbackUrl(dto.getCallbackUrl());
        }
        taskGruppe.addNesteSekvensiell(innhentingTask);
        taskGruppe.addNesteSekvensiell(callbackTask);

        return prosessTaskRepository.lagre(innhentingTask);
    }

    public boolean innhentingFerdig(String taskReferanse) {
        List<TaskStatus> taskStatuses = prosessTaskRepository.finnStatusForGruppe(taskReferanse);
        return taskStatuses.stream().anyMatch(it -> !ProsessTaskStatus.KLAR.equals(it.getStatus()));
    }

    public Optional<GrunnlagReferanse> hentSisteReferanseFor(KoblingReferanse koblingRef) {
        Optional<Kobling> kobling = koblingTjeneste.hentFor(koblingRef);
        if (kobling.isEmpty()) {
            return Optional.empty();
        }
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = iayTjeneste.hentGrunnlagFor(kobling.get().getKoblingReferanse());
        return grunnlag.map(InntektArbeidYtelseGrunnlag::getGrunnlagReferanse);
    }

}
