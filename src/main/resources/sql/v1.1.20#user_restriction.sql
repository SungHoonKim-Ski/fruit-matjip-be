ALTER TABLE users ADD COLUMN restricted_until DATE NULL;
ALTER TABLE users CHANGE COLUMN warn_count monthly_warn_count INT DEFAULT 0;
