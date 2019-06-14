alter table IAY_YRKESAKTIVITET drop column arbeidsforhold_id;
alter table IAY_YRKESAKTIVITET add column arbeidsforhold_intern_id UUID;
comment on column IAY_YRKESAKTIVITET.arbeidsforhold_intern_id is 'Global unik arbeidsforhold id for intern bruk i FP familien';
create index IDX_YRKESAKTIVITET_5 on IAY_YRKESAKTIVITET (arbeidsforhold_intern_id);
create index IDX_YRKESAKTIVITET_6 on IAY_YRKESAKTIVITET (ARBEIDSGIVER_AKTOR_ID);
    
    
alter table IAY_ARBEIDSFORHOLD drop column arbeidsforhold_id;
alter table IAY_ARBEIDSFORHOLD add column arbeidsforhold_intern_id UUID;
comment on column IAY_ARBEIDSFORHOLD.arbeidsforhold_intern_id is 'Global unik arbeidsforhold id for intern bruk i FP familien';
create index IDX_ARBEIDSFORHOLD_5 on IAY_ARBEIDSFORHOLD (arbeidsforhold_intern_id);

alter table IAY_ARBEIDSFORHOLD drop column ny_arbeidsforhold_id;
alter table IAY_ARBEIDSFORHOLD add column arbeidsforhold_intern_id_ny UUID;
comment on column IAY_ARBEIDSFORHOLD.arbeidsforhold_intern_id_ny is 'Global unik arbeidsforhold id for intern bruk i FP familien';
create index IDX_ARBEIDSFORHOLD_6 on IAY_ARBEIDSFORHOLD (arbeidsforhold_intern_id_ny);
    
    
alter table IAY_INNTEKTSMELDING drop column arbeidsforhold_id;
alter table IAY_INNTEKTSMELDING add column arbeidsforhold_intern_id UUID;
comment on column IAY_INNTEKTSMELDING.arbeidsforhold_intern_id is 'Global unik arbeidsforhold id for intern bruk i FP familien';
create index IDX_INNTEKTSMELDING_3 on IAY_INNTEKTSMELDING (arbeidsforhold_intern_id);
    
    
alter table IAY_ARBEIDSFORHOLD_REFER drop column intern_referanse;
alter table IAY_ARBEIDSFORHOLD_REFER add column intern_referanse UUID NOT NULL;
comment on column IAY_ARBEIDSFORHOLD_REFER.intern_referanse is 'Global unik arbeidsforhold id for intern bruk i FP familien';
create index IDX_ARBEIDSFORHOLD_REFER_1 on IAY_ARBEIDSFORHOLD_REFER (INTERN_REFERANSE);