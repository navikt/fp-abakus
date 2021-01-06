create sequence SEQ_LONNSKOMP_VEDTAK
    increment by 50
    START WITH 1000000 NO CYCLE;

create table LONNSKOMP_VEDTAK
(
    ID                   bigint                              not null
        constraint PK_LONNSKOMP_VEDTAK
            primary key,
    SAKID                VARCHAR(100)                        not null,
    FORRIGE_VEDTAK_DATO  DATE,
    AKTOER_ID            VARCHAR(50)                         NOT NULL,
    ORG_NUMMER           VARCHAR(100)                        not null,
    BELOEP               NUMERIC(19, 2)                      NOT NULL,
    FOM                  DATE                                not null,
    TOM                  DATE                                not null,
    VERSJON              bigint       default 0              not null,
    OPPRETTET_AV         VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID        TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV            VARCHAR(20),
    ENDRET_TID           TIMESTAMP(3),
    AKTIV                VARCHAR(1)   default 'J'            not null,
    constraint CHK_VEDTAK_YTELSE_AKTIV
        check (aktiv IN ('J', 'N'))
);
comment on table LONNSKOMP_VEDTAK is 'En tabell med informasjon om Lønnskompensasjon / Koronapenger';
comment on column LONNSKOMP_VEDTAK.ID is 'Primærnøkkel';
comment on column LONNSKOMP_VEDTAK.AKTOER_ID is 'Stønadsmottakeren';
comment on column LONNSKOMP_VEDTAK.ORG_NUMMER is 'Arbeidsgiver som har permittert';
comment on column LONNSKOMP_VEDTAK.BELOEP is 'Sum utbetalt Stønadsmottakeren';
comment on column LONNSKOMP_VEDTAK.FOM is 'Startdato for ytelsen.';
comment on column LONNSKOMP_VEDTAK.TOM is 'Sluttdato for ytelsen';
comment on column LONNSKOMP_VEDTAK.SAKID is 'Saksnummer i kildesystem';
comment on column LONNSKOMP_VEDTAK.FORRIGE_VEDTAK_DATO is 'Revurderinger har dato for forrige vedtak';
comment on column LONNSKOMP_VEDTAK.AKTIV is 'Er innslaget aktivt';
create index IDX_LONNSKOMP_VEDTAK_1
    on LONNSKOMP_VEDTAK (SAKID);
create index IDX_LONNSKOMP_VEDTAK_2
    on LONNSKOMP_VEDTAK (AKTOER_ID);
create index IDX_LONNSKOMP_VEDTAK_10
    on LONNSKOMP_VEDTAK (AKTIV);
