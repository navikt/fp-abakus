
alter table IAY_AKTIVITETS_AVTALE add column
    ANTALL_TIMER             NUMERIC(6, 2);
    
alter table IAY_AKTIVITETS_AVTALE add column
    ANTALL_TIMER_FULLTID     NUMERIC(6, 2);
    
comment on column IAY_AKTIVITETS_AVTALE.ANTALL_TIMER is 'Antall timer med avtalt arbeid';
comment on column IAY_AKTIVITETS_AVTALE.ANTALL_TIMER_FULLTID is 'Antall timer som tilsvarer full stilling';