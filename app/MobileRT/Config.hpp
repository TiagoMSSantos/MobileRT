#ifndef MOBILERT_CONFIG_HPP
#define MOBILERT_CONFIG_HPP

#include <cstdint>
#include <string>
#include <vector>

namespace MobileRT {
    /**
     * The configurator for the MobileRT engine.
     */
    struct Config {
    public:
        /**
         * The bitmap to where the rendered image should be put.
         */
        ::std::vector<::std::int32_t> bitmap;

        /**
         * The path to the OBJ file of the scene.
         */
        ::std::string objFilePath;

        /**
         * The path to the MTL file of the scene.
         */
        ::std::string mtlFilePath;

        /**
         * The path to the CAM file of the scene.
         */
        ::std::string camFilePath;

        /**
         * The width of the image to render.
         */
        ::std::int32_t width;

        /**
         * The height of the image to render.
         */
        ::std::int32_t height;

        /**
         * The number of threads to be used by the Ray Tracer engine.
         */
        ::std::int32_t threads;

        /**
         * The shader to be used.
         */
        ::std::int32_t shader;

        /**
         * The scene index to render.
         */
        ::std::int32_t sceneIndex;

        /**
         * The number of samples per pixel to use.
         */
        ::std::int32_t samplesPixel;

        /**
         * The number of samples per light to use.
         */
        ::std::int32_t samplesLight;

        /**
         * The number of times to render the scene.
         */
        ::std::int32_t repeats;

        /**
         * The acceleration structure to use.
         */
        ::std::int32_t accelerator;
    };
}//namespace MobileRT


#endif //MOBILERT_CONFIG_HPP
