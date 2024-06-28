CREATE TABLE profile (
 id TEXT PRIMARY KEY,
 buckets TEXT,
 repositories TEXT,
 status BOOLEAN DEFAULT TRUE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user (
  id TEXT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  password TEXT NOT NULL,
  id_profile TEXT,
  admin BOOLEAN NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_profile) REFERENCES profile(id)
);