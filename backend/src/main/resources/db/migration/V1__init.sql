-- ===================================================================
-- Materia schema, version 1: everything a fresh install needs.
--
-- Flyway owns the schema; Hibernate only validates it (spring.jpa.hibernate.ddl-auto=validate), so a change
-- to an entity needs a new migration (V2__..., V3__...) next to this one, never an edit of this file once
-- an environment ran it.
--
-- Generated with pg_dump --schema-only from the database the previous migrations built, then stripped of the
-- dump's session settings so it runs on PostgreSQL 16 and 17. No row is inserted: the first administrator is
-- created at start-up from APP_BOOTSTRAP_ADMIN_EMAIL / APP_BOOTSTRAP_ADMIN_PASSWORD.
-- ===================================================================

-- -------------------------------------------------------------------
-- Tables (23)
-- -------------------------------------------------------------------

CREATE TABLE public.categories (
    level integer,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint,
    id uuid NOT NULL,
    status character varying(20) NOT NULL,
    category_type character varying(30),
    code character varying(50) NOT NULL,
    parent_code character varying(50),
    created_by character varying(100),
    name character varying(100) NOT NULL,
    updated_by character varying(100),
    short_description character varying(200),
    description character varying(500),
    path character varying(500),
    parent_id character varying(255),
    CONSTRAINT categories_category_type_check CHECK (((category_type)::text = ANY ((ARRAY['MATERIAL'::character varying, 'PRODUCT'::character varying, 'SERVICE'::character varying, 'RAW_MATERIAL_CAT'::character varying, 'COMPONENT_CAT'::character varying, 'PACKAGING_CAT'::character varying, 'SPARE_PART_CAT'::character varying, 'CONSUMABLE_CAT'::character varying, 'TOOL_CAT'::character varying, 'CHEMICAL_CAT'::character varying, 'ELECTRONIC_CAT'::character varying, 'FAMILY'::character varying, 'BRAND'::character varying, 'DEPARTMENT'::character varying, 'PROJECT'::character varying, 'GEOGRAPHIC'::character varying, 'SEASONAL'::character varying])::text[])))
);

CREATE TABLE public.employees (
    id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    version bigint DEFAULT 0,
    created_by character varying(100),
    updated_by character varying(100),
    code character varying(50) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    full_name character varying(200) NOT NULL,
    email character varying(255) NOT NULL,
    phone character varying(50),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    user_id uuid,
    hire_date date,
    termination_date date,
    termination_reason character varying(500)
);

CREATE TABLE public.global_code_sequences (
    next_val integer NOT NULL,
    prefix character varying(30) NOT NULL
);

CREATE TABLE public.goods_receipt_lines (
    expiry_date date,
    line_number integer,
    line_total numeric(19,4),
    quantity_accepted integer,
    quantity_ordered integer,
    quantity_pending integer,
    quantity_received integer,
    quantity_rejected integer,
    stock_after integer,
    stock_before integer,
    unit_price numeric(19,4),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint,
    currency_code character varying(10),
    goods_receipt_id uuid NOT NULL,
    id uuid NOT NULL,
    material_id uuid,
    unit_of_measure character varying(20),
    quality_status character varying(30),
    material_code character varying(50) NOT NULL,
    batch_number character varying(100),
    created_by character varying(100),
    purchase_order_line_id character varying(100),
    storage_location character varying(100),
    supplier_id character varying(100),
    updated_by character varying(100),
    notes character varying(1000),
    quality_notes character varying(1000),
    rejection_reason character varying(1000),
    material_name character varying(255),
    supplier_name character varying(255),
    CONSTRAINT goods_receipt_lines_quality_status_check CHECK (((quality_status)::text = ANY ((ARRAY['ACCEPTED'::character varying, 'REJECTED'::character varying, 'UNDER_REVIEW'::character varying, 'PARTIAL'::character varying])::text[])))
);

