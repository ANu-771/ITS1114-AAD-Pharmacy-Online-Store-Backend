# KK DIGITAL PHARMACY — ONLINE PHARMACY & MEDICAL SUPPLIES PLATFORM
## COURSEWORK TECHNICAL SPECIFICATION & BACKEND IMPLEMENTATION
**Module**: ITS1114 — Advanced API Development Module  
**Qualification**: Higher Diploma in Software Engineering (HDSE), Institute of Software Engineering (IJSE)  
**Module Weighting**: Coursework 60% + Exam 40%  
**Author**: Student Final Coursework Project  
**System Name**: KK Digital Pharmacy & Medical Equipment E-Commerce  
**Document Version**: 2.0.0 (Complete Spring Boot Backend + Frontend Integration Architecture)

---

## TABLE OF CONTENTS
1. [Executive Summary & Problem Statement](#1-executive-summary--problem-statement)
2. [Frontend Architecture & Layered Design](#2-frontend-architecture--layered-design)
3. [Healthcare UI Design System & Aesthetic Tokens](#3-healthcare-ui-design-system--aesthetic-tokens)
4. [Complete 15+ Normalized Relational Database Schema (Spring Data JPA)](#4-complete-15-normalized-relational-database-schema-spring-data-jpa)
5. [Spring Boot REST API Contract Specification](#5-spring-boot-rest-api-contract-specification)
6. [Security Architecture & JWT Authentication](#6-security-architecture--jwt-authentication)
7. [Role-Based Access Control (RBAC) Matrix](#7-role-based-access-control-rbac-matrix)
8. [Coursework Bonus Features Implemented](#8-coursework-bonus-features-implemented)
9. [Project Directory & Module Responsibilities](#9-project-directory--module-responsibilities)
10. [User Manual, Testing Guide & Demo Credentials](#10-user-manual-testing-guide--demo-credentials)

---

## 1. EXECUTIVE SUMMARY & PROBLEM STATEMENT

### 1.1 Business Problem Domain
Traditional retail pharmacies and medical supply stores face severe operational inefficiencies:
- **Manual Prescription Handling**: High risk of dispensing errors and slow fulfillment.
- **Fragmented Medical Equipment Retail**: Diagnostic instruments (e.g., blood pressure monitors, glucometers, nebulizers) are often unavailable in local community dispensaries.
- **Lack of Real-Time Stock Visibility**: Patients frequently travel to physical dispensaries only to find vital medications out of stock.
- **Absence of Centralized Order Tracking**: Disconnected fulfillment leads to missed deliveries for chronic-care patients requiring timely refills.

### 1.2 The MEDORA Digital Solution
MEDORA is a production-grade, API-first online healthcare and medical equipment marketplace. It provides:
1. **Direct Customer Storefront**: Intuitive browsing of prescription medicines, OTC drugs, diagnostic instruments, and daily vitamins.
2. **Prescription Upload & Validation**: Secure workflow for prescription-only pharmaceuticals.
3. **Real-Time Dynamic Cart & Checkout**: Automated threshold meters for free island-wide delivery and multi-method checkout (Card, Bank Transfer, COD).
4. **Role-Protected Admin Management Suite**: Real-time sales analytics, inventory tracking with batch/expiry monitoring, user role management, and order fulfillment workflows.
5. **AI Healthcare Assistant (Bonus Feature)**: Interactive clinical dosage advisor and OTC guidance assistant.

---

## 2. FRONTEND ARCHITECTURE & LAYERED DESIGN

The frontend strictly conforms to a **layered separation of concerns**, completely isolating presentation code from network requests and business rules.

```
Presentation Layer (HTML5 / Pages / Admin)
    ↓
Component Layer (navbar.js, product-card.js, modal.js, toast.js)
    ↓
Service Layer (auth-service.js, cart-service.js, product-service.js, order-service.js)
    ↓
API Client Layer (api-client.js, auth-api.js, product-api.js, order-api.js)
    ↓
Spring Boot REST API (Spring Security, JWT, JPA/Hibernate, MySQL)
```

### 2.1 Universal Relative Asset Resolution & Resilient Cart Synchronization
- **Asset Resolution Engine (`resolveImagePath`)**: Seamlessly normalizes image URIs across root (`/index.html`) and nested subdirectories (`/pages/`, `/admin/`) to guarantee zero 404 image errors with automatic fallback handling.
- **Cart Lifecycle & Re-ordering**: Following successful order checkout, the active cart clears its state while preserving real-time re-synchronization with `localStorage`, enabling users to seamlessly browse and add new items without page refresh or stale session conflicts.

### Architectural Layer Responsibilities:
- **Layer 1 — Presentation & Pages (`pages/`, `admin/`, `index.html`)**: Defines semantic DOM structures, accessibility markers, and forms without hardcoded API calls.
- **Layer 2 — Reusable UI Components (`js/components/`)**: Pure UI rendering functions (`renderProductCard`, `renderNavbar`, `Toast.show`, `Modal.showQuickView`, `LoadingState`).
- **Layer 3 — Business & State Services (`js/services/`)**: Encapsulates client-side domain rules (cart calculations, currency formatting, JWT token decode, local session caching, mock fallback toggle).
- **Layer 4 — HTTP API Clients (`js/api/`)**: Central `APIClient` using standard Fetch API, handling Bearer header injection, response mapping, and HTTP error normalization (401, 403, 404, 500).
- **Layer 5 — Backend REST API (Spring Boot)**: Spring Security, BCrypt, JPA/Hibernate entities, SLF4J logging, and MySQL persistence.

---

## 3. HEALTHCARE UI DESIGN SYSTEM & AESTHETIC TOKENS

The interface uses a tailored, high-trust healthcare color palette strictly conforming to medical industry standards.

| Token Name | Hex Code | Semantic Purpose |
| :--- | :--- | :--- |
| `--primary-blue` | `#0066B3` | Primary brand accent, primary CTA buttons, active links |
| `--primary-hover` | `#005290` | Interactive hover state for primary elements |
| `--dark-navy` | `#003B66` | Typography headings (`h1`–`h6`), brand logo, navbar dark text |
| `--secondary-blue` | `#2D9CDB` | Informational highlights, secondary badges, timeline steps |
| `--light-medical-blue` | `#E8F4FC` | Healthcare banners, card backgrounds, hover fills |
| `--soft-bg` | `#F3F9FD` | Global body background (reduces eye strain) |
| `--white` | `#FFFFFF` | Content surfaces, modals, dropdowns, form controls |
| `--text-main` | `#0F172A` | Primary body typography (Slate 900) |
| `--text-muted` | `#475569` | Secondary captions, dosage instructions (Slate 600) |
| `--border-color` | `#D9E8F2` | Subtle healthcare borders |
| `--success` | `#16A34A` | In-stock tags, verified Rx badges, checkout confirmations |
| `--warning` | `#F59E0B` | Low-stock warnings, prescription-required notices |
| `--error` | `#DC2626` | Out-of-stock tags, invalid input alerts, 403/500 errors |

### Typography & Spacing:
- **Headings**: `Outfit`, sans-serif (Weights: 600, 700)
- **Body**: `Inter`, system-ui, sans-serif (Weights: 400, 500, 600)
- **Grid System**: Bootstrap 5 Responsive Grid (8px baseline scale)
- **Corner Radius**: `8px` (inputs/buttons), `14px` (cards/banners), `50px` (pills/badges)

---

## 4. COMPLETE 15+ NORMALIZED RELATIONAL DATABASE SCHEMA (SPRING DATA JPA)

To fulfill and exceed the coursework mandate (minimum 15 normalized tables), the database is normalized to 3rd Normal Form (3NF) comprising 18 relational entities.

```
+-------------------------------------------------------------------------------+
|                       DATABASE RELATIONAL SCHEMA (MySQL 8+)                   |
+-------------------------------------------------------------------------------+
 1. users               (id, email, password, full_name, phone, enabled, created_at)
 2. roles               (id, name [ROLE_GUEST, ROLE_USER, ROLE_ADMIN, ROLE_PHARMACIST])
 3. user_roles          (user_id, role_id) [Composite PK]
 4. user_addresses      (id, user_id, address_line1, city, postal_code, is_default)
 5. categories          (id, name, slug, description, icon_class, active)
 6. brands              (id, name, manufacturer_country, verified)
 7. products            (id, category_id, brand_id, name, sku, price, old_price, rx_required)
 8. medicine_details    (id, product_id, active_ingredient, dosage_form, strength, storage_info)
 9. product_images      (id, product_id, image_url, is_primary, display_order)
10. inventory           (id, product_id, current_stock, reorder_level, location_aisle)
11. inventory_batches   (id, inventory_id, batch_number, mfg_date, expiry_date, quantity)
12. cart_items          (id, user_id, product_id, quantity, updated_at)
13. orders              (id, user_id, order_number, total_amount, discount, status, created_at)
14. order_items         (id, order_id, product_id, unit_price, quantity, subtotal)
15. payments            (id, order_id, payment_method, transaction_ref, status, amount)
16. prescriptions       (id, user_id, order_id, doctor_name, prescription_url, status)
17. reviews             (id, user_id, product_id, rating, comment, is_verified_purchase)
18. wishlists           (id, user_id, product_id, created_at)
```

---

## 5. SPRING BOOT REST API CONTRACT SPECIFICATION

All endpoints return standard JSON payloads with consistent HTTP status codes:
- `200 OK` / `201 Created`: Success
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Missing or invalid JWT
- `403 Forbidden`: Insufficient role authority
- `404 Not Found`: Entity not found
- `500 Internal Server Error`: Unhandled server exception

### 5.1 Authentication API (`/api/v1/auth`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Authenticate user & issue JWT Bearer token | Public |
| `POST` | `/api/v1/auth/register` | Register new customer account | Public |
| `POST` | `/api/v1/auth/refresh-token`| Renew expiring JWT token | Authenticated |
| `POST` | `/api/v1/auth/logout` | Invalidate active server session | Authenticated |

### 5.2 Products & Catalog API (`/api/v1/products`)
| Method | Endpoint | Query Params | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | `category`, `search`, `minPrice`, `maxPrice`, `sort`, `page` | Get paginated products |
| `GET` | `/api/v1/products/{id}`| - | Get detailed product by ID |
| `POST` | `/api/v1/products` | - | Create new product (Admin only) |
| `PUT` | `/api/v1/products/{id}`| - | Update product details (Admin only) |
| `DELETE` | `/api/v1/products/{id}`| - | Soft delete product (Admin only) |

### 5.3 Cart & Order API (`/api/v1/orders`, `/api/v1/cart`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/cart` | Get current user's shopping cart | `ROLE_USER` |
| `POST` | `/api/v1/cart/items` | Add product to cart | `ROLE_USER` |
| `DELETE` | `/api/v1/cart/items/{id}`| Remove product from cart | `ROLE_USER` |
| `POST` | `/api/v1/orders` | Place new order with address & payment | `ROLE_USER` |
| `GET` | `/api/v1/orders/my-orders`| Retrieve user's order history | `ROLE_USER` |
| `GET` | `/api/v1/orders/{id}` | Get itemized invoice & tracking | `ROLE_USER` / `ROLE_ADMIN` |
| `PATCH` | `/api/v1/orders/{id}/status`| Update order fulfillment status | `ROLE_ADMIN` |

### 5.4 Admin & Inventory API (`/api/v1/admin`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/admin/dashboard-stats` | KPI metrics (Revenue, Orders, Low stock) | `ROLE_ADMIN` |
| `GET` | `/api/v1/admin/inventory/low-stock` | Low stock items beneath reorder level | `ROLE_ADMIN` |
| `POST` | `/api/v1/admin/inventory/batch` | Record incoming batch with expiry | `ROLE_ADMIN` |
| `GET` | `/api/v1/admin/reports/sales` | Generate sales aggregations for Jasper | `ROLE_ADMIN` |

---

## 6. SECURITY ARCHITECTURE & JWT AUTHENTICATION

1. **Password Hashing**: Spring Security utilizes `BCryptPasswordEncoder(12)` on all passwords.
2. **Stateless JWT Tokens**: Tokens are generated on login with an HMAC-SHA256 signature containing `sub` (email), `roles` (authorities), `iat`, and `exp` claims.
3. **Frontend Bearer Injection**: The central `APIClient` reads the token from safe storage and automatically attaches `Authorization: Bearer <token>` to all HTTP requests.
4. **Graceful Expired Session Handling**: Any `401 Unauthorized` response triggers an automatic purge of the token and dispatches an `auth:unauthorized` event to prompt the user to re-authenticate without crashing the UI.

---

## 7. ROLE-BASED ACCESS CONTROL (RBAC) MATRIX

| Feature / Page | GUEST | USER (Customer) | ADMIN |
| :--- | :---: | :---: | :---: |
| Browse Products & Categories | Yes | Yes | Yes |
| View Product Details & Dosage Info | Yes | Yes | Yes |
| Add to Cart (Local Storage) | Yes | Yes | Yes |
| Proceed to Checkout & Place Order | No (Prompted) | Yes | Yes |
| View Order History & Invoices | No | Yes | Yes |
| Manage Profile & Saved Addresses | No | Yes | Yes |
| Access Admin Dashboard & KPIs | No (403) | No (403) | Yes |
| Manage Products & Categories (CRUD) | No (403) | No (403) | Yes |
| Update Order Fulfillment Status | No (403) | No (403) | Yes |
| View Inventory Batches & Expiries | No (403) | No (403) | Yes |
| Export Sales & Jasper Reports | No (403) | No (403) | Yes |

---

## 8. COURSEWORK BONUS FEATURES IMPLEMENTED

Beyond the core mandatory rubric, MEDORA incorporates high-scoring bonus features:
1. **AI Health Assistant & Dosage Advisor Widget**: An interactive clinical chatbot widget (`chat-assistant.js`) capable of answering OTC medicine inquiries, explaining dosage instructions, recommending first-aid products, and warning patients when a doctor visit is mandatory.
2. **Visual Analytics & Revenue Dashboard**: Interactive sales graphs and category split charts built using Canvas on the admin dashboard.
3. **Prescription Upload & Zoom Inspection**: Interactive image preview with zoom modal allowing pharmacists to inspect doctors' signatures and prescriptions.
4. **Printable Invoice / Jasper Simulator**: Clean, printable itemized receipt generator with QR code and delivery tracking timeline.
5. **Real-time Free Shipping Threshold Meter**: Interactive progress bar in the cart indicating how much more needs to be added to unlock free island-wide delivery.

---

## 9. PROJECT DIRECTORY & MODULE RESPONSIBILITIES

```
pharmacy-frontend/
│
├── index.html                   # Complete 12-section homepage
├── DOCUMENTATION.md             # Master coursework specification (This document)
│
├── pages/                       # Customer Storefront Pages
│   ├── products.html            # Product catalog with filter sidebar
│   ├── product-details.html     # Medicine/instrument single view
│   ├── categories.html          # Browse all categories
│   ├── cart.html                # Cart management & free delivery meter
│   ├── checkout.html            # Checkout & payment forms
│   ├── login.html               # Sign-in page with quick demo buttons
│   ├── register.html            # User registration form
│   ├── profile.html             # Profile & address management
│   ├── orders.html              # Customer order history
│   ├── order-details.html       # Itemized receipt & delivery status
│   ├── wishlist.html            # Saved products
│   ├── about.html               # Pharmacy licensing & medical team
│   ├── contact.html             # Customer support & branch map
│   └── faq.html                 # Prescription & delivery FAQ
│
├── admin/                       # Admin Portal (Role Protected)
│   ├── dashboard.html           # Sales KPIs, charts, alerts
│   ├── products.html            # Product CRUD with modal forms
│   ├── categories.html          # Category CRUD
│   ├── orders.html              # Order management & status updater
│   ├── users.html               # User accounts & role manager
│   ├── inventory.html           # Batch tracking & expiry alerts
│   └── reports.html             # Jasper reports & analytics
│
├── css/
│   ├── style.css                # Brand colors, typography, buttons
│   ├── components.css           # Cards, modals, toasts, skeletons
│   ├── responsive.css           # Breakpoints (320px, 768px, 1200px, 1440px)
│   └── admin.css                # Admin sidebar, tables, stat cards
│
├── js/
│   ├── app.js                   # Application bootstrapper
│   ├── config.js                # API URLs & storage keys
│   ├── api/                     # Layer 1: HTTP API Clients
│   ├── services/                # Layer 2: Domain Services
│   ├── components/              # Layer 3: Reusable UI Components
│   └── pages/                   # Layer 4: Page Controllers
│
└── assets/images/               # High Quality Photographic Assets
```

---

## 10. USER MANUAL, TESTING GUIDE & DEMO CREDENTIALS

### 10.1 Quick Demo Login Credentials
For instant coursework demonstration and evaluation:

| Role | Email | Password | Access Rights |
| :--- | :--- | :--- | :--- |
| **Customer User** | `user@example.com` | `password123` | Storefront, Cart, Checkout, Orders, Profile |
| **Administrator** | `admin@kkdigitalpharmacy.com` | `admin123` | All Storefront + Full Admin Portal (`admin/`) |
| **Pharmacist** | `pharmacist@kkdigitalpharmacy.com` | `pharma123` | Inventory, Prescriptions, Orders |

### 10.2 Switching between Mock Mode and Live Spring Boot API
In `js/config.js`:
- To run with built-in realistic mock data:
  ```javascript
  USE_MOCK_DATA: true
  ```
- To connect to a live running Spring Boot REST API:
  ```javascript
  USE_MOCK_DATA: false,
  API_BASE_URL: 'http://localhost:8080/api/v1'
  ```

---

## 11. PHARMACEUTICAL CATALOG REGISTRY (INCREMENT 1.1)

The system now hosts an expanded catalog of 14 certified healthcare products across 4 core departments:

| ID | Product Name | Brand | Department | Retail Price | Key Specification |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Amoxicillin 500mg Antibiotic Capsules | GlaxoSmithKline | Medicines (Rx) | Rs. 650.00 | Amoxicillin Trihydrate 500mg (Rx Required) |
| **2** | Digital BP Upper Arm Monitor | Omron Healthcare | Medical Equipment | Rs. 14,850.00 | IntelliWrap Sensor & 60 Memory slots |
| **3** | Daily Multivitamin & Immunobooster 60s | Seven Seas | Vitamins | Rs. 3,200.00 | Multivitamins A, B, C, D3, E, Zinc, Iron |
| **4** | Instant Fingertip Pulse Oximeter OLED | Beurer | Medical Equipment | Rs. 4,950.00 | SpO2 Arterial Blood Oxygen & Pulse Rate |
| **5** | Accu-Chek Instant Blood Glucose Kit | Roche Diagnostics | Medical Equipment | Rs. 8,900.00 | 4-Second Test Time & Softclix Lancing Pen |
| **6** | Paracetamol Extra Strength 500mg (100s)| Haleon / Panadol | Medicines (OTC) | Rs. 480.00 | Paracetamol BP 500mg Pain & Fever Relief |
| **7** | Ultrasonic Compressor Nebulizer | Philips Respironics | Medical Equipment | Rs. 12,500.00 | Piston Compressor Aerosol Therapy 0.35ml/min |
| **8** | Non-Contact Forehead Thermometer | Microlife | Medical Equipment | Rs. 6,200.00 | 1-Second Infrared Fever Scanner (±0.2°C) |
| **9** | Gentle Baby Wash & Tear-Free Shampoo | Gentle Sprout / Sebamed | Baby Care | Rs. 2,450.00 | Hypoallergenic, pH 5.5, Chamomile & Panthenol |
| **10**| Baby Healing Diaper Rash Barrier Cream | Nurture Baby / Sudocrem| Baby Care | Rs. 1,850.00 | Zinc Oxide 15.25%, Calendula, Panthenol |
| **11**| Infant Natural Colic Relief Gripe Water| Tummy Calm | Baby Care | Rs. 780.00 | Dill Seed Oil & Sodium Bicarbonate (Alcohol-Free) |
| **12**| Effervescent Vitamin C 1000mg + Zinc | Vita-Immune / Redoxon | Vitamins | Rs. 1,950.00 | Fast-Dissolving Orange Effervescent (20 Tabs) |
| **13**| Omega-3 Triple Strength Fish Oil 1000mg| Vita-Premium | Vitamins | Rs. 4,600.00 | Concentrated EPA 360mg / DHA 240mg (90 Softgels) |
| **14**| Calcium Carbonate + Vitamin D3 60s | Vita-Premium | Vitamins | Rs. 2,800.00 | Calcium 600mg + Vit D3 400IU Bone Density |

---
*Document maintained automatically with each build increment.*
