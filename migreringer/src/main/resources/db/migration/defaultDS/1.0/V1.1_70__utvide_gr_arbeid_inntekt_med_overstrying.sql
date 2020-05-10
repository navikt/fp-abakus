alter table gr_arbeid_inntekt add column OVERSTYRT_OPPGITT_OPPTJENING_ID bigint;

alter table gr_arbeid_inntekt add constraint FK_GR_ARBEID_INNTEKT_7 foreign key (OVERSTYRT_OPPGITT_OPPTJENING_ID) references IAY_OPPGITT_OPPTJENING;

create index IDX_GR_ARBEID_INNTEKT_8
    on gr_ARBEID_INNTEKT (OVERSTYRT_OPPGITT_OPPTJENING_ID);

comment on column gr_ARBEID_INNTEKT.OVERSTYRT_OPPGITT_OPPTJENING_ID is 'FK: Fremmenøkkel for kobling til overstyring av egen oppgitt opptjening';
