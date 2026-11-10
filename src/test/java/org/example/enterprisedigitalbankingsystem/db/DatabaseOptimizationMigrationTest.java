package org.example.enterprisedigitalbankingsystem.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Day 70: Database Optimization Verification Test.
 * Validates that Flyway migration scripts define the required
 * B-Tree indexes, composite indexes, and strict financial check constraints.
 */
class DatabaseOptimizationMigrationTest {

    @Test
    @DisplayName("Should verify V1 baseline migration script exists and defines all 10 core tables")
    void testV1BaselineMigrationScript() throws IOException {
        ClassPathResource v1Resource = new ClassPathResource("db/migration/V1__init_banking_schema.sql");
        assertThat(v1Resource.exists()).isTrue();

        String v1Sql = v1Resource.getContentAsString(StandardCharsets.UTF_8);
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS users");
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS customers");
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS accounts");
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS transactions");
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS ledger_entries");
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS beneficiaries");
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS loans");
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS emis");
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS audit_logs");
        assertThat(v1Sql).contains("CREATE TABLE IF NOT EXISTS processed_events");
    }

    @Test
    @DisplayName("Should verify V2 database optimization migration defines all high-performance indexes")
    void testV2PerformanceIndexes() throws IOException {
        ClassPathResource v2Resource = new ClassPathResource("db/migration/V2__add_indexes_and_constraints.sql");
        assertThat(v2Resource.exists()).isTrue();

        String v2Sql = v2Resource.getContentAsString(StandardCharsets.UTF_8);

        // B-Tree and Composite Index verifications
        assertThat(v2Sql).contains("idx_accounts_customer_id");
        assertThat(v2Sql).contains("idx_tx_source_created");
        assertThat(v2Sql).contains("idx_tx_dest_created");
        assertThat(v2Sql).contains("idx_ledger_account_created");
        assertThat(v2Sql).contains("idx_beneficiaries_customer");
        assertThat(v2Sql).contains("idx_loans_customer");
        assertThat(v2Sql).contains("idx_emis_loan_status");
        assertThat(v2Sql).contains("idx_audit_entity_search");
        assertThat(v2Sql).contains("idx_processed_events_type_time");
    }

    @Test
    @DisplayName("Should verify V2 database optimization migration defines all financial CHECK constraints")
    void testV2FinancialConstraints() throws IOException {
        ClassPathResource v2Resource = new ClassPathResource("db/migration/V2__add_indexes_and_constraints.sql");
        assertThat(v2Resource.exists()).isTrue();

        String v2Sql = v2Resource.getContentAsString(StandardCharsets.UTF_8);

        // Strict Financial Integrity Constraint verifications
        assertThat(v2Sql).contains("chk_account_balance_non_negative");
        assertThat(v2Sql).contains("chk_transaction_amount_positive");
        assertThat(v2Sql).contains("chk_ledger_amount_positive");
        assertThat(v2Sql).contains("chk_loan_principal_positive");
        assertThat(v2Sql).contains("chk_loan_interest_non_negative");
        assertThat(v2Sql).contains("chk_loan_tenure_positive");
        assertThat(v2Sql).contains("chk_emi_amount_positive");
    }
}

