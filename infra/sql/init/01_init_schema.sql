-- AI Scientist Ecosystem Database Schema
-- This script initializes the database with tables needed for MVP

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Space weather metrics table
CREATE TABLE IF NOT EXISTS metrics (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    source VARCHAR(50) NOT NULL, -- 'noaa', 'nasa', 'esa'
    metric_type VARCHAR(50) NOT NULL, -- 'kp_index', 'cme', 'solar_flare'
    kp_index NUMERIC(3,1), -- Kp index (0.0 to 9.0)
    cme_class VARCHAR(10), -- 'A', 'B', 'C', 'M', 'X' for solar flares
    speed_kmh INTEGER, -- CME speed in km/h
    raw_data JSONB, -- Store full API response
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT valid_kp_index CHECK (kp_index IS NULL OR (kp_index >= 0 AND kp_index <= 9))
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_metrics_timestamp ON metrics (timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_source ON metrics (source);
CREATE INDEX IF NOT EXISTS idx_metrics_type ON metrics (metric_type);
CREATE INDEX IF NOT EXISTS idx_metrics_kp ON metrics (kp_index) WHERE kp_index IS NOT NULL;

-- Alerts table
CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    severity VARCHAR(20) NOT NULL, -- 'INFO', 'WARNING', 'CRITICAL'
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    ethical_message TEXT, -- Educational context, not just warnings
    kp_index NUMERIC(3,1),
    cme_class VARCHAR(10),
    affected_regions TEXT[], -- Array of regions affected
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    acknowledged_by VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE', -- 'ACTIVE', 'ACKNOWLEDGED', 'EXPIRED'
    CONSTRAINT valid_severity CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL')),
    CONSTRAINT valid_status CHECK (status IN ('ACTIVE', 'ACKNOWLEDGED', 'EXPIRED'))
);

-- Create indexes for alerts
CREATE INDEX IF NOT EXISTS idx_alerts_severity ON alerts (severity);
CREATE INDEX IF NOT EXISTS idx_alerts_status ON alerts (status);
CREATE INDEX IF NOT EXISTS idx_alerts_created ON alerts (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_expires ON alerts (expires_at) WHERE expires_at IS NOT NULL;

-- Users table (for future notification features)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone VARCHAR(20),
    email VARCHAR(255),
    preferred_language VARCHAR(5) DEFAULT 'en', -- 'en', 'es', 'zh', 'hi', 'ar'
    notification_preferences JSONB DEFAULT '{"kp_threshold": 6, "email": true, "sms": false}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    active BOOLEAN DEFAULT true,
    CONSTRAINT valid_phone_or_email CHECK (phone IS NOT NULL OR email IS NOT NULL)
);

-- User subscriptions table
CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    alert_types VARCHAR(50)[] DEFAULT ARRAY['kp_index', 'cme', 'solar_flare'],
    regions VARCHAR(100)[], -- Geographic regions of interest
    min_severity VARCHAR(20) DEFAULT 'WARNING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    active BOOLEAN DEFAULT true,
    CONSTRAINT valid_min_severity CHECK (min_severity IN ('INFO', 'WARNING', 'CRITICAL'))
);

-- Create indexes for user tables
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users (phone);
CREATE INDEX IF NOT EXISTS idx_subscriptions_user ON subscriptions (user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_active ON subscriptions (active) WHERE active = true;

-- Alert delivery log (track what was sent to whom)
CREATE TABLE IF NOT EXISTS alert_deliveries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    alert_id UUID NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    delivery_method VARCHAR(20) NOT NULL, -- 'email', 'sms', 'push', 'cell_broadcast'
    delivery_address VARCHAR(255), -- email/phone/device_id
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    delivered_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' -- 'PENDING', 'SENT', 'DELIVERED', 'FAILED'
);

-- Create indexes for delivery tracking
CREATE INDEX IF NOT EXISTS idx_deliveries_alert ON alert_deliveries (alert_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_user ON alert_deliveries (user_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON alert_deliveries (status);
CREATE INDEX IF NOT EXISTS idx_deliveries_sent ON alert_deliveries (sent_at DESC);

-- System health table (monitoring our services)
CREATE TABLE IF NOT EXISTS system_health (
    id SERIAL PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL, -- 'data-collector', 'alert-engine', 'frontend'
    status VARCHAR(20) NOT NULL, -- 'UP', 'DOWN', 'DEGRADED'
    last_heartbeat TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    response_time_ms INTEGER,
    error_count INTEGER DEFAULT 0,
    metadata JSONB, -- Additional service-specific data
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for system health
CREATE INDEX IF NOT EXISTS idx_health_service ON system_health (service_name);
CREATE INDEX IF NOT EXISTS idx_health_status ON system_health (status);
CREATE INDEX IF NOT EXISTS idx_health_heartbeat ON system_health (last_heartbeat DESC);

-- Insert some sample data for development
INSERT INTO metrics (source, metric_type, kp_index, raw_data) VALUES
('noaa', 'kp_index', 3.7, '{"timestamp": "2024-12-03T10:00:00Z", "kp": 3.7, "source": "noaa"}'),
('noaa', 'kp_index', 5.2, '{"timestamp": "2024-12-03T13:00:00Z", "kp": 5.2, "source": "noaa"}'),
('nasa', 'cme', NULL, '{"timestamp": "2024-12-03T14:30:00Z", "class": "M2.5", "speed": 800}');

INSERT INTO alerts (severity, title, message, ethical_message, kp_index) VALUES
('WARNING', 'Moderate Geomagnetic Storm Watch', 
 'Kp index reaching 5.2 - minor disruptions to HF radio communications possible',
 'This is a natural phenomenon caused by solar wind interacting with Earth''s magnetosphere. Aurora may be visible at higher latitudes. No immediate danger to humans.',
 5.2);

-- Grant permissions (adjust username as needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ai_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ai_user;