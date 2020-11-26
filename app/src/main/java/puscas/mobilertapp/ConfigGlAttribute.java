package puscas.mobilertapp;

import androidx.annotation.Nullable;
import java.nio.Buffer;
import java.util.Locale;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Contract;

/**
 * The configurator for the OpenGL attribute.
 */
public final class ConfigGlAttribute {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConfigGlAttribute.class.getName());

    /**
     * The attributeName.
     */
    private final String attributeName;

    /**
     * The buffer.
     */
    private final Buffer buffer;

    /**
     * The attributeLocation.
     */
    private final int attributeLocation;

    /**
     * The componentsInBuffer.
     */
    private final int componentsInBuffer;


    /**
     * A private constructor to force the usage of the {@link ConfigGlAttribute.Builder}.
     *
     * @param builder The {@link Config.Builder} for this class.
     */
    private ConfigGlAttribute(@Nonnull final ConfigGlAttribute.Builder builder) {
        LOGGER.info("ConfigGLAttribute");

        this.attributeName = builder.getAttributeName();
        this.buffer = builder.getBuffer();
        this.attributeLocation = builder.getAttributeLocation();
        this.componentsInBuffer = builder.getComponentsInBuffer();
    }

    /**
     * Gets the name of the attribute.
     *
     * @return The name of the attribute.
     */
    @Nullable
    public String getAttributeName() {
        return this.attributeName;
    }

    /**
     * Gets the {@link Buffer} with the data for the attribute.
     *
     * @return The {@link Buffer}.
     */
    @Nullable
    public Buffer getBuffer() {
        return this.buffer;
    }

    /**
     * Gets the attribute location.
     *
     * @return The attribute location.
     */
    public int getAttributeLocation() {
        return this.attributeLocation;
    }

    /**
     * Gets the number of components in the {@link Buffer}.
     *
     * @return The number of components in the {@link Buffer}.
     */
    public int getComponentsInBuffer() {
        return this.componentsInBuffer;
    }


    /**
     * The builder for this class.
     */
    static final class Builder {

        /**
         * The {@link Logger} for this class.
         */
        private static final Logger LOGGER_BUILDER = Logger.getLogger(
            ConfigGlAttribute.Builder.class.getName());

        /**
         * The attributeName.
         */
        private String attributeName = "Unknown";

        /**
         * The buffer.
         */
        private Buffer buffer = null;

        /**
         * The attributeLocation.
         */
        private int attributeLocation = 0;

        /**
         * The componentsInBuffer.
         */
        private int componentsInBuffer = 0;

        /**
         * Sets the attribute name of {@link ConfigGlAttribute}.
         *
         * @param name The name of the attribute.
         * @return The builder with {@link ConfigGlAttribute.Builder#attributeName} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        ConfigGlAttribute.Builder withName(final String name) {
            final String message = String.format(Locale.US, "withName: %s", name);
            LOGGER_BUILDER.info(message);

            this.attributeName = name;
            return this;
        }

        /**
         * Sets the buffer of {@link ConfigGlAttribute}.
         *
         * @param buffer The {@link Buffer} with the data for the attribute.
         * @return The builder with {@link ConfigGlAttribute.Builder#buffer} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        ConfigGlAttribute.Builder withBuffer(final Buffer buffer) {
            final String message = String.format(Locale.US, "withBuffer: %s", buffer.toString());
            LOGGER_BUILDER.info(message);

            this.buffer = buffer;
            return this;
        }

        /**
         * Sets the location of {@link ConfigGlAttribute}.
         *
         * @param location The GLSL location for the attribute.
         * @return The builder with {@link ConfigGlAttribute.Builder#attributeLocation} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        ConfigGlAttribute.Builder withLocation(final int location) {
            final String message = String.format(Locale.US, "withLocation: %d", location);
            LOGGER_BUILDER.info(message);

            this.attributeLocation = location;
            return this;
        }

        /**
         * Sets the number of components of {@link ConfigGlAttribute}.
         *
         * @param components The number of components in the attribute.
         * @return The builder with {@link ConfigGlAttribute.Builder#attributeLocation} already set.
         */
        @Contract("_ -> this")
        @Nonnull
        ConfigGlAttribute.Builder withComponents(final int components) {
            final String message = String.format(Locale.US, "withComponents: %d", components);
            LOGGER_BUILDER.info(message);

            this.componentsInBuffer = components;
            return this;
        }

        /**
         * Builds a new instance of {@link ConfigGlAttribute}.
         *
         * @return A new instance of {@link ConfigGlAttribute}.
         */
        @Contract(" -> new")
        @Nonnull
        ConfigGlAttribute build() {
            LOGGER_BUILDER.info("build");

            return new ConfigGlAttribute(this);
        }


        /**
         * Gets the name of the attribute.
         *
         * @return The name of the attribute.
         */
        String getAttributeName() {
            return this.attributeName;
        }

        /**
         * Gets the {@link Buffer} with the data for the attribute.
         *
         * @return The {@link Buffer}.
         */
        Buffer getBuffer() {
            return this.buffer;
        }

        /**
         * Gets the attribute location.
         *
         * @return The attribute location.
         */
        int getAttributeLocation() {
            return this.attributeLocation;
        }

        /**
         * Gets the number of components in the {@link Buffer}.
         *
         * @return The number of components in the {@link Buffer}.
         */
        int getComponentsInBuffer() {
            return this.componentsInBuffer;
        }

    }

}
