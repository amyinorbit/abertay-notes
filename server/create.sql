CREATE TABLE IF NOT EXISTS user (
    uniqueID            INTEGER UNIQUE NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email               VARCHAR(255) NOT NULL UNIQUE,
    salt                CHAR(64) NOT NULL,
    hash                CHAR(64) NOT NULL,
    deleteSeqID         BIGINT NOT NULL DEFAULT 0,
    updateSeqID         BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS token (
    userID              INTEGER NOT NULL,
    deviceID            CHAR(32) NOT NULL,
    token               CHAR(128) NOT NULL,
    pushToken           TEXT,
    CONSTRAINT token_pk PRIMARY KEY(userID, deviceID),
    CONSTRAINT token_user_fk FOREIGN KEY (userID) REFERENCES user(uniqueID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS note (
    userID              INTEGER NOT NULL,
    uniqueID            char(36) NOT NULL,
    text                TEXT NOT NULL,
    createDate          DATETIME NOT NULL,
    sortDate            DATETIME NOT NULL,
    seqID               BIGINT NOT NULL,
    CONSTRAINT note_pk PRIMARY KEY (uniqueID),
    CONSTRAINT note_user_fk FOREIGN KEY (userID) REFERENCES user(uniqueID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS deletedNote (
    userID              INTEGER NOT NULL,
    uniqueID            char(36) NOT NULL,
    seqID               BIGINT NOT NULL,
    CONSTRAINT dnote_pk PRIMARY KEY (uniqueID),
    CONSTRAINT dnote_user_fk FOREIGN KEY (userID) REFERENCES user(uniqueID) ON DELETE CASCADE
);