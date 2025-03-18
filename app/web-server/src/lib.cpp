#include "System_dependent/Native/C_wrapper.h"
#include <iostream>
#include <mutex>
#include <chrono>
#include <thread>

extern "C" {
    static ::std::mutex mutex_ {};
    static ::MobileRT::Config m_config {};
    ::std::string m_objPath {};

    void select_obj_path(const char* input) {
        m_objPath = ::std::string {input};
        ::std::cout << "Next OBJ file path: " << m_objPath << ::std::endl;
    }

    void start_rendering() {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        stopRender();
        ::std::this_thread::sleep_for(::std::chrono::seconds(1));

        ::std::cout << "Start rendering" << ::std::endl;
        m_config = {};
        m_config.width = 800;
        m_config.height = 800;
        m_config.threads = 8;
        m_config.shader = 1; // 0 -> NoShadows, 1 -> Whitted, 2 -> PathTracing
        m_config.sceneIndex = 4; // 0 -> cornellBox, 1 -> spheres, 2 -> cornellBox2, 3 -> spheres2, 4 -> OBJ
        m_config.samplesPixel = 1;
        m_config.samplesLight = 1;
        m_config.repeats = 1;
        m_config.accelerator = 3; // 0 -> off, 1 -> naive, 2 -> RegGrid, 3 -> BVH

        /*const ::std::string fileName {"../src/androidTest/resources/teapot/teapot"};
        ::std::cout << "Loading scene " << fileName << ::std::endl;
        m_config.objFilePath = fileName + ".obj";
        m_config.mtlFilePath = fileName + ".mtl";
        m_config.camFilePath = fileName + ".cam";*/

        m_config.objFilePath = m_objPath;
        size_t pos = m_objPath.find(".obj");
        m_config.mtlFilePath = m_objPath;
        m_config.camFilePath = m_objPath;
        if (pos != std::string::npos) {
            m_config.mtlFilePath.replace(pos, 4, ".mtl");
            m_config.camFilePath.replace(pos, 4, ".cam");
        }
        ::std::cout << "Loading scene 2 " << m_objPath << ::std::endl;
        if (m_objPath.empty()) {
            m_config.sceneIndex = 2;
        }

        const ::std::uint32_t size {static_cast<::std::uint32_t> (m_config.width) * static_cast<::std::uint32_t> (m_config.height)};
        ::std::cout << "Scene size " << size << ::std::endl;
        m_config.bitmap = ::std::vector<::std::int32_t> (size);

        ::std::cout << "Starting Ray tracing" << ::std::endl;
        RayTrace(m_config, true);
        ::std::cout << "Ray tracing started" << ::std::endl;
    }

    ::std::int32_t *get_bitmap() {
        ::std::cout << "Getting bitmap from C++!" << ::std::endl;
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        return m_config.bitmap.data();
    }
}
