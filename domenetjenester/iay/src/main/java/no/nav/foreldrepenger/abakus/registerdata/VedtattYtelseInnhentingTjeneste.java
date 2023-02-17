package no.nav.foreldrepenger.abakus.registerdata;

import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistAndel;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistAndelBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelse;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseAndel;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;

@ApplicationScoped
public class VedtattYtelseInnhentingTjeneste {

    private VedtakYtelseRepository vedtakYtelseRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    protected VedtattYtelseInnhentingTjeneste() {
    }

    @Inject
    public VedtattYtelseInnhentingTjeneste(VedtakYtelseRepository vedtakYtelseRepository,
                                           InntektArbeidYtelseRepository inntektArbeidYtelseRepository) {
        this.vedtakYtelseRepository = vedtakYtelseRepository;
        this.inntektArbeidYtelseRepository = inntektArbeidYtelseRepository;
    }

    void innhentFraYtelsesRegister(AktørId aktørId, Kobling kobling, InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder builder) {
        IntervallEntitet opplysningsperiode = kobling.getOpplysningsperiode();
        List<VedtakYtelse> vedtatteYtelser = vedtakYtelseRepository.hentYtelserForIPeriode(aktørId, opplysningsperiode.getFomDato(),
            opplysningsperiode.getTomDato());

        var arbeidsforholdInformasjon = inntektArbeidYtelseRepository.hentArbeidsforholdInformasjonForBehandling(kobling.getKoblingReferanse());
        var arbeidsforholdInformasjonBuilder = ArbeidsforholdInformasjonBuilder.builder(arbeidsforholdInformasjon);
        for (var vedtattYtelse : vedtatteYtelser) {
            YtelseBuilder ytelseBuilder = builder.getYtelselseBuilderForType(vedtattYtelse.getKilde(), vedtattYtelse.getYtelseType(),
                vedtattYtelse.getSaksnummer());
            ytelseBuilder.medPeriode(vedtattYtelse.getPeriode())
                .medStatus(vedtattYtelse.getStatus())
                .medVedtattTidspunkt(vedtattYtelse.getVedtattTidspunkt());

            mapAnvisninger(vedtattYtelse, ytelseBuilder, arbeidsforholdInformasjonBuilder);
            builder.leggTilYtelse(ytelseBuilder);
        }
        var inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentInntektArbeidYtelseGrunnlagForBehandling(kobling.getKoblingReferanse());
        var nyttGrunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidYtelseGrunnlag);
        nyttGrunnlagBuilder.medInformasjon(arbeidsforholdInformasjonBuilder.build());
        inntektArbeidYtelseRepository.lagre(kobling.getKoblingReferanse(), nyttGrunnlagBuilder);
    }


    private void mapAnvisninger(VedtakYtelse vedtattYtelse,
                                YtelseBuilder ytelseBuilder,
                                ArbeidsforholdInformasjonBuilder arbeidsforholdInformasjonBuilder) {
        vedtattYtelse.getYtelseAnvist().forEach(anvisning -> {
            YtelseAnvistBuilder anvistBuilder = ytelseBuilder.getAnvistBuilder();
            IntervallEntitet periode = utledPeriodeNårTomMuligFørFom(anvisning.getAnvistFom(), anvisning.getAnvistTom());
            anvistBuilder.medAnvistPeriode(periode)
                .medBeløp(anvisning.getBeløp().map(Beløp::getVerdi).orElse(null))
                .medDagsats(anvisning.getDagsats().map(Beløp::getVerdi).orElse(null))
                .medUtbetalingsgradProsent(anvisning.getUtbetalingsgradProsent().map(Stillingsprosent::getVerdi).orElse(null));
            if (anvisning.getAndeler() != null) {
                anvisning.getAndeler().forEach(andel -> anvistBuilder.leggTilYtelseAnvistAndel(mapAndel(arbeidsforholdInformasjonBuilder, andel)));
            }
            ytelseBuilder.leggtilYtelseAnvist(anvistBuilder.build());
        });
    }

    private YtelseAnvistAndel mapAndel(ArbeidsforholdInformasjonBuilder arbeidsforholdInformasjonBuilder, VedtakYtelseAndel andel) {
        var arbeidsgiver = mapArbeidsgiver(andel);
        return YtelseAnvistAndelBuilder.ny()
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdRef(mapInternArbeidsforholdRef(arbeidsforholdInformasjonBuilder, andel, arbeidsgiver))
            .medInntektskategori(andel.getInntektskategori())
            .medRefusjonsgrad(andel.getRefusjonsgradProsent().getVerdi())
            .medUtbetalingsgrad(andel.getUtbetalingsgradProsent().getVerdi())
            .medDagsats(andel.getDagsats().getVerdi())
            .build();
    }

    private InternArbeidsforholdRef mapInternArbeidsforholdRef(ArbeidsforholdInformasjonBuilder arbeidsforholdInformasjonBuilder,
                                                               VedtakYtelseAndel andel,
                                                               Arbeidsgiver arbeidsgiver) {
        if (andel.getArbeidsforholdId() != null) {
            return arbeidsforholdInformasjonBuilder.finnEllerOpprett(arbeidsgiver, EksternArbeidsforholdRef.ref(andel.getArbeidsforholdId()));
        }
        return null;
    }

    private Arbeidsgiver mapArbeidsgiver(VedtakYtelseAndel andel) {
        return andel.getArbeidsgiver()
            .map(a -> a.getOrgnr() != null ? Arbeidsgiver.virksomhet(a.getOrgnr()) : Arbeidsgiver.person(a.getAktørId()))
            .orElse(null);
    }

    private IntervallEntitet utledPeriodeNårTomMuligFørFom(LocalDate fom, LocalDate tom) {
        if (tom == null) {
            return IntervallEntitet.fraOgMed(fom);
        }
        if (tom.isBefore(fom)) {
            return IntervallEntitet.fraOgMedTilOgMed(fom, fom);
        }
        return IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }


}
