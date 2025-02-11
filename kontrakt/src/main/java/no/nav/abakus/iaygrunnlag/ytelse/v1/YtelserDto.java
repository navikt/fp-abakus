package no.nav.abakus.iaygrunnlag.ytelse.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import no.nav.abakus.iaygrunnlag.PersonIdent;

import java.util.List;
import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.ALWAYS)
public class YtelserDto {

    /**
     * Bruker ytelse gjelder for.
     */
    @JsonProperty(value = "person", required = true)
    @Valid
    private PersonIdent person;

    @JsonProperty(value = "ytelser")
    @Valid
    private List<YtelseDto> ytelser;

    protected YtelserDto() {
    }

    public YtelserDto(PersonIdent person) {
        Objects.requireNonNull(person, "person");
        this.person = person;
    }

    public PersonIdent getPerson() {
        return person;
    }

    public List<YtelseDto> getYtelser() {
        return ytelser;
    }

    public void setYtelser(List<YtelseDto> ytelser) {
        this.ytelser = ytelser;
    }

    public YtelserDto medYtelser(List<YtelseDto> ytelser) {
        this.ytelser = ytelser;
        return this;
    }
}
