create table myentity (
        id varchar(255) not null,
        version int4 not null,
        created_at timestamp with time zone  NOT NULL,
        created_by varchar(255) NULL,
        last_modified_at timestamp with time zone NULL,
        last_modified_by varchar(255) NULL,
        is_active boolean not null,
        title varchar(255),
        primary key (id));