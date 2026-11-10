-- =============================================================================
-- Flyway Database Migration V1: Initial Enterprise Digital Banking Schema
-- =============================================================================

-- 1. Users Table (Authentication & Security)
CREATE TABLE IF NOT EXISTS users (
    user_id BINARY(16) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    kyc_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BINARY(16) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    postal_code VARCHAR(255) NOT NULL,
    kyc_status VARCHAR(50) NOT NULL,
    pan_number VARCHAR(10) UNIQUE,
    aadhaar_number VARCHAR(12) UNIQUE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Accounts Table (Optimistic Locking Version)
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT NOT NULL,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    account_holder_name VARCHAR(255) NOT NULL,
    branch VARCHAR(255) NOT NULL,
    balance DECIMAL(18, 2) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    account_status VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    version BIGINT DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    transaction_reference VARCHAR(255) NOT NULL UNIQUE,
    source_account_id BIGINT,
    destination_account_id BIGINT,
    amount DECIMAL(18, 2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    transaction_status VARCHAR(50) NOT NULL,
    balance_after_transaction DECIMAL(18, 2) NOT NULL,
    remarks VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tx_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_tx_dest_account FOREIGN KEY (destination_account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Ledger Entries Table (Double-Entry Bookkeeping Ledger)
CREATE TABLE IF NOT EXISTS ledger_entries (
    id BIGINT AUTO_INCREMENT NOT NULL,
    transaction_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    balance_after_entry DECIMAL(18, 2) NOT NULL,
    narration VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ledger_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT fk_ledger_account FOREIGN KEY (account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Beneficiaries Table
CREATE TABLE IF NOT EXISTS beneficiaries (
    id BIGINT AUTO_INCREMENT NOT NULL,
    customer_id BIGINT NOT NULL,
    beneficiary_account_id BIGINT NOT NULL,
    nick_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_beneficiary_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_beneficiary_account FOREIGN KEY (beneficiary_account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Loans Table
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT NOT NULL,
    customer_id BIGINT NOT NULL,
    disbursement_account_id BIGINT NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    principal_amount DECIMAL(18, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    tenure_months INT NOT NULL,
    emi_amount DECIMAL(18, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    approved_at DATETIME(6),
    closed_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_loan_account FOREIGN KEY (disbursement_account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. EMIs Table
CREATE TABLE IF NOT EXISTS emis (
    id BIGINT AUTO_INCREMENT NOT NULL,
    loan_id BIGINT NOT NULL,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    paid_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_emi_loan FOREIGN KEY (loan_id) REFERENCES loans(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT NOT NULL,
    performed_by BINARY(16),
    action VARCHAR(50) NOT NULL,
    entity_name VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255),
    old_value LONGTEXT,
    new_value LONGTEXT,
    description VARCHAR(255),
    ip_address VARCHAR(64),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_user FOREIGN KEY (performed_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Processed Events Table (Kafka Consumer Idempotency Key Guard)
CREATE TABLE IF NOT EXISTS processed_events (
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

