package no.nav.foreldrepenger.abakus.vedtak.extract.v1;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.vedtak.ytelse.Kildesystem;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.Status;
import no.nav.abakus.vedtak.ytelse.Ytelser;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.AnvistAndel;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Inntektklasse;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.domene.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseAndelBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.YtelseAnvistBuilder;
import no.nav.foreldrepenger.abakus.vedtak.extract.ExtractFromYtelse;

@ApplicationScoped
public class ExtractFromYtelseV1 implements ExtractFromYtelse<YtelseV1> {

    private VedtakYtelseRepository repository;

    ExtractFromYtelseV1() {
    }

    @Inject
    public ExtractFromYtelseV1(VedtakYtelseRepository repository) {
        this.repository = repository;
    }

    @Override
    public VedtakYtelseBuilder extractFrom(YtelseV1 ytelse) {
        Fagsystem fagsystem = Optional.ofNullable(ytelse.getKildesystem()).map(this::mapKildesystem).orElseGet(ytelse::getFagsystem);
        no.nav.abakus.iaygrunnlag.kodeverk.YtelseType ytelseType = getYtelseType(ytelse.getType(), ytelse.getYtelse());
        Saksnummer saksnummer = new Saksnummer(ytelse.getSaksnummer());
        AktørId aktørId = new AktørId(ytelse.getAktør().getVerdi());

        VedtakYtelseBuilder builder = repository.opprettBuilderFor(aktørId, saksnummer, fagsystem, ytelseType);
        builder.medAktør(aktørId)
            .medVedtakReferanse(UUID.fromString(ytelse.getVedtakReferanse()))
            .medVedtattTidspunkt(ytelse.getVedtattTidspunkt())
            .medSaksnummer(saksnummer)
            .medKilde(fagsystem)
            .medYtelseType(ytelseType)
            .medPeriode(mapTilEntitet(ytelse.getPeriode()))
            .medStatus(mapStatus(ytelse.getStatus(), ytelse.getYtelseStatus()))
            .medTilleggsopplysninger(ytelse.getTilleggsopplysninger())
            .tilbakestillAnvisteYtelser();

        ytelse.getAnvist().forEach(anv -> mapAnvisning(builder, anv));

        return builder;
    }

    private void mapAnvisning(VedtakYtelseBuilder builder, Anvisning anv) {
        YtelseAnvistBuilder anvistBuilder = builder.getAnvistBuilder();
        anvistBuilder.medAnvistPeriode(mapTilEntitet(anv.getPeriode()))
            .medBeløp(anv.getBeløp() != null ? anv.getBeløp().getVerdi() : null)
            .medDagsats(anv.getDagsats() != null ? anv.getDagsats().getVerdi() : null)
            .medUtbetalingsgradProsent(anv.getUtbetalingsgrad() != null ? anv.getUtbetalingsgrad().getVerdi() : null);
        anv.getAndeler().stream().map(this::mapFordeling).forEach(anvistBuilder::leggTilFordeling);
        builder.leggTil(anvistBuilder);
    }

    private VedtakYtelseAndelBuilder mapFordeling(AnvistAndel andel) {
        return VedtakYtelseAndelBuilder.ny()
            .medInntektskategori(mapInntektsklasse(andel.getInntektskategori(), andel.getInntektklasse()))
            .medDagsats(andel.getDagsats().getVerdi())
            .medUtbetalingsgrad(andel.getUtbetalingsgrad().getVerdi())
            .medRefusjonsgrad(andel.getRefusjonsgrad().getVerdi())
            .medArbeidsgiver(mapArbeidsgiver(andel.getArbeidsgiver()))
            .medArbeidsforholdId(andel.getArbeidsforholdId());
    }

    private Fagsystem mapKildesystem(Kildesystem kildesystem) {
        return switch (kildesystem) {
            case FPSAK -> Fagsystem.FPSAK;
            case K9SAK -> Fagsystem.K9SAK;
        };
    }

    private YtelseStatus mapStatus(YtelseStatus ytelseStatus, Status status) {
        if (status == null) {
            return ytelseStatus != null ? ytelseStatus : YtelseStatus.UDEFINERT;
        }
        return switch (status) {
            case UNDER_BEHANDLING -> YtelseStatus.UNDER_BEHANDLING;
            case LØPENDE -> YtelseStatus.LØPENDE;
            case AVSLUTTET -> YtelseStatus.AVSLUTTET;
            default -> YtelseStatus.UDEFINERT;
        };
    }

    private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return arbeidsgiver.getErOrganisasjon() ? Arbeidsgiver.virksomhet(arbeidsgiver.getIdent()) : Arbeidsgiver.person(arbeidsgiver.getIdent());
    }

    private IntervallEntitet mapTilEntitet(Periode periode) {
        return IntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }

    private YtelseType getYtelseType(YtelseType kodeverk, Ytelser ytelse) {
        if (ytelse == null) {
            return kodeverk != null ? kodeverk : YtelseType.UDEFINERT;
        }
        return switch (ytelse) {
            case PLEIEPENGER_SYKT_BARN -> YtelseType.PLEIEPENGER_SYKT_BARN;
            case PLEIEPENGER_NÆRSTÅENDE -> YtelseType.PLEIEPENGER_NÆRSTÅENDE;
            case OMSORGSPENGER -> YtelseType.OMSORGSPENGER;
            case OPPLÆRINGSPENGER -> YtelseType.OPPLÆRINGSPENGER;

            case ENGANGSTØNAD -> YtelseType.ENGANGSTØNAD;
            case FORELDREPENGER -> YtelseType.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> YtelseType.SVANGERSKAPSPENGER;

            case FRISINN -> YtelseType.FRISINN;
        };
    }

    private Inntektskategori mapInntektsklasse(Inntektskategori inntektskategori, Inntektklasse inntektklasse) {
        if (inntektklasse == null) {
            return inntektskategori != null ? inntektskategori : Inntektskategori.UDEFINERT;
        }
        return AnvistAndel.fraInntektklasse(inntektklasse);

    }

}
