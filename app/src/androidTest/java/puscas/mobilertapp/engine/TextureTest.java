package puscas.mobilertapp.engine;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;

/**
 * The test suite for the textures used in Ray Tracing engine.
 */
public final class TextureTest extends AbstractTest {

    /**
     * Tests rendering a scene which has a single texture.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    public void testRenderingSceneWithSingleTexture() throws TimeoutException {
        mockFileManagerReply(true,
            "/MobileRT/WavefrontOBJs/teapot/teapot.obj",
            "/MobileRT/WavefrontOBJs/teapot/teapot.mtl",
            "/MobileRT/WavefrontOBJs/teapot/teapot.cam",
            "/MobileRT/WavefrontOBJs/teapot/default.png"
        );

        assertRenderScene(Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false, false);
    }

}
