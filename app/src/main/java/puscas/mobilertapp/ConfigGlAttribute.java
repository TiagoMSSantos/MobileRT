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
     * The name of the vertex shader attribute variable to which index is to be bound.
     */
    private final String attributeName;

    /**
     * Specifies a offset of the first component of the first generic vertex attribute in the array
     * in the data store of the buffer currently bound to the GL_ARRAY_BUFFER target.
     * The initial value is 0.
     */
    private final Buffer buffer;

    /**
     * The index of the generic vertex attribute to be bound.
     */
    private final int attributeLocation;

    /**
     * The number of components per generic vertex attribute.
     * Must be 1, 2, 3, 4.
     * Additionally, the symbolic constant GL_BGRA is accepted by glVertexAttribPointer.
     * The initial value is 4.
     */
    private final int componentsInBuffer;
}
