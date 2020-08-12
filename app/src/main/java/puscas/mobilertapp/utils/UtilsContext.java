package puscas.mobilertapp.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java8.util.Optional;
import javax.annotation.Nonnull;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * Utility class with some helper methods that need the Android {@link Context}.
 */
public final class UtilsContext {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(UtilsContext.class.getName());

    /**
     * The latest version of Android API which needs the old method of getting
     * the number of CPU cores.
     */
    private static final int OLD_API_GET_CORES = 16;

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private UtilsContext() {
        LOGGER.info("UtilsContext");
    }

    /**
     * Gets the path to the SD card.
     * <br>
     * This method should get the correct path independently of the
     * device / emulator used.
     *
     * @return The path to the SD card.
     * @implNote This method still uses the deprecated method
     * {@link Environment#getExternalStorageDirectory()} in order to be
     * compatible with Android 4.1.
     */
    @Nonnull
    public static String getSDCardPath(@Nonnull final Context context) {
        LOGGER.info("Getting SD card path");
        final File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
        final String sdCardPath = Optional.ofNullable(dirs.length > 1 ? dirs[1] : dirs[0])
            .map(File::getAbsolutePath)
            .orElse(Environment.getExternalStorageDirectory().getAbsolutePath());
        return cleanSDCardPath(sdCardPath);
    }

    /**
     * Helper method which reads a text based asset file.
     *
     * @param context  The Android {@link Context} to read the assets from.
     * @param filePath The path to the file (relative to the asset directory).
     * @return A {@link String} containing the contents of the asset file.
     */
    @Nonnull
    public static String readTextAsset(@Nonnull final Context context,
                                       @Nonnull final String filePath) {
        LOGGER.info("readTextAsset");
        final AssetManager assetManager = context.getAssets();
        final String text;
        try (InputStream inputStream = assetManager.open(filePath)) {
            text = Utils.readTextFromInputStream(inputStream);
        } catch (final IOException ex) {
            throw new FailureException(ex);
        }
        return text;
    }

    /**
     * Helper method that gets the number of available CPU cores in the Android
     * device for devices with the SDK API version <= {@link #OLD_API_GET_CORES}.
     *
     * @return The number of CPU cores.
     */
    private static int getNumCoresOldAndroid(final Context context) {
        final String cpuInfoPath = readTextAsset(context,
            "Utils" + ConstantsUI.FILE_SEPARATOR + "cpuInfoDeviceSystemPath.txt");
        final File cpuTopologyPath = new File(cpuInfoPath.trim());
        final File[] files = cpuTopologyPath.listFiles(
            pathname -> Pattern.matches("cpu[0-9]+", pathname.getName()));
        return Optional.ofNullable(files).map(filesInPath -> filesInPath.length).get();
    }

    /**
     * Helper method which gets the number of available CPU cores.
     *
     * @return The number of CPU cores.
     */
    public static int getNumOfCores(@Nonnull final Context context) {
        final int cores = (Build.VERSION.SDK_INT <= OLD_API_GET_CORES)
            ? getNumCoresOldAndroid(context)
            : Runtime.getRuntime().availableProcessors();

        final String message = String.format(Locale.US, "Number of cores: %d", cores);
        LOGGER.info(message);
        return cores;
    }

    /**
     * Helper method that checks if the system is a 64 device or not.
     *
     * @return Whether the system is 64 bit.
     */
    public static boolean is64BitDevice(@Nonnull final Context context) {
        LOGGER.info("is64BitDevice");
        final String cpuInfoPath = UtilsContext.readTextAsset(context,
            "Utils" + ConstantsUI.FILE_SEPARATOR + "cpuInfoPath.txt");
        try (InputStream inputStream = new FileInputStream(cpuInfoPath.trim())) {
            final String text = Utils.readTextFromInputStream(inputStream);
            if (text.matches("64.*bit")) {
                return true;
            }
        } catch (final IOException ex) {
            throw new FailureException(ex);
        }
        return false;
    }


    // Private methods

    /**
     * Helper method that cleans the path to the external SD Card.
     * This is useful for some devices since the {@link #getSDCardPath(Context)}
     * method might get the SD Card path with some extra paths at the end.
     *
     * @param sdCardPath The path to the external SD Card to clean.
     * @return A cleaned SD Card path.
     */
    @Nonnull
    private static String cleanSDCardPath(final String sdCardPath) {
        final int removeIndex = sdCardPath.indexOf("Android");
        if (removeIndex >= 1) {
            return sdCardPath.substring(0, removeIndex - 1);
        }
        return sdCardPath;
    }

}
