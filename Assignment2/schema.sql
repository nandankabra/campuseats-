-- CampusEats — CREATE TABLE sketch
-- CS543 Web Services, Assignment 2, Task 5
--
-- One schema per service. In deployment each schema would be its own
-- database; they are kept as separate schemas here so the whole design
-- can be created and inspected in one place.
--
-- BOUNDARY RULE: a FOREIGN KEY only ever points at a table in the SAME
-- schema. Columns ending in _ref hold an id owned by another service and
-- deliberately carry NO foreign key constraint -- that value is resolved
-- by calling the owning service, not by joining to it.

CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS catalogue;
CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS payments;
CREATE SCHEMA IF NOT EXISTS delivery;


-- ====================================================================
-- Identity Service  (schema: identity)
-- ====================================================================

CREATE TABLE identity.app_user (
    user_id              UUID           PRIMARY KEY,
    email                VARCHAR(255)   NOT NULL UNIQUE,
    password_hash        CHAR(60)       NOT NULL,
    display_name         VARCHAR(120)   NOT NULL,
    role                 VARCHAR(20)    NOT NULL,
    status               VARCHAR(20)    NOT NULL DEFAULT 'active',
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE identity.courier_profile (
    courier_id           UUID           PRIMARY KEY,
    user_id              UUID           NOT NULL UNIQUE,
    vehicle_type         VARCHAR(20)    NOT NULL,
    is_on_shift          BOOLEAN        NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES identity.app_user(user_id) ON DELETE CASCADE
);

CREATE TABLE identity.outlet_staff (
    staff_id             UUID           PRIMARY KEY,
    user_id              UUID           NOT NULL,
    outlet_ref           UUID           NOT NULL,   -- cross-service id, no FK by design
    FOREIGN KEY (user_id) REFERENCES identity.app_user(user_id) ON DELETE CASCADE
);


-- ====================================================================
-- Catalogue Service  (schema: catalogue)
-- ====================================================================

CREATE TABLE catalogue.outlet (
    outlet_id            UUID           PRIMARY KEY,
    name                 VARCHAR(120)   NOT NULL,
    campus_zone          VARCHAR(60)    NOT NULL,
    is_open              BOOLEAN        NOT NULL DEFAULT FALSE,
    opens_at             TIME,
    closes_at            TIME
);

CREATE TABLE catalogue.menu_item (
    item_id              UUID           PRIMARY KEY,
    outlet_id            UUID           NOT NULL,
    name                 VARCHAR(120)   NOT NULL,
    price_cents          INTEGER        NOT NULL CHECK (price_cents >= 0),
    currency             CHAR(3)        NOT NULL DEFAULT 'GBP',
    is_available         BOOLEAN        NOT NULL DEFAULT TRUE,
    FOREIGN KEY (outlet_id) REFERENCES catalogue.outlet(outlet_id) ON DELETE CASCADE
);

CREATE TABLE catalogue.review (
    review_id            UUID           PRIMARY KEY,
    outlet_id            UUID           NOT NULL,
    order_ref            UUID           NOT NULL UNIQUE,   -- cross-service id, no FK by design
    customer_ref         UUID           NOT NULL,   -- cross-service id, no FK by design
    rating               SMALLINT       NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment              TEXT,
    FOREIGN KEY (outlet_id) REFERENCES catalogue.outlet(outlet_id) ON DELETE CASCADE
);


-- ====================================================================
-- Orders Service  (schema: orders)
-- ====================================================================

CREATE TABLE orders.cart (
    cart_id              UUID           PRIMARY KEY,
    customer_ref         UUID           NOT NULL,   -- cross-service id, no FK by design
    outlet_ref           UUID           NOT NULL,   -- cross-service id, no FK by design
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE orders.cart_line (
    cart_line_id         UUID           PRIMARY KEY,
    cart_id              UUID           NOT NULL,
    item_ref             UUID           NOT NULL,   -- cross-service id, no FK by design
    quantity             SMALLINT       NOT NULL CHECK (quantity > 0),
    FOREIGN KEY (cart_id) REFERENCES orders.cart(cart_id) ON DELETE CASCADE
);

CREATE TABLE orders.customer_order (
    order_id             UUID           PRIMARY KEY,
    customer_ref         UUID           NOT NULL,   -- cross-service id, no FK by design
    outlet_ref           UUID           NOT NULL,   -- cross-service id, no FK by design
    location_ref         UUID           NOT NULL,   -- cross-service id, no FK by design
    status               VARCHAR(20)    NOT NULL DEFAULT 'placed',
    total_cents          INTEGER        NOT NULL CHECK (total_cents >= 0),
    currency             CHAR(3)        NOT NULL DEFAULT 'GBP',
    placed_at            TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE orders.order_line (
    order_line_id        UUID           PRIMARY KEY,
    order_id             UUID           NOT NULL,
    item_ref             UUID           NOT NULL,   -- cross-service id, no FK by design
    item_name_snap       VARCHAR(120)   NOT NULL,
    unit_price_cents     INTEGER        NOT NULL,
    quantity             SMALLINT       NOT NULL CHECK (quantity > 0),
    FOREIGN KEY (order_id) REFERENCES orders.customer_order(order_id) ON DELETE CASCADE
);

CREATE TABLE orders.order_status_history (
    history_id           UUID           PRIMARY KEY,
    order_id             UUID           NOT NULL,
    from_status          VARCHAR(20),
    to_status            VARCHAR(20)    NOT NULL,
    changed_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    FOREIGN KEY (order_id) REFERENCES orders.customer_order(order_id) ON DELETE CASCADE
);


-- ====================================================================
-- Payments Service  (schema: payments)
-- ====================================================================

CREATE TABLE payments.payment (
    payment_id           UUID           PRIMARY KEY,
    order_ref            UUID           NOT NULL UNIQUE,   -- cross-service id, no FK by design
    customer_ref         UUID           NOT NULL,   -- cross-service id, no FK by design
    amount_cents         INTEGER        NOT NULL CHECK (amount_cents > 0),
    currency             CHAR(3)        NOT NULL DEFAULT 'GBP',
    status               VARCHAR(20)    NOT NULL,
    provider_ref         VARCHAR(120),
    captured_at          TIMESTAMPTZ
);

CREATE TABLE payments.refund (
    refund_id            UUID           PRIMARY KEY,
    payment_id           UUID           NOT NULL,
    amount_cents         INTEGER        NOT NULL CHECK (amount_cents > 0),
    reason               VARCHAR(200),
    status               VARCHAR(20)    NOT NULL,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    FOREIGN KEY (payment_id) REFERENCES payments.payment(payment_id) ON DELETE CASCADE
);


-- ====================================================================
-- Delivery Service  (schema: delivery)
-- ====================================================================

CREATE TABLE delivery.campus_location (
    location_id          UUID           PRIMARY KEY,
    name                 VARCHAR(120)   NOT NULL,
    building             VARCHAR(80)    NOT NULL,
    floor_label          VARCHAR(40),
    is_active            BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE delivery.delivery (
    delivery_id          UUID           PRIMARY KEY,
    order_ref            UUID           NOT NULL UNIQUE,   -- cross-service id, no FK by design
    courier_ref          UUID,   -- cross-service id, no FK by design
    location_id          UUID           NOT NULL,
    status               VARCHAR(20)    NOT NULL DEFAULT 'pending',
    collected_at         TIMESTAMPTZ,
    delivered_at         TIMESTAMPTZ,
    FOREIGN KEY (location_id) REFERENCES delivery.campus_location(location_id) ON DELETE CASCADE
);

CREATE TABLE delivery.delivery_offer (
    offer_id             UUID           PRIMARY KEY,
    delivery_id          UUID           NOT NULL,
    courier_ref          UUID           NOT NULL,   -- cross-service id, no FK by design
    offered_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    response             VARCHAR(20),
    FOREIGN KEY (delivery_id) REFERENCES delivery.delivery(delivery_id) ON DELETE CASCADE
);


-- ====================================================================
-- Indexes on the cross-service reference columns.
-- These are looked up constantly but cannot be indexed via a FK,
-- so they need explicit indexes.
-- ====================================================================

CREATE INDEX idx_customer_order_customer_ref ON orders.customer_order(customer_ref);
CREATE INDEX idx_customer_order_outlet_ref ON orders.customer_order(outlet_ref);
CREATE INDEX idx_customer_order_status ON orders.customer_order(status);
CREATE INDEX idx_menu_item_outlet_id ON catalogue.menu_item(outlet_id);
CREATE INDEX idx_payment_order_ref ON payments.payment(order_ref);
CREATE INDEX idx_delivery_order_ref ON delivery.delivery(order_ref);
CREATE INDEX idx_delivery_courier_ref ON delivery.delivery(courier_ref);
CREATE INDEX idx_outlet_staff_outlet_ref ON identity.outlet_staff(outlet_ref);
