package puscas.mobilertapp;

import java.util.Locale;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Contract;

/**
 * The configurator for the number of samples in the Ray Tracer engine.
 */
public final class ConfigSamples {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConfigSamples.class.getName());

    /**
     * @see ConfigSamples#getSamplesPixel()
     */
    private final int samplesPixel;

    /**
     * @see ConfigSamples#getSamplesLight()
     */
    private final int samplesLight;

    /**
     * A private constructor to force the usage of the {@link ConfigSamples.Builder}.
     *
     * @param builder The {@link ConfigSamples.Builder} for this class.
     */
    @Contract(pure = true) ConfigSamples(@Nonnull final ConfigSamples.Builder builder) {
        LOGGER.info("ConfigSamples");

        this.samplesPixel = builder.getSamplesPixel();
        this.samplesLight = builder.getSamplesLight();
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
     * The builder for this class.
     */
    static final class Builder {

        /**
         * The {@link Logger} for this class.
         */
        private static final Logger LOGGER_BUILDER = Logger.getLogger(
            ConfigSamples.Builder.class.getName());

        /**
         * @see ConfigSamples.Builder#withSamplesPixel(int)
         */
        private int samplesPixel = 0;

        /**
         * @see ConfigSamples.Builder#withSamplesLight(int)
         */
        private int samplesLight = 0;

        /**
         * Sets the samples per pixel of {@link ConfigSamples}.
         *
         * @param samplesPixel The new value for the
         *                     {@link ConfigSamples#samplesPixel} field.
         * @return The builder with {@link ConfigSamples.Builder#samplesPixel}
         * already set.
         */
        @Contract("_ -> this")
        @Nonnull
        ConfigSamples.Builder withSamplesPixel(final int samplesPixel) {
            final String message = String.format(Locale.US, "withSamplesPixel: %d", samplesPixel);
            LOGGER_BUILDER.info(message);

            this.samplesPixel = samplesPixel;
            return this;
        }

        /**
         * Sets the samples per light of {@link ConfigSamples}.
         *
         * @param samplesLight The new value for the
         *                     {@link ConfigSamples#samplesLight} field.
         * @return The builder with {@link ConfigSamples.Builder#samplesLight}
         * already set.
         */
        @Contract("_ -> this")
        @Nonnull
        ConfigSamples.Builder withSamplesLight(final int samplesLight) {
            final String message = String.format(Locale.US, "withSamplesLight: %d", samplesLight);
            LOGGER_BUILDER.info(message);

            this.samplesLight = samplesLight;
            return this;
        }


        /**
         * Builds a new instance of {@link ConfigSamples}.
         *
         * @return A new instance of {@link ConfigSamples}.
         */
        @Contract(" -> new")
        @Nonnull
        ConfigSamples build() {
            LOGGER_BUILDER.info("build");

            return new ConfigSamples(this);
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
    }
}
