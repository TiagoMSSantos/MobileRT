package puscas.mobilertapp;

import static puscas.mobilertapp.constants.Constants.BYTES_IN_MEGABYTE;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.widget.TextView;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import puscas.mobilertapp.constants.ConstantsError;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * The test suite for the {@link DrawView}.
 */
@PrepareForTest({MainActivity.class, DrawView.class, MainRenderer.class})
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
        UtilsT.setPrivateField(memoryInfo, "availMem", (long) BYTES_IN_MEGABYTE);
        UtilsT.setPrivateField(drawView.getRenderer(), "memoryInfo", memoryInfo);

        Assertions.assertThatThrownBy(() -> drawView.setViewAndActivityManager(textViewMocked, activityManagerMocked))
            .as("The call to DrawView#setViewAndActivityManager method")
            .isInstanceOf(FailureException.class);
    }
}
