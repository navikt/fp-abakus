create table KOBLING
(
  ID                     bigint                              not null,
  referanse_id           varchar(150)                        not null,
  bruker_aktoer_id       varchar(50)                         not null,
  annen_part_aktoer_id   varchar(50),
  opplysning_periode_fom DATE                                not null,
  opplysning_periode_tom DATE                                not null,
  opptjening_periode_fom DATE,
  opptjening_periode_tom DATE,
  VERSJON                bigint       default 0              not null,
  OPPRETTET_AV           VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID          TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV              VARCHAR(20),
  ENDRET_TID             TIMESTAMP(3),
  constraint PK_KOBLING
    primary key (ID),
  constraint UIDX_KOBLING_1
    unique (referanse_id)
);
CREATE SEQUENCE SEQ_KOBLING MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 50 START WITH 1368400 NO CYCLE;
create index IDX_KOBLING_1
  on KOBLING (referanse_id);

comment on table KOBLING is 'Holder referansen som kalles på fra av eksternt system';
comment on column KOBLING.ID is 'Primærnøkkel';
comment on column KOBLING.referanse_id is 'Referansenøkkel som eksponeres lokalt';
comment on column KOBLING.bruker_aktoer_id is 'Aktøren koblingen gjelder for';
comment on column KOBLING.annen_part_aktoer_id is 'Annen part det skal hentes informasjon for';
comment on column KOBLING.opplysning_periode_fom is 'Start på perioden det skal hentes data for';
comment on column KOBLING.opplysning_periode_tom is 'Slutt på perioden det skal hentes data for';
comment on column KOBLING.opptjening_periode_fom is 'Start på opptjeningsperioden det skal vurere på';
comment on column KOBLING.opptjening_periode_tom is 'Slutt på opptjeningsperioden det skal vurere på';

create sequence SEQ_GR_ARBEID_INNTEKT
  minvalue 1
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_INNTEKT
  minvalue 1
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_INNTEKT_ARBEID_YTELSER
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_AKTOER_INNTEKT
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_INNTEKTSPOST
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_AKTOER_ARBEID
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_YRKESAKTIVITET
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_PERMISJON
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_AKTIVITETS_AVTALE
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_INNTEKTSMELDINGER
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_INNTEKTSMELDING
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_NATURAL_YTELSE
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_OPPGITT_ARBEIDSFORHOLD
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_EGEN_NAERING
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_ANNEN_AKTIVITET
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_AKTOER_YTELSE
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_YTELSE
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_YTELSE_GRUNNLAG
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_YTELSE_STOERRELSE
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_YTELSE_ANVIST
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_IAY_INFORMASJON
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_IAY_ARBEIDSFORHOLD
  increment by 50
  START WITH 1000000 NO CYCLE;
create sequence SEQ_IAY_ARBEIDSFORHOLD_REFER
  increment by 50
  START WITH 1000000 NO CYCLE;
create table VIRKSOMHET
(
  ID                         bigint                              not null,
  ORGNR                      VARCHAR(100)                        not null,
  NAVN                       VARCHAR(400),
  REGISTRERT                 DATE,
  OPPSTART                   DATE,
  AVSLUTTET                  DATE,
  VERSJON                    bigint       default 0              not null,
  OPPRETTET_AV               VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID              TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                  VARCHAR(20),
  ENDRET_TID                 TIMESTAMP(3),
  OPPLYSNINGER_OPPDATERT_TID TIMESTAMP(3) default NULL           not null,
  ORGANISASJONSTYPE          VARCHAR(100) default '-'            not null,
  KL_ORGANISASJONSTYPE       VARCHAR(100) default 'ORGANISASJONSTYPE',
  constraint PK_VIRKSOMHET
    primary key (ID),
  constraint FK_VIRKSOMHET_1
    foreign key (ORGANISASJONSTYPE, KL_ORGANISASJONSTYPE) references KODELISTE (KODE, KODEVERK)
);
CREATE SEQUENCE SEQ_VIRKSOMHET MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 50 START WITH 1000000 NO CYCLE;
comment on table VIRKSOMHET is 'Virksomhet fra enhetsregisteret';
comment on column VIRKSOMHET.ID is 'Primærnøkkel';
comment on column VIRKSOMHET.ORGNR is 'Bedriftens unike identifikator i enhetsregisteret';
comment on column VIRKSOMHET.NAVN is 'Bedriftens navn i enhetsregisteret';
comment on column VIRKSOMHET.REGISTRERT is 'Når virksomheten ble registrert i enhetsregisteret';
comment on column VIRKSOMHET.OPPSTART is 'Når næringen startet opp';
comment on column VIRKSOMHET.AVSLUTTET is 'Når næringen opphørte';
comment on column VIRKSOMHET.OPPLYSNINGER_OPPDATERT_TID is 'Siste tidspunkt for forespørsel til enhetsregisteret';
comment on column VIRKSOMHET.ORGANISASJONSTYPE is 'Organisasjonstype';
comment on column VIRKSOMHET.KL_ORGANISASJONSTYPE is 'Peker til kodeverk';
create index IDX_VIRKSOMHET_1
  on VIRKSOMHET (ORGNR);
create index IDX_VIRKSOMHET_2
  on VIRKSOMHET (ORGANISASJONSTYPE);
alter table VIRKSOMHET
  add constraint UIDX_VIRKSOMHET_1
    unique (ORGNR);


create table INNTEKT_ARBEID_YTELSER
(
  ID            bigint                              not null
    constraint PK_INNTEKT_ARBEID_YTELSER
      primary key,
  VERSJON       bigint       default 0              not null,
  OPPRETTET_AV  VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV     VARCHAR(20),
  ENDRET_TID    TIMESTAMP(3)
);
comment on table INNTEKT_ARBEID_YTELSER is 'Mange til mange tabell for inntekt, arbeid, ytelser.';
comment on column INNTEKT_ARBEID_YTELSER.ID is 'Primærnøkkel';
create table AKTOER_INNTEKT
(
  ID                        bigint                              not null
    constraint PK_AKTOER_INNTEKT
      primary key,
  INNTEKT_ARBEID_YTELSER_ID bigint                              not null
    constraint FK_AKTOER_INNTEKT_1
      references INNTEKT_ARBEID_YTELSER,
  VERSJON                   bigint       default 0              not null,
  OPPRETTET_AV              VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID             TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                 VARCHAR(20),
  ENDRET_TID                TIMESTAMP(3),
  AKTOER_ID                 VARCHAR(50)
);
comment on table AKTOER_INNTEKT is 'Tabell med rad per aktør med inntekt relatert til behandlingen.';
comment on column AKTOER_INNTEKT.ID is 'Primærnøkkel';
comment on column AKTOER_INNTEKT.INNTEKT_ARBEID_YTELSER_ID is 'FK:';
comment on column AKTOER_INNTEKT.AKTOER_ID is 'Aktørid (fra NAV Aktørregister)';
create index IDX_AKTOER_INNTEKT_2
  on AKTOER_INNTEKT (INNTEKT_ARBEID_YTELSER_ID);
create index IDX_AKTOER_INNTEKT_1
  on AKTOER_INNTEKT (AKTOER_ID);
create table INNTEKT
(
  ID                         bigint                              not null
    constraint PK_TMP_INNTEKT
      primary key,
  AKTOER_INNTEKT_ID          bigint                              not null
    constraint FK_INNTEKT_2
      references AKTOER_INNTEKT,
  KILDE                      VARCHAR(100),
  KL_KILDE                   VARCHAR(100) default 'INNTEKTS_KILDE',
  OPPRETTET_AV               VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID              TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                  VARCHAR(20),
  ENDRET_TID                 TIMESTAMP(3),
  ARBEIDSGIVER_AKTOR_ID      VARCHAR(100),
  ARBEIDSGIVER_VIRKSOMHET_ID bigint
    constraint FK_INNTEKT_1
      references VIRKSOMHET,
  constraint FK_INNTEKT_3
    foreign key (KILDE, KL_KILDE) references KODELISTE (KODE, KODEVERK)
);
comment on table INNTEKT is 'Inntekter per virksomhet';
comment on column INNTEKT.ID is 'Primærnøkkel';
comment on column INNTEKT.AKTOER_INNTEKT_ID is 'FK:';
comment on column INNTEKT.KL_KILDE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column INNTEKT.ARBEIDSGIVER_AKTOR_ID is 'Aktøren referansen til den personligebedriften som har utbetalt inntekt.';
comment on column INNTEKT.ARBEIDSGIVER_VIRKSOMHET_ID is 'Fjernnøkkel virksomhet';
create index IDX_INNTEKT_1
  on INNTEKT (AKTOER_INNTEKT_ID);
