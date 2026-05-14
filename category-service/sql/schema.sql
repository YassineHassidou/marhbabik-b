CREATE TABLE IF NOT EXISTS categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    icon TEXT,
    sort_order INTEGER NOT NULL
);

INSERT OR IGNORE INTO categories(id, name, icon, sort_order) VALUES
('transport', 'Transport', 'directions_car', 1),
('food', 'Food', 'restaurant', 2),
('housing', 'Housing', 'home', 3),
('guide', 'Guide', 'map', 4),
('events', 'Events', 'event', 5),
('shopping', 'Shopping', 'shopping_bag', 6),
('wellness', 'Wellness', 'spa', 7),
('other', 'Other', 'category', 8);
