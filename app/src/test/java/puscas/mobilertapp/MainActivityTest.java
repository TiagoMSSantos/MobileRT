package puscas.mobilertapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

/**
 * The test suite for {@link MainActivity} class.
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("puscas.mobilertapp.MainActivity")
public final class MainActivityTest {

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
     *
     * @implNote Loads the native MobileRT library for these tests.
     */
    @Before
    public void setUp() throws Exception {
        // Because of using PowerMock to mock the static initializer, then it's not necessary
        // to add the native MobileRT library to the Java library path.
        // addLibraryPath("../build_release/lib");

        mainActivityMocked = PowerMockito.spy(new MainActivity());
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
    }

    /**
     * Adds the specified path to the java library path.
     *
     * @implNote It appends a path to the `java.library.path` property by following this recipe:
     * <a href="https://fahdshariff.blogspot.com/2011/08/changing-java-library-path-at-runtime.html">fahd.blog</a>
     *
     * @param pathToAdd The path to add.
     * @throws Exception If anything goes wrong.
     */
    private static void addLibraryPath(final String pathToAdd) throws Exception{
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);
        // Get array of paths.
        final String[] paths = (String[]) usrPathsField.get(null);
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
}
