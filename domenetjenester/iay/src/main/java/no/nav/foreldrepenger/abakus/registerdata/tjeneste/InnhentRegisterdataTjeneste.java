package no.nav.foreldrepenger.abakus.registerdata.tjeneste;

import static no.nav.foreldrepenger.abakus.registerdata.callback.CallbackTask.EKSISTERENDE_GRUNNLAG_REF;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.abakus.iaygrunnlag.request.RegisterdataType;
import no.nav.abakus.prosesstask.batch.BatchProsessTaskRepository;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.TaskConstants;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.RegisterdataInnhentingTask;
import no.nav.foreldrepenger.abakus.registerdata.callback.CallbackTask;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.json.JacksonJsonConfig;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

@ApplicationScoped
public class InnhentRegisterdataTjeneste {

    private static final Map<RegisterdataType, RegisterdataElement> registerdataMapping = initMapping();
    private Instance<IAYRegisterInnhentingTjeneste> innhentTjenester;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private BatchProsessTaskRepository prosessTaskRepository;

    InnhentRegisterdataTjeneste() {
        // CDI
    }

    @Inject
    public InnhentRegisterdataTjeneste(@Any Instance<IAYRegisterInnhentingTjeneste> innhentingTjeneste,
                                       InntektArbeidYtelseTjeneste iayTjeneste,
                                       KoblingTjeneste koblingTjeneste,
                                       BatchProsessTaskRepository prosessTaskRepository) {
        this.innhentTjenester = innhentingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    private static Map<RegisterdataType, RegisterdataElement> initMapping() {
        return Map.of(RegisterdataType.ARBEIDSFORHOLD, RegisterdataElement.ARBEIDSFORHOLD,
            RegisterdataType.YTELSE, RegisterdataElement.YTELSE,
            RegisterdataType.LIGNET_NÆRING, RegisterdataElement.LIGNET_NÆRING,
            RegisterdataType.INNTEKT_PENSJONSGIVENDE, RegisterdataElement.INNTEKT_PENSJONSGIVENDE,
            RegisterdataType.INNTEKT_BEREGNINGSGRUNNLAG, RegisterdataElement.INNTEKT_BEREGNINGSGRUNNLAG,
            RegisterdataType.INNTEKT_SAMMENLIGNINGSGRUNNLAG, RegisterdataElement.INNTEKT_SAMMENLIGNINGSGRUNNLAG);
    }

    public static Set<RegisterdataElement> hentUtInformasjonsElementer(InnhentRegisterdataRequest dto) {
        final var elementer = dto.getElementer();

        if (elementer == null || elementer.isEmpty()) {
            return Set.of();
        }

        return elementer.stream()
            .map(registerdataMapping::get)
            .collect(Collectors.toSet());
    }

    public Optional<GrunnlagReferanse> innhent(InnhentRegisterdataRequest dto) {
        Kobling kobling = oppdaterKobling(dto);

        // Trigg innhenting
        final var innhentingTjeneste = finnInnhenter(mapTilYtelseType(dto));
        var informasjonsElementer = hentUtInformasjonsElementer(dto);
        InntektArbeidYtelseGrunnlagBuilder builder = innhentingTjeneste.innhentRegisterdata(kobling, informasjonsElementer);
        iayTjeneste.lagre(kobling.getKoblingReferanse(), builder);

        Optional<InntektArbeidYtelseGrunnlag> grunnlag = iayTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse());
        return grunnlag.map(InntektArbeidYtelseGrunnlag::getGrunnlagReferanse);
    }

    private IAYRegisterInnhentingTjeneste finnInnhenter(YtelseType ytelseType) {
        return YtelseTypeRef.Lookup.find(innhentTjenester, ytelseType).get();
    }

    private Kobling oppdaterKobling(InnhentRegisterdataRequest dto) {
        KoblingReferanse referanse = new KoblingReferanse(dto.getReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(referanse)); // kan bli null hvis gjelder ny
        Optional<Kobling> koblingOpt = koblingTjeneste.hentFor(referanse);
        Kobling kobling;
        if (koblingOpt.isEmpty()) {
            // Lagre kobling
            AktørId aktørId = new AktørId(dto.getAktør().getIdent());
            kobling = new Kobling(dto.getYtelseType(), new Saksnummer(dto.getSaksnummer()), referanse, aktørId);
        } else {
            kobling = koblingOpt.get();
            if (YtelseType.UDEFINERT.equals(kobling.getYtelseType())) {
                var ytelseType = mapTilYtelseType(dto);
                if (ytelseType != null) {
                    kobling.setYtelseType(ytelseType);
                }
            }
        }

        // Oppdater kobling
        Periode opplysningsperiode = dto.getOpplysningsperiode();
        if (opplysningsperiode != null) {
            kobling.setOpplysningsperiode(IntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFom(), opplysningsperiode.getTom()));
        }
        Periode opptjeningsperiode = dto.getOpptjeningsperiode();
        if (opptjeningsperiode != null) {
            kobling.setOpptjeningsperiode(IntervallEntitet.fraOgMedTilOgMed(opptjeningsperiode.getFom(), opptjeningsperiode.getTom()));
        }
        // Diff & log endringer
        koblingTjeneste.lagre(kobling);
        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));
        
        return kobling;
    }

    private YtelseType mapTilYtelseType(InnhentRegisterdataRequest dto) {
        return YtelseType.fraKode(dto.getYtelseType().getKode());
    }

    public String triggAsyncInnhent(InnhentRegisterdataRequest dto) {
        Kobling kobling = oppdaterKobling(dto);

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        ProsessTaskData innhentingTask = new ProsessTaskData(RegisterdataInnhentingTask.TASKTYPE);
        ProsessTaskData callbackTask = new ProsessTaskData(CallbackTask.TASKTYPE);
        innhentingTask.setAktørId(kobling.getAktørId().getId());
        innhentingTask.setProperty(TaskConstants.KOBLING_ID, kobling.getId().toString());
        try {
            innhentingTask.setPayload(JacksonJsonConfig.getMapper().writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Feil i serialisering av innhentingrequest", e);
        }
        callbackTask.setAktørId(kobling.getAktørId().getId());
        callbackTask.setProperty(TaskConstants.KOBLING_ID, kobling.getId().toString());

        Optional<GrunnlagReferanse> eksisterendeGrunnlagRef = hentSisteReferanseFor(kobling.getKoblingReferanse());
        eksisterendeGrunnlagRef.map(GrunnlagReferanse::getReferanse).ifPresent(ref -> callbackTask.setProperty(EKSISTERENDE_GRUNNLAG_REF, ref.toString()));

        if (dto.getCallbackUrl() != null) {
            innhentingTask.setProperty(TaskConstants.CALLBACK_URL, dto.getCallbackUrl());
            callbackTask.setProperty(TaskConstants.CALLBACK_URL, dto.getCallbackUrl());
        }
        taskGruppe.addNesteSekvensiell(innhentingTask);
        taskGruppe.addNesteSekvensiell(callbackTask);
        taskGruppe.setCallIdFraEksisterende();

        return prosessTaskRepository.lagre(taskGruppe);
    }

    public boolean innhentingFerdig(String taskReferanse) {
        final var statuses = prosessTaskRepository.finnStatusForGruppe(taskReferanse);
        return statuses.stream().anyMatch(it -> !ProsessTaskStatus.KLAR.equals(it.getStatus()));
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
