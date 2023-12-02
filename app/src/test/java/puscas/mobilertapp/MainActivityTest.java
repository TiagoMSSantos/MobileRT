package puscas.mobilertapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.google.common.collect.ImmutableList;

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
import java.util.logging.Logger;

import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for {@link MainActivity} class.
 */
// Annotations necessary for PowerMock to be able to mock final classes and static methods.
@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({MainActivity.class, UtilsContext.class, Environment.class})
public final class MainActivityTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(MainActivityTest.class.getSimpleName());

    /**
     * The mocked {@link MainActivity}.
     *
     * @implNote The usage of {@link EasyMock#partialMockBuilder(Class)} is to only mock
     * some methods and the others should be the real ones. This is necessary to mock only some
     * methods from the Android API.
     */
    private final MainActivity mainActivityMocked = EasyMock.partialMockBuilder(MainActivity.class)
        .addMockedMethod("runOnUiThread", Runnable.class)
        .withConstructor()
        .createMock();

    /**
     * Setup method called before each test.
     * <p>
     * The {@link MainActivity#showUiMessage(String)} method needs {@link MainActivity#setCurrentInstance()}
     * to be called so it sets the {@link MainActivity#currentInstance} field 1st.
     */
    @Before
    public void setUp() {
        logger.info("setUp");
        mainActivityMocked.setCurrentInstance();
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        logger.info("tearDown");
    }

    /**
     * Tests the {@link MainActivity#showUiMessage(String)} method.
     */
    @Test
    public void testShowUiMessage() {
        mainActivityMocked.runOnUiThread(EasyMock.anyObject(Runnable.class));
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(mainActivityMocked);
        MainActivity.showUiMessage("test");

        EasyMock.verify(mainActivityMocked);
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
        final Intent intentMocked = EasyMock.mock(Intent.class);

        final Uri uriMocked = EasyMock.mock(Uri.class);
        EasyMock.expect(uriMocked.getPathSegments())
            .andReturn(ImmutableList.of("data", "local", "tmp", "MobileRT", "WavefrontOBJs", "CornellBox", "CornellBox-Water.obj"))
            .anyTimes();
        EasyMock.expect(uriMocked.getPath())
            .andReturn("/data/local/tmp/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj")
            .anyTimes();
        EasyMock.expect(intentMocked.getData())
            .andReturn(uriMocked)
            .anyTimes();
        EasyMock.replay(intentMocked, uriMocked);

        PowerMock.mockStatic(UtilsContext.class);
        EasyMock.expect(UtilsContext.getInternalStoragePath(EasyMock.anyObject(Context.class)))
            .andReturn("/data/local/tmp")
            .anyTimes();

        PowerMock.mockStatic(Environment.class);
        EasyMock.expect(Environment.getExternalStorageDirectory())
            .andReturn(new File(""))
            .anyTimes();
        PowerMock.replayAll();

        mainActivityMocked.onActivityResult(MainActivity.OPEN_FILE_REQUEST_CODE, Activity.RESULT_OK, intentMocked);

        PowerMock.verifyAll();
        Assertions.assertThat((String) ReflectionTestUtils.getField(mainActivityMocked, "sceneFilePath"))
            .as("The 'MainActivity#sceneFilePath' field")
            .isEqualTo("/data/local/tmp/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
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
            .andReturn(ImmutableList.of("file", "sdcard", "MobileRT", "WavefrontOBJs", "CornellBox", "CornellBox-Water.obj"))
            .anyTimes();
        EasyMock.expect(uriMocked.getPath())
            .andReturn("/data/local/tmp/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj")
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
        EasyMock.expect(UtilsContext.getInternalStoragePath(EasyMock.anyObject(Context.class)))
            .andReturn("/data/local/tmp")
            .anyTimes();
        PowerMock.mockStatic(Environment.class);
        EasyMock.expect(Environment.getExternalStorageDirectory())
            .andReturn(new File("/mockedSDCard"))
            .anyTimes();

        PowerMock.replayAll();
        mainActivityMocked.onActivityResult(MainActivity.OPEN_FILE_REQUEST_CODE, Activity.RESULT_OK, intentMocked);

        Assertions.assertThat((String) ReflectionTestUtils.getField(mainActivityMocked, "sceneFilePath"))
            .as("The 'MainActivity#sceneFilePath' field")
            .isEqualTo("/data/local/tmp/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
    }
}
