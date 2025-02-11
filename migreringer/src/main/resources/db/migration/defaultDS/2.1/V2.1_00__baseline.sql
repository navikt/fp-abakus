create sequence seq_kobling
    start with 1368400
    increment by 50
    maxvalue 999999999999999999;

create sequence seq_gr_arbeid_inntekt
    start with 1000000
    increment by 50;

create sequence seq_inntekt
    start with 1000000
    increment by 50;

create sequence seq_inntekt_arbeid_ytelser
    start with 1000000
    increment by 50;

create sequence seq_aktoer_inntekt
    start with 1000000
    increment by 50;

create sequence seq_inntektspost
    start with 1000000
    increment by 50;

create sequence seq_aktoer_arbeid
    start with 1000000
    increment by 50;

create sequence seq_yrkesaktivitet
    start with 1000000
    increment by 50;

create sequence seq_permisjon
    start with 1000000
    increment by 50;

create sequence seq_aktivitets_avtale
    start with 1000000
    increment by 50;

create sequence seq_inntektsmeldinger
    start with 1000000
    increment by 50;

create sequence seq_inntektsmelding
    start with 1000000
    increment by 50;

create sequence seq_natural_ytelse
    start with 1000000
    increment by 50;

create sequence seq_oppgitt_arbeidsforhold
    start with 1000000
    increment by 50;

create sequence seq_egen_naering
    start with 1000000
    increment by 50;

create sequence seq_annen_aktivitet
    start with 1000000
    increment by 50;

create sequence seq_aktoer_ytelse
    start with 1000000
    increment by 50;

create sequence seq_ytelse
    start with 1000000
    increment by 50;

create sequence seq_ytelse_grunnlag
    start with 1000000
    increment by 50;

create sequence seq_ytelse_stoerrelse
    start with 1000000
    increment by 50;

create sequence seq_ytelse_anvist
    start with 1000000
    increment by 50;

create sequence seq_iay_informasjon
    start with 1000000
    increment by 50;

create sequence seq_iay_arbeidsforhold
    start with 1000000
    increment by 50;

create sequence seq_iay_arbeidsforhold_refer
    start with 1000000
    increment by 50;

create sequence seq_prosess_task
    minvalue 1000000
    increment by 50;

create sequence seq_prosess_task_gruppe
    minvalue 10000000;

create sequence seq_vedtak_ytelse
    start with 1000000
    increment by 50;

create sequence seq_vedtak_ytelse_anvist
    start with 1000000
    increment by 50;

create sequence seq_so_oppgitt_opptjening
    start with 1000000
    increment by 50;

create sequence seq_so_oppgitt_frilans
    start with 1000000
    increment by 50;

create sequence seq_so_oppgitt_frilansoppdrag
    start with 1000000
    increment by 50;

create sequence seq_utsettelse_periode
    start with 1000000
    increment by 50;

create sequence seq_gradering
    start with 1000000
    increment by 50;

create sequence seq_refusjon
    start with 1000000
    increment by 50;

create sequence seq_iay_overstyrte_perioder
    start with 1000000
    increment by 50;

create sequence seq_iay_fravaer
    start with 1000000
    increment by 50;

create sequence seq_lonnskomp_vedtak
    start with 1000000
    increment by 50;

create sequence seq_lonnskomp_anvist
    start with 1000000
    increment by 50;

create sequence seq_diagnostikk_logg
    minvalue 1000000
    increment by 50;

create sequence seq_iay_oppgitte_opptjeninger
    start with 1000000
    increment by 50;

create sequence seq_ve_ytelse_andel
    start with 1000000
    increment by 50;

create sequence seq_ytelse_anvist_andel
    start with 1000000
    increment by 50;

create table kobling
(
    id                                    bigint                              not null
        constraint pk_kobling
            primary key,
    kobling_referanse                     uuid                                not null
        constraint uidx_kobling_1
            unique,
    ytelse_type                           varchar(100)                        not null,
    bruker_aktoer_id                      varchar(50)                         not null,
    annen_part_aktoer_id                  varchar(50),
    opplysning_periode_fom                date,
    opplysning_periode_tom                date,
    opptjening_periode_fom                date,
    opptjening_periode_tom                date,
    versjon                               bigint       default 0              not null,
    opprettet_av                          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                         timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                             varchar(20),
    endret_tid                            timestamp(3),
    saksnummer                            varchar(19)                         not null,
    aktiv                                 boolean      default true           not null,
    opplysning_periode_skattegrunnlag_fom date,
    opplysning_periode_skattegrunnlag_tom date
);

comment on table kobling is 'Holder referansen som kalles på fra av eksternt system';

comment on column kobling.id is 'Primærnøkkel';

comment on column kobling.kobling_referanse is 'Referansenøkkel som eksponeres lokalt';

comment on column kobling.ytelse_type is 'Hvilken ytelse komplekset henger under';

comment on column kobling.bruker_aktoer_id is 'Aktøren koblingen gjelder for';

comment on column kobling.annen_part_aktoer_id is 'Annen part det skal hentes informasjon for';

comment on column kobling.opplysning_periode_fom is 'Start på perioden det skal hentes data for';

comment on column kobling.opplysning_periode_tom is 'Slutt på perioden det skal hentes data for';

comment on column kobling.opptjening_periode_fom is 'Start på opptjeningsperioden det skal vurere på';

comment on column kobling.opptjening_periode_tom is 'Slutt på opptjeningsperioden det skal vurere på';

comment on column kobling.saksnummer is 'Saksnummer til saken koblingen gjelder for';

comment on column kobling.opplysning_periode_skattegrunnlag_fom is 'Start på perioden det skal hentes skattegrunnlag for';

comment on column kobling.opplysning_periode_skattegrunnlag_tom is 'Slutt på perioden det skal hentes skattegrunnlag for';

create index idx_kobling_1
    on kobling (kobling_referanse);

create index idx_kobling_2
    on kobling (saksnummer);

create table iay_inntekt_arbeid_ytelser
(
    id                bigint                              not null
        constraint pk_inntekt_arbeid_ytelser
            primary key,
    versjon           bigint       default 0              not null,
    opprettet_av      varchar(20)  default 'VL'::character varying not null,
    opprettet_tid     timestamp(3) default LOCALTIMESTAMP not null,
    endret_av         varchar(20),
    endret_tid        timestamp(3),
    ekstern_referanse uuid
);

comment on table iay_inntekt_arbeid_ytelser is 'Mange til mange tabell for inntekt, arbeid, ytelser.';

comment on column iay_inntekt_arbeid_ytelser.id is 'Primærnøkkel';

comment on column iay_inntekt_arbeid_ytelser.ekstern_referanse is 'Unik UUID for behandling til utvortes bruk. Representerer en immutable og unikt identifiserbar instans av dette aggregatet';

create unique index uidx_iay_01
    on iay_inntekt_arbeid_ytelser (ekstern_referanse);

create table iay_aktoer_inntekt
(
    id                        bigint                              not null
        constraint pk_aktoer_inntekt
            primary key,
    inntekt_arbeid_ytelser_id bigint                              not null
        constraint fk_aktoer_inntekt_1
            references iay_inntekt_arbeid_ytelser,
    versjon                   bigint       default 0              not null,
    opprettet_av              varchar(20)  default 'VL'::character varying not null,
    opprettet_tid             timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                 varchar(20),
    endret_tid                timestamp(3),
    aktoer_id                 varchar(50)
);

comment on table iay_aktoer_inntekt is 'Tabell med rad per aktør med inntekt relatert til behandlingen.';

comment on column iay_aktoer_inntekt.id is 'Primærnøkkel';

comment on column iay_aktoer_inntekt.inntekt_arbeid_ytelser_id is 'FK:';

comment on column iay_aktoer_inntekt.aktoer_id is 'Aktørid (fra NAV Aktørregister)';

create index idx_aktoer_inntekt_2
    on iay_aktoer_inntekt (inntekt_arbeid_ytelser_id);

create index idx_aktoer_inntekt_1
    on iay_aktoer_inntekt (aktoer_id);

create table iay_inntekt
(
    id                    bigint                              not null
        constraint pk_tmp_inntekt
            primary key,
    aktoer_inntekt_id     bigint                              not null
        constraint fk_inntekt_2
            references iay_aktoer_inntekt,
    kilde                 varchar(100),
    opprettet_av          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid         timestamp(3) default LOCALTIMESTAMP not null,
    endret_av             varchar(20),
    endret_tid            timestamp(3),
    arbeidsgiver_aktor_id varchar(100),
    arbeidsgiver_orgnr    varchar(100),
    versjon               bigint       default 0              not null
);

comment on table iay_inntekt is 'Inntekter per virksomhet';

comment on column iay_inntekt.id is 'Primærnøkkel';

comment on column iay_inntekt.aktoer_inntekt_id is 'FK:';

comment on column iay_inntekt.arbeidsgiver_aktor_id is 'Aktøren referansen til den personligebedriften som har utbetalt inntekt.';

comment on column iay_inntekt.arbeidsgiver_orgnr is 'Orgnr som betalte ut inntekten';

create index idx_inntekt_1
    on iay_inntekt (aktoer_inntekt_id);

create index idx_inntekt_2
    on iay_inntekt (kilde);

create index idx_inntekt_3
    on iay_inntekt (arbeidsgiver_orgnr);

create index idx_inntekt_4
    on iay_inntekt (arbeidsgiver_aktor_id);

