package puscas.mobilertapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Environment;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import java8.util.Optional;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * Utility class with some helper methods that need the Android {@link Context}.
 */
public final class UtilsContext {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(UtilsContext.class.getSimpleName());

    /**
     * Private constructor to avoid creating instances.
     */
    private UtilsContext() {
        throw new UnsupportedOperationException("Not implemented.");
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
    @NonNull
    public static String getSdCardPath(@NonNull final Context context) {
        logger.info("Getting SD card path.");

        final File sdCardPath = getSdCardFilePath(context);
        logger.info("Files in SD card path '" + sdCardPath + "': ");
        for(final File file : Objects.requireNonNull(sdCardPath.listFiles())) {
            logger.info("Detected path in SD Card: " + file);
        }

        if (isPathReadable(sdCardPath)) {
            return sdCardPath.getAbsolutePath();
        }

        throw new IllegalArgumentException("The SD card path '" + sdCardPath + "' can't be read.");
    }

    /**
     * Gets the path to the internal storage.
     * <br>
     * This method should get the correct path independently of the
     * device / emulator used.
     *
     * @return The path to the internal storage.
     */
    @NonNull
    public static String getInternalStoragePath() {
        logger.info("Getting internal storage path.");

        final File internalStoragePath = getInternalStorageFilePath();
        logger.info("Internal storage path: " + internalStoragePath);

        // If the internal storage path starts with '/data', then it's assumed that it's '/data/local/tmp'.
        // Because the shell scripts are already trying to copy some OBJ files to '/data/local/tmp' by
        // default as an internal storage path. But, for some reason, this path is not even readable
        // from Android API even though it was possible to create it with ADB and even change the
        // permissions of those files with ADB. That's why, it's not being called here the
        // 'UtilsContext#isPathReadable' method to verify if this fallback path its readable.
        final String internalStoragePathStr = internalStoragePath.getAbsolutePath();
        if (internalStoragePathStr.startsWith(ConstantsUI.FILE_SEPARATOR + "data")) {
            logger.info("Since the internal storage path starts with '/data', then it's assuming " +
                    "that the internal storage path is correct.");
        }
        if (isPathReadable(internalStoragePath)) {
            return internalStoragePathStr;
        }

        throw new IllegalArgumentException("The internal storage path '" + internalStoragePath + "' can't be read.");
    }

    /**
     * Gets the {@link File path} to the external SD card.
     *
     * @param context The {@link Context} of the Android system.
     * @return The {@link File path} to the SD card.
     * @implNote This method still uses the old method
     *     {@link Environment#getExternalStorageDirectory()} in order to be
     *     compatible with Android 4.1.
     */
    @SuppressLint("ObsoleteSdkInt")
    public static File getSdCardFilePath(@NonNull final Context context) {
        return Optional.ofNullable(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 ? context.getExternalFilesDirs(null) : null)
            .map(dirs -> dirs.length > 1 ? dirs[1] : dirs[0])
            .orElseGet(() -> {
                logger.info("Using the old approach to retrieve the SD Card path.");
                return Environment.getExternalStorageDirectory();
            });
    }

    /**
     * Gets the {@link File path} to the internal storage.
     *
     * @return The {@link File path} to the internal storage.
     */
    public static File getInternalStorageFilePath() {
        logger.info("Using static path to retrieve internal storage path.");
        return new File("/data/local/tmp/");
    }

    /**
     * Validates whether a {@link File path} is readable or not.
     *
     * @param file The path to a {@link File}.
     * @return {@code true} if the path is readable or {@code false} if not.
     */
    private static boolean isPathReadable(final File file) {
        final boolean readable = file.setReadable(true);
        if (!file.canRead() && !readable) {
            final File parentFile = file.getParentFile();
            if (parentFile == null) {
                logger.warning("Trying to load parent file from '" + file.getAbsolutePath() + "' path, but it's not readable.\n");
                return false;
            }
            final boolean parentReadable = parentFile.setReadable(true);
            if (!parentFile.canRead() && !parentReadable) {
                logger.warning("Trying to load parent path '" + parentFile.getAbsolutePath() + "', but it's not readable.\n");
                // Do not return false, since it is possible that the parent path is not readable.
            }
        }
        final boolean writeable = file.setWritable(true);
        if (!file.canWrite() && !writeable) {
            final File parentFile = file.getParentFile();
            if (parentFile == null) {
                logger.warning("Trying to load parent file from '" + file.getAbsolutePath() + "' path, but it's not writeable.\n");
                return false;
            }
            final boolean parentWriteable = parentFile.setWritable(true);
            if (!parentFile.canWrite() && !parentWriteable) {
                logger.warning("Trying to load parent path '" + parentFile.getAbsolutePath() + "', but it's not writeable.\n");
                // Do not return false, since Android API 24 emulator doesn't provide writeable file system.
            }
        }
        return true;
    }

    /**
     * Helper method which reads a text based asset file.
     *
     * @param context  The Android {@link Context} to read the assets from.
     * @param filePath The path to the file (relative to the asset directory).
     * @return A {@link String} containing the contents of the asset file.
     */
    @NonNull
    private static String readTextAsset(@NonNull final Context context,
                                        @NonNull final String filePath) {
        logger.info("readTextAsset");
        final AssetManager assetManager = context.getAssets();
        final String text;
        try (InputStream inputStream = assetManager.open(filePath)) {
            text = Utils.readTextFromInputStream(inputStream);
        } catch (final IOException ex) {
            UtilsLogging.logException(ex, "UtilsContext#readTextAsset");
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
            pathname -> Pattern.matches("cpu\\d+", pathname.getName()));
        return Optional.ofNullable(files).map(filesInPath -> filesInPath.length).get();
    }

    /**
     * Helper method which gets the number of available CPU cores.
     *
     * @param context The {@link Context} of the Android system.
     * @return The number of CPU cores.
     */
    @SuppressLint("ObsoleteSdkInt")
    public static int getNumOfCores(@NonNull final Context context) {
        logger.info("getNumOfCores started");
        final int cores = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN)
            ? getNumCoresOldAndroid(context)
            : Runtime.getRuntime().availableProcessors();

        final String message = "Number of cores: " + cores;
        logger.info(message);
        return cores;
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
        @NonNull final Context context,
        @NonNull final Map<Integer, String> shadersPaths) {
        logger.info("readShaders");

        final String vertexShader = readTextAsset(context,
            Objects.requireNonNull(shadersPaths.get(GLES20.GL_VERTEX_SHADER)));

        final String fragmentShader = readTextAsset(context,
            Objects.requireNonNull(shadersPaths.get(GLES20.GL_FRAGMENT_SHADER)));

        return ImmutableMap.of(
            GLES20.GL_VERTEX_SHADER, vertexShader,
            GLES20.GL_FRAGMENT_SHADER, fragmentShader
        );
    }

    /**
     * Helper method which asks the user for permission to read and write to the external SD
     * card if it doesn't have yet.
     *
     * @param activity The {@link Activity} of MobileRT.
     */
    @SuppressLint("ObsoleteSdkInt")
    public static void checksStoragePermission(@NonNull final Activity activity) {
        logger.info("checksStoragePermission");
        final String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            permissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            permissions = new String[] {};
        }
        checksAccessPermission(activity, permissions);
    }

    /**
     * Helper method which asks the user for permission to access some external
     * component if it doesn't have it yet.
     * An external component can be access to the Internet, access the external
     * SD card, bluetooth, etc.
     *
     * @param activity    The {@link Activity} of MobileRT.
     * @param permissions The permissions to ask access to.
     */
    private static void checksAccessPermission(@NonNull final Activity activity,
                                               @NonNull final String[] permissions) {
        for (final String permission : permissions) {
            final int permissionAccess = ContextCompat.checkSelfPermission(activity, permission);
            if (permissionAccess != PackageManager.PERMISSION_GRANTED) {
                final int permissionCode = 1;
                ActivityCompat.requestPermissions(activity, permissions, permissionCode);
                return;
            }
        }
    }

}
