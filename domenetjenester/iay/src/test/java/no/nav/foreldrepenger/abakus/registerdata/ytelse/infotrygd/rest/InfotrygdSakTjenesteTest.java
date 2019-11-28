package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles.InfotrygdSakAggregator;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InfotrygdSakTjenesteTest {

    @Inject
    @Any
    private InfotrygdSakAggregator sakAggregator;
    
    @Test
    public void fant_den() throws Exception {
        System.out.println(sakAggregator);
    }
}
