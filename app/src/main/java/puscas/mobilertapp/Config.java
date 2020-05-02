package puscas.mobilertapp;

import org.jetbrains.annotations.Contract;

import java.util.Locale;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * The configurator for the Ray Tracer engine.
 */
public final class Config {

    /**
     * @see Config#getScene()
     */
    private final int scene;

    /**
     * @see Config#getShader()
     */
    private final int shader;

    /**
     * @see Config#getAccelerator()
     */
    private final int accelerator;

    /**
     * @see Config#getWidth()
     */
    private final int width;

    /**
     * @see Config#getHeight()
     */
    private final int height;

    /**
     * @see Config#getSamplesPixel()
     */
    private final int samplesPixel;

    /**
     * @see Config#getSamplesLight()
     */
    private final int samplesLight;

    /**
     * @see Config#getObjFilePath()
     */
    private final String objFilePath;

    /**
     * @see Config#getMatFilePath()
     */
    private final String matFilePath;

    /**
     * @see Config#getCamFilePath()
     */
    private final String camFilePath;

    /**
     * A private constructor to force the usage of the {@link Config.Builder}.
     *
     * @param builder The {@link Config.Builder} for this class.
     */
    @Contract(pure = true)
    Config(@Nonnull final Config.Builder builder) {
        this.scene = builder.getScene();
        this.shader = builder.getShader();
        this.accelerator = builder.getAccelerator();
        this.width = builder.getWidth();
        this.height = builder.getHeight();
        this.samplesPixel = builder.getSamplesPixel();
        this.samplesLight = builder.getSamplesLight();
        this.objFilePath = builder.getObjFilePath();
        this.matFilePath = builder.getMatFilePath();
        this.camFilePath = builder.getCamFilePath();
    }


    /**
     * Gets the index of the scene.
     */
    @Contract(pure = true)
    public int getScene() {
        return this.scene;
    }

    /**
     * Gets the index of the shader.
     */
    @Contract(pure = true)
    public int getShader() {
        return this.shader;
    }

    /**
     * Gets the index of the acceleration structure.
     */
    @Contract(pure = true)
    public int getAccelerator() {
        return this.accelerator;
    }

    /**
     * Gets the width of the image.
     */
    @Contract(pure = true)
    public int getWidth() {
        return this.width;
    }

    /**
     * Gets the height of the image.
     */
    @Contract(pure = true)
    public int getHeight() {
        return this.height;
    }

    /**
     * Gets the number of samples per pixel.
     */
    @Contract(pure = true)
    public int getSamplesPixel() {
        return this.samplesPixel;
    }

    /**
     * Gets the number of samples per light.
     */
    @Contract(pure = true)
    public int getSamplesLight() {
        return this.samplesLight;
    }


    /**
     * Gets the path to the OBJ file containing the geometry of the scene.
     */
    @Contract(pure = true)
    @Nonnull
    public String getObjFilePath() {
        return this.objFilePath;
    }

    /**
     * Gets the path to the MTL file containing the materials of the scene.
     */
    @Contract(pure = true)
    @Nonnull
    public String getMatFilePath() {
        return this.matFilePath;
    }

    /**
     * Gets the path to the CAM file containing the camera in the scene.
     */
    @Contract(pure = true)
    @Nonnull
    public String getCamFilePath() {
        return this.camFilePath;
    }

    /**
     * The builder for this class.
     */
    static final class Builder {

        /**
         * The {@link Logger} for this class.
         */
        private static final Logger LOGGER_BUILDER = Logger.getLogger(Config.Builder.class.getName());

        /**
         * @see Config.Builder#withScene(int)
         */
        private int scene = 0;

        /**
         * @see Config.Builder#withShader(int)
         */
        private int shader = 0;

        /**
         * @see Config.Builder#withAccelerator(int)
         */
        private int accelerator = 0;

        /**
         * @see Config.Builder#withWidth(int)
         */
        private int width = 0;

        /**
         * @see Config.Builder#withHeight(int)
         */
        private int height = 0;

        /**
         * @see Config.Builder#withSamplesPixel(int)
         */
        private int samplesPixel = 0;

        /**
         * @see Config.Builder#withSamplesLight(int)
         */
        private int samplesLight = 0;

        /**
         * The path to the OBJ file.
         */
        private String objFilePath = "";

        /**
         * The path to the MTL file.
         */
        private String matFilePath = "";

        /**
         * The path to the CAM file.
         */
        private String camFilePath = "";