create table iay_inntektspost
(
    id                          bigint                              not null
        constraint pk_inntektspost
            primary key,
    inntekt_id                  bigint                              not null
        constraint fk_inntektspost_1
            references iay_inntekt,
    inntektspost_type           varchar(100)                        not null,
    fom                         date                                not null,
    tom                         date                                not null,
    beloep                      numeric(19, 2)                      not null,
    versjon                     bigint       default 0              not null,
    opprettet_av                varchar(20)  default 'VL'::character varying not null,
    opprettet_tid               timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                   varchar(20),
    endret_tid                  timestamp(3),
    ytelse_type                 varchar(100),
    skatte_og_avgiftsregel_type varchar(100) default '-'::character varying  not null,
    lonnsinntekt_beskrivelse    varchar(100) default '-'::character varying  not null
);

comment on table iay_inntektspost is 'Utbetaling per type per periode';

comment on column iay_inntektspost.id is 'Primærnøkkel';

comment on column iay_inntektspost.inntekt_id is 'FK: Fremmednøkkel til kodeverkstabellen for inntektsposttyper';

comment on column iay_inntektspost.inntektspost_type is 'Type utbetaling, lønn eller ytelse';

comment on column iay_inntektspost.beloep is 'Utbetalt beløp';

comment on column iay_inntektspost.ytelse_type is 'Fremmednøkkel til tabell for ytelsestyper';

comment on column iay_inntektspost.skatte_og_avgiftsregel_type is 'Skatte -og avgiftsregel fra inntektskomponenten';

comment on column iay_inntektspost.lonnsinntekt_beskrivelse is 'Dersom posten er av type lønnsinntekt gir denne en mer detaljert beskrivelse av hva inntekten gjelder.';

create index idx_inntektpost_1
    on iay_inntektspost (inntekt_id);

create index idx_inntektpost_2
    on iay_inntektspost (inntektspost_type);

create index idx_inntektspost_6
    on iay_inntektspost (ytelse_type);

create index idx_inntektspost_2
    on iay_inntektspost (skatte_og_avgiftsregel_type);

create table iay_aktoer_arbeid
(
    id                        bigint                              not null
        constraint pk_aktoer_arbeid
            primary key,
    inntekt_arbeid_ytelser_id bigint                              not null
        constraint fk_aktoer_arbeid_1
            references iay_inntekt_arbeid_ytelser,
    versjon                   bigint       default 0              not null,
    opprettet_av              varchar(20)  default 'VL'::character varying not null,
    opprettet_tid             timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                 varchar(20),
    endret_tid                timestamp(3),
    aktoer_id                 varchar(50)
);

comment on table iay_aktoer_arbeid is 'Tabell med rad per aktør med arbeid eller aktiviteter som er likestilt pensjonsgivende arbeid relatert til behandlingen.';

comment on column iay_aktoer_arbeid.id is 'Primærnøkkel';

comment on column iay_aktoer_arbeid.inntekt_arbeid_ytelser_id is 'FK:';

comment on column iay_aktoer_arbeid.aktoer_id is 'Aktørid (fra NAV Aktørregister)';

create index idx_aktoer_arbeid_2
    on iay_aktoer_arbeid (inntekt_arbeid_ytelser_id);

create index idx_aktoer_arbeid_1
    on iay_aktoer_arbeid (aktoer_id);

create table iay_yrkesaktivitet
(
    id                       bigint                              not null
        constraint pk_yrkesaktivitet
            primary key,
    aktoer_arbeid_id         bigint                              not null
        constraint fk_yrkesaktivitet_1
            references iay_aktoer_arbeid,
    arbeidsgiver_aktor_id    varchar(100),
    arbeidsgiver_orgnr       varchar(100),
    arbeid_type              varchar(100)                        not null,
    versjon                  bigint       default 0              not null,
    opprettet_av             varchar(20)  default 'VL'::character varying not null,
    opprettet_tid            timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3),
    navn_arbeidsgiver_utland varchar(100),
    arbeidsforhold_intern_id uuid
);

comment on table iay_yrkesaktivitet is 'Arbeid eller aktiviteter som er likestilt pensjonsgivende arbeid';

comment on column iay_yrkesaktivitet.id is 'Primærnøkkel';

comment on column iay_yrkesaktivitet.aktoer_arbeid_id is 'FK:';

comment on column iay_yrkesaktivitet.arbeidsgiver_aktor_id is 'Aktøren referansen til den personligebedriften som har ansatt vedkommende.';

comment on column iay_yrkesaktivitet.arbeidsgiver_orgnr is 'Orgnummer til arbeidsgiver';

comment on column iay_yrkesaktivitet.arbeid_type is 'Fremmednøkkel til tabell over arbeidstyper';

comment on column iay_yrkesaktivitet.navn_arbeidsgiver_utland is 'Navn på utenlandske arbeidsgiver';

comment on column iay_yrkesaktivitet.arbeidsforhold_intern_id is 'Global unik arbeidsforhold id for intern bruk i FP familien';

create index idx_yrkesaktivitet_1
    on iay_yrkesaktivitet (aktoer_arbeid_id);

create index idx_yrkesaktivitet_2
    on iay_yrkesaktivitet (arbeid_type);

create index idx_yrkesaktivitet_3
    on iay_yrkesaktivitet (arbeidsgiver_orgnr);

create index idx_yrkesaktivitet_4
    on iay_yrkesaktivitet (arbeidsgiver_aktor_id);

create index idx_yrkesaktivitet_5
    on iay_yrkesaktivitet (arbeidsforhold_intern_id);

create index idx_yrkesaktivitet_6
    on iay_yrkesaktivitet (arbeidsgiver_aktor_id);

create table iay_permisjon
(
    id                bigint                              not null
        constraint pk_permisjon
            primary key,
    yrkesaktivitet_id bigint                              not null
        constraint fk_permisjon_1
            references iay_yrkesaktivitet,
    beskrivelse_type  varchar(100)                        not null,
    fom               date                                not null,
    tom               date                                not null,
    prosentsats       numeric(5, 2)                       not null,
    versjon           bigint       default 0              not null,
    opprettet_av      varchar(20)  default 'VL'::character varying not null,
    opprettet_tid     timestamp(3) default LOCALTIMESTAMP not null,
    endret_av         varchar(20),
    endret_tid        timestamp(3)
);

comment on table iay_permisjon is 'Oversikt over avtalt permisjon hos arbeidsgiver';

comment on column iay_permisjon.id is 'Primærnøkkel';

comment on column iay_permisjon.yrkesaktivitet_id is 'FK:';

comment on column iay_permisjon.beskrivelse_type is 'FK: Fremmednøkkel til tabell for beskrivelse av permisjonstyper';

comment on column iay_permisjon.prosentsats is 'Antall prosent permisjon';

create index idx_permisjon_1
    on iay_permisjon (yrkesaktivitet_id);

create index idx_permisjon_6
    on iay_permisjon (beskrivelse_type);

create table iay_aktivitets_avtale
(
    id                       bigint                              not null
        constraint pk_aktivitets_avtale
            primary key,
    yrkesaktivitet_id        bigint                              not null
        constraint fk_aktivitets_avtale_1
            references iay_yrkesaktivitet,
    prosentsats              numeric(5, 2),
    fom                      date                                not null,
    tom                      date                                not null,
    versjon                  bigint       default 0              not null,
    opprettet_av             varchar(20)  default 'VL'::character varying not null,
    opprettet_tid            timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3),
    beskrivelse              text,
    siste_loennsendringsdato date
);

comment on table iay_aktivitets_avtale is 'Ansettelses avtaler og avtaler om periode av en gitt type.';

comment on column iay_aktivitets_avtale.id is 'Primærnøkkel';

comment on column iay_aktivitets_avtale.yrkesaktivitet_id is 'FK:';

comment on column iay_aktivitets_avtale.prosentsats is 'Stillingsprosent';

comment on column iay_aktivitets_avtale.beskrivelse is 'Saksbehandlers vurdering om perioden. Forekommer data her kun hvis den ligger i overstyrt.';

comment on column iay_aktivitets_avtale.siste_loennsendringsdato is 'Beskriver siste lønnsendringsdato';

create index idx_aktivitets_avtale_1
    on iay_aktivitets_avtale (yrkesaktivitet_id);

create table iay_inntektsmeldinger
(
    id            bigint                              not null
        constraint pk_inntektsmeldinger
            primary key,
    versjon       bigint       default 0              not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default LOCALTIMESTAMP not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table iay_inntektsmeldinger is 'Koblingstabell mellom grunnlag og inntektsmeldinger';

comment on column iay_inntektsmeldinger.id is 'Primærnøkkel';

create table iay_inntektsmelding
(
    id                   bigint           not null
        constraint pk_inntektsmelding
            primary key,
    inntektsmeldinger_id bigint           not null
        constraint fk_inntektsmelding_1
            references iay_inntektsmeldinger,
    journalpost_id       varchar(100)     not null,
    versjon              bigint default 0 not null,
    arbeidsgiver_orgnr   varchar(100),
    inntekt_beloep       numeric(10, 2)   not null,
    start_dato_permisjon date,
    refusjon_beloep      numeric(10, 2),
    refusjon_opphoerer   date,
    naer_relasjon        varchar(1)       not null
        constraint chk_fk_inntektsmelding_1
            check ((naer_relasjon):: text = ANY ((ARRAY ['J':: character varying, 'N':: character varying]):: text [])
) ,
    opprettet_av             varchar(20)  default 'VL'::character varying not null,
    opprettet_tid            timestamp(3) default LOCALTIMESTAMP          not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3),
    innsendingsaarsak        varchar(100) default '-'::character varying  not null,
    innsendingstidspunkt     timestamp(6)                                 not null,
    arbeidsgiver_aktor_id    varchar(100),
    arbeidsforhold_intern_id uuid,
    kanalreferanse           varchar(100),
    kildesystem              varchar(200),
    mottatt_dato             date
);

