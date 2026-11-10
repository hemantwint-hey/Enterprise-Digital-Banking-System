-- =============================================================================
-- Flyway Migration V2: Database Optimization (Performance Indexes & Constraints)
-- Day 70: Enterprise Digital Banking System
-- =============================================================================

-- =============================================================================
-- 1. High-Performance B-Tree & Composite Indexes
-- =============================================================================

-- Optimize Customer -> Accounts Lookups (Dashboard Account Summary)
CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);

-- Optimize Statement Retrieval: Filter by Account + Ordered by Date (Eliminates filesort)
CREATE INDEX idx_tx_source_created ON transactions (source_account_id, created_at DESC);
CREATE INDEX idx_tx_dest_created ON transactions (destination_account_id, created_at DESC);

-- Optimize Double-Entry Ledger Audit Lookups (Filter by Account + Ordered by Date)
CREATE INDEX idx_ledger_account_created ON ledger_entries (account_id, created_at DESC);

-- Optimize Payee Management Lookups (Customer's active beneficiaries)
CREATE INDEX idx_beneficiaries_customer ON beneficiaries (customer_id, status);

-- Optimize Active Loans and EMI Schedule Queries
CREATE INDEX idx_loans_customer ON loans (customer_id, status);
CREATE INDEX idx_emis_loan_status ON emis (loan_id, status);

-- Optimize Audit Trail Searches by Entity Type and Identifier
CREATE INDEX idx_audit_entity_search ON audit_logs (entity_name, entity_id);

-- Optimize Kafka Deduplication Cleanup Tasks
CREATE INDEX idx_processed_events_type_time ON processed_events (event_type, processed_at);


-- =============================================================================
-- 2. Strict Financial & Accounting Integrity Constraints (CHECK Constraints)
-- =============================================================================

-- Account balance can never be negative (without approved overdraft facility)
ALTER TABLE accounts 
    ADD CONSTRAINT chk_account_balance_non_negative CHECK (balance >= 0.00);

-- Financial transactions must always represent a positive monetary amount
ALTER TABLE transactions 
    ADD CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0.00);

-- Double-entry ledger postings must always be positive
ALTER TABLE ledger_entries 
    ADD CONSTRAINT chk_ledger_amount_positive CHECK (amount > 0.00);

-- Loan principal must be strictly positive
ALTER TABLE loans 
    ADD CONSTRAINT chk_loan_principal_positive CHECK (principal_amount > 0.00);

-- Loan interest rate cannot be negative
ALTER TABLE loans 
    ADD CONSTRAINT chk_loan_interest_non_negative CHECK (interest_rate >= 0.00);

-- Loan tenure must be at least 1 month
ALTER TABLE loans 
    ADD CONSTRAINT chk_loan_tenure_positive CHECK (tenure_months > 0);

-- EMI monthly installment amount must be positive
ALTER TABLE emis 
    ADD CONSTRAINT chk_emi_amount_positive CHECK (amount > 0.00);

