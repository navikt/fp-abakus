CREATE DATABASE fpabakus;
CREATE USER fpabakus WITH PASSWORD 'fpabakus';
GRANT ALL ON DATABASE fpabakus TO fpabakus;
ALTER DATABASE fpabakus SET timezone TO 'Europe/Oslo';
ALTER DATABASE fpabakus OWNER TO fpabakus;
