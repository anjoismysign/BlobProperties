package io.github.anjoismysign.blobproperties.numberformat;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Optional;

public class BlobTycoon implements Formatter {

    private NumberFormat numberFormat;

    public BlobTycoon() {
        numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        numberFormat.setRoundingMode(RoundingMode.FLOOR);
        numberFormat.setMaximumFractionDigits(5);
    }

    public String format(double amount, Optional<String> currency) {
        return numberFormat.format(amount).replace("\u00A0", ",");
    }
}
