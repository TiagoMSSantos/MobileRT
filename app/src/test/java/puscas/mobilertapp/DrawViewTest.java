package puscas.mobilertapp;

import static puscas.mobilertapp.constants.Constants.BYTES_IN_MEGABYTE;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import kotlin.Pair;
import puscas.mobilertapp.configs.Config;
import puscas.mobilertapp.configs.ConfigSamples;
import puscas.mobilertapp.constants.ConstantsError;
import puscas.mobilertapp.exceptions.FailureException;
import puscas.mobilertapp.exceptions.LowMemoryException;

/**
 * The test suite for the {@link DrawView}.
 *
 * @implNote Necessary to use the {@link EasyMock} class, so it's possible to mock only some methods
 * and suppress the native methods in the {@link DrawView} and {@link MainRenderer} classes in the
 * {@link #testRenderSceneWithLowMemory} test method.
 */
// Annotations necessary for PowerMock to be able to mock final classes, and static and native methods.
@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({MainActivity.class, DrawView.class, MainRenderer.class})
public final class DrawViewTest {

    /**
     * Tests the {@link DrawView#getActivity()} method.
     */
    @Test
    public void testGetActivity() {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final ContextWrapper contextWrapperMocked = EasyMock.mock(ContextWrapper.class);
        final Activity activityMocked = EasyMock.mock(Activity.class);

        EasyMock.expect(contextWrapperMocked.getBaseContext())
            .andReturn(activityMocked)
            .anyTimes();

        final Context contextMocked = EasyMock.partialMockBuilder(Context.class)
            .withConstructor()
            .createMock();

        final DrawView drawViewMocked = EasyMock.partialMockBuilder(DrawView.class)
            .withConstructor(Context.class)
            .withArgs(contextMocked)
            .addMockedMethod("getContext")
            .createMock();
        EasyMock.expect(drawViewMocked.getContext())
            .andReturn(activityMocked)
            .anyTimes();

        EasyMock.replay(activityMocked, contextWrapperMocked, contextMocked, drawViewMocked);
        Assertions.assertThat(drawViewMocked.getActivity())
            .as("The call to DrawView#getActivity method")
            .isSameAs(activityMocked);
    }

    /**
     * Tests that the {@link DrawView#getActivity()} method will throw an {@link IllegalStateException}
     * if the provided {@link android.content.Context} is not an {@link Activity}.
     */
    @Test
    public void testGetActivityFailure() {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final DrawView drawViewMocked = EasyMock.partialMockBuilder(DrawView.class)
            .withConstructor(Context.class)
            .withArgs(new MainActivity())
            .addMockedMethod("getContext")
            .createMock();

        final ContextWrapper contextMocked = EasyMock.mock(ContextWrapper.class);
        final Context baseContextMocked = EasyMock.mock(Context.class);
        EasyMock.expect(contextMocked.getBaseContext())
            .andReturn(baseContextMocked)
            .anyTimes();
        EasyMock.expect(drawViewMocked.getContext())
            .andReturn(contextMocked)
            .anyTimes();

        EasyMock.replay(drawViewMocked, contextMocked, baseContextMocked);
        Assertions.assertThatThrownBy(drawViewMocked::getActivity)
            .as("The call to DrawView#getActivity method")
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(ConstantsError.UNABLE_TO_FIND_AN_ACTIVITY);
    }

    /**
     * Tests that the {@link DrawView#setViewAndActivityManager(TextView, ActivityManager)} method
     * will throw a {@link FailureException} if there is not 2 free megabytes in main memory.
     */
    @Test
    public void testSetViewAndActivityManager() {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final DrawView drawViewMocked = EasyMock.partialMockBuilder(DrawView.class)
            .withConstructor(Context.class)
            .withArgs(new MainActivity())
            .addMockedMethod("getContext")
            .createMock();

        final TextView textViewMocked = EasyMock.mock(TextView.class);
        final ActivityManager activityManagerMocked = EasyMock.mock(ActivityManager.class);
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ReflectionTestUtils.setField(memoryInfo, "availMem", (long) BYTES_IN_MEGABYTE);
        ReflectionTestUtils.setField(drawViewMocked.getRenderer(), "memoryInfo", memoryInfo);

        Assertions.assertThatThrownBy(() -> drawViewMocked.setViewAndActivityManager(textViewMocked, activityManagerMocked))
            .as("The call to DrawView#setViewAndActivityManager method")
            .isInstanceOf(FailureException.class);
    }

