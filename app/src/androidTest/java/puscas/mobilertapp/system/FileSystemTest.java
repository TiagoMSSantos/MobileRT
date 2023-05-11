package puscas.mobilertapp.system;

import android.os.Build;

import com.google.common.collect.ImmutableList;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import java8.util.stream.StreamSupport;
import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.MainActivity;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for the File system operations used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class FileSystemTest extends AbstractTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(FileSystemTest.class.getSimpleName());

    /**
     * Tests that a file in the internal storage from the Android device exists and is readable.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesExistAndReadableFromInternalStorage() {
        final String internalStorage = UtilsContext.getInternalStoragePath(this.activity);

        final List<String> paths = ImmutableList.<String>builder().add(
            internalStorage + Constants.OBJ_FILE_CORNELL_BOX
        ).build();

        validatePathsExist(paths);
    }

    /**
     * Tests that the SD card device exists and is readable.
     */
    @Test(timeout = 5L * 1000L)
    public void testReadableSdCard() {
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);

        final List<String> paths = ImmutableList.<String>builder().add(
            "/mnt",
            sdCardPath,
            sdCardPath + "/MobileRT"
        ).build();
        StreamSupport.stream(paths)
            .forEach(path -> {
                final File file = new File(path);
                final String filePath = getAbsolutePath(path);
                logger.info("Files in directory: " + filePath);
                final String[] list = file.list();
                if (list != null) {
                    for (final String content : list) {
                        logger.info(getAbsolutePath(content));
                    }
                }
                logger.info("List finished.");

                Assert.assertTrue(Constants.FILE_SHOULD_EXIST + ": " + filePath, file.exists());
                Assert.assertTrue("File should be a directory: " + filePath, file.isDirectory());
            });
    }

    /**
     * Tests that a file in the SD card device exists and is readable.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesExistAndReadableSdCard() {
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);

        final List<String> paths = ImmutableList.<String>builder().add(
            sdCardPath + ConstantsUI.FILE_SEPARATOR + Constants.OBJ_FILE_TEAPOT
        ).build();

        // TODO: Necessary to find a way to give permissions for Android API 30+ to read files from SD Card for the test.
        Assume.assumeTrue(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q);
        validatePathsExist(paths);
    }

    /**
     * Tests that a file does not exist in the Android device.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesNotExist() {
        final String internalStorage = UtilsContext.getInternalStoragePath(this.activity);

        final List<String> paths = ImmutableList.<String>builder().add(
            Constants.EMPTY_FILE,
            internalStorage + Constants.OBJ_FILE_NOT_EXISTS
        ).build();

        validatePathsNotExist(paths);
    }

    /**
     * Tests that a file does not exist in the SD card device.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesNotExistSdCard() {
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);

        final List<String> paths = ImmutableList.<String>builder().add(
            sdCardPath + ConstantsUI.FILE_SEPARATOR + Constants.OBJ_FILE_NOT_EXISTS_SD_CARD
        ).build();

        validatePathsNotExist(paths);
    }

    /**
     * Helper method that validates that a {@link List} of paths are for {@link File}s that do NOT exist and can't be
     * read.
     *
     * @param paths The paths to validate.
     */
    private static void validatePathsNotExist(final List<String> paths) {
        StreamSupport.stream(paths)
            .forEach(path -> {
                final File file = new File(path);
                Assert.assertFalse("File should not exist!", file.exists());
                Assert.assertFalse("File should not be readable!", file.canRead());
            });
    }

    /**
     * Helper method that validates that a {@link List} of paths are for {@link File}s that exist and can be read.
     *
     * @param paths The paths to validate.
     */
    private static void validatePathsExist(final List<String> paths) {
        StreamSupport.stream(paths)
            .forEach(path -> {
                final File file = new File(path);
                final String filePath = file.getAbsolutePath();
                Assert.assertTrue(Constants.FILE_SHOULD_EXIST + ": " + filePath, file.exists());
                Assert.assertTrue("File should be a file: " + filePath, file.isFile());
                Assert.assertTrue("File should be readable: " + filePath, file.canRead());
            });
    }

    /**
     * Helper method that gets the absolute path for a file.
     *
     * @param path The path to the file.
     */
    private static String getAbsolutePath(final String path) {
        final File file = new File(path);
        return file.getAbsolutePath();
    }

}
