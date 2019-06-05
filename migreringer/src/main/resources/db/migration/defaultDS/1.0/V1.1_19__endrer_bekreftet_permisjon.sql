alter table IAY_ARBEIDSFORHOLD rename column permisjon_bruk  to bekreftet_permisjon_status;
alter table IAY_ARBEIDSFORHOLD alter column bekreftet_permisjon_status varchar(20);

alter table IAY_ARBEIDSFORHOLD rename column permisjon_fom  to bekreftet_permisjon_fom;
alter table IAY_ARBEIDSFORHOLD rename column permisjon_tom  to bekreftet_permisjon_tom;

