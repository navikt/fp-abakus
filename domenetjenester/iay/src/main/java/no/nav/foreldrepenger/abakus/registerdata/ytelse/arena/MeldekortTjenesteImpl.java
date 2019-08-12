package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseTema;
import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagConsumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MeldekortTjenesteImpl implements MeldekortTjeneste {
    private static final String TJENESTE = "MeldekortUtbetalingsgrunnlag (Arena)";

    // SAK/status fra tjenesten (value/termnavn): AVSLU/Avsluttet, AKTIV/Aktiv, INAKT/Inaktiv - mulig ?/Lukket
    // VEDTAK/status fra tjenesten (value/termnavn): AVSLU/Avsluttet, INNST/Innstilt, IVERK/Iverksatt, REGIS/Registrert, OPPRE/Opprettet, MOTAT/Mottatt

    private MeldekortUtbetalingsgrunnlagConsumer meldekortUtbetalingsgrunnlagConsumer;
    private ObjectFactory objectFactory = new ObjectFactory();

    MeldekortTjenesteImpl() {
        // CDI
    }

    @Inject
    public MeldekortTjenesteImpl(MeldekortUtbetalingsgrunnlagConsumer meldekortUtbetalingsgrunnlagConsumer) {
        this.meldekortUtbetalingsgrunnlagConsumer = meldekortUtbetalingsgrunnlagConsumer;
    }

    private LocalDate oversettDatoNullable(XMLGregorianCalendar datoXML) {
        if (datoXML == null) {
            return null;
        }
        return datoXML.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }

    private YtelseType oversettType(Sak sak) {
        if (RelatertYtelseTema.AAP.getKode().equals(sak.getTema().getValue())) {
            return YtelseType.ARBEIDSAVKLARINGSPENGER;
        } else if (RelatertYtelseTema.DAG.getKode().equals(sak.getTema().getValue())) {
            return YtelseType.DAGPENGER;
        } else {
            return YtelseType.UDEFINERT;
        }
    }

    private YtelseStatus oversettTilstand(Sak sak, Vedtak vedtak) {
        if (RelatertYtelseStatus.AVSLU.getKode().equals(vedtak.getVedtaksstatus().getValue())) {
            return YtelseStatus.AVSLUTTET;
        } else if (RelatertYtelseStatus.IVERK.getKode().equals(vedtak.getVedtaksstatus().getValue())) {
            return YtelseStatus.LØPENDE;
        } else if (RelatertYtelseStatus.AVSLU.getKode().equals(sak.getSaksstatus().getValue()) || "INAKT".equals(sak.getSaksstatus().getValue())) {
            return YtelseStatus.AVSLUTTET;
        } else {
            return YtelseStatus.UNDER_BEHANDLING;
        }
    }

    private YtelseStatus oversettTilstandUtenVedtak(Sak sak) {
        if (RelatertYtelseStatus.AVSLU.getKode().equals(sak.getSaksstatus().getValue())) {
            return YtelseStatus.AVSLUTTET;
        } else {
            return YtelseStatus.UNDER_BEHANDLING;
        }
    }

    private MeldekortUtbetalingsgrunnlagMeldekort oversettArenaMeldekort(Meldekort meldekort) {
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(oversettDatoNullable(meldekort.getMeldekortperiode().getFom()))
            .medMeldekortTom(oversettDatoNullable(meldekort.getMeldekortperiode().getTom()))
            .medDagsats(BigDecimal.valueOf(meldekort.getDagsats()))
            .medBeløp(BigDecimal.valueOf(meldekort.getBeloep()))
            .medUtbetalingsgrad(BigDecimal.valueOf(meldekort.getUtbetalingsgrad()))
            .build();
    }

    private MeldekortUtbetalingsgrunnlagSak oversettArenaVedtakMeldekort(Sak sak, Vedtak vedtak) {
        List<MeldekortUtbetalingsgrunnlagMeldekort> meldekortList = new ArrayList<>();
        for (Meldekort meldekort : vedtak.getMeldekortListe()) {
            meldekortList.add(oversettArenaMeldekort(meldekort));
        }
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .medType(oversettType(sak))
            .medTilstand(oversettTilstand(sak, vedtak))
            .medKilde(Fagsystem.ARENA)
            .medKravMottattDato(oversettDatoNullable(vedtak.getDatoKravMottatt()))
            .medSaksnummer(new Saksnummer(sak.getFagsystemSakId()))
            .medSakStatus(sak.getSaksstatus().getValue())
            .medVedtakStatus(vedtak.getVedtaksstatus().getValue())
            .medVedtattDato(oversettDatoNullable(vedtak.getVedtaksdato()))
            .medVedtaksPeriodeFom(oversettDatoNullable(vedtak.getVedtaksperiode().getFom()))
            .medVedtaksPeriodeTom(oversettDatoNullable(vedtak.getVedtaksperiode().getTom()))
            .medVedtaksDagsats(BigDecimal.valueOf(vedtak.getDagsats()))
            .leggTilMeldekort(meldekortList)
            .build();

    }

    private MeldekortUtbetalingsgrunnlagSak oversettArenaUtenVedtak(Sak sak) {
        MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder sakBuilder = MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .medType(oversettType(sak))
            .medTilstand(oversettTilstandUtenVedtak(sak))
            .medKilde(Fagsystem.ARENA)
            .medSaksnummer(new Saksnummer(sak.getFagsystemSakId()))
            .medSakStatus(sak.getSaksstatus().getValue())
            .medKravMottattDato(null)
            .medVedtakStatus(null);
        return sakBuilder.build();
    }

    private List<MeldekortUtbetalingsgrunnlagSak> oversettArenaSak(Sak sak) {
        List<MeldekortUtbetalingsgrunnlagSak> vedtakene = new ArrayList<>();
        if (sak.getVedtakListe().isEmpty()) {
            vedtakene.add(oversettArenaUtenVedtak(sak));
        }
        for (Vedtak vedtak : sak.getVedtakListe()) {
            vedtakene.add(oversettArenaVedtakMeldekort(sak, vedtak));
        }
        return vedtakene;
    }

    @Override
    public List<MeldekortUtbetalingsgrunnlagSak> hentMeldekortListe(AktørId aktørId, LocalDate fom, LocalDate tom) {
        try {
            FinnMeldekortUtbetalingsgrunnlagListeRequest request = new FinnMeldekortUtbetalingsgrunnlagListeRequest();

            AktoerId aktoer = objectFactory.createAktoerId();
            aktoer.setAktoerId(aktørId.getId());
            request.setIdent(aktoer);
            Periode periode = objectFactory.createPeriode();
            periode.setFom(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(fom));
            if (tom != null) {
                periode.setTom(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(tom));
            }
            request.setPeriode(periode);

            Tema aapTema = objectFactory.createTema();
            aapTema.setValue(RelatertYtelseTema.AAP.getKode());
            request.getTemaListe().add(aapTema);
            Tema dagTema = objectFactory.createTema();
            dagTema.setValue(RelatertYtelseTema.DAG.getKode());
            request.getTemaListe().add(dagTema);

            FinnMeldekortUtbetalingsgrunnlagListeResponse response = meldekortUtbetalingsgrunnlagConsumer.finnMeldekortUtbetalingsgrunnlagListe(request);

            List<MeldekortUtbetalingsgrunnlagSak> saker = new ArrayList<>();
            if (response != null && !response.getMeldekortUtbetalingsgrunnlagListe().isEmpty()) {
                for (Sak sak : response.getMeldekortUtbetalingsgrunnlagListe()) {
                    saker.addAll(oversettArenaSak(sak));
                }
            }

            return saker;
        } catch (FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning e) {
            throw MeldekortFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        } catch (FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput e) {
            throw MeldekortFeil.FACTORY.tjenesteUgyldigInput(TJENESTE, e).toException();
        } catch (FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet e) {
            throw MeldekortFeil.FACTORY.fantIkkePersonForAktorId(TJENESTE, e).toException();
        }
    }
}
