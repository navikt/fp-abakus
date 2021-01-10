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
    AKTIV                BOOLEAN   default true            not null
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

create sequence SEQ_LONNSKOMP_ANVIST
    increment by 50
    START WITH 1000000 NO CYCLE;

create table LONNSKOMP_ANVIST
(
    ID                      bigint                              not null
        constraint PK_LONNSKOMP_ANVIST
            primary key,
    VEDTAK_ID               bigint                              not null
        constraint FK_LONNSKOMP_ANVIST_1
            references LONNSKOMP_VEDTAK,
    BELOEP                  NUMERIC(19, 2),
    FOM                  DATE                                not null,
    TOM                  DATE                                not null,
    VERSJON                 bigint       default 0              not null,
    OPPRETTET_AV            VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID           TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV               VARCHAR(20),
    ENDRET_TID              TIMESTAMP(3)
);
comment on table LONNSKOMP_ANVIST is 'En tabell med informasjon om beløp pr måned';
comment on column LONNSKOMP_ANVIST.ID is 'PK';
comment on column LONNSKOMP_ANVIST.VEDTAK_ID is 'FK:YTELSE Fremmednøkkel til vedtakstabellen';
comment on column LONNSKOMP_ANVIST.BELOEP is 'Beløp ifm utbetaling.';
comment on column LONNSKOMP_ANVIST.FOM is 'Startdato for anvisning.';
comment on column LONNSKOMP_ANVIST.TOM is 'Sluttdato for anvisning';
create index IDX_LONNSKOMP_ANVIST_1
    on LONNSKOMP_ANVIST (VEDTAK_ID);
