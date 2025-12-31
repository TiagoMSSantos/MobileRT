package puscas.mobilertapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.exceptions.FailureException;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for {@link MainActivity} class.
 */
// Annotations necessary for PowerMock to be able to mock final classes, and static and native methods.
@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({MainActivity.class, DrawView.class, UtilsContext.class, Environment.class})
public final class MainActivityTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(MainActivityTest.class.getSimpleName());

    /**
     * The partial mocked {@link MainActivity} to be used by the tests.
     */
    private MainActivity targetMainActivityMocked = null;

    /**
     * The expected internal storage path.
     */
    private static final String PATH_TO_INTERNAL_STORAGE = ConstantsUI.FILE_SEPARATOR + "data" +
        ConstantsUI.FILE_SEPARATOR + "local" + ConstantsUI.FILE_SEPARATOR + "tmp";

    /**
     * The expected external storage path.
     */
    private static final String PATH_TO_EXTERNAL_STORAGE = ConstantsUI.FILE_SEPARATOR + "mnt" +
        ConstantsUI.FILE_SEPARATOR + "sdcard";

    /**
     * The expected internal storage path to read the OBJ file used by these tests.
     */
    private static final String PATH_TO_OBJ_FILE = PATH_TO_INTERNAL_STORAGE +
        ConstantsUI.FILE_SEPARATOR + "MobileRT" +
        ConstantsUI.FILE_SEPARATOR + "WavefrontOBJs" + ConstantsUI.FILE_SEPARATOR + "CornellBox" +
        ConstantsUI.FILE_SEPARATOR + "CornellBox-Water.obj";

    /**
     * Setup method called before each test.
     * <p>
     * Sets the {@link #targetMainActivityMocked}.
     * The {@link MainActivity#showUiMessage(String)} method needs {@link MainActivity#setCurrentInstance()}
     * to be called so it sets the {@link MainActivity#currentInstance} field 1st.
     *
     * @implNote The usage of {@link PowerMock#createNicePartialMockAndInvokeDefaultConstructor(Class, String...)}
     * used to setup the mock of {@link #targetMainActivityMocked} is to only mock some methods and
     * the others should be the real ones.<br>
     * This is necessary to mock only some methods from the Android API.
     */
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        logger.info("setUp");

        this.targetMainActivityMocked = PowerMock.createNicePartialMockAndInvokeDefaultConstructor(MainActivity.class,
            "runOnUiThread", "loadMobileRT", "validateViews", "findViewById", "initializePickerThreads", "initializePickerResolutions", "initializeCheckBoxRasterize"
        );

        Assertions.assertThat(this.targetMainActivityMocked)
            .as("The target MainActivity")
            .isNotNull();

        PowerMock.mockStaticPartialNice(MainActivity.class, "loadMobileRT", "checksOpenGlVersion", "initializePicker");
        PowerMock.expectPrivate(MainActivity.class, "loadMobileRT").andVoid().anyTimes();
        PowerMock.expectPrivate(MainActivity.class, "checksOpenGlVersion", EasyMock.anyObject(ActivityManager.class)).andVoid().anyTimes();
        PowerMock.expectPrivate(this.targetMainActivityMocked, "validateViews").andVoid().anyTimes();
        PowerMock.expectPrivate(this.targetMainActivityMocked, "initializePickerThreads", EasyMock.anyInt()).andVoid().anyTimes();

        EasyMock.expect(this.targetMainActivityMocked.findViewById(R.id.renderButton))
            .andReturn(EasyMock.mock(Button.class))
            .anyTimes();
        EasyMock.expect(this.targetMainActivityMocked.findViewById(R.id.drawLayout))
            .andReturn(EasyMock.mock(DrawView.class))
            .anyTimes();

        PowerMock.mockStaticNice(UtilsContext.class);

        EasyMock.expect(UtilsContext.readShaders(EasyMock.anyObject(Context.class), EasyMock.anyObject(Map.class)))
            .andReturn(EasyMock.mock(Map.class))
            .anyTimes();

        PowerMock.replayAll();
        PowerMock.replay(this.targetMainActivityMocked);
        this.targetMainActivityMocked.onCreate(null);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        logger.info("tearDown");

        PowerMock.verify(this.targetMainActivityMocked);
        PowerMock.verifyAll();
    }

    /**
     * Tests the {@link MainActivity#showUiMessage(String)} method.
     */
    @Test
    public void testShowUiMessage() {
        EasyMock.reset(this.targetMainActivityMocked);
        this.targetMainActivityMocked.runOnUiThread(EasyMock.anyObject(Runnable.class));
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(this.targetMainActivityMocked);
        PowerMock.replayAll();

        Assertions.assertThatCode(() -> MainActivity.showUiMessage("test"))
            .as("Call to 'MainActivity#showUiMessage'")
            .doesNotThrowAnyException();

        EasyMock.verify(this.targetMainActivityMocked);
    }

    /**
     * Tests that the {@link MainActivity#onCreate(Bundle)} method will throw an {@link Exception}
     * if the loading of native MobileRT library fails.
     */
    @Test
    public void testOnCreateFailLoadLibrary() throws Exception {
        PowerMock.reset(MainActivity.class);
        final FailureException failureException = new FailureException("Test");
        PowerMock.expectPrivate(MainActivity.class, "loadMobileRT")
            .andThrow(failureException)
            .anyTimes();

        PowerMock.replayAll();
        Assertions.assertThatThrownBy(() -> this.targetMainActivityMocked.onCreate(null))
            .as("The MainActivity#onCreate")
            .extracting(Throwable::getCause)
            .isInstanceOf(failureException.getClass())
            .isEqualTo(failureException);
    }

    /**
     * Tests that the {@link MainActivity#onActivityResult(int, int, Intent)} method sets the
     * {@link MainActivity#sceneFilePath} field when it is called by an external file manager with
     * a path to a file.
     */
    @Test
    public void testOnActivityResultSetsSceneFilePath() {
        final Intent intentMocked = EasyMock.mock(Intent.class);

        final Uri uriMocked = EasyMock.mock(Uri.class);
        EasyMock.expect(uriMocked.getPathSegments())
            .andReturn(List.of("data", "local", "tmp", "MobileRT", "WavefrontOBJs", "CornellBox", "CornellBox-Water.obj"))
            .anyTimes();
        EasyMock.expect(uriMocked.getPath())
            .andReturn(PATH_TO_OBJ_FILE)
            .anyTimes();
        EasyMock.expect(intentMocked.getData())
            .andReturn(uriMocked)
            .anyTimes();
        EasyMock.replay(intentMocked, uriMocked);

        PowerMock.mockStatic(UtilsContext.class);
        EasyMock.expect(UtilsContext.getInternalStorageFilePath())
            .andReturn(new File (PATH_TO_INTERNAL_STORAGE))
            .anyTimes();
        EasyMock.expect(UtilsContext.getSdCardFilePath(EasyMock.anyObject(Context.class)))
            .andReturn(new File(PATH_TO_EXTERNAL_STORAGE))
            .anyTimes();
        EasyMock.expect(UtilsContext.getInternalStoragePath())
            .andReturn(PATH_TO_INTERNAL_STORAGE)
            .anyTimes();
        EasyMock.expect(UtilsContext.getSdCardPath(EasyMock.anyObject(Context.class)))
            .andReturn(PATH_TO_EXTERNAL_STORAGE)
            .anyTimes();

        PowerMock.mockStatic(Environment.class);
        EasyMock.expect(Environment.getExternalStorageDirectory())
            .andReturn(new File(PATH_TO_EXTERNAL_STORAGE))
            .anyTimes();
        EasyMock.expect(Environment.getDataDirectory())
            .andReturn(new File(PATH_TO_INTERNAL_STORAGE))
            .anyTimes();
        PowerMock.replayAll();

        Assertions.assertThat((String) ReflectionTestUtils.getField(this.targetMainActivityMocked, "sceneFilePath"))
            .as("The 'MainActivity#sceneFilePath' field")
            .isNull();

        final int openFileRequestCode = (int) Objects.requireNonNull(ReflectionTestUtils.getField(MainActivity.class, "OPEN_FILE_REQUEST_CODE"));
        this.targetMainActivityMocked.onActivityResult(openFileRequestCode, Activity.RESULT_OK, intentMocked);

        Assertions.assertThat((String) ReflectionTestUtils.getField(this.targetMainActivityMocked, "sceneFilePath"))
            .as("The 'MainActivity#sceneFilePath' field")
            .isEqualTo(PATH_TO_OBJ_FILE);
    }

    /**
     * Tests that the {@link MainActivity#getPathFromFile(Uri)} method will get the proper path to
     * an OBJ file in the internal storage.
     */
    @Test
    public void testOnActivityResultWithInternalPath() {
        final Intent intentMocked = EasyMock.mock(Intent.class);

        final Uri uriMocked = EasyMock.mock(Uri.class);
        EasyMock.expect(uriMocked.getPathSegments())
            .andReturn(List.of("data", "local", "tmp", "MobileRT", "WavefrontOBJs", "CornellBox", "CornellBox-Water.obj"))
            .anyTimes();
        EasyMock.expect(uriMocked.getPath())
            .andReturn(PATH_TO_OBJ_FILE)
            .anyTimes();
        EasyMock.expect(intentMocked.getData())
            .andReturn(uriMocked)
            .anyTimes();
        final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item("a"));
        EasyMock.expect(intentMocked.getClipData())
            .andReturn(clipData)
            .anyTimes();
        EasyMock.replay(intentMocked, uriMocked);

        PowerMock.mockStatic(UtilsContext.class);
        EasyMock.expect(UtilsContext.getInternalStorageFilePath())
            .andReturn(new File (PATH_TO_INTERNAL_STORAGE))
            .anyTimes();
        EasyMock.expect(UtilsContext.getSdCardFilePath(EasyMock.anyObject(Context.class)))
            .andReturn(new File(PATH_TO_EXTERNAL_STORAGE))
            .anyTimes();
        EasyMock.expect(UtilsContext.getInternalStoragePath())
            .andReturn(PATH_TO_INTERNAL_STORAGE)
            .anyTimes();
        EasyMock.expect(UtilsContext.getSdCardPath(EasyMock.anyObject(Context.class)))
            .andReturn(PATH_TO_EXTERNAL_STORAGE)
            .anyTimes();
        PowerMock.mockStatic(Environment.class);
        EasyMock.expect(Environment.getExternalStorageDirectory())
            .andReturn(new File(PATH_TO_EXTERNAL_STORAGE))
            .anyTimes();
        EasyMock.expect(Environment.getDataDirectory())
            .andReturn(new File(PATH_TO_INTERNAL_STORAGE))
            .anyTimes();
        PowerMock.replayAll();

        Assertions.assertThat((String) ReflectionTestUtils.getField(this.targetMainActivityMocked, "sceneFilePath"))
            .as("The 'MainActivity#sceneFilePath' field")
            .isNull();

        final int openFileRequestCode = (int) Objects.requireNonNull(ReflectionTestUtils.getField(MainActivity.class, "OPEN_FILE_REQUEST_CODE"));
        this.targetMainActivityMocked.onActivityResult(openFileRequestCode, Activity.RESULT_OK, intentMocked);

        Assertions.assertThat((String) ReflectionTestUtils.getField(this.targetMainActivityMocked, "sceneFilePath"))
            .as("The 'MainActivity#sceneFilePath' field")
            .isEqualTo(PATH_TO_OBJ_FILE);
    }
}
