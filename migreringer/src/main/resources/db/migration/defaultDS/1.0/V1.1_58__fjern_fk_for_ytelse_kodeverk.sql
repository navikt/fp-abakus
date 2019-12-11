alter table IAY_RELATERT_YTELSE drop constraint FK_YTELSE_1;
alter table IAY_RELATERT_YTELSE drop constraint FK_YTELSE_3;
alter table IAY_RELATERT_YTELSE drop constraint FK_YTELSE_5;

alter table VEDTAK_YTELSE drop constraint FK_VEDTAK_YTELSE_1;
alter table VEDTAK_YTELSE drop constraint FK_VEDTAK_YTELSE_3;
alter table VEDTAK_YTELSE drop constraint FK_VEDTAK_YTELSE_5;

alter table KOBLING drop constraint fk_kobling_1;

alter table IAY_OPPGITT_ARBEIDSFORHOLD drop constraint fk_oppgitt_arbeidsforhold_4;
alter table IAY_EGEN_NAERING drop constraint fk_egen_naering_4;


