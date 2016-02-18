package lombok;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author https://github.com/lexfaraday
 *
 */
public final class VersionableUtils {

    public static final String VERSIONABLE_KEY = "VERSIONABLE_KEY";

    /**
     * Check if Version, used by @SetterVersionable or @DataVersionable 
     * @param version
     * @param versionsToCheck
     * @return
     */
    public static boolean isAllowedForThisVersion(Version... versions) {
        boolean allowed = false;
        if (versions != null) {
            Version currentVersion = VersionableUtils.getCurrentVersion();
            if (currentVersion != null) {
                List<Version> availableVersions = Arrays.asList(versions);
                for (int i = 0; i < availableVersions.size() && !allowed; i++) {
                    allowed = currentVersion.name().equals(availableVersions.get(i).name());
                }
            }
        }
        return allowed;
    }

    /**
     * Used by @Versionable
     * @param version
     * @return
     */
    public static Version resolveVersion(String version) {
        if (version != null) {
            try {
                return Version.valueOf(version);
            } catch (Exception e) {
                System.err.println("Version value not found for: " + version + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Get current version for this context
     * @return
     */
    public static Version getCurrentVersion() {
        Object value = org.apache.log4j.MDC.get(VERSIONABLE_KEY);
        return value != null ? (Version) value : null;
    }
}
