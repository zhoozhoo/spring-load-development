package ca.zhoozhoo.loaddev.loads.model;

public enum Unit {
    INCHES("in."),
    MILLIMETERS("mm"),
    YARDS("yd."),
    METERS("m"),
    GRAINS("gr"),
    GRAMS("g");

    private final String symbol;

    Unit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