        /**
         * Sets the scene of {@link Config}.
         *
         * @param scene The new value for the {@link Config#scene} field.
         * @return The builder with {@link Config.Builder#scene} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withScene(final int scene) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withScene: %d", scene));

            this.scene = scene;
            return this;
        }

        /**
         * Sets the shader of {@link Config}.
         *
         * @param shader The new value for the {@link Config#shader} field.
         * @return The builder with {@link Config.Builder#height} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withShader(final int shader) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withShader: %d", shader));

            this.shader = shader;
            return this;
        }

        /**
         * Sets the {@link Config#accelerator}.
         *
         * @param accelerator The new value for the {@link Config#accelerator} field.
         * @return The builder with {@link Config.Builder#samplesPixel} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withAccelerator(final int accelerator) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withAccelerator: %d", accelerator));

            this.accelerator = accelerator;
            return this;
        }

        /**
         * Sets the width of {@link Config}.
         *
         * @param width The new value for the {@link Config#width} field.
         * @return The builder with {@link Config.Builder#width} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withWidth(final int width) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withWidth: %d", width));

            this.width = width;
            return this;
        }

        /**
         * Sets the height of {@link Config}.
         *
         * @param height The new value for the {@link Config#height} field.
         * @return The builder with {@link Config.Builder#height} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withHeight(final int height) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withHeight: %d", height));

            this.height = height;
            return this;
        }

        /**
         * Sets the samples per pixel of {@link Config}.
         *
         * @param samplesPixel The new value for the {@link Config#samplesPixel} field.
         * @return The builder with {@link Config.Builder#samplesPixel} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withSamplesPixel(final int samplesPixel) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withSamplesPixel: %d", samplesPixel));

            this.samplesPixel = samplesPixel;
            return this;
        }

        /**
         * Sets the samples per light of {@link Config}.
         *
         * @param samplesLight The new value for the {@link Config#samplesLight} field.
         * @return The builder with {@link Config.Builder#samplesLight} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withSamplesLight(final int samplesLight) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withSamplesLight: %d", samplesLight));

            this.samplesLight = samplesLight;
            return this;
        }

        /**
         * Sets the path to the OBJ file of {@link Config}.
         *
         * @param objFilePath The new value for the {@link Config#objFilePath} field.
         * @return The builder with {@link Config.Builder#objFilePath} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withOBJ(@Nonnull final String objFilePath) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withOBJ: %s", objFilePath));

            this.objFilePath = objFilePath;
            return this;
        }

        /**
         * Sets the path to the MTL file of {@link Config}.
         *
         * @param matFilePath The new value for the {@link Config#matFilePath} field.
         * @return The builder with {@link Config.Builder#matFilePath} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withMAT(@Nonnull final String matFilePath) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withMAT: %s", matFilePath));

            this.matFilePath = matFilePath;
            return this;
        }

        /**
         * Sets the path to the CAM file of {@link Config}.
         *
         * @param camFilePath The new value for the {@link Config#camFilePath} field.
         * @return The builder with {@link Config.Builder#camFilePath} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        Config.Builder withCAM(@Nonnull final String camFilePath) {
            LOGGER_BUILDER.info(String.format(Locale.US, "withCAM: %s", camFilePath));

            this.camFilePath = camFilePath;
            return this;
        }

        /**
         * Builds a new instance of {@link Config}.
         *
         * @return A new instance of {@link Config}.
         */
        @Contract(" -> new")
        @Nonnull
        Config build() {
            LOGGER_BUILDER.info("build");

            return new Config(this);
        }


        /**
         * Gets the scene index.
         */
        @Contract(pure = true)
        int getScene() {
            return this.scene;
        }

        /**
         * Gets the shader index.
         */
        @Contract(pure = true)
        int getShader() {
            return this.shader;
        }

        /**
         * Gets the accelerator index.
         */
        @Contract(pure = true)
        int getAccelerator() {
            return this.accelerator;
        }

        /**
         * Gets the width of the {@link android.graphics.Bitmap}.
         */
        @Contract(pure = true)
        int getWidth() {
            return this.width;
        }

        /**
         * Gets the height of the {@link android.graphics.Bitmap}.
         */
        @Contract(pure = true)
        int getHeight() {
            return this.height;
        }

        /**
         * Gets the number of samples per pixel.
         */
        @Contract(pure = true)
        int getSamplesPixel() {
            return this.samplesPixel;
        }

        /**
         * Gets the number of samples per light.
         */
        @Contract(pure = true)
        int getSamplesLight() {
            return this.samplesLight;
        }

        /**
         * Gets the path to the OBJ file.
         */
        @Contract(pure = true)
        String getObjFilePath() {
            return this.objFilePath;
        }

        /**
         * Gets the path to the MTL file.
         */
        @Contract(pure = true)
        String getMatFilePath() {
            return this.matFilePath;
        }

        /**
         * Gets the path to the CAM file.
         */
        @Contract(pure = true)
        String getCamFilePath() {
            return this.camFilePath;
        }
    }
}