create index IDX_INNTEKT_2
  on INNTEKT (KILDE);
create index IDX_INNTEKT_3
  on INNTEKT (ARBEIDSGIVER_VIRKSOMHET_ID);
create index IDX_INNTEKT_4
  on INNTEKT (ARBEIDSGIVER_AKTOR_ID);
create table INNTEKTSPOST
(
  ID                             bigint                              not null
    constraint PK_INNTEKTSPOST
      primary key,
  INNTEKT_ID                     bigint                              not null
    constraint FK_INNTEKTSPOST_1
      references INNTEKT,
  INNTEKTSPOST_TYPE              VARCHAR(100)                        not null,
  KL_INNTEKTSPOST_TYPE           VARCHAR(100) default 'INNTEKTSPOST_TYPE',
  FOM                            DATE                                not null,
  TOM                            DATE                                not null,
  BELOEP                         NUMERIC(19,2)                       not null,
  VERSJON                        bigint       default 0              not null,
  OPPRETTET_AV                   VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID                  TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                      VARCHAR(20),
  ENDRET_TID                     TIMESTAMP(3),
  YTELSE_TYPE                    VARCHAR(100),
  KL_YTELSE_TYPE                 VARCHAR(100),
  SKATTE_OG_AVGIFTSREGEL_TYPE    VARCHAR(100) default '-'            not null,
  KL_SKATTE_OG_AVGIFTSREGEL_TYPE VARCHAR(100) default 'SKATTE_OG_AVGIFTSREGEL',
  constraint FK_INNTEKTSPOST_80
    foreign key (SKATTE_OG_AVGIFTSREGEL_TYPE, KL_SKATTE_OG_AVGIFTSREGEL_TYPE) references KODELISTE (KODE, KODEVERK),
  constraint FK_INNTEKTSPOST_2
    foreign key (YTELSE_TYPE, KL_YTELSE_TYPE) references KODELISTE (KODE, KODEVERK),
  constraint FK_INNTEKTSPOST_3
    foreign key (INNTEKTSPOST_TYPE, KL_INNTEKTSPOST_TYPE) references KODELISTE (KODE, KODEVERK)
);
comment on table INNTEKTSPOST is 'Utbetaling per type per periode';
comment on column INNTEKTSPOST.ID is 'Primærnøkkel';
comment on column INNTEKTSPOST.INNTEKT_ID is 'FK: Fremmednøkkel til kodeverkstabellen for inntektsposttyper';
comment on column INNTEKTSPOST.INNTEKTSPOST_TYPE is 'Type utbetaling, lønn eller ytelse';
comment on column INNTEKTSPOST.KL_INNTEKTSPOST_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column INNTEKTSPOST.BELOEP is 'Utbetalt beløp';
comment on column INNTEKTSPOST.YTELSE_TYPE is 'Fremmednøkkel til tabell for ytelsestyper';
comment on column INNTEKTSPOST.KL_YTELSE_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column INNTEKTSPOST.SKATTE_OG_AVGIFTSREGEL_TYPE is 'Skatte -og avgiftsregel fra inntektskomponenten';
comment on column INNTEKTSPOST.KL_SKATTE_OG_AVGIFTSREGEL_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
create index IDX_INNTEKTPOST_1
  on INNTEKTSPOST (INNTEKT_ID);
create index IDX_INNTEKTPOST_2
  on INNTEKTSPOST (INNTEKTSPOST_TYPE);
create index IDX_INNTEKTSPOST_6
  on INNTEKTSPOST (YTELSE_TYPE);
create index IDX_INNTEKTSPOST_2
  on INNTEKTSPOST (SKATTE_OG_AVGIFTSREGEL_TYPE);
create table AKTOER_ARBEID
(
  ID                        bigint                              not null
    constraint PK_AKTOER_ARBEID
      primary key,
  INNTEKT_ARBEID_YTELSER_ID bigint                              not null
    constraint FK_AKTOER_ARBEID_1
      references INNTEKT_ARBEID_YTELSER,
  VERSJON                   bigint       default 0              not null,
  OPPRETTET_AV              VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID             TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                 VARCHAR(20),
  ENDRET_TID                TIMESTAMP(3),
  AKTOER_ID                 VARCHAR(50)
);
comment on table AKTOER_ARBEID is 'Tabell med rad per aktør med arbeid eller aktiviteter som er likestilt pensjonsgivende arbeid relatert til behandlingen.';
comment on column AKTOER_ARBEID.ID is 'Primærnøkkel';
comment on column AKTOER_ARBEID.INNTEKT_ARBEID_YTELSER_ID is 'FK:';
comment on column AKTOER_ARBEID.AKTOER_ID is 'Aktørid (fra NAV Aktørregister)';
create index IDX_AKTOER_ARBEID_2
  on AKTOER_ARBEID (INNTEKT_ARBEID_YTELSER_ID);
create index IDX_AKTOER_ARBEID_1
  on AKTOER_ARBEID (AKTOER_ID);
create table YRKESAKTIVITET
(
  ID                         bigint                              not null
    constraint PK_YRKESAKTIVITET
      primary key,
  AKTOER_ARBEID_ID           bigint                              not null
    constraint FK_YRKESAKTIVITET_1
      references AKTOER_ARBEID,
  ARBEIDSGIVER_AKTOR_ID      VARCHAR(100),
  ARBEIDSFORHOLD_ID          VARCHAR(100) default NULL,
  ARBEIDSGIVER_VIRKSOMHET_ID bigint
    constraint FK_YRKESAKTIVITET_3
      references VIRKSOMHET,
  ARBEID_TYPE                VARCHAR(100)                        not null,
  KL_ARBEID_TYPE             VARCHAR(100) default 'ARBEID_TYPE',
  VERSJON                    bigint       default 0              not null,
  OPPRETTET_AV               VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID              TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                  VARCHAR(20),
  ENDRET_TID                 TIMESTAMP(3),
  NAVN_ARBEIDSGIVER_UTLAND   VARCHAR(100),
  constraint FK_YRKESAKTIVITET_2
    foreign key (ARBEID_TYPE, KL_ARBEID_TYPE) references KODELISTE (KODE, KODEVERK)
);
comment on table YRKESAKTIVITET is 'Arbeid eller aktiviteter som er likestilt pensjonsgivende arbeid';
comment on column YRKESAKTIVITET.ID is 'Primærnøkkel';
comment on column YRKESAKTIVITET.AKTOER_ARBEID_ID is 'FK:';
comment on column YRKESAKTIVITET.ARBEIDSGIVER_AKTOR_ID is 'Aktøren referansen til den personligebedriften som har ansatt vedkommende.';
comment on column YRKESAKTIVITET.ARBEIDSFORHOLD_ID is 'FK:';
comment on column YRKESAKTIVITET.ARBEIDSGIVER_VIRKSOMHET_ID is 'FK:';
comment on column YRKESAKTIVITET.ARBEID_TYPE is 'Fremmednøkkel til tabell over arbeidstyper';
comment on column YRKESAKTIVITET.KL_ARBEID_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column YRKESAKTIVITET.NAVN_ARBEIDSGIVER_UTLAND is 'Navn på utenlandske arbeidsgiver';
create index IDX_YRKESAKTIVITET_1
  on YRKESAKTIVITET (AKTOER_ARBEID_ID);
create index IDX_YRKESAKTIVITET_2
  on YRKESAKTIVITET (ARBEID_TYPE);
