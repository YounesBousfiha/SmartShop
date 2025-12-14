package com.jartiste.smartshop.infrastructure.seeder;


import com.jartiste.smartshop.domain.entity.Admin;
import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.Product;
import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.OrderItem;
import com.jartiste.smartshop.domain.entity.Payment;
import com.jartiste.smartshop.domain.enums.CustomerTier;
import com.jartiste.smartshop.domain.enums.OrderStatus;
import com.jartiste.smartshop.domain.enums.PaymentMethod;
import com.jartiste.smartshop.domain.enums.PaymentStatus;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.domain.repository.ClientRepository;
import com.jartiste.smartshop.domain.repository.ProductRepository;
import com.jartiste.smartshop.domain.repository.OrderRepository;
import com.jartiste.smartshop.domain.repository.PaymentRepository;
import com.jartiste.smartshop.domain.repository.UserRepository;
import com.jartiste.smartshop.infrastructure.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordUtil passwordUtil;


    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedClients();
        seedProducts();
        seedOrders();
        seedCodePromos();
    }


    private void seedUsers() {
        if(this.userRepository.count() > 0) {
            log.info("Users(Admin) Already Seeded...");
            return;
        }

        Admin admin = Admin.builder()
                .username("admin")
                .password(passwordUtil.hashPassword("admin123"))
                .role(UserRole.ADMIN)
                .build();

        this.userRepository.save(admin);
        log.info("Admin User Seeded");
    }

    private void seedClients() {

        if(this.clientRepository.count() > 0) {
            log.info("Client Already Seeded...");
            return;
        }
        // Client 1 - Will become Silver through orders
        Client client1 = Client.builder()
                .nom("Nokia Maroc")
                .username("nokia_admin")
                .password(passwordUtil.hashPassword("nokia123"))
                .tier(CustomerTier.BASIC)
                .role(UserRole.CLIENT)
                .totalOrders(0)
                .totalSpent(BigDecimal.ZERO)
                .build();

        // Client 2 - Will become Gold through orders
        Client client2 = Client.builder()
                .nom("Samsung Store")
                .username("samsung_user")
                .password(passwordUtil.hashPassword("samsung123"))
                .role(UserRole.CLIENT)
                .tier(CustomerTier.BASIC)
                .totalOrders(0)
                .totalSpent(BigDecimal.ZERO)
                .build();

        // Client 3 - Will become Platinum through orders
        Client client3 = Client.builder()
                .nom("Tech Giant SARL")
                .username("tech_giant")
                .password(passwordUtil.hashPassword("giant123"))
                .role(UserRole.CLIENT)
                .tier(CustomerTier.BASIC)
                .totalOrders(0)
                .totalSpent(BigDecimal.ZERO)
                .build();

        // Client 4 - Basic (few orders)
        Client client4 = Client.builder()
                .nom("Small Shop")
                .username("new_shop")
                .password(passwordUtil.hashPassword("shop123"))
                .role(UserRole.CLIENT)
                .tier(CustomerTier.BASIC)
                .totalOrders(0)
                .totalSpent(BigDecimal.ZERO)
                .build();



        this.clientRepository.saveAll(Arrays.asList(
                client1,
                client2,
                client3,
                client4
        ));
        log.info("Client seeded: Will be updated with orders to reach different tiers");
    }

    private void seedProducts() {

        if(this.productRepository.count() > 0) {
            log.info("Product Already Seeded...");
            return;
        }
        // Expensive Product
        Product product1 = Product.builder()
                .name("MacBook Pro M3 Max")
                .price(BigDecimal.valueOf(35000.00))
                .stock(10)
                .deleted(false)
                .build();

        // Inexpensive Product
        Product product2 = Product.builder()
                .name("Souris Logitech Sans Fil")
                .price(BigDecimal.valueOf(150.00))
                .stock(100)
                .deleted(false)
                .build();

        // Medium price Product
        Product product3 = Product.builder()
                .name("Ecran Dell 27 pouces 4K")
                .price(BigDecimal.valueOf(4500.00))
                .stock(20)
                .deleted(false)
                .build();

        // Stock Zero
        Product product4 = Product.builder()
                .name("Carte Graphique RTX 4090 (Rupture)")
                .price(BigDecimal.valueOf(25000.00))
                .stock(0) // Stock Zero!
                .deleted(false)
                .build();

        // Soft Deleted Product
        Product product5 = Product.builder()
                .name("Cable VGA")
                .price(BigDecimal.valueOf(50.00))
                .stock(5)
                .deleted(true)
                .build();



        this.productRepository.saveAll(Arrays.asList(
                product1,
                product2,
                product3,
                product4,
                product5
                )
        );

        log.info("Product Seeded (Expensive, Inexpensive, Medium , Out of Stock, Deleted)");
    }

    private void seedOrders() {
        if (this.orderRepository.count() > 0) {
            log.info("Orders Already Seeded...");
            return;
        }

        // Get all clients and products
        List<Client> clients = this.clientRepository.findAll();
        List<Product> products = this.productRepository.findAll().stream()
                .filter(p -> !p.getDeleted() && p.getStock() > 0)
                .toList();

        if (clients.isEmpty() || products.isEmpty()) {
            log.warn("Cannot seed orders: No clients or products available");
            return;
        }

        // Reference products by name for easier access
        Product macbook = products.stream().filter(p -> p.getName().contains("MacBook")).findFirst().orElse(products.get(0));
        Product mouse = products.stream().filter(p -> p.getName().contains("Souris")).findFirst().orElse(products.get(0));
        Product screen = products.stream().filter(p -> p.getName().contains("Ecran")).findFirst().orElse(products.get(0));

        List<Order> allOrders = new ArrayList<>();

        // CLIENT 1 (Nokia Maroc) - Will reach SILVER tier (3+ orders or 1000+ spent)
        Client client1 = clients.get(0);

        // Order 1 - CONFIRMED - 6 months ago
        Order order1_1 = createOrder(client1, OrderStatus.CONFIRMED,
                LocalDateTime.now().minusMonths(6),
                Arrays.asList(
                    createOrderItem(mouse, 10, mouse.getPrice()),
                    createOrderItem(screen, 1, screen.getPrice())
                ));
        allOrders.add(order1_1);

        // Order 2 - CONFIRMED - 3 months ago
        Order order1_2 = createOrder(client1, OrderStatus.CONFIRMED,
                LocalDateTime.now().minusMonths(3),
                Arrays.asList(
                    createOrderItem(mouse, 5, mouse.getPrice())
                ));
        allOrders.add(order1_2);

        // Order 3 - CONFIRMED - 1 month ago
        Order order1_3 = createOrder(client1, OrderStatus.CONFIRMED,
                LocalDateTime.now().minusMonths(1),
                Arrays.asList(
                    createOrderItem(screen, 1, screen.getPrice())
                ));
        allOrders.add(order1_3);

        // Order 4 - PENDING - recent
        Order order1_4 = createOrder(client1, OrderStatus.PENDING,
                LocalDateTime.now().minusDays(2),
                Arrays.asList(
                    createOrderItem(mouse, 3, mouse.getPrice())
                ));
        allOrders.add(order1_4);

        // Order 5 - PENDING with partial payment (50% paid)
        Order order1_5 = createOrder(client1, OrderStatus.PENDING,
                LocalDateTime.now().minusDays(1),
                Arrays.asList(
                    createOrderItem(screen, 2, screen.getPrice()),
                    createOrderItem(mouse, 5, mouse.getPrice())
                ));
        allOrders.add(order1_5);
        // Will add partial payment later

        // CLIENT 2 (Samsung Store) - Will reach GOLD tier (10+ orders or 5000+ spent)
        Client client2 = clients.get(1);

        // Create 12 orders over 8 months
        for (int i = 0; i < 10; i++) {
            OrderStatus status = i < 8 ? OrderStatus.CONFIRMED : (i == 8 ? OrderStatus.PENDING : OrderStatus.CANCELED);
            LocalDateTime orderDate = LocalDateTime.now().minusMonths(8).plusWeeks(i * 3);

            List<OrderItem> items = new ArrayList<>();
            if (i % 3 == 0) {
                items.add(createOrderItem(macbook, 1, macbook.getPrice()));
            } else if (i % 3 == 1) {
                items.add(createOrderItem(screen, 2, screen.getPrice()));
            } else {
                items.add(createOrderItem(mouse, 8, mouse.getPrice()));
                items.add(createOrderItem(screen, 1, screen.getPrice()));
            }

            allOrders.add(createOrder(client2, status, orderDate, items));
        }

        // Add PENDING order with multiple partial payments (75% paid)
        Order order2_pending = createOrder(client2, OrderStatus.PENDING,
                LocalDateTime.now().minusDays(3),
                Arrays.asList(
                    createOrderItem(macbook, 1, macbook.getPrice()),
                    createOrderItem(mouse, 10, mouse.getPrice())
                ));
        allOrders.add(order2_pending);
        // Will add multiple partial payments later

        // CLIENT 3 (Tech Giant) - Will reach PLATINUM tier (20+ orders or 15000+ spent)
        Client client3 = clients.get(2);

        // Create 25 orders over 12 months - mix of large and small orders
        for (int i = 0; i < 25; i++) {
            OrderStatus status;
            if (i < 20) {
                status = OrderStatus.CONFIRMED;
            } else if (i < 23) {
                status = OrderStatus.PENDING;
            } else {
                status = i == 23 ? OrderStatus.CANCELED : OrderStatus.REJECTED;
            }

            LocalDateTime orderDate = LocalDateTime.now().minusMonths(12).plusWeeks(i * 2);

            List<OrderItem> items = new ArrayList<>();
            // Large orders every 5th order
            if (i % 5 == 0) {
                items.add(createOrderItem(macbook, 2, macbook.getPrice()));
                items.add(createOrderItem(screen, 3, screen.getPrice()));
            } else if (i % 5 == 1) {
                items.add(createOrderItem(screen, 5, screen.getPrice()));
            } else if (i % 5 == 2) {
                items.add(createOrderItem(macbook, 1, macbook.getPrice()));
            } else {
                items.add(createOrderItem(mouse, 10, mouse.getPrice()));
                items.add(createOrderItem(screen, 2, screen.getPrice()));
            }

            allOrders.add(createOrder(client3, status, orderDate, items));
        }

        // Add PENDING order with partial payment by CHEQUE (30% paid, awaiting clearance)
        Order order3_pending1 = createOrder(client3, OrderStatus.PENDING,
                LocalDateTime.now().minusDays(7),
                Arrays.asList(
                    createOrderItem(macbook, 2, macbook.getPrice()),
                    createOrderItem(screen, 2, screen.getPrice())
                ));
        allOrders.add(order3_pending1);

        // Add another PENDING order with VIREMENT payment (60% paid)
        Order order3_pending2 = createOrder(client3, OrderStatus.PENDING,
                LocalDateTime.now().minusDays(4),
                Arrays.asList(
                    createOrderItem(screen, 5, screen.getPrice())
                ));
        allOrders.add(order3_pending2);

        // CLIENT 4 (Small Shop) - Will stay BASIC tier (< 3 orders and < 1000 spent)
        Client client4 = clients.get(3);

        // Order 1 - CONFIRMED
        Order order4_1 = createOrder(client4, OrderStatus.CONFIRMED,
                LocalDateTime.now().minusMonths(2),
                Arrays.asList(
                    createOrderItem(mouse, 2, mouse.getPrice())
                ));
        allOrders.add(order4_1);

        // Order 2 - PENDING
        Order order4_2 = createOrder(client4, OrderStatus.PENDING,
                LocalDateTime.now().minusDays(5),
                Arrays.asList(
                    createOrderItem(mouse, 1, mouse.getPrice())
                ));
        allOrders.add(order4_2);

        // Order 3 - CANCELED
        Order order4_3 = createOrder(client4, OrderStatus.CANCELED,
                LocalDateTime.now().minusDays(1),
                Arrays.asList(
                    createOrderItem(screen, 1, screen.getPrice())
                ));
        allOrders.add(order4_3);

        // Save all orders
        this.orderRepository.saveAll(allOrders);

        // Create partial payments for PENDING orders
        List<Payment> allPayments = new ArrayList<>();

        // Client 1 - Order 5: 50% paid via ESPECES (cleared)
        BigDecimal order1_5_total = order1_5.getTotalAmount();
        BigDecimal order1_5_payment = order1_5_total.multiply(BigDecimal.valueOf(0.50));
        allPayments.add(createPayment(order1_5, order1_5_payment, PaymentMethod.ESPECES,
                PaymentStatus.ENCAISSE, "ESP-2024-001", null,
                LocalDateTime.now().minusDays(1)));
        order1_5.setRemainingAmount(order1_5_total.subtract(order1_5_payment));

        // Client 2 - Order (pending): 75% paid via multiple payments
        BigDecimal order2_pending_total = order2_pending.getTotalAmount();
        // First payment: 40% via VIREMENT (cleared)
        BigDecimal payment2_1 = order2_pending_total.multiply(BigDecimal.valueOf(0.40));
        allPayments.add(createPayment(order2_pending, payment2_1, PaymentMethod.VIREMENT,
                PaymentStatus.ENCAISSE, "VIR-2024-045", "Attijariwafa Bank",
                LocalDateTime.now().minusDays(3)));

        // Second payment: 35% via CHEQUE (cleared)
        BigDecimal payment2_2 = order2_pending_total.multiply(BigDecimal.valueOf(0.35));
        allPayments.add(createPayment(order2_pending, payment2_2, PaymentMethod.CHEQUE,
                PaymentStatus.ENCAISSE, "CHQ-8765432", "BMCE Bank",
                LocalDateTime.now().minusDays(2)));

        BigDecimal totalPaid2 = payment2_1.add(payment2_2);
        order2_pending.setRemainingAmount(order2_pending_total.subtract(totalPaid2));

        // Client 3 - Order pending 1: 30% paid via CHEQUE (awaiting clearance)
        BigDecimal order3_p1_total = order3_pending1.getTotalAmount();
        BigDecimal payment3_1 = order3_p1_total.multiply(BigDecimal.valueOf(0.30));
        allPayments.add(createPayment(order3_pending1, payment3_1, PaymentMethod.CHEQUE,
                PaymentStatus.EN_ATTENTE, "CHQ-9988776", "Banque Populaire",
                LocalDateTime.now().minusDays(7)));
        order3_pending1.setRemainingAmount(order3_p1_total.subtract(payment3_1));

        // Client 3 - Order pending 2: 60% paid via VIREMENT (cleared)
        BigDecimal order3_p2_total = order3_pending2.getTotalAmount();
        BigDecimal payment3_2 = order3_p2_total.multiply(BigDecimal.valueOf(0.60));
        allPayments.add(createPayment(order3_pending2, payment3_2, PaymentMethod.VIREMENT,
                PaymentStatus.ENCAISSE, "VIR-2024-078", "CIH Bank",
                LocalDateTime.now().minusDays(4)));
        order3_pending2.setRemainingAmount(order3_p2_total.subtract(payment3_2));

        // Client 4 - Order 2 (pending): 25% paid via ESPECES
        BigDecimal order4_2_total = order4_2.getTotalAmount();
        BigDecimal payment4_1 = order4_2_total.multiply(BigDecimal.valueOf(0.25));
        allPayments.add(createPayment(order4_2, payment4_1, PaymentMethod.ESPECES,
                PaymentStatus.ENCAISSE, "ESP-2024-025", null,
                LocalDateTime.now().minusDays(5)));
        order4_2.setRemainingAmount(order4_2_total.subtract(payment4_1));

        // Save all payments
        this.paymentRepository.saveAll(allPayments);

        // Update orders with new remaining amounts
        this.orderRepository.saveAll(Arrays.asList(order1_5, order2_pending, order3_pending1, order3_pending2, order4_2));

        // Update client statistics based on CONFIRMED orders
        updateClientStats(client1, allOrders.stream().filter(o -> o.getClient().getId().equals(client1.getId())).toList());
        updateClientStats(client2, allOrders.stream().filter(o -> o.getClient().getId().equals(client2.getId())).toList());
        updateClientStats(client3, allOrders.stream().filter(o -> o.getClient().getId().equals(client3.getId())).toList());
        updateClientStats(client4, allOrders.stream().filter(o -> o.getClient().getId().equals(client4.getId())).toList());

        this.clientRepository.saveAll(clients);

        log.info("Orders Seeded: {} orders created across {} clients", allOrders.size(), clients.size());
        log.info("Client tiers updated based on confirmed orders");
    }

    private Order createOrder(Client client, OrderStatus status, LocalDateTime createdAt, List<OrderItem> items) {
        // Calculate order totals
        BigDecimal subTotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = subTotal.multiply(BigDecimal.valueOf(0.20)); // 20% tax
        BigDecimal totalAmount = subTotal.add(taxAmount).subtract(discountAmount);

        Order order = Order.builder()
                .client(client)
                .orderStatus(status)
                .subTotal(subTotal)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .remainingAmount(status == OrderStatus.CONFIRMED ? BigDecimal.ZERO : totalAmount)
                .itemList(items)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        // Set order reference for all items
        items.forEach(item -> item.setOrder(order));

        return order;
    }

    private OrderItem createOrderItem(Product product, Integer quantity, BigDecimal unitPrice) {
        return OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }

    private void updateClientStats(Client client, List<Order> clientOrders) {
        List<Order> confirmedOrders = clientOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.CONFIRMED)
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .toList();

        if (confirmedOrders.isEmpty()) {
            return;
        }

        // Update total orders and total spent
        client.setTotalOrders(confirmedOrders.size());
        BigDecimal totalSpent = confirmedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        client.setTotalSpent(totalSpent);

        // Update first and last order dates
        client.setFirstOrderDate(confirmedOrders.get(0).getCreatedAt().toLocalDate());
        client.setLastOrderDate(confirmedOrders.get(confirmedOrders.size() - 1).getCreatedAt().toLocalDate());

        // Recalculate tier based on orders and spending
        recalculateTier(client);

        log.info("Updated client {}: {} confirmed orders, {} spent, tier: {}",
                client.getNom(), client.getTotalOrders(), client.getTotalSpent(), client.getTier());
    }

    private void recalculateTier(Client client) {
        double spent = client.getTotalSpent().doubleValue();
        int orders = client.getTotalOrders();

        if (orders >= 20 || spent >= 15000) {
            client.setTier(CustomerTier.PLATINUM);
        } else if (orders >= 10 || spent >= 5000) {
            client.setTier(CustomerTier.GOLD);
        } else if (orders >= 3 || spent >= 1000) {
            client.setTier(CustomerTier.SLIVER);
        } else {
            client.setTier(CustomerTier.BASIC);
        }
    }

    private Payment createPayment(Order order, BigDecimal amount, PaymentMethod method,
                                   PaymentStatus status, String reference, String bankName,
                                   LocalDateTime createdAt) {
        return Payment.builder()
                .order(order)
                .amount(amount)
                .paymentMethod(method)
                .paymentStatus(status)
                .reference(reference)
                .bankName(bankName)
                .clearedDate(status == PaymentStatus.ENCAISSE ? createdAt.plusDays(1) : null)
                .dueDate(method == PaymentMethod.CHEQUE ? createdAt.toLocalDate().plusDays(30) : null)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private void seedCodePromos() {
        /* Code Promo */
    }
}
