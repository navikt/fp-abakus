package no.nav.foreldrepenger.abakus.app.diagnostikk.dumps;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import no.nav.foreldrepenger.abakus.app.diagnostikk.DebugDump;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DebugDumpsters;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpKontekst;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpOutput;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;

@ApplicationScoped
@YtelseTypeRef
public class ArbeidsforholdReferanserDataDump implements DebugDump {

    private EntityManager entityManager;

    ArbeidsforholdReferanserDataDump() {
        //
    }

    @Inject
    public ArbeidsforholdReferanserDataDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(DumpKontekst dumpKontekst) {
        var sql = "select"
            + " k.saksnummer, "
            + " k.id as kobling_id, "
            + " cast(k.kobling_referanse as varchar) as kobling_referanse, "
            + " k.ytelse_type,"
            + " cast(k.bruker_aktoer_id as varchar) bruker_aktoer_id,"
            + " cast(k.annen_part_aktoer_id as varchar) annen_part_aktoer_id, "
            + " k.aktiv as k_aktiv, "
            + " gr.id as grunnlag_id, "
            + " gr.aktiv as gr_aktiv,"
            + " gr.informasjon_id as gr_informasjon_id,"
            + " cast(gr.grunnlag_referanse as varchar) as grunnlag_referanse,"
            + " r.informasjon_id,"
            + " cast(r.ekstern_referanse as varchar) as r_ekstern_referanse,"
            + " cast(r.intern_referanse as varchar) as r_intern_referanse,"
            + " r.arbeidsgiver_orgnr as r_arbeidsgiver_orgnr,"
            + " r.arbeidsgiver_aktor_id as r_arbeidsgiver_aktor_id,"
            + " replace(cast(r.opprettet_tid as varchar), ' ', 'T') as r_opprettet_tid"
            + " from gr_arbeid_inntekt gr"
            + " inner join kobling k on k.id=gr.kobling_id"
            + " inner join iay_arbeidsforhold_refer r on gr.informasjon_id=r.informasjon_id"
            + " where k.saksnummer=:saksnummer and gr.informasjon_id is not null"
            + " order by saksnummer, kobling_id, grunnlag_id, informasjon_id, r_arbeidsgiver_orgnr";

        var query = entityManager.createNativeQuery(sql, Tuple.class)
            .setParameter("saksnummer", dumpKontekst.getSaksnummer().getVerdi());
        String path = "arbeidsforhold-referanser-data.csv";

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return List.of();
        }

        return DebugDumpsters.dumpResultSetToCsv(path, results)
            .map(v -> List.of(v)).orElse(List.of());
    }
}