create index IDX_YRKESAKTIVITET_3
  on YRKESAKTIVITET (ARBEIDSGIVER_VIRKSOMHET_ID);
create index IDX_YRKESAKTIVITET_4
  on YRKESAKTIVITET (ARBEIDSGIVER_AKTOR_ID);
create index IDX_YRKESAKTIVITET_5
  on YRKESAKTIVITET (ARBEIDSGIVER_VIRKSOMHET_ID, ARBEIDSFORHOLD_ID);
create table PERMISJON
(
  ID                  bigint                              not null
    constraint PK_PERMISJON
      primary key,
  YRKESAKTIVITET_ID   bigint                              not null
    constraint FK_PERMISJON_1
      references YRKESAKTIVITET,
  BESKRIVELSE_TYPE    VARCHAR(100)                        not null,
  KL_BESKRIVELSE_TYPE VARCHAR(100) default 'PERMISJONSBESKRIVELSE_TYPE',
  FOM                 DATE                                not null,
  TOM                 DATE                                not null,
  PROSENTSATS         NUMERIC(5,2)                        not null,
  VERSJON             bigint       default 0              not null,
  OPPRETTET_AV        VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID       TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV           VARCHAR(20),
  ENDRET_TID          TIMESTAMP(3),
  constraint FK_PERMISJON_2
    foreign key (BESKRIVELSE_TYPE, KL_BESKRIVELSE_TYPE) references KODELISTE (KODE, KODEVERK)
);
comment on table PERMISJON is 'Oversikt over avtalt permisjon hos arbeidsgiver';
comment on column PERMISJON.ID is 'Primærnøkkel';
comment on column PERMISJON.YRKESAKTIVITET_ID is 'FK:';
comment on column PERMISJON.BESKRIVELSE_TYPE is 'FK: Fremmednøkkel til tabell for beskrivelse av permisjonstyper';
comment on column PERMISJON.KL_BESKRIVELSE_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column PERMISJON.PROSENTSATS is 'Antall prosent permisjon';
create index IDX_PERMISJON_1
  on PERMISJON (YRKESAKTIVITET_ID);
create index IDX_PERMISJON_6
  on PERMISJON (BESKRIVELSE_TYPE);
create table AKTIVITETS_AVTALE
(
  ID                       bigint                              not null
    constraint PK_AKTIVITETS_AVTALE
      primary key,
  YRKESAKTIVITET_ID        bigint                              not null
    constraint FK_AKTIVITETS_AVTALE_1
      references YRKESAKTIVITET,
  PROSENTSATS              NUMERIC(5,2),
  FOM                      DATE                                not null,
  TOM                      DATE                                not null,
  VERSJON                  bigint       default 0              not null,
  OPPRETTET_AV             VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID            TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                VARCHAR(20),
  ENDRET_TID               TIMESTAMP(3),
  BESKRIVELSE              VARCHAR(1000),
  SISTE_LOENNSENDRINGSDATO DATE,
  ANTALL_TIMER             NUMERIC(6,2),
  ANTALL_TIMER_FULLTID     NUMERIC(6,2)
);
comment on table AKTIVITETS_AVTALE is 'Ansettelses avtaler og avtaler om periode av en gitt type.';
comment on column AKTIVITETS_AVTALE.ID is 'Primærnøkkel';
comment on column AKTIVITETS_AVTALE.YRKESAKTIVITET_ID is 'FK:';
comment on column AKTIVITETS_AVTALE.PROSENTSATS is 'Stillingsprosent';
comment on column AKTIVITETS_AVTALE.BESKRIVELSE is 'Saksbehandlers vurdering om perioden. Forekommer data her kun hvis den ligger i overstyrt.';
comment on column AKTIVITETS_AVTALE.SISTE_LOENNSENDRINGSDATO is 'Beskriver siste lønnsendringsdato';
comment on column AKTIVITETS_AVTALE.ANTALL_TIMER is 'Antall timer med avtalt arbeid';
comment on column AKTIVITETS_AVTALE.ANTALL_TIMER_FULLTID is 'Antall timer som tilsvarer full stilling';
create index IDX_AKTIVITETS_AVTALE_1
  on AKTIVITETS_AVTALE (YRKESAKTIVITET_ID);
create table INNTEKTSMELDINGER
(
  ID            bigint                              not null
    constraint PK_INNTEKTSMELDINGER
      primary key,
  VERSJON       bigint       default 0              not null,
  OPPRETTET_AV  VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV     VARCHAR(20),
  ENDRET_TID    TIMESTAMP(3)
);
comment on table INNTEKTSMELDINGER is 'Koblingstabell mellom grunnlag og inntektsmeldinger';
comment on column INNTEKTSMELDINGER.ID is 'Primærnøkkel';
create table INNTEKTSMELDING
(
  ID                         bigint                              not null
    constraint PK_INNTEKTSMELDING
      primary key,
  INNTEKTSMELDINGER_ID       bigint                              not null
    constraint FK_INNTEKTSMELDING_1
      references INNTEKTSMELDINGER,
  MOTTATT_DOKUMENT_ID        bigint                              not null,
  VERSJON                    bigint       default 0              not null,
  ARBEIDSGIVER_VIRKSOMHET_ID bigint
    constraint FK_INNTEKTSMELDING_2
      references VIRKSOMHET,
  ARBEIDSFORHOLD_ID          VARCHAR(200),
  INNTEKT_BELOEP             NUMERIC(10,2)                       not null,
  START_DATO_PERMISJON       DATE                                not null,
  REFUSJON_BELOEP            NUMERIC(10,2),
  REFUSJON_OPPHOERER         DATE,
  NAER_RELASJON              VARCHAR(1)                          not null
    constraint CHK_FK_INNTEKTSMELDING_1
      check (naer_relasjon IN ('J', 'N')),
  OPPRETTET_AV               VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID              TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                  VARCHAR(20),
  ENDRET_TID                 TIMESTAMP(3),
  INNSENDINGSAARSAK          VARCHAR(100) default '-'            not null,
  KL_INNSENDINGSAARSAK       VARCHAR(100) default 'INNTEKTSMELDING_INNSENDINGSAARSAK',
  INNSENDINGSTIDSPUNKT       TIMESTAMP(6)                        not null,
  ARBEIDSGIVER_AKTOR_ID      VARCHAR(100),
  constraint FK_INNTEKTSMELDING_3
    foreign key (INNSENDINGSAARSAK, KL_INNSENDINGSAARSAK) references KODELISTE (KODE, KODEVERK)
);
comment on table INNTEKTSMELDING is 'Inntektsmeldinger';
comment on column INNTEKTSMELDING.ID is 'Primærnøkkel';
comment on column INNTEKTSMELDING.INNTEKTSMELDINGER_ID is 'FK:';
comment on column INNTEKTSMELDING.MOTTATT_DOKUMENT_ID is 'FK:';
comment on column INNTEKTSMELDING.ARBEIDSGIVER_VIRKSOMHET_ID is 'FK:';
comment on column INNTEKTSMELDING.ARBEIDSFORHOLD_ID is 'FK:';
comment on column INNTEKTSMELDING.INNTEKT_BELOEP is 'Oppgitt årslønn fra arbeidsgiver';
comment on column INNTEKTSMELDING.START_DATO_PERMISJON is 'Avtalt startdato for permisjonen fra arbeidsgiver';
comment on column INNTEKTSMELDING.REFUSJON_BELOEP is 'Beløpet arbeidsgiver ønsker refundert';
comment on column INNTEKTSMELDING.REFUSJON_OPPHOERER is 'Dato for når refusjonen opphører';
comment on column INNTEKTSMELDING.NAER_RELASJON is 'Arbeidsgiver oppgir at søker har nær relasjon til arbeidsgiver';
comment on column INNTEKTSMELDING.KL_INNSENDINGSAARSAK is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column INNTEKTSMELDING.INNSENDINGSTIDSPUNKT is 'Innsendingstidspunkt fra LPS-system. For Altinn bruker kjøretidspunkt';
comment on column INNTEKTSMELDING.ARBEIDSGIVER_AKTOR_ID is 'AktørID i de tilfeller hvor arbeidsgiver er en person.';
create index IDX_INNTEKTSMELDING_1
  on INNTEKTSMELDING (INNTEKTSMELDINGER_ID);