CREATE TABLE public.goods_receipts (
    expected_delivery_date date,
    has_discrepancy boolean NOT NULL,
    receipt_date date,
    total_quantity_accepted integer,
    total_quantity_ordered integer,
    total_quantity_received integer,
    total_quantity_rejected integer,
    created_at timestamp(6) without time zone NOT NULL,
    obsoleted_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    version bigint,
    id uuid NOT NULL,
    status character varying(30) NOT NULL,
    receipt_code character varying(50) NOT NULL,
    created_by character varying(100),
    obsoleted_by character varying(100),
    purchase_order_code character varying(100),
    purchase_order_id character varying(100) NOT NULL,
    received_by character varying(100) NOT NULL,
    supplier_id character varying(100),
    updated_by character varying(100),
    discrepancy_notes character varying(1000),
    notes character varying(1000),
    obsoleted_reason character varying(1000),
    received_by_name character varying(255) NOT NULL,
    supplier_name character varying(255),
    CONSTRAINT goods_receipts_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'PARTIAL'::character varying, 'CANCELLED'::character varying])::text[])))
);

CREATE TABLE public.invoice_lines (
    id uuid NOT NULL,
    invoice_id uuid NOT NULL,
    line_number integer NOT NULL,
    purchase_order_line_id character varying(100),
    goods_receipt_line_id character varying(100),
    material_code character varying(50),
    material_name character varying(255) NOT NULL,
    unit_of_measure character varying(50),
    quantity_ordered integer,
    quantity_received integer,
    quantity_invoiced integer NOT NULL,
    quantity_discrepancy integer,
    unit_price numeric(19,4) NOT NULL,
    line_total numeric(19,4) NOT NULL,
    tax_amount numeric(19,4),
    line_total_with_tax numeric(19,4),
    currency_code character varying(3) NOT NULL,
    has_quantity_discrepancy boolean NOT NULL,
    discrepancy_notes character varying(1000),
    notes character varying(1000),
    created_at timestamp(6) without time zone NOT NULL,
    created_by character varying(100),
    updated_at timestamp(6) without time zone,
    updated_by character varying(100),
    version bigint
);

CREATE TABLE public.invoices (
    id uuid NOT NULL,
    invoice_code character varying(50) NOT NULL,
    invoice_type character varying(30) NOT NULL,
    status character varying(30) NOT NULL,
    purchase_order_id character varying(100),
    purchase_order_code character varying(100),
    goods_receipt_id character varying(100),
    goods_receipt_code character varying(100),
    supplier_id character varying(100) NOT NULL,
    supplier_name character varying(255) NOT NULL,
    supplier_code character varying(50),
    external_reference character varying(100),
    invoice_date date NOT NULL,
    due_date date,
    received_date date,
    payment_date date,
    currency_code character varying(3) NOT NULL,
    total_amount numeric(19,4) NOT NULL,
    total_tax_amount numeric(19,4) NOT NULL,
    total_amount_with_tax numeric(19,4) NOT NULL,
    is_verified boolean NOT NULL,
    has_discrepancy boolean NOT NULL,
    discrepancy_summary character varying(1000),
    verification_date timestamp(6) without time zone,
    verified_by character varying(100),
    verified_by_name character varying(255),
    paid_amount numeric(19,4),
    paid_at timestamp(6) without time zone,
    paid_by character varying(100),
    paid_by_name character varying(255),
    notes character varying(1000),
    internal_notes character varying(1000),
    created_at timestamp(6) without time zone NOT NULL,
    created_by character varying(100),
    updated_at timestamp(6) without time zone,
    updated_by character varying(100),
    version bigint,
    CONSTRAINT invoices_invoice_type_check CHECK (((invoice_type)::text = ANY ((ARRAY['STANDARD'::character varying, 'CREDIT_NOTE'::character varying])::text[]))),
    CONSTRAINT invoices_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'VERIFIED'::character varying, 'PAID'::character varying, 'CANCELLED'::character varying])::text[])))
);

CREATE TABLE public.material_stock_movements (
    new_stock integer NOT NULL,
    previous_stock integer NOT NULL,
    quantity integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    occurred_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint,
    id uuid NOT NULL,
    material_id uuid NOT NULL,
    movement_type character varying(30) NOT NULL,
    created_by character varying(100),
    updated_by character varying(100),
    reason character varying(500),
    CONSTRAINT material_stock_movements_movement_type_check CHECK (((movement_type)::text = ANY ((ARRAY['OPENING_BALANCE'::character varying, 'RECEIPT'::character varying, 'ISSUE'::character varying, 'ADJUSTMENT'::character varying])::text[])))
);

