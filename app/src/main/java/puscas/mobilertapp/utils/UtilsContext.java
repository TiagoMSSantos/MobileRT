package puscas.mobilertapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
     * A private constructor in order to prevent instantiating this helper class.
     */
    private UtilsContext() {
    }

    /**
     * Gets the path to the external SD card.
     * <br>
     * This method should get the correct path independently of the
     * device / emulator used.
     *
     * @param context The {@link Context} of the Android system.
     * @return The path to the SD card.
     * @implNote This method still uses the deprecated method
     *     {@link Environment#getExternalStorageDirectory()} in order to be
     *     compatible with Android 4.1.
     */
    @Nonnull
    public static String getSdCardPath(@Nonnull final Context context) {
        LOGGER.info("Getting SD card path.");

        // The new method to get the SD card path.
        // This method returns an array of File with null (so is not working properly yet).
        // This is why it is still needed to use the old (deprecated) approach to guarantee
        // compatibility with most Androids.
        final File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, null);

        final String sdCardPath = Optional.of(externalFilesDirs)
            .map(dirs -> dirs.length > 1 ? dirs[1] : dirs[0])
            .map(File::getAbsolutePath)
            .orElseGet(() -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    // The old (deprecated) approach to retrieve the SD Card path.
                    return Environment.getExternalStorageDirectory().getAbsolutePath();
                } else {
                    // In case using a SDK API 19+, then we just give up and throw an exception.
                    throw new Resources.NotFoundException("External SD Card path not found!");
                }
            });

        return cleanStoragePath(sdCardPath);
    }

    /**
     * Gets the path to the internal storage.
     * <br>
     * This method should get the correct path independently of the
     * device / emulator used.
     *
     * @param context The {@link Context} of the Android system.
     * @return The path to the internal storage.
     */
    @Nonnull
    public static String getInternalStoragePath(@Nonnull final Context context) {
        LOGGER.info("Getting internal storage path.");

        final File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, null);

        final String sdCardPath = Optional.of(externalFilesDirs)
            .map(dirs -> dirs[0])
            .map(File::getAbsolutePath)
            .orElseGet(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // The new method to retrieve the internal storage path.
                    return Environment.getStorageDirectory().getAbsolutePath();
                } else {
                    // In case using a SDK API < 30, then we just give up and throw an exception.
                    throw new Resources.NotFoundException("Internal storage path not found!");
                }
            });

        return cleanStoragePath(sdCardPath);
    }

    /**
     * Helper method which reads a text based asset file.
     *
     * @param context  The Android {@link Context} to read the assets from.
     * @param filePath The path to the file (relative to the asset directory).
     * @return A {@link String} containing the contents of the asset file.
     */
    @Nonnull
    private static String readTextAsset(@Nonnull final Context context,
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
     * device for devices with the SDK API version <= {@link Build.VERSION_CODES#JELLY_BEAN}.
     *
     * @param context The {@link Context} of the Android system.
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
     * @param context The {@link Context} of the Android system.
     * @return The number of CPU cores.
     */
    public static int getNumOfCores(@Nonnull final Context context) {
        final int cores = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN)
            ? getNumCoresOldAndroid(context)
            : Runtime.getRuntime().availableProcessors();

        final String message = String.format(Locale.US, "Number of cores: %d", cores);
        LOGGER.info(message);
        return cores;
    }

    /**
     * Helper method that checks if the system is a 64 device or not.
     *
     * @param context The {@link Context} of the Android system.
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
     * Helper method that cleans the path to the external SD Card or to the internal storage.
     * This is useful for some devices since the {@link #getSdCardPath(Context)}
     * method might get the SD Card path with some extra paths at the end.
     *
     * @param storagePath The path to the storage to clean.
     * @return A cleaned storage path.
     */
    @Nonnull
    private static String cleanStoragePath(@Nonnull final String storagePath) {
        final int removeIndex = storagePath.indexOf("Android");
        if (removeIndex >= 1) {
            return storagePath.substring(0, removeIndex - 1);
        }
        return storagePath;
    }

    /**
     * Load the GLSL shaders from files where the paths are given via argument.
     *
     * @param context      The {@link Context} of the Android system.
     * @param shadersPaths The paths to the GLSL shaders.
     * @return A {@link Map} with the loaded shaders.
     */
    @NonNull
    public static Map<Integer, String> readShaders(
        @Nonnull final Context context,
        @NonNull final Map<Integer, String> shadersPaths) {
        LOGGER.info("readShaders");

        final String vertexShader = UtilsContext.readTextAsset(context,
            Objects.requireNonNull(shadersPaths.get(GLES20.GL_VERTEX_SHADER)));

        final String fragmentShader = UtilsContext.readTextAsset(context,
            Objects.requireNonNull(shadersPaths.get(GLES20.GL_FRAGMENT_SHADER)));

        return ImmutableMap.of(
            GLES20.GL_VERTEX_SHADER, vertexShader,
            GLES20.GL_FRAGMENT_SHADER, fragmentShader
        );
    }

    /**
     * Helper method which asks the user for permission to read the external SD
     * card if it doesn't have yet.
     *
     * @param activity The {@link Activity} of MobileRT.
     */
    public static void checksStoragePermission(@Nonnull final Activity activity) {
        LOGGER.info("checksStoragePermission");
        checksAccessPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * Helper method which asks the user for permission to access the internet
     * if it doesn't have it yet.
     *
     * @param activity The {@link Activity} of MobileRT.
     */
    public static void checksInternetPermission(@Nonnull final Activity activity) {
        LOGGER.info("checksInternetPermission");
        checksAccessPermission(activity, Manifest.permission.INTERNET);
    }

    /**
     * Helper method which asks the user for permission to access some external
     * component if it doesn't have it yet.
     * An external component can be access to the Internet, access the external
     * SD card, bluetooth, etc.
     *
     * @param activity   The {@link Activity} of MobileRT.
     * @param permission The permission to ask access to.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private static void checksAccessPermission(@Nonnull final Activity activity,
                                               @Nonnull final String permission) {
        final int permissionCode = 1;
        final int permissionAccess = ContextCompat.checkSelfPermission(
            activity,
            permission
        );
        if (permissionAccess != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {
                permission
            };
            ActivityCompat.requestPermissions(activity, permissions, permissionCode);
        }
    }

}
