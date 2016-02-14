CREATE TABLE user (
    uniqueID            INTEGER UNIQUE NOT NULL PRIMARY KEY AUTOINCREMENT,
    email               TEXT NOT NULL UNIQUE,
    salt                char(64) NOT NULL,
    hash                CHAR(64) NOT NULL
);

CREATE TABLE note (
    uniqueID            char(36) NOT NULL,
    userID              INTEGER NOT NULL,
    text                TEXT NOT NULL,
    createDate          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sortDate            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT note_pk PRIMARY KEY (uniqueID, userID),
    CONSTRAINT note_user_fk FOREIGN KEY (userID) REFERENCES user(uniqueID)
);

CREATE TABLE deletedNote (
    uniqueID            INTEGER UNIQUE NOT NULL PRIMARY KEY
);