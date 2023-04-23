package puscas.mobilertapp.configs

import java.nio.Buffer

/**
 * The configurator for the OpenGL attribute.
 *
 * @property attributeName      The name of the vertex shader attribute variable to which index is
 *                              to be bound.
 * @property buffer             Specifies a offset of the first component of the first generic vertex
 *                              attribute in the array in the data store of the buffer currently
 *                              bound to the GL_ARRAY_BUFFER target.
 *                              The initial value is 0.
 * @property attributeLocation  The index of the generic vertex attribute to be bound.
 * @property componentsInBuffer The number of components per generic vertex attribute.
 *                              Must be 1, 2, 3, 4.
 *                              Additionally, the symbolic constant GL_BGRA is accepted by glVertexAttribPointer.
 *                              The initial value is 4.
 */
data class ConfigGlAttribute private constructor(
    val attributeName : String,
    val buffer : Buffer,
    val attributeLocation : Int,
    val componentsInBuffer : Int,
) {

    init {
        require(attributeLocation >= 0) { "The attributeLocation must be >= 0." }
        require(componentsInBuffer >= 0) { "The componentsInBuffer be >= 0." }
    }

    class Builder private constructor() {
        var attributeName = ""
        lateinit var buffer :  Buffer
        var attributeLocation = 0
        var componentsInBuffer = 0

        companion object { fun create() = Builder() }

        fun build() = ConfigGlAttribute(attributeName, buffer, attributeLocation, componentsInBuffer)
    }
}