create index IDX_INNTEKTSMELDING_2
  on INNTEKTSMELDING (ARBEIDSGIVER_VIRKSOMHET_ID);
create index IDX_INNTEKTSMELDING_3
  on INNTEKTSMELDING (ARBEIDSGIVER_VIRKSOMHET_ID, ARBEIDSFORHOLD_ID);
create index IDX_INNTEKTSMELDING_6
  on INNTEKTSMELDING (MOTTATT_DOKUMENT_ID);
create index IDX_INNTEKTSMELDING_7
  on INNTEKTSMELDING (INNSENDINGSAARSAK);
create table NATURAL_YTELSE
(
  ID                     bigint                              not null
    constraint PK_NATURAL_YTELSE
      primary key,
  INNTEKTSMELDING_ID     bigint                              not null
    constraint FK_NATURAL_YTELSE_1
      references INNTEKTSMELDING,
  NATURAL_YTELSE_TYPE    VARCHAR(100)                        not null,
  KL_NATURAL_YTELSE_TYPE VARCHAR(100) default 'NATURAL_YTELSE_TYPE',
  BELOEP_MND             NUMERIC(10,2)                       not null,
  FOM                    DATE                                not null,
  TOM                    DATE                                not null,
  VERSJON                bigint       default 0              not null,
  OPPRETTET_AV           VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID          TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV              VARCHAR(20),
  ENDRET_TID             TIMESTAMP(3),
  constraint FK_NATURAL_YTELSE_2
    foreign key (NATURAL_YTELSE_TYPE, KL_NATURAL_YTELSE_TYPE) references KODELISTE (KODE, KODEVERK)
);
comment on table NATURAL_YTELSE is 'Arbeidsgivers informasjon om oppstart og opphør av natural ytelser';
comment on column NATURAL_YTELSE.ID is 'Primærnøkkel';
comment on column NATURAL_YTELSE.NATURAL_YTELSE_TYPE is 'Fremmednøkkel til kodeverktabell for naturalytelsetype.';
comment on column NATURAL_YTELSE.KL_NATURAL_YTELSE_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column NATURAL_YTELSE.BELOEP_MND is 'Verdi i kroner per måned';
comment on column NATURAL_YTELSE.TOM is 'Fremmednøkkel til kodeverktabell for naturalytelsetype.';
create index IDX_NATURAL_YTELSE_1
  on NATURAL_YTELSE (INNTEKTSMELDING_ID);
create index IDX_NATURAL_YTELSE_6
  on NATURAL_YTELSE (NATURAL_YTELSE_TYPE);
create table GRADERING
(
  ID                 bigint                              not null
    constraint PK_GRADERING
      primary key,
  INNTEKTSMELDING_ID bigint                              not null
    constraint FK_GRADERING_1
      references INNTEKTSMELDING,
  ARBEIDSTID_PROSENT NUMERIC(5,2)                        not null,
  FOM                DATE                                not null,
  TOM                DATE                                not null,
  VERSJON            bigint       default 0              not null,
  OPPRETTET_AV       VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID      TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV          VARCHAR(20),
  ENDRET_TID         TIMESTAMP(3)
);
comment on table GRADERING is 'Arbeidsgivers informasjon om gradering';
comment on column GRADERING.ID is 'Primærnøkkel';
comment on column GRADERING.INNTEKTSMELDING_ID is 'FK:';
comment on column GRADERING.ARBEIDSTID_PROSENT is 'Avtalt arbeidstid i prosent under gradering';
create index IDX_GRADERING_1
  on GRADERING (INNTEKTSMELDING_ID);
create table UTSETTELSE_PERIODE
(
  ID                        bigint                              not null
    constraint PK_UTSETTELSE_PERIODE
      primary key,
  INNTEKTSMELDING_ID        bigint                              not null
    constraint FK_UTSETTELSE_PERIODE_1
      references INNTEKTSMELDING,
  UTSETTELSE_AARSAK_TYPE    VARCHAR(100)                        not null,
  KL_UTSETTELSE_AARSAK_TYPE VARCHAR(100) default 'UTSETTELSE_AARSAK_TYPE',
  FOM                       DATE                                not null,
  TOM                       DATE                                not null,
  VERSJON                   bigint       default 0              not null,
  OPPRETTET_AV              VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID             TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                 VARCHAR(20),
  ENDRET_TID                TIMESTAMP(3),
  constraint FK_UTSETTELSE_PERIODE_3
    foreign key (UTSETTELSE_AARSAK_TYPE, KL_UTSETTELSE_AARSAK_TYPE) references KODELISTE (KODE, KODEVERK)
);
comment on table UTSETTELSE_PERIODE is 'Arbeidsgivers informasjon om utsettelser.';
comment on column UTSETTELSE_PERIODE.ID is 'Primærnøkkel';
comment on column UTSETTELSE_PERIODE.UTSETTELSE_AARSAK_TYPE is 'Fremmednøkkel til kodeverkstabellen som beskriver uttsettelsesårsaker';
comment on column UTSETTELSE_PERIODE.KL_UTSETTELSE_AARSAK_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
create index IDX_UTSETTELSE_PERIODE_1
  on UTSETTELSE_PERIODE (INNTEKTSMELDING_ID);
create index IDX_UTSETTELSE_PERIODE_6
  on UTSETTELSE_PERIODE (UTSETTELSE_AARSAK_TYPE);

create table OPPGITT_OPPTJENING
(
  ID            bigint                              not null
    constraint PK_OPPGITT_OPPTJENING
      primary key,
  OPPRETTET_AV  VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV     VARCHAR(20),
  ENDRET_TID    TIMESTAMP(3)
);

comment on table OPPGITT_OPPTJENING is 'Inneholder brukers oppgitte opplysninger om arbeid eller aktiviteter som er likestilt pensjonsgivende arbeid.';
comment on column OPPGITT_OPPTJENING.ID is 'Primærnøkkel';

create table OPPGITT_ARBEIDSFORHOLD
(
  ID                         bigint                              not null
    constraint PK_OPPGITT_ARBEIDSFORHOLD
      primary key,
  OPPGITT_OPPTJENING_ID      bigint                              not null
    constraint FK_ARBEIDSFORHOLD_1
      references OPPGITT_OPPTJENING,
  VIRKSOMHET_ID              bigint
    constraint FK_ARBEIDSFORHOLD_2
      references VIRKSOMHET,
  FOM                        DATE                                not null,
  TOM                        DATE                                not null,
  UTENLANDSK_INNTEKT         VARCHAR(1)                          not null,
  ARBEID_TYPE                VARCHAR(100)                        not null,
  KL_ARBEID_TYPE             VARCHAR(100) default 'ARBEID_TYPE',
  UTENLANDSK_VIRKSOMHET_NAVN VARCHAR(100),
  LAND                       VARCHAR(100),
  KL_LANDKODER               VARCHAR(100) default 'LANDKODER'    not null,
  OPPRETTET_AV               VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID              TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                  VARCHAR(20),
  ENDRET_TID                 TIMESTAMP(3),
  constraint FK_OPPGITT_ARBEIDSFORHOLD_3
    foreign key (ARBEID_TYPE, KL_ARBEID_TYPE) references KODELISTE (KODE, KODEVERK),
  constraint FK_OPPGITT_ARBEIDSFORHOLD_4
    foreign key (LAND, KL_LANDKODER) references KODELISTE (KODE, KODEVERK)
);
comment on table OPPGITT_ARBEIDSFORHOLD is 'Oppgitt informasjon om arbeidsforhold';
comment on column OPPGITT_ARBEIDSFORHOLD.ID is 'Primærnøkkel';
comment on column OPPGITT_ARBEIDSFORHOLD.OPPGITT_OPPTJENING_ID is 'FK:';
comment on column OPPGITT_ARBEIDSFORHOLD.VIRKSOMHET_ID is 'FK:';
comment on column OPPGITT_ARBEIDSFORHOLD.UTENLANDSK_INNTEKT is 'Inntekt fra utenlandsk arbeidsforhold';
comment on column OPPGITT_ARBEIDSFORHOLD.ARBEID_TYPE is 'Fremmednøkkel til tabell over arbeidstyper';
comment on column OPPGITT_ARBEIDSFORHOLD.KL_ARBEID_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column OPPGITT_ARBEIDSFORHOLD.UTENLANDSK_VIRKSOMHET_NAVN is 'Navn på virksomheten i utlandet hvis det er der den finnes';
comment on column OPPGITT_ARBEIDSFORHOLD.KL_LANDKODER is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
create index IDX_OPPGITT_ARBEIDSFORHOLD_1
  on OPPGITT_ARBEIDSFORHOLD (OPPGITT_OPPTJENING_ID);
