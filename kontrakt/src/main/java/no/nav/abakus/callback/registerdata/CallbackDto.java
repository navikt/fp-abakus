package no.nav.abakus.callback.registerdata;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Metadata for callback
 * Inneholder:
 * - opprettetTidspunkt: Tidspunktet hvor det oppdaterte grunnlaget ble commitet til database
 * - avsenderRef: avsenderRef til behandlingen
 * - opprinneligGrunnlagRef: tilstand før registerinnhenting, referanse til opprinnelig grunnlag
 * - oppdatertGrunnlagRef: resultatet av registerinnhenting, referanse til nytt grunnlag
 */
public class CallbackDto {

    @NotNull
    @Valid
    private LocalDateTime opprettetTidspunkt;

    @NotNull
    @Valid
    private ReferanseDto avsenderRef;

    @Valid
    private ReferanseDto opprinneligGrunnlagRef;

    @Valid
    private ReferanseDto oppdatertGrunnlagRef;

    public ReferanseDto getAvsenderRef() {
        return avsenderRef;
    }

    public void setAvsenderRef(ReferanseDto avsenderRef) {
        this.avsenderRef = avsenderRef;
    }

    public ReferanseDto getOppdatertGrunnlagRef() {
        return oppdatertGrunnlagRef;
    }

    public void setOppdatertGrunnlagRef(ReferanseDto oppdatertGrunnlagRef) {
        this.oppdatertGrunnlagRef = oppdatertGrunnlagRef;
    }

    public ReferanseDto getOpprinneligGrunnlagRef() {
        return opprinneligGrunnlagRef;
    }

    public void setOpprinneligGrunnlagRef(ReferanseDto opprinneligGrunnlagRef) {
        this.opprinneligGrunnlagRef = opprinneligGrunnlagRef;
    }

    public LocalDateTime getOpprettetTidspunkt() {
        return opprettetTidspunkt;
    }

    public void setOpprettetTidspunkt(LocalDateTime opprettetTidspunkt) {
        this.opprettetTidspunkt = opprettetTidspunkt;
    }
}
