package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.abakus.kodeverk.Landkoder;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface OppgittEgenNæring {

    LocalDate getFraOgMed();

    LocalDate getTilOgMed();

    DatoIntervallEntitet getPeriode();

    VirksomhetType getVirksomhetType();

    OrgNummer getOrgnummer();

    String getRegnskapsførerNavn();

    String getRegnskapsførerTlf();

    LocalDate getEndringDato();

    BigDecimal getBruttoInntekt();

    String getBegrunnelse();

    boolean getNyoppstartet();

    boolean getNyIArbeidslivet();

    boolean getVarigEndring();

    boolean getNærRelasjon();


    Landkoder getLandkode();

    String getUtenlandskVirksomhetNavn();
}
