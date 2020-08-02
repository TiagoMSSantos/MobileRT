package puscas.mobilertapp;

import org.jetbrains.annotations.Contract;

import java.util.Locale;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * The configurator for the desired resolution in the Ray Tracer engine.
 */
public final class ConfigResolution {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConfigResolution.class.getName());

    /**
     * @see ConfigResolution#getWidth()
     */
    private final int width;

    /**
     * @see ConfigResolution#getHeight()
     */
    private final int height;

    /**
     * A private constructor to force the usage of the
     * {@link ConfigResolution.Builder}.
     *
     * @param builder The {@link ConfigResolution.Builder} for this class.
     */
    @Contract(pure = true)
    ConfigResolution(@Nonnull final ConfigResolution.Builder builder) {
        LOGGER.info("ConfigResolution");

        this.width = builder.getWidth();
        this.height = builder.getHeight();
    }

    /**
     * Gets the width of the image plane.
     */
    @Contract(pure = true)
    public int getWidth() {
        return this.width;
    }

    /**
     * Gets the height of the image plane.
     */
    @Contract(pure = true)
    public int getHeight() {
        return this.height;
    }

    /**
     * The builder for this class.
     */
    static final class Builder {

        /**
         * The {@link Logger} for this class.
         */
        private static final Logger LOGGER_BUILDER = Logger.getLogger(ConfigResolution.Builder.class.getName());

        /**
         * @see ConfigResolution.Builder#withWidth(int)
         */
        private int width = 1;

        /**
         * @see ConfigResolution.Builder#withHeight(int)
         */
        private int height = 1;

        /**
         * Sets the width of {@link ConfigResolution}.
         *
         * @param width The new value for the {@link ConfigResolution#width} field.
         * @return The builder with {@link ConfigResolution.Builder#width} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        ConfigResolution.Builder withWidth(final int width) {
            final String message = String.format(Locale.US, "withWidth: %d", width);
            LOGGER_BUILDER.info(message);

            this.width = width;
            return this;
        }

        /**
         * Sets the height of {@link ConfigResolution}.
         *
         * @param height The new value for the {@link ConfigResolution#height} field.
         * @return The builder with {@link ConfigResolution.Builder#height} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        ConfigResolution.Builder withHeight(final int height) {
            final String message = String.format(Locale.US, "withHeight: %d", height);
            LOGGER_BUILDER.info(message);

            this.height = height;
            return this;
        }


        /**
         * Builds a new instance of {@link ConfigResolution}.
         *
         * @return A new instance of {@link ConfigResolution}.
         */
        @Contract(" -> new")
        @Nonnull
        ConfigResolution build() {
            LOGGER_BUILDER.info("build");

            return new ConfigResolution(this);
        }


        /**
         * Gets the width of the image plane.
         */
        @Contract(pure = true)
        int getWidth() {
            return this.width;
        }

        /**
         * Gets the height of the image plane.
         */
        @Contract(pure = true)
        int getHeight() {
            return this.height;
        }
    }
}
