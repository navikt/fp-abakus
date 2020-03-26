CREATE TABLE iay_fravaer
(
    id bigint NOT NULL,
    inntektsmelding_id bigint NOT NULL,
    fom date NOT NULL,
    tom date NOT NULL,
    varighet_per_dag varchar(20),
    versjon bigint NOT NULL DEFAULT 0,
    opprettet_av varchar(20) NOT NULL DEFAULT 'VL',
    opprettet_tid timestamp(3) without time zone NOT NULL DEFAULT LOCALTIMESTAMP,
    endret_av varchar(20),
    endret_tid timestamp(3) without time zone,
    CONSTRAINT pk_fravaer PRIMARY KEY (id),
    CONSTRAINT fk_fravaer_1 FOREIGN KEY (inntektsmelding_id)
        REFERENCES iay_inntektsmelding (id)
);

COMMENT ON TABLE iay_fravaer
    IS 'Arbeidsgivers informasjon om fravær fra arbeid angitt i inntektsmelding';
    
create index idx_iay_fravaer_01 on iay_fravaer(inntektsmelding_id);

alter table iay_fravaer add constraint iay_fravaer_valider_periode check (fom <= tom);

create sequence SEQ_IAY_FRAVAER
    increment by 50
    START WITH 1000000 NO CYCLE;

