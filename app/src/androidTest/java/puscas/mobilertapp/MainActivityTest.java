package puscas.mobilertapp;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.core.internal.deps.guava.collect.ImmutableList;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.stream.IntStream;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
class MainActivityTest {

    /**
     * The rule to create the MainActivity.
     */
    @Rule
    private ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, true);


    @BeforeAll
    static void setUpAll() {

    }

    @BeforeEach
    void setup() {

    }

    @AfterEach
    void tearDown() {

    }

    @AfterAll
    static void tearDownAll() {

    }

    @Test
    void testFilesExist() {
        final List<String> paths = ImmutableList.<String>builder().add(
                "/storage/1D19-170B/WavefrontOBJs/conference/conference.obj",
                "/storage/1D19-170B/WavefrontOBJs/teapot/teapot.obj"
        ).build();
        paths.forEach(path -> {
            final File file = new File(path);
            Assertions.assertTrue(file.exists(), "File should exist!");
        });
    }

    @Test
    void testFileReads() {
        final List<String> paths = ImmutableList.<String>builder().add(
                "/storage/1D19-170B/WavefrontOBJs/conference/conference.obj",
                "/storage/1D19-170B/WavefrontOBJs/teapot/teapot.obj"
        ).build();
        paths.forEach(path -> {
            final File file = new File(path);
            Assertions.assertTrue(file.exists(), "File should exist!");
            Assertions.assertTrue(file.canRead(), "File should not be readable!");
        });
    }

    @Test
    void testFilesNotExist() {
        final List<String> paths = ImmutableList.<String>builder().add(
                "",
                "/storage/1D19-170B/WavefrontOBJs/teapot/teapot2.obj"
        ).build();
        paths.forEach(path -> {
            final File file = new File(path);
            Assertions.assertFalse(file.exists(), "File should not exist!");
            Assertions.assertFalse(file.canRead(), "File should not be readable!");
        });
    }

    @Test
    void testPickerNumbers() {
        mainActivityActivityTestRule.launchActivity(null);

        IntStream.rangeClosed(0, 2).forEach(value -> assertPickerValue(R.id.pickerAccelerator, value));
        IntStream.rangeClosed(1, 100).forEach(value -> assertPickerValue(R.id.pickerSamplesLight, value));
        IntStream.rangeClosed(0, 4).forEach(value -> assertPickerValue(R.id.pickerScene, value));
        IntStream.rangeClosed(0, 4).forEach(value -> assertPickerValue(R.id.pickerShader, value));
        IntStream.rangeClosed(1, 4).forEach(value -> assertPickerValue(R.id.pickerThreads, value));
        IntStream.rangeClosed(1, 10).forEach(value -> assertPickerValue(R.id.pickerSamplesPixel, value));
        IntStream.rangeClosed(1, 9).forEach(value -> assertPickerValue(R.id.pickerSize, value));

        mainActivityActivityTestRule.finishActivity();
    }

    @Test
    void testRenderButton() {
        mainActivityActivityTestRule.launchActivity(null);

        onView(withId(R.id.renderButton))
        .check((view, exception) -> {
            final Button button = view.findViewById(R.id.renderButton);
            Assertions.assertEquals("Render", button.getText().toString());
        })
        .perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(Button.class);
            }

            @Override
            public String getDescription() {
                return "Click render button";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        })
        .check((view, exception) -> {
            final Button button = view.findViewById(R.id.renderButton);
            Assertions.assertEquals("Stop", button.getText().toString());
        });

        mainActivityActivityTestRule.finishActivity();
    }

    @Test
    void testPreviewCheckBox() {
        mainActivityActivityTestRule.launchActivity(null);

        onView(withId(R.id.preview))
        .check((view, exception) -> {
            final CheckBox checkbox = view.findViewById(R.id.preview);
            Assertions.assertEquals("Preview", checkbox.getText().toString());
            Assertions.assertTrue(checkbox.isChecked());
        })
        .perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(CheckBox.class);
            }

            @Override
            public String getDescription() {
                return "Click preview checkbox";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        })
        .check((view, exception) -> {
            final CheckBox checkbox = view.findViewById(R.id.preview);
            Assertions.assertEquals("Preview", checkbox.getText().toString());
            Assertions.assertFalse(checkbox.isChecked());
        });

        mainActivityActivityTestRule.finishActivity();
    }

    private void assertPickerValue(final int pickerId, final int value) {
        onView(withId(pickerId))
        .perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }

            @Override
            public String getDescription() {
                return "Set the value of a NumberPicker";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((NumberPicker) view).setValue(value);
            }
        })
        .check((view, exception) -> {
            final NumberPicker numberPicker = view.findViewById(pickerId);
            Assertions.assertEquals(value, numberPicker.getValue());
        });
    }
}
