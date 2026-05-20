DO
$$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'dancemoments') THEN
    CREATE ROLE dancemoments;
  END IF;
END
$$;

