package puscas.mobilertapp;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.core.internal.deps.guava.collect.ImmutableList;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.stream.IntStream;

@RunWith(AndroidJUnit4.class)
class MainActivityTest {

    /**
     * The rule to create the MainActivity.
     */
    @Rule
    private ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, true);


    @Before
    public void setUp() {

    }

    @After
    public void tearDown()  {

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
            Assertions.assertTrue(file.canRead(), "File should be readable!");
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
    void testUI() {
        mainActivityActivityTestRule.launchActivity(null);

        testRenderButton(1);
        testRenderButton(100);
        testPickerNumbers();
        testPreviewCheckBox();

        mainActivityActivityTestRule.finishActivity();
    }

    private void testPickerNumbers() {
        IntStream.rangeClosed(0, 2).forEach(value -> assertPickerValue(R.id.pickerAccelerator, value));
        IntStream.rangeClosed(1, 100).forEach(value -> assertPickerValue(R.id.pickerSamplesLight, value));
        IntStream.rangeClosed(0, 4).forEach(value -> assertPickerValue(R.id.pickerScene, value));
        IntStream.rangeClosed(0, 4).forEach(value -> assertPickerValue(R.id.pickerShader, value));
        IntStream.rangeClosed(1, 4).forEach(value -> assertPickerValue(R.id.pickerThreads, value));
        IntStream.rangeClosed(1, 10).forEach(value -> assertPickerValue(R.id.pickerSamplesPixel, value));
        IntStream.rangeClosed(1, 9).forEach(value -> assertPickerValue(R.id.pickerSize, value));
    }

    private void testRenderButton(final int repetitions) {
        if (repetitions < 2) {
            assertPickerValue(R.id.pickerSamplesPixel, 3);
            assertPickerValue(R.id.pickerScene, 5);
            assertPickerValue(R.id.pickerThreads, 4);
            assertPickerValue(R.id.pickerSize, 9);
            assertPickerValue(R.id.pickerSamplesLight, 1);
            assertPickerValue(R.id.pickerAccelerator, 2);
            assertPickerValue(R.id.pickerShader, 2);
        } else {
            assertPickerValue(R.id.pickerSamplesPixel, 3);
            assertPickerValue(R.id.pickerScene, 2);
            assertPickerValue(R.id.pickerThreads, 4);
            assertPickerValue(R.id.pickerSize, 9);
            assertPickerValue(R.id.pickerSamplesLight, 1);
            assertPickerValue(R.id.pickerAccelerator, 2);
            assertPickerValue(R.id.pickerShader, 2);
        }
        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
        .check((view, exception) -> {
            final Button button = view.findViewById(R.id.renderButton);
            Assertions.assertEquals("Render", button.getText().toString(), "Button message");
        });

        final List<String> buttonTextList = ImmutableList.<String>builder().add("Stop", "Render").build();
        IntStream.rangeClosed(1, buttonTextList.size() * repetitions).forEach(index -> {
            final int finalIndex = (index - 1) % buttonTextList.size();
            Espresso.onView(ViewMatchers.withId(R.id.renderButton)).perform(new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return ViewMatchers.isAssignableFrom(Button.class);
                }

                @Override
                public String getDescription() {
                    return "Click render button";
                }

                @Override
                public void perform(final UiController uiController, final View view) {
                    view.performClick();
                }
            })
            .check((view, exception) -> {
                final Button button = view.findViewById(R.id.renderButton);
                Assertions.assertEquals(buttonTextList.get(finalIndex), button.getText().toString(),
                        "Button message at index " + index);
            });
        });
    }

    private void testPreviewCheckBox() {
        Espresso.onView(ViewMatchers.withId(R.id.preview))
        .check((view, exception) -> {
            final CheckBox checkbox = view.findViewById(R.id.preview);
            Assertions.assertEquals("Preview", checkbox.getText().toString(), "Check box message");
            Assertions.assertTrue(checkbox.isChecked(), "Check box should be checked");
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
            Assertions.assertEquals("Preview", checkbox.getText().toString(), "Check box message");
            Assertions.assertFalse(checkbox.isChecked(), "Check box should not be checked");
        });
    }

    private void assertPickerValue(final int pickerId, final int value) {
        Espresso.onView(ViewMatchers.withId(pickerId))
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
            public void perform(final UiController uiController, final View view) {
                ((NumberPicker) view).setValue(value);
            }
        })
        .check((view, exception) -> {
            final NumberPicker numberPicker = view.findViewById(pickerId);
            Assertions.assertEquals(value, numberPicker.getValue(), "Number picker message");
        });
    }
}
