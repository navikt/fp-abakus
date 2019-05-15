----------------------------------------------------------------------------------------
-- rename GR_ARBEID_INNTEKT.BEHANDLING_ID -> KOBLING_ID
----------------------------------------------------------------------------------------
alter table GR_ARBEID_INNTEKT rename column behandling_id to kobling_id;
comment on column gr_ARBEID_INNTEKT.KOBLING_ID is 'FK: Fremmednøkkel til KOBLING';


alter table GR_ARBEID_INNTEKT rename column referanse_id to grunnlag_referanse;

alter table KOBLING rename column referanse_id to kobling_referanse;






