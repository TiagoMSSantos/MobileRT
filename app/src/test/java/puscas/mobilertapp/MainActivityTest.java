package puscas.mobilertapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import lombok.extern.java.Log;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for {@link MainActivity} class.
 */
@PrepareForTest({MainActivity.class, UtilsContext.class})
@Log
public final class MainActivityTest {

    /**
     * The {@link Rule} for the {@link MainActivity} for each test.
     */
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    /**
     * The mocked {@link MainActivity}.
     *
     * @implNote The usage of {@link PowerMockito#spy(java.lang.Object)} is to only mock
     * some methods and the others should be the real ones. This is necessary to mock only some
     * methods from the Android API.
     */
    MainActivity mainActivityMocked = null;

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        log.info("setUp");
        // Because of using PowerMock to mock the static initializer, then it's not necessary
        // to add the native MobileRT library to the Java library path.
        // addLibraryPath("../build_release/lib");

        mainActivityMocked = Mockito.spy(MainActivity.class);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        log.info("tearDown");
    }

    /**
     * Adds the specified path to the java library path.
     *
     * @implNote It appends a path to the `java.library.path` property by following this recipe:
     * <a href="https://fahdshariff.blogspot.com/2011/08/changing-java-library-path-at-runtime.html">fahd.blog</a>
     *
     * @param pathToAdd The path to add.
     * @throws Exception If anything goes wrong.
     * @implNote Loads the native MobileRT library to be used by the {@link MainActivity} that these
     * tests use.
     */
    private static void addLibraryPath(final String pathToAdd) throws Exception{
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);
        // Get array of paths.
        final String[] paths = (String[]) usrPathsField.get(null);
        Preconditions.checkNotNull(paths, "paths should not be null");
        // Check if the path to add is already present.
        for(final String path : paths) {
            if (Objects.equals(path, pathToAdd)) {
                return;
            }
        }
        // Add the new path.
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length-1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }

    /**
     * Tests the {@link MainActivity#showUiMessage(String)} method.
     */
    @Test
    public void testShowUiMessage() {
        // The #showUiMessage method needs #setCurrentInstance to set the #currentInstance field 1st.
        mainActivityMocked.setCurrentInstance();
        MainActivity.showUiMessage("test");

        Mockito.verify(mainActivityMocked, Mockito.times(1))
            .runOnUiThread(Mockito.any(Runnable.class));
    }

    /**
     * Tests that the {@link MainActivity#onCreate(Bundle)} method will throw an {@link Exception}
     * if the loading of native MobileRT library fails.
     */
    @Test
    public void testOnCreateFailLoadLibrary() {
        Assertions.assertThatThrownBy(() -> mainActivityMocked.onCreate(null))
            .as("The MainActivity#onCreate")
            .isInstanceOf(UnsatisfiedLinkError.class)
            .hasMessageContaining("no MobileRT in java.library.path");
    }

    /**
     * Tests that the {@link MainActivity#onActivityResult(int, int, Intent)} method sets the
     * {@link MainActivity#sceneFilePath} field when it is called by an external file manager with
     * a path to a file.
     */
    @Test
    public void testOnActivityResultSetsSceneFilePath() {
        final Intent intentMocked = Mockito.mock(Intent.class);

        final Uri uriMocked = Mockito.mock(Uri.class);
        Mockito.when(uriMocked.getPathSegments())
            .thenReturn(ImmutableList.of("document", "1CE6-261B:MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl"));
        Mockito.when(intentMocked.getData())
            .thenReturn(uriMocked);

        try (final MockedStatic<UtilsContext> utilsContextMockedStatic = Mockito.mockStatic(UtilsContext.class);
             final MockedStatic<Environment> environmentMockedStatic = Mockito.mockStatic(Environment.class)) {
            utilsContextMockedStatic.when(() -> UtilsContext.getSdCardPath(ArgumentMatchers.any()))
                .thenReturn("");
            environmentMockedStatic.when(Environment::getExternalStorageDirectory)
                .thenReturn(new File(""));

            mainActivityMocked.onActivityResult(MainActivity.OPEN_FILE_REQUEST_CODE, Activity.RESULT_OK, intentMocked);

            Assertions.assertThat((String) ReflectionTestUtils.getField(mainActivityMocked, "sceneFilePath"))
                .as("The 'MainActivity#sceneFilePath' field")
                .isEqualTo("/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water");
        }
    }

    /**
     * Tests that the {@link MainActivity#getPathFromFile(Uri)} method will get the proper path to
     * an OBJ file in the internal storage.
     */
    @Test
    public void testOnActivityResultWithInternalPath() {
        final Intent intentMocked = Mockito.mock(Intent.class);

        final Uri uriMocked = Mockito.mock(Uri.class);
        Mockito.when(uriMocked.getPathSegments())
            .thenReturn(ImmutableList.of("file", "sdcard", "MobileRT", "WavefrontOBJs", "CornellBox", "CornellBox-Water.obj"));
        Mockito.when(intentMocked.getData())
            .thenReturn(uriMocked);

        try (final MockedStatic<UtilsContext> utilsContextMockedStatic = Mockito.mockStatic(UtilsContext.class);
             final MockedStatic<Environment> environmentMockedStatic = Mockito.mockStatic(Environment.class)) {
            utilsContextMockedStatic.when(() -> UtilsContext.getInternalStoragePath(ArgumentMatchers.any()))
                .thenReturn("/mockedStorage");
            environmentMockedStatic.when(Environment::getExternalStorageDirectory)
                .thenReturn(new File("/mockedSDCard"));

            mainActivityMocked.onActivityResult(MainActivity.OPEN_FILE_REQUEST_CODE, Activity.RESULT_OK, intentMocked);

            Assertions.assertThat((String) ReflectionTestUtils.getField(mainActivityMocked, "sceneFilePath"))
                .as("The 'MainActivity#sceneFilePath' field")
                .isEqualTo("/mockedStorage/sdcard/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water");
        }
    }
}
