alter table KOBLING
add column if not exists opplysning_periode_skattegrunnlag_fom DATE;
alter table KOBLING
add column if not exists opplysning_periode_skattegrunnlag_tom DATE;

comment on column KOBLING.opplysning_periode_skattegrunnlag_fom is 'Start på perioden det skal hentes skattegrunnlag for';
comment on column KOBLING.opplysning_periode_skattegrunnlag_tom is 'Slutt på perioden det skal hentes skattegrunnlag for';
