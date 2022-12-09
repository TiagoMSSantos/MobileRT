package puscas.mobilertapp;

import static puscas.mobilertapp.constants.Constants.BYTES_IN_MEGABYTE;

import android.app.ActivityManager;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import puscas.mobilertapp.configs.ConfigSamples;
import puscas.mobilertapp.exceptions.LowMemoryException;

/**
 * The test suite for the {@link MainRenderer}.
 */
@PrepareForTest(MainActivity.class)
public class MainRendererTest {

    /**
     * The {@link Rule} for the {@link MainActivity} for each test.
     */
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    /**
     * Tests the {@link MainRenderer#checksFreeMemory(int, Runnable)} method.
     */
    @Test
    public void testChecksFreeMemory() {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        final MainRenderer mainRenderer = new MainRenderer();
        final ActivityManager activityManagerMocked = Mockito.mock(ActivityManager.class);
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        UtilsT.setPrivateField(memoryInfo, "availMem", 100L * BYTES_IN_MEGABYTE);
        UtilsT.setPrivateField(mainRenderer, "memoryInfo", memoryInfo);
        final Runnable runnable = () -> {};

        mainRenderer.setActivityManager(activityManagerMocked);
        Assertions.assertThatCode(() -> mainRenderer.checksFreeMemory(10, runnable))
            .as("The MainRenderer#checksFreeMemory method")
            .doesNotThrowAnyException();
    }

    /**
     * Tests that the {@link MainRenderer#checksFreeMemory(int, Runnable)} method will throw an
     * {@link Exception} when the system is low on free memory.
     */
    @Test
    public void testChecksFreeLowMemory() {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        final MainRenderer mainRenderer = new MainRenderer();
        final ActivityManager activityManagerMocked = Mockito.mock(ActivityManager.class);
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        UtilsT.setPrivateField(memoryInfo, "availMem", 9L * BYTES_IN_MEGABYTE);
        UtilsT.setPrivateField(mainRenderer, "memoryInfo", memoryInfo);
        final Runnable runnable = () -> {};

        mainRenderer.setActivityManager(activityManagerMocked);
        Assertions.assertThatThrownBy(() -> mainRenderer.checksFreeMemory(10, runnable))
            .as("The MainRenderer#checksFreeMemory method")
            .isInstanceOf(LowMemoryException.class);

        UtilsT.setPrivateField(memoryInfo, "lowMemory", true);
        Assertions.assertThatThrownBy(() -> mainRenderer.checksFreeMemory(1, runnable))
            .as("The MainRenderer#checksFreeMemory method")
            .isInstanceOf(LowMemoryException.class);
    }

    /**
     * Tests that the {@link MainRenderer#checksFreeMemory(int, Runnable)} method will throw an
     * {@link Exception} when the requested {@code memoryNeeded} is below 1.
     */
    @Test
    public void testChecksFreeZeroMemory() {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        final MainRenderer mainRenderer = new MainRenderer();
        final ActivityManager activityManagerMocked = Mockito.mock(ActivityManager.class);
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        UtilsT.setPrivateField(memoryInfo, "availMem", 100L * BYTES_IN_MEGABYTE);
        UtilsT.setPrivateField(mainRenderer, "memoryInfo", memoryInfo);
        final Runnable runnable = () -> {};

        mainRenderer.setActivityManager(activityManagerMocked);
        Assertions.assertThatThrownBy(() -> mainRenderer.checksFreeMemory(0, runnable))
            .as("The MainRenderer#checksFreeMemory method")
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Tests that the {@link MainRenderer#resetStats(int, ConfigSamples, int, int)} method will
     * throw an {@link Exception} when the provided {@code numThreads} is less than -1.
     */
    @Test
    public void testResetStats() {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        final MainRenderer mainRenderer = new MainRenderer();

        Assertions.assertThatThrownBy(() -> mainRenderer.resetStats(0, ConfigSamples.builder().build(), -2, 0))
            .as("The MainRenderer#resetStats method")
            .isInstanceOf(IllegalArgumentException.class);
    }

}
