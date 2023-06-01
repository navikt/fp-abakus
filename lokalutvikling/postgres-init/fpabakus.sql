CREATE DATABASE fpabakus_unit;
CREATE USER fpabakus_unit WITH PASSWORD 'fpabakus_unit';
GRANT ALL ON DATABASE fpabakus_unit TO fpabakus_unit;
ALTER DATABASE fpabakus_unit SET timezone TO 'Europe/Oslo';
ALTER DATABASE fpabakus_unit OWNER TO fpabakus_unit;

CREATE DATABASE fpabakus;
CREATE USER fpabakus WITH PASSWORD 'fpabakus';
GRANT ALL ON DATABASE fpabakus TO fpabakus;
ALTER DATABASE fpabakus SET timezone TO 'Europe/Oslo';
ALTER DATABASE fpabakus OWNER TO fpabakus;