create index IDX_OPPGITT_ARBEIDSFORHOLD_2
  on OPPGITT_ARBEIDSFORHOLD (VIRKSOMHET_ID);
create index IDX_OPPGITT_ARBEIDSFORHOLD_3
  on OPPGITT_ARBEIDSFORHOLD (ARBEID_TYPE);
create index IDX_OPPGITT_ARBEIDSFORHOLD_4
  on OPPGITT_ARBEIDSFORHOLD (LAND);
create table EGEN_NAERING
(
  ID                         bigint                              not null
    constraint PK_EGEN_NAERING
      primary key,
  OPPGITT_OPPTJENING_ID      bigint                              not null
    constraint FK_EGEN_NAERING_1
      references OPPGITT_OPPTJENING,
  FOM                        DATE                                not null,
  TOM                        DATE                                not null,
  VIRKSOMHET_TYPE            VARCHAR(100),
  VIRKSOMHET_ID              bigint
    constraint FK_EGEN_NAERING_2
      references VIRKSOMHET,
  REGNSKAPSFOERER_NAVN       VARCHAR(400),
  REGNSKAPSFOERER_TLF        VARCHAR(100),
  ENDRING_DATO               DATE,
  BEGRUNNELSE                VARCHAR(1000),
  BRUTTO_INNTEKT             NUMERIC(10,2),
  UTENLANDSK_VIRKSOMHET_NAVN VARCHAR(100),
  LAND                       VARCHAR(100),
  OPPRETTET_AV               VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID              TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                  VARCHAR(20),
  ENDRET_TID                 TIMESTAMP(3),
  NYOPPSTARTET               VARCHAR(1)   default 'N'            not null,
  VARIG_ENDRING              VARCHAR(1)   default 'N'            not null,
  KL_VIRKSOMHET_TYPE         VARCHAR(100) default 'VIRKSOMHET_TYPE',
  KL_LANDKODER               VARCHAR(100) default 'LANDKODER',
  NY_I_ARBEIDSLIVET          VARCHAR(1)   default 'N'            not null,
  NAER_RELASJON              VARCHAR(1)                          not null,
  constraint FK_EGEN_NAERING_3
    foreign key (VIRKSOMHET_TYPE, KL_VIRKSOMHET_TYPE) references KODELISTE (KODE, KODEVERK),
  constraint FK_EGEN_NAERING_4
    foreign key (LAND, KL_LANDKODER) references KODELISTE (KODE, KODEVERK)
);
comment on table EGEN_NAERING is 'Oppgitt informasjon om egen næringsvirksomhet';
comment on column EGEN_NAERING.ID is 'Primærnøkkel';
comment on column EGEN_NAERING.OPPGITT_OPPTJENING_ID is 'FK.';
comment on column EGEN_NAERING.VIRKSOMHET_ID is 'FK:';
comment on column EGEN_NAERING.REGNSKAPSFOERER_NAVN is 'Navn på oppgitt regnskapsfører';
comment on column EGEN_NAERING.REGNSKAPSFOERER_TLF is 'Telefonnr til oppgitt regnskapsfører';
comment on column EGEN_NAERING.ENDRING_DATO is 'Dato for endring i næringen';
comment on column EGEN_NAERING.BEGRUNNELSE is 'Saksbehandlers vurderinger av avslag på perioden';
comment on column EGEN_NAERING.BRUTTO_INNTEKT is 'Brutto inntekt';
comment on column EGEN_NAERING.UTENLANDSK_VIRKSOMHET_NAVN is 'Navn på virksomheten i utlandet hvis det er der den finnes.';
comment on column EGEN_NAERING.NYOPPSTARTET is 'Er næringen startet opp innenfor siste ligningsåret';
comment on column EGEN_NAERING.VARIG_ENDRING is 'Om det i søknaden er angitt varig endring i næring';
comment on column EGEN_NAERING.KL_VIRKSOMHET_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column EGEN_NAERING.KL_LANDKODER is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column EGEN_NAERING.NY_I_ARBEIDSLIVET is 'J hvis søker er ny i arbeidslivet';
comment on column EGEN_NAERING.NAER_RELASJON is 'Om det i søknaden er angitt nær relasjon for egen næring';
create index IDX_EGEN_NAERING_1
  on EGEN_NAERING (OPPGITT_OPPTJENING_ID);
create index IDX_EGEN_NAERING_2
  on EGEN_NAERING (VIRKSOMHET_ID);
create index IDX_EGEN_NAERING_3
  on EGEN_NAERING (VIRKSOMHET_TYPE);
create index IDX_EGEN_NAERING_6
  on EGEN_NAERING (LAND);
create table ANNEN_AKTIVITET
(
  ID                    bigint                              not null
    constraint PK_ANNEN_AKTIVITET
      primary key,
  OPPGITT_OPPTJENING_ID bigint                              not null
    constraint FK_ANNEN_AKTIVITET_1
      references OPPGITT_OPPTJENING,
  FOM                   DATE                                not null,
  TOM                   DATE                                not null,
  ARBEID_TYPE           VARCHAR(100)                        not null,
  OPPRETTET_AV          VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID         TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV             VARCHAR(20),
  ENDRET_TID            TIMESTAMP(3),
  KL_ARBEID_TYPE        VARCHAR(100) default 'ARBEID_TYPE',
  constraint FK_ANNEN_AKTIVITET_2
    foreign key (ARBEID_TYPE, KL_ARBEID_TYPE) references KODELISTE (KODE, KODEVERK)
);
comment on table ANNEN_AKTIVITET is 'Aktiviteter som er likestilt pensjonsgivende arbeid.';
comment on column ANNEN_AKTIVITET.ID is 'Primærnøkkel';
comment on column ANNEN_AKTIVITET.OPPGITT_OPPTJENING_ID is 'FK:';
comment on column ANNEN_AKTIVITET.KL_ARBEID_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
create index IDX_ANNEN_AKTIVITET_1
  on ANNEN_AKTIVITET (OPPGITT_OPPTJENING_ID);
create index IDX_ANNEN_AKTIVITET_6
  on ANNEN_AKTIVITET (ARBEID_TYPE);
create table AKTOER_YTELSE
(
  ID                        bigint                              not null
    constraint PK_AKTOER_YTELSE
      primary key,
  INNTEKT_ARBEID_YTELSER_ID bigint                              not null
    constraint FK_AKTOER_YTELSE_1
      references INNTEKT_ARBEID_YTELSER,
  VERSJON                   bigint       default 0              not null,
  OPPRETTET_AV              VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID             TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                 VARCHAR(20),
  ENDRET_TID                TIMESTAMP(3),
  AKTOER_ID                 VARCHAR(50)
);
comment on table AKTOER_YTELSE is 'Tabell med rad per aktør med tilstøtende ytelser relatert til behandlingen.';
comment on column AKTOER_YTELSE.ID is 'Primærnøkkel';
comment on column AKTOER_YTELSE.INNTEKT_ARBEID_YTELSER_ID is 'FK';
comment on column AKTOER_YTELSE.AKTOER_ID is 'Aktørid (fra NAV Aktørregister)';
create index IDX_AKTOER_YTELSE_2
  on AKTOER_YTELSE (INNTEKT_ARBEID_YTELSER_ID);
