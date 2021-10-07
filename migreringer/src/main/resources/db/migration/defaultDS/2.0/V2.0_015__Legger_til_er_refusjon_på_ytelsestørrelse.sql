alter table IAY_YTELSE_STOERRELSE add column er_refusjon BOOLEAN;

comment on column IAY_YTELSE_STOERRELSE.er_refusjon is 'Sier om beløpet er refusjon eller direkteutbetaling';
