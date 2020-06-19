alter table iay_utsettelse_periode drop constraint fk_utsettelse_periode_3 ;
alter table iay_relatert_ytelse drop constraint fk_ytelse_2;
alter table iay_ytelse_stoerrelse drop constraint fk_ytelse_stoerrelse_1;
alter table vedtak_ytelse drop constraint fk_vedtak_ytelse_2;

drop TABLE kodeliste_navn_i18n  ;
drop TABLE kodeliste  ;
drop table kodeverk  ;

drop sequence seq_kodeliste;
drop sequence seq_kodeliste_navn;
drop sequence seq_kodeliste_relasjon;