    /**
     * Tests that the {@link DrawView#renderScene(Config)} method will still continue without
     * throwing any {@link Exception} even if the device doesn't have enough available memory to
     * render a scene.
     *
     * @throws Exception If there is an error with the mocks.
     *
     * @implNote Can't validate call to the {@link MainActivity#showUiMessage(String)} method due
     * to conflicts with mocks.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRenderSceneWithLowMemory() throws Exception {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "showUiMessage"));

        final DrawView drawView = EasyMock.partialMockBuilder(DrawView.class)
            .addMockedMethod("rtStartRender", boolean.class)
            .addMockedMethod("rtStopRender", boolean.class)
            .addMockedMethod("waitLastTask")
            .addMockedMethod("rtGetNumberOfLights")
            .addMockedMethod("createScene")
            .createMock();

        final MainRenderer mainRenderer = EasyMock.partialMockBuilder(MainRenderer.class)
            .addMockedMethod("rtInitialize")
            .addMockedMethod("rtFinishRender")
            .addMockedMethod("freeArrays")
            .addMockedMethod("resetStats", int.class, ConfigSamples.class, int.class, int.class)
            .createMock();

        mainRenderer.resetStats(EasyMock.anyInt(), EasyMock.eq(ConfigSamples.Builder.Companion.create().build()), EasyMock.anyInt(), EasyMock.anyInt());
        EasyMock.expectLastCall().andVoid();
        EasyMock.expect(mainRenderer.rtInitialize(EasyMock.anyObject(Config.class))).andReturn(2);
        mainRenderer.rtFinishRender();
        EasyMock.expectLastCall().andVoid().anyTimes();
        ReflectionTestUtils.setField(drawView, "renderer", mainRenderer);
        mainRenderer.freeArrays();
        EasyMock.expectLastCall().andVoid().times(1);
        EasyMock.replay(mainRenderer);

        final Button buttonMocked = EasyMock.mock(Button.class);
        drawView.setUpButtonRender(buttonMocked);

        // Make the thread pool use only the thread from the test (current one), so it counts to the code coverage.
        final ListeningExecutorService currentThreadExecutorService = MoreExecutors.newDirectExecutorService();
        ReflectionTestUtils.setField(drawView, "executorService", currentThreadExecutorService);

        EasyMock.expect(drawView.rtGetNumberOfLights()).andReturn(0);
        // For the mock of 'rtStartRender' to work, it's necessary for the method to be package visible,
        // otherwise an 'UnsatisfiedLinkError' is thrown.
        drawView.rtStartRender(EasyMock.anyBoolean());
        EasyMock.expectLastCall().andVoid().times(2);
        drawView.waitLastTask();
        EasyMock.expectLastCall().andVoid().times(2);
        drawView.rtStopRender(EasyMock.anyBoolean());
        EasyMock.expectLastCall().andVoid().times(1);

        final Config config = Config.Builder.Companion.create().build();
        drawView.startRayTracing(config);
        EasyMock.expectLastCall().andThrow(new LowMemoryException("The device has not enough memory."));

        EasyMock.replay(drawView);
        Assertions.assertThatCode(() -> drawView.renderScene(config))
            .as("The call to DrawView#renderScene method")
            .doesNotThrowAnyException();

        currentThreadExecutorService.shutdown();
        Assertions.assertThat(currentThreadExecutorService.awaitTermination(10L, TimeUnit.SECONDS))
            .as("The thread pool finished")
            .isTrue();
        final Pair<Long, Future<Boolean>> lastTask = (Pair<Long, Future<Boolean>>) Objects.requireNonNull(ReflectionTestUtils.getField(drawView, DrawView.class, "lastTask"));
        Assertions.assertThat(lastTask.getSecond().get())
            .as("The last task should be false.")
            .isFalse();

        // Missing verification of call to MainActivity#showUiMessage method.
    }

    /**
     * Tests that the {@link DrawView#waitLastTask()} method will call the {@link Thread#interrupt()}
     * method if an {@link InterruptedException} was thrown while waiting for the last task to
     * finish.
     *
     * @throws ExecutionException   If the mocks fail.
     * @throws InterruptedException If the mocks fail.
     * @throws TimeoutException     If the mocks fail.
     *
     * @implNote Can't validate call to the {@link Thread#currentThread()} method because it is a
     * native method.
     */
    @Test
    public void testWaitLastTaskInterrupt() throws ExecutionException, InterruptedException, TimeoutException {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final DrawView drawViewMocked = EasyMock.partialMockBuilder(DrawView.class)
            .withConstructor(Context.class)
            .withArgs(new MainActivity())
            .addMockedMethod("getContext")
            .createMock();

        final Future<Boolean> lastTask = EasyMock.mock(Future.class);
        EasyMock.expect(lastTask.get(1L, TimeUnit.DAYS))
            .andThrow(new InterruptedException())
            .anyTimes();
        // The '-1L' value is to allow the 'waitLastTask' method to not block waiting for the task to start and finish.
        ReflectionTestUtils.setField(drawViewMocked, "lastTask", new Pair<>(-1L, lastTask));

        Assertions.assertThatCode(drawViewMocked::waitLastTask)
            .as("The call to DrawView#waitLastTask method")
            .doesNotThrowAnyException();

        // Missing verification of call to Thread.currentThread().interrupt() method.
    }

    /**
     * Tests that the {@link DrawView#onWindowFocusChanged(boolean)} method sets the current window
     * as visible when it has focus and it isn't visible.
     */
    @Test
    public void testOnWindowFocusChanged() {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final DrawView drawViewMocked = EasyMock.partialMockBuilder(DrawView.class)
            .withConstructor(Context.class)
            .withArgs(new MainActivity())
            .addMockedMethod("getContext")
            .addMockedMethod("getVisibility")
            .addMockedMethod("setVisibility")
            .createMock();
        EasyMock.expect(drawViewMocked.getVisibility())
            .andReturn(View.GONE)
            .anyTimes();

        EasyMock.replay(drawViewMocked);
        Assertions.assertThatCode(() -> drawViewMocked.onWindowFocusChanged(false))
            .as("The call to DrawView#onWindowFocusChanged method")
            .doesNotThrowAnyException();
        EasyMock.verify(drawViewMocked);


        EasyMock.reset(drawViewMocked);
        drawViewMocked.setVisibility(View.VISIBLE);
        EasyMock.expectLastCall().times(1);
        EasyMock.expect(drawViewMocked.getVisibility())
            .andReturn(View.GONE)
            .anyTimes();

        EasyMock.replay(drawViewMocked);
        Assertions.assertThatCode(() -> drawViewMocked.onWindowFocusChanged(true))
            .as("The call to DrawView#onWindowFocusChanged method")
            .doesNotThrowAnyException();

        EasyMock.verifyUnexpectedCalls(drawViewMocked);
        EasyMock.verify(drawViewMocked);
    }
}
