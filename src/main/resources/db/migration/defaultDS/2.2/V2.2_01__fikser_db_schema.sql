alter table iay_oppgitt_arbeidsforhold rename constraint "fk_arbeidsforhold_1" TO "fk_oppgitt_arbeidsforhold_1";

alter table diagnostikk_logg rename constraint "diagnostikk_logg_pkey" to "pk_diagnostikk_logg_id";

alter index diagnostikk_logg_saksnummer_idx rename to "idx_diagnostikk_logg_saksnummer";

comment on table diagnostikk_logg is 'Inneholder logger av alle diagnostikk oppslagg fra forvaltning diagnostikk api.';
comment on column diagnostikk_logg.saksnummer is 'Saksnummer som ble slått opp.';
comment on column iay_annen_aktivitet.arbeid_type is 'Arbeidstype';
comment on column iay_arbeidsforhold.handling_type is 'Handlingstype';
comment on column iay_egen_naering.land is 'Land bedriften er registrert i.';
comment on column iay_egen_naering.virksomhet_type is 'Type virksomhet.';
comment on column iay_fravaer.varighet_per_dag is 'Varighet per dag.';
comment on column iay_inntekt.kilde is 'Kilde til inntekten.';
comment on column iay_inntektsmelding.innsendingsaarsak is 'Årsak til innsending av inntektsmelding.';
comment on column iay_oppgitt_arbeidsforhold.land is 'I hvilket land er arbeidsforholdet registrert.';
comment on column lonnskomp_vedtak.fnr is 'Bruker som har fått vedtaket.';

