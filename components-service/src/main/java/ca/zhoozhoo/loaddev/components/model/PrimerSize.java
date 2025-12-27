package ca.zhoozhoo.loaddev.components.model;

/**
 * Primer sizes matching cartridge case pocket dimensions and propellant requirements.
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
