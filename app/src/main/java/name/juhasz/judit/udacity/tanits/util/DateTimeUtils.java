package name.juhasz.judit.udacity.tanits.util;

import org.joda.time.LocalDate;

public class DateTimeUtils {
    public static LocalDate parseLocalDateOrDefault(final String localDate,
                                                    final LocalDate defaultLocalDate) {
        try {
            return new LocalDate(localDate);
        } catch (Exception e) {
            return defaultLocalDate;
        }
    }
}