CREATE TABLE public.materials (
    available_stock integer,
    average_purchase_price numeric(19,4),
    cost_price numeric(19,4),
    current_stock integer,
    economic_order_quantity integer,
    stock_on_order integer,
    last_purchase_price numeric(19,4),
    maximum_stock integer,
    minimum_stock integer,
    reorder_point integer,
    safety_stock integer,
    standard_price numeric(19,4),
    created_at timestamp(6) without time zone NOT NULL,
    obsoleted_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    version bigint,
    average_purchase_price_currency character varying(10),
    cost_price_currency character varying(10),
    last_purchase_price_currency character varying(10),
    standard_price_currency character varying(10),
    id uuid NOT NULL,
    unit_of_measure character varying(20),
    material_type character varying(30) NOT NULL,
    status character varying(30) NOT NULL,
    code character varying(50) NOT NULL,
    category_name character varying(100),
    created_by character varying(100),
    obsoleted_by character varying(100),
    updated_by character varying(100),
    short_description character varying(200),
    obsoleted_reason character varying(500),
    search_keywords character varying(500),
    description character varying(1000),
    alternative_name character varying(255),
    category_id character varying(255),
    name character varying(255) NOT NULL,
    supplier_id character varying(255),
    supplier_name character varying(255),
    CONSTRAINT materials_average_purchase_price_currency_check CHECK (((average_purchase_price_currency)::text = ANY ((ARRAY['MAD'::character varying, 'EUR'::character varying, 'USD'::character varying])::text[]))),
    CONSTRAINT materials_cost_price_currency_check CHECK (((cost_price_currency)::text = ANY ((ARRAY['MAD'::character varying, 'EUR'::character varying, 'USD'::character varying])::text[]))),
    CONSTRAINT materials_last_purchase_price_currency_check CHECK (((last_purchase_price_currency)::text = ANY ((ARRAY['MAD'::character varying, 'EUR'::character varying, 'USD'::character varying])::text[]))),
    CONSTRAINT materials_material_type_check CHECK (((material_type)::text = ANY ((ARRAY['RAW_MATERIAL'::character varying, 'FINISHED_GOOD'::character varying, 'COMPONENT'::character varying, 'PACKAGING'::character varying, 'SPARE_PART'::character varying, 'CONSUMABLE'::character varying, 'SERVICE'::character varying, 'TOOL'::character varying, 'CHEMICAL'::character varying, 'ELECTRONIC'::character varying])::text[]))),
    CONSTRAINT materials_standard_price_currency_check CHECK (((standard_price_currency)::text = ANY ((ARRAY['MAD'::character varying, 'EUR'::character varying, 'USD'::character varying])::text[]))),
    CONSTRAINT materials_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'ACTIVE'::character varying, 'INACTIVE'::character varying, 'BLOCKED'::character varying, 'OBSOLETE'::character varying])::text[]))),
    CONSTRAINT materials_unit_of_measure_check CHECK (((unit_of_measure)::text = ANY ((ARRAY['KG'::character varying, 'G'::character varying, 'T'::character varying, 'MG'::character varying, 'LB'::character varying, 'OZ'::character varying, 'L'::character varying, 'ML'::character varying, 'M3'::character varying, 'DM3'::character varying, 'CM3'::character varying, 'GAL'::character varying, 'FT3'::character varying, 'M'::character varying, 'CM'::character varying, 'MM'::character varying, 'KM'::character varying, 'IN'::character varying, 'FT'::character varying, 'YD'::character varying, 'M2'::character varying, 'CM2'::character varying, 'MM2'::character varying, 'HA'::character varying, 'ACRE'::character varying, 'PCE'::character varying, 'BOX'::character varying, 'CART'::character varying, 'PACK'::character varying, 'SET'::character varying, 'PAL'::character varying, 'DRUM'::character varying, 'ROLL'::character varying, 'SHEET'::character varying, 'REEL'::character varying, 'HOUR'::character varying, 'DAY'::character varying, 'WEEK'::character varying, 'MONTH'::character varying, 'KWH'::character varying, 'KW'::character varying, 'HP'::character varying, 'C'::character varying, 'F'::character varying, 'K'::character varying, 'BAR'::character varying, 'PSI'::character varying, 'PA'::character varying, 'ATM'::character varying, 'NONE'::character varying])::text[])))
);

