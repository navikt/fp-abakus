package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

public class InfotrygdYtelseGrunnlag {

    private LocalDateTime vedtattTidspunkt;
    private YtelseType ytelseType = YtelseType.UDEFINERT;
    private TemaUnderkategori temaUnderkategori = TemaUnderkategori.UDEFINERT;
    private YtelseStatus ytelseStatus = YtelseStatus.AVSLUTTET;
    private List<InfotrygdYtelseAnvist> utbetaltePerioder;
    private LocalDate vedtaksPeriodeFom;
    private LocalDate vedtaksPeriodeTom;
    private String vedtaksreferanse;
    private Arbeidskategori kategori;
    private List<InfotrygdYtelseArbeid> arbeidsforhold;

    private BigDecimal dekningsgrad;
    private BigDecimal gradering;
    private LocalDate fødselsdatoBarn;
    private LocalDate opprinneligIdentdato;

    public InfotrygdYtelseGrunnlag() {
    }

    public LocalDateTime getVedtattTidspunkt() {
        return vedtattTidspunkt;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public TemaUnderkategori getTemaUnderkategori() {
        return temaUnderkategori;
    }

    public YtelseStatus getYtelseStatus() {
        return ytelseStatus;
    }

    public List<InfotrygdYtelseAnvist> getUtbetaltePerioder() {
        return utbetaltePerioder;
    }

    public LocalDate getVedtaksPeriodeFom() {
        return vedtaksPeriodeFom;
    }

    public LocalDate getVedtaksPeriodeTom() {
        return vedtaksPeriodeTom;
    }

    public Arbeidskategori getKategori() {
        return kategori;
    }

    public List<InfotrygdYtelseArbeid> getArbeidsforhold() {
        return arbeidsforhold;
    }

    public BigDecimal getDekningsgrad() {
        return dekningsgrad;
    }

    public BigDecimal getGradering() {
        return gradering;
    }

    public LocalDate getFødselsdatoBarn() {
        return fødselsdatoBarn;
    }

    public LocalDate getOpprinneligIdentdato() {
        return opprinneligIdentdato;
    }

    public String getVedtaksreferanse() {
        return vedtaksreferanse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfotrygdYtelseGrunnlag that = (InfotrygdYtelseGrunnlag) o;
        return Objects.equals(vedtattTidspunkt, that.vedtattTidspunkt) &&
            Objects.equals(ytelseType, that.ytelseType) &&
            Objects.equals(temaUnderkategori, that.temaUnderkategori) &&
            Objects.equals(ytelseStatus, that.ytelseStatus) &&
            Objects.equals(utbetaltePerioder, that.utbetaltePerioder) &&
            Objects.equals(vedtaksPeriodeFom, that.vedtaksPeriodeFom) &&
            Objects.equals(vedtaksPeriodeTom, that.vedtaksPeriodeTom) &&
            Objects.equals(kategori, that.kategori) &&
            Objects.equals(arbeidsforhold, that.arbeidsforhold) &&
            Objects.equals(dekningsgrad, that.dekningsgrad) &&
            Objects.equals(gradering, that.gradering) &&
            Objects.equals(fødselsdatoBarn, that.fødselsdatoBarn) &&
            Objects.equals(opprinneligIdentdato, that.opprinneligIdentdato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vedtattTidspunkt, ytelseType, temaUnderkategori, ytelseStatus, utbetaltePerioder, vedtaksPeriodeFom, vedtaksPeriodeTom, kategori, arbeidsforhold, dekningsgrad, gradering, fødselsdatoBarn, opprinneligIdentdato);
    }

    @Override
    public String toString() {
        return "InfotrygdYtelseGrunnlag{" +
            "identdato=" + vedtattTidspunkt +
            ", ytelseType=" + ytelseType +
            ", temaUnderkategori=" + temaUnderkategori +
            ", ytelseStatus=" + ytelseStatus +
            ", utbetaltePerioder=" + utbetaltePerioder +
            ", vedtaksPeriodeFom=" + vedtaksPeriodeFom +
            ", vedtaksPeriodeTom=" + vedtaksPeriodeTom +
            ", kategori=" + kategori +
            ", arbeidsforhold=" + arbeidsforhold +
            ", dekningsgrad=" + dekningsgrad +
            ", gradering=" + gradering +
            ", fødselsdatoBarn=" + fødselsdatoBarn +
            ", opprinneligIdentdato=" + opprinneligIdentdato +
            '}';
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private InfotrygdYtelseGrunnlag grunnlag;

        Builder() {
            grunnlag = new InfotrygdYtelseGrunnlag();
            grunnlag.arbeidsforhold = new ArrayList<>();
            grunnlag.utbetaltePerioder = new ArrayList<>();
        }

        public Builder medVedtattTidspunkt(LocalDateTime vedtattTidspunkt) {
            grunnlag.vedtattTidspunkt = vedtattTidspunkt;
            return this;
        }

        public Builder medYtelseType(YtelseType ytelseType) {
            grunnlag.ytelseType = ytelseType;
            return this;
        }

        public Builder medTemaUnderkategori(TemaUnderkategori temaUnderkategori) {
            grunnlag.temaUnderkategori = temaUnderkategori;
            return this;
        }

        public Builder medYtelseStatus(YtelseStatus ytelseStatus) {
            grunnlag.ytelseStatus = ytelseStatus;
            return this;
        }

        public Builder medVedtaksPeriodeFom(LocalDate vedtaksPeriodeFom) {
            grunnlag.vedtaksPeriodeFom = vedtaksPeriodeFom;
            return this;
        }

        public Builder medVedtaksPeriodeTom(LocalDate vedtaksPeriodeTom) {
            grunnlag.vedtaksPeriodeTom = vedtaksPeriodeTom;
            return this;
        }

        public Builder medArbeidskategori(Arbeidskategori arbeidskategori) {
            grunnlag.kategori = arbeidskategori;
            return this;
        }

        public Builder leggTillAnvistPerioder(InfotrygdYtelseAnvist anvistPeriode) {
            grunnlag.utbetaltePerioder.add(anvistPeriode);
            return this;
        }

        public Builder leggTilArbeidsforhold(InfotrygdYtelseArbeid arbeidsforhold) {
            grunnlag.arbeidsforhold.add(arbeidsforhold);
            return this;
        }

        public Builder medDekningsgrad(Integer dekningsgrad) {
            grunnlag.dekningsgrad = dekningsgrad == null ? null : new BigDecimal(dekningsgrad);
            return this;
        }

        public Builder medDekningsgrad(BigDecimal dekningsgrad) {
            grunnlag.dekningsgrad = dekningsgrad;
            return this;
        }

        public Builder medGradering(Integer gradering) {
            grunnlag.gradering = gradering == null ? null : new BigDecimal(gradering);
            return this;
        }


        public Builder medGradering(BigDecimal gradering) {
            grunnlag.gradering = gradering;
            return this;
        }

        public Builder medFødselsdatoBarn(LocalDate fødselsdatoBarn) {
            grunnlag.fødselsdatoBarn = fødselsdatoBarn;
            return this;
        }

        public Builder medOpprinneligIdentdato(LocalDate opprinneligIdentdato) {
            grunnlag.opprinneligIdentdato = opprinneligIdentdato;
            return this;
        }

        public Builder medVedtaksreferanse(String vedtaksreferanse) {
            grunnlag.vedtaksreferanse = vedtaksreferanse;
            return this;
        }

        public InfotrygdYtelseGrunnlag build() {
            Objects.requireNonNull(grunnlag.ytelseType);
            Objects.requireNonNull(grunnlag.temaUnderkategori);
            Objects.requireNonNull(grunnlag.vedtattTidspunkt);
            Objects.requireNonNull(grunnlag.vedtaksPeriodeFom);
            return grunnlag;
        }
    }
}
