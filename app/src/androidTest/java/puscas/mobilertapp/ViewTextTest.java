package puscas.mobilertapp;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
class ViewTextTest {
    /**
     * The rule to create the MainActivity.
     */
    @Rule
    private ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, true);


    final private ViewText viewText = new ViewText();


    @Before
    public void setUp() {
        mainActivityActivityTestRule.launchActivity(null);
    }

    @After
    public void tearDown()  {
        mainActivityActivityTestRule.finishActivity();
    }

    @Test
    void testGetFPS() {
        Assertions.assertEquals(1, viewText.getFPS(), "");
    }

    @Test
    void testGetTimeRenderer() {

    }

    @Test
    void testGetSample() {

    }

    @Test
    void testIsWorking() {

    }

    @Test
    void testFPS() {

    }

    @Test
    void testResetPrint() {

    }

    @Test
    void testPrintText() {

    }
}
