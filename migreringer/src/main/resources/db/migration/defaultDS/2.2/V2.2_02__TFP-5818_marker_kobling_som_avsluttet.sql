alter table kobling add column avsluttet boolean default false not null;

comment on column kobling.avsluttet is 'Settes true når koblingen blir avsluttet. Grunnlaget blir sperret for endringer.';
