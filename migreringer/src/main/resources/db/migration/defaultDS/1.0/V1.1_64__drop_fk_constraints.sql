alter table iay_ytelse_grunnlag drop constraint fk_ytelse_grunnlag_81;
alter table iay_permisjon drop constraint fk_permisjon_2;
alter table iay_inntekt drop constraint fk_inntekt_3;
alter table iay_inntektsmelding drop constraint fk_inntektsmelding_3;
alter table iay_inntektspost drop constraint fk_inntektspost_2;
alter table iay_inntektspost drop constraint fk_inntektspost_3;
alter table iay_natural_ytelse drop constraint fk_natural_ytelse_2;

ALTER TABLE iay_oppgitt_arbeidsforhold ALTER COLUMN kl_landkoder DROP NOT NULL;