CREATE TABLE tweet_user(id BIGINT PRIMARY KEY, name VARCHAR(256), screen_name VARCHAR(256));
CREATE TABLE tweet_place(id VARCHAR(256) PRIMARY KEY, country VARCHAR(256), country_code VARCHAR(3), type VARCHAR(64),name VARCHAR(256), full_name VARCHAR(256));
CREATE TABLE tweet(id BIGINT PRIMARY KEY, tweet_date TIMESTAMP WITH TIME ZONE, text VARCHAR(512), place_id VARCHAR(256) REFERENCES tweet_place(id), user_id BIGINT REFERENCES tweet_user(id));

CREATE INDEX tweet_idx_tweet_date ON tweet(tweet_date);
CLUSTER tweet USING tweet_idx_tweet_date;