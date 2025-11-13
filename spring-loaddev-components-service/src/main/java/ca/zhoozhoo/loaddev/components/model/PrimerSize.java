package ca.zhoozhoo.loaddev.components.model;

/**
 * Enumeration of primer sizes used in ammunition reloading.
 * <p>
 * Primer sizes correspond to the primer pocket dimensions in cartridge cases and must match
 * both the case specifications and the propellent charge requirements. Different primer sizes
 * provide varying ignition characteristics suitable for different cartridge types and propellent loads.
 * </p>
 *
 * @author Zhubin Salehi
 */
public enum PrimerSize {
    /** Small primer for pistol cartridges */
    SMALL_PISTOL,
    /** Large primer for pistol cartridges */
    LARGE_PISTOL,
    /** Small primer for rifle cartridges */
    SMALL_RIFLE,
    /** Large primer for rifle cartridges */
    LARGE_RIFLE,
    /** Small magnum primer for rifle cartridges with heavy propellent charges */
    SMALL_RIFLE_MAGNUM,
    /** Large magnum primer for rifle cartridges with heavy propellent charges */
    LARGE_RIFLE_MAGNUM,
    /** Small magnum primer for pistol cartridges */
    SMALL_PISTOL_MAGNUM,
    /** Large magnum primer for pistol cartridges */
    LARGE_PISTOL_MAGNUM
}