create index IDX_AKTOER_YTELSE_1
  on AKTOER_YTELSE (AKTOER_ID);
create table RELATERT_YTELSE
(
  ID                         bigint                              not null
    constraint PK_YTELSE
      primary key,
  AKTOER_YTELSE_ID           bigint                              not null
    constraint FK_YTELSE_4
      references AKTOER_YTELSE,
  YTELSE_TYPE                VARCHAR(100)                        not null,
  FOM                        DATE                                not null,
  TOM                        DATE                                not null,
  STATUS                     VARCHAR(100)                        not null,
  KILDE                      VARCHAR(100)                        not null,
  KL_YTELSE_TYPE             VARCHAR(100) default 'RELATERT_YTELSE_TYPE',
  KL_STATUS                  VARCHAR(100) default 'RELATERT_YTELSE_TILSTAND',
  KL_KILDE                   VARCHAR(100) default 'FAGSYSTEM',
  VERSJON                    bigint       default 0              not null,
  OPPRETTET_AV               VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID              TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                  VARCHAR(20),
  ENDRET_TID                 TIMESTAMP(3),
  TEMAUNDERKATEGORI          VARCHAR(100),
  KL_TEMAUNDERKATEGORI       VARCHAR(100) default 'TEMA_UNDERKATEGORI',
  FAGSYSTEM_UNDERKATEGORI    VARCHAR(100),
  KL_FAGSYSTEM_UNDERKATEGORI VARCHAR(100) default 'FAGSYSTEM_UNDERKATEGORI',
  SAKSNUMMER                 VARCHAR(19),
  constraint FK_YTELSE_1
    foreign key (YTELSE_TYPE, KL_YTELSE_TYPE) references KODELISTE (KODE, KODEVERK),
  constraint FK_YTELSE_2
    foreign key (STATUS, KL_STATUS) references KODELISTE (KODE, KODEVERK),
  constraint FK_YTELSE_3
    foreign key (KILDE, KL_KILDE) references KODELISTE (KODE, KODEVERK),
  constraint FK_YTELSE_5
    foreign key (TEMAUNDERKATEGORI, KL_TEMAUNDERKATEGORI) references KODELISTE (KODE, KODEVERK),
  constraint FK_YTELSE_6
    foreign key (FAGSYSTEM_UNDERKATEGORI, KL_FAGSYSTEM_UNDERKATEGORI) references KODELISTE (KODE, KODEVERK)
);
comment on table RELATERT_YTELSE is 'En tabell med informasjon om ytelser fra Arena og Infotrygd';
comment on column RELATERT_YTELSE.ID is 'Primærnøkkel';
comment on column RELATERT_YTELSE.AKTOER_YTELSE_ID is 'FK:AKTOER_YTELSE';
comment on column RELATERT_YTELSE.YTELSE_TYPE is 'Type ytelse for eksempel sykepenger, foreldrepenger.. (dagpenger?) etc';
comment on column RELATERT_YTELSE.FOM is 'Startdato for ytelsten. Er tilsvarende Identdato fra Infotrygd.';
comment on column RELATERT_YTELSE.TOM is 'Sluttdato er en utledet dato enten fra opphørFOM eller fra identdaot pluss periode';
comment on column RELATERT_YTELSE.STATUS is 'Er om ytelsen er ÅPEN, LØPENDE eller AVSLUTTET';
comment on column RELATERT_YTELSE.KILDE is 'Hvilket system informasjonen kommer fra';
comment on column RELATERT_YTELSE.KL_YTELSE_TYPE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column RELATERT_YTELSE.KL_STATUS is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column RELATERT_YTELSE.KL_KILDE is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column RELATERT_YTELSE.TEMAUNDERKATEGORI is 'Fremmednøkkel til kodeverktabellen for beskrivelser av underkategori fra Infotrygd';
comment on column RELATERT_YTELSE.KL_TEMAUNDERKATEGORI is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column RELATERT_YTELSE.FAGSYSTEM_UNDERKATEGORI is 'Underkategori for der fagsystem ikke er nok.';
comment on column RELATERT_YTELSE.KL_FAGSYSTEM_UNDERKATEGORI is 'Kodeverkreferanse for fagsystemUnderkategori';
comment on column RELATERT_YTELSE.SAKSNUMMER is 'Saksnummer i GSAK';
create index IDX_YTELSE_1
  on RELATERT_YTELSE (AKTOER_YTELSE_ID);
create index IDX_RELATERT_YTELSE_6
  on RELATERT_YTELSE (YTELSE_TYPE);
create index IDX_RELATERT_YTELSE_7
  on RELATERT_YTELSE (STATUS);
create index IDX_RELATERT_YTELSE_8
  on RELATERT_YTELSE (KILDE);
create index IDX_RELATERT_YTELSE_9
  on RELATERT_YTELSE (TEMAUNDERKATEGORI);
create index IDX_RELATERT_YTELSE_10
  on RELATERT_YTELSE (FAGSYSTEM_UNDERKATEGORI);
create table YTELSE_GRUNNLAG
(
  ID                       bigint                              not null
    constraint PK_YTELSE_GRUNNLAG
      primary key,
  YTELSE_ID                bigint                              not null
    constraint FK_YTELSE_GRUNNLAG_2
      references RELATERT_YTELSE,
  OPPRINNELIG_IDENTDATO    DATE,
  DEKNINGSGRAD_PROSENT     NUMERIC(5,2),
  GRADERING_PROSENT        NUMERIC(5,2),
  INNTEKTSGRUNNLAG_PROSENT NUMERIC(5,2),
  VERSJON                  bigint       default 0              not null,
  OPPRETTET_AV             VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID            TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                VARCHAR(20),
  ENDRET_TID               TIMESTAMP(3),
  ARBEIDSKATEGORI          VARCHAR(100)                        not null,
  KL_ARBEIDSKATEGORI       VARCHAR(100) default 'ARBEIDSKATEGORI',
  constraint FK_YTELSE_GRUNNLAG_81
    foreign key (ARBEIDSKATEGORI, KL_ARBEIDSKATEGORI) references KODELISTE (KODE, KODEVERK)
);
comment on table YTELSE_GRUNNLAG is 'En tabell med informasjon om ytelsesgrunnlag fra Arena og Infotrygd';
comment on column YTELSE_GRUNNLAG.ID is 'Primærnøkkel';
comment on column YTELSE_GRUNNLAG.YTELSE_ID is 'FK:YTELSE';
comment on column YTELSE_GRUNNLAG.OPPRINNELIG_IDENTDATO is 'Identdato (samme som stardato. kan hende denne er overflødig';
comment on column YTELSE_GRUNNLAG.DEKNINGSGRAD_PROSENT is 'Dekningsgrad hentet fra infotrygd';
comment on column YTELSE_GRUNNLAG.GRADERING_PROSENT is 'Gradering hentet fra infotrygd';
comment on column YTELSE_GRUNNLAG.INNTEKTSGRUNNLAG_PROSENT is 'Inntektsgrunnlag hentet fra infotrygd';
comment on column YTELSE_GRUNNLAG.ARBEIDSKATEGORI is 'FK:ARBEIDSKATEGORI';
comment on column YTELSE_GRUNNLAG.KL_ARBEIDSKATEGORI is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
create index IDX_YTELSE_GRUNNLAG_1
  on YTELSE_GRUNNLAG (YTELSE_ID);
create index IDX_YTELSE_GRUNNLAG_FELT_2
  on YTELSE_GRUNNLAG (ARBEIDSKATEGORI, KL_ARBEIDSKATEGORI);
