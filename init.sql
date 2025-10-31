-- Создаем пользователя если он не существует
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 's381032') THEN
        CREATE ROLE s381032 WITH LOGIN PASSWORD 'UVos.8847';
    END IF;
END
$$;

-- Даем права пользователю
GRANT ALL PRIVILEGES ON DATABASE studs TO s381032;

-- Создаем таблицы для MusicBand системы
CREATE TABLE IF NOT EXISTS coordinates (
    id BIGSERIAL PRIMARY KEY,
    x REAL NOT NULL CHECK (x <= 931),
    y INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS album (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL CHECK (name <> ''),
    tracks INTEGER NOT NULL CHECK (tracks > 0),
    length INTEGER NOT NULL CHECK (length > 0),
    sales DOUBLE PRECISION NOT NULL CHECK (sales > 0)
);

CREATE TABLE IF NOT EXISTS studio (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS music_band (
    id BIGSERIAL PRIMARY KEY CHECK (id > 0),
    name VARCHAR(255) NOT NULL CHECK (name <> ''),
    coordinates_id BIGINT NOT NULL REFERENCES coordinates(id) ON DELETE RESTRICT,
    creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
    genre VARCHAR(50) NOT NULL,
    number_of_participants BIGINT NOT NULL CHECK (number_of_participants > 0),
    single_count BIGINT CHECK (single_count > 0),
    description TEXT,
    best_album_id BIGINT REFERENCES album(id) ON DELETE SET NULL,
    album_count INTEGER NOT NULL CHECK (album_count > 0),
    establishment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    studio_id BIGINT REFERENCES studio(id) ON DELETE SET NULL
);

-- Даем права на все таблицы
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO s381032;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO s381032;