comment on table iay_inntektsmelding is 'Inntektsmeldinger';

comment on column iay_inntektsmelding.id is 'Primærnøkkel';

comment on column iay_inntektsmelding.inntektsmeldinger_id is 'FK:';

comment on column iay_inntektsmelding.journalpost_id is 'Journalposten inntektsmeldingen er journalført på';

comment on column iay_inntektsmelding.arbeidsgiver_orgnr is 'Arbeidsgiver orgnr';

comment on column iay_inntektsmelding.inntekt_beloep is 'Oppgitt årslønn fra arbeidsgiver';

comment on column iay_inntektsmelding.start_dato_permisjon is 'Avtalt startdato for permisjonen fra arbeidsgiver';

comment on column iay_inntektsmelding.refusjon_beloep is 'Beløpet arbeidsgiver ønsker refundert';

comment on column iay_inntektsmelding.refusjon_opphoerer is 'Dato for når refusjonen opphører';

comment on column iay_inntektsmelding.naer_relasjon is 'Arbeidsgiver oppgir at søker har nær relasjon til arbeidsgiver';

comment on column iay_inntektsmelding.innsendingstidspunkt is 'Innsendingstidspunkt fra LPS-system. For Altinn bruker kjøretidspunkt';

comment on column iay_inntektsmelding.arbeidsgiver_aktor_id is 'AktørID i de tilfeller hvor arbeidsgiver er en person.';

comment on column iay_inntektsmelding.arbeidsforhold_intern_id is 'Global unik arbeidsforhold id for intern bruk i FP familien';

comment on column iay_inntektsmelding.kanalreferanse is 'Kildereferanse for journalposten';

comment on column iay_inntektsmelding.kildesystem is 'Kildesystem for inntektsmeldingen';

comment on column iay_inntektsmelding.mottatt_dato is 'Dato inntektsmelding mottatt';

create index idx_inntektsmelding_1
    on iay_inntektsmelding (inntektsmeldinger_id);

create index idx_inntektsmelding_2
    on iay_inntektsmelding (arbeidsgiver_orgnr);

create index idx_inntektsmelding_6
    on iay_inntektsmelding (journalpost_id);

create index idx_inntektsmelding_7
    on iay_inntektsmelding (innsendingsaarsak);

create index idx_inntektsmelding_3
    on iay_inntektsmelding (arbeidsforhold_intern_id);

create table iay_natural_ytelse
(
    id                  bigint                              not null
        constraint pk_natural_ytelse
            primary key,
    inntektsmelding_id  bigint                              not null
        constraint fk_natural_ytelse_1
            references iay_inntektsmelding,
    natural_ytelse_type varchar(100)                        not null,
    beloep_mnd          numeric(10, 2)                      not null,
    fom                 date                                not null,
    tom                 date                                not null,
    versjon             bigint       default 0              not null,
    opprettet_av        varchar(20)  default 'VL'::character varying not null,
    opprettet_tid       timestamp(3) default LOCALTIMESTAMP not null,
    endret_av           varchar(20),
    endret_tid          timestamp(3)
);

comment on table iay_natural_ytelse is 'Arbeidsgivers informasjon om oppstart og opphør av natural ytelser';

comment on column iay_natural_ytelse.id is 'Primærnøkkel';

comment on column iay_natural_ytelse.natural_ytelse_type is 'Fremmednøkkel til kodeverktabell for naturalytelsetype.';

comment on column iay_natural_ytelse.beloep_mnd is 'Verdi i kroner per måned';

comment on column iay_natural_ytelse.tom is 'Fremmednøkkel til kodeverktabell for naturalytelsetype.';

create index idx_natural_ytelse_1
    on iay_natural_ytelse (inntektsmelding_id);

create index idx_natural_ytelse_6
    on iay_natural_ytelse (natural_ytelse_type);

create table iay_gradering
(
    id                 bigint                              not null
        constraint pk_gradering
            primary key,
    inntektsmelding_id bigint                              not null
        constraint fk_gradering_1
            references iay_inntektsmelding,
    arbeidstid_prosent numeric(5, 2)                       not null,
    fom                date                                not null,
    tom                date                                not null,
    versjon            bigint       default 0              not null,
    opprettet_av       varchar(20)  default 'VL'::character varying not null,
    opprettet_tid      timestamp(3) default LOCALTIMESTAMP not null,
    endret_av          varchar(20),
    endret_tid         timestamp(3)
);

comment on table iay_gradering is 'Arbeidsgivers informasjon om gradering';

comment on column iay_gradering.id is 'Primærnøkkel';

comment on column iay_gradering.inntektsmelding_id is 'FK:';

comment on column iay_gradering.arbeidstid_prosent is 'Avtalt arbeidstid i prosent under gradering';

create index idx_gradering_1
    on iay_gradering (inntektsmelding_id);

create table iay_utsettelse_periode
(
    id                     bigint                              not null
        constraint pk_utsettelse_periode
            primary key,
    inntektsmelding_id     bigint                              not null
        constraint fk_utsettelse_periode_1
            references iay_inntektsmelding,
    utsettelse_aarsak_type varchar(100)                        not null,
    fom                    date                                not null,
    tom                    date                                not null,
    versjon                bigint       default 0              not null,
    opprettet_av           varchar(20)  default 'VL'::character varying not null,
    opprettet_tid          timestamp(3) default LOCALTIMESTAMP not null,
    endret_av              varchar(20),
    endret_tid             timestamp(3)
);

comment on table iay_utsettelse_periode is 'Arbeidsgivers informasjon om utsettelser.';

comment on column iay_utsettelse_periode.id is 'Primærnøkkel';

comment on column iay_utsettelse_periode.utsettelse_aarsak_type is 'Fremmednøkkel til kodeverkstabellen som beskriver uttsettelsesårsaker';

create index idx_utsettelse_periode_1
    on iay_utsettelse_periode (inntektsmelding_id);

create index idx_utsettelse_periode_6
    on iay_utsettelse_periode (utsettelse_aarsak_type);

create table iay_oppgitt_opptjening
(
    id                       bigint                              not null
        constraint pk_oppgitt_opptjening
            primary key,
    opprettet_av             varchar(20)  default 'VL'::character varying not null,
    opprettet_tid            timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3),
    ekstern_referanse        uuid,
    versjon                  bigint       default 0              not null,
    oppgitte_opptjeninger_id bigint,
    journalpost_id           varchar(100),
    innsendingstidspunkt     timestamp(6)
);

comment on table iay_oppgitt_opptjening is 'Inneholder brukers oppgitte opplysninger om arbeid eller aktiviteter som er likestilt pensjonsgivende arbeid.';

comment on column iay_oppgitt_opptjening.id is 'Primærnøkkel';

comment on column iay_oppgitt_opptjening.ekstern_referanse is 'Unik UUID for behandling til utvortes bruk. Representerer en immutable og unikt identifiserbar instans av disse opplysningene';

comment on column iay_oppgitt_opptjening.oppgitte_opptjeninger_id is 'Fremmednøkkel til IAY_OPPGITTE_OPPTJENINGER';

comment on column iay_oppgitt_opptjening.journalpost_id is 'Journalposten dokument (søknad) med oppgitt opptjening er journalført på';

comment on column iay_oppgitt_opptjening.innsendingstidspunkt is 'Innsendingstidspunkt (sannsynligvis fra søknadsdialogen)';

create unique index uidx_iay_oppgitt_opptje_01
    on iay_oppgitt_opptjening (oppgitte_opptjeninger_id, ekstern_referanse);

create table iay_oppgitt_arbeidsforhold
(
    id                         bigint                              not null
        constraint pk_oppgitt_arbeidsforhold
            primary key,
    oppgitt_opptjening_id      bigint                              not null
        constraint fk_arbeidsforhold_1
            references iay_oppgitt_opptjening,
    fom                        date                                not null,
    tom                        date                                not null,
    utenlandsk_inntekt         varchar(1)                          not null,
    arbeid_type                varchar(100)                        not null,
    utenlandsk_virksomhet_navn varchar(100),
    land                       varchar(100),
    opprettet_av               varchar(20)  default 'VL'::character varying not null,
    opprettet_tid              timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                  varchar(20),
    endret_tid                 timestamp(3),
    versjon                    bigint       default 0              not null,
    inntekt                    numeric(10, 2)
);

comment on table iay_oppgitt_arbeidsforhold is 'Oppgitt informasjon om arbeidsforhold';

comment on column iay_oppgitt_arbeidsforhold.id is 'Primærnøkkel';

comment on column iay_oppgitt_arbeidsforhold.oppgitt_opptjening_id is 'FK:';

comment on column iay_oppgitt_arbeidsforhold.utenlandsk_inntekt is 'Inntekt fra utenlandsk arbeidsforhold';

comment on column iay_oppgitt_arbeidsforhold.arbeid_type is 'Fremmednøkkel til tabell over arbeidstyper';

comment on column iay_oppgitt_arbeidsforhold.utenlandsk_virksomhet_navn is 'Navn på virksomheten i utlandet hvis det er der den finnes';

comment on column iay_oppgitt_arbeidsforhold.inntekt is 'Oppgitt inntekt for arbeidshold i perioden';

