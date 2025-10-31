package ca.zhoozhoo.loaddev.components.model;

import org.junit.jupiter.api.Test;

import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_PISTOL;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_PISTOL_MAGNUM;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_RIFLE;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.LARGE_RIFLE_MAGNUM;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.SMALL_PISTOL;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.SMALL_PISTOL_MAGNUM;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.SMALL_RIFLE;
import static ca.zhoozhoo.loaddev.components.model.PrimerSize.SMALL_RIFLE_MAGNUM;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PrimerSize} enum.
 * Tests enum values and behavior.
 *
 * @author Zhubin Salehi
 */
class PrimerSizeTest {

    @Test
    void shouldHaveSmallPistolValue() {
        assertThat(SMALL_PISTOL).isNotNull();
        assertThat(SMALL_PISTOL.name()).isEqualTo("SMALL_PISTOL");
    }

    @Test
    void shouldHaveLargePistolValue() {
        assertThat(LARGE_PISTOL).isNotNull();
        assertThat(LARGE_PISTOL.name()).isEqualTo("LARGE_PISTOL");
    }

    @Test
    void shouldHaveSmallRifleValue() {
        assertThat(SMALL_RIFLE).isNotNull();
        assertThat(SMALL_RIFLE.name()).isEqualTo("SMALL_RIFLE");
    }

    @Test
    void shouldHaveLargeRifleValue() {
        assertThat(LARGE_RIFLE).isNotNull();
        assertThat(LARGE_RIFLE.name()).isEqualTo("LARGE_RIFLE");
    }

    @Test
    void shouldHaveSmallRifleMagnumValue() {
        assertThat(SMALL_RIFLE_MAGNUM).isNotNull();
        assertThat(SMALL_RIFLE_MAGNUM.name()).isEqualTo("SMALL_RIFLE_MAGNUM");
    }

    @Test
    void shouldHaveLargeRifleMagnumValue() {
        assertThat(LARGE_RIFLE_MAGNUM).isNotNull();
        assertThat(LARGE_RIFLE_MAGNUM.name()).isEqualTo("LARGE_RIFLE_MAGNUM");
    }

    @Test
    void shouldHaveSmallPistolMagnumValue() {
        assertThat(SMALL_PISTOL_MAGNUM).isNotNull();
        assertThat(SMALL_PISTOL_MAGNUM.name()).isEqualTo("SMALL_PISTOL_MAGNUM");
    }

    @Test
    void shouldHaveLargePistolMagnumValue() {
        assertThat(LARGE_PISTOL_MAGNUM).isNotNull();
        assertThat(LARGE_PISTOL_MAGNUM.name()).isEqualTo("LARGE_PISTOL_MAGNUM");
    }

    @Test
    void shouldHaveExactlyEightValues() {
        assertThat(PrimerSize.values()).hasSize(8);
    }

    @Test
    void shouldBeAbleToRetrieveByName() {
        assertThat(PrimerSize.valueOf("SMALL_RIFLE")).isEqualTo(SMALL_RIFLE);
        assertThat(PrimerSize.valueOf("LARGE_RIFLE")).isEqualTo(LARGE_RIFLE);
        assertThat(PrimerSize.valueOf("SMALL_PISTOL")).isEqualTo(SMALL_PISTOL);
        assertThat(PrimerSize.valueOf("LARGE_PISTOL")).isEqualTo(LARGE_PISTOL);
    }

    @Test
    void shouldMaintainEnumEquality() {
        // given
        var size1 = LARGE_RIFLE;
        var size2 = LARGE_RIFLE;
        var size3 = SMALL_RIFLE;

        // then
        assertThat(size1).isEqualTo(size2);
        assertThat(size1).isNotEqualTo(size3);
    }

    @Test
    void shouldHaveConsistentHashCode() {
        // given
        var size1 = LARGE_RIFLE;
        var size2 = LARGE_RIFLE;

        // then
        assertThat(size1.hashCode()).isEqualTo(size2.hashCode());
    }

    @Test
    void shouldSupportSwitchStatements() {
        // given
        var size = LARGE_RIFLE;
        String result;

        // when
        switch (size) {
            case SMALL_PISTOL:
                result = "Small Pistol";
                break;
            case LARGE_PISTOL:
                result = "Large Pistol";
                break;
            case SMALL_RIFLE:
                result = "Small Rifle";
                break;
            case LARGE_RIFLE:
                result = "Large Rifle";
                break;
            case SMALL_RIFLE_MAGNUM:
                result = "Small Rifle Magnum";
                break;
            case LARGE_RIFLE_MAGNUM:
                result = "Large Rifle Magnum";
                break;
            case SMALL_PISTOL_MAGNUM:
                result = "Small Pistol Magnum";
                break;
            case LARGE_PISTOL_MAGNUM:
                result = "Large Pistol Magnum";
                break;
            default:
                result = "Unknown";
        }

        // then
        assertThat(result).isEqualTo("Large Rifle");
    }

    @Test
    void shouldBeComparable() {
        // Enums are comparable by their ordinal values
        assertThat(SMALL_PISTOL.compareTo(LARGE_PISTOL)).isLessThan(0);
        assertThat(LARGE_PISTOL.compareTo(SMALL_PISTOL)).isGreaterThan(0);
        assertThat(SMALL_RIFLE.compareTo(SMALL_RIFLE)).isEqualTo(0);
    }

    @Test
    void shouldHaveCorrectOrdinalValues() {
        // Verify the order matches the declaration order
        assertThat(SMALL_PISTOL.ordinal()).isEqualTo(0);
        assertThat(LARGE_PISTOL.ordinal()).isEqualTo(1);
        assertThat(SMALL_RIFLE.ordinal()).isEqualTo(2);
        assertThat(LARGE_RIFLE.ordinal()).isEqualTo(3);
        assertThat(SMALL_RIFLE_MAGNUM.ordinal()).isEqualTo(4);
        assertThat(LARGE_RIFLE_MAGNUM.ordinal()).isEqualTo(5);
        assertThat(SMALL_PISTOL_MAGNUM.ordinal()).isEqualTo(6);
        assertThat(LARGE_PISTOL_MAGNUM.ordinal()).isEqualTo(7);
    }
}
