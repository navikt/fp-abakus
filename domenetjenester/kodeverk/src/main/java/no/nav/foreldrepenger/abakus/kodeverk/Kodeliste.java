package no.nav.foreldrepenger.abakus.kodeverk;


import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.vedtak.util.StringUtils;

/**
 * Et innslag i en liste av koder tilgjengelig for et Kodeverk.
 * Koder kan legges til og oppdateres, men tracker ikke endringer over tid (kun av om de er tilgjengelig).
 * <p>
 * Koder skal ikke gjenbrukes, i tråd med anbefalinger fra Kodeverkforvaltningen.Derfor vil kun en
 * gyldighetsperiode vedlikeholdes per kode.
 */
@MappedSuperclass
@Table(name = "KODELISTE")
@DiscriminatorColumn(name = "kodeverk")
@NamedEntityGraph(
    name = "KodelistMedNavn",
    attributeNodes = {
        @NamedAttributeNode(value = "kode"),
        @NamedAttributeNode(value = "kodeverk"),
        @NamedAttributeNode(value = "offisiellKode"),
        @NamedAttributeNode(value = "beskrivelse"),
        @NamedAttributeNode(value = "ekstraData"),
        @NamedAttributeNode(value = "kodelisteNavnI18NList", subgraph = "kodelistNavn")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "kodelistNavn",
            attributeNodes = {
                @NamedAttributeNode(value = "id"),
                @NamedAttributeNode(value = "kodeliste"),
                @NamedAttributeNode(value = "navn"),
                @NamedAttributeNode(value = "språk")
            }
        )
    }
)
public abstract class Kodeliste extends KodeverkBaseEntitet implements Comparable<Kodeliste>, Kodeverdi {
    public static final Comparator<Kodeliste> NULLSAFE_KODELISTE_COMPARATOR = Comparator.nullsFirst(Kodeliste::compareTo);
    private static final Logger LOG = LoggerFactory.getLogger(Kodeliste.class);

    /**
     * Default fil er samme som property key navn.
     */
    @Id
    @Column(name = "kodeverk", nullable = false)
    private String kodeverk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kodeverk", referencedColumnName = "kode", insertable = false, updatable = false, nullable = false)
    private Kodeverk kodeverkEntitet;

    @Id
    @Column(name = "kode", nullable = false, updatable = false, insertable = false)
    private String kode;

    /**
     * Kode bestemt av kodeeier. Kan avvike fra intern kodebruk
     */
    @Column(name = "offisiell_kode", updatable = false, insertable = false)
    private String offisiellKode;

    @Column(name = "beskrivelse", updatable = false, insertable = false)
    private String beskrivelse;

    @Column(name = "navn", updatable = false, insertable = false)
    private String navn;

    /**
     * ISO-639-1-alpha-2 lower-case språk.
     */
    @Column(name = "sprak", nullable = false, updatable = false, insertable = false)
    private String språk = "nb";

    /**
     * Når koden gjelder fra og med.
     */
    @Column(name = "gyldig_fom", nullable = false, updatable = false, insertable = false)
    private LocalDate gyldigFraOgMed = LocalDate.of(2000, 01, 01); // NOSONAR

    /**
     * Når koden gjelder til og med.
     */
    @Column(name = "gyldig_tom", nullable = false, updatable = false, insertable = false)
    private LocalDate gyldigTilOgMed = LocalDate.of(9999, 12, 31); // NOSONAR

    /**
     * Denne skal kun inneholde JSON data. Struktur på Json er opp til konkret subklasse å tolke (bruk {@link #getJsonField(String)}
     */
    @Column(name = "ekstra_data", updatable = false, insertable = false)
    private String ekstraData;

    @DiffIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "kodeliste", fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @BatchSize(size = 1000)
    private List<KodelisteNavnI18N> kodelisteNavnI18NList;

    /**
     * Skal ikke leses fra databasen, kun slås opp.
     */
    @Transient
    private String displayNavn;

    protected Kodeliste() {
        // proxy for hibernate
    }