create index idx_oppgitt_arbeidsforhold_1
    on iay_oppgitt_arbeidsforhold (oppgitt_opptjening_id);

create index idx_oppgitt_arbeidsforhold_3
    on iay_oppgitt_arbeidsforhold (arbeid_type);

create index idx_oppgitt_arbeidsforhold_4
    on iay_oppgitt_arbeidsforhold (land);

create table iay_egen_naering
(
    id                         bigint                              not null
        constraint pk_egen_naering
            primary key,
    oppgitt_opptjening_id      bigint                              not null
        constraint fk_egen_naering_1
            references iay_oppgitt_opptjening,
    fom                        date                                not null,
    tom                        date                                not null,
    virksomhet_type            varchar(100),
    org_nummer                 varchar(100),
    regnskapsfoerer_navn       varchar(400),
    regnskapsfoerer_tlf        varchar(100),
    endring_dato               date,
    begrunnelse                text,
    brutto_inntekt             numeric(11, 2),
    utenlandsk_virksomhet_navn varchar(100),
    land                       varchar(100),
    opprettet_av               varchar(20)  default 'VL'::character varying not null,
    opprettet_tid              timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                  varchar(20),
    endret_tid                 timestamp(3),
    nyoppstartet               varchar(1)   default 'N'::character varying  not null,
    varig_endring              varchar(1)   default 'N'::character varying  not null,
    ny_i_arbeidslivet          varchar(1)   default 'N'::character varying  not null,
    naer_relasjon              varchar(1)                          not null,
    versjon                    bigint       default 0              not null
);

comment on table iay_egen_naering is 'Oppgitt informasjon om egen næringsvirksomhet';

comment on column iay_egen_naering.id is 'Primærnøkkel';

comment on column iay_egen_naering.oppgitt_opptjening_id is 'FK.';

comment on column iay_egen_naering.org_nummer is 'Orgnummer';

comment on column iay_egen_naering.regnskapsfoerer_navn is 'Navn på oppgitt regnskapsfører';

comment on column iay_egen_naering.regnskapsfoerer_tlf is 'Telefonnr til oppgitt regnskapsfører';

comment on column iay_egen_naering.endring_dato is 'Dato for endring i næringen';

comment on column iay_egen_naering.begrunnelse is 'Saksbehandlers vurderinger av avslag på perioden';

comment on column iay_egen_naering.brutto_inntekt is 'Brutto inntekt';

comment on column iay_egen_naering.utenlandsk_virksomhet_navn is 'Navn på virksomheten i utlandet hvis det er der den finnes.';

comment on column iay_egen_naering.nyoppstartet is 'Er næringen startet opp innenfor siste ligningsåret';

comment on column iay_egen_naering.varig_endring is 'Om det i søknaden er angitt varig endring i næring';

comment on column iay_egen_naering.ny_i_arbeidslivet is 'J hvis søker er ny i arbeidslivet';

comment on column iay_egen_naering.naer_relasjon is 'Om det i søknaden er angitt nær relasjon for egen næring';

create index idx_egen_naering_1
    on iay_egen_naering (oppgitt_opptjening_id);

create index idx_egen_naering_2
    on iay_egen_naering (org_nummer);

create index idx_egen_naering_3
    on iay_egen_naering (virksomhet_type);

create index idx_egen_naering_6
    on iay_egen_naering (land);

create table iay_annen_aktivitet
(
    id                    bigint                              not null
        constraint pk_annen_aktivitet
            primary key,
    oppgitt_opptjening_id bigint                              not null
        constraint fk_annen_aktivitet_1
            references iay_oppgitt_opptjening,
    fom                   date                                not null,
    tom                   date                                not null,
    arbeid_type           varchar(100)                        not null,
    opprettet_av          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid         timestamp(3) default LOCALTIMESTAMP not null,
    endret_av             varchar(20),
    endret_tid            timestamp(3)
);

comment on table iay_annen_aktivitet is 'Aktiviteter som er likestilt pensjonsgivende arbeid.';

comment on column iay_annen_aktivitet.id is 'Primærnøkkel';

comment on column iay_annen_aktivitet.oppgitt_opptjening_id is 'FK:';

create index idx_annen_aktivitet_1
    on iay_annen_aktivitet (oppgitt_opptjening_id);

create index idx_annen_aktivitet_6
    on iay_annen_aktivitet (arbeid_type);

create table iay_aktoer_ytelse
(
    id                        bigint                              not null
        constraint pk_aktoer_ytelse
            primary key,
    inntekt_arbeid_ytelser_id bigint                              not null
        constraint fk_aktoer_ytelse_1
            references iay_inntekt_arbeid_ytelser,
    versjon                   bigint       default 0              not null,
    opprettet_av              varchar(20)  default 'VL'::character varying not null,
    opprettet_tid             timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                 varchar(20),
    endret_tid                timestamp(3),
    aktoer_id                 varchar(50)
);

comment on table iay_aktoer_ytelse is 'Tabell med rad per aktør med tilstøtende ytelser relatert til behandlingen.';

comment on column iay_aktoer_ytelse.id is 'Primærnøkkel';

comment on column iay_aktoer_ytelse.inntekt_arbeid_ytelser_id is 'FK';

comment on column iay_aktoer_ytelse.aktoer_id is 'Aktørid (fra NAV Aktørregister)';

create index idx_aktoer_ytelse_2
    on iay_aktoer_ytelse (inntekt_arbeid_ytelser_id);

create index idx_aktoer_ytelse_1
    on iay_aktoer_ytelse (aktoer_id);

create table iay_relatert_ytelse
(
    id                bigint                              not null
        constraint pk_ytelse
            primary key,
    aktoer_ytelse_id  bigint                              not null
        constraint fk_ytelse_4
            references iay_aktoer_ytelse,
    ytelse_type       varchar(100)                        not null,
    fom               date                                not null,
    tom               date                                not null,
    status            varchar(100) default NULL::character varying not null,
    kilde             varchar(100)                        not null,
    versjon           bigint       default 0              not null,
    opprettet_av      varchar(20)  default 'VL'::character varying not null,
    opprettet_tid     timestamp(3) default LOCALTIMESTAMP not null,
    endret_av         varchar(20),
    endret_tid        timestamp(3),
    saksnummer        varchar(19),
    saksreferanse     varchar(36),
    vedtatt_tidspunkt timestamp(3)
);

comment on table iay_relatert_ytelse is 'En tabell med informasjon om ytelser fra Arena og Infotrygd';

comment on column iay_relatert_ytelse.id is 'Primærnøkkel';

comment on column iay_relatert_ytelse.aktoer_ytelse_id is 'FK:AKTOER_YTELSE';

comment on column iay_relatert_ytelse.ytelse_type is 'Type ytelse for eksempel sykepenger, foreldrepenger.. (dagpenger?) etc';

comment on column iay_relatert_ytelse.fom is 'Startdato for ytelsten. Er tilsvarende Identdato fra Infotrygd.';

comment on column iay_relatert_ytelse.tom is 'Sluttdato er en utledet dato enten fra opphørFOM eller fra identdaot pluss periode';

comment on column iay_relatert_ytelse.status is 'Er om ytelsen er ÅPEN, LØPENDE eller AVSLUTTET';

comment on column iay_relatert_ytelse.kilde is 'Hvilket system informasjonen kommer fra';

comment on column iay_relatert_ytelse.saksnummer is 'Obsolete - bruk kolonne saksreferanse';

comment on column iay_relatert_ytelse.saksreferanse is 'Referanse til saken som er innhentet';

comment on column iay_relatert_ytelse.vedtatt_tidspunkt is 'Tidspunktet vedtaket ble fattet';

create index idx_ytelse_1
    on iay_relatert_ytelse (aktoer_ytelse_id);

create index idx_relatert_ytelse_6
    on iay_relatert_ytelse (ytelse_type);

create index idx_relatert_ytelse_7
    on iay_relatert_ytelse (status);

create index idx_relatert_ytelse_8
    on iay_relatert_ytelse (kilde);

create table iay_ytelse_grunnlag
(
    id                       bigint                              not null
        constraint pk_ytelse_grunnlag
            primary key,
    ytelse_id                bigint                              not null
        constraint fk_ytelse_grunnlag_2
            references iay_relatert_ytelse,
    opprinnelig_identdato    date,
    dekningsgrad_prosent     numeric(5, 2),
    gradering_prosent        numeric(5, 2),
    inntektsgrunnlag_prosent numeric(5, 2),
    versjon                  bigint       default 0              not null,
    opprettet_av             varchar(20)  default 'VL'::character varying not null,
    opprettet_tid            timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3),
    arbeidskategori          varchar(100)                        not null,
    dagsats                  numeric(19, 2)
);

comment on table iay_ytelse_grunnlag is 'En tabell med informasjon om ytelsesgrunnlag fra Arena og Infotrygd';

comment on column iay_ytelse_grunnlag.id is 'Primærnøkkel';

comment on column iay_ytelse_grunnlag.ytelse_id is 'FK:YTELSE';

comment on column iay_ytelse_grunnlag.opprinnelig_identdato is 'Identdato (samme som stardato. kan hende denne er overflødig';

comment on column iay_ytelse_grunnlag.dekningsgrad_prosent is 'Dekningsgrad hentet fra infotrygd';

comment on column iay_ytelse_grunnlag.gradering_prosent is 'Gradering hentet fra infotrygd';

comment on column iay_ytelse_grunnlag.inntektsgrunnlag_prosent is 'Inntektsgrunnlag hentet fra infotrygd';

