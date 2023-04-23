package puscas.mobilertapp.configs

import java.lang.annotation.Native

/**
 * The configurator for the desired resolution in the Ray Tracer engine.
 *
 * @property width  The width.
 * @property height The height.
 */
data class ConfigResolution private constructor(
    @Native val width: Int,
    @Native val height: Int,
) {

    init {
        require(width > 0) { "The width must be > 0." }
        require(height > 0) { "The height must be > 0." }
    }

    class Builder private constructor() {
        var width = 1
        var height = 1

        companion object { fun create() = Builder() }

        fun build() = ConfigResolution(width, height)
    }
}
