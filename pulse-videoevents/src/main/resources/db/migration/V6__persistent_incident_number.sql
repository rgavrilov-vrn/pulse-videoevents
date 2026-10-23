-- Do not rewrite applied migrations. Seed from the largest existing numeric suffix.
LOCK TABLE incidents IN ACCESS EXCLUSIVE MODE;
CREATE SEQUENCE incident_number_seq AS BIGINT;
DO $$
DECLARE highest BIGINT;
BEGIN
    SELECT max(substring(incident_number FROM '^INC-[0-9]{4}-([0-9]+)$')::BIGINT)
      INTO highest FROM incidents;
    PERFORM setval('incident_number_seq', greatest(coalesce(highest, 0) + 1, 1), false);
END $$;
