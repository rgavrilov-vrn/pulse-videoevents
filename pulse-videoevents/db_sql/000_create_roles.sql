-- 000_create_roles.sql
-- Выполняется суперпользователем PostgreSQL.
-- Пароль замените на секрет из защищенного хранилища.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_roles WHERE rolname = 'pulse_videoevents_app'
    ) THEN
CREATE ROLE pulse_videoevents_app LOGIN PASSWORD 'password@pulse.ru';
END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_roles WHERE rolname = 'pulse_videoevents_readonly'
    ) THEN
CREATE ROLE pulse_videoevents_readonly LOGIN PASSWORD 'password@pulse.ru';
END IF;
END;
$$;

ALTER ROLE pulse_videoevents_app SET TimeZone TO 'UTC';
ALTER ROLE pulse_videoevents_readonly SET TimeZone TO 'UTC';
