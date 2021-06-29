-- unik index per grunnlag for ekstern_referanse og arbeidsgiver (blokkerer alle duplikate innslag av intern_referanse fra angitt tidspunkt)
create unique index UIDX_ARBEIDSFORHOLD_REFER_01 on IAY_ARBEIDSFORHOLD_REFER (informasjon_id, ekstern_referanse, arbeidsgiver_orgnr, arbeidsgiver_aktor_id)
where opprettet_tid > '2021-06-30T00:00:00';
