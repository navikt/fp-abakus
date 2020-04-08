package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kodeverk.Landkoder;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

public class OppgittOpptjeningBuilder {

    private final OppgittOpptjening kladd;

    private OppgittOpptjeningBuilder(OppgittOpptjening kladd) {
        this.kladd = kladd;
    }

    public static OppgittOpptjeningBuilder ny() {
        return ny(UUID.randomUUID(), LocalDateTime.now());
    }

    public static OppgittOpptjeningBuilder ny(UUID eksternReferanse, LocalDateTime opprettetTidspunktOriginalt) {
        return new OppgittOpptjeningBuilder(new OppgittOpptjening(eksternReferanse, opprettetTidspunktOriginalt));
    }

    public static OppgittOpptjeningBuilder ny(UUID eksternReferanse, OffsetDateTime opprettetTidspunktOriginalt) {
        return new OppgittOpptjeningBuilder(
            new OppgittOpptjening(eksternReferanse, opprettetTidspunktOriginalt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()));
    }

    public OppgittOpptjeningBuilder leggTilAnnenAktivitet(OppgittAnnenAktivitet annenAktivitet) {
        this.kladd.leggTilAnnenAktivitet(annenAktivitet);
        return this;
    }

    public OppgittOpptjeningBuilder leggTilFrilansOpplysninger(OppgittFrilans frilans) {
        this.kladd.leggTilFrilans(frilans);
        return this;
    }

    public OppgittOpptjeningBuilder leggTilEgneNæringer(List<EgenNæringBuilder> builders) {
        builders.forEach(builder -> this.kladd.leggTilEgenNæring(builder.build()));
        return this;
    }

    public OppgittOpptjeningBuilder leggTilOppgittArbeidsforhold(OppgittArbeidsforholdBuilder builder) {
        this.kladd.leggTilOppgittArbeidsforhold(builder.build());
        return this;
    }

    public UUID getEksternReferanse() {
        return kladd.getEksternReferanse();
    }

    public OppgittOpptjening build() {
        return kladd;
    }

    public static class EgenNæringBuilder {
        private final OppgittEgenNæring entitet;

        private EgenNæringBuilder(OppgittEgenNæring entitet) {
            this.entitet = entitet;
        }

        public static EgenNæringBuilder ny() {
            return new EgenNæringBuilder(new OppgittEgenNæring());
        }

        public EgenNæringBuilder medPeriode(IntervallEntitet periode) {
            this.entitet.setPeriode(periode);
            return this;
        }

        public EgenNæringBuilder medVirksomhet(OrgNummer virksomhet) {
            this.entitet.setVirksomhet(virksomhet);
            return this;
        }

        public EgenNæringBuilder medVirksomhetType(VirksomhetType type) {
            this.entitet.setVirksomhetType(type);
            return this;
        }

        public EgenNæringBuilder medRegnskapsførerNavn(String navn) {
            this.entitet.setRegnskapsførerNavn(navn);
            return this;
        }

        public EgenNæringBuilder medRegnskapsførerTlf(String tlf) {
            this.entitet.setRegnskapsførerTlf(tlf);
            return this;
        }

        public EgenNæringBuilder medEndringDato(LocalDate dato) {
            this.entitet.setEndringDato(dato);
            return this;
        }

        public EgenNæringBuilder medBegrunnelse(String begrunnelse) {
            this.entitet.setBegrunnelse(begrunnelse);
            return this;
        }

        public EgenNæringBuilder medNyoppstartet(boolean nyoppstartet) {
            this.entitet.setNyoppstartet(nyoppstartet);
            return this;
        }

        public EgenNæringBuilder medVarigEndring(boolean varigEndring) {
            this.entitet.setVarigEndring(varigEndring);
            return this;
        }

        public EgenNæringBuilder medNærRelasjon(boolean nærRelasjon) {
            this.entitet.setNærRelasjon(nærRelasjon);
            return this;
        }

        public EgenNæringBuilder medBruttoInntekt(BigDecimal bruttoInntekt) {
            this.entitet.setBruttoInntekt(bruttoInntekt);
            return this;
        }

        public EgenNæringBuilder medUtenlandskVirksomhet(Landkoder landkode, String utenlandskVirksomhetNavn) {
            this.entitet.setLandkode(landkode);
            this.entitet.setUtenlandskVirksomhetNavn(utenlandskVirksomhetNavn);
            return this;
        }

        public OppgittEgenNæring build() {
            return entitet;
        }

        public EgenNæringBuilder medNyIArbeidslivet(boolean nyIArbeidslivet) {
            this.entitet.setNyIArbeidslivet(nyIArbeidslivet);
            return this;

        }

        public EgenNæringBuilder medLandkode(Landkoder landkode) {
            this.entitet.setLandkode(landkode);
            return this;
        }
    }

    public static class OppgittArbeidsforholdBuilder {
        private OppgittArbeidsforhold entitet;

        private OppgittArbeidsforholdBuilder(OppgittArbeidsforhold entitet) {
            this.entitet = entitet;
        }

        public static OppgittArbeidsforholdBuilder ny() {
            return new OppgittArbeidsforholdBuilder(new OppgittArbeidsforhold());
        }

        public OppgittArbeidsforholdBuilder medPeriode(IntervallEntitet periode) {
            this.entitet.setPeriode(periode);
            return this;
        }

        public OppgittArbeidsforholdBuilder medErUtenlandskInntekt(Boolean erUtenlandskInntekt) {
            this.entitet.setErUtenlandskInntekt(erUtenlandskInntekt);
            return this;
        }

        public OppgittArbeidsforholdBuilder medArbeidType(ArbeidType arbeidType) {
            this.entitet.setArbeidType(arbeidType);
            return this;
        }

        public OppgittArbeidsforholdBuilder medUtenlandskVirksomhet(Landkoder landkode, String utenlandskVirksomhetNavn) {
            this.entitet.setLandkode(landkode);
            if (utenlandskVirksomhetNavn != null) {
                this.entitet.setUtenlandskVirksomhetNavn(utenlandskVirksomhetNavn);
            }
            return this;
        }

        public OppgittArbeidsforhold build() {
            return entitet;
        }

        public OppgittArbeidsforholdBuilder medLandkode(Landkoder landkode) {
            this.entitet.setLandkode(landkode);
            return this;
        }
    }
}
