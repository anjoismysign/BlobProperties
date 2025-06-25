package blobproperties.numberformats;

import java.util.Optional;

public interface Formatter {

    String format(double amount, Optional<String> currency);

    default String format(double amount) {
        return format(amount, Optional.empty());
    }
}
