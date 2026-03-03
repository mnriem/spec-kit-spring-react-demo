-- V2: Model pricing table and seed data

CREATE TABLE model_pricing (
    id                                UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    model_identifier                  VARCHAR(100)  NOT NULL,
    input_price_per_million_tokens    NUMERIC(12,6) NOT NULL CHECK (input_price_per_million_tokens >= 0),
    output_price_per_million_tokens   NUMERIC(12,6) NOT NULL CHECK (output_price_per_million_tokens >= 0),
    effective_from                    DATE          NOT NULL,
    notes                             TEXT
);

CREATE UNIQUE INDEX idx_model_pricing_identifier ON model_pricing (model_identifier);

-- Seed pricing data (as of 2024-11)
INSERT INTO model_pricing (model_identifier, input_price_per_million_tokens, output_price_per_million_tokens, effective_from, notes)
VALUES
    ('gpt-4o',                      2.50,  10.00, '2024-11-01', 'OpenAI pricing as of 2024-11'),
    ('gpt-4o-mini',                 0.15,   0.60, '2024-11-01', 'OpenAI pricing as of 2024-11'),
    ('gpt-4-turbo',                10.00,  30.00, '2024-11-01', 'OpenAI pricing as of 2024-11'),
    ('claude-3-5-sonnet-20241022',  3.00,  15.00, '2024-11-01', 'Anthropic pricing as of 2024-11'),
    ('claude-3-haiku-20240307',     0.25,   1.25, '2024-11-01', 'Anthropic pricing as of 2024-11'),
    ('gemini-1.5-pro',              1.25,   5.00, '2024-11-01', 'Google pricing as of 2024-11'),
    ('gemini-1.5-flash',            0.075,  0.30, '2024-11-01', 'Google pricing as of 2024-11');
