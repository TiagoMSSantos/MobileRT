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
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
@PrepareForTest({MainActivity.class, MainRenderer.class})
public class DrawViewTest {

    /**
     * The {@link Rule} for the {@link MainActivity} for each test.
     */
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    /**
     * Tests the {@link DrawView#getActivity()} method.
     */
    @Test
    public void testGetActivity() {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final DrawView drawView = PowerMockito.spy(new DrawView(new MainActivity()));

        final ContextWrapper contextMocked = Mockito.mock(ContextWrapper.class);
        final Activity activityMocked = Mockito.mock(Activity.class);
        Mockito.when(contextMocked.getBaseContext())
            .thenReturn(activityMocked);

        Mockito.when(drawView.getContext())
            .thenReturn(contextMocked);

        Assertions.assertThat(drawView.getActivity())
            .as("The call to DrawView#getActivity method")
            .isSameAs(activityMocked);
    }

    /**
     * Tests that the {@link DrawView#getActivity()} method will throw an {@link IllegalStateException}
     * if the provided {@link android.content.Context} is {@link Activity}.
     */
    @Test
    public void testGetActivityFailure() {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final DrawView drawView = PowerMockito.spy(new DrawView(new MainActivity()));

        final ContextWrapper contextMocked = Mockito.mock(ContextWrapper.class);
        final Context baseContextMocked = Mockito.mock(Context.class);
        Mockito.when(contextMocked.getBaseContext())
            .thenReturn(baseContextMocked);

        Mockito.when(drawView.getContext())
            .thenReturn(contextMocked);

        Assertions.assertThatThrownBy(drawView::getActivity)
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
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final DrawView drawView = PowerMockito.spy(new DrawView(new MainActivity()));

        final TextView textViewMocked = Mockito.mock(TextView.class);
        final ActivityManager activityManagerMocked = Mockito.mock(ActivityManager.class);
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ReflectionTestUtils.setField(memoryInfo, "availMem", (long) BYTES_IN_MEGABYTE);
        ReflectionTestUtils.setField(drawView.getRenderer(), "memoryInfo", memoryInfo);

        Assertions.assertThatThrownBy(() -> drawView.setViewAndActivityManager(textViewMocked, activityManagerMocked))
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
    @Test
    public void testRenderSceneWithLowMemory() throws Exception {
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "showUiMessage"));

        final DrawView drawView = EasyMock.createMockBuilder(DrawView.class)
            .addMockedMethod("rtStartRender")
            .addMockedMethod("waitLastTask")
            .addMockedMethod("rtGetNumberOfLights")
            .addMockedMethod("createScene")
            .createMock();

        final MainRenderer mainRenderer = EasyMock.createMockBuilder(MainRenderer.class)
            .addMockedMethod("rtInitialize")
            .addMockedMethod("rtFinishRender")
            .addMockedMethod("resetStats", int.class, ConfigSamples.class, int.class, int.class)
            .createMock();

        mainRenderer.resetStats(EasyMock.anyInt(), EasyMock.eq(ConfigSamples.builder().build()), EasyMock.anyInt(), EasyMock.anyInt());
        EasyMock.expectLastCall().andVoid();
        EasyMock.expect(mainRenderer.rtInitialize(EasyMock.anyObject(Config.class))).andReturn(2);
        mainRenderer.rtFinishRender();
        EasyMock.expectLastCall().andVoid();
        ReflectionTestUtils.setField(drawView, "renderer", mainRenderer);
        EasyMock.replay(mainRenderer);

        final Button buttonMocked = Mockito.mock(Button.class);
        drawView.setUpButtonRender(buttonMocked);

        // Make the thread pool use only the thread from the test (current one), so it counts to the code coverage.
        final ListeningExecutorService executorService = MoreExecutors.newDirectExecutorService();
        ReflectionTestUtils.setField(drawView, "executorService", executorService);

        EasyMock.expect(drawView.rtGetNumberOfLights()).andReturn(0);
        drawView.rtStartRender(EasyMock.anyBoolean());
        EasyMock.expectLastCall().andVoid().times(2);
        drawView.waitLastTask();
        EasyMock.expectLastCall().andVoid();

        final Config config = Config.builder().build();
        drawView.startRayTracing(config);
        EasyMock.expectLastCall().andThrow(new LowMemoryException());

        EasyMock.replay(drawView);
        Assertions.assertThatCode(() -> drawView.renderScene(config))
            .as("The call to DrawView#renderScene method")
            .doesNotThrowAnyException();

        executorService.shutdown();
        Assertions.assertThat(executorService.awaitTermination(10L, TimeUnit.SECONDS))
            .as("The thread pool finished")
            .isTrue();
        final Future<Boolean> lastTask = (Future) ReflectionTestUtils.getField(drawView, DrawView.class, "lastTask");
        Assertions.assertThat(lastTask.get())
            .as("The last task")
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
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final DrawView drawView = PowerMockito.spy(new DrawView(new MainActivity()));
        final Future<Boolean> lastTask = Mockito.mock(Future.class);
        Mockito.when(lastTask.get(1L, TimeUnit.DAYS))
            .thenThrow(new InterruptedException());
        ReflectionTestUtils.setField(drawView, "lastTask", lastTask);

        Assertions.assertThatCode(drawView::waitLastTask)
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
        MemberModifier.suppress(MemberModifier.method(MainActivity.class, "resetErrno"));
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final DrawView drawView = PowerMockito.spy(new DrawView(new MainActivity()));
        Mockito.when(drawView.getVisibility())
            .thenReturn(View.GONE);


        Assertions.assertThatCode(() -> drawView.onWindowFocusChanged(false))
            .as("The call to DrawView#onWindowFocusChanged method")
            .doesNotThrowAnyException();
        Mockito.verify(drawView, Mockito.times(0))
            .setVisibility(View.VISIBLE);


        Assertions.assertThatCode(() -> drawView.onWindowFocusChanged(true))
            .as("The call to DrawView#onWindowFocusChanged method")
            .doesNotThrowAnyException();

        Mockito.verify(drawView, Mockito.times(1))
            .setVisibility(View.VISIBLE);
    }
}
