package puscas.mobilertapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java8.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * Utility class with some helper methods that need the Android {@link Context}.
 */
@UtilityClass
@Log
public final class UtilsContext {

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
        log.info("Getting SD card path.");

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
                    log.info("Using the old (deprecated) approach to retrieve the SD Card path.");
                    return Environment.getExternalStorageDirectory().getAbsolutePath();
                } else {
                    log.info("Using fallback path since using a SDK API 19+, and hoping this path is right.");
                    return "/mnt/sdcard";
                }
            });

        final String sdCardPathCleaned = cleanStoragePath(sdCardPath);
        final String message = "SD card path: " + sdCardPathCleaned;
        log.info(message);

        final File file = new File(sdCardPathCleaned);
        final boolean readable = file.setReadable(true, true);
        if (!file.canRead() && !readable) {
            throw new FailureException("External storage path is not readable: " + file.getAbsolutePath());
        }
        final boolean writable = file.setWritable(true, true);
        if (!file.canWrite() && !writable) {
            log.warning("External storage path is not writable: " + file.getAbsolutePath());
        }

        return sdCardPathCleaned;
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
        log.info("Getting internal storage path.");

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
        log.info(message);

        final File file = new File(internalStoragePathCleaned);
        final boolean readable = file.setReadable(true);
        if (!file.canRead() && !readable) {
            final String fallbackPath = "/data/local/tmp";
            log.warning("Internal storage path is not readable: " + file.getAbsolutePath() + "\n");
            log.warning("Will try " + fallbackPath + " instead.");
            final File fileSdCard = new File(fallbackPath);
            final boolean readableSdCard = fileSdCard.setReadable(true);
            if (!fileSdCard.canRead() && !readableSdCard) {
                final String messageError = "Internal storage path is not readable: " + fileSdCard.getAbsolutePath();
                log.warning(messageError);
            }
            final boolean writableSdCard = fileSdCard.setWritable(true);
            if (!fileSdCard.canWrite() && !writableSdCard) {
                final String messageError = "Internal storage path is not writable: " + fileSdCard.getAbsolutePath();
                log.warning(messageError);
            }
            return fileSdCard.getAbsolutePath();
        }
        final boolean writable = file.setWritable(true);
        if (!file.canWrite() && !writable) {
            throw new FailureException("Internal storage path is not writable: " + file.getAbsolutePath());
        }

        return internalStoragePathCleaned;
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
        log.info("readTextAsset");
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
    public static int getNumOfCores(@NonNull final Context context) {
        final int cores = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN)
            ? getNumCoresOldAndroid(context)
            : Runtime.getRuntime().availableProcessors();

        final String message = "Number of cores: " + cores;
        log.info(message);
        return cores;
    }

    /**
     * Helper method that checks if the system is a 64 device or not.
     *
     * @param context The {@link Context} of the Android system.
     * @return Whether the system is 64 bit.
     */
    public static boolean is64BitDevice(@NonNull final Context context) {
        log.info("is64BitDevice");
        final String text = readTextAsset(context, "Utils" + ConstantsUI.FILE_SEPARATOR +  "cpuInfoPath.txt");
        return text.matches("64.*bit");
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
    private static String cleanStoragePath(@NonNull final String storagePath) {
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
        log.info("readShaders");

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
        log.info("checksStoragePermission");
        checksAccessPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        checksAccessPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Helper method which asks the user for permission to access the internet
     * if it doesn't have it yet.
     *
     * @param activity The {@link Activity} of MobileRT.
     */
    public static void checksInternetPermission(@NonNull final Activity activity) {
        log.info("checksInternetPermission");
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
    private static void checksAccessPermission(@NonNull final Activity activity,
                                               @NonNull final String permission) {
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
