create sequence if not exists SEQ_YTELSE_ANVIST_ANDEL
    increment by 50
    START WITH 1000000 NO CYCLE;

create table if not exists IAY_YTELSE_ANVIST_ANDEL
(
    ID                      bigint                              not null
        constraint PK_IAY_YTELSE_ANVIST_ANDEL
            primary key,
    YTELSE_ANVIST_ID               bigint                              not null
        constraint FK_IAY_YTELSE_ANVIST_ANDEL_1
            references IAY_YTELSE_ANVIST,
    DAGSATS                     NUMERIC(19, 2),
    UTBETALINGSGRAD_PROSENT     NUMERIC(5, 2),
    REFUSJONSGRAD_PROSENT       NUMERIC(5, 2),
    ARBEIDSGIVER_AKTOR_ID       VARCHAR(100),
    ARBEIDSGIVER_ORGNR          VARCHAR(100),
    ARBEIDSFORHOLD_INTERN_ID           VARCHAR(100),
    INNTEKTSKATEGORI            VARCHAR(100),
    VERSJON                     bigint       default 0              not null,
    OPPRETTET_AV                VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID               TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV                   VARCHAR(20),
    ENDRET_TID                  TIMESTAMP(3)
);
comment
on table IAY_YTELSE_ANVIST_ANDEL is 'En tabell med informasjon om fordeling for en ytelseperiode';
comment
on column IAY_YTELSE_ANVIST_ANDEL.ID is 'PK';
comment
on column IAY_YTELSE_ANVIST_ANDEL.YTELSE_ANVIST_ID is 'FK:YTELSE_ANVIST Fremmednøkkel til ytelseperioden';
comment
on column IAY_YTELSE_ANVIST_ANDEL.DAGSATS is 'Dagsats ifm utbetaling.';
comment
on column IAY_YTELSE_ANVIST_ANDEL.UTBETALINGSGRAD_PROSENT is 'Utbetalingsgrad for dagsats.';
comment
on column IAY_YTELSE_ANVIST_ANDEL.REFUSJONSGRAD_PROSENT is 'Andel av dagsats som betales til arbeidsgiver i refusjon';
comment
on column IAY_YTELSE_ANVIST_ANDEL.ARBEIDSGIVER_AKTOR_ID is 'Arbeidsgiver aktør-id.';
comment
on column IAY_YTELSE_ANVIST_ANDEL.ARBEIDSGIVER_ORGNR is 'Arbeidsgiver orgnr';
comment
on column IAY_YTELSE_ANVIST_ANDEL.ARBEIDSFORHOLD_INTERN_ID is  'Arbeidsforhold intern id';
comment
on column IAY_YTELSE_ANVIST_ANDEL.INNTEKTSKATEGORI is 'Inntektskategori for utbetalingen';

create index IDX_IAY_YTELSE_ANVIST_ANDEL_1
    on IAY_YTELSE_ANVIST_ANDEL (YTELSE_ANVIST_ID);