comment on column iay_ytelse_grunnlag.arbeidskategori is 'FK:ARBEIDSKATEGORI';

comment on column iay_ytelse_grunnlag.dagsats is 'Vedtakets dagsats fra Arena';

create index idx_ytelse_grunnlag_1
    on iay_ytelse_grunnlag (ytelse_id);

create table iay_ytelse_stoerrelse
(
    id                 bigint                              not null
        constraint pk_ytelse_stoerrelse
            primary key,
    ytelse_grunnlag_id bigint                              not null
        constraint fk_ytelse_stoerrelse_2
            references iay_ytelse_grunnlag,
    org_nummer         varchar(100),
    beloep             numeric(19, 2)                      not null,
    hyppighet          varchar(100)                        not null,
    versjon            bigint       default 0              not null,
    opprettet_av       varchar(20)  default 'VL'::character varying not null,
    opprettet_tid      timestamp(3) default LOCALTIMESTAMP not null,
    endret_av          varchar(20),
    endret_tid         timestamp(3),
    er_refusjon        boolean
);

comment on table iay_ytelse_stoerrelse is 'En tabell med informasjon om beløpene som kommer fra ytelsesgrunnlag fra Arena og Infotrygd';

comment on column iay_ytelse_stoerrelse.id is 'FK:YTELSE_GRUNNLAG Primærnøkkel';

comment on column iay_ytelse_stoerrelse.ytelse_grunnlag_id is 'FK:YTELSE_GRUNNLAG';

comment on column iay_ytelse_stoerrelse.org_nummer is 'FK:VIRKSOMHET';

comment on column iay_ytelse_stoerrelse.beloep is 'Beløpet som er for den gitte perioden i ytelsesgrunnlag';

comment on column iay_ytelse_stoerrelse.hyppighet is 'Hyppigheten for beløpet';

comment on column iay_ytelse_stoerrelse.er_refusjon is 'Sier om beløpet er refusjon eller direkteutbetaling';

create index idx_ytelse_stoerrelse_1
    on iay_ytelse_stoerrelse (ytelse_grunnlag_id);

create index idx_ytelse_stoerrelse_2
    on iay_ytelse_stoerrelse (org_nummer);

create index idx_ytelse_stoerrelse_3
    on iay_ytelse_stoerrelse (hyppighet);

create table iay_ytelse_anvist
(
    id                      bigint                              not null
        constraint pk_ytelse_anvist
            primary key,
    ytelse_id               bigint                              not null
        constraint fk_ytelse_anvist_1
            references iay_relatert_ytelse,
    beloep                  numeric(19, 2),
    fom                     date                                not null,
    tom                     date                                not null,
    utbetalingsgrad_prosent numeric(5, 2),
    versjon                 bigint       default 0              not null,
    opprettet_av            varchar(20)  default 'VL'::character varying not null,
    opprettet_tid           timestamp(3) default LOCALTIMESTAMP not null,
    endret_av               varchar(20),
    endret_tid              timestamp(3),
    dagsats                 numeric(19, 2)
);

comment on table iay_ytelse_anvist is 'En tabell med informasjon om ytelsesperioder';

comment on column iay_ytelse_anvist.id is 'PK';

comment on column iay_ytelse_anvist.ytelse_id is 'FK:YTELSE Fremmednøkkel til kodeverktabellen over ytelser???';

comment on column iay_ytelse_anvist.beloep is 'Beløp ifm utbetaling.';

comment on column iay_ytelse_anvist.fom is 'Anvist periode første dag.';

comment on column iay_ytelse_anvist.tom is 'Anvist periode siste dag.';

comment on column iay_ytelse_anvist.utbetalingsgrad_prosent is 'Utbetalingsprosent fra kildesystem.';

comment on column iay_ytelse_anvist.dagsats is 'Dagsatsen på den relaterte ytelsen';

create index idx_ytelse_anvist_1
    on iay_ytelse_anvist (ytelse_id);

create table iay_informasjon
(
    id            bigint                              not null
        constraint pk_informasjon
            primary key,
    versjon       bigint       default 0              not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default LOCALTIMESTAMP not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table iay_informasjon is 'Mange til mange tabell for arbeidsforhold referanse og overstyrende betraktninger om arbeidsforhold';

create table iay_arbeidsforhold_refer
(
    id                    bigint                              not null
        constraint pk_arbeidsforhold_refer
            primary key,
    informasjon_id        bigint                              not null
        constraint fk_arbeidsforhold_refer_1
            references iay_informasjon,
    ekstern_referanse     varchar(100)                        not null,
    arbeidsgiver_aktor_id varchar(100),
    arbeidsgiver_orgnr    varchar(100),
    versjon               bigint       default 0              not null,
    opprettet_av          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid         timestamp(3) default LOCALTIMESTAMP not null,
    endret_av             varchar(20),
    endret_tid            timestamp(3),
    intern_referanse      uuid                                not null
);

comment on table iay_arbeidsforhold_refer is 'Kobling mellom arbeidsforhold fra aa-reg og intern nøkkel for samme representasjon';

comment on column iay_arbeidsforhold_refer.ekstern_referanse is 'ArbeidsforholdId hentet fra AA-reg';

comment on column iay_arbeidsforhold_refer.arbeidsgiver_aktor_id is 'Aktør til personlig foretak.';

comment on column iay_arbeidsforhold_refer.arbeidsgiver_orgnr is 'Orgnr til arbeidsgiver';

comment on column iay_arbeidsforhold_refer.intern_referanse is 'Global unik arbeidsforhold id for intern bruk i FP familien';

create index idx_arbeidsforhold_refer_2
    on iay_arbeidsforhold_refer (arbeidsgiver_orgnr);

create index idx_arbeidsforhold_refer_3
    on iay_arbeidsforhold_refer (informasjon_id);

create index idx_arbeidsforhold_refer_4
    on iay_arbeidsforhold_refer (ekstern_referanse);

create index idx_arbeidsforhold_refer_5
    on iay_arbeidsforhold_refer (arbeidsgiver_aktor_id);

create index idx_arbeidsforhold_refer_1
    on iay_arbeidsforhold_refer (intern_referanse);

create unique index uidx_arbeidsforhold_refer_01
    on iay_arbeidsforhold_refer (informasjon_id, ekstern_referanse, arbeidsgiver_orgnr, arbeidsgiver_aktor_id)
    where
    (opprettet_tid > '2021-06-30 00:00:00':: timestamp without time zone);

create table iay_arbeidsforhold
(
    id                          bigint                              not null
        constraint pk_arbeidsforhold
            primary key,
    informasjon_id              bigint                              not null
        constraint fk_arbeidsforhold_1
            references iay_informasjon,
    arbeidsgiver_aktor_id       varchar(100),
    arbeidsgiver_orgnr          varchar(100),
    begrunnelse                 text,
    handling_type               varchar(100)                        not null,
    versjon                     bigint       default 0              not null,
    opprettet_av                varchar(20)  default 'VL'::character varying not null,
    opprettet_tid               timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                   varchar(20),
    endret_tid                  timestamp(3),
    bekreftet_tom_dato          date,
    arbeidsgiver_navn           varchar(100),
    stillingsprosent            numeric(5, 2),
    bekreftet_permisjon_status  varchar(100),
    bekreftet_permisjon_fom     date,
    bekreftet_permisjon_tom     date,
    arbeidsforhold_intern_id    uuid,
    arbeidsforhold_intern_id_ny uuid
);

comment on table iay_arbeidsforhold is 'Overstyrende betraktninger om arbeidsforhold';

comment on column iay_arbeidsforhold.arbeidsgiver_aktor_id is 'Personlig foretak som arbeidsgiver';

comment on column iay_arbeidsforhold.arbeidsgiver_orgnr is 'Foretak som arbeidsgiver';

comment on column iay_arbeidsforhold.begrunnelse is 'Saksbehandlers begrunnelsen for tiltaket';

comment on column iay_arbeidsforhold.bekreftet_tom_dato is 'Til og med dato fastsatt av saksbehandler';

comment on column iay_arbeidsforhold.arbeidsgiver_navn is 'Kjært navn for arbeidsgiver angitt av saksbehandler';

comment on column iay_arbeidsforhold.stillingsprosent is 'Stillingsprosent for arbeidsgiver overstyr av saksbehandler';

comment on column iay_arbeidsforhold.bekreftet_permisjon_status is 'Angi om permisjonperiode skal brukes (dersom saksbehandler har tatt stilling til det). Ellers bruk permisjon fra IAY_PERMISJON';

comment on column iay_arbeidsforhold.bekreftet_permisjon_fom is 'Startdato for permisjon som skal brukes opplyst av saksbehandler (dersom saksbehandler har tatt stilling til det). Ellers bruk permisjon fra IAY_PERMISJON';

comment on column iay_arbeidsforhold.bekreftet_permisjon_tom is 'Siste dato for permisjon som skal brukes opplyst av saksbehandler (dersom saksbehandler har tatt stilling til det). Ellers bruk permisjon fra IAY_PERMISJON';

comment on column iay_arbeidsforhold.arbeidsforhold_intern_id is 'Global unik arbeidsforhold id for intern bruk i FP familien';

comment on column iay_arbeidsforhold.arbeidsforhold_intern_id_ny is 'Global unik arbeidsforhold id for intern bruk i FP familien';

create index idx_arbeidsforhold_1
    on iay_arbeidsforhold (informasjon_id);

create index idx_arbeidsforhold_2
    on iay_arbeidsforhold (arbeidsgiver_orgnr);

create index idx_arbeidsforhold_3
    on iay_arbeidsforhold (handling_type);

create index idx_arbeidsforhold_4
    on iay_arbeidsforhold (arbeidsgiver_aktor_id);

create index idx_arbeidsforhold_5
    on iay_arbeidsforhold (arbeidsforhold_intern_id);

create index idx_arbeidsforhold_6
    on iay_arbeidsforhold (arbeidsforhold_intern_id_ny);

create index idx_iay_arbeidsforhold_4
    on iay_arbeidsforhold (bekreftet_permisjon_status);

create table iay_refusjon
(
    id                  bigint                              not null
        constraint pk_refusjon
            primary key,
    inntektsmelding_id  bigint                              not null
        constraint fk_refusjon_1
            references iay_inntektsmelding,
    refusjonsbeloep_mnd numeric(10, 2)                      not null,
    fom                 date                                not null,
    versjon             bigint       default 0              not null,
    opprettet_av        varchar(20)  default 'VL'::character varying not null,
    opprettet_tid       timestamp(3) default LOCALTIMESTAMP not null,
    endret_av           varchar(20),
    endret_tid          timestamp(3)
);

comment on table iay_refusjon is 'Endringer i refusjonsbeløp fra en oppgitt dato';

comment on column iay_refusjon.id is 'Primær nøkkel';

comment on column iay_refusjon.inntektsmelding_id is 'Fremmednøkkel til inntektsmelding';

comment on column iay_refusjon.refusjonsbeloep_mnd is 'Verdi i kroner per måned';

comment on column iay_refusjon.fom is 'Dato refusjonsbeløpet gjelder fra';

create index idx_refusjon_1
    on iay_refusjon (inntektsmelding_id);

create table iay_oppgitt_frilans
(
    id                     bigint                              not null
        constraint pk_oppgitt_frilans
            primary key,
    oppgitt_opptjening_id  bigint                              not null
        constraint fk_oppgitt_frilans
            references iay_oppgitt_opptjening,
    inntekt_fra_fosterhjem varchar(1)                          not null,
    nyoppstartet           varchar(1)                          not null,
    naer_relasjon          varchar(1)                          not null,
    versjon                bigint       default 0              not null,
    opprettet_av           varchar(20)  default 'VL'::character varying not null,
    opprettet_tid          timestamp(3) default LOCALTIMESTAMP not null,
    endret_av              varchar(20),
    endret_tid             timestamp(3)
);

comment on table iay_oppgitt_frilans is 'Frilans oppgitt av søker';

comment on column iay_oppgitt_frilans.id is 'Primary Key';

comment on column iay_oppgitt_frilans.oppgitt_opptjening_id is 'FOREIGN KEY';

comment on column iay_oppgitt_frilans.inntekt_fra_fosterhjem is 'J hvis inntekt fra forsterhjem';

comment on column iay_oppgitt_frilans.nyoppstartet is 'J hvis nyoppstartet';

comment on column iay_oppgitt_frilans.naer_relasjon is 'J hvis nær relasjon';

create index idx_oppgitt_f_1
    on iay_oppgitt_frilans (oppgitt_opptjening_id);

create table iay_oppgitt_frilansoppdrag
(
    id            bigint                              not null
        constraint pk_oppgitt_frilansoppdrag
            primary key,
    frilans_id    bigint                              not null
        constraint fk_oppgitt_frilansoppdrag
            references iay_oppgitt_frilans,
    fom           date                                not null,
    tom           date                                not null,
    oppdragsgiver varchar(100),
    versjon       bigint       default 0              not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default LOCALTIMESTAMP not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3),
    inntekt       numeric(10, 2)
);

