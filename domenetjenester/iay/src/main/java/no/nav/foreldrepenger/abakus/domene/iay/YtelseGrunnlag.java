package no.nav.foreldrepenger.abakus.domene.iay;

import jakarta.persistence.*;
import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.ArbeidskategoriKodeverdiConverter;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity(name = "YtelseGrunnlagEntitet")
@Table(name = "IAY_YTELSE_GRUNNLAG")
public class YtelseGrunnlag extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE_GRUNNLAG")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "ytelse_id", nullable = false, updatable = false, unique = true)
    private Ytelse ytelse;

    @OneToMany(mappedBy = "ytelseGrunnlag")
    @ChangeTracked
    private List<YtelseStørrelse> ytelseStørrelse = new ArrayList<>();

    @Convert(converter = ArbeidskategoriKodeverdiConverter.class)
    @Column(name = "arbeidskategori", nullable = false, updatable = false)
    private Arbeidskategori arbeidskategori = Arbeidskategori.UDEFINERT;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "dekningsgrad_prosent")))
    private Stillingsprosent dekningsgradProsent;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "gradering_prosent")))
    private Stillingsprosent graderingProsent;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "inntektsgrunnlag_prosent")))
    private Stillingsprosent inntektProsent;

    @Column(name = "opprinnelig_identdato")
    @ChangeTracked
    private LocalDate opprinneligIdentdato;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "dagsats")))
    @ChangeTracked
    private Beløp vedtaksDagsats;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public YtelseGrunnlag() {
        // hibernate
    }

    public YtelseGrunnlag(YtelseGrunnlag ytelseGrunnlag) {
        this.arbeidskategori = ytelseGrunnlag.getArbeidskategori().orElse(null);
        this.dekningsgradProsent = ytelseGrunnlag.getDekningsgradProsent().orElse(null);
        this.graderingProsent = ytelseGrunnlag.getGraderingProsent().orElse(null);
        this.inntektProsent = ytelseGrunnlag.getInntektsgrunnlagProsent().orElse(null);
        this.opprinneligIdentdato = ytelseGrunnlag.getOpprinneligIdentdato().orElse(null);
        this.ytelseStørrelse = ytelseGrunnlag.getYtelseStørrelse().stream().map(ys -> {
            YtelseStørrelse ytelseStørrelseEntitet = new YtelseStørrelse(ys);
            ytelseStørrelseEntitet.setYtelseGrunnlag(this);
            return ytelseStørrelseEntitet;
        }).collect(Collectors.toList());
        this.vedtaksDagsats = ytelseGrunnlag.getVedtaksDagsats().orElse(null);
    }

    public Optional<Arbeidskategori> getArbeidskategori() {
        return Optional.ofNullable(arbeidskategori);
    }

    void setArbeidskategori(Arbeidskategori arbeidskategori) {
        this.arbeidskategori = arbeidskategori;
    }

    public Optional<Stillingsprosent> getDekningsgradProsent() {
        return Optional.ofNullable(dekningsgradProsent);
    }

    void setDekningsgradProsent(Stillingsprosent prosent) {
        this.dekningsgradProsent = prosent;
    }

    public Optional<Stillingsprosent> getGraderingProsent() {
        return Optional.ofNullable(graderingProsent);
    }

    void setGraderingProsent(Stillingsprosent prosent) {
        this.graderingProsent = prosent;
    }

    public Optional<Stillingsprosent> getInntektsgrunnlagProsent() {
        return Optional.ofNullable(inntektProsent);
    }

    void setInntektsgrunnlagProsent(Stillingsprosent prosent) {
        this.inntektProsent = prosent;
    }

    public Optional<LocalDate> getOpprinneligIdentdato() {
        return Optional.ofNullable(opprinneligIdentdato);
    }

    void setOpprinneligIdentdato(LocalDate dato) {
        this.opprinneligIdentdato = dato;
    }

    public List<YtelseStørrelse> getYtelseStørrelse() {
        return Collections.unmodifiableList(ytelseStørrelse);
    }

    void leggTilYtelseStørrelse(YtelseStørrelse ytelseStørrelse) {
        ytelseStørrelse.setYtelseGrunnlag(this);
        this.ytelseStørrelse.add(ytelseStørrelse);

    }

    void tilbakestillStørrelse() {
        ytelseStørrelse.clear();
    }


    void setYtelse(Ytelse ytelse) {
        this.ytelse = ytelse;
    }

    public Optional<Beløp> getVedtaksDagsats() {
        return Optional.ofNullable(vedtaksDagsats);
    }

    void setVedtaksDagsats(Beløp vedtaksDagsats) {
        this.vedtaksDagsats = vedtaksDagsats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof YtelseGrunnlag)) {
            return false;
        }
        var that = (YtelseGrunnlag) o;
        return Objects.equals(arbeidskategori, that.arbeidskategori) && Objects.equals(dekningsgradProsent, that.dekningsgradProsent)
            && Objects.equals(graderingProsent, that.graderingProsent) && Objects.equals(inntektProsent, that.inntektProsent) && Objects.equals(
            opprinneligIdentdato, that.opprinneligIdentdato) && Objects.equals(vedtaksDagsats, that.vedtaksDagsats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidskategori, dekningsgradProsent, graderingProsent, inntektProsent, opprinneligIdentdato, vedtaksDagsats);
    }

    @Override
    public String toString() {
        return "YtelseGrunnlagEntitet{" + "arbeidskategori=" + arbeidskategori + ", dekngradProsent=" + dekningsgradProsent + ", graderingProsent="
            + graderingProsent + ", inntektProsent=" + inntektProsent + ", opprinneligIdentdato=" + opprinneligIdentdato + ", vedtaksDagsats="
            + vedtaksDagsats + '}';
    }
}
