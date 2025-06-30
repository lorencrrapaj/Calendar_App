CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  start_date_time TIMESTAMP NOT NULL,
  end_date_time TIMESTAMP NOT NULL,
  recurrence_rule VARCHAR(255),
  recurrence_end_date TIMESTAMP,
  recurrence_count INT,
  parent_event_id BIGINT,
  original_start_date_time TIMESTAMP,
  excluded_dates TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (parent_event_id) REFERENCES events(id)
);

CREATE TABLE tags (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  UNIQUE (user_id, name),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE event_tags (
  event_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (event_id, tag_id),
  FOREIGN KEY (event_id) REFERENCES events(id),
  FOREIGN KEY (tag_id) REFERENCES tags(id)
);