    public Kodeliste(String kode, String kodeverk) {
        Objects.requireNonNull(kode, "kode"); //$NON-NLS-1$
        Objects.requireNonNull(kodeverk, "kodeverk"); //$NON-NLS-1$
        this.kode = kode;
        this.kodeverk = kodeverk;
    }

    public Kodeliste(String kode, String kodeverk, String offisiellKode, String navn, LocalDate fom, LocalDate tom) {
        this(kode, kodeverk);
        this.offisiellKode = offisiellKode;
        this.navn = navn;
        this.gyldigFraOgMed = fom;
        this.gyldigTilOgMed = tom;
    }

    static final String hentLoggedInBrukerSpråk() {
        return "NB"; //TODO(HUMLE): må utvidere til å finne språk til bruker som er logged inn.
    }

    public static List<String> kodeVerdier(Kodeliste... entries) {
        return kodeVerdier(Arrays.asList(entries));
    }

    public static List<String> kodeVerdier(Collection<? extends Kodeliste> entries) {
        return entries.stream().map(k -> k.getKode()).collect(Collectors.toList());
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }

    @Override
    public String getIndexKey() {
        return kodeverk + ":" + kode;
    }

    public boolean erLikOffisiellKode(String annenOffisiellKode) {
        if (offisiellKode == null) {
            throw new IllegalArgumentException("Har ikke offisiellkode for, Kodeverk=" + getKodeverk() + ", kode=" + getKode()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return offisiellKode.equals(annenOffisiellKode);
    }

    /**
     * Returnerer språk i standard ISO 639-1-alpha2 lower case.
     */
    public String getSpråk() {
        return språk;
    }

    @Override
    public String getNavn() {
        String navn = null;
        if (displayNavn == null) {
            if (kodelisteNavnI18NList != null) {
                String brukerSpråk = hentLoggedInBrukerSpråk();
                for (KodelisteNavnI18N kodelisteNavnI18N : kodelisteNavnI18NList) {
                    if (brukerSpråk.equals(kodelisteNavnI18N.getSpråk())) {
                        navn = kodelisteNavnI18N.getNavn();
                        break;
                    }
                }
            }

            if (!StringUtils.nullOrEmpty(navn)) {
                this.displayNavn = navn;
            } else {
                // FIXME (FC): må her bytte ut med brukers lang fra HTTP Accept-Language header når får på plass full
                // i18n
                this.displayNavn = navn;
            }
        }
        if (displayNavn == null) {
            LOG.warn("Kodeliste(kode={}, kodeverk={}) mangler navn. Prøver sannsynligvis å hente navn fra konstant.", kode, kodeverk);
        }
        return displayNavn;
    }

    public LocalDate getGyldigFraOgMed() {
        return gyldigFraOgMed;
    }

    public LocalDate getGyldigTilOgMed() {
        return gyldigTilOgMed;
    }

    protected String getEkstraData() {
        return ekstraData;
    }

    protected String getJsonField(String key) {
        if (getEkstraData() == null) {
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode jsonNode = om.readTree(getEkstraData()).get(key);
            return jsonNode == null ? null : jsonNode.asText();
        } catch (IOException e) {
            throw new IllegalStateException("Ugyldig format (forventet JSON) for kodeverk=" + getKodeverk() + ", kode=" + getKode() //$NON-NLS-1$ //$NON-NLS-2$
                + ", jsonKey=" + key + ": " + getEkstraData(), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Kodeliste)) {
            return false;
        }
        Kodeliste other = (Kodeliste) obj;
        return Objects.equals(getKode(), other.getKode())
            && Objects.equals(getKodeverk(), other.getKodeverk());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKode(), getKodeverk());
    }

    @Override
    public int compareTo(Kodeliste that) {
        return that.getKode().compareTo(this.getKode());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "<" //$NON-NLS-1$
            + "kode=" + getKode() //$NON-NLS-1$
            + ">"; //$NON-NLS-1$
    }

    @Override
    public String getKodeverk() {
        if (kodeverk == null) {
            DiscriminatorValue dc = getClass().getDeclaredAnnotation(DiscriminatorValue.class);
            if (dc != null) {
                kodeverk = dc.value();
            }
        }
        return kodeverk;
    }
}
