package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.cart.AddToCartRequest;
import lk.ijse.pharmacy_backend.dto.cart.CartItemDTO;
import lk.ijse.pharmacy_backend.dto.cart.CartResponseDTO;
import lk.ijse.pharmacy_backend.entity.CartItem;
import lk.ijse.pharmacy_backend.entity.Inventory;
import lk.ijse.pharmacy_backend.entity.Product;
import lk.ijse.pharmacy_backend.entity.ProductImage;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.exception.BadRequestException;
import lk.ijse.pharmacy_backend.exception.InsufficientStockException;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.*;
import lk.ijse.pharmacy_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductImageRepository productImageRepository;

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("5000.00");
    private static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("350.00");

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCart(String userEmail) {
        User user = getUser(userEmail);
        List<CartItem> items = cartItemRepository.findByUser(user);
        return buildCartResponse(items);
    }

    @Override
    @Transactional
    public CartResponseDTO addToCart(String userEmail, AddToCartRequest request) {
        User user = getUser(userEmail);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        if (!product.isActive()) {
            throw new BadRequestException("Product is currently unavailable");
        }

        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new ResourceNotFoundException("Product inventory not found"));

        int currentStock = inventory.getCurrentStock() != null ? inventory.getCurrentStock() : 0;
        int requestedQty = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;

        Optional<CartItem> existingItemOpt = cartItemRepository.findByUserAndProduct(user, product);

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int newTotalQty = item.getQuantity() + requestedQty;

            if (newTotalQty > currentStock) {
                throw new InsufficientStockException(
                        String.format("Cannot add %d items. Only %d units available in stock for %s.",
                                requestedQty, currentStock - item.getQuantity(), product.getName())
                );
            }

            item.setQuantity(newTotalQty);
            cartItemRepository.save(item);
        } else {
            if (requestedQty > currentStock) {
                throw new InsufficientStockException(
                        String.format("Requested quantity (%d) exceeds available stock (%d) for %s.",
                                requestedQty, currentStock, product.getName())
                );
            }

            CartItem newItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(requestedQty)
                    .build();
            cartItemRepository.save(newItem);
        }

        List<CartItem> items = cartItemRepository.findByUser(user);
        return buildCartResponse(items);
    }

    @Override
    @Transactional
    public CartResponseDTO updateQuantity(String userEmail, Long cartItemId, Integer quantity) {
        User user = getUser(userEmail);
        CartItem cartItem = cartItemRepository.findByIdAndUser(cartItemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        if (quantity == null || quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            Inventory inventory = inventoryRepository.findByProduct(cartItem.getProduct())
                    .orElseThrow(() -> new ResourceNotFoundException("Product inventory not found"));

            int currentStock = inventory.getCurrentStock() != null ? inventory.getCurrentStock() : 0;
            if (quantity > currentStock) {
                throw new InsufficientStockException(
                        String.format("Requested quantity (%d) exceeds available stock (%d) for %s.",
                                quantity, currentStock, cartItem.getProduct().getName())
                );
            }

            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        List<CartItem> items = cartItemRepository.findByUser(user);
        return buildCartResponse(items);
    }

    @Override
    @Transactional
    public CartResponseDTO removeFromCart(String userEmail, Long cartItemId) {
        User user = getUser(userEmail);
        CartItem cartItem = cartItemRepository.findByIdAndUser(cartItemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        cartItemRepository.delete(cartItem);
        List<CartItem> items = cartItemRepository.findByUser(user);
        return buildCartResponse(items);
    }

    @Override
    @Transactional
    public void clearCart(String userEmail) {
        User user = getUser(userEmail);
        cartItemRepository.deleteByUser(user);
        log.info("Shopping cart cleared for user: {}", userEmail);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private CartResponseDTO buildCartResponse(List<CartItem> items) {
        List<CartItemDTO> dtos = new ArrayList<>();
        int count = 0;
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            Product p = item.getProduct();
            BigDecimal itemPrice = p.getPrice();
            BigDecimal itemSubtotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            subtotal = subtotal.add(itemSubtotal);
            count += item.getQuantity();

            List<ProductImage> images = productImageRepository.findByProductOrderByDisplayOrderAsc(p);
            String imgUrl = !images.isEmpty() ? images.get(0).getImageUrl() : "assets/images/medicine_1.png";

            Inventory inv = inventoryRepository.findByProduct(p).orElse(null);
            int stock = inv != null ? inv.getCurrentStock() : 0;

            dtos.add(CartItemDTO.builder()
                    .id(item.getId())
                    .productId(p.getId())
                    .name(p.getName())
                    .brand(p.getBrand() != null ? p.getBrand().getName() : "KK Digital Pharmacy")
                    .price(itemPrice)
                    .image(imgUrl)
                    .category(p.getCategory() != null ? p.getCategory().getSlug() : "general")
                    .requiresPrescription(p.isRxRequired())
                    .quantity(item.getQuantity())
                    .subtotal(itemSubtotal)
                    .availableStock(stock)
                    .build());
        }

        boolean isFreeShipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0;
        BigDecimal deliveryFee = count == 0 ? BigDecimal.ZERO : (isFreeShipping ? BigDecimal.ZERO : STANDARD_DELIVERY_FEE);
        BigDecimal total = subtotal.add(deliveryFee);

        BigDecimal needed = FREE_SHIPPING_THRESHOLD.subtract(subtotal);
        if (needed.compareTo(BigDecimal.ZERO) < 0) {
            needed = BigDecimal.ZERO;
        }

        return CartResponseDTO.builder()
                .items(dtos)
                .count(count)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .total(total)
                .freeShippingEligible(isFreeShipping)
                .freeShippingThreshold(FREE_SHIPPING_THRESHOLD)
                .amountNeededForFreeShipping(needed)
                .build();
    }
}
