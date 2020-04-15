alter table IAY_OPPGITT_FRILANSOPPDRAG
    add column INNTEKT NUMERIC(10, 2);

comment on column IAY_OPPGITT_FRILANSOPPDRAG.INNTEKT is 'Frilansinntekt i perioden';
