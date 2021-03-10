package puscas.mobilertapp;

import java.nio.Buffer;
import lombok.Builder;
import lombok.Getter;

/**
 * The configurator for the OpenGL attribute.
 */
@Builder
@Getter
public final class ConfigGlAttribute {

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
}
