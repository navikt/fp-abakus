alter table vedtak_ytelse add tilleggsopplysninger TEXT ;
comment on column vedtak_ytelse.tilleggsopplysninger is 'Beskrivelse av ytelse eller vedtak';
