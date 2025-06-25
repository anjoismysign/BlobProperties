package io.github.anjoismysign.blobproperties.numberformats;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Optional;

public class Integers implements Formatter {

    private NumberFormat numberFormat;

    public Integers() {
        numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        numberFormat.setRoundingMode(RoundingMode.FLOOR);
        numberFormat.setMaximumFractionDigits(0);
    }

    public String format(double amount, Optional<String> currency) {
        return numberFormat.format(amount).replace("\u00A0", ",");
    }
}
