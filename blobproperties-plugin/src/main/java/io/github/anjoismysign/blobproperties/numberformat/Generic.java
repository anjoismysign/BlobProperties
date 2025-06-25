package io.github.anjoismysign.blobproperties.numberformat;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Optional;

public class Generic implements Formatter {

    private NumberFormat numberFormat;

    public Generic() {
        numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        numberFormat.setRoundingMode(RoundingMode.FLOOR);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
    }

    public String format(double amount, Optional<String> currency) {
        return numberFormat.format(amount).replace("\u00A0", ",");
    }
}
