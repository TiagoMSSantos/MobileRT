package puscas.mobilertapp.configs

/**
 * The configurator for the number of samples in the Ray Tracer engine.
 *
 * @property samplesPixel The number of samples per pixel.
 * @property samplesLight The number of samples per light.
 */
@ConsistentCopyVisibility
data class ConfigSamples private constructor(
    val samplesPixel : Int,
    val samplesLight : Int,
) {

    init {
        require(samplesPixel >= 0) { "The samplesPixel must be >= 0." }
        require(samplesLight >= 0) { "The samplesLight must be >= 0." }
    }

    class Builder private constructor() {
        var samplesPixel = 0
        var samplesLight = 0

        companion object { fun create() = Builder() }

        fun build() = ConfigSamples(samplesPixel, samplesLight)
    }
}
