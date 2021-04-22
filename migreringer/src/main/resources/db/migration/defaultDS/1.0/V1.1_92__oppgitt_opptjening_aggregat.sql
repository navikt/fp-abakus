alter table IAY_OPPGITT_OPPTJENING add column oppgitte_opptjeninger_id bigint;
alter table IAY_OPPGITT_OPPTJENING add column journalpost_id        varchar(100);
alter table IAY_OPPGITT_OPPTJENING add column INNSENDINGSTIDSPUNKT  TIMESTAMP(6);

alter table GR_ARBEID_INNTEKT add column oppgitte_opptjeninger_id bigint;

create table IAY_OPPGITTE_OPPTJENINGER (
    ID            bigint                              not null constraint PK_IAY_OPPGITTE_OPPTJENINGER primary key,
    VERSJON       bigint       default 0              not null,
    OPPRETTET_AV  VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV     VARCHAR(20),
    ENDRET_TID    TIMESTAMP(3)
);
comment on table IAY_OPPGITTE_OPPTJENINGER is 'Koblingstabell mellom grunnlag og oppgitte opptjeninger';
comment on column IAY_OPPGITTE_OPPTJENINGER.ID is 'Primærnøkkel';

CREATE SEQUENCE SEQ_IAY_OPPGITTE_OPPTJENINGER MINVALUE 1 START WITH 1000000 INCREMENT BY 50 NO CYCLE;

comment on column IAY_OPPGITT_OPPTJENING.oppgitte_opptjeninger_id is 'Fremmednøkkel til IAY_OPPGITTE_OPPTJENINGER';
comment on column IAY_OPPGITT_OPPTJENING.journalpost_id is 'Journalposten dokument (søknad) med oppgitt opptjening er journalført på';
comment on column IAY_OPPGITT_OPPTJENING.INNSENDINGSTIDSPUNKT is 'Innsendingstidspunkt (sannsynligvis fra søknadsdialogen)';

comment on column GR_ARBEID_INNTEKT.oppgitte_opptjeninger_id is 'Kobling mellom grunnlag og oppgitt opptjeninger-aggregat';
