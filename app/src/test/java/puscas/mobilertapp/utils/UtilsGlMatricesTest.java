package puscas.mobilertapp.utils;

import android.opengl.Matrix;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;

import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * The unit tests for the {@link UtilsGlMatrices} util class.
 */
// Annotations necessary for PowerMock to be able to mock final classes, and static and native methods.
@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({Matrix.class})
public final class UtilsGlMatricesTest {

    /**
     * The expected projection matrix if the {@link Matrix#perspectiveM(float[], int, float, float, float, float)}
     * method is called.
     */
    private static final float[] projectionMatrixPerspectiveCamera = new float[]{
        1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f
    };

    /**
     * The expected projection matrix if the {@link Matrix#orthoM(float[], int, float, float, float, float, float, float)}
     * method is called.
     */
    private static final float[] projectionMatrixOrthographicCamera = new float[]{
        2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f
    };

    /**
     * Tests that it's not possible to instantiate {@link UtilsGlMatrices}.
     *
     * @throws NoSuchMethodException If Java reflection fails when using the private constructor.
     */
    @Test
    public void testDefaultUtilsGlMatrices() throws NoSuchMethodException {
        final Constructor<UtilsGlMatrices> constructor = UtilsGlMatrices.class.getDeclaredConstructor();
        Assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
            .as("The constructor is private")
            .isTrue();
        constructor.setAccessible(true);
        Assertions.assertThatThrownBy(constructor::newInstance)
            .as("The default constructor of ConfigRenderTask")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

    /**
     * Tests the method {@link UtilsGlMatrices#createProjectionMatrix(ByteBuffer, int, int)}.
     */
    @Test
    public void testCreateProjectionMatrix() {
        PowerMock.mockStaticPartialNice(Matrix.class, "perspectiveM", "orthoM");
        final float[] expectedEmptyArray = new float[]{ 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
        final ByteBuffer camera = ByteBuffer.allocate(UtilsGlMatrices.INDEX_SIZEY + Constants.BYTES_IN_FLOAT);
        final int width = 10;
        final int height = 10;

        mockMatrixMethods(0, 0);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVX, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVY, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEH, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEY, 0.0f);
        Assertions.assertThat(UtilsGlMatrices.createProjectionMatrix(camera, width, height))
            .as("The projection matrix should be zero")
            .containsOnly(expectedEmptyArray);
        PowerMock.verifyAll();

        mockMatrixMethods(0, 0);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVX, 1.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVY, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEH, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEY, 0.0f);
        Assertions.assertThat(UtilsGlMatrices.createProjectionMatrix(camera, width, height))
            .as("The projection matrix should be zero")
            .containsOnly(expectedEmptyArray);
        PowerMock.verifyAll();

        mockMatrixMethods(0, 0);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVX, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVY, 1.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEH, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEY, 0.0f);
        Assertions.assertThat(UtilsGlMatrices.createProjectionMatrix(camera, width, height))
            .as("The projection matrix should be zero")
            .containsOnly(expectedEmptyArray);
        PowerMock.verifyAll();

        mockMatrixMethods(1, 0);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVX, 1.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVY, 1.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEH, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEY, 0.0f);
        Assertions.assertThat(UtilsGlMatrices.createProjectionMatrix(camera, width, height))
            .as("The projection matrix should be filled with perspective camera's data")
            .containsOnly(projectionMatrixPerspectiveCamera);
        PowerMock.verifyAll();

        mockMatrixMethods(0, 0);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVX, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVY, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEH, 1.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEY, 0.0f);
        Assertions.assertThat(UtilsGlMatrices.createProjectionMatrix(camera, width, height))
            .as("The projection matrix should be zero")
            .containsOnly(expectedEmptyArray);
        PowerMock.verifyAll();

        mockMatrixMethods(0, 0);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVX, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVY, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEH, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEY, 1.0f);
        Assertions.assertThat(UtilsGlMatrices.createProjectionMatrix(camera, width, height))
            .as("The projection matrix should be zero")
            .containsOnly(expectedEmptyArray);
        PowerMock.verifyAll();

        mockMatrixMethods(0, 1);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVX, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_FOVY, 0.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEH, 1.0f);
        camera.putFloat(UtilsGlMatrices.INDEX_SIZEY, 1.0f);
        Assertions.assertThat(UtilsGlMatrices.createProjectionMatrix(camera, width, height))
            .as("The projection matrix should be filled with orthographic camera's data")
            .containsOnly(projectionMatrixOrthographicCamera);
        PowerMock.verifyAll();
    }

    /**
     * Mocks the methods of {@link Matrix}.
     *
     * @param timesPerspectiveM The expected number of calls to {@link Matrix#perspectiveM(float[], int, float, float, float, float)}.
     * @param timesOrthoM       The expected number of calls to {@link Matrix#orthoM(float[], int, float, float, float, float, float, float)}.
     */
    private void mockMatrixMethods(final int timesPerspectiveM, final int timesOrthoM) {
        PowerMock.resetAll();
        Matrix.perspectiveM(
            EasyMock.anyObject(float[].class), EasyMock.anyInt(), EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat()
        );
        if (timesPerspectiveM <= 0) {
            PowerMock.expectLastCall().andThrow(new FailureException("Method 'perspectiveM' shouldn't be called")).anyTimes();
        } else {
            PowerMock.expectLastCall()
                .andAnswer(() -> {
                    final float[] inputParamToMutate = EasyMock.getCurrentArgument(0);
                    System.arraycopy(projectionMatrixPerspectiveCamera, 0, inputParamToMutate, 0, projectionMatrixPerspectiveCamera.length);
                    return null;
                })
                .times(timesPerspectiveM);
        }

        Matrix.orthoM(
            EasyMock.anyObject(float[].class), EasyMock.anyInt(), EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat()
        );
        if (timesOrthoM <= 0) {
            PowerMock.expectLastCall().andThrow(new FailureException("Method 'orthoM' shouldn't be called")).anyTimes();
        } else {
            PowerMock.expectLastCall()
                .andAnswer(() -> {
                    final float[] inputParamToMutate = EasyMock.getCurrentArgument(0);
                    System.arraycopy(projectionMatrixOrthographicCamera, 0, inputParamToMutate, 0, projectionMatrixOrthographicCamera.length);
                    return null;
                })
                .times(timesOrthoM);
        }
        PowerMock.replayAll();
    }

}
