create sequence SEQ_LONNSKOMP_FILTER
    increment by 50
    START WITH 1000000 NO CYCLE;

create table LONNSKOMP_FILTER
(
    ID                   bigint                              not null
        constraint PK_LONNSKOMP_FILTER
            primary key,
    SAKSNUMMER           VARCHAR(19)                         not null,
    KILDE                VARCHAR(100)                        not null,
    VERSJON              bigint       default 0              not null,
    OPPRETTET_AV         VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID        TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV            VARCHAR(20),
    ENDRET_TID           TIMESTAMP(3)
);
comment on table LONNSKOMP_FILTER is 'En tabell som styrer innhenting fra Lønnskompensasjon / Koronapenger';
comment on column LONNSKOMP_FILTER.ID is 'Primærnøkkel';
comment on column LONNSKOMP_FILTER.SAKSNUMMER is 'Saksnummer fra kobling';
comment on column LONNSKOMP_FILTER.KILDE is 'Inntektsfilter som skal hente inntekt';
create index IDX_LONNSKOMP_FILTER_1
    on LONNSKOMP_FILTER (SAKSNUMMER);