CREATE TABLE public.outbox_messages (
    id uuid NOT NULL,
    event_id uuid NOT NULL,
    event_type character varying(150) NOT NULL,
    source_context character varying(60) NOT NULL,
    aggregate_id character varying(100),
    payload text NOT NULL,
    occurred_at timestamp(6) without time zone NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    published_at timestamp(6) without time zone,
    attempts integer NOT NULL,
    last_error character varying(1000)
);

CREATE TABLE public.password_reset_tokens (
    id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    version bigint DEFAULT 0,
    created_by character varying(100),
    updated_by character varying(100),
    token character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    email character varying(255) NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    used boolean DEFAULT false NOT NULL
);

CREATE TABLE public.payment_lines (
    id uuid NOT NULL,
    payment_id uuid NOT NULL,
    line_number integer NOT NULL,
    invoice_id character varying(100) NOT NULL,
    invoice_code character varying(100),
    supplier_id character varying(100) NOT NULL,
    supplier_name character varying(255),
    amount numeric(19,4) NOT NULL,
    paid_amount numeric(19,4),
    currency_code character varying(3) NOT NULL,
    is_paid boolean NOT NULL,
    notes character varying(1000),
    created_at timestamp(6) without time zone NOT NULL,
    created_by character varying(100),
    updated_at timestamp(6) without time zone,
    updated_by character varying(100),
    version bigint
);

