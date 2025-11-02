package ca.zhoozhoo.loaddev.loads.model;

import static ca.zhoozhoo.loaddev.loads.model.Load.IMPERIAL;
import static java.time.LocalDate.now;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for model classes testing equals, hashCode, and constructors.
 *
 * @author Zhubin Salehi
 */
class ModelTests {

    @Test
    void load_equals_shouldHandleSameObject() {
        var load = new Load(1L, "user1", "Test Load", "Description", IMPERIAL,
                "PowderMfg", "PowderType", "BulletMfg", "BulletType", 150.0,
                "PrimerMfg", "PrimerType", 0.020, null, null, 1L);
        
        assertTrue(load.equals(load));
    }

    @Test
    void load_equals_shouldHandleNull() {
        assertFalse(new Load(1L, "user1", "Test Load", "Description", IMPERIAL,
                "PowderMfg", "PowderType", "BulletMfg", "BulletType", 150.0,
                "PrimerMfg", "PrimerType", 0.020, null, null, 1L).equals(null));
    }

    @Test
    void load_equals_shouldHandleDifferentClass() {
        assertFalse(new Load(1L, "user1", "Test Load", "Description", IMPERIAL,
                "PowderMfg", "PowderType", "BulletMfg", "BulletType", 150.0,
                "PrimerMfg", "PrimerType", 0.020, null, null, 1L).equals("Not a Load"));
    }

    @Test
    void load_equals_shouldHandleEqualObjects() {
        var load1 = new Load(1L, "user1", "Test Load", "Description", IMPERIAL,
                "PowderMfg", "PowderType", "BulletMfg", "BulletType", 150.0,
                "PrimerMfg", "PrimerType", 0.020, 2.260, 0.002, 1L);
        var load2 = new Load(1L, "user1", "Test Load", "Description", IMPERIAL,
                "PowderMfg", "PowderType", "BulletMfg", "BulletType", 150.0,
                "PrimerMfg", "PrimerType", 0.020, 2.260, 0.002, 1L);
        
        assertTrue(load1.equals(load2));
        assertEquals(load1.hashCode(), load2.hashCode());
    }

    @Test
    void load_equals_shouldHandleDifferentIds() {
        var load1 = new Load(1L, "user1", "Test Load", "Description", IMPERIAL,
                "PowderMfg", "PowderType", "BulletMfg", "BulletType", 150.0,
                "PrimerMfg", "PrimerType", 0.020, null, null, 1L);
        var load2 = new Load(2L, "user1", "Test Load", "Description", IMPERIAL,
                "PowderMfg", "PowderType", "BulletMfg", "BulletType", 150.0,
                "PrimerMfg", "PrimerType", 0.020, null, null, 1L);
        
        assertFalse(load1.equals(load2));
    }

    @Test
    void load_hashCode_shouldBeConsistent() {
        var load = new Load(1L, "user1", "Test Load", "Description", IMPERIAL,
                "PowderMfg", "PowderType", "BulletMfg", "BulletType", 150.0,
                "PrimerMfg", "PrimerType", 0.020, null, null, 1L);
        
        assertEquals(load.hashCode(), load.hashCode());
    }

    @Test
    void group_equals_shouldHandleSameObject() {
        var group = new Group(1L, "user1", 1L, now(), 40.5, 100, 1.5);
        
        assertTrue(group.equals(group));
    }

    @Test
    void group_equals_shouldHandleNull() {
        assertFalse(new Group(1L, "user1", 1L, now(), 40.5, 100, 1.5).equals(null));
    }

    @Test
    void group_equals_shouldHandleDifferentClass() {
        assertFalse(new Group(1L, "user1", 1L, now(), 40.5, 100, 1.5).equals("Not a Group"));
    }

    @Test
    void group_equals_shouldHandleEqualObjects() {
        var date = now();
        var group1 = new Group(1L, "user1", 1L, date, 40.5, 100, 1.5);
        var group2 = new Group(1L, "user1", 1L, date, 40.5, 100, 1.5);
        
        assertTrue(group1.equals(group2));
        assertEquals(group1.hashCode(), group2.hashCode());
    }

    @Test
    void group_equals_shouldHandleDifferentIds() {
        var date = now();
        var group1 = new Group(1L, "user1", 1L, date, 40.5, 100, 1.5);
        var group2 = new Group(2L, "user1", 1L, date, 40.5, 100, 1.5);
        
        assertFalse(group1.equals(group2));
    }

    @Test
    void group_hashCode_shouldBeConsistent() {
        var group = new Group(1L, "user1", 1L, now(), 40.5, 100, 1.5);
        
        assertEquals(group.hashCode(), group.hashCode());
    }

    @Test
    void shot_equals_shouldHandleSameObject() {
        var shot = new Shot(1L, "user1", 1L, 2850);
        
        assertTrue(shot.equals(shot));
    }

    @Test
    void shot_equals_shouldHandleNull() {
        assertFalse(new Shot(1L, "user1", 1L, 2850).equals(null));
    }

    @Test
    void shot_equals_shouldHandleDifferentClass() {
        assertFalse(new Shot(1L, "user1", 1L, 2850).equals("Not a Shot"));
    }

    @Test
    void shot_equals_shouldHandleEqualObjects() {
        var shot1 = new Shot(1L, "user1", 1L, 2850);
        var shot2 = new Shot(1L, "user1", 1L, 2850);
        
        assertTrue(shot1.equals(shot2));
        assertEquals(shot1.hashCode(), shot2.hashCode());
    }

    @Test
    void shot_equals_shouldHandleDifferentIds() {
        var shot1 = new Shot(1L, "user1", 1L, 2850);
        var shot2 = new Shot(2L, "user1", 1L, 2850);
        
        assertFalse(shot1.equals(shot2));
    }

    @Test
    void shot_hashCode_shouldBeConsistent() {
        var shot = new Shot(1L, "user1", 1L, 2850);
        
        assertEquals(shot.hashCode(), shot.hashCode());
    }

    @Test
    void groupStatistics_shouldCreateCorrectly() {
        var stats = new GroupStatistics(new Group(1L, "user1", 1L, now(), 40.5, 100, 1.5), 
                2850.0, 25.5, 75.0, of());
        
        assertNotNull(stats);
        assertEquals(2850.0, stats.averageVelocity());
        assertEquals(25.5, stats.standardDeviation());
        assertEquals(75.0, stats.extremeSpread());
    }
}
