CREATE DATABASE k9_abakus_unit;
CREATE USER k9_abakus_unit WITH PASSWORD 'k9_abakus_unit';
GRANT ALL ON DATABASE k9_abakus_unit TO k9abakus_unit;
ALTER DATABASE k9_abakus_unit SET timezone TO 'Europe/Oslo';
ALTER DATABASE k9_abakus_unit OWNER TO k9_abakus_unit;

CREATE DATABASE k9_abakus;
CREATE USER k9_abakus WITH PASSWORD 'k9_abakus';
GRANT ALL ON DATABASE k9_abakus TO k9_abakus;
ALTER DATABASE k9_abakus SET timezone TO 'Europe/Oslo';
ALTER DATABASE k9_abakus OWNER TO k9_abakus;