CREATE TABLE public.payments (
    id uuid NOT NULL,
    payment_code character varying(50) NOT NULL,
    status character varying(30) NOT NULL,
    supplier_id character varying(100) NOT NULL,
    supplier_name character varying(255) NOT NULL,
    supplier_code character varying(50),
    total_amount numeric(19,4) NOT NULL,
    paid_amount numeric(19,4),
    currency_code character varying(3) NOT NULL,
    payment_date timestamp(6) without time zone,
    confirmed_date timestamp(6) without time zone,
    bank_reference character varying(100),
    transaction_id character varying(100),
    payment_method character varying(50),
    payment_receipt character varying(255),
    notes character varying(1000),
    internal_notes character varying(1000),
    created_at timestamp(6) without time zone NOT NULL,
    created_by character varying(100),
    updated_at timestamp(6) without time zone,
    updated_by character varying(100),
    version bigint,
    CONSTRAINT payments_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PENDING'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);

CREATE TABLE public.purchase_order_lines (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint,
    created_by character varying(100),
    updated_by character varying(100),
    purchase_order_id uuid NOT NULL,
    line_number integer,
    requisition_line_id uuid,
    material_id uuid,
    material_code character varying(50),
    material_name character varying(255),
    material_description character varying(1000),
    unit_of_measure character varying(20),
    quantity integer,
    unit_price numeric(19,4),
    line_total numeric(19,4),
    currency_code character varying(10),
    supplier_id uuid,
    supplier_name character varying(255),
    expected_delivery_date date,
    notes character varying(1000)
);

CREATE TABLE public.purchase_orders (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint,
    created_by character varying(100),
    updated_by character varying(100),
    order_code character varying(50) NOT NULL,
    requisition_id uuid,
    requisition_code character varying(100),
    status character varying(30) NOT NULL,
    delivery_status character varying(30) NOT NULL,
    supplier_id uuid NOT NULL,
    supplier_name character varying(255) NOT NULL,
    supplier_code character varying(50),
    order_date date,
    expected_delivery_date date,
    confirmed_delivery_date date,
    received_date date,
    payment_terms character varying(255),
    payment_delay_days integer,
    delivery_terms character varying(255),
    incoterm character varying(20),
    currency_code character varying(10),
    total_amount numeric(19,4),
    tax_amount numeric(19,4),
    shipping_cost numeric(19,4),
    grand_total numeric(19,4),
    ordered_by character varying(100),
    ordered_by_name character varying(255),
    approved_by character varying(100),
    approved_by_name character varying(255),
    assigned_to character varying(100),
    assigned_to_name character varying(255),
    assigned_at timestamp(6) without time zone,
    assigned_by character varying(100),
    assigned_by_name character varying(255),
    notes character varying(1000),
    internal_notes character varying(1000),
    obsoleted_at timestamp(6) without time zone,
    obsoleted_by character varying(100),
    obsoleted_reason character varying(1000),
    CONSTRAINT purchase_orders_delivery_status_check CHECK (((delivery_status)::text = ANY ((ARRAY['NOT_SHIPPED'::character varying, 'SHIPPED'::character varying, 'IN_TRANSIT'::character varying, 'PARTIAL'::character varying, 'DELIVERED'::character varying, 'DELAYED'::character varying])::text[]))),
    CONSTRAINT purchase_orders_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'CONFIRMED'::character varying, 'READY_FOR_RECEIPT'::character varying, 'RECEIVED'::character varying, 'PARTIALLY_RECEIVED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying, 'REJECTED'::character varying])::text[])))
);

CREATE TABLE public.purchase_requisition_lines (
    expiry_date date,
    line_number integer,
    line_total numeric(19,4),
    quantity integer,
    quantity_received integer,
    quantity_rejected integer,
    required_date date,
    standard_price numeric(19,4),
    unit_price numeric(19,4),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint,
    currency_code character varying(10),
    currency_code_line character varying(10),
    id uuid NOT NULL,
    material_id uuid,
    requisition_id uuid NOT NULL,
    supplier_id uuid,
    unit_of_measure character varying(20),
    material_code character varying(50),
    supplier_code character varying(50),
    batch_number character varying(100),
    created_by character varying(100),
    storage_location character varying(100),
    updated_by character varying(100),
    material_description character varying(1000),
    notes character varying(1000),
    delivery_terms character varying(255),
    material_name character varying(255),
    supplier_name character varying(255)
);

CREATE TABLE public.purchase_requisitions (
    approved_date date,
    cancelled_date date,
    converted_date date,
    required_date date,
    submitted_date date,
    total_amount numeric(19,4),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint,
    currency_code character varying(10),
    id uuid NOT NULL,
    status character varying(30) NOT NULL,
    requisition_code character varying(50) NOT NULL,
    approver_id character varying(100),
    created_by character varying(100),
    purchase_order_code character varying(100),
    purchase_order_id character varying(100),
    requester_id character varying(100) NOT NULL,
    updated_by character varying(100),
    justification character varying(500),
    approval_notes character varying(1000),
    cancellation_reason character varying(1000),
    description character varying(1000),
    rejection_reason character varying(1000),
    approver_name character varying(255),
    requester_name character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    CONSTRAINT purchase_requisitions_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying, 'CONVERTED'::character varying])::text[])))
);

CREATE TABLE public.refresh_tokens (
    id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    version bigint DEFAULT 0,
    token_hash character varying(64) NOT NULL,
    user_id uuid NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    revoked boolean DEFAULT false NOT NULL,
    device_info character varying(255),
    ip_address character varying(50),
    created_by character varying(100),
    updated_by character varying(100)
);

CREATE TABLE public.return_to_vendor (
    id uuid NOT NULL,
    return_code character varying(50) NOT NULL,
    goods_receipt_id character varying(100),
    goods_receipt_code character varying(100),
    purchase_order_id character varying(100),
    purchase_order_code character varying(100),
    supplier_id character varying(100),
    supplier_name character varying(200),
    supplier_code character varying(100),
    status character varying(50),
    resolution_type character varying(50),
    return_date date,
    resolution_date date,
    return_reason character varying(500),
    supplier_response character varying(500),
    rejection_summary character varying(500),
    credit_note_reference character varying(100),
    credit_note_amount character varying(50),
    replacement_purchase_order_reference character varying(100),
    replacement_purchase_order_code character varying(100),
    notes character varying(1000),
    internal_notes character varying(1000),
    created_at timestamp(6) without time zone NOT NULL,
    created_by character varying(100),
    updated_at timestamp(6) without time zone,
    updated_by character varying(100),
    version bigint,
    currency_code character varying(3)
);