comment on table iay_oppgitt_frilansoppdrag is 'Frilansoppdrag oppgitt av søker';

comment on column iay_oppgitt_frilansoppdrag.id is 'Primary Key';

comment on column iay_oppgitt_frilansoppdrag.frilans_id is 'FOREIGN KEY';

comment on column iay_oppgitt_frilansoppdrag.fom is 'Periode start';

comment on column iay_oppgitt_frilansoppdrag.tom is 'Periode slutt';

comment on column iay_oppgitt_frilansoppdrag.oppdragsgiver is 'Oppdragsgiver';

comment on column iay_oppgitt_frilansoppdrag.inntekt is 'Frilansinntekt i perioden';

create index idx_oppgitt_fo_1
    on iay_oppgitt_frilansoppdrag (frilans_id);

create table gr_arbeid_inntekt
(
    id                              bigint                              not null
        constraint pk_gr_arbeid_inntekt
            primary key,
    kobling_id                      bigint                              not null
        constraint fk_gr_arbeid_inntekt_1
            references kobling,
    grunnlag_referanse              uuid                                not null
        constraint uidx_gr_arbeid_inntekt_3
            unique,
    register_id                     bigint
        constraint fk_gr_arbeid_inntekt_2
            references iay_inntekt_arbeid_ytelser,
    saksbehandlet_id                bigint
        constraint fk_gr_arbeid_inntekt_5
            references iay_inntekt_arbeid_ytelser,
    versjon                         bigint       default 0              not null,
    opprettet_av                    varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                   timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                       varchar(20),
    endret_tid                      timestamp(3),
    inntektsmeldinger_id            bigint
        constraint fk_gr_arbeid_inntekt_3
            references iay_inntektsmeldinger,
    oppgitt_opptjening_id           bigint
        constraint fk_gr_arbeid_inntekt_4
            references iay_oppgitt_opptjening,
    informasjon_id                  bigint
        constraint fk_gr_arbeid_inntekt_6
            references iay_informasjon,
    aktiv                           varchar(1)   default 'J'::character varying  not null
        constraint chk_aktiv2
        check ((aktiv)::text = ANY ((ARRAY ['J'::character varying, 'N'::character varying])::text[])),
    overstyrt_oppgitt_opptjening_id bigint
        constraint fk_gr_arbeid_inntekt_7
            references iay_oppgitt_opptjening,
    oppgitte_opptjeninger_id        bigint
);

comment on table gr_arbeid_inntekt is 'Behandlingsgrunnlag for arbeid, inntekt og ytelser (aggregat)';

comment on column gr_arbeid_inntekt.id is 'Primary Key';

comment on column gr_arbeid_inntekt.kobling_id is 'FK: Fremmednøkkel til KOBLING';

comment on column gr_arbeid_inntekt.grunnlag_referanse is 'Unik referanse til grunnlaget, returneres til systemet som etterspør informasjonen.';

comment on column gr_arbeid_inntekt.register_id is 'Arbeid inntekt register før skjæringstidspunkt';

comment on column gr_arbeid_inntekt.saksbehandlet_id is 'Arbeid inntekt saksbehandlet før skjæringstidspunkt';

comment on column gr_arbeid_inntekt.inntektsmeldinger_id is 'FK: Fremmednøkkel for kobling til inntektsmeldinger';

comment on column gr_arbeid_inntekt.oppgitt_opptjening_id is 'FK: Fremmenøkkel for kobling til egen oppgitt opptjening';

comment on column gr_arbeid_inntekt.overstyrt_oppgitt_opptjening_id is 'FK: Fremmenøkkel for kobling til overstyring av egen oppgitt opptjening';

comment on column gr_arbeid_inntekt.oppgitte_opptjeninger_id is 'Kobling mellom grunnlag og oppgitt opptjeninger-aggregat';

create index idx_gr_arbeid_inntekt_1
    on gr_arbeid_inntekt (kobling_id);

create index idx_gr_arbeid_inntekt_2
    on gr_arbeid_inntekt (register_id);

create index idx_gr_arbeid_inntekt_3
    on gr_arbeid_inntekt (inntektsmeldinger_id);

create index idx_gr_arbeid_inntekt_4
    on gr_arbeid_inntekt (saksbehandlet_id);

create index idx_gr_arbeid_inntekt_5
    on gr_arbeid_inntekt (oppgitt_opptjening_id);

create index idx_gr_arbeid_inntekt_6
    on gr_arbeid_inntekt (informasjon_id);

create index idx_gr_arbeid_inntekt_7
    on gr_arbeid_inntekt (grunnlag_referanse);

create index idx_gr_arbeid_inntekt_8
    on gr_arbeid_inntekt (overstyrt_oppgitt_opptjening_id);

create unique index uidx_gr_arbeid_inntekt_02
    on gr_arbeid_inntekt (kobling_id)
    where
    ((aktiv):: text = 'J':: text);

create table vedtak_ytelse
(
    id                   bigint                              not null
        constraint pk_vedtak_ytelse
            primary key,
    aktoer_id            varchar(50)                         not null,
    ytelse_type          varchar(100)                        not null,
    vedtatt_tidspunkt    timestamp(3)                        not null,
    vedtak_referanse     uuid                                not null,
    fom                  date                                not null,
    tom                  date                                not null,
    status               varchar(100)                        not null,
    kilde                varchar(100)                        not null,
    versjon              bigint       default 0              not null,
    opprettet_av         varchar(20)  default 'VL'::character varying not null,
    opprettet_tid        timestamp(3) default LOCALTIMESTAMP not null,
    endret_av            varchar(20),
    endret_tid           timestamp(3),
    saksnummer           varchar(19),
    aktiv                varchar(1)   default 'J'::character varying  not null
        constraint chk_vedtak_ytelse_aktiv
        check ((aktiv)::text = ANY ((ARRAY ['J'::character varying, 'N'::character varying])::text[])),
    tilleggsopplysninger text
);

comment on table vedtak_ytelse is 'En tabell med informasjon om ytelser fra Arena og Infotrygd';

