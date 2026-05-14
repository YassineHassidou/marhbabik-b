CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    email TEXT,
    role TEXT NOT NULL CHECK(role IN ('visitor', 'worker')),
    age INTEGER,
    cn TEXT,
    phone TEXT,
    category TEXT,
    business_name TEXT,
    profile_photo TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
