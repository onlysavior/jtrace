package com.github.onlysavior.jtrace.core;

/**
 * Created by Administrator on 14-4-7.
 */
public class TraceSign {
    private static final String SPCHAR = "-";

    public static long sign(String path) {
        assert path != null : "path is null";
        String[] parts = path.split(SPCHAR);
        if (parts.length <= 1) {
            return 0L;
        }

        long sign = 0L;
        for(int index = 1; index < parts.length; index++) {
            sign += 31*sign + parts[index].hashCode();
        }

        return sign;
    }

    public static String getEntry(String path) {
        assert path != null : "path is null";
        String[] parts = path.split(SPCHAR);
        if (parts.length < 1) {
            return null;
        }
        return parts[1];
    }
}
