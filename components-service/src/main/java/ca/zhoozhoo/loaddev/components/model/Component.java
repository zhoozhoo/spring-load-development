package ca.zhoozhoo.loaddev.components.model;

import javax.money.MonetaryAmount;

public sealed interface Component
        permits Projectile, Propellant, Primer, Case {

    String manufacturer();

    MonetaryAmount cost();
}
