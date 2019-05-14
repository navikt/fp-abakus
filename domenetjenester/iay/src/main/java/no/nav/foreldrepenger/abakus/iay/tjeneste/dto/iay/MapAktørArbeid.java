package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.weld.exceptions.UnsupportedOperationException;

import no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.ArbeidDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.PermisjonDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.arbeid.v1.YrkesaktivitetDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;

public class MapAktørArbeid {
    private class MapFraDto {

        List<AktørArbeid> map(Collection<ArbeidDto> aktørArbeid) {
            // FIXME Map AktørArbeid på vei inn
            throw new UnsupportedOperationException("Not Yet Implemented");
        }
    }

    private class MapTilDto {
        List<ArbeidDto> map(Collection<AktørArbeid> aktørArbeid) {

            return aktørArbeid.stream()
                .map(arb -> {
                    List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>(getYrkesaktiviteter(arb.getYrkesaktiviteter()));
                    List<YrkesaktivitetDto> frilansOppdrag = getYrkesaktiviteter(arb.getFrilansOppdrag());
                    yrkesaktiviteter.addAll(frilansOppdrag);
                    return new ArbeidDto(new AktørIdPersonident(arb.getAktørId().getId()))
                        .medYrkesaktiviteter(yrkesaktiviteter);
                })
                .collect(Collectors.toList());
        }

        private List<YrkesaktivitetDto> getYrkesaktiviteter(Collection<Yrkesaktivitet> aktiviteter) {
            return aktiviteter.stream().map(a -> mapYrkesaktivitet(a)).collect(Collectors.toList());
        }

        private YrkesaktivitetDto mapYrkesaktivitet(Yrkesaktivitet a) {
            var aktivitetsAvtaler = a.getAktivitetsAvtaler().stream()
                .map(aa -> new AktivitetsAvtaleDto(aa.getFraOgMed(), aa.getTilOgMed())
                    .medAntallTimer(aa.getAntallTimerVerdi())
                    .medSistLønnsendring(aa.getSisteLønnsendringsdato())
                    .medStillingsprosent(aa.getProsentsatsVerdi()))
                .collect(Collectors.toList());

            var ansettelsesperioder = a.getAnsettelsesPerioder().stream()
                .map(ap -> new Periode(ap.getFraOgMed(), ap.getTilOgMed()))
                .collect(Collectors.toList());

            var permisjoner = a.getPermisjon().stream()
                .map(p -> new PermisjonDto(new Periode(p.getFraOgMed(), p.getTilOgMed()),
                    new PermisjonsbeskrivelseType(p.getPermisjonsbeskrivelseType().getKode()))
                        .medProsentsats(p.getProsentsats().getVerdi()))
                .collect(Collectors.toList());

            var arbeidsforholdId = mapArbeidsforholdsId(a);

            var dto = new YrkesaktivitetDto(mapAktør(a.getArbeidsgiver()), new ArbeidType(a.getArbeidType().getKode()))
                .medAktivitetsAvtaler(aktivitetsAvtaler)
                .medAnsettelsesperiode(ansettelsesperioder)
                .medPermisjoner(permisjoner)
                .medArbeidsforholdId(arbeidsforholdId);

            return dto;
        }

        private ArbeidsforholdRefDto mapArbeidsforholdsId(Yrkesaktivitet yrkesaktivitet) {
            String internId = yrkesaktivitet.getArbeidsforholdRef().map(ArbeidsforholdRef::getReferanse).orElse(null);
            if (internId != null) {
                String eksternReferanse = tjeneste
                    .finnReferanseFor(koblingReferanse, yrkesaktivitet.getArbeidsgiver(), yrkesaktivitet.getArbeidsforholdRef().orElse(null), true)
                    .getReferanse();
                return new ArbeidsforholdRefDto(internId, eksternReferanse);
            }
            return new ArbeidsforholdRefDto(internId, null);
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiver) {
            return arbeidsgiver.erAktørId()
                ? new AktørIdPersonident(arbeidsgiver.getAktørId().getId())
                : new Organisasjon(arbeidsgiver.getOrgnr().getId());
        }

    }

    private InntektArbeidYtelseTjeneste tjeneste;
    private UUID koblingReferanse;

    public MapAktørArbeid(InntektArbeidYtelseTjeneste tjeneste, UUID koblingReferanse) {
        this.tjeneste = tjeneste;
        this.koblingReferanse = koblingReferanse;
    }

    public List<ArbeidDto> mapTilDto(Collection<AktørArbeid> aktørArbeid) {
        return new MapTilDto().map(aktørArbeid);
    }

    public List<AktørArbeid> mapFraDto(Collection<ArbeidDto> aktørArbeid) {
        return new MapFraDto().map(aktørArbeid);
    }
}
