package puscas.mobilertapp;

import android.content.Intent;
import android.view.View;
import android.widget.NumberPicker;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mainActivityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);


    @BeforeAll
    static void setUpAll() {
    }

    @BeforeEach
    void setup() {
    }

    @Test
    void testFilesExist() {
        final File file1 = new File("/storage/1D19-170B/WavefrontOBJs/conference/conference.obj");
        final File file2 = new File("/storage/1D19-170B/WavefrontOBJs/teapot/teapot.obj");

        Assertions.assertTrue(file1.exists());
        Assertions.assertTrue(file2.exists());
    }

    @Test
    void testFileReads() {
        final File file1 = new File("/storage/1D19-170B/WavefrontOBJs/conference/conference.obj");
        final File file2 = new File("/storage/1D19-170B/WavefrontOBJs/teapot/teapot.obj");

        Assertions.assertTrue(file1.exists());
        Assertions.assertTrue(file1.canRead());

        Assertions.assertTrue(file2.exists());
        Assertions.assertTrue(file2.canRead());
    }

    @Test
    void testFilesNotExist() {
        final File file1 = new File("");
        final File file2 = new File("/storage/1D19-170B/WavefrontOBJs/teapot/teapot2.obj");

        Assertions.assertFalse(file1.exists());
        Assertions.assertFalse(file1.canRead());

        Assertions.assertFalse(file2.exists());
        Assertions.assertFalse(file2.canRead());
    }

    @Test
    void testPickerSamplesLight() {
        IntentsTestRule firstActivity = new IntentsTestRule(MainActivity.class);
        firstActivity.launchActivity(new Intent());

        onView(withId(R.id.pickerSamplesLight)).perform(new ViewAction() {
            @Override
            public Matcher getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }

            @Override
            public String getDescription() {
                return "Set the value of a NumberPicker";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((NumberPicker)view).setValue(12);
            }
        });

        onView(withId(R.id.pickerSamplesLight)).perform(new ViewAction() {
            @Override
            public Matcher getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }

            @Override
            public String getDescription() {
                return "Set the value of a NumberPicker";
            }

            @Override
            public void perform(UiController uiController, View view) {
                Assertions.assertEquals(12, ((NumberPicker)view).getValue());
            }
        });


        ViewInteraction viewInteraction = onView(withId(R.id.pickerSamplesLight));
        viewInteraction.check((view, exception) -> {
            final NumberPicker picker = view.findViewById(R.id.pickerSamplesLight);
            Assertions.assertEquals(12, picker.getValue());
        });


        //onView(withId(R.id.pickerSamplesLight)).check(matches(withText("1")));

    }



    @AfterEach
    void tearDown() {
    }

    @AfterAll
    static void tearDownAll() {
    }
}
