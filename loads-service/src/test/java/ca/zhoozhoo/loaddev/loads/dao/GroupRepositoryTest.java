package ca.zhoozhoo.loaddev.loads.dao;

import static java.util.UUID.randomUUID;
import static reactor.test.StepVerifier.create;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static systems.uom.ucum.UCUM.YARD_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.model.Group;
import ca.zhoozhoo.loaddev.loads.model.Load;

/**
 * Integration tests for {@link GroupRepository}.
 * <p>
 * Tests R2DBC repository operations for group data including CRUD operations,
 * custom query methods, and Quantity type persistence for powder charges,
 * target ranges, and group sizes.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class GroupRepositoryTest {

    private static final LocalDate TEST_DATE = LocalDate.now().minusDays(1);

    @Autowired
    private ShotRepository shotRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private LoadRepository loadRepository;
    
    private Long testLoadId;

    @BeforeEach
    public void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        loadRepository.deleteAll().block();
        
        // Create a test load to satisfy foreign key constraint
        var ownerId = randomUUID().toString();
        var testLoad = createTestLoad(ownerId);
        var savedLoad = loadRepository.save(testLoad).block();
        testLoadId = savedLoad.id();
    }
    
    private Load createTestLoad(String ownerId) {
        return new Load(
                null,
                ownerId,
                "Test Load",
                "Test Description",
                "Hodgdon",
                "H4350",
                "Sierra",
                "MatchKing",
                getQuantity(168, GRAIN),
                "Federal",
                "210M",
                getQuantity(0.020, INCH_INTERNATIONAL),
                null,
                null,
                null);
    }

    private Group createTestGroup(String ownerId) {
        return new Group(
                null,
                ownerId,
                testLoadId,
                TEST_DATE,
                getQuantity(41.5, GRAIN),
                getQuantity(100, YARD_INTERNATIONAL),
                getQuantity(0.75, INCH_INTERNATIONAL));
    }

    @Test
    void findByIdAndOwnerId() {
        var ownerId = randomUUID().toString();
        var savedGroup = groupRepository.save(createTestGroup(ownerId)).block();

        create(groupRepository.findByIdAndOwnerId(savedGroup.id(), savedGroup.ownerId()))
                .expectNextMatches(g ->
                    g.id().equals(savedGroup.id()) &&
                    g.ownerId().equals(ownerId) &&
                    g.loadId().equals(testLoadId) &&
                    g.date().equals(TEST_DATE) &&
                    g.powderCharge().to(GRAIN).getValue().doubleValue() == 41.5 &&
                    g.targetRange().to(YARD_INTERNATIONAL).getValue().doubleValue() == 100.0 &&
                    g.groupSize().to(INCH_INTERNATIONAL).getValue().doubleValue() == 0.75)
                .verifyComplete();
    }

    @Test
    void findAllByLoadIdAndOwnerId() {
        var ownerId = randomUUID().toString();
        var group1 = createTestGroup(ownerId);
        var group2 = new Group(null,
                ownerId,
                testLoadId,
                TEST_DATE.minusDays(1),
                getQuantity(42.0, GRAIN),
                getQuantity(200, YARD_INTERNATIONAL),
                getQuantity(1.25, INCH_INTERNATIONAL));

        groupRepository.save(group1).block();
        groupRepository.save(group2).block();

        create(groupRepository.findAllByLoadIdAndOwnerId(testLoadId, ownerId))
                .expectNextMatches(g ->
                    g.ownerId().equals(ownerId) &&
                    g.loadId().equals(testLoadId) &&
                    g.date().equals(TEST_DATE) &&
                    g.powderCharge().to(GRAIN).getValue().doubleValue() == 41.5 &&
                    g.targetRange().to(YARD_INTERNATIONAL).getValue().doubleValue() == 100.0 &&
                    g.groupSize().to(INCH_INTERNATIONAL).getValue().doubleValue() == 0.75)
                .expectNextMatches(g ->
                    g.ownerId().equals(ownerId) &&
                    g.loadId().equals(testLoadId) &&
                    g.date().equals(TEST_DATE.minusDays(1)) &&
                    g.powderCharge().to(GRAIN).getValue().doubleValue() == 42.0 &&
                    g.targetRange().to(YARD_INTERNATIONAL).getValue().doubleValue() == 200.0 &&
                    g.groupSize().to(INCH_INTERNATIONAL).getValue().doubleValue() == 1.25)
                .verifyComplete();
    }

    @Test
    void save() {
        var ownerId = randomUUID().toString();
        var group = createTestGroup(ownerId);

        create(groupRepository.save(group))
                .expectNextMatches(g ->
                    g.id() != null &&
                    g.ownerId().equals(ownerId) &&
                    g.loadId().equals(testLoadId))
                .verifyComplete();
    }

    @Test
    void update() {
        var ownerId = randomUUID().toString();
        var savedGroup = groupRepository.save(createTestGroup(ownerId)).block();

        var updatedGroup = new Group(
                savedGroup.id(),
                savedGroup.ownerId(),
                savedGroup.loadId(),
                savedGroup.date(),
                getQuantity(42.5, GRAIN),
                getQuantity(300, YARD_INTERNATIONAL),
                getQuantity(0.85, INCH_INTERNATIONAL));

        create(groupRepository.save(updatedGroup))
                .expectNextMatches(g ->
                    g.id().equals(savedGroup.id()) &&
                    g.ownerId().equals(ownerId) &&
                    g.powderCharge().to(GRAIN).getValue().doubleValue() == 42.5 &&
                    g.targetRange().to(YARD_INTERNATIONAL).getValue().doubleValue() == 300.0 &&
                    g.groupSize().to(INCH_INTERNATIONAL).getValue().doubleValue() == 0.85)
                .verifyComplete();
    }

    @Test
    void delete() {
        var ownerId = randomUUID().toString();
        var savedGroup = groupRepository.save(createTestGroup(ownerId)).block();

        groupRepository.deleteById(savedGroup.id()).block();

        create(groupRepository.findByIdAndOwnerId(savedGroup.id(), savedGroup.ownerId()))
                .verifyComplete();
    }

    @Test
    void validatePowderChargeRange() {
        var ownerId = randomUUID().toString();
        
        // Test lower bound violation
        create(reactor.core.publisher.Mono.fromCallable(() -> 
            new Group(
                    null,
                    ownerId,
                    testLoadId,
                    TEST_DATE,
                    getQuantity(0.05, GRAIN), // Below minimum
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.75, INCH_INTERNATIONAL))))
                .expectError(IllegalArgumentException.class)
                .verify();

        // Test upper bound violation
        create(reactor.core.publisher.Mono.fromCallable(() ->
            new Group(
                    null,
                    ownerId,
                    testLoadId,
                    TEST_DATE,
                    getQuantity(200, GRAIN), // Above maximum
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.75, INCH_INTERNATIONAL))))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void validateTargetRangeRange() {
        var ownerId = randomUUID().toString();

        // Test lower bound violation
        create(reactor.core.publisher.Mono.fromCallable(() ->
            new Group(
                    null,
                    ownerId,
                    testLoadId,
                    TEST_DATE,
                    getQuantity(41.5, GRAIN),
                    getQuantity(5, YARD_INTERNATIONAL), // Below minimum
                    getQuantity(0.75, INCH_INTERNATIONAL))))
                .expectError(IllegalArgumentException.class)
                .verify();

        // Test upper bound violation
        create(reactor.core.publisher.Mono.fromCallable(() ->
            new Group(
                    null,
                    ownerId,
                    testLoadId,
                    TEST_DATE,
                    getQuantity(41.5, GRAIN),
                    getQuantity(3000, YARD_INTERNATIONAL), // Above maximum
                    getQuantity(0.75, INCH_INTERNATIONAL))))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void validateGroupSizeRange() {
        var ownerId = randomUUID().toString();

        // Test lower bound violation
        create(reactor.core.publisher.Mono.fromCallable(() ->
            new Group(
                    null,
                    ownerId,
                    testLoadId,
                    TEST_DATE,
                    getQuantity(41.5, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.005, INCH_INTERNATIONAL)))) // Below minimum
                .expectError(IllegalArgumentException.class)
                .verify();

        // Test upper bound violation
        create(reactor.core.publisher.Mono.fromCallable(() ->
            new Group(
                    null,
                    ownerId,
                    testLoadId,
                    TEST_DATE,
                    getQuantity(41.5, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(60, INCH_INTERNATIONAL)))) // Above maximum
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void validateDateNotInFuture() {
        var ownerId = randomUUID().toString();

        create(reactor.core.publisher.Mono.fromCallable(() ->
            new Group(
                    null,
                    ownerId,
                    testLoadId,
                    LocalDate.now().plusDays(1), // Future date
                    getQuantity(41.5, GRAIN),
                    getQuantity(100, YARD_INTERNATIONAL),
                    getQuantity(0.75, INCH_INTERNATIONAL))))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
