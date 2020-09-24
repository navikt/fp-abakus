package no.nav.foreldrepenger.abakus.app.selftest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.app.selftest.checks.SelftestHealthCheck;

@ApplicationScoped
public class Selftests {

    private List<SelftestHealthCheck> healthCheckList = new ArrayList<>();
    private List<SelftestResultat> selftestResultat;
    private LocalDateTime sistOppdatertTid = LocalDateTime.now().minusDays(1);

    @Inject
    public Selftests(@Any Instance<SelftestHealthCheck> healthChecks) {
        healthChecks.forEach(this.healthCheckList::add);

    }

    Selftests() {
        // for CDI proxy
    }

    public List<SelftestResultat> run() {
        oppdaterSelftestResultatHvisNødvendig();
        return selftestResultat;
    }

    private synchronized void oppdaterSelftestResultatHvisNødvendig() {
        if (sistOppdatertTid.isBefore(LocalDateTime.now().minusSeconds(30))) {
            selftestResultat = innhentSelftestResultat();
            sistOppdatertTid = LocalDateTime.now();
        }
    }

    private List<SelftestResultat> innhentSelftestResultat() {

        List<SelftestResultat> samletResultat = new ArrayList<>();

        healthCheckList.forEach(h -> samletResultat.add(new SelftestResultat(h.isCritical(), h.isReady(), h.getDescription(), h.getEndpoint())));
        return samletResultat;
    }


}
