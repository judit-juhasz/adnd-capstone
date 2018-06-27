package name.juhasz.judit.udacity.tanits.util;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

import name.juhasz.judit.udacity.tanits.R;

public class ConfigurationUtils {
    public static boolean isTwoPaneMode(@NonNull final Context context) {
        final int orientation = context.getResources().getConfiguration().orientation;
        return context.getResources().getBoolean(R.bool.tablet_device) &&
                Configuration.ORIENTATION_LANDSCAPE == orientation;
    }
}