CREATE TABLE public.return_to_vendor_lines (
    id uuid NOT NULL,
    return_to_vendor_id uuid NOT NULL,
    line_number integer,
    goods_receipt_line_id character varying(100),
    material_code character varying(100),
    material_name character varying(200),
    unit_of_measure character varying(50),
    rejected_quantity integer,
    quantity_to_return integer,
    quantity_already_returned integer,
    rejection_reason character varying(500),
    quality_notes character varying(500),
    defect_description character varying(500),
    is_replaced boolean,
    is_credit_note boolean,
    notes character varying(500),
    created_at timestamp(6) without time zone NOT NULL,
    created_by character varying(100),
    updated_at timestamp(6) without time zone,
    updated_by character varying(100),
    version bigint,
    purchase_order_line_id character varying(100),
    material_id character varying(100),
    unit_price numeric(19,4)
);

CREATE TABLE public.supplier_payment_terms (
    supplier_id uuid NOT NULL,
    payment_term character varying(255)
);

CREATE TABLE public.suppliers (
    payment_delay integer,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint,
    currency_code character varying(10),
    id uuid NOT NULL,
    contact_phone character varying(20),
    postal_code character varying(20),
    status character varying(20) NOT NULL,
    code character varying(50) NOT NULL,
    city character varying(100),
    contact_person character varying(100),
    country character varying(100),
    created_by character varying(100),
    updated_by character varying(100),
    address character varying(500),
    description character varying(500),
    contact_email character varying(255),
    name character varying(255) NOT NULL,
    CONSTRAINT suppliers_currency_code_check CHECK (((currency_code)::text = ANY ((ARRAY['MAD'::character varying, 'EUR'::character varying, 'USD'::character varying])::text[])))
);

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    version bigint DEFAULT 0,
    created_by character varying(100),
    updated_by character varying(100),
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    role character varying(50) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    must_change_password boolean DEFAULT false NOT NULL,
    first_name character varying(100),
    last_name character varying(100),
    full_name character varying(200),
    phone character varying(30),
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    department character varying(100)
);

-- -------------------------------------------------------------------
-- Primary keys and unique constraints
-- -------------------------------------------------------------------

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_code_key UNIQUE (code);

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.global_code_sequences
    ADD CONSTRAINT global_code_sequences_pkey PRIMARY KEY (prefix);

ALTER TABLE ONLY public.goods_receipt_lines
    ADD CONSTRAINT goods_receipt_lines_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.goods_receipts
    ADD CONSTRAINT goods_receipts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.goods_receipts
    ADD CONSTRAINT goods_receipts_receipt_code_key UNIQUE (receipt_code);

ALTER TABLE ONLY public.invoice_lines
    ADD CONSTRAINT invoice_lines_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_invoice_code_key UNIQUE (invoice_code);

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.material_stock_movements
    ADD CONSTRAINT material_stock_movements_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.materials
    ADD CONSTRAINT materials_code_key UNIQUE (code);

ALTER TABLE ONLY public.materials
    ADD CONSTRAINT materials_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.outbox_messages
    ADD CONSTRAINT outbox_messages_event_id_key UNIQUE (event_id);

ALTER TABLE ONLY public.outbox_messages
    ADD CONSTRAINT outbox_messages_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.payment_lines
    ADD CONSTRAINT payment_lines_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_payment_code_key UNIQUE (payment_code);

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.purchase_order_lines
    ADD CONSTRAINT purchase_order_lines_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.purchase_orders
    ADD CONSTRAINT purchase_orders_order_code_key UNIQUE (order_code);

