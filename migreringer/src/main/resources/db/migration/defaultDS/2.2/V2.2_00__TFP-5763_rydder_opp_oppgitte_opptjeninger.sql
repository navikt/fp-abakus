alter table gr_arbeid_inntekt
    drop column oppgitte_opptjeninger_id;
alter table iay_oppgitt_opptjening
    drop column oppgitte_opptjeninger_id;
drop table iay_oppgitte_opptjeninger;
drop sequence seq_iay_oppgitte_opptjeninger;