create table YTELSE_STOERRELSE
(
  ID                 bigint                              not null
    constraint PK_YTELSE_STOERRELSE
      primary key,
  YTELSE_GRUNNLAG_ID bigint                              not null
    constraint FK_YTELSE_STOERRELSE_2
      references YTELSE_GRUNNLAG,
  VIRKSOMHET_ID      bigint
    constraint FK_YTELSE_STOERRELSE_3
      references VIRKSOMHET,
  BELOEP             NUMERIC(19,2)                       not null,
  HYPPIGHET          VARCHAR(100)                        not null,
  KL_HYPPIGHET       VARCHAR(100) default 'INNTEKT_PERIODE_TYPE',
  VERSJON            bigint       default 0              not null,
  OPPRETTET_AV       VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID      TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV          VARCHAR(20),
  ENDRET_TID         TIMESTAMP(3),
  constraint FK_YTELSE_STOERRELSE_1
    foreign key (HYPPIGHET, KL_HYPPIGHET) references KODELISTE (KODE, KODEVERK)
);
comment on table YTELSE_STOERRELSE is 'En tabell med informasjon om beløpene som kommer fra ytelsesgrunnlag fra Arena og Infotrygd';
comment on column YTELSE_STOERRELSE.ID is 'FK:YTELSE_GRUNNLAG Primærnøkkel';
comment on column YTELSE_STOERRELSE.YTELSE_GRUNNLAG_ID is 'FK:YTELSE_GRUNNLAG';
comment on column YTELSE_STOERRELSE.VIRKSOMHET_ID is 'FK:VIRKSOMHET';
comment on column YTELSE_STOERRELSE.BELOEP is 'Beløpet som er for den gitte perioden i ytelsesgrunnlag';
comment on column YTELSE_STOERRELSE.HYPPIGHET is 'Hyppigheten for beløpet';
comment on column YTELSE_STOERRELSE.KL_HYPPIGHET is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
create index IDX_YTELSE_STOERRELSE_1
  on YTELSE_STOERRELSE (YTELSE_GRUNNLAG_ID);
create index IDX_YTELSE_STOERRELSE_2
  on YTELSE_STOERRELSE (VIRKSOMHET_ID);
create index IDX_YTELSE_STOERRELSE_3
  on YTELSE_STOERRELSE (HYPPIGHET);
