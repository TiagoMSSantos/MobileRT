package puscas.mobilertapp;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import puscas.mobilertapp.constants.ConstantsError;

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
}
