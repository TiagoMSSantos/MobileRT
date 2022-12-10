package puscas.mobilertapp;

import static puscas.mobilertapp.constants.Constants.BYTES_IN_MEGABYTE;

import android.app.ActivityManager;
import android.graphics.Bitmap;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.util.concurrent.CountDownLatch;

import javax.microedition.khronos.opengles.GL10;

import puscas.mobilertapp.configs.ConfigResolution;
import puscas.mobilertapp.configs.ConfigSamples;
import puscas.mobilertapp.exceptions.LowMemoryException;

/**
 * The test suite for the {@link MainRenderer}.
 */
@PrepareForTest({MainActivity.class, Bitmap.class})
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

        final int initialValue = 1;
        final CountDownLatch countDownLatch = new CountDownLatch(initialValue);
        final Runnable runnable = countDownLatch::countDown;

        mainRenderer.setActivityManager(activityManagerMocked);
        Assertions.assertThatCode(() -> mainRenderer.checksFreeMemory(10, runnable))
            .as("The MainRenderer#checksFreeMemory method")
            .doesNotThrowAnyException();

        Assertions.assertThat(countDownLatch.getCount())
            .as("The CountDownLatch value")
            .isEqualTo(initialValue);
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

        final int initialValue = 2;
        final CountDownLatch countDownLatch = new CountDownLatch(initialValue);
        final Runnable runnable = countDownLatch::countDown;

        mainRenderer.setActivityManager(activityManagerMocked);
        Assertions.assertThatThrownBy(() -> mainRenderer.checksFreeMemory(10, runnable))
            .as("The MainRenderer#checksFreeMemory method")
            .isInstanceOf(LowMemoryException.class);
        Assertions.assertThat(countDownLatch.getCount())
            .as("The CountDownLatch value")
            .isEqualTo(initialValue - 1);

        UtilsT.setPrivateField(memoryInfo, "lowMemory", true);
        Assertions.assertThatThrownBy(() -> mainRenderer.checksFreeMemory(1, runnable))
            .as("The MainRenderer#checksFreeMemory method")
            .isInstanceOf(LowMemoryException.class);

        Assertions.assertThat(countDownLatch.getCount())
            .as("The CountDownLatch value")
            .isEqualTo(initialValue - 2);
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

        final int initialValue = 1;
        final CountDownLatch countDownLatch = new CountDownLatch(initialValue);
        final Runnable runnable = countDownLatch::countDown;

        mainRenderer.setActivityManager(activityManagerMocked);
        Assertions.assertThatThrownBy(() -> mainRenderer.checksFreeMemory(0, runnable))
            .as("The MainRenderer#checksFreeMemory method")
            .isInstanceOf(IllegalArgumentException.class);

        Assertions.assertThat(countDownLatch.getCount())
            .as("The CountDownLatch value")
            .isEqualTo(initialValue);
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

    /**
     * Tests the {@link MainRenderer#setBitmap(ConfigResolution, ConfigResolution, boolean)} method
     * (called by {@link MainRenderer#MainRenderer()}) will throw an {@link Exception} when the
     * created {@link Bitmap} does not have the expected size.
     */
    @Test
    public void testSetBitmap() {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        try (final MockedStatic<Bitmap> bitmapMockedStatic = Mockito.mockStatic(Bitmap.class)) {
            final Bitmap bitmapMocked = Mockito.mock(Bitmap.class);
            bitmapMockedStatic.when(() -> Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                .thenReturn(bitmapMocked);

            Mockito.when(bitmapMocked.isRecycled())
                .thenReturn(true);
            Mockito.when(bitmapMocked.getWidth())
                .thenReturn(1);
            Mockito.when(bitmapMocked.getHeight())
                .thenReturn(1);
            Assertions.assertThatThrownBy(MainRenderer::new)
                .as("The MainRenderer#setBitmap method")
                .isInstanceOf(IllegalArgumentException.class);

            Mockito.when(bitmapMocked.isRecycled())
                .thenReturn(false);
            Mockito.when(bitmapMocked.getWidth())
                .thenReturn(2);
            Mockito.when(bitmapMocked.getHeight())
                .thenReturn(1);
            Assertions.assertThatThrownBy(MainRenderer::new)
                .as("The MainRenderer#setBitmap method")
                .isInstanceOf(IllegalArgumentException.class);

            Mockito.when(bitmapMocked.isRecycled())
                .thenReturn(false);
            Mockito.when(bitmapMocked.getWidth())
                .thenReturn(1);
            Mockito.when(bitmapMocked.getHeight())
                .thenReturn(2);
            Assertions.assertThatThrownBy(MainRenderer::new)
                .as("The MainRenderer#setBitmap method")
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    /**
     * Tests that when the {@link MainRenderer#onDrawFrame(GL10)} method fails to render a frame,
     * it still exits the method normally without any {@link Exception} being thrown.
     */
    @Test
    public void testRenderingErrorOnDrawFrame() {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "showUiMessage"));
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetRenderButton"));
        final MainRenderer mainRenderer = new MainRenderer();
        Assertions.assertThat((boolean) UtilsT.getPrivateField(mainRenderer, "firstFrame"))
            .as("The 1st frame field")
            .isTrue();

        try (final MockedStatic<MainActivity> mainActivityMockedStatic = Mockito.mockStatic(MainActivity.class)) {
            mainRenderer.onDrawFrame(Mockito.mock(GL10.class));

            mainActivityMockedStatic.verify(() -> MainActivity.showUiMessage(ArgumentMatchers.anyString()), Mockito.times(1));
            mainActivityMockedStatic.verify(MainActivity::resetRenderButton, Mockito.times(1));

            Assertions.assertThat((boolean) UtilsT.getPrivateField(mainRenderer, "firstFrame"))
                .as("The 1st frame field")
                .isFalse();
        }

    }

}
