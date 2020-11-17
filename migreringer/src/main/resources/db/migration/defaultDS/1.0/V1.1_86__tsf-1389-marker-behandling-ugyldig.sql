-- benyttes til å markere kobling opprettet feilaktig som ugyldig (må så filtreres ut)
alter table kobling add column aktiv boolean default true not null;