comment on column vedtak_ytelse.id is 'Primærnøkkel';

comment on column vedtak_ytelse.aktoer_id is 'Stønadsmottakeren';

comment on column vedtak_ytelse.ytelse_type is 'Type ytelse for eksempel sykepenger, foreldrepenger.. (dagpenger?) etc';

comment on column vedtak_ytelse.vedtatt_tidspunkt is 'Tidspunktet hvor vedtaket ble fattet';

comment on column vedtak_ytelse.vedtak_referanse is 'Referanse til vedtaket. Kan benyttes til å etterspørre mer informasjon om vedtaket.';

comment on column vedtak_ytelse.fom is 'Startdato for ytelsten. Er tilsvarende Identdato fra Infotrygd.';

comment on column vedtak_ytelse.tom is 'Sluttdato er en utledet dato enten fra opphørFOM eller fra identdaot pluss periode';

comment on column vedtak_ytelse.status is 'Er om ytelsen er ÅPEN, LØPENDE eller AVSLUTTET';

comment on column vedtak_ytelse.kilde is 'Hvilket system informasjonen kommer fra';

comment on column vedtak_ytelse.saksnummer is 'Saksnummer i GSAK';

comment on column vedtak_ytelse.aktiv is 'Er innslaget aktivt';

comment on column vedtak_ytelse.tilleggsopplysninger is 'Beskrivelse av ytelse eller vedtak';

create index idx_vedtak_ytelse_1
    on vedtak_ytelse (aktoer_id);

create index idx_vedtak_ytelse_6
    on vedtak_ytelse (ytelse_type);

create index idx_vedtak_ytelse_7
    on vedtak_ytelse (status);

create index idx_vedtak_ytelse_8
    on vedtak_ytelse (kilde);

create index idx_vedtak_ytelse_10
    on vedtak_ytelse (aktiv);

create index idx_vedtak_ytelse_11
    on vedtak_ytelse (vedtatt_tidspunkt);

create unique index uidx_vedtak_ytelse_02
    on vedtak_ytelse (saksnummer, aktoer_id, kilde, ytelse_type)
    where
    ((aktiv):: text = 'J':: text);

create table ve_ytelse_anvist
(
    id                      bigint                              not null
        constraint pk_ve_ytelse_anvist
            primary key,
    ytelse_id               bigint                              not null
        constraint fk_ve_ytelse_anvist_1
            references vedtak_ytelse,
    beloep                  numeric(19, 2),
    fom                     date                                not null,
    tom                     date                                not null,
    utbetalingsgrad_prosent numeric(5, 2),
    versjon                 bigint       default 0              not null,
    opprettet_av            varchar(20)  default 'VL'::character varying not null,
    opprettet_tid           timestamp(3) default LOCALTIMESTAMP not null,
    endret_av               varchar(20),
    endret_tid              timestamp(3),
    dagsats                 numeric(19, 2)
);

comment on table ve_ytelse_anvist is 'En tabell med informasjon om ytelsesperioder';

comment on column ve_ytelse_anvist.id is 'PK';

comment on column ve_ytelse_anvist.ytelse_id is 'FK:YTELSE Fremmednøkkel til kodeverktabellen over ytelser???';

comment on column ve_ytelse_anvist.beloep is 'Beløp ifm utbetaling.';

comment on column ve_ytelse_anvist.fom is 'Anvist periode første dag.';

comment on column ve_ytelse_anvist.tom is 'Anvist periode siste dag.';

comment on column ve_ytelse_anvist.utbetalingsgrad_prosent is 'Utbetalingsprosent fra kildesystem.';

comment on column ve_ytelse_anvist.dagsats is 'Dagsatsen på den relaterte ytelsen';

create index idx_ve_ytelse_anvist_1
    on ve_ytelse_anvist (ytelse_id);

create table iay_overstyrte_perioder
(
    id                bigint                                            not null
        constraint pk_iay_overstyrte_perioder
            primary key,
    fom               date                                              not null,
    tom               date                                              not null,
    arbeidsforhold_id bigint                                            not null
        constraint fk_iay_overstyrte_perioder
            references iay_arbeidsforhold,
    versjon           bigint       default 0                            not null,
    opprettet_av      varchar(20)  default 'VL'::character varying      not null,
    opprettet_tid     timestamp(3) default timezone('utc'::text, now()) not null,
    endret_av         varchar(20),
    endret_tid        timestamp(3)
);

comment on table iay_overstyrte_perioder is 'En tabell for overstyrte ansettelsesperioder for arbeidsforhold.';

comment on column iay_overstyrte_perioder.fom is 'Fra og med dato til perioden for arbeidsforholdet.';

comment on column iay_overstyrte_perioder.tom is 'Til og med dato til perioden for arbeidsforholdet.';

comment on column iay_overstyrte_perioder.arbeidsforhold_id is 'FK til IAY_ARBEIDSFORHOLD sin PK.';

create index idx_iay_overstyrte_perioder_1
    on iay_overstyrte_perioder (arbeidsforhold_id);

create table iay_fravaer
(
    id                 bigint                              not null
        constraint pk_fravaer
            primary key,
    inntektsmelding_id bigint                              not null
        constraint fk_fravaer_1
            references iay_inntektsmelding,
    fom                date                                not null,
    tom                date                                not null,
    varighet_per_dag   varchar(20),
    versjon            bigint       default 0              not null,
    opprettet_av       varchar(20)  default 'VL'::character varying not null,
    opprettet_tid      timestamp(3) default LOCALTIMESTAMP not null,
    endret_av          varchar(20),
    endret_tid         timestamp(3),
    constraint iay_fravaer_valider_periode
        check (fom <= tom)
);

comment on table iay_fravaer is 'Arbeidsgivers informasjon om fravær fra arbeid angitt i inntektsmelding';

create index idx_iay_fravaer_01
    on iay_fravaer (inntektsmelding_id);

create table lonnskomp_vedtak
(
    id                  bigint                              not null
        constraint pk_lonnskomp_vedtak
            primary key,
    sakid               varchar(100)                        not null,
    forrige_vedtak_dato date,
    aktoer_id           varchar(50),
    org_nummer          varchar(100)                        not null,
    beloep              numeric(19, 2)                      not null,
    fom                 date                                not null,
    tom                 date                                not null,
    versjon             bigint       default 0              not null,
    opprettet_av        varchar(20)  default 'VL'::character varying not null,
    opprettet_tid       timestamp(3) default LOCALTIMESTAMP not null,
    endret_av           varchar(20),
    endret_tid          timestamp(3),
    aktiv               boolean      default true           not null,
    fnr                 varchar(50)
);

comment on table lonnskomp_vedtak is 'En tabell med informasjon om Lønnskompensasjon / Koronapenger';

comment on column lonnskomp_vedtak.id is 'Primærnøkkel';

comment on column lonnskomp_vedtak.sakid is 'Saksnummer i kildesystem';

comment on column lonnskomp_vedtak.forrige_vedtak_dato is 'Revurderinger har dato for forrige vedtak';

comment on column lonnskomp_vedtak.aktoer_id is 'Stønadsmottakeren';

comment on column lonnskomp_vedtak.org_nummer is 'Arbeidsgiver som har permittert';

comment on column lonnskomp_vedtak.beloep is 'Sum utbetalt Stønadsmottakeren';

comment on column lonnskomp_vedtak.fom is 'Startdato for ytelsen.';

comment on column lonnskomp_vedtak.tom is 'Sluttdato for ytelsen';

comment on column lonnskomp_vedtak.aktiv is 'Er innslaget aktivt';

create index idx_lonnskomp_vedtak_1
    on lonnskomp_vedtak (sakid);

create index idx_lonnskomp_vedtak_2
    on lonnskomp_vedtak (aktoer_id);

create index idx_lonnskomp_vedtak_10
    on lonnskomp_vedtak (aktiv);

create index idx_lonnskomp_vedtak_3
    on lonnskomp_vedtak (fnr);

