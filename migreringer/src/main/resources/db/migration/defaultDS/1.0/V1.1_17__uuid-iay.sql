-- IAY_INNTEKT_ARBEID_YTELSER
alter table IAY_INNTEKT_ARBEID_YTELSER add column ekstern_referanse uuid;
create unique index UIDX_IAY_01 ON IAY_INNTEKT_ARBEID_YTELSER (ekstern_referanse);
COMMENT ON COLUMN IAY_INNTEKT_ARBEID_YTELSER.ekstern_referanse IS 'Unik UUID for behandling til utvortes bruk. Representerer en immutable og unikt identifiserbar instans av dette aggregatet';

-- IAY_OPPGITT_OPPTJENING
alter table IAY_OPPGITT_OPPTJENING add column ekstern_referanse uuid;
create unique index UIDX_IAY_OPPGITT_OPPTJE_01 ON IAY_OPPGITT_OPPTJENING (ekstern_referanse);
COMMENT ON COLUMN IAY_OPPGITT_OPPTJENING.ekstern_referanse IS 'Unik UUID for behandling til utvortes bruk. Representerer en immutable og unikt identifiserbar instans av disse opplysningene';
