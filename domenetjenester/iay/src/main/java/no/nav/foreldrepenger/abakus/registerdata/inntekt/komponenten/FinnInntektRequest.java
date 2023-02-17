package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import java.time.YearMonth;
import java.util.Objects;

public class FinnInntektRequest {

    private String aktørId;
    private String fnr;
    private YearMonth fom;
    private YearMonth tom;

    private FinnInntektRequest(YearMonth fom, YearMonth tom) {
        this.fom = fom;
        this.tom = tom;
    }

    public static FinnInntektRequestBuilder builder(YearMonth fom, YearMonth tom) {
        FinnInntektRequestBuilder builder = new FinnInntektRequestBuilder();
        builder.kladd = new FinnInntektRequest(fom, tom);
        return builder;
    }

    public String getAktørId() {
        return aktørId;
    }

    public String getFnr() {
        return fnr;
    }

    public YearMonth getFom() {
        return fom;
    }

    public YearMonth getTom() {
        return tom;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof FinnInntektRequest)) {
            return false;
        }
        FinnInntektRequest other = (FinnInntektRequest) obj;
        return Objects.equals(this.fnr, other.fnr) && Objects.equals(this.aktørId, other.aktørId) && Objects.equals(this.fom, other.fom)
            && Objects.equals(this.tom, other.tom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fnr, aktørId, fom, tom);
    }

    public static final class FinnInntektRequestBuilder {
        private FinnInntektRequest kladd;
        private boolean gyldigTilstand;

        private FinnInntektRequestBuilder() {
        }

        public FinnInntektRequestBuilder medFnr(String fnr) {
            if (kladd.aktørId != null) {
                throw new IllegalStateException("Utviklerfeil: kan ikke oppgi både fnr og aktørid");
            }
            kladd.fnr = fnr;
            this.gyldigTilstand = true;
            return this;
        }

        public FinnInntektRequestBuilder medAktørId(String aktørId) {
            if (kladd.fnr != null) {
                throw new IllegalStateException("Utviklerfeil: kan ikke oppgi både fnr og aktørid");
            }
            kladd.aktørId = aktørId;
            this.gyldigTilstand = true;
            return this;
        }

        public FinnInntektRequest build() {
            if (!gyldigTilstand) {
                throw new IllegalStateException("Utviklerfeil: må oppgi fnr eller aktørid");
            }
            return kladd;
        }
    }
}
