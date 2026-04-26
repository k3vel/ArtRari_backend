
-- ENUMS
CREATE TYPE user_role AS ENUM('user', 'curator', 'admin');

CREATE TYPE exhibition_status AS ENUM('waiting', 'running', 'converted_in_auction');

CREATE TYPE auction_status AS ENUM('scheduled', 'active', 'finished');

CREATE TYPE work_status AS ENUM('available', 'in_auction', 'sold');

CREATE TYPE lot_status AS ENUM('available', 'sold', 'cancelled');

CREATE TYPE purchase_status AS ENUM(
  'pending_payment',
  'pending_shipment',
  'completed',
  'cancelled'
);

-- Tables
CREATE TABLE users (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  full_name TEXT NOT NULL,
  phone TEXT UNIQUE NOT NULL,
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  role user_role NOT NULL DEFAULT 'user',
  is_banned BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE exhibition (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  curator_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
  title TEXT NOT NULL,
  theme TEXT,
  description TEXT,
  background_url TEXT,
  start_date TIMESTAMPTZ NOT NULL,
  status exhibition_status NOT NULL DEFAULT 'waiting',
  thumbnail_url TEXT
);

CREATE TABLE auction (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  exhibition_id BIGINT NOT NULL REFERENCES exhibition (id) ON DELETE CASCADE,
  start_date TIMESTAMPTZ NOT NULL,
  auction_step NUMERIC(10, 2) NOT NULL,
  status auction_status NOT NULL DEFAULT 'scheduled',
  end_date TIMESTAMPTZ,
  CONSTRAINT chk_auction_step CHECK (auction_step > 0)
);

CREATE TABLE work (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  owner_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  exhibition_id BIGINT REFERENCES exhibition (id) ON DELETE SET NULL,
  title TEXT NOT NULL,
  author TEXT NOT NULL,
  description TEXT NOT NULL,
  technique TEXT NOT NULL,
  creation_date DATE NOT NULL,
  photo_url TEXT NOT NULL,
  start_price NUMERIC(12, 2) NOT NULL,
  status work_status NOT NULL DEFAULT 'available',
  CONSTRAINT chk_work_start_price CHECK (start_price >= 0)
);

CREATE TABLE lot (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  work_id BIGINT NOT NULL UNIQUE REFERENCES work (id) ON DELETE CASCADE,
  auction_id BIGINT NOT NULL REFERENCES auction (id) ON DELETE CASCADE,
  current_price NUMERIC(12, 2) NOT NULL,
  end_date TIMESTAMPTZ NOT NULL,
  status lot_status NOT NULL DEFAULT 'available',
  CONSTRAINT chk_lot_current_price CHECK (current_price >= 0)
);

CREATE TABLE bid (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  lot_id BIGINT NOT NULL REFERENCES lot (id) ON DELETE CASCADE,
  amount NUMERIC(12, 2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_win BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT chk_bid_amount CHECK (amount > 0)
);

CREATE TABLE purchase_history (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  lot_id BIGINT NOT NULL REFERENCES lot (id) ON DELETE SET NULL,
  final_price NUMERIC(12, 2) NOT NULL,
  win_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status purchase_status NOT NULL DEFAULT 'pending_payment',
  CONSTRAINT chk_purchase_final_price CHECK (final_price > 0)
);

CREATE TABLE tag (
  id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  name TEXT UNIQUE NOT NULL
);

CREATE TABLE tag_work (
  tag_id BIGINT NOT NULL REFERENCES tag (id) ON DELETE CASCADE,
  work_id BIGINT NOT NULL REFERENCES work (id) ON DELETE CASCADE,
  PRIMARY KEY (tag_id, work_id)
);

