-- Replaces the closed one-row-per-enum-constant AI provider model with admin-managed
-- provider instances: many instances can now share one adapter_type (e.g. several
-- OPENAI_COMPATIBLE instances), instead of exactly one row per hardcoded provider.

ALTER TABLE ai_provider_configuration
    ADD COLUMN adapter_type character varying(50),
    ADD COLUMN display_name character varying(255);

-- Migrate existing seeded rows forward (provider was previously the enum name):
UPDATE ai_provider_configuration SET adapter_type = 'PLACEHOLDER', display_name = 'Placeholder'
    WHERE provider = 'PLACEHOLDER';
UPDATE ai_provider_configuration SET adapter_type = 'GEMINI_GENERATE_CONTENT', display_name = 'Google Gemini'
    WHERE provider = 'GEMINI';

ALTER TABLE ai_provider_configuration
    ALTER COLUMN adapter_type SET NOT NULL,
    ALTER COLUMN display_name SET NOT NULL;

ALTER TABLE ai_provider_configuration
    DROP CONSTRAINT uk67jbcjopbn4hgjuuoh9yuynlm;

ALTER TABLE ai_provider_configuration
    DROP COLUMN provider;

ALTER TABLE generationrequest
    ADD COLUMN provider_instance_id uuid,
    ADD CONSTRAINT fk_generationrequest_provider_instance
        FOREIGN KEY (provider_instance_id) REFERENCES ai_provider_configuration(id) ON DELETE SET NULL;
