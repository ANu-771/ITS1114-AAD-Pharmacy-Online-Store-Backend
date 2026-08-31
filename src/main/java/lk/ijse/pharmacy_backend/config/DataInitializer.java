package lk.ijse.pharmacy_backend.config;

import lk.ijse.pharmacy_backend.dto.product.ProductRequestDTO;
import lk.ijse.pharmacy_backend.entity.Brand;
import lk.ijse.pharmacy_backend.entity.Category;
import lk.ijse.pharmacy_backend.entity.Role;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.enumiration.UserRole;
import lk.ijse.pharmacy_backend.repository.BrandRepository;
import lk.ijse.pharmacy_backend.repository.CategoryRepository;
import lk.ijse.pharmacy_backend.repository.RoleRepository;
import lk.ijse.pharmacy_backend.repository.UserRepository;
import lk.ijse.pharmacy_backend.service.InventoryService;
import lk.ijse.pharmacy_backend.service.ProductService;
import lk.ijse.pharmacy_backend.service.UserAddressService;
import lk.ijse.pharmacy_backend.dto.inventory.AddBatchRequest;
import lk.ijse.pharmacy_backend.dto.user.UserAddressDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final UserAddressService userAddressService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking and initializing KK Digital Pharmacy database seeds...");
        seedRoles();
        seedUsers();
        seedCategories();
        seedBrands();
        seedProductsAndInventory();
        log.info("Database seeding complete for KK Digital Pharmacy!");
    }

    private void seedRoles() {
        for (UserRole roleName : UserRole.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    private void seedUsers() {
        Role adminRole = roleRepository.findByName(UserRole.ROLE_ADMIN).orElseThrow();
        Role userRole = roleRepository.findByName(UserRole.ROLE_USER).orElseThrow();
        Role pharmacistRole = roleRepository.findByName(UserRole.ROLE_PHARMACIST).orElseThrow();

        // 1. Admin account
        if (userRepository.findByEmail("admin@kkdigitalpharmacy.com").isEmpty() &&
                userRepository.findByEmail("admin@medora.com").isEmpty()) {
            User admin = User.builder()
                    .email("admin@kkdigitalpharmacy.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("KK Pharmacy System Administrator")
                    .phone("+94 11 234 5678")
                    .enabled(true)
                    .roles(new HashSet<>(Arrays.asList(adminRole, userRole)))
                    .build();
            userRepository.save(admin);
            log.info("Seeded Admin: admin@kkdigitalpharmacy.com / admin123");
        }

        // 2. Pharmacist account
        if (userRepository.findByEmail("pharmacist@kkdigitalpharmacy.com").isEmpty() &&
                userRepository.findByEmail("pharmacist@medora.com").isEmpty()) {
            User pharmacist = User.builder()
                    .email("pharmacist@kkdigitalpharmacy.com")
                    .password(passwordEncoder.encode("pharma123"))
                    .fullName("Dr. Sarah Perera, Chief Pharmacist")
                    .phone("+94 77 987 6543")
                    .enabled(true)
                    .roles(new HashSet<>(Arrays.asList(pharmacistRole, userRole)))
                    .build();
            userRepository.save(pharmacist);
            log.info("Seeded Pharmacist: pharmacist@kkdigitalpharmacy.com / pharma123");
        }

        // 3. Customer account
        if (userRepository.findByEmail("user@example.com").isEmpty()) {
            User customer = User.builder()
                    .email("user@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Sarah Perera")
                    .phone("+94 77 123 4567")
                    .enabled(true)
                    .roles(new HashSet<>(Collections.singletonList(userRole)))
                    .build();
            userRepository.save(customer);
            log.info("Seeded Customer: user@example.com / password123");

            // Seed default address for demo customer
            userAddressService.addAddress("user@example.com", UserAddressDTO.builder()
                    .recipientName("Sarah Perera")
                    .phone("+94 77 123 4567")
                    .addressLine1("No. 45, Galle Road")
                    .addressLine2("Apartment 4B")
                    .city("Colombo 03")
                    .state("Western Province")
                    .postalCode("00300")
                    .isDefault(true)
                    .build());
        }
    }

    private void seedCategories() {
        createCategoryIfAbsent("Medicines", "medicines", "Over-the-counter & essential remedies", "bi-capsule");
        createCategoryIfAbsent("Prescription Medicines", "prescription", "Rx medicines requiring verified doctor prescription", "bi-file-earmark-medical");
        createCategoryIfAbsent("Medical Equipment", "equipment", "Clinical diagnostic instruments & health monitoring devices", "bi-heart-pulse");
        createCategoryIfAbsent("Vitamins & Supplements", "vitamins", "Multivitamins, minerals & immune boosters", "bi-lightning-charge");
        createCategoryIfAbsent("Personal Care", "personal-care", "Skincare, oral care & daily hygiene", "bi-droplet-half");
        createCategoryIfAbsent("Baby & Mother Care", "baby-care", "Baby nutrition, gentle skincare & formula", "bi-emoji-smile");
        createCategoryIfAbsent("First Aid", "first-aid", "Bandages, antiseptics, kits & wound care", "bi-bandaid");
    }

    private void createCategoryIfAbsent(String name, String slug, String desc, String icon) {
        if (!categoryRepository.existsByName(name) && categoryRepository.findBySlug(slug).isEmpty()) {
            categoryRepository.save(Category.builder()
                    .name(name)
                    .slug(slug)
                    .description(desc)
                    .iconClass(icon)
                    .active(true)
                    .build());
        }
    }

    private void seedBrands() {
        createBrandIfAbsent("GlaxoSmithKline", "United Kingdom");
        createBrandIfAbsent("Omron Healthcare", "Japan");
        createBrandIfAbsent("Seven Seas", "United Kingdom");
        createBrandIfAbsent("Beurer", "Germany");
        createBrandIfAbsent("Roche Diagnostics", "Switzerland");
        createBrandIfAbsent("Panadol", "United Kingdom");
        createBrandIfAbsent("Abbott Laboratories", "United States");
        createBrandIfAbsent("Johnson & Johnson", "United States");
        createBrandIfAbsent("Dettol", "United Kingdom");
        createBrandIfAbsent("Pfizer", "United States");
    }

    private void createBrandIfAbsent(String name, String country) {
        if (!brandRepository.existsByName(name)) {
            brandRepository.save(Brand.builder()
                    .name(name)
                    .manufacturerCountry(country)
                    .verified(true)
                    .build());
        }
    }

    private void seedProductsAndInventory() {
        if (productService.getProducts(null, null, null, null, "newest", 0, 1).getTotalElements() > 0) {
            return; // already seeded
        }

        Category medicines = categoryRepository.findBySlug("medicines").orElse(null);
        Category prescription = categoryRepository.findBySlug("prescription").orElse(null);
        Category equipment = categoryRepository.findBySlug("equipment").orElse(null);
        Category vitamins = categoryRepository.findBySlug("vitamins").orElse(null);
        Category babyCare = categoryRepository.findBySlug("baby-care").orElse(null);
        Category firstAid = categoryRepository.findBySlug("first-aid").orElse(null);

        Brand gsk = brandRepository.findByName("GlaxoSmithKline").orElse(null);
        Brand omron = brandRepository.findByName("Omron Healthcare").orElse(null);
        Brand sevenSeas = brandRepository.findByName("Seven Seas").orElse(null);
        Brand beurer = brandRepository.findByName("Beurer").orElse(null);
        Brand roche = brandRepository.findByName("Roche Diagnostics").orElse(null);
        Brand panadol = brandRepository.findByName("Panadol").orElse(null);
        Brand abbott = brandRepository.findByName("Abbott Laboratories").orElse(null);
        Brand jnj = brandRepository.findByName("Johnson & Johnson").orElse(null);
        Brand dettol = brandRepository.findByName("Dettol").orElse(null);

        // Product 1: Amoxicillin 500mg (Rx)
        var p1 = productService.createProduct(ProductRequestDTO.builder()
                .name("Amoxicillin 500mg Antibiotic Capsules")
                .sku("RX-AMX-500")
                .categoryId(prescription != null ? prescription.getId() : medicines.getId())
                .brandId(gsk.getId())
                .price(new BigDecimal("650.00"))
                .oldPrice(new BigDecimal("750.00"))
                .rxRequired(true)
                .badge("Popular")
                .description("Amoxicillin is a broad-spectrum penicillin-class antibiotic used to treat bacterial infections including respiratory tract infections, ear infections, and skin infections.")
                .image("assets/images/medicine_1.png")
                .activeIngredient("Amoxicillin Trihydrate 500mg")
                .dosageForm("Hard Gelatin Capsules (30s Blister Pack)")
                .strength("500mg per capsule")
                .manufacturer("GlaxoSmithKline Pharmaceuticals Ltd")
                .storageInfo("Store below 25°C in a dry place away from direct sunlight.")
                .initialStock(45)
                .reorderLevel(20)
                .locationAisle("Pharmacy Shelf A-1")
                .build());

        inventoryService.addBatch(AddBatchRequest.builder()
                .productId(p1.getId())
                .batchNumber("AMX24G01")
                .manufacturingDate(LocalDate.of(2025, 1, 10))
                .expiryDate(LocalDate.of(2027, 9, 30))
                .quantity(45)
                .build());

        // Product 2: Digital BP Monitor
        var p2 = productService.createProduct(ProductRequestDTO.builder()
                .name("Digital Blood Pressure Upper Arm Monitor")
                .sku("EQ-OMR-BP01")
                .categoryId(equipment.getId())
                .brandId(omron.getId())
                .price(new BigDecimal("14850.00"))
                .oldPrice(new BigDecimal("16500.00"))
                .rxRequired(false)
                .badge("Top Rated")
                .description("Clinically validated accurate digital blood pressure monitor with IntelliWrap cuff technology, irregular heartbeat detection, and 60-reading memory storage.")
                .image("assets/images/bp_monitor.png")
                .activeIngredient("N/A (Diagnostic Medical Device)")
                .dosageForm("Upper Arm Cuff & Electronic Console Unit")
                .strength("Oscillometric Sensor Tech")
                .manufacturer("Omron Healthcare Co., Ltd. (Japan)")
                .storageInfo("Store in protective pouch at room temperature.")
                .initialStock(12)
                .reorderLevel(10)
                .locationAisle("Equipment Section B-2")
                .build());

        inventoryService.addBatch(AddBatchRequest.builder()
                .productId(p2.getId())
                .batchNumber("OMR25-11")
                .manufacturingDate(LocalDate.of(2025, 3, 1))
                .expiryDate(LocalDate.of(2028, 12, 31))
                .quantity(12)
                .build());

        // Product 3: Daily Multivitamin
        var p3 = productService.createProduct(ProductRequestDTO.builder()
                .name("Daily Multivitamin & Immunobooster 60s")
                .sku("VT-7S-60")
                .categoryId(vitamins.getId())
                .brandId(sevenSeas.getId())
                .price(new BigDecimal("3200.00"))
                .oldPrice(new BigDecimal("3800.00"))
                .rxRequired(false)
                .badge("Best Value")
                .description("Comprehensive daily multivitamin formula packed with Vitamin C, Vitamin D3, Zinc, B-Complex, and essential minerals to support vitality and immune health.")
                .image("assets/images/vitamins_1.png")
                .activeIngredient("Multivitamins A, B-Complex, C, D3, E, Zinc, Iron")
                .dosageForm("Amber Glass Bottle (60 Softgels)")
                .strength("100% Daily Value RDA")
                .manufacturer("Seven Seas Ltd (UK)")
                .storageInfo("Keep tightly sealed in a cool, dry place.")
                .initialStock(65)
                .reorderLevel(15)
                .locationAisle("Wellness Shelf C-1")
                .build());

        inventoryService.addBatch(AddBatchRequest.builder()
                .productId(p3.getId())
                .batchNumber("VT99B4")
                .manufacturingDate(LocalDate.of(2025, 2, 15))
                .expiryDate(LocalDate.of(2027, 4, 15))
                .quantity(65)
                .build());

        // Product 4: Pulse Oximeter
        var p4 = productService.createProduct(ProductRequestDTO.builder()
                .name("Instant Fingertip Pulse Oximeter OLED")
                .sku("EQ-OXI-02")
                .categoryId(equipment.getId())
                .brandId(beurer.getId())
                .price(new BigDecimal("4950.00"))
                .oldPrice(new BigDecimal("5900.00"))
                .rxRequired(false)
                .badge("Essential")
                .description("Non-invasive medical fingertip pulse oximeter that accurately measures arterial blood oxygen saturation (SpO2) and heart pulse rate in 5 seconds.")
                .image("assets/images/oximeter.png")
                .activeIngredient("N/A (Optical Diagnostic Instrument)")
                .dosageForm("Fingertip Device with Lanyard & Case")
                .strength("High Precision Photoelectric Sensor")
                .manufacturer("Beurer GmbH (Germany)")
                .storageInfo("Keep clean and dry.")
                .initialStock(18)
                .reorderLevel(8)
                .locationAisle("Equipment Section B-1")
                .build());

        inventoryService.addBatch(AddBatchRequest.builder()
                .productId(p4.getId())
                .batchNumber("OX25-05")
                .manufacturingDate(LocalDate.of(2025, 4, 1))
                .expiryDate(LocalDate.of(2028, 6, 30))
                .quantity(18)
                .build());

        // Product 5: Accu-Chek Glucose Kit
        var p5 = productService.createProduct(ProductRequestDTO.builder()
                .name("Accu-Chek Instant Blood Glucose Meter Kit")
                .sku("EQ-GLU-01")
                .categoryId(equipment.getId())
                .brandId(roche.getId())
                .price(new BigDecimal("8400.00"))
                .oldPrice(new BigDecimal("9500.00"))
                .rxRequired(false)
                .badge("Diabetes Care")
                .description("Effortless blood glucose monitoring system featuring target range indicator, memory for 720 readings, and test strips included.")
                .image("assets/images/glucose_meter.png")
                .activeIngredient("N/A (Biosensor Diagnostic Kit)")
                .dosageForm("Meter Kit with 25 Strips & FastClix Lancing Device")
                .strength("Electrochemical Biosensor")
                .manufacturer("Roche Diabetes Care GmbH (Switzerland)")
                .storageInfo("Store strips at 4°C to 30°C.")
                .initialStock(24)
                .reorderLevel(10)
                .locationAisle("Equipment Section B-3")
                .build());

        inventoryService.addBatch(AddBatchRequest.builder()
                .productId(p5.getId())
                .batchNumber("GLU25-88")
                .manufacturingDate(LocalDate.of(2025, 5, 10))
                .expiryDate(LocalDate.of(2027, 8, 20))
                .quantity(24)
                .build());

        // Product 6: Paracetamol 500mg
        var p6 = productService.createProduct(ProductRequestDTO.builder()
                .name("Paracetamol Extra Strength 500mg Tablets")
                .sku("OTC-PCM-100")
                .categoryId(medicines.getId())
                .brandId(panadol.getId())
                .price(new BigDecimal("480.00"))
                .oldPrice(new BigDecimal("550.00"))
                .rxRequired(false)
                .badge("Pain Relief")
                .description("Effective, fast-acting relief for headaches, dental pain, fever, muscle aches, and colds. Gentle on sensitive stomachs.")
                .image("assets/images/paracetamol.png")
                .activeIngredient("Paracetamol (Acetaminophen) 500mg")
                .dosageForm("Film-coated caplets (100s box)")
                .strength("500mg per tablet")
                .manufacturer("GlaxoSmithKline Consumer Healthcare")
                .storageInfo("Store below 30°C in original packaging.")
                .initialStock(150)
                .reorderLevel(30)
                .locationAisle("OTC Shelf A-3")
                .build());

        inventoryService.addBatch(AddBatchRequest.builder()
                .productId(p6.getId())
                .batchNumber("PCM25A04")
                .manufacturingDate(LocalDate.of(2025, 3, 20))
                .expiryDate(LocalDate.of(2027, 11, 15))
                .quantity(150)
                .build());

        // Product 7: Silent Mesh Nebulizer
        productService.createProduct(ProductRequestDTO.builder()
                .name("Ultrasonic Silent Mesh Nebulizer Machine")
                .sku("EQ-NEB-09")
                .categoryId(equipment.getId())
                .brandId(beurer.getId())
                .price(new BigDecimal("12500.00"))
                .oldPrice(new BigDecimal("14200.00"))
                .rxRequired(false)
                .badge("Respiratory")
                .description("Portable handheld ultrasonic mesh inhaler for asthma, bronchitis, and respiratory treatments. Whisper-quiet operation suitable for children and adults.")
                .image("assets/images/nebulizer.png")
                .activeIngredient("N/A (Respiratory Medical Device)")
                .dosageForm("Handheld Inhaler with Adult & Child Masks")
                .strength("Vibrating Mesh Particle size < 5µm")
                .manufacturer("Beurer Medical (Germany)")
                .storageInfo("Disinfect medication chamber after each use.")
                .initialStock(10)
                .reorderLevel(5)
                .locationAisle("Equipment Section B-4")
                .build());

        // Product 8: Infrared Thermometer
        productService.createProduct(ProductRequestDTO.builder()
                .name("Non-Contact Infrared Forehead Thermometer")
                .sku("EQ-THM-03")
                .categoryId(equipment.getId())
                .brandId(omron.getId())
                .price(new BigDecimal("6750.00"))
                .oldPrice(new BigDecimal("7800.00"))
                .rxRequired(false)
                .badge("Diagnostic")
                .description("Fast 1-second temperature measurement with high accuracy, fever alarm backlight color alerts, and 32-memory capacity.")
                .image("assets/images/thermometer.png")
                .activeIngredient("N/A (Infrared Thermal Sensor)")
                .dosageForm("Ergonomic Gun Device with LCD Display")
                .strength("0.1°C Resolution")
                .manufacturer("Omron Healthcare (Japan)")
                .storageInfo("Keep lens clean.")
                .initialStock(30)
                .reorderLevel(10)
                .locationAisle("Equipment Section B-2")
                .build());

        // Product 9: Gentle Baby Wash
        productService.createProduct(ProductRequestDTO.builder()
                .name("Gentle Nourishing Baby Head-to-Toe Wash 500ml")
                .sku("BC-JNJ-500")
                .categoryId(babyCare.getId())
                .brandId(jnj.getId())
                .price(new BigDecimal("1850.00"))
                .oldPrice(new BigDecimal("2100.00"))
                .rxRequired(false)
                .badge("Pediatric Care")
                .description("Hypoallergenic, tear-free baby wash enriched with natural oat extracts to gently cleanse and moisturize delicate newborn skin.")
                .image("assets/images/baby_wash.png")
                .activeIngredient("Colloidal Oatmeal, Glycerin, Provitamin B5")
                .dosageForm("Pump Dispenser Bottle (500ml)")
                .strength("pH Balanced Formula")
                .manufacturer("Johnson & Johnson Consumer Inc.")
                .storageInfo("Keep at room temperature.")
                .initialStock(40)
                .reorderLevel(15)
                .locationAisle("Baby Section D-1")
                .build());

        // Product 10: Diaper Rash Cream
        productService.createProduct(ProductRequestDTO.builder()
                .name("Calming Diaper Rash Protective Cream 100g")
                .sku("BC-DRC-100")
                .categoryId(babyCare.getId())
                .brandId(jnj.getId())
                .price(new BigDecimal("1200.00"))
                .oldPrice(new BigDecimal("1450.00"))
                .rxRequired(false)
                .badge("Baby Care")
                .description("Zinc Oxide barrier cream providing instant soothing relief and long-lasting protection against diaper rash and skin irritation.")
                .image("assets/images/rash_cream.png")
                .activeIngredient("Zinc Oxide 15%, Chamomile & Aloe Extracts")
                .dosageForm("Topical Ointment Tube (100g)")
                .strength("15% Active Barrier")
                .manufacturer("Johnson & Johnson Consumer Inc.")
                .storageInfo("Store below 25°C.")
                .initialStock(35)
                .reorderLevel(10)
                .locationAisle("Baby Section D-2")
                .build());

        // Product 11: Vitamin C + Zinc
        productService.createProduct(ProductRequestDTO.builder()
                .name("High Potency Vitamin C 1000mg + Zinc Effervescent 20s")
                .sku("VT-VCZ-20")
                .categoryId(vitamins.getId())
                .brandId(abbott.getId())
                .price(new BigDecimal("1950.00"))
                .oldPrice(new BigDecimal("2300.00"))
                .rxRequired(false)
                .badge("Immune Defense")
                .description("Delicious orange-flavored effervescent tablets delivering 1000mg Vitamin C plus 10mg Zinc for superior antioxidant protection and recovery.")
                .image("assets/images/vitamins_1.png")
                .activeIngredient("Ascorbic Acid (Vitamin C) 1000mg, Zinc Citrate 10mg")
                .dosageForm("Effervescent Tube (20 Dissolvable Tablets)")
                .strength("1000mg + 10mg")
                .manufacturer("Abbott Healthcare Products")
                .storageInfo("Keep tube tightly capped in a dry place.")
                .initialStock(55)
                .reorderLevel(15)
                .locationAisle("Wellness Shelf C-2")
                .build());

        // Product 12: Antiseptic Liquid
        productService.createProduct(ProductRequestDTO.builder()
                .name("Antiseptic First Aid Wound Disinfectant Liquid 500ml")
                .sku("FA-DTL-500")
                .categoryId(firstAid.getId())
                .brandId(dettol.getId())
                .price(new BigDecimal("950.00"))
                .oldPrice(new BigDecimal("1100.00"))
                .rxRequired(false)
                .badge("First Aid")
                .description("Hospital-grade proven antiseptic liquid for cleaning cuts, grazes, bites, minor burns, and general household hygiene disinfection.")
                .image("assets/images/first_aid.png")
                .activeIngredient("Chloroxylenol (PCMX) 4.8% w/v")
                .dosageForm("Liquid Disinfectant Bottle (500ml)")
                .strength("4.8% Antiseptic Concentrate")
                .manufacturer("Reckitt Benckiser Healthcare Ltd")
                .storageInfo("Keep out of reach of children.")
                .initialStock(80)
                .reorderLevel(25)
                .locationAisle("First Aid Shelf E-1")
                .build());
    }
}
