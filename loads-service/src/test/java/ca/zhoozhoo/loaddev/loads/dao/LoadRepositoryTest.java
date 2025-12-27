package ca.zhoozhoo.loaddev.loads.dao;

import static java.util.UUID.randomUUID;
import static reactor.core.publisher.Flux.just;
import static reactor.test.StepVerifier.create;
import static systems.uom.ucum.UCUM.GRAIN;
import static systems.uom.ucum.UCUM.INCH_INTERNATIONAL;
import static tech.units.indriya.quantity.Quantities.getQuantity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ca.zhoozhoo.loaddev.loads.config.TestSecurityConfig;
import ca.zhoozhoo.loaddev.loads.model.Load;

/**
 * Integration tests for {@link LoadRepository}.
 * <p>
 * Tests R2DBC repository operations for load data including CRUD operations,
 * custom query methods, and Quantity type persistence.
 * </p>
 *
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class LoadRepositoryTest {

    private static final String NAME = "6.5 Creedmoor Match Load";
    private static final String DESCRIPTION = "Long range precision load";
    private static final String POWDER_MFG = "Hodgdon";
    private static final String POWDER_TYPE = "H4350";
    private static final String BULLET_MFG = "Hornady";
    private static final String BULLET_TYPE = "ELD Match";
    private static final String PRIMER_MFG = "CCI";
    private static final String PRIMER_TYPE = "BR-2";

    @Autowired
    private ShotRepository shotRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private LoadRepository loadRepository;

    @BeforeEach
    public void setup() {
        shotRepository.deleteAll().block();
        groupRepository.deleteAll().block();
        loadRepository.deleteAll().block();
    }

    private Load createTestLoad(String ownerId) {
        return new Load(
                null,
                ownerId,
                NAME,
                DESCRIPTION,
                POWDER_MFG,
                POWDER_TYPE,
                BULLET_MFG,
                BULLET_TYPE,
                getQuantity(140, GRAIN),
                PRIMER_MFG,
                PRIMER_TYPE,
                getQuantity(0.020, INCH_INTERNATIONAL),
                getQuantity(2.800, INCH_INTERNATIONAL),
                getQuantity(0.002, INCH_INTERNATIONAL),
                1L);
    }

    @Test
    void findByIdAndOwnerId() {
        var ownerId = randomUUID().toString();
        var savedLoad = loadRepository.save(createTestLoad(ownerId)).block();

        create(loadRepository.findByIdAndOwnerId(savedLoad.id(), savedLoad.ownerId()))
                .expectNextMatches(l -> 
                    l.id().equals(savedLoad.id()) &&
                    l.ownerId().equals(ownerId) &&
                    l.name().equals(NAME) &&
                    l.description().equals(DESCRIPTION) &&
                    l.powderManufacturer().equals(POWDER_MFG) &&
                    l.powderType().equals(POWDER_TYPE) &&
                    l.bulletManufacturer().equals(BULLET_MFG) &&
                    l.bulletType().equals(BULLET_TYPE) &&
                    l.bulletWeight().to(GRAIN).getValue().doubleValue() == 140.0 &&
                    l.primerManufacturer().equals(PRIMER_MFG) &&
                    l.primerType().equals(PRIMER_TYPE) &&
                    l.distanceFromLands().to(INCH_INTERNATIONAL).getValue().doubleValue() == 0.020 &&
                    l.caseOverallLength().to(INCH_INTERNATIONAL).getValue().doubleValue() == 2.800 &&
                    l.neckTension().to(INCH_INTERNATIONAL).getValue().doubleValue() == 0.002 &&
                    l.rifleId().equals(1L))
                .verifyComplete();
    }

    @Test
    void findAllByOwnerId() {
        var ownerId = randomUUID().toString();
        var load1 = createTestLoad(ownerId);
        var load2 = new Load(null,
                ownerId,
                "Hornady 52 BTHP 4198",
                "Hornady 52gr 4198 BTHP with IMR 4198",
                "IMR",
                "4198",
                "Hornady",
                "BTHP Match",
                getQuantity(52, GRAIN),
                "Federal",
                "205M",
                getQuantity(0.020, INCH_INTERNATIONAL),
                getQuantity(2.250, INCH_INTERNATIONAL),
                getQuantity(0.003, INCH_INTERNATIONAL),
                2L);
        
        loadRepository.saveAll(just(load1, load2)).blockLast();

        create(loadRepository.findAllByOwnerId(ownerId))
                .expectNextMatches(l -> 
                    l.ownerId().equals(ownerId) &&
                    l.name().equals(NAME) &&
                    l.bulletWeight().to(GRAIN).getValue().doubleValue() == 140.0)
                .expectNextMatches(l -> 
                    l.ownerId().equals(ownerId) &&
                    l.name().equals("Hornady 52 BTHP 4198") &&
                    l.description().equals("Hornady 52gr 4198 BTHP with IMR 4198") &&
                    l.powderManufacturer().equals("IMR") &&
                    l.powderType().equals("4198") &&
                    l.bulletManufacturer().equals("Hornady") &&
                    l.bulletType().equals("BTHP Match") &&
                    l.bulletWeight().to(GRAIN).getValue().doubleValue() == 52.0 &&
                    l.primerManufacturer().equals("Federal") &&
                    l.primerType().equals("205M") &&
                    l.distanceFromLands().to(INCH_INTERNATIONAL).getValue().doubleValue() == 0.020 &&
                    l.caseOverallLength().to(INCH_INTERNATIONAL).getValue().doubleValue() == 2.250 &&
                    l.neckTension().to(INCH_INTERNATIONAL).getValue().doubleValue() == 0.003 &&
                    l.rifleId().equals(2L))
                .verifyComplete();
    }

    @Test
    void findByNameAndOwnerId() {
        var ownerId = randomUUID().toString();
        var expectedLoad = new Load(null,
                ownerId,
                "Hornady 52 BTHP 4198",
                "Hornady 52gr 4198 BTHP with IMR 4198",
                "IMR",
                "4198",
                "Hornady",
                "BTHP Match",
                getQuantity(52, GRAIN),
                "Federal",
                "205M",
                getQuantity(0.020, INCH_INTERNATIONAL),
                getQuantity(2.250, INCH_INTERNATIONAL),
                getQuantity(0.003, INCH_INTERNATIONAL),
                1L);
        
        loadRepository.saveAll(just(
                createTestLoad(randomUUID().toString()),
                expectedLoad))
                .blockLast();

        create(loadRepository.findByNameAndOwnerId(expectedLoad.name(), expectedLoad.ownerId()))
                .expectNextMatches(l -> 
                    l.ownerId().equals(ownerId) &&
                    l.name().equals("Hornady 52 BTHP 4198") &&
                    l.description().equals("Hornady 52gr 4198 BTHP with IMR 4198") &&
                    l.powderManufacturer().equals("IMR") &&
                    l.powderType().equals("4198") &&
                    l.bulletManufacturer().equals("Hornady") &&
                    l.bulletType().equals("BTHP Match") &&
                    l.bulletWeight().to(GRAIN).getValue().doubleValue() == 52.0 &&
                    l.primerManufacturer().equals("Federal") &&
                    l.primerType().equals("205M") &&
                    l.distanceFromLands().to(INCH_INTERNATIONAL).getValue().doubleValue() == 0.020 &&
                    l.caseOverallLength().to(INCH_INTERNATIONAL).getValue().doubleValue() == 2.250 &&
                    l.neckTension().to(INCH_INTERNATIONAL).getValue().doubleValue() == 0.003 &&
                    l.rifleId().equals(1L))
                .verifyComplete();
    }

    @Test
    void save() {
        var ownerId = randomUUID().toString();
        
        create(loadRepository.save(createTestLoad(ownerId)))
                .expectNextMatches(l -> 
                    l.id() != null &&
                    l.ownerId().equals(ownerId) &&
                    l.name().equals(NAME))
                .verifyComplete();
    }

    @Test
    void update() {
        var ownerId = randomUUID().toString();
        var savedLoad = loadRepository.save(createTestLoad(ownerId)).block();
        
        var updatedLoad = new Load(
                savedLoad.id(),
                ownerId,
                "SMK 53 HP H335",
                "SMK 53gr HP with Hodgdon H335",
                "Hodgdon",
                "H335",
                "Sierra",
                "MatchKing HP",
                getQuantity(53, GRAIN),
                "Federal",
                "205M",
                getQuantity(0.020, INCH_INTERNATIONAL),
                getQuantity(2.260, INCH_INTERNATIONAL),
                getQuantity(0.002, INCH_INTERNATIONAL),
                1L);

        create(loadRepository.save(updatedLoad))
                .expectNextMatches(l -> 
                    l.id().equals(savedLoad.id()) && 
                    l.ownerId().equals(ownerId) &&
                    l.name().equals("SMK 53 HP H335") &&
                    l.bulletWeight().to(GRAIN).getValue().doubleValue() == 53.0)
                .verifyComplete();
    }

    @Test
    void delete() {
        var savedLoad = loadRepository.save(createTestLoad(randomUUID().toString())).block();

        create(loadRepository.deleteById(savedLoad.id())).verifyComplete();
        create(loadRepository.findById(savedLoad.id())).expectNextCount(0).verifyComplete();
    }
}