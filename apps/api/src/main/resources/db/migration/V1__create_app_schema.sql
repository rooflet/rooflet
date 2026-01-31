-- Consolidated database schema migration with UUID primary keys
-- MySQL compatible schema
-- Creates all tables for the rooflet property management system

-- Create users table
CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL DEFAULT '',
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Create portfolios table
CREATE TABLE portfolios (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    archived BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create portfolio_members table (join table between users and portfolios with roles)
CREATE TABLE portfolio_members (
    id BINARY(16) PRIMARY KEY,
    portfolio_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_portfolio_user UNIQUE (portfolio_id, user_id),
    CONSTRAINT fk_portfolio_members_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    CONSTRAINT fk_portfolio_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create properties table
CREATE TABLE properties (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    portfolio_id BINARY(16),

    -- Address fields
    address1 VARCHAR(255) NOT NULL,
    address2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,

    -- Property details
    property_type VARCHAR(50) NOT NULL,
    bedrooms INT,
    bathrooms DECIMAL(3,1),
    square_feet INT,

    -- Financial information
    market_value DECIMAL(12,2),
    purchase_price DECIMAL(12,2),
    purchase_date DATE,
    debt DECIMAL(12,2),
    interest_rate DECIMAL(5,2),

    -- Monthly costs
    monthly_hoa DECIMAL(10,2),
    monthly_property_tax DECIMAL(10,2),
    monthly_insurance DECIMAL(10,2),

    -- Additional fields
    notes TEXT,
    archived BOOLEAN DEFAULT FALSE,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_properties_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_properties_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

-- Create tenants table
CREATE TABLE tenants (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone_number VARCHAR(20),
    property_id BINARY(16),

    -- Lease information
    lease_start_date DATE,
    lease_end_date DATE,

    -- Monthly rent
    monthly_rent DECIMAL(10,2) NOT NULL,

    -- Additional fields
    archived BOOLEAN DEFAULT FALSE,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraint (nullable - tenant may not be associated with property)
    CONSTRAINT fk_tenants_property FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE SET NULL
);

-- Create expenses table
CREATE TABLE expenses (
    id BINARY(16) PRIMARY KEY,
    property_id BINARY(16),
    user_id BINARY(16),

    -- Expense details
    expense_date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description TEXT,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_expenses_property FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE SET NULL,
    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create rent_collections table
CREATE TABLE rent_collections (
    id BINARY(16) PRIMARY KEY,
    property_id BINARY(16) NOT NULL,
    tenant_id BINARY(16) NOT NULL,

    -- Payment details
    expected_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    payment_date DATE,

    -- Status and notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_rent_collections_property FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    CONSTRAINT fk_rent_collections_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

-- Create feedback table
CREATE TABLE feedback (
    id BINARY(16) PRIMARY KEY,
    feedback_type VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    current_page VARCHAR(255) NOT NULL,
    user_id BINARY(16),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes for better query performance
CREATE INDEX idx_portfolio_members_portfolio ON portfolio_members(portfolio_id);
CREATE INDEX idx_portfolio_members_user ON portfolio_members(user_id);
CREATE INDEX idx_portfolios_archived ON portfolios(archived);
CREATE INDEX idx_properties_user ON properties(user_id);
CREATE INDEX idx_properties_portfolio ON properties(portfolio_id);
CREATE INDEX idx_tenants_property ON tenants(property_id);
CREATE INDEX idx_expenses_property ON expenses(property_id);
CREATE INDEX idx_expenses_user ON expenses(user_id);
CREATE INDEX idx_rent_collections_property ON rent_collections(property_id);
CREATE INDEX idx_rent_collections_tenant ON rent_collections(tenant_id);
CREATE INDEX idx_feedback_user ON feedback(user_id);

-- Create market_listings table for storing property listings from various sources
-- This table will be populated by periodic scrapers from sources like Redfin, Zillow, etc.
CREATE TABLE market_listings (
    id BINARY(16) PRIMARY KEY,

    -- Source information
    source VARCHAR(50) NOT NULL,                    -- e.g., 'REDFIN', 'ZILLOW', 'REALTOR'
    source_listing_id VARCHAR(255),                 -- External listing ID from the source
    source_url TEXT,                                -- URL to the listing on the source website
    source_mls_number VARCHAR(100),                 -- MLS listing number if available

    -- Sale information
    sale_type VARCHAR(50),                          -- e.g., 'MLS Listing', 'For Sale by Owner', etc.
    listing_status VARCHAR(50) NOT NULL,            -- e.g., 'ACTIVE', 'PENDING', 'SOLD', 'OFF_MARKET'
    sold_date DATE,                                 -- Date property was sold (if applicable)
    list_date DATE,                                 -- Date property was listed
    days_on_market INT,                             -- Number of days on market

    -- Address information
    address VARCHAR(500) NOT NULL,                  -- Full address as provided by source
    address1 VARCHAR(255),                          -- Parsed primary address
    address2 VARCHAR(255),                          -- Parsed secondary address (unit, apt, etc.)
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    location VARCHAR(100),                          -- Neighborhood or area name
    latitude DECIMAL(10, 8),                        -- Geographic latitude
    longitude DECIMAL(11, 8),                       -- Geographic longitude

    -- Property details
    property_type VARCHAR(100) NOT NULL,            -- e.g., 'Single Family', 'Condo', 'Multi-Family', 'Townhouse', 'Vacant Land'
    bedrooms INT,                                   -- Number of bedrooms (null for land/commercial)
    bathrooms DECIMAL(4,1),                         -- Number of bathrooms (includes half baths)
    square_feet INT,                                -- Interior square footage
    lot_size INT,                                   -- Lot size in square feet
    year_built INT,                                 -- Year property was built

    -- Pricing information
    price DECIMAL(15,2),                            -- Current listing price or sold price
    price_per_sqft DECIMAL(10,2),                   -- Price per square foot
    original_list_price DECIMAL(15,2),              -- Original listing price (for price history)

    -- HOA and fees
    hoa_fee DECIMAL(10,2),                          -- Monthly HOA fee

    -- Open house information
    next_open_house_start TIMESTAMP,                -- Next open house start time
    next_open_house_end TIMESTAMP,                  -- Next open house end time

    -- Additional metadata
    raw_data TEXT,                                  -- JSON/text storage for additional source-specific fields

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    last_scraped_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Constraints
    CONSTRAINT uk_market_listings_source UNIQUE (source, source_listing_id)
);

-- Create indexes for market_listings
CREATE INDEX idx_market_listings_source ON market_listings(source);
CREATE INDEX idx_market_listings_status ON market_listings(listing_status);
CREATE INDEX idx_market_listings_location ON market_listings(city, state, zip_code);
CREATE INDEX idx_market_listings_property_type ON market_listings(property_type);
CREATE INDEX idx_market_listings_price ON market_listings(price);
CREATE INDEX idx_market_listings_bedrooms_bathrooms ON market_listings(bedrooms, bathrooms);
CREATE INDEX idx_market_listings_scraped_at ON market_listings(last_scraped_at);
CREATE INDEX idx_market_listings_days_on_market ON market_listings(days_on_market);
CREATE INDEX idx_market_listings_coordinates ON market_listings(latitude, longitude);

-- Create user_market_listing_preferences table (bridge table for user-specific interested flags)
CREATE TABLE user_market_listing_preferences (
    user_id BINARY(16) NOT NULL,
    listing_id BINARY(16) NOT NULL,

    -- User preference flag
    is_interested BOOLEAN DEFAULT FALSE NOT NULL,

    -- Optional notes
    notes VARCHAR(2000),

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

    -- Composite primary key
    PRIMARY KEY (user_id, listing_id),

    -- Foreign key constraints with cascade delete
    CONSTRAINT fk_user_ml_pref_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_ml_pref_listing FOREIGN KEY (listing_id) REFERENCES market_listings(id) ON DELETE CASCADE
);

-- Create user_market_listing_lists table (custom lists for organizing market listings)
CREATE TABLE user_market_listing_lists (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,

    -- List details
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    color VARCHAR(20),                              -- Hex color code or named color

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

    -- Foreign key constraint with cascade delete
    CONSTRAINT fk_user_ml_lists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create user_market_listing_list_items table (join table between lists and listings)
CREATE TABLE user_market_listing_list_items (
    list_id BINARY(16) NOT NULL,
    listing_id BINARY(16) NOT NULL,

    -- Audit field
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Composite primary key
    PRIMARY KEY (list_id, listing_id),

    -- Foreign key constraints with cascade delete
    CONSTRAINT fk_user_ml_list_items_list FOREIGN KEY (list_id) REFERENCES user_market_listing_lists(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_ml_list_items_listing FOREIGN KEY (listing_id) REFERENCES market_listings(id) ON DELETE CASCADE
);

-- Create indexes for user_market_listing_preferences
CREATE INDEX idx_user_ml_pref_user ON user_market_listing_preferences(user_id);
CREATE INDEX idx_user_ml_pref_interested ON user_market_listing_preferences(is_interested);

-- Create indexes for user_market_listing_lists
CREATE INDEX idx_user_ml_lists_user ON user_market_listing_lists(user_id);

-- Create indexes for user_market_listing_list_items
CREATE INDEX idx_user_ml_list_items_list ON user_market_listing_list_items(list_id);
CREATE INDEX idx_user_ml_list_items_listing ON user_market_listing_list_items(listing_id);

-- Create user_zip_code_preferences table for tracking which zip codes users want to watch
-- This enables filtering market listings to only show data from user-selected zip codes
CREATE TABLE user_zip_code_preferences (
    user_id BINARY(16) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,

    -- Audit field
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Composite primary key
    PRIMARY KEY (user_id, zip_code),

    -- Foreign key constraint with cascade delete
    CONSTRAINT fk_user_zip_pref_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for faster lookups by user
CREATE INDEX idx_user_zip_pref_user ON user_zip_code_preferences(user_id);

-- Create index for faster lookups by zip code
CREATE INDEX idx_user_zip_pref_zip ON user_zip_code_preferences(zip_code);

-- Create expected_rent table for storing rent expectations by zip code and bedroom count
-- Data source: Boston Housing Authority Payment Standard Chart
-- Effective July 1, 2025

CREATE TABLE expected_rent (
    id BINARY(16) PRIMARY KEY,
    zip_code VARCHAR(10) NOT NULL,
    bedrooms INT NOT NULL,
    expected_rent DECIMAL(10,2) NOT NULL,

    -- Metadata
    source VARCHAR(100) DEFAULT 'Boston Housing Authority Payment Standard' NOT NULL,
    effective_date DATE NOT NULL,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

    -- Unique constraint to prevent duplicate entries
    CONSTRAINT uk_expected_rent_zip_bedrooms UNIQUE (zip_code, bedrooms, effective_date)
);

-- Create indexes for expected_rent
CREATE INDEX idx_expected_rent_zip ON expected_rent(zip_code);
CREATE INDEX idx_expected_rent_bedrooms ON expected_rent(bedrooms);
CREATE INDEX idx_expected_rent_zip_bedrooms ON expected_rent(zip_code, bedrooms);
CREATE INDEX idx_expected_rent_effective_date ON expected_rent(effective_date);
