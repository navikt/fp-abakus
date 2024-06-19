package no.nav.foreldrepenger.abakus.app.diagnostikk.rapportering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Set;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

public enum RapportType {
    DUPLIKAT_ARBEIDSFORHOLD(YtelseType.abakusYtelser());

    @JsonIgnore
    private final Set<YtelseType> ytelseTyper;

    private RapportType(Collection<YtelseType> ytelseTyper) {
        this.ytelseTyper = Set.copyOf(ytelseTyper);
    }

    public void valider(YtelseType ytelseType) {
        if (!ytelseTyper.contains(ytelseType)) {
            throw new IllegalArgumentException("Støtter ikke dette uttrekket [" + this.name() + "] for ytelseType:"
                    + ytelseType + ", tillater kun: " + ytelseTyper);
        }
    }
}
