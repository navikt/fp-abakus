alter table prosess_task_feilhand alter column opprettet_tid set  default (now() at time zone 'utc');
alter table iay_overstyrte_perioder alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_type alter column opprettet_tid set  default (now() at time zone 'utc');
alter table kodeliste alter column gyldig_fom set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_11 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_01 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_10 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_02 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_09 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_03 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_08 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_04 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_07 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_06 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_05 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_default alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_12 alter column neste_kjoering_etter set  default (now() at time zone 'utc');
alter table kodeverk alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_05 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_06 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_04 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_07 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_03 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_08 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_02 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_09 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_01 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_10 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_11 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_default alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task_partition_ferdig_12 alter column opprettet_tid set  default (now() at time zone 'utc');
alter table prosess_task alter column opprettet_tid set  default (now() at time zone 'utc');