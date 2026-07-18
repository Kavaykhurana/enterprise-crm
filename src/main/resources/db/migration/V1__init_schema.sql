-- =============================================================================
-- V1__init_schema.sql
-- Enterprise CRM — Initial Schema
-- Managed by Flyway. DO NOT modify Hibernate ddl-auto in production.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- USERS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                   UUID        NOT NULL,
    email                VARCHAR(150) NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100) NOT NULL,
    phone_number         VARCHAR(20),
    profile_image_url    VARCHAR(512),
    role                 VARCHAR(50)  NOT NULL,          -- ADMIN | SALES_MANAGER | SALES_EXECUTIVE
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    token_version        INTEGER      NOT NULL DEFAULT 1,
    failed_login_attempts INTEGER     NOT NULL DEFAULT 0,
    account_locked       BOOLEAN      NOT NULL DEFAULT FALSE,
    account_locked_until TIMESTAMP,
    last_login_at        TIMESTAMP,
    password_changed_at  TIMESTAMP,
    -- BaseEntity audit columns
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    created_by           VARCHAR(255) NOT NULL,
    updated_by           VARCHAR(255) NOT NULL,
    deleted_at           TIMESTAMP,
    version              INTEGER,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role  ON users (role);

-- ---------------------------------------------------------------------------
-- REFRESH TOKENS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id           UUID         NOT NULL,
    user_id      UUID         NOT NULL,
    token_hash   VARCHAR(64)  NOT NULL,
    expiry_date  TIMESTAMP    NOT NULL,
    is_revoked   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_refresh_tokens  PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token   UNIQUE (token_hash)
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry     ON refresh_tokens (expiry_date);