create table YTELSE_ANVIST
(
  ID                      bigint                              not null
    constraint PK_YTELSE_ANVIST
      primary key,
  YTELSE_ID               bigint                              not null
    constraint FK_YTELSE_ANVIST_1
      references RELATERT_YTELSE,
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
comment on table YTELSE_ANVIST is 'En tabell med informasjon om ytelsesperioder';
comment on column YTELSE_ANVIST.ID is 'PK';
comment on column YTELSE_ANVIST.YTELSE_ID is 'FK:YTELSE Fremmednøkkel til kodeverktabellen over ytelser???';
comment on column YTELSE_ANVIST.BELOEP is 'Beløp ifm utbetaling.';
comment on column YTELSE_ANVIST.FOM is 'Anvist periode første dag.';
comment on column YTELSE_ANVIST.TOM is 'Anvist periode siste dag.';
comment on column YTELSE_ANVIST.UTBETALINGSGRAD_PROSENT is 'Utbetalingsprosent fra kildesystem.';
comment on column YTELSE_ANVIST.DAGSATS is 'Dagsatsen på den relaterte ytelsen';
create index IDX_YTELSE_ANVIST_1
  on YTELSE_ANVIST (YTELSE_ID);

create table INFORMASJON
(
  ID            bigint                              not null
    constraint PK_INFORMASJON
      primary key,
  VERSJON       bigint       default 0              not null,
  OPPRETTET_AV  VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV     VARCHAR(20),
  ENDRET_TID    TIMESTAMP(3)
);
comment on table INFORMASJON is 'Mange til mange tabell for arbeidsforhold referanse og overstyrende betraktninger om arbeidsforhold';
create table ARBEIDSFORHOLD_REFER
(
  ID                         bigint                              not null
    constraint PK_ARBEIDSFORHOLD_REFER
      primary key,
  INFORMASJON_ID             bigint                              not null
    constraint FK_ARBEIDSFORHOLD_REFER_1
      references INFORMASJON,
  INTERN_REFERANSE           VARCHAR(100)                        not null,
  EKSTERN_REFERANSE          VARCHAR(100)                        not null,
  ARBEIDSGIVER_AKTOR_ID      VARCHAR(100),
  ARBEIDSGIVER_VIRKSOMHET_ID bigint
    constraint FK_ARBEIDSFORHOLD_REFER_2
      references VIRKSOMHET,
  VERSJON                    bigint       default 0              not null,
  OPPRETTET_AV               VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID              TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                  VARCHAR(20),
  ENDRET_TID                 TIMESTAMP(3)
);
comment on table ARBEIDSFORHOLD_REFER is 'Kobling mellom arbeidsforhold fra aa-reg og intern nøkkel for samme representasjon';
comment on column ARBEIDSFORHOLD_REFER.INTERN_REFERANSE is 'Syntetisk nøkkel for å representere et arbeidsforhold';
comment on column ARBEIDSFORHOLD_REFER.EKSTERN_REFERANSE is 'ArbeidsforholdId hentet fra AA-reg';
comment on column ARBEIDSFORHOLD_REFER.ARBEIDSGIVER_AKTOR_ID is 'Aktør til personlig foretak.';
create index IDX_ARBEIDSFORHOLD_REFER_1
  on ARBEIDSFORHOLD_REFER (INTERN_REFERANSE);
create index IDX_ARBEIDSFORHOLD_REFER_2
  on ARBEIDSFORHOLD_REFER (ARBEIDSGIVER_VIRKSOMHET_ID);
create index IDX_ARBEIDSFORHOLD_REFER_3
  on ARBEIDSFORHOLD_REFER (INFORMASJON_ID);
create index IDX_ARBEIDSFORHOLD_REFER_4
  on ARBEIDSFORHOLD_REFER (EKSTERN_REFERANSE);
create table ARBEIDSFORHOLD
(
  ID                         bigint                              not null
    constraint PK_ARBEIDSFORHOLD
      primary key,
  INFORMASJON_ID             bigint                              not null
    constraint FK_ARBEIDSFORHOLD_1
      references INFORMASJON,
  ARBEIDSFORHOLD_ID          VARCHAR(100),
  NY_ARBEIDSFORHOLD_ID       VARCHAR(100),
  ARBEIDSGIVER_AKTOR_ID      VARCHAR(100),
  ARBEIDSGIVER_VIRKSOMHET_ID bigint
    constraint FK_ARBEIDSFORHOLD_2
      references VIRKSOMHET,
  BEGRUNNELSE                VARCHAR(2000),
  HANDLING_TYPE              VARCHAR(100)                        not null,
  KL_HANDLING_TYPE           VARCHAR(100) default 'ARBEIDSFORHOLD_HANDLING_TYPE',
  VERSJON                    bigint       default 0              not null,
  OPPRETTET_AV               VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID              TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV                  VARCHAR(20),
  ENDRET_TID                 TIMESTAMP(3),
  BEKREFTET_TOM_DATO         DATE,
  constraint FK_ARBEIDSFORHOLD_3
    foreign key (HANDLING_TYPE, KL_HANDLING_TYPE) references KODELISTE (KODE, KODEVERK)
);
comment on table ARBEIDSFORHOLD is 'Overstyrende betraktninger om arbeidsforhold';
comment on column ARBEIDSFORHOLD.ARBEIDSFORHOLD_ID is 'Intern nøkkel som representerer arbeidsforhodl-id fra AA-reg';
comment on column ARBEIDSFORHOLD.NY_ARBEIDSFORHOLD_ID is 'Den nye intern nøkkel som representerer arbeidsforhodl-id fra AA-reg etter merge av nøkler';
comment on column ARBEIDSFORHOLD.ARBEIDSGIVER_AKTOR_ID is 'Personlig foretak som arbeidsgiver';
comment on column ARBEIDSFORHOLD.BEGRUNNELSE is 'Saksbehandlers begrunnelsen for tiltaket';
comment on column ARBEIDSFORHOLD.BEKREFTET_TOM_DATO is 'Til og med dato fastsatt av saksbehandler';
create index IDX_ARBEIDSFORHOLD_1
  on ARBEIDSFORHOLD (INFORMASJON_ID);
create index IDX_ARBEIDSFORHOLD_2
  on ARBEIDSFORHOLD (ARBEIDSGIVER_VIRKSOMHET_ID);
create index IDX_ARBEIDSFORHOLD_3
  on ARBEIDSFORHOLD (HANDLING_TYPE);
create table REFUSJON
(
  ID                  bigint                              not null
    constraint PK_REFUSJON
      primary key,
  INNTEKTSMELDING_ID  bigint                              not null
    constraint FK_REFUSJON_1
      references INNTEKTSMELDING,
  REFUSJONSBELOEP_MND NUMERIC(10,2)                       not null,
  FOM                 DATE                                not null,
  VERSJON             bigint       default 0              not null,
  OPPRETTET_AV        VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID       TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV           VARCHAR(20),
  ENDRET_TID          TIMESTAMP(3)
);
comment on table REFUSJON is 'Endringer i refusjonsbeløp fra en oppgitt dato';
comment on column REFUSJON.ID is 'Primær nøkkel';
comment on column REFUSJON.INNTEKTSMELDING_ID is 'Fremmednøkkel til inntektsmelding';
comment on column REFUSJON.REFUSJONSBELOEP_MND is 'Verdi i kroner per måned';
comment on column REFUSJON.FOM is 'Dato refusjonsbeløpet gjelder fra';
create index IDX_REFUSJON_1
  on REFUSJON (INNTEKTSMELDING_ID);

create table OPPGITT_FRILANS
(
  ID                     bigint                              not null
    constraint PK_OPPGITT_FRILANS
      primary key,
  OPPGITT_OPPTJENING_ID  bigint                              not null
    constraint FK_OPPGITT_FRILANS
      references OPPGITT_OPPTJENING,
  INNTEKT_FRA_FOSTERHJEM VARCHAR(1)                          not null,
  NYOPPSTARTET           VARCHAR(1)                          not null,
  NAER_RELASJON          VARCHAR(1)                          not null,
  VERSJON                bigint       default 0              not null,
  OPPRETTET_AV           VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID          TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV              VARCHAR(20),
  ENDRET_TID             TIMESTAMP(3)
);

comment on table OPPGITT_FRILANS is 'Frilans oppgitt av søker';
comment on column OPPGITT_FRILANS.ID is 'Primary Key';
comment on column OPPGITT_FRILANS.OPPGITT_OPPTJENING_ID is 'FOREIGN KEY';
comment on column OPPGITT_FRILANS.INNTEKT_FRA_FOSTERHJEM is 'J hvis inntekt fra forsterhjem';
comment on column OPPGITT_FRILANS.NYOPPSTARTET is 'J hvis nyoppstartet';
comment on column OPPGITT_FRILANS.NAER_RELASJON is 'J hvis nær relasjon';

create index IDX_OPPGITT_F_1
  on OPPGITT_FRILANS (OPPGITT_OPPTJENING_ID);

create table OPPGITT_FRILANSOPPDRAG
(
  ID            bigint                              not null
    constraint PK_OPPGITT_FRILANSOPPDRAG
      primary key,
  FRILANS_ID    bigint                              not null
    constraint FK_OPPGITT_FRILANSOPPDRAG
      references OPPGITT_FRILANS,
  FOM           DATE                                not null,
  TOM           DATE                                not null,
  OPPDRAGSGIVER VARCHAR(100)                        not null,
  VERSJON       bigint       default 0              not null,
  OPPRETTET_AV  VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV     VARCHAR(20),
  ENDRET_TID    TIMESTAMP(3)
);

comment on table OPPGITT_FRILANSOPPDRAG is 'Frilansoppdrag oppgitt av søker';
comment on column OPPGITT_FRILANSOPPDRAG.ID is 'Primary Key';
comment on column OPPGITT_FRILANSOPPDRAG.FRILANS_ID is 'FOREIGN KEY';
comment on column OPPGITT_FRILANSOPPDRAG.FOM is 'Periode start';
comment on column OPPGITT_FRILANSOPPDRAG.TOM is 'Periode slutt';
comment on column OPPGITT_FRILANSOPPDRAG.OPPDRAGSGIVER is 'Oppdragsgiver';

create index IDX_OPPGITT_FO_1
  on OPPGITT_FRILANSOPPDRAG (FRILANS_ID);



create table GR_ARBEID_INNTEKT
(
  ID                    bigint                              not null,
  BEHANDLING_ID         bigint                              not null,
  REGISTER_ID           bigint,
  SAKSBEHANDLET_ID      bigint,
  VERSJON               bigint       default 0              not null,
  OPPRETTET_AV          VARCHAR(20)  default 'VL'           not null,
  OPPRETTET_TID         TIMESTAMP(3) default localtimestamp not null,
  ENDRET_AV             VARCHAR(20),
  ENDRET_TID            TIMESTAMP(3),
  INNTEKTSMELDINGER_ID  bigint,
  OPPGITT_OPPTJENING_ID bigint,
  INFORMASJON_ID        bigint,
  AKTIV                 VARCHAR(1)   default 'J'            not null,
  constraint PK_GR_ARBEID_INNTEKT
    primary key (ID),
  constraint FK_GR_ARBEID_INNTEKT_1
    foreign key (BEHANDLING_ID) references KOBLING,
  constraint FK_GR_ARBEID_INNTEKT_2
    foreign key (REGISTER_ID) references INNTEKT_ARBEID_YTELSER,
  constraint FK_GR_ARBEID_INNTEKT_3
    foreign key (INNTEKTSMELDINGER_ID) references INNTEKTSMELDINGER,
  constraint FK_GR_ARBEID_INNTEKT_4
    foreign key (OPPGITT_OPPTJENING_ID) references OPPGITT_OPPTJENING,
  constraint FK_GR_ARBEID_INNTEKT_5
    foreign key (SAKSBEHANDLET_ID) references INNTEKT_ARBEID_YTELSER,
  constraint FK_GR_ARBEID_INNTEKT_6
    foreign key (INFORMASJON_ID) references INFORMASJON
);

comment on table GR_ARBEID_INNTEKT is 'Behandlingsgrunnlag for arbeid, inntekt og ytelser (aggregat)';
comment on column GR_ARBEID_INNTEKT.ID is 'Primary Key';
comment on column GR_ARBEID_INNTEKT.BEHANDLING_ID is 'FK: BEHANDLING Fremmednøkkel for kobling til behandling';
comment on column GR_ARBEID_INNTEKT.REGISTER_ID is 'Arbeid inntekt register før skjæringstidspunkt';
comment on column GR_ARBEID_INNTEKT.SAKSBEHANDLET_ID is 'Arbeid inntekt saksbehandlet før skjæringstidspunkt';
comment on column GR_ARBEID_INNTEKT.INNTEKTSMELDINGER_ID is 'FK: Fremmednøkkel for kobling til inntektsmeldinger';
comment on column GR_ARBEID_INNTEKT.OPPGITT_OPPTJENING_ID is 'FK: Fremmenøkkel for kobling til egen oppgitt opptjening';

create index IDX_GR_ARBEID_INNTEKT_1
  on GR_ARBEID_INNTEKT (BEHANDLING_ID);

create index IDX_GR_ARBEID_INNTEKT_2
  on GR_ARBEID_INNTEKT (REGISTER_ID);

create index IDX_GR_ARBEID_INNTEKT_3
  on GR_ARBEID_INNTEKT (INNTEKTSMELDINGER_ID);

create index IDX_GR_ARBEID_INNTEKT_4
  on GR_ARBEID_INNTEKT (SAKSBEHANDLET_ID);

create index IDX_GR_ARBEID_INNTEKT_5
  on GR_ARBEID_INNTEKT (OPPGITT_OPPTJENING_ID);

create index IDX_GR_ARBEID_INNTEKT_6
  on GR_ARBEID_INNTEKT (INFORMASJON_ID);

alter table GR_ARBEID_INNTEKT
  add constraint CHK_AKTIV2
    check (aktiv IN ('J', 'N'));




