#include "MobileRT/Renderer.hpp"
#include <thread>
#include <vector>

using ::MobileRT::Renderer;
using ::MobileRT::NumberOfBlocks;

namespace {
    ::std::array<float, NumberOfBlocks> values {};

    bool fillThings() {
        for (auto it {values.begin()}; it < values.end(); std::advance(it, 1)) {
            const ::std::uint32_t index {static_cast<uint32_t> (::std::distance(values.begin(), it))};
            *it = ::MobileRT::haltonSequence(index, 2);
        }
        static ::std::random_device randomDevice {};
        static ::std::mt19937 generator {randomDevice()};
        ::std::shuffle(values.begin(), values.end(), generator);
        return true;
    }
}//namespace

Renderer::Renderer(::std::unique_ptr<Shader> shader,
                   ::std::unique_ptr<Camera> camera,
                   ::std::unique_ptr<Sampler> samplerPixel,
                   const ::std::int32_t width, const ::std::int32_t height,
                   const ::std::int32_t samplesPixel) :
        camera_ {::std::move(camera)},
        shader_ {::std::move(shader)},
        samplerPixel_ {::std::move(samplerPixel)},
        blockSizeX_ {width / static_cast<::std::int32_t> (::std::sqrt(NumberOfBlocks))},
        blockSizeY_ {height / static_cast<::std::int32_t> (::std::sqrt(NumberOfBlocks))},
        sample_ {},
        width_ {width},
        height_ {height},
        domainSize_ {(width / blockSizeX_) * (height / blockSizeY_)},
        resolution_ {width * height},
        samplesPixel_ {samplesPixel} {
    static auto unused {fillThings()};
    static_cast<void> (unused);
}

void Renderer::renderFrame(::std::int32_t *const bitmap, const ::std::int32_t numThreads) {
    LOG("numThreads = ", numThreads);
    LOG("Resolution = ", this->width_, "x", this->height_);

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

    LOG("FINISH");
}

void Renderer::stopRender() {
    this->blockSizeX_ = 0;
    this->blockSizeY_ = 0;
    this->samplerPixel_->stopSampling();
}

void Renderer::renderScene(::std::int32_t *const bitmap, const ::std::int32_t tid) {
    const auto invImgWidth {1.0F / this->width_};
    const auto invImgHeight {1.0F / this->height_};
    const auto pixelWidth {0.5F / this->width_};
    const auto pixelHeight {0.5F / this->height_};
    const auto samples {this->samplesPixel_};
    ::glm::vec3 pixelRgb {};

    for (::std::int32_t sample {}; sample < samples; ++sample) {
        while (true) {
            const auto tile {getTile(sample)};
            if (tile >= 1.0F) {
                break;
            }
            const auto roundBlock {static_cast<::std::int32_t> (::std::roundf(tile * this->domainSize_))};
            const auto pixel {roundBlock * this->blockSizeX_ % this->resolution_};
            const auto startY {((pixel / this->width_) * this->blockSizeY_) % this->height_};
            const auto endY {startY + this->blockSizeY_};
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
                    const auto &ray {this->camera_->generateRay(u, v, deviationU, deviationV)};
                    pixelRgb = {};
                    this->shader_->rayTrace(&pixelRgb, ray);
                    const auto pixelIndex {yWidth + x};
                    ::std::int32_t *bitmapPixel {&bitmap[pixelIndex]};
                    const auto pixelColor {::MobileRT::incrementalAvg(pixelRgb, *bitmapPixel, sample + 1)};
                    *bitmapPixel = pixelColor;
                }
            }
        }
        if (tid == 0) {
            this->sample_ = sample + 1;
            LOG("Sample = ", this->sample_);
        }
    }
}

::std::int32_t Renderer::getSample() const {
    return this->sample_;
}

float Renderer::getTile(const ::std::int32_t sample) {
    const auto current {this->block_.fetch_add(1, ::std::memory_order_relaxed) - NumberOfBlocks * sample};
    if (current >= NumberOfBlocks) {
        this->block_.fetch_sub(1, ::std::memory_order_relaxed);
        return 1.0F;
    }
    const auto it {values.begin() + current};
    return *it;
}
