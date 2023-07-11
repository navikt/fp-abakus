package no.nav.foreldrepenger.abakus.app.diagnostikk.rapportering;

import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.app.diagnostikk.CsvOutput;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpOutput;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

@ApplicationScoped
@RapportTypeRef(RapportType.DUPLIKAT_ARBEIDSFORHOLD)
public class UttrekkDuplikatArbeidsforhold implements RapportGenerator {

    private EntityManager entityManager;

    UttrekkDuplikatArbeidsforhold() {
        //
    }

    @Inject
    public UttrekkDuplikatArbeidsforhold(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DumpOutput> generer(YtelseType ytelseType, IntervallEntitet periode) {
        String sql = """
               select
                  k.saksnummer, k.bruker_aktoer_id as aktoer_id, k.ytelse_type,
                  ar.informasjon_id, arbeidsgiver_orgnr, ekstern_referanse,
                  count(distinct ar.intern_referanse)
                from IAY_ARBEIDSFORHOLD_REFER ar
                inner join gr_arbeid_inntekt g on (ar.informasjon_id=g.informasjon_id and g.aktiv='J')
                inner join kobling k on k.id=g.kobling_id
                where k.aktiv=true and k.ytelse_type=:ytelseType
                  and (k.opplysning_periode_fom IS NULL OR ( k.opplysning_periode_fom <= :tom AND k.opplysning_periode_tom >=:fom ))
                group by k.saksnummer, k.bruker_aktoer_id, k.ytelse_type, ar.informasjon_id, arbeidsgiver_orgnr, ekstern_referanse
                having count(distinct ar.intern_referanse) > 1;
            """;

        var query = entityManager.createNativeQuery(sql, Tuple.class)
            .setParameter("ytelseType", ytelseType.getKode())
            .setParameter("fom", periode.getFomDato())
            .setParameter("tom", periode.getTomDato()) // tar alt overlappende
            .setHint("javax.persistence.query.timeout", 2 * 60 * 1000) // 2:00 min
            ;
        String path = "duplikat-arbeidsforhold.csv";

        try (Stream<Tuple> stream = query.getResultStream()) {
            return CsvOutput.dumpResultSetToCsv(path, stream).map(List::of).orElse(List.of());
        }

    }

}
