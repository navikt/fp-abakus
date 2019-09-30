package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class InfotrygdSak {
    private TemaUnderkategori temaUnderkategori = TemaUnderkategori.UDEFINERT;
    private LocalDate registrert;
    private LocalDate iverksatt;
    private YtelseType relatertYtelseType = YtelseType.UDEFINERT;
    private YtelseStatus relatertYtelseTilstand = YtelseStatus.AVSLUTTET;
    private DatoIntervallEntitet periode;

    private InfotrygdSak() {
        // NOSONAR
    }

    public TemaUnderkategori getTemaUnderkategori() {
        return temaUnderkategori;
    }

    public LocalDate getRegistrert() {
        return registrert;
    }

    public LocalDate getIverksatt() {
        return iverksatt;
    }

    public YtelseStatus getYtelseStatus() {
        return relatertYtelseTilstand;
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public YtelseType getYtelseType() {
        return relatertYtelseType;
    }

    public YtelseType hentRelatertYtelseTypeForSammenstillingMedBeregningsgrunnlag() {
        if (relatertYtelseType != null && (relatertYtelseType.equals(YtelseType.SVANGERSKAPSPENGER))) {
            return YtelseType.FORELDREPENGER;
        }
        return relatertYtelseType;
    }

    public boolean erAvRelatertYtelseType(YtelseType... ytelseTyper) {
        if (relatertYtelseType == null) return false;
        for (YtelseType relatertYtelse : ytelseTyper) {
            if (relatertYtelse.equals(relatertYtelseType)) return true;
        }
        return false;
    }

    public boolean erVedtak() {
        return iverksatt != null;
    }

    public static class InfotrygdSakBuilder {
        private final InfotrygdSak sak;
        private LocalDate opphørFom;

        InfotrygdSakBuilder(InfotrygdSak sak) {
            this.sak = sak;
        }

        public static InfotrygdSakBuilder ny() {
            return new InfotrygdSakBuilder(new InfotrygdSak());
        }

        public InfotrygdSakBuilder medTemaUnderkategori(TemaUnderkategori temaUnderkategori) {
            this.sak.temaUnderkategori = temaUnderkategori;
            return this;
        }

        public InfotrygdSakBuilder medRegistrert(LocalDate registrert) {
            this.sak.registrert = registrert;
            return this;
        }

        public InfotrygdSakBuilder medIverksatt(LocalDate iverksatt) {
            this.sak.iverksatt = iverksatt;
            return this;
        }

        public InfotrygdSakBuilder medOpphørFom(LocalDate opphørFom) {
            this.opphørFom = opphørFom;
            return this;
        }

        public InfotrygdSakBuilder medYtelseType(YtelseType relatertYtelseType) {
            this.sak.relatertYtelseType = relatertYtelseType;
            return this;
        }

        public InfotrygdSakBuilder medRelatertYtelseTilstand(YtelseStatus relatertYtelseTilstand) {
            this.sak.relatertYtelseTilstand = relatertYtelseTilstand;
            return this;
        }

        public InfotrygdSakBuilder medPeriode(DatoIntervallEntitet periode) {
            this.sak.periode = periode;
            return this;
        }

        public InfotrygdSak build() {
            Objects.requireNonNull(this.sak.registrert);
            if (this.sak.periode == null) {
                this.sak.periode = utledPeriode(this.sak.iverksatt, this.opphørFom, this.sak.registrert);
            }
            return this.sak;
        }

        private DatoIntervallEntitet utledPeriode(LocalDate iverksatt, LocalDate opphoerFomDato, LocalDate registrert) {
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
                }
                return DatoIntervallEntitet.fraOgMed(registrert);
            }
        }

        private LocalDate localDateMinus1Virkedag(LocalDate opphoerFomDato) {
            LocalDate dato = opphoerFomDato.minusDays(1);
            if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
                dato = opphoerFomDato.minusDays(1L + dato.getDayOfWeek().getValue() - DayOfWeek.FRIDAY.getValue());
            }
            return dato;
        }
    }

}
