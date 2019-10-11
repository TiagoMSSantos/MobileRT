//
// Created by Tiago on 09-Feb-17.
//

#include "MobileRT/Renderer.hpp"
#include <gsl/gsl>
#include <thread>
#include <vector>

using ::MobileRT::Renderer;

Renderer::Renderer(::std::unique_ptr<Shader> shader,
                   ::std::unique_ptr<Camera> camera,
                   ::std::unique_ptr<Sampler> samplerPixel,
                   const ::std::uint32_t width, const ::std::uint32_t height,
                   const ::std::uint32_t samplesPixel) noexcept :
        camera_{::std::move(camera)},
        shader_{::std::move(shader)},
        samplerPixel_{::std::move(samplerPixel)},
        blockSizeX_{width / static_cast<::std::uint32_t>(::std::sqrt(NumberOfBlocks))},
        blockSizeY_{height / static_cast<::std::uint32_t>(::std::sqrt(NumberOfBlocks))},
        sample_{0},
        width_{width},
        height_{height},
        domainSize_{(width / blockSizeX_) * (height / blockSizeY_)},
        resolution_{width * height},
        samplesPixel_{samplesPixel} {
    this->shader_->initializeAccelerators(camera_.get());
}

void Renderer::renderFrame(::std::uint32_t *const bitmap, const ::std::int32_t numThreads,
                           const ::std::uint32_t stride) noexcept {
    LOG("numThreads = ", numThreads);
    const ::std::uint32_t realWidth {stride / static_cast<::std::uint32_t>(sizeof(::std::uint32_t))};
    LOG("realWidth = ", realWidth);
    LOG("width_ = ", width_);

    this->sample_ = 0;
    this->samplerPixel_->resetSampling();
    this->shader_->resetSampling();
    this->camera_->resetSampling();

    const ::std::int32_t numChildren{numThreads - 1};
    ::std::vector<::std::thread> threads {};
    threads.reserve(static_cast<::std::uint32_t>(numChildren));

    for (::std::int32_t i{0}; i < numChildren; ++i) {
        threads.emplace_back(&Renderer::renderScene, this, bitmap, i, realWidth);
    }
    renderScene(bitmap, numChildren, realWidth);
    for (::std::thread &thread : threads) {
        thread.join();
    }
    threads.clear();
    ::std::vector<::std::thread> {}.swap(threads);

    LOG("Resolution = ", width_, "x", height_);
    LOG("FINISH");
}

void Renderer::stopRender() noexcept {
    this->blockSizeX_ = 0;
    this->blockSizeY_ = 0;
    this->samplerPixel_->stopSampling();
}

void Renderer::renderScene(::std::uint32_t *const bitmap, const ::std::int32_t tid, const ::std::uint32_t width) noexcept {
    const float INV_IMG_WIDTH{1.0f / this->width_};
    const float INV_IMG_HEIGHT{1.0f / this->height_};
    const float pixelWidth{0.5f / this->width_};
    const float pixelHeight{0.5f / this->height_};
    ::glm::vec3 pixelRGB{};
    const ::std::uint32_t samples{this->samplesPixel_};

    ::gsl::span<::std::uint32_t> spanBitmap (bitmap, static_cast<::std::int32_t> (width_ * height_));
    const auto bitmapItBegin {spanBitmap.begin()};

    for (::std::uint32_t sample{0}; sample < samples; ++sample) {
        while (true) {
            const float block{this->camera_->getBlock(sample)};
            if (block >= 1.0f) { break; }
            const ::std::uint32_t roundBlock{
                    static_cast<::std::uint32_t> (::std::roundf(block * this->domainSize_))};
            const ::std::uint32_t pixel{
                    static_cast<::std::uint32_t>(roundBlock * this->blockSizeX_ % resolution_)};
            const ::std::uint32_t startY{
                    ((pixel / this->width_) * this->blockSizeY_) % this->height_};
            const ::std::uint32_t endY{startY + this->blockSizeY_};
            for (::std::uint32_t y{startY}; y < endY; ++y) {
                const float v{y * INV_IMG_HEIGHT};
                const ::std::uint32_t yWidth{y * width};
                const ::std::uint32_t startX{(pixel + yWidth) % this->width_};
                const ::std::uint32_t endX{startX + this->blockSizeX_};
                for (::std::uint32_t x{startX}; x < endX; ++x) {
                    const float u{x * INV_IMG_WIDTH};
                    const float r1{this->samplerPixel_->getSample()};
                    const float r2{this->samplerPixel_->getSample()};
                    const float deviationU{(r1 - 0.5f) * 2.0f * pixelWidth};
                    const float deviationV{(r2 - 0.5f) * 2.0f * pixelHeight};
                    const Ray &ray {this->camera_->generateRay(u, v, deviationU, deviationV)};
                    pixelRGB = {};
                    this->shader_->rayTrace(&pixelRGB, ray);
                    const ::std::uint32_t pixelIndex {yWidth + x};
                    ::std::uint32_t &bitmapPixel {*(bitmapItBegin + static_cast<::std::int32_t> (pixelIndex))};
                    const ::std::uint32_t pixelColor {
                            ::MobileRT::incrementalAvg(pixelRGB, bitmapPixel, sample + 1)};
                    bitmapPixel = pixelColor;
                }
            }
        }
        if (tid == 0) {
            this->sample_ = sample + 1;
            LOG("Samples terminados = ", this->sample_);
        }
    }
}

::std::uint32_t Renderer::getSample() const noexcept {
    return this->sample_;
}
