alter table IAY_OPPGITT_ARBEIDSFORHOLD
    add column INNTEKT NUMERIC(10, 2);

comment on column IAY_OPPGITT_ARBEIDSFORHOLD.INNTEKT is 'Oppgitt inntekt for arbeidshold i perioden';
