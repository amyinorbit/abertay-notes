CREATE TABLE IF NOT EXISTS user (
    uniqueID            INTEGER UNIQUE NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email               VARCHAR(255) NOT NULL UNIQUE,
    salt                CHAR(64) NOT NULL,
    hash                CHAR(64) NOT NULL,
    token               CHAR(128)
);

CREATE TABLE IF NOT EXISTS note (
    userID              INTEGER NOT NULL,
    uniqueID            char(36) NOT NULL,
    text                TEXT NOT NULL,
    createDate          DATETIME NOT NULL,
    sortDate            DATETIME NOT NULL,
    CONSTRAINT note_pk PRIMARY KEY (uniqueID),
    CONSTRAINT note_user_fk FOREIGN KEY (userID) REFERENCES user(uniqueID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS deletedNote (
    userID              INTEGER NOT NULL,
    uniqueID            char(36) NOT NULL,
    deleteDate          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT dnote_pk PRIMARY KEY (uniqueID),
    CONSTRAINT dnote_user_fk FOREIGN KEY (userID) REFERENCES user(uniqueID) ON DELETE CASCADE
);