create sequence SEQ_VEDTAK_YTELSE
    increment by 50
    START WITH 1000000 NO CYCLE;

create table VEDTAK_YTELSE
(
    ID                   bigint                              not null
        constraint PK_VEDTAK_YTELSE
            primary key,
    AKTOER_ID            VARCHAR(50)                         NOT NULL,
    YTELSE_TYPE          VARCHAR(100)                        not null,
    FOM                  DATE                                not null,
    TOM                  DATE                                not null,
    STATUS               VARCHAR(100)                        not null,
    KILDE                VARCHAR(100)                        not null,
    KL_YTELSE_TYPE       VARCHAR(100) default 'RELATERT_YTELSE_TYPE',
    KL_STATUS            VARCHAR(100) default 'RELATERT_YTELSE_TILSTAND',
    KL_KILDE             VARCHAR(100) default 'FAGSYSTEM',
    VERSJON              bigint       default 0              not null,
    OPPRETTET_AV         VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID        TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV            VARCHAR(20),
    ENDRET_TID           TIMESTAMP(3),
    TEMAUNDERKATEGORI    VARCHAR(100),
    KL_TEMAUNDERKATEGORI VARCHAR(100) default 'TEMA_UNDERKATEGORI',
    SAKSNUMMER           VARCHAR(19),
    constraint FK_VEDTAK_YTELSE_1
        foreign key (YTELSE_TYPE, KL_YTELSE_TYPE) references KODELISTE (KODE, KODEVERK),
    constraint FK_VEDTAK_YTELSE_2
        foreign key (STATUS, KL_STATUS) references KODELISTE (KODE, KODEVERK),
    constraint FK_VEDTAK_YTELSE_3
        foreign key (KILDE, KL_KILDE) references KODELISTE (KODE, KODEVERK),
    constraint FK_VEDTAK_YTELSE_5
        foreign key (TEMAUNDERKATEGORI, KL_TEMAUNDERKATEGORI) references KODELISTE (KODE, KODEVERK)
);
comment on table VEDTAK_YTELSE is 'En tabell med informasjon om ytelser fra Arena og Infotrygd';
comment on column VEDTAK_YTELSE.ID is 'Primærnøkkel';
comment on column VEDTAK_YTELSE.AKTOER_ID is 'Stønadsmottakeren';
comment on column VEDTAK_YTELSE.YTELSE_TYPE is 'Type ytelse for eksempel sykepenger, foreldrepenger.. (dagpenger?) etc';
comment on column VEDTAK_YTELSE.FOM is 'Startdato for ytelsten. Er tilsvarende Identdato fra Infotrygd.';
comment on column VEDTAK_YTELSE.TOM is 'Sluttdato er en utledet dato enten fra opphørFOM eller fra identdaot pluss periode';
comment on column VEDTAK_YTELSE.STATUS is 'Er om ytelsen er ÅPEN, LØPENDE eller AVSLUTTET';
comment on column VEDTAK_YTELSE.KILDE is 'Hvilket system informasjonen kommer fra';
comment on column VEDTAK_YTELSE.KL_YTELSE_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column VEDTAK_YTELSE.KL_STATUS is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column VEDTAK_YTELSE.KL_KILDE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column VEDTAK_YTELSE.TEMAUNDERKATEGORI is 'Fremmednøkkel til kodeverktabellen for beskrivelser av underkategori fra Infotrygd';
comment on column VEDTAK_YTELSE.KL_TEMAUNDERKATEGORI is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column VEDTAK_YTELSE.SAKSNUMMER is 'Saksnummer i GSAK';
create index IDX_VEDTAK_YTELSE_1
    on VEDTAK_YTELSE (AKTOER_ID);
create index IDX_VEDTAK_YTELSE_6
    on VEDTAK_YTELSE (YTELSE_TYPE);
create index IDX_VEDTAK_YTELSE_7
    on VEDTAK_YTELSE (STATUS);
create index IDX_VEDTAK_YTELSE_8
    on VEDTAK_YTELSE (KILDE);
create index IDX_VEDTAK_YTELSE_9
    on VEDTAK_YTELSE (TEMAUNDERKATEGORI);

create sequence SEQ_VEDTAK_YTELSE_ANVIST
    increment by 50
    START WITH 1000000 NO CYCLE;

create table VE_YTELSE_ANVIST
(
    ID                      bigint                              not null
        constraint PK_VE_YTELSE_ANVIST
            primary key,
    YTELSE_ID               bigint                              not null
        constraint FK_VE_YTELSE_ANVIST_1
            references VEDTAK_YTELSE,
    BELOEP                  NUMERIC(19,2),
    FOM                     DATE                                not null,
    TOM                     DATE                                not null,
    UTBETALINGSGRAD_PROSENT NUMERIC(5,2),
    VERSJON                 bigint       default 0              not null,
    OPPRETTET_AV            VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID           TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV               VARCHAR(20),
    ENDRET_TID              TIMESTAMP(3),
    DAGSATS                 NUMERIC(19,2)
);
comment on table VE_YTELSE_ANVIST is 'En tabell med informasjon om ytelsesperioder';
comment on column VE_YTELSE_ANVIST.ID is 'PK';
comment on column VE_YTELSE_ANVIST.YTELSE_ID is 'FK:YTELSE Fremmednøkkel til kodeverktabellen over ytelser???';
comment on column VE_YTELSE_ANVIST.BELOEP is 'Beløp ifm utbetaling.';
comment on column VE_YTELSE_ANVIST.FOM is 'Anvist periode første dag.';
comment on column VE_YTELSE_ANVIST.TOM is 'Anvist periode siste dag.';
comment on column VE_YTELSE_ANVIST.UTBETALINGSGRAD_PROSENT is 'Utbetalingsprosent fra kildesystem.';
comment on column VE_YTELSE_ANVIST.DAGSATS is 'Dagsatsen på den relaterte ytelsen';
create index IDX_VE_YTELSE_ANVIST_1
    on VE_YTELSE_ANVIST (YTELSE_ID);
