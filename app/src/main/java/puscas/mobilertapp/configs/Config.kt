package puscas.mobilertapp.configs

import java.lang.annotation.Native

/**
 * The configurator for the Ray Tracer engine.
 * <br>
 * Note that this class is used to pass data from JVM to Native code in C++ via JNI.
 *
 * @property scene            The scene.
 * @property shader           The shader.
 * @property accelerator      The accelerator.
 * @property objFilePath      The path to the OBJ file.
 * @property matFilePath      The path to the MAT file.
 * @property camFilePath      The path to the CAM file.
 * @property configSamples    The configurator for the number of samples.
 * @property configResolution The configurator for the desired resolution.
 * @property threads          The number of threads.
 * @property rasterize        Whether the Ray Tracing engine should render a preview frame.
 */
data class Config private constructor(
    @Native val scene: Int,
    @Native val shader: Int,
    @Native val accelerator: Int,
    @Native val objFilePath :String,
    @Native val matFilePath :String,
    @Native val camFilePath :String,
    @Native val configSamples: ConfigSamples,
    @Native val configResolution: ConfigResolution,
    @Native val threads: Int,
    @Native val rasterize: Boolean,
) {

    init {
        require(scene >= 0) { "The scene must be >= 0." }
        require(shader >= 0) { "The shader must be >= 0." }
    }

    class Builder private constructor() {
        var scene = 0
        var shader = 0
        var accelerator = 0
        var objFilePath = ""
        var matFilePath = ""
        var camFilePath = ""
        var configSamples = ConfigSamples.Builder.create().build()
        var configResolution = ConfigResolution.Builder.create().build()
        var threads = 0
        var rasterize = false

        companion object { fun create() = Builder() }

        fun build() = Config(scene, shader, accelerator, objFilePath, matFilePath, camFilePath, configSamples, configResolution, threads, rasterize)
    }

}
