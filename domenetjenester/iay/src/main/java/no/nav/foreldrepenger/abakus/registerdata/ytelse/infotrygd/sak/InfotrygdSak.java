package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak;

import static no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseTema.ENSLIG_FORSORGER_TEMA;
import static no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseTema.FORELDREPENGER_TEMA;
import static no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseTema.PÅRØRENDE_SYKDOM_TEMA;
import static no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseTema.SYKEPENGER_TEMA;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.FagsystemUnderkategori;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseResultat;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseSakstype;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseTema;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class InfotrygdSak {
    private static final EnumSet<DayOfWeek> HELG = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    private FagsystemUnderkategori fagsystemUnderkategori;
    private Saksnummer saksnummer;
    private RelatertYtelseTema tema;
    private TemaUnderkategori temaUnderkategori;
    private RelatertYtelseStatus status;
    private RelatertYtelseSakstype type;
    private RelatertYtelseResultat resultat;
    private String saksbehandlerId;
    private LocalDate registrert;
    private LocalDate vedtatt;
    private LocalDate iverksatt;
    private LocalDate opphoerFomDato;
    private YtelseType relatertYtelseType;
    private YtelseStatus ytelseStatus;
    private DatoIntervallEntitet periode;

    public InfotrygdSak(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak sak, KodeverkRepository kodeverkRepository) {
        fagsystemUnderkategori = FagsystemUnderkategori.INFOTRYGD_SAK;
        konverterInfotrygdSak(sak, kodeverkRepository);
    }

    public InfotrygdSak(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak vedtak, KodeverkRepository kodeverkRepository) {
        opphoerFomDato = DateUtil.convertToLocalDate(vedtak.getOpphoerFom());
        fagsystemUnderkategori = FagsystemUnderkategori.INFOTRYGD_VEDTAK;
        konverterInfotrygdSak(vedtak, kodeverkRepository);
        if (YtelseType.ENGANGSSTØNAD.equals(relatertYtelseType)) {
            periode = DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFomDato(), periode.getFomDato());
        }
    }

    private void konverterInfotrygdSak(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak sak, KodeverkRepository kodeverkRepository) {
        saksnummer = Saksnummer.infotrygd(sak.getSakId());

        registrert = DateUtil.convertToLocalDate(sak.getRegistrert());
        vedtatt = DateUtil.convertToLocalDate(sak.getVedtatt());
        iverksatt = DateUtil.convertToLocalDate(sak.getIverksatt());
        periode = utledPeriode(iverksatt, opphoerFomDato, vedtatt, registrert);
        saksbehandlerId = sak.getSaksbehandlerId();
        if (sak.getTema() != null) {
            tema = kodeverkRepository.finn(RelatertYtelseTema.class, sak.getTema().getValue());
        }
        if (sak.getBehandlingstema() != null && sak.getBehandlingstema().getValue() != null) {
            temaUnderkategori = kodeverkRepository.finnForKodeverkEiersKode(TemaUnderkategori.class, sak.getBehandlingstema().getValue(), TemaUnderkategori.UDEFINERT);
        } else {
            temaUnderkategori = TemaUnderkategori.UDEFINERT;
        }
        if (sak.getType() != null && sak.getType().getValue() != null) {
            type = kodeverkRepository.finnForKodeverkEiersKode(RelatertYtelseSakstype.class, sak.getType().getValue(), RelatertYtelseSakstype.UDEFINERT);
        } else {
            type = RelatertYtelseSakstype.UDEFINERT;
        }
        if (sak.getStatus() != null && sak.getStatus().getValue() != null) {
            status = kodeverkRepository.finnForKodeverkEiersKode(RelatertYtelseStatus.class, sak.getStatus().getValue(), RelatertYtelseStatus.AVSLUTTET_IT);
        }
        if (sak.getResultat() != null && sak.getResultat().getValue() != null) {
            resultat = kodeverkRepository.finnForKodeverkEiersKode(RelatertYtelseResultat.class, sak.getResultat().getValue(), RelatertYtelseResultat.UDEFINERT);
        } else {
            resultat = RelatertYtelseResultat.UDEFINERT;
        }
        relatertYtelseType = utledRelatertYtelseType(tema, getTemaUnderkategoriString());
        ytelseStatus = getYteleseTilstand();
    }

    private DatoIntervallEntitet utledPeriode(LocalDate iverksatt, LocalDate opphoerFomDato, LocalDate vedtatt, LocalDate registrert) {
        if (opphoerFomDato != null) {
            LocalDate tomFraOpphørFom = localDateMinus1Virkedag(opphoerFomDato);
            if (tomFraOpphørFom.isAfter(iverksatt)) {
                return DatoIntervallEntitet.fraOgMedTilOgMed(iverksatt, tomFraOpphørFom);
            } else {
                return DatoIntervallEntitet.fraOgMedTilOgMed(iverksatt, iverksatt);
            }
        } else {
            if (iverksatt != null) {
                return DatoIntervallEntitet.fraOgMed(iverksatt);
            } else if (vedtatt != null) {
                return DatoIntervallEntitet.fraOgMed(vedtatt);
            }
            return DatoIntervallEntitet.fraOgMed(registrert);
        }
    }

    private LocalDate localDateMinus1Virkedag(LocalDate opphoerFomDato) {
        int antallDager = beregnAntallDagerTilForrigeVirkedag(opphoerFomDato);
        return opphoerFomDato.minusDays(antallDager);
    }

    private int beregnAntallDagerTilForrigeVirkedag(LocalDate opphoerDato) {
        int antallDagerTilbake = 1;
        LocalDate dato = opphoerDato.minusDays(1);
        while (HELG.contains(dato.getDayOfWeek())) {
            if (HELG.contains(dato.getDayOfWeek())) {
                antallDagerTilbake++;
                dato = dato.minusDays(1);
            }
        }
        return antallDagerTilbake;
    }

    public FagsystemUnderkategori getFagsystemUnderkategori() {
        return fagsystemUnderkategori;
    }

    public Saksnummer getSakId() {
        return saksnummer;
    }

    public RelatertYtelseTema getTema() {
        return tema;
    }

    public TemaUnderkategori getTemaUnderkategori() {
        return temaUnderkategori;
    }

    public RelatertYtelseStatus getStatus() {
        return status;
    }

    public LocalDate getRegistrert() {
        return registrert;
    }

    public LocalDate getVedtatt() {
        return vedtatt;
    }

    public LocalDate getIverksatt() {
        return iverksatt;
    }

    public LocalDate getOpphoerFomDato() {
        return opphoerFomDato;
    }

    public RelatertYtelseSakstype getSaksType() {
        return type;
    }

    public YtelseStatus getYtelseStatus() {
        return ytelseStatus;
    }

    public RelatertYtelseResultat getResultat() {
        return resultat;
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public YtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    private String getTemaUnderkategoriString() {
        if (temaUnderkategori == null) {
            return null;
        } else {
            return temaUnderkategori.getKode();
        }
    }

    private String getStatusString() {
        if (status == null) {
            return null;
        } else {
            return status.getKode();
        }

    }

    public YtelseType hentRelatertYtelseTypeForSammenstillingMedBeregningsgrunnlag() {
        if (relatertYtelseType != null && (relatertYtelseType.equals(YtelseType.SVANGERSKAPSPENGER))) {
            return YtelseType.FORELDREPENGER;
        }
        return relatertYtelseType;
    }

    private YtelseType utledRelatertYtelseType(RelatertYtelseTema ytelseTema, String behandlingsTema) {
        if (ENSLIG_FORSORGER_TEMA.equals(ytelseTema)) {
            return YtelseType.ENSLIG_FORSØRGER;
        } else if (FORELDREPENGER_TEMA.equals(ytelseTema)) {
            if (TemaUnderkategori.erGjelderSvangerskapspenger(behandlingsTema)) {
                return YtelseType.SVANGERSKAPSPENGER;
            } else if (TemaUnderkategori.erGjelderForeldrepenger(behandlingsTema)) {
                return YtelseType.FORELDREPENGER;
            } else if (TemaUnderkategori.erGjelderEngangsstonad(behandlingsTema)) {
                return YtelseType.ENGANGSSTØNAD;
            }
        } else if (SYKEPENGER_TEMA.equals(ytelseTema)) {
            return YtelseType.SYKEPENGER;
        } else if (PÅRØRENDE_SYKDOM_TEMA.equals(ytelseTema)) {
            return YtelseType.PÅRØRENDESYKDOM;
        }
        return YtelseType.UDEFINERT;
    }

    public boolean erAvRelatertYtelseType(YtelseType... ytelseTyper) {
        if (relatertYtelseType == null) return false;
        for (YtelseType relatertYtelse : ytelseTyper) {
            if (relatertYtelse.equals(relatertYtelseType)) return true;
        }
        return false;
    }

    public boolean erLøpendeVedtak() {
        return FagsystemUnderkategori.INFOTRYGD_VEDTAK.equals(fagsystemUnderkategori) && RelatertYtelseStatus.erLøpendeVedtak(getStatusString());
    }

    public boolean erAvsluttetVedtak() {
        return FagsystemUnderkategori.INFOTRYGD_VEDTAK.equals(fagsystemUnderkategori) && YtelseStatus.AVSLUTTET.equals(ytelseStatus);
    }

    public boolean erVedtak() {
        return FagsystemUnderkategori.INFOTRYGD_VEDTAK.equals(fagsystemUnderkategori);
    }

    public boolean erÅpenSak() {
        return FagsystemUnderkategori.INFOTRYGD_SAK.equals(fagsystemUnderkategori) && RelatertYtelseStatus.erÅpenSakStatus(getStatusString());
    }

    private boolean erIkkeStartet() {
        return RelatertYtelseStatus.erIkkeStartetStatus(getStatusString());
    }

    public String getSaksbehandlerId() {
        return saksbehandlerId;
    }


    private YtelseStatus getYteleseTilstand() {
        if (erLøpendeVedtak()) {
            return YtelseStatus.LØPENDE;
        } else if (erÅpenSak()) {
            return YtelseStatus.UNDER_BEHANDLING;
        } else if (erIkkeStartet()) {
            return YtelseStatus.OPPRETTET;
        }
        return YtelseStatus.AVSLUTTET;
    }

}
