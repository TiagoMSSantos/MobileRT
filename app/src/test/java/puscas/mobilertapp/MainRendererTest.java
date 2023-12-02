package puscas.mobilertapp;

import static puscas.mobilertapp.constants.Constants.BYTES_IN_MEGABYTE;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.Uninterruptibles;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import puscas.mobilertapp.configs.ConfigResolution;
import puscas.mobilertapp.configs.ConfigSamples;
import puscas.mobilertapp.exceptions.FailureException;
import puscas.mobilertapp.exceptions.LowMemoryException;

/**
 * The test suite for the {@link MainRenderer}.
 */
// Annotation & Rule necessary for PowerMock to be able to mock static methods.
@PrepareOnlyThisForTest({MainActivity.class, Bitmap.class, GLES20.class})
public class MainRendererTest {

    /**
     * The {@link Rule} for the {@link PowerMock} to be able to mock static methods.
     */
    @NonNull
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    /**
     * Tests the {@link MainRenderer#checksFreeMemory(int, Runnable)} method.
     */
    @Test
    public void testChecksFreeMemory() {
        final MainRenderer mainRenderer = createMockedMainRenderer();

        final ActivityManager activityManagerMocked = EasyMock.mock(ActivityManager.class);
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ReflectionTestUtils.setField(memoryInfo, "availMem", 100L * BYTES_IN_MEGABYTE);
        ReflectionTestUtils.setField(mainRenderer, "memoryInfo", memoryInfo);

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
        final MainRenderer mainRenderer = createMockedMainRenderer();

        final ActivityManager activityManagerMocked = EasyMock.mock(ActivityManager.class);
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ReflectionTestUtils.setField(memoryInfo, "availMem", 9L * BYTES_IN_MEGABYTE);
        ReflectionTestUtils.setField(mainRenderer, "memoryInfo", memoryInfo);

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

        ReflectionTestUtils.setField(memoryInfo, "lowMemory", true);
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
        final MainRenderer mainRenderer = createMockedMainRenderer();

        final ActivityManager activityManagerMocked = EasyMock.mock(ActivityManager.class);
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ReflectionTestUtils.setField(memoryInfo, "availMem", 100L * BYTES_IN_MEGABYTE);
        ReflectionTestUtils.setField(mainRenderer, "memoryInfo", memoryInfo);

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
        final MainRenderer mainRenderer = createMockedMainRenderer();

        Assertions.assertThatThrownBy(() -> mainRenderer.resetStats(0, ConfigSamples.Builder.Companion.create().build(), -2, 0))
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

        final Bitmap bitmapMocked = EasyMock.createNiceMock(Bitmap.class);
        EasyMock.expect(bitmapMocked.isRecycled())
            .andReturn(true)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getWidth())
            .andReturn(1)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getHeight())
            .andReturn(1)
            .anyTimes();
        EasyMock.replay(bitmapMocked);

        PowerMock.mockStatic(Bitmap.class);
        EasyMock.expect(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
            .andReturn(bitmapMocked)
            .anyTimes();
        PowerMock.replayAll();

        Assertions.assertThatThrownBy(MainRenderer::new)
            .as("The MainRenderer#setBitmap method")
            .isInstanceOf(IllegalArgumentException.class);

        EasyMock.reset(bitmapMocked);
        EasyMock.expect(bitmapMocked.isRecycled())
            .andReturn(false)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getWidth())
            .andReturn(2)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getHeight())
            .andReturn(1)
            .anyTimes();
        EasyMock.replay(bitmapMocked);

        Assertions.assertThatThrownBy(MainRenderer::new)
            .as("The MainRenderer#setBitmap method")
            .isInstanceOf(IllegalArgumentException.class);

        EasyMock.reset(bitmapMocked);
        EasyMock.expect(bitmapMocked.isRecycled())
            .andReturn(false)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getWidth())
            .andReturn(1)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getHeight())
            .andReturn(2)
            .anyTimes();
        EasyMock.replay(bitmapMocked);

        Assertions.assertThatThrownBy(MainRenderer::new)
            .as("The MainRenderer#setBitmap method")
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Tests that when the {@link MainRenderer#onDrawFrame(GL10)} method fails to render a frame,
     * it still exits the method normally without any {@link Exception} being thrown.
     */
    @Test
    public void testRenderingErrorOnDrawFrame() throws LowMemoryException {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "showUiMessage"));
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetRenderButton"));

        final MainRenderer mainRenderer = createMockedMainRenderer();

        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ReflectionTestUtils.setField(memoryInfo, "availMem", 100L * BYTES_IN_MEGABYTE);
        ReflectionTestUtils.setField(mainRenderer, "arrayVertices", ByteBuffer.allocate(1));
        ReflectionTestUtils.setField(mainRenderer, "arrayColors", ByteBuffer.allocate(1));
        ReflectionTestUtils.setField(mainRenderer, "arrayCamera", ByteBuffer.allocate(1));

        Assertions.assertThat((boolean) ReflectionTestUtils.getField(mainRenderer, "firstFrame"))
            .as("The 1st frame field should be true")
            .isTrue();


        // Setup mock.
        mainRenderer.rtRenderIntoBitmap(EasyMock.anyObject(Bitmap.class), EasyMock.anyInt());
        EasyMock.expectLastCall().andThrow(new FailureException("Exception test")).anyTimes();
        EasyMock.replay(mainRenderer);

        Assertions.assertThatCode(() -> mainRenderer.onDrawFrame(EasyMock.mock(GL10.class)))
            .as("The call to 'onDrawFrame' should catch the exception and not rethrow it.")
            .doesNotThrowAnyException();

        EasyMock.verify(mainRenderer);

        Assertions.assertThat((boolean) ReflectionTestUtils.getField(mainRenderer, "firstFrame"))
            .as("The 1st frame field should be false")
            .isFalse();
    }

    /**
     * Tests that the {@link MainRenderer#getState()} method will advance when the
     * {@link MainRenderer#onDrawFrame(GL10)} method is called for the 1st time.
     */
    @Test
    public void testGetStateWhileCallingOnDrawFrame() throws InterruptedException {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "showUiMessage"));
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetRenderButton"));

        final MainRenderer mainRenderer = createMockedMainRenderer();

        final Thread thread = new Thread(() -> {
            Uninterruptibles.sleepUninterruptibly(1L, TimeUnit.SECONDS);
            mainRenderer.onDrawFrame(EasyMock.mock(GL10.class));
        });
        Assertions.assertThat((boolean) ReflectionTestUtils.getField(mainRenderer, "firstFrame"))
            .as("The 1st frame field")
            .isTrue();

        thread.start();
        mainRenderer.getState();

        Assertions.assertThat((boolean) ReflectionTestUtils.getField(mainRenderer, "firstFrame"))
            .as("The 1st frame field")
            .isFalse();

        thread.join();
        Assertions.assertThat(thread.isAlive())
            .as("The thread calling the MainRender#onDrawFrame method")
            .isFalse();
    }

    /**
     * Tests that the {@link MainRenderer#closeRenderer()} method will not call the
     * {@link GLES20#glDeleteTextures(int, int[], int)} method if
     * {@link MainRenderer#onSurfaceCreated(GL10, EGLConfig)} was not called first.
     */
    @Test
    public void testCloseRenderer() {
        final MainRenderer mainRenderer = createMockedMainRenderer();

        PowerMock.mockStaticStrict(GLES20.class);
        GLES20.glDeleteTextures(EasyMock.anyInt(), EasyMock.anyObject(int[].class), EasyMock.anyInt());
        PowerMock.expectLastCall().andThrow(new FailureException("Method 'glDeleteTextures' should not be called!")).anyTimes();

        PowerMock.replayAll();
        mainRenderer.closeRenderer();

        PowerMock.verifyAll();
    }

    /**
     * Tests that the {@link MainRenderer#renderSceneToBitmap(ByteBuffer, ByteBuffer, ByteBuffer, int)}
     * method will throw a {@link LowMemoryException} if there is not enough memory available
     * to render the scene.
     */
    @Test
    public void testRenderSceneToBitmapLowMemory() throws LowMemoryException {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "showUiMessage"));
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetRenderButton"));

        final MainRenderer mainRenderer = createMockedMainRenderer();

        final ActivityManager activityManagerMocked = PowerMock.createMock(ActivityManager.class);
        activityManagerMocked.getMemoryInfo(EasyMock.anyObject(ActivityManager.MemoryInfo.class));
        EasyMock.expectLastCall().andVoid().anyTimes();
        mainRenderer.setActivityManager(activityManagerMocked);

        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ReflectionTestUtils.setField(memoryInfo, "availMem", 100L * BYTES_IN_MEGABYTE);
        ReflectionTestUtils.setField(mainRenderer, "memoryInfo", memoryInfo);

        Assertions.assertThatThrownBy(() -> mainRenderer.renderSceneToBitmap(
            ByteBuffer.allocate(1),
            ByteBuffer.allocate(1),
            ByteBuffer.allocate(1),
            1234567
        ))
            .as("The call to MainRenderer#renderSceneToBitmap method")
            .isInstanceOf(LowMemoryException.class);
    }

    /**
     * Tests that the {@link MainRenderer#copyGlFrameBufferToBitmap(ConfigResolution, ConfigResolution)}
     * method will throw an {@link IllegalArgumentException} if the new created {@link Bitmap} for
     * preview does not have the expected width and height.
     */
    @Test
    public void testCopyGlFrameBufferToBitmapBadBitmap() {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "showUiMessage"));
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetRenderButton"));

        final MainRenderer mainRenderer = createMockedMainRenderer();

        final ActivityManager activityManagerMocked = PowerMock.createMock(ActivityManager.class);
        activityManagerMocked.getMemoryInfo(EasyMock.anyObject(ActivityManager.MemoryInfo.class));
        EasyMock.expectLastCall().andVoid().anyTimes();
        mainRenderer.setActivityManager(activityManagerMocked);

        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ReflectionTestUtils.setField(memoryInfo, "availMem", 100L * BYTES_IN_MEGABYTE);
        ReflectionTestUtils.setField(mainRenderer, "memoryInfo", memoryInfo);


        final Bitmap bitmapMocked = EasyMock.mock(Bitmap.class);
        final int[] arrayBytesNewBitmap = new int[1];

        PowerMock.mockStatic(Bitmap.class);
        EasyMock.expect(Bitmap.createBitmap(arrayBytesNewBitmap, 1, 1, Bitmap.Config.ARGB_8888))
            .andReturn(bitmapMocked)
            .anyTimes();
        EasyMock.expect(Bitmap.createScaledBitmap(EasyMock.anyObject(Bitmap.class), EasyMock.eq(1), EasyMock.eq(1), EasyMock.eq(true)))
            .andReturn(bitmapMocked)
            .anyTimes();
        PowerMock.replayAll();

        // Set width as invalid.
        EasyMock.reset(bitmapMocked);
        EasyMock.expect(bitmapMocked.isRecycled())
            .andReturn(false)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getWidth())
            .andReturn(2)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getHeight())
            .andReturn(1)
            .anyTimes();

        EasyMock.replay(bitmapMocked);
        Assertions.assertThatThrownBy(() -> mainRenderer.copyGlFrameBufferToBitmap(
            ConfigResolution.Builder.Companion.create().build(),
            ConfigResolution.Builder.Companion.create().build()
        ))
        .as("The call to MainRenderer#copyGlFrameBufferToBitmap method")
        .isInstanceOf(IllegalArgumentException.class);

        // Set height as invalid.
        EasyMock.reset(bitmapMocked);
        EasyMock.expect(bitmapMocked.isRecycled())
            .andReturn(false)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getWidth())
            .andReturn(2)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getHeight())
            .andReturn(2)
            .anyTimes();

        EasyMock.replay(bitmapMocked);
        Assertions.assertThatThrownBy(() -> mainRenderer.copyGlFrameBufferToBitmap(
            ConfigResolution.Builder.Companion.create().build(),
            ConfigResolution.Builder.Companion.create().build()
        ))
        .as("The call to MainRenderer#copyGlFrameBufferToBitmap method")
        .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Helper method which creates a partial mocked {@link MainRenderer} for the tests.
     *
     * @return A partial mocked {@link MainRenderer} to be used by the tests.
     */
    @NonNull
    private MainRenderer createMockedMainRenderer() {
        final MainRenderer mainRenderer;
        final Bitmap bitmapMocked = EasyMock.createNiceMock(Bitmap.class);
        EasyMock.expect(bitmapMocked.isRecycled())
            .andReturn(false)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getWidth())
            .andReturn(1)
            .anyTimes();
        EasyMock.expect(bitmapMocked.getHeight())
            .andReturn(1)
            .anyTimes();
        EasyMock.replay(bitmapMocked);
        PowerMock.mockStatic(Bitmap.class);
        try {
            EasyMock.expect(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                .andReturn(bitmapMocked)
                .anyTimes();

            PowerMock.replayAll();
            MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
            mainRenderer = EasyMock.partialMockBuilder(MainRenderer.class)
                .addMockedMethod("initPreviewArrays")
                .addMockedMethod("rtRenderIntoBitmap", Bitmap.class, int.class)
                .withConstructor()
                .createMock();

            mainRenderer.initPreviewArrays();
            EasyMock.expectLastCall().andVoid().anyTimes();
        } catch (final LowMemoryException ex) {
            throw new RuntimeException(ex);
        }
        return mainRenderer;
    }
}
