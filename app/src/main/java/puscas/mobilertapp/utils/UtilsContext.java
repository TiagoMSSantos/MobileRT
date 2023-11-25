package puscas.mobilertapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Environment;

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
                    logger.info("Using the old (deprecated) approach to retrieve the SD Card path.");
                    return Environment.getExternalStorageDirectory().getAbsolutePath();
                } else {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        logger.info("Using the new approach to retrieve the SD Card path.");
                        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    }
                    logger.info("Using fallback path since using a SDK API 19+, and hoping this path is right.");
                    return "/mnt/sdcard";
                }
            });

        final String sdCardPathCleaned = cleanStoragePath(sdCardPath);
        final String message = "SD card path: " + sdCardPathCleaned;
        logger.info(message);

        final File file = new File(sdCardPathCleaned);
        if (isPathReadable(file)) {
            return file.getAbsolutePath();
        }

        throw new FailureException("The SD card path '" + file.getAbsolutePath() + "' can't be read.");
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
    @NonNull
    public static String getInternalStoragePath(@NonNull final Context context) {
        logger.info("Getting internal storage path.");

        final File dataDir = ContextCompat.getDataDir(context);

        final String internalStoragePath = Optional.ofNullable(dataDir)
            .map(File::getAbsolutePath)
            .orElseGet(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // The new method to retrieve the internal storage path.
                    return Environment.getStorageDirectory().getAbsolutePath();
                } else {
                    // In case using a SDK API < 30, then just give up and hope this path is right.
                    return context.getFilesDir().getPath();
                }
            });

        final String internalStoragePathCleaned = cleanStoragePath(internalStoragePath);
        final String message = "Internal storage path: " + internalStoragePathCleaned;
        logger.info(message);

        // If the internal storage path starts with '/data', then it's assumed that it's '/data/local/tmp'.
        // Because the shell scripts are already trying to copy some OBJ files to '/data/local/tmp' by
        // default as an internal storage path. But, for some reason, this path is not even readable
        // from Android API even though it was possible to create it with ADB and even change the
        // permissions of those files with ADB. That's why, it's not being called here the
        // 'UtilsContext#isPathReadable' method to verify if this fallback path its readable.
        if (internalStoragePathCleaned.startsWith("/data")) {
            logger.info("Since the internal storage path starts with '/data', then it's assuming " +
                    "that the internal storage path is '/data/local/tmp/'.");
            return "/data/local/tmp/";
        }
        final File file = new File(internalStoragePathCleaned);
        if (isPathReadable(file)) {
            return file.getAbsolutePath();
        }

        throw new FailureException("The internal storage path '" + file.getAbsolutePath() + "' can't be read.");
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
                logger.warning("Trying to load file from '" + file.getAbsolutePath() + "' path, but it's not readable.\n");
                return false;
            }
            final boolean parentReadable = parentFile.setReadable(true);
            if (!parentFile.canRead() && !parentReadable) {
                logger.warning("Trying to load file from '" + parentFile.getAbsolutePath() + "' path, but it's not readable.\n");
                return false;
            }
        }
        final boolean writeable = file.setWritable(true);
        if (!file.canWrite() && !writeable) {
            final File parentFile = file.getParentFile();
            if (parentFile == null) {
                logger.warning("Trying to load file from '" + file.getAbsolutePath() + "' path, but it's not writeable.\n");
                return false;
            }
            final boolean parentWriteable = parentFile.setWritable(true);
            if (!parentFile.canWrite() && !parentWriteable) {
                logger.warning("Trying to load file from '" + parentFile.getAbsolutePath() + "' path, but it's not writeable.\n");
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
            UtilsLogging.logThrowable(ex, "UtilsContext#readTextAsset");
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
    public static int getNumOfCores(@NonNull final Context context) {
        final int cores = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN)
            ? getNumCoresOldAndroid(context)
            : Runtime.getRuntime().availableProcessors();

        final String message = "Number of cores: " + cores;
        logger.info(message);
        return cores;
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
    @NonNull
    public static String cleanStoragePath(@NonNull final String storagePath) {
        String storagePathCleaned = storagePath;

        // Remove Android path
        final int removeIndexAndroid = storagePathCleaned.indexOf("Android");
        if (removeIndexAndroid >= 1) {
            storagePathCleaned = storagePathCleaned.substring(0, removeIndexAndroid - 1);
        }

        // Remove data path
        final int removeIndexData = storagePathCleaned.indexOf("data/puscas.mobilertapp");
        if (removeIndexData >= 1) {
            storagePathCleaned = storagePathCleaned.substring(0, removeIndexData - 1);
        }

        // Remove user path
        final int removeIndexUser = storagePathCleaned.indexOf("user/0/puscas.mobilertapp");
        if (removeIndexUser >= 1) {
            storagePathCleaned = storagePathCleaned.substring(0, removeIndexUser - 1);
        }

        // Remove document raw path
        final int removeDocumentRaw = storagePathCleaned.indexOf("/document/raw:");
        if (removeDocumentRaw == 0) {
            storagePathCleaned = storagePathCleaned.substring(14);
        }

        // Remove path starting with '/file/'
        final int removeFileType = storagePathCleaned.indexOf("/file/");
        if (removeFileType == 0) {
            storagePathCleaned = storagePathCleaned.substring(5);
        }

        // Remove path ending with '/Download'
        final boolean removeDownloadPath = storagePathCleaned.endsWith("/Download");
        if (removeDownloadPath) {
            storagePathCleaned = storagePathCleaned.substring(0, storagePathCleaned.length() - 9);
        }

        return storagePathCleaned;
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
    public static void checksStoragePermission(@NonNull final Activity activity) {
        logger.info("checksStoragePermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // TODO: Necessary to fix granting permissions for Android API 33.
            checksAccessPermission(activity, Manifest.permission.READ_MEDIA_IMAGES);
            checksAccessPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            checksAccessPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        // TODO: Can request only one set of permissions at a time.
        // checksAccessPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
    private static void checksAccessPermission(@NonNull final Activity activity,
                                               @NonNull final String permission) {
        final int permissionAccess = ContextCompat.checkSelfPermission(
            activity,
            permission
        );
        if (permissionAccess != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {
                permission
            };
            final int permissionCode = 1;
            ActivityCompat.requestPermissions(activity, permissions, permissionCode);
        }
    }

}
