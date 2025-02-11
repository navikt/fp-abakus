package no.nav.abakus.iaygrunnlag;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class JournalpostId {

    @JsonValue
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-:|]+$", message = "JournalpostId [${validatedValue}] matcher ikke tillatt pattern")
    @NotNull
    private String journalpostId;

    @JsonCreator
    public JournalpostId(String journalpostId) {
        Objects.requireNonNull(journalpostId, "journalpostId");
        this.journalpostId = journalpostId;
    }

    public String getId() {
        return journalpostId;
    }

    @Override
    public String toString() {
        return journalpostId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var other = getClass().cast(obj);
        return Objects.equals(this.journalpostId, other.journalpostId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(journalpostId);
    }

}
