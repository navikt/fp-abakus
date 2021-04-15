alter table IAY_OPPGITT_OPPTJENING add column journalpost_id        varchar(100) ;
alter table IAY_OPPGITT_OPPTJENING add column INNSENDINGSTIDSPUNKT  TIMESTAMP(6) ;

comment on column IAY_OPPGITT_OPPTJENING.journalpost_id is 'Journalposten dokument (søknad) med oppgitt opptjening er journalført på';
comment on column IAY_OPPGITT_OPPTJENING.INNSENDINGSTIDSPUNKT is 'Innsendingstidspunkt (sannsynligvis fra søknadsdialogen)';
