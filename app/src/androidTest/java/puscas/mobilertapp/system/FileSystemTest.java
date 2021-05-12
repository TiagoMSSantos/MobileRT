package puscas.mobilertapp.system;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.List;
import java8.util.stream.StreamSupport;
import lombok.extern.java.Log;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.MainActivity;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for the File system operations used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Log
public final class FileSystemTest extends AbstractTest {

    /**
     * Tests that a file in the internal storage from the Android device exists and is readable.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesExistAndReadableFromInternalStorage() {
        final String internalStorage = UtilsContext.getInternalStoragePath(this.activity);

        final List<String> paths = ImmutableList.<String>builder().add(
            internalStorage + Constants.OBJ_FILE_TEAPOT
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
                log.info("Files in directory: " + filePath);
                final String[] list = file.list();
                if (list != null) {
                    for (final String content : list) {
                        log.info(getAbsolutePath(content));
                    }
                }
                log.info("List finished.");
                Assertions.assertTrue(file.exists(), Constants.FILE_SHOULD_EXIST + ": " + filePath);
                Assertions.assertTrue(file.isDirectory(), "File should be a directory: " + filePath);
            });
    }

    /**
     * Tests that a file in the SD card device exists and is readable.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesExistAndReadableSdCard() {
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);

        final List<String> paths = ImmutableList.<String>builder().add(
            sdCardPath + ConstantsUI.FILE_SEPARATOR + Constants.OBJ_FILE_CORNELL_BOX
        ).build();
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
                Assertions.assertFalse(file.exists(), "File should not exist!");
                Assertions.assertFalse(file.canRead(), "File should not be readable!");
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
                Assertions.assertTrue(file.exists(), Constants.FILE_SHOULD_EXIST + ": " + filePath);
                Assertions.assertTrue(file.isFile(), "File should be a file: " + filePath);
                Assertions.assertTrue(file.canRead(), "File should be readable: " + filePath);
            });
    }

    /**
     * Helper method that gets the absolute path for a file.
     *
     * @param path The path to the file.
     */
    private static String getAbsolutePath(final String path) {
        final File file = new File(path);
        final String filePath = file.getAbsolutePath();
        return filePath;
    }

}
