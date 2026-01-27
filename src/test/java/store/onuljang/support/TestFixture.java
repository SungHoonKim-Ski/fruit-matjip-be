package store.onuljang.support;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import store.onuljang.auth.JwtUtil;
import store.onuljang.repository.AdminRepository;
import store.onuljang.repository.ProductCategoryRepository;
import store.onuljang.repository.ProductsRepository;
import store.onuljang.repository.ReservationRepository;
import store.onuljang.repository.UserRepository;
import store.onuljang.repository.entity.*;
import store.onuljang.repository.entity.enums.AdminRole;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.dto.JwtToken;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static store.onuljang.util.TimeUtil.nowDate;

/**
 * 테스트 데이터 생성 헬퍼 클래스
 */
@Component
public class TestFixture {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EntityManager entityManager;

    /**
     * 기본 사용자 생성 (닉네임 변경 완료 상태)
     */
    public Users createUser(String name) {
        String uniqueName = name + UUID.randomUUID().toString().substring(0, 8);
        return createUserWithExactName(uniqueName);
    }

    public Users createUserWithExactName(String name) {
        Users user = Users.builder().socialId("social-" + UUID.randomUUID()).uid(UUID.randomUUID()).name(name).build();
        user.modifyName(name); // changeName을 true로 설정
        return userRepository.save(user);
    }

    /**
     * 닉네임 미변경 사용자 생성
     */
    public Users createNewUser() {
        Users user = Users.builder().socialId("social-" + UUID.randomUUID()).uid(UUID.randomUUID()).name("신규 고객")
                .build();
        return userRepository.save(user);
    }

    /**
     * 경고 횟수가 설정된 사용자 생성
     */
    public Users createUserWithWarns(String name, int warnCount) {
        Users user = createUser(name);
        user.warn(warnCount);
        return userRepository.save(user);
    }

    /**
     * 관리자 생성
     */
    public Admin createAdmin(String name, String email, String password) {
        Admin admin = Admin.builder().name(name).email(email).password(password)
                .role(AdminRole.OWNER).build();
        return adminRepository.saveAndFlush(admin);
    }

    /**
     * 기본 관리자 생성 (이미 있으면 조회)
     */
    public Admin createDefaultAdmin() {
        return adminRepository.findByEmail("testadmin").orElseGet(() -> createAdmin("관리자", "testadmin", "password123"));
    }

    /**
     * 상품 생성
     */
    public Product createProduct(String name, int stock, BigDecimal price, LocalDate sellDate, Admin admin) {
        Product product = Product.builder().name(name).stock(stock).price(price).sellDate(sellDate)
                .productUrl("https://example.com/image.jpg").visible(true).selfPick(true).registeredAdmin(admin)
                .build();
        return productsRepository.save(product);
    }

    /**
     * 오늘 판매 상품 생성
     */
    public Product createTodayProduct(String name, int stock, BigDecimal price, Admin admin) {
        return createProduct(name, stock, price, nowDate(), admin);
    }

    /**
     * 내일 판매 상품 생성 (시간 무관 테스트용)
     *
     * 19:30 이후에도 예약 가능하도록 내일 날짜로 생성. 시간과 무관하게 테스트가 동작해야 하는 경우 사용.
     */
    public Product createTomorrowProduct(String name, int stock, BigDecimal price, Admin admin) {
        return createProduct(name, stock, price, nowDate().plusDays(1), admin);
    }

    /**
     * 미래 판매 상품 생성
     */
    public Product createFutureProduct(String name, int stock, BigDecimal price, int daysLater, Admin admin) {
        return createProduct(name, stock, price, nowDate().plusDays(daysLater), admin);
    }

    /**
     * 과거 판매 상품 생성
     */
    public Product createPastProduct(String name, int stock, BigDecimal price, int daysAgo, Admin admin) {
        return createProduct(name, stock, price, nowDate().minusDays(daysAgo), admin);
    }

    /**
     * 특정 날짜와 시간에 판매되는 상품 생성
     */
    public Product createProductAtDateTime(String name, int stock, BigDecimal price,
            java.time.LocalDateTime sellDateTime, Admin admin) {
        Product product = createProduct(name, stock, price, sellDateTime.toLocalDate(), admin);
        product.setSellTime(sellDateTime.toLocalTime());
        return productsRepository.save(product);
    }

    /**
     * 비공개 상품 생성
     */
    public Product createInvisibleProduct(String name, int stock, BigDecimal price, LocalDate sellDate, Admin admin) {
        Product product = createProduct(name, stock, price, sellDate, admin);
        product.toggleVisible(); // visible = false
        return productsRepository.save(product);
    }

    /**
     * 셀프 픽업 불가 상품 생성
     */
    public Product createNoSelfPickProduct(String name, int stock, BigDecimal price, LocalDate sellDate, Admin admin) {
        Product product = createProduct(name, stock, price, sellDate, admin);
        product.toggleSelfPick(); // selfPick = false
        return productsRepository.save(product);
    }

    /**
     * 판매 시간이 설정된 상품 생성
     */
    public Product createProductWithSellTime(String name, int stock, BigDecimal price, LocalDate sellDate,
            LocalTime sellTime, Admin admin) {
        Product product = createProduct(name, stock, price, sellDate, admin);
        product.setSellTime(sellTime);
        return productsRepository.save(product);
    }

    /**
     * 예약 생성
     */
    public Reservation createReservation(Users user, Product product, int quantity) {
        BigDecimal amount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        Reservation reservation = Reservation.builder().user(user).product(product).quantity(quantity).amount(amount)
                .sellPrice(product.getPrice()).pickupDate(product.getSellDate()).build();
        return reservationRepository.save(reservation);
    }

    /**
     * 특정 상태의 예약 생성
     */
    public Reservation createReservationWithStatus(Users user, Product product, int quantity,
            ReservationStatus status) {
        Reservation reservation = createReservation(user, product, quantity);
        reservation.changeStatus(status);
        return reservationRepository.save(reservation);
    }

    /**
     * 상품 카테고리 생성
     */
    public ProductCategory createProductCategory(String name) {
        ProductCategory category = ProductCategory.builder().name(name).build();
        return productCategoryRepository.save(category);
    }

    /**
     * 상품 카테고리 생성 (이미지 포함)
     */
    public ProductCategory createProductCategory(String name, String imageUrl) {
        ProductCategory category = ProductCategory.builder().name(name).imageUrl(imageUrl).build();
        return productCategoryRepository.save(category);
    }

    /**
     * 상품에 카테고리 연결
     */
    public Product addCategoryToProduct(Product product, ProductCategory category) {
        category.getProducts().add(product);
        productCategoryRepository.saveAndFlush(category);
        return productsRepository.findById(product.getId()).orElse(product);
    }

    /**
     * 카테고리가 연결된 상품 생성
     */
    public Product createProductWithCategory(String name, int stock, BigDecimal price, LocalDate sellDate, Admin admin,
            ProductCategory category) {
        Product product = createProduct(name, stock, price, sellDate, admin);
        category.getProducts().add(product);
        productCategoryRepository.saveAndFlush(category);
        return product;
    }

    /**
     * 사용자의 JWT 토큰 생성
     */
    public String createAccessToken(Users user) {
        JwtToken token = jwtUtil.generateToken(user);
        return token.access();
    }

    /**
     * uid로 JWT 토큰 생성
     */
    public String createAccessToken(String uid, String name) {
        JwtToken token = jwtUtil.generateToken(uid, name);
        return token.access();
    }
}
