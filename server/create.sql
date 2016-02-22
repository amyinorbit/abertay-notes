CREATE TABLE IF NOT EXISTS user (
    uniqueID            INTEGER UNIQUE NOT NULL PRIMARY KEY AUTOINCREMENT,
    email               TEXT NOT NULL UNIQUE,
    salt                CHAR(64) NOT NULL,
    hash                CHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS note (
    userID              INTEGER NOT NULL,
    uniqueID            char(36) NOT NULL,
    text                TEXT NOT NULL,
    createDate          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sortDate            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT note_pk PRIMARY KEY (uniqueID, userID),
    CONSTRAINT note_user_fk FOREIGN KEY (userID) REFERENCES user(uniqueID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS deletedNote (
    userID              INTEGER NOT NULL,
    uniqueID            char(36) NOT NULL,
    deleteDate          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT dnote_user_fk FOREIGN KEY (userID) REFERENCES user(uniqueID) ON DELETE CASCADE
);