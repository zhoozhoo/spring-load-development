package ca.zhoozhoo.loaddev.rifles.model;

import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
@ReadingConverter
public enum Unit {
    INCHES("in"),
    MILLIMETERS("mm"),
    YARDS("yd"),
    FEET("ft"),
    METERS("m"),
    GRAINS("gr"),
    GRAMS("g"),
    FEET_PER_SECOND("fps"),
    METERS_PER_SECOND("mps");

    private final String value;

    Unit(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
