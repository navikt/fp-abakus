CREATE TABLE IAY_OVERSTYRTE_PERIODER
(
    ID                bigint                                 not null,
    FOM               DATE                                   NOT NULL,
    TOM               DATE                                   NOT NULL,
    ARBEIDSFORHOLD_ID bigint                                 NOT NULL,
    versjon           bigint       DEFAULT 0                 NOT NULL,
    opprettet_av      VARCHAR(20)  default 'VL'              not null,
    opprettet_tid     timestamp(3) default current_timestamp not null,
    endret_av         VARCHAR(20),
    endret_tid        timestamp(3),
    constraint PK_IAY_OVERSTYRTE_PERIODER primary key (ID),
    constraint FK_IAY_OVERSTYRTE_PERIODER FOREIGN KEY (ARBEIDSFORHOLD_ID) REFERENCES IAY_ARBEIDSFORHOLD (id)
);
COMMENT ON TABLE IAY_OVERSTYRTE_PERIODER IS 'En tabell for overstyrte ansettelsesperioder for arbeidsforhold.';
COMMENT ON COLUMN IAY_OVERSTYRTE_PERIODER.FOM IS 'Fra og med dato til perioden for arbeidsforholdet.';
COMMENT ON COLUMN IAY_OVERSTYRTE_PERIODER.TOM IS 'Til og med dato til perioden for arbeidsforholdet.';
COMMENT ON COLUMN IAY_OVERSTYRTE_PERIODER.ARBEIDSFORHOLD_ID IS 'FK til IAY_ARBEIDSFORHOLD sin PK.';

CREATE INDEX IDX_IAY_OVERSTYRTE_PERIODER_1 ON IAY_OVERSTYRTE_PERIODER (ARBEIDSFORHOLD_ID);
