package no.nav.foreldrepenger.abakus.domene.iay;

import javax.persistence.Entity;

import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;

@Entity(name = "YtelseType")
public class YtelseType extends Kodeliste {

    YtelseType(String kode, String discriminator) {
        super(kode, discriminator);
    }

    public YtelseType() {
        //hibernate
    }
}