ALTER TABLE ONLY public.purchase_orders
    ADD CONSTRAINT purchase_orders_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.purchase_requisition_lines
    ADD CONSTRAINT purchase_requisition_lines_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.purchase_requisitions
    ADD CONSTRAINT purchase_requisitions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.purchase_requisitions
    ADD CONSTRAINT purchase_requisitions_requisition_code_key UNIQUE (requisition_code);

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.return_to_vendor_lines
    ADD CONSTRAINT return_to_vendor_lines_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.return_to_vendor
    ADD CONSTRAINT return_to_vendor_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.return_to_vendor
    ADD CONSTRAINT return_to_vendor_return_code_key UNIQUE (return_code);

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_code_key UNIQUE (code);

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT uk_employees_code UNIQUE (code);

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT uk_employees_email UNIQUE (email);

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT uk_password_reset_tokens_token UNIQUE (token);

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

-- -------------------------------------------------------------------
-- Indexes
-- -------------------------------------------------------------------

CREATE INDEX idx_category_parent_id ON public.categories USING btree (parent_id);

CREATE INDEX idx_category_status ON public.categories USING btree (status);

CREATE INDEX idx_category_type ON public.categories USING btree (category_type);

CREATE INDEX idx_employees_code ON public.employees USING btree (code);

CREATE INDEX idx_employees_email ON public.employees USING btree (email);

CREATE INDEX idx_employees_status ON public.employees USING btree (status);

CREATE INDEX idx_employees_user_id ON public.employees USING btree (user_id);

CREATE INDEX idx_gr_purchase_order_id ON public.goods_receipts USING btree (purchase_order_id);

CREATE INDEX idx_gr_receipt_date ON public.goods_receipts USING btree (receipt_date);

CREATE INDEX idx_gr_received_by ON public.goods_receipts USING btree (received_by);

CREATE INDEX idx_gr_status ON public.goods_receipts USING btree (status);

CREATE INDEX idx_gr_supplier_id ON public.goods_receipts USING btree (supplier_id);

CREATE INDEX idx_grl_goods_receipt_id ON public.goods_receipt_lines USING btree (goods_receipt_id);

CREATE INDEX idx_grl_line_number ON public.goods_receipt_lines USING btree (line_number);

CREATE INDEX idx_grl_material_code ON public.goods_receipt_lines USING btree (material_code);

CREATE INDEX idx_grl_purchase_order_line_id ON public.goods_receipt_lines USING btree (purchase_order_line_id);

CREATE INDEX idx_invoice_date ON public.invoices USING btree (invoice_date);

CREATE INDEX idx_invoice_lines_invoice_id ON public.invoice_lines USING btree (invoice_id);

CREATE INDEX idx_invoice_po_id ON public.invoices USING btree (purchase_order_id);

CREATE INDEX idx_invoice_status ON public.invoices USING btree (status);

CREATE INDEX idx_invoice_supplier_id ON public.invoices USING btree (supplier_id);

CREATE INDEX idx_material_category_id ON public.materials USING btree (category_id);

CREATE INDEX idx_material_status ON public.materials USING btree (status);

CREATE INDEX idx_material_supplier_id ON public.materials USING btree (supplier_id);

CREATE INDEX idx_outbox_messages_pending ON public.outbox_messages USING btree (created_at) WHERE (published_at IS NULL);

CREATE INDEX idx_payment_lines_invoice_id ON public.payment_lines USING btree (invoice_id);

CREATE INDEX idx_payment_lines_payment_id ON public.payment_lines USING btree (payment_id);

CREATE INDEX idx_payment_status ON public.payments USING btree (status);

CREATE INDEX idx_payment_supplier_id ON public.payments USING btree (supplier_id);

CREATE INDEX idx_po_delivery_status ON public.purchase_orders USING btree (delivery_status);

CREATE INDEX idx_po_expected_delivery_date ON public.purchase_orders USING btree (expected_delivery_date);

CREATE INDEX idx_po_order_date ON public.purchase_orders USING btree (order_date);

CREATE INDEX idx_po_requisition_id ON public.purchase_orders USING btree (requisition_id);

CREATE INDEX idx_po_status ON public.purchase_orders USING btree (status);

CREATE INDEX idx_po_supplier_id ON public.purchase_orders USING btree (supplier_id);

CREATE INDEX idx_pol_line_number ON public.purchase_order_lines USING btree (line_number);