create table lonnskomp_anvist
(
    id            bigint                              not null
        constraint pk_lonnskomp_anvist
            primary key,
    vedtak_id     bigint                              not null
        constraint fk_lonnskomp_anvist_1
            references lonnskomp_vedtak,
    beloep        numeric(19, 2),
    fom           date                                not null,
    tom           date                                not null,
    versjon       bigint       default 0              not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default LOCALTIMESTAMP not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table lonnskomp_anvist is 'En tabell med informasjon om beløp pr måned';

comment on column lonnskomp_anvist.id is 'PK';

comment on column lonnskomp_anvist.vedtak_id is 'FK:YTELSE Fremmednøkkel til vedtakstabellen';

comment on column lonnskomp_anvist.beloep is 'Beløp ifm utbetaling.';

comment on column lonnskomp_anvist.fom is 'Startdato for anvisning.';

comment on column lonnskomp_anvist.tom is 'Sluttdato for anvisning';

create index idx_lonnskomp_anvist_1
    on lonnskomp_anvist (vedtak_id);

create table diagnostikk_logg
(
    id            bigint                                 not null
        primary key,
    saksnummer    varchar(20)                            not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default CURRENT_TIMESTAMP not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

create index diagnostikk_logg_saksnummer_idx
    on diagnostikk_logg (saksnummer);

create table iay_oppgitte_opptjeninger
(
    id            bigint                              not null
        constraint pk_iay_oppgitte_opptjeninger
            primary key,
    versjon       bigint       default 0              not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default LOCALTIMESTAMP not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table iay_oppgitte_opptjeninger is 'Koblingstabell mellom grunnlag og oppgitte opptjeninger';

comment on column iay_oppgitte_opptjeninger.id is 'Primærnøkkel';

create table ve_ytelse_andel
(
    id                      bigint                              not null
        constraint pk_ve_ytelse_andel
            primary key,
    ytelse_anvist_id        bigint                              not null
        constraint fk_ve_ytelse_andel_1
            references ve_ytelse_anvist,
    dagsats                 numeric(19, 2),
    utbetalingsgrad_prosent numeric(5, 2),
    refusjonsgrad_prosent   numeric(5, 2),
    arbeidsgiver_aktor_id   varchar(100),
    arbeidsgiver_orgnr      varchar(100),
    arbeidsforhold_id       varchar(100),
    inntektskategori        varchar(100),
    versjon                 bigint       default 0              not null,
    opprettet_av            varchar(20)  default 'VL'::character varying not null,
    opprettet_tid           timestamp(3) default LOCALTIMESTAMP not null,
    endret_av               varchar(20),
    endret_tid              timestamp(3)
);

comment on table ve_ytelse_andel is 'En tabell med informasjon om fordeling for en ytelseperiode';

comment on column ve_ytelse_andel.id is 'PK';

comment on column ve_ytelse_andel.ytelse_anvist_id is 'FK:YTELSE_ANVIST Fremmednøkkel til ytelseperioden';

comment on column ve_ytelse_andel.dagsats is 'Dagsats ifm utbetaling.';

comment on column ve_ytelse_andel.utbetalingsgrad_prosent is 'Utbetalingsgrad for dagsats.';

comment on column ve_ytelse_andel.refusjonsgrad_prosent is 'Andel av dagsats som betales til arbeidsgiver i refusjon';

comment on column ve_ytelse_andel.arbeidsgiver_aktor_id is 'Arbeidsgiver aktør-id.';

comment on column ve_ytelse_andel.arbeidsgiver_orgnr is 'Arbeidsgiver orgnr';

comment on column ve_ytelse_andel.arbeidsforhold_id is 'Arbeidsforhold ekstern id';

comment on column ve_ytelse_andel.inntektskategori is 'Inntektskategori for utbetalingen';

create index idx_ve_ytelse_andel_1
    on ve_ytelse_andel (ytelse_anvist_id);

create table iay_ytelse_anvist_andel
(
    id                       bigint                              not null
        constraint pk_iay_ytelse_anvist_andel
            primary key,
    ytelse_anvist_id         bigint                              not null
        constraint fk_iay_ytelse_anvist_andel_1
            references iay_ytelse_anvist,
    dagsats                  numeric(19, 2),
    utbetalingsgrad_prosent  numeric(5, 2),
    refusjonsgrad_prosent    numeric(5, 2),
    arbeidsgiver_aktor_id    varchar(100),
    arbeidsgiver_orgnr       varchar(100),
    arbeidsforhold_intern_id varchar(100),
    inntektskategori         varchar(100),
    versjon                  bigint       default 0              not null,
    opprettet_av             varchar(20)  default 'VL'::character varying not null,
    opprettet_tid            timestamp(3) default LOCALTIMESTAMP not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3)
);

comment on table iay_ytelse_anvist_andel is 'En tabell med informasjon om fordeling for en ytelseperiode';

comment on column iay_ytelse_anvist_andel.id is 'PK';

comment on column iay_ytelse_anvist_andel.ytelse_anvist_id is 'FK:YTELSE_ANVIST Fremmednøkkel til ytelseperioden';

comment on column iay_ytelse_anvist_andel.dagsats is 'Dagsats ifm utbetaling.';

comment on column iay_ytelse_anvist_andel.utbetalingsgrad_prosent is 'Utbetalingsgrad for dagsats.';

comment on column iay_ytelse_anvist_andel.refusjonsgrad_prosent is 'Andel av dagsats som betales til arbeidsgiver i refusjon';

comment on column iay_ytelse_anvist_andel.arbeidsgiver_aktor_id is 'Arbeidsgiver aktør-id.';

comment on column iay_ytelse_anvist_andel.arbeidsgiver_orgnr is 'Arbeidsgiver orgnr';

comment on column iay_ytelse_anvist_andel.arbeidsforhold_intern_id is 'Arbeidsforhold intern id';

comment on column iay_ytelse_anvist_andel.inntektskategori is 'Inntektskategori for utbetalingen';

create index idx_iay_ytelse_anvist_andel_1
    on iay_ytelse_anvist_andel (ytelse_anvist_id);

create table prosess_task
(
    id                        numeric                                                                             not null,
    task_type                 varchar(50)                                                                         not null,
    prioritet                 numeric(3)   default 0                                                              not null,
    status                    varchar(20)  default 'KLAR'::character varying                                     not null,
    task_parametere           varchar(4000),
    task_payload              text,
    task_gruppe               varchar(250),
    task_sekvens              varchar(100) default '1'::character varying                                        not null,
    partition_key             varchar(4)   default to_char((CURRENT_DATE) ::timestamp with time zone, 'MM'::text) not null,
    neste_kjoering_etter      timestamp(0) default CURRENT_TIMESTAMP,
    feilede_forsoek           numeric(5)   default 0,
    siste_kjoering_ts         timestamp(6),
    siste_kjoering_feil_kode  varchar(50),
    siste_kjoering_feil_tekst text,
    siste_kjoering_server     varchar(50),
    opprettet_av              varchar(20)  default 'VL'::character varying                                       not null,
    opprettet_tid             timestamp(6) default CURRENT_TIMESTAMP                                              not null,
    blokkert_av               numeric,
    versjon                   numeric      default 0                                                              not null,
    siste_kjoering_slutt_ts   timestamp(6),
    siste_kjoering_plukk_ts   timestamp(6),
    constraint pk_prosess_task
        primary key (id, status, partition_key)
)
    partition by LIST
(
    status
);

comment on table prosess_task is 'Inneholder tasks som skal kjøres i bakgrunnen';

comment on column prosess_task.id is 'Primary Key';

comment on column prosess_task.task_type is 'navn på task. Brukes til å matche riktig implementasjon';

comment on column prosess_task.prioritet is 'prioritet på task.  Høyere tall har høyere prioritet';

comment on column prosess_task.status is 'status på task: KLAR, NYTT_FORSOEK, FEILET, VENTER_SVAR, FERDIG';

comment on column prosess_task.task_parametere is 'parametere angitt for en task';

comment on column prosess_task.task_payload is 'inputdata for en task';

comment on column prosess_task.task_gruppe is 'angir en unik id som grupperer flere ';

comment on column prosess_task.task_sekvens is 'angir rekkefølge på task innenfor en gruppe ';

comment on column prosess_task.neste_kjoering_etter is 'tasken skal ikke kjøeres før tidspunkt er passert';

comment on column prosess_task.feilede_forsoek is 'antall feilede forsøk';

comment on column prosess_task.siste_kjoering_ts is 'siste gang tasken ble forsøkt kjørt (før kjøring)';

comment on column prosess_task.siste_kjoering_feil_kode is 'siste feilkode tasken fikk';

comment on column prosess_task.siste_kjoering_feil_tekst is 'siste feil tasken fikk';

comment on column prosess_task.siste_kjoering_server is 'navn på node som sist kjørte en task (server@pid)';

comment on column prosess_task.blokkert_av is 'Id til ProsessTask som blokkerer kjøring av denne (når status=VETO)';

comment on column prosess_task.versjon is 'angir versjon for optimistisk låsing';

comment on column prosess_task.siste_kjoering_slutt_ts is 'siste gang tasken ble forsøkt plukket (klargjort til kjøring)';

CREATE INDEX IDX_PROSESS_TASK_2
    ON PROSESS_TASK (TASK_TYPE);
CREATE INDEX IDX_PROSESS_TASK_3
    ON PROSESS_TASK (NESTE_KJOERING_ETTER);
CREATE INDEX IDX_PROSESS_TASK_5
    ON PROSESS_TASK (TASK_GRUPPE);
CREATE INDEX IDX_PROSESS_TASK_1
    ON PROSESS_TASK (STATUS);
CREATE INDEX IDX_PROSESS_TASK_4
    ON PROSESS_TASK (ID);
CREATE INDEX IDX_PROSESS_TASK_7
    ON PROSESS_TASK (PARTITION_KEY);
CREATE UNIQUE INDEX UIDX_PROSESS_TASK
    ON PROSESS_TASK (ID, STATUS, PARTITION_KEY);

CREATE INDEX IDX_PROSESS_TASK_6 ON PROSESS_TASK (BLOKKERT_AV);

-- Etablerer et sett med bøtter som ferdig tasks kan legge seg i avhengig av hvilken måned de er opprettet i.
-- Legger opp til at disse bøttene kan prunes etter kontinuerlig for å bevare ytelsen
CREATE TABLE PROSESS_TASK_PARTITION_DEFAULT PARTITION OF PROSESS_TASK
    DEFAULT;

CREATE TABLE PROSESS_TASK_PARTITION_FERDIG PARTITION OF PROSESS_TASK
    FOR VALUES IN ('FERDIG') PARTITION BY LIST (PARTITION_KEY);
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_01 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('01');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_02 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('02');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_03 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('03');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_04 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('04');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_05 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('05');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_06 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('06');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_07 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('07');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_08 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('08');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_09 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('09');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_10 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('10');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_11 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('11');
CREATE TABLE PROSESS_TASK_PARTITION_FERDIG_12 PARTITION OF PROSESS_TASK_PARTITION_FERDIG
    FOR VALUES IN ('12');