-- ---------------------------------------------------------------------------
-- SESSION LOGS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS session_logs (
    id            UUID         NOT NULL,
    user_id       UUID         NOT NULL,
    login_time    TIMESTAMP    NOT NULL,
    last_activity TIMESTAMP    NOT NULL,
    device_info   VARCHAR(255),
    ip_address    VARCHAR(45),
    user_agent    VARCHAR(512),
    CONSTRAINT pk_session_logs PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_session_logs_user_id ON session_logs (user_id);

-- ---------------------------------------------------------------------------
-- TAGS  (shared by customers and leads)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tags (
    id          UUID         NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    color       VARCHAR(7)   NOT NULL,   -- hex e.g. #FF5733
    description TEXT,
    -- BaseEntity audit columns
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    version     INTEGER,
    CONSTRAINT pk_tags      PRIMARY KEY (id),
    CONSTRAINT uq_tags_name UNIQUE (name)
);

-- ---------------------------------------------------------------------------
-- CUSTOMERS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id                    UUID         NOT NULL,
    company_name          VARCHAR(200) NOT NULL,
    company_size          VARCHAR(50)  NOT NULL,   -- SMALL | MEDIUM | ENTERPRISE
    customer_status       VARCHAR(30)  NOT NULL,   -- PROSPECT | ACTIVE | INACTIVE
    assigned_sales_rep_id UUID,
    tax_identifier        VARCHAR(50),
    description           TEXT,
    -- BaseEntity audit columns
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    version     INTEGER,
    CONSTRAINT pk_customers PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_customers_sales_rep    ON customers (assigned_sales_rep_id);
CREATE INDEX IF NOT EXISTS idx_customers_status       ON customers (customer_status);
CREATE INDEX IF NOT EXISTS idx_customers_deleted_at   ON customers (deleted_at);

-- ---------------------------------------------------------------------------
-- CUSTOMER CONTACTS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_contacts (
    id                  UUID         NOT NULL,
    customer_id         UUID         NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    mobile_phone        VARCHAR(20),
    work_phone          VARCHAR(20),
    job_title           VARCHAR(100),
    department          VARCHAR(100),
    linkedin_url        VARCHAR(512),
    is_primary_contact  BOOLEAN      NOT NULL DEFAULT FALSE,
    -- BaseEntity audit columns
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    version     INTEGER,
    CONSTRAINT pk_customer_contacts PRIMARY KEY (id),
    CONSTRAINT fk_contacts_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_contacts_customer_id  ON customer_contacts (customer_id);
CREATE INDEX IF NOT EXISTS idx_contacts_deleted_at   ON customer_contacts (deleted_at);

-- ---------------------------------------------------------------------------
-- CUSTOMER ADDRESSES
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_addresses (
    id           UUID         NOT NULL,
    customer_id  UUID         NOT NULL,
    address_type VARCHAR(50)  NOT NULL,   -- BILLING | SHIPPING | HEAD_OFFICE | BRANCH
    street       VARCHAR(255) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    state        VARCHAR(100),
    postal_code  VARCHAR(20),
    country      VARCHAR(100) NOT NULL,
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    -- BaseEntity audit columns
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    version     INTEGER,
    CONSTRAINT pk_customer_addresses PRIMARY KEY (id),
    CONSTRAINT fk_addresses_customer FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_addresses_customer_id ON customer_addresses (customer_id);
CREATE INDEX IF NOT EXISTS idx_addresses_deleted_at  ON customer_addresses (deleted_at);

-- ---------------------------------------------------------------------------
-- CUSTOMER TAGS  (join table)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_tags (
    customer_id UUID NOT NULL,
    tag_id      UUID NOT NULL,
    CONSTRAINT pk_customer_tags PRIMARY KEY (customer_id, tag_id),
    CONSTRAINT fk_ct_customer   FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE,
    CONSTRAINT fk_ct_tag        FOREIGN KEY (tag_id)      REFERENCES tags (id)      ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- LEADS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS leads (
    id                    UUID         NOT NULL,
    company_name          VARCHAR(200),
    first_name            VARCHAR(100) NOT NULL,
    last_name             VARCHAR(100) NOT NULL,
    email                 VARCHAR(150) NOT NULL,
    phone                 VARCHAR(20),
    source                VARCHAR(50)  NOT NULL,  -- WEB | REFERRAL | COLD_CALL | PARTNER | CONFERENCE | OTHER
    priority              VARCHAR(20)  NOT NULL,  -- LOW | MEDIUM | HIGH
    status                VARCHAR(30)  NOT NULL,  -- NEW | CONTACTED | QUALIFIED | PROPOSAL | NEGOTIATION | WON | LOST
    assigned_sales_rep_id UUID,
    converted_customer_id UUID,
    expected_budget       NUMERIC(19, 4),
    expected_close_date   DATE,
    last_contacted_at     TIMESTAMP,
    next_follow_up_date   TIMESTAMP,
    -- BaseEntity audit columns
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    version     INTEGER,
    CONSTRAINT pk_leads PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_leads_sales_rep       ON leads (assigned_sales_rep_id);
CREATE INDEX IF NOT EXISTS idx_leads_status          ON leads (status);
CREATE INDEX IF NOT EXISTS idx_leads_deleted_at      ON leads (deleted_at);
CREATE INDEX IF NOT EXISTS idx_leads_converted_cust  ON leads (converted_customer_id);

-- ---------------------------------------------------------------------------
-- LEAD TAGS  (join table)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lead_tags (
    lead_id UUID NOT NULL,
    tag_id  UUID NOT NULL,
    CONSTRAINT pk_lead_tags PRIMARY KEY (lead_id, tag_id),
    CONSTRAINT fk_lt_lead FOREIGN KEY (lead_id) REFERENCES leads (id) ON DELETE CASCADE,
    CONSTRAINT fk_lt_tag  FOREIGN KEY (tag_id)  REFERENCES tags (id)  ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- OPPORTUNITIES
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS opportunities (
    id                    UUID         NOT NULL,
    customer_id           UUID         NOT NULL,
    assigned_sales_rep_id UUID,
    name                  VARCHAR(200) NOT NULL,
    expected_revenue      NUMERIC(19, 4) NOT NULL,
    probability           INTEGER      NOT NULL,  -- 0..100
    stage                 VARCHAR(30)  NOT NULL,  -- QUALIFICATION | PROPOSAL | NEGOTIATION | WON | LOST
    reason_lost           TEXT,
    expected_close_date   DATE         NOT NULL,
    actual_close_date     DATE,
    actual_revenue        NUMERIC(19, 4),
    currency              VARCHAR(3)   NOT NULL DEFAULT 'USD',
    -- BaseEntity audit columns
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    version     INTEGER,
    CONSTRAINT pk_opportunities   PRIMARY KEY (id),
    CONSTRAINT fk_opp_customer    FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE INDEX IF NOT EXISTS idx_opp_customer_id  ON opportunities (customer_id);
CREATE INDEX IF NOT EXISTS idx_opp_sales_rep    ON opportunities (assigned_sales_rep_id);
CREATE INDEX IF NOT EXISTS idx_opp_stage        ON opportunities (stage);

-- ---------------------------------------------------------------------------
-- TASKS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tasks (
    id                  UUID         NOT NULL,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    due_date            TIMESTAMP    NOT NULL,
    status              VARCHAR(30)  NOT NULL DEFAULT 'TODO',   -- TODO | IN_PROGRESS | COMPLETED
    assigned_user_id    UUID,
    related_entity_type VARCHAR(50),                            -- CUSTOMER | LEAD
    related_entity_id   UUID,
    recurrence_rule     VARCHAR(50)  NOT NULL DEFAULT 'NONE',   -- NONE | DAILY | WEEKLY | MONTHLY
    -- BaseEntity audit columns
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    version     INTEGER,
    CONSTRAINT pk_tasks PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_tasks_assigned_user   ON tasks (assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status          ON tasks (status);
CREATE INDEX IF NOT EXISTS idx_tasks_related_entity  ON tasks (related_entity_type, related_entity_id);
CREATE INDEX IF NOT EXISTS idx_tasks_deleted_at      ON tasks (deleted_at);

-- ---------------------------------------------------------------------------
-- TASK COMMENTS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS task_comments (
    id           UUID         NOT NULL,
    task_id      UUID         NOT NULL,
    content      TEXT         NOT NULL,
    author_email VARCHAR(255) NOT NULL,
    -- BaseEntity audit columns
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    version     INTEGER,
    CONSTRAINT pk_task_comments  PRIMARY KEY (id),
    CONSTRAINT fk_comments_task  FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_task_comments_task_id ON task_comments (task_id);

-- ---------------------------------------------------------------------------
-- ACTIVITY LOGS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_logs (
    id          UUID         NOT NULL,
    event_type  VARCHAR(100) NOT NULL,   -- CUSTOMER_CREATED | LEAD_CONVERTED | ...
    entity_type VARCHAR(50)  NOT NULL,   -- CUSTOMER | LEAD
    entity_id   UUID         NOT NULL,
    actor_email VARCHAR(255) NOT NULL,
    payload     TEXT,
    -- BaseEntity audit columns
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    version     INTEGER,
    CONSTRAINT pk_activity_logs PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_activity_entity ON activity_logs (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_activity_actor  ON activity_logs (actor_email);
