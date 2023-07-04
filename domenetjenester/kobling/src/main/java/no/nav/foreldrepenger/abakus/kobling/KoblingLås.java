package no.nav.foreldrepenger.abakus.kobling;

import java.util.Objects;

public class KoblingLås {
    /**
     * brukes kun for nye behandlinger som dummy.
     */
    private Long koblingId;

    /**
     * protected, unngå å opprette utenfor denne pakken. Kan overstyres kun til test
     */
    public KoblingLås(Long koblingId) {
        this.koblingId = koblingId;
    }

    public Long getKoblingId() {
        return this.koblingId;
    }

    void setKoblingId(long koblingId) {
        if (this.koblingId != null && !Objects.equals(koblingId, this.koblingId)) {
            throw new IllegalStateException("Kan ikke endre koblingId til annen verdi, var [" +
                this.koblingId + "], forsøkte å sette til [" +
                koblingId + "]");
        }
        this.koblingId = koblingId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof KoblingLås)) {
            return false;
        }
        KoblingLås other = (KoblingLås) obj;
        return Objects.equals(getKoblingId(), other.getKoblingId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKoblingId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<koblingId=" + getKoblingId() +
            ">";
    }
}