CREATE INDEX idx_pol_material_code ON public.purchase_order_lines USING btree (material_code);

CREATE INDEX idx_pol_purchase_order_id ON public.purchase_order_lines USING btree (purchase_order_id);

CREATE INDEX idx_pr_approver_id ON public.purchase_requisitions USING btree (approver_id);

CREATE INDEX idx_pr_line_line_number ON public.purchase_requisition_lines USING btree (line_number);

CREATE INDEX idx_pr_line_material_code ON public.purchase_requisition_lines USING btree (material_code);

CREATE INDEX idx_pr_line_requisition_id ON public.purchase_requisition_lines USING btree (requisition_id);

CREATE INDEX idx_pr_requester_id ON public.purchase_requisitions USING btree (requester_id);

CREATE INDEX idx_pr_required_date ON public.purchase_requisitions USING btree (required_date);

CREATE INDEX idx_pr_status ON public.purchase_requisitions USING btree (status);

CREATE INDEX idx_prt_email ON public.password_reset_tokens USING btree (email);

CREATE INDEX idx_prt_token ON public.password_reset_tokens USING btree (token);

CREATE INDEX idx_prt_user_id ON public.password_reset_tokens USING btree (user_id);

CREATE INDEX idx_refresh_tokens_hash ON public.refresh_tokens USING btree (token_hash);

CREATE INDEX idx_refresh_tokens_user_id ON public.refresh_tokens USING btree (user_id);

CREATE INDEX idx_return_to_vendor_goods_receipt ON public.return_to_vendor USING btree (goods_receipt_id);

CREATE INDEX idx_return_to_vendor_lines_return ON public.return_to_vendor_lines USING btree (return_to_vendor_id);

CREATE INDEX idx_return_to_vendor_purchase_order ON public.return_to_vendor USING btree (purchase_order_id);

CREATE INDEX idx_return_to_vendor_status ON public.return_to_vendor USING btree (status);

CREATE INDEX idx_return_to_vendor_supplier ON public.return_to_vendor USING btree (supplier_id);

CREATE INDEX idx_supplier_city ON public.suppliers USING btree (city);

CREATE INDEX idx_supplier_country ON public.suppliers USING btree (country);

CREATE INDEX idx_supplier_status ON public.suppliers USING btree (status);

CREATE INDEX idx_users_email ON public.users USING btree (email);

-- -------------------------------------------------------------------
-- Foreign keys
-- -------------------------------------------------------------------

ALTER TABLE ONLY public.supplier_payment_terms
    ADD CONSTRAINT fk8kpayf27d2wsb1cu8i22d940t FOREIGN KEY (supplier_id) REFERENCES public.suppliers(id);

ALTER TABLE ONLY public.goods_receipt_lines
    ADD CONSTRAINT fk911tv4c70yqo1u58rdfa0pf5w FOREIGN KEY (goods_receipt_id) REFERENCES public.goods_receipts(id);

ALTER TABLE ONLY public.purchase_requisition_lines
    ADD CONSTRAINT fk9qdinktyun2qy11gv52akx9cx FOREIGN KEY (requisition_id) REFERENCES public.purchase_requisitions(id);

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT fk_employees_users FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.invoice_lines
    ADD CONSTRAINT fk_invoice_lines_invoice FOREIGN KEY (invoice_id) REFERENCES public.invoices(id);

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.payment_lines
    ADD CONSTRAINT fk_payment_lines_payment FOREIGN KEY (payment_id) REFERENCES public.payments(id);

ALTER TABLE ONLY public.purchase_order_lines
    ADD CONSTRAINT fk_pol_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES public.purchase_orders(id);

ALTER TABLE ONLY public.return_to_vendor_lines
    ADD CONSTRAINT fk_return_to_vendor_lines_return FOREIGN KEY (return_to_vendor_id) REFERENCES public.return_to_vendor(id);

ALTER TABLE ONLY public.material_stock_movements
    ADD CONSTRAINT fkkihr1ny42uednqq2dgv6y79v FOREIGN KEY (material_id) REFERENCES public.materials(id);

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
