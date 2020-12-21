#include "MobileRT/Renderer.hpp"
#include <thread>
#include <vector>

using ::MobileRT::Renderer;
using ::MobileRT::NumberOfTiles;

namespace {
    ::std::array<float, NumberOfTiles> randomSequence {};
}//namespace

/**
 * The constructor.
 *
 * @param shader       The shader to use to render the scene.
 * @param camera       The camera in the scene.
 * @param samplerPixel The sampler to use for the pixel jittering.
 * @param width        The width of the image to render.
 * @param height       The height of the image to render.
 * @param samplesPixel The number of samples per pixel.
 */
Renderer::Renderer(::std::unique_ptr<Shader> shader,
                   ::std::unique_ptr<Camera> camera,
                   ::std::unique_ptr<Sampler> samplerPixel,
                   const ::std::int32_t width, const ::std::int32_t height,
                   const ::std::int32_t samplesPixel) :
        camera_ {::std::move(camera)},
        shader_ {::std::move(shader)},
        samplerPixel_ {::std::move(samplerPixel)},
        blockSizeX_ {width / static_cast<::std::int32_t> (::std::sqrt(NumberOfTiles))},
        blockSizeY_ {height / static_cast<::std::int32_t> (::std::sqrt(NumberOfTiles))},
        sample_ {},
        width_ {width},
        height_ {height},
        domainSize_ {(width / blockSizeX_) * (height / blockSizeY_)},
        resolution_ {width * height},
        samplesPixel_ {samplesPixel} {
    fillArrayWithHaltonSeq(&randomSequence);
    Ray::resetIdGenerator();
}

/**
 * Starts the rendering process of the scene into a bitmap.
 *
 * @param bitmap     The bitmap where the rendered scene should be put.
 * @param numThreads The number of threads to use during the rendering process.
 */
void Renderer::renderFrame(::std::int32_t *const bitmap, const ::std::int32_t numThreads) {
    LOG_DEBUG("numThreads = ", numThreads);
    LOG_DEBUG("Resolution = ", this->width_, "x", this->height_);

    this->sample_ = 0;
    this->samplerPixel_->resetSampling();
    this->shader_->resetSampling();
    this->block_ = 0;

    const auto numChildren {numThreads - 1};
    ::std::vector<::std::thread> threads {};
    threads.reserve(static_cast<::std::uint32_t> (numChildren));

    for (::std::int32_t i {}; i < numChildren; ++i) {
        threads.emplace_back(&Renderer::renderScene, this, bitmap, i);
    }
    renderScene(bitmap, numChildren);
    for (auto &thread : threads) {
        thread.join();
    }
    threads.clear();

    LOG_DEBUG("FINISH");
}

/**
 * Stops the rendering process.
 */
void Renderer::stopRender() {
    this->blockSizeX_ = 0;
    this->blockSizeY_ = 0;
    this->block_.store(::std::numeric_limits<::std::int32_t>::max() - NumberOfTiles, ::std::memory_order_relaxed);
    this->samplesPixel_ = 0;
    this->samplerPixel_->stopSampling();
}

/**
 * Helper method which a thread renders the scene into the bitmap.
 *
 * @param bitmap The bitmap where the rendered scene should be put.
 * @param tid    The thread id.
 */
void Renderer::renderScene(::std::int32_t *const bitmap, const ::std::int32_t tid) {
    const auto invImgWidth {1.0F / this->width_};
    const auto invImgHeight {1.0F / this->height_};
    const auto pixelWidth {0.5F / this->width_};
    const auto pixelHeight {0.5F / this->height_};
    ::glm::vec3 pixelRgb {};
    LOG_DEBUG("renderScene");

    for (::std::int32_t sample {}; sample < this->samplesPixel_; ++sample) {
        LOG_DEBUG("renderScene sample: ", sample);
        while (true) {
            LOG_DEBUG("Will get a tile: bx=", this->blockSizeX_, ", by=", this->blockSizeY_, ", spp=", sample, " (", this->samplesPixel_, ")");
            const auto tile {getTile(sample)};
            if (tile >= 1.0F) {
                break;
            }
            const auto roundBlock {static_cast<::std::int32_t> (::std::roundf(tile * this->domainSize_))};
            const auto pixel {roundBlock * this->blockSizeX_ % this->resolution_};
            const auto startY {((pixel / this->width_) * this->blockSizeY_) % this->height_};
            const auto endY {startY + this->blockSizeY_};
            LOG_DEBUG("Will render a tile");
            for (auto y {startY}; y < endY; ++y) {
                const auto v {y * invImgHeight};
                const auto yWidth {y * this->width_};
                const auto startX {(pixel + yWidth) % this->width_};
                const auto endX {startX + this->blockSizeX_};
                for (auto x {startX}; x < endX; ++x) {
                    const auto u {x * invImgWidth};
                    const auto r1 {this->samplerPixel_->getSample()};
                    const auto r2 {this->samplerPixel_->getSample()};
                    const auto deviationU {(r1 - 0.5F) * 2.0F * pixelWidth};
                    const auto deviationV {(r2 - 0.5F) * 2.0F * pixelHeight};
                    auto &&ray {this->camera_->generateRay(u, v, deviationU, deviationV)};
                    pixelRgb = {};
                    this->shader_->rayTrace(&pixelRgb, ::std::move(ray));
                    const auto pixelIndex {yWidth + x};
                    ::std::int32_t *bitmapPixel {&bitmap[pixelIndex]};
                    const auto pixelColor {::MobileRT::incrementalAvg(pixelRgb, *bitmapPixel, sample + 1)};
                    *bitmapPixel = pixelColor;
                }
            }
            LOG_DEBUG("Tile rendered");
        }
        if (tid == 0) {
            this->sample_ = sample + 1;
            LOG_DEBUG("Sample = ", this->sample_);
        }
        LOG_DEBUG("renderScene sample: ", sample, " finished");
    }
}

/**
 * Gets the number of samples per pixel already rendered.
 *
 * @return The current number of samples per pixel.
 */
::std::int32_t Renderer::getSample() const {
    return this->sample_;
}

/**
 * Helper method which calculates a random value between 0 and 1.
 * <br>
 * This method never repeats the calculated value for the same given sample.
 *
 * @param sample The current sample of samples per pixel.
 * @return A random value between 0 and 1.
 */
float Renderer::getTile(const ::std::int32_t sample) {
    const auto current {this->block_.fetch_add(1, ::std::memory_order_relaxed) - NumberOfTiles * sample};
    if (current >= NumberOfTiles) {
        this->block_.fetch_sub(1, ::std::memory_order_relaxed);
        return 1.0F;
    }
    const auto it {randomSequence.begin() + current};
    return *it;
}

/**
 * Helper method that calculates the total number of casted rays in the scene.
 *
 * @return The total number of casted rays.
 */
::std::uint64_t Renderer::getTotalCastedRays() const {
    const auto castedRays {Ray::getNumberOfCastedRays()};
    return castedRays;
}
