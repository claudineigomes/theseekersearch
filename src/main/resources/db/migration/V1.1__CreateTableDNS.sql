CREATE TABLE DNS(
  dominio         TEXT PRIMARY KEY,
  ip              VARCHAR(30) NOT NULL,
  lasttimeaccess  BIGINT,
  robots          boolean
);