CREATE TABLE categories (
    ID bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    UUID binary(16) NOT NULL,
    WORKSPACE_UUID binary(16) NOT NULL,
    NAME varchar(50) NOT NULL,
    DESCRIPTION varchar(254),
    CREATED_BY_USER_UUID binary(16) NOT NULL,
    CREATED_DATE timestamp(0) DEFAULT NOW(0),
    COLOR varchar(50),
    PRIMARY KEY (ID),
    UNIQUE KEY UK_UUID (UUID),
    UNIQUE KEY UK_NAME (WORKSPACE_UUID, NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE tasks (
    ID bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    UUID binary(16) NOT NULL,
    CATEGORY_UUID binary(16) NOT NULL,
    WORKSPACE_UUID binary(16) NOT NULL,
    NAME varchar(50) NOT NULL,
    DESCRIPTION varchar(254),
    STATUS varchar(10) NOT NULL,
    CREATED_DATE timestamp(0) DEFAULT NOW(0),
    CREATED_BY_USER_UUID binary(16) NOT NULL,
    ASSIGNED_TO_USER_UUID binary(16),
    PRIORITY int(10),
    REMINDER_DATE timestamp(0) NULL DEFAULT NULL,
    PRIMARY KEY (ID),
    UNIQUE KEY UK_UUID (UUID),
    FOREIGN KEY (CATEGORY_UUID) REFERENCES categories(UUID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE work_log (
    ID bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    UUID binary(16) NOT NULL,
    WORKSPACE_UUID binary(16) NOT NULL,
    TASK_UUID binary(16) NOT NULL,
    USER_UUID binary(16) NOT NULL,
    STARTED_DATE timestamp(0) NULL DEFAULT NULL,
    ENDED_DATE timestamp(0) NULL DEFAULT NULL,
    COMMENT TEXT,
    PRIMARY KEY (ID),
    UNIQUE KEY UK_UUID (UUID),
    FOREIGN KEY (TASK_UUID) REFERENCES tasks(UUID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
