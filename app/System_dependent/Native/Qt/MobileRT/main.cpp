#include "mainwindow.h"
#include "MobileRT/Config.hpp"
#include "MobileRT/Utils/Constants.hpp"
#include "MobileRT/Utils/Utils.hpp"

#include <QApplication>
#include <cmath>

int main(int argc, char **argv) {
    /*
     * ${THREAD} ${SHADER} ${SCENE} ${SPP} ${SPL} ${WIDTH} ${HEIGHT} ${ACC} ${REP} \
            ${OBJ} ${MTL} ${CAM} ${PRINT} ${ASYNC} ${SHOWIMAGE}
     */
//    const char* argv[] {"appName",
//        "2", "2", "4", "1", "1", "800", "800", "3", "1",
//        "./WavefrontOBJs/conference/conference.obj",
//        "./WavefrontOBJs/conference/conference.mtl",
//        "./WavefrontOBJs/conference/conference.cam",
//        "true", "true", "true"};
//    argc = 16;

    if (argc != 16) {
        LOG_ERROR("Wrong number of arguments: ", argc, ", must be 16");
        ::std::exit(1);
    }

    const ::std::int32_t threads {static_cast<::std::int32_t> (strtol(argv[1], nullptr, 0))};
    const ::std::int32_t shader {static_cast<::std::int32_t> (strtol(argv[2], nullptr, 0))};
    const ::std::int32_t scene {static_cast<::std::int32_t> (strtol(argv[3], nullptr, 0))};
    const ::std::int32_t samplesPixel {static_cast<::std::int32_t> (strtol(argv[4], nullptr, 0))};
    const ::std::int32_t samplesLight {static_cast<::std::int32_t> (strtol(argv[5], nullptr, 0))};

    const ::std::int32_t width {
            ::MobileRT::roundDownToMultipleOf(static_cast<::std::int32_t> (strtol(argv[6], nullptr, 0)),
                                              static_cast<::std::int32_t> (::std::sqrt(
                                                      ::MobileRT::NumberOfTiles)))};

    const ::std::int32_t height {
            ::MobileRT::roundDownToMultipleOf(static_cast<::std::int32_t> (strtol(argv[7], nullptr, 0)),
                                              static_cast<::std::int32_t> (::std::sqrt(
                                                      ::MobileRT::NumberOfTiles)))};

    const ::std::int32_t accelerator {static_cast<::std::int32_t> (strtol(argv[8], nullptr, 0))};

    const ::std::int32_t repeats {static_cast<::std::int32_t> (strtol(argv[9], nullptr, 0))};
    const char *const pathObj {argv[10]};
    const char *const pathMtl {argv[11]};
    const char *const pathCam {argv[12]};

    ::std::istringstream ssPrintStdOut (argv[13]);
    ::std::istringstream ssAsync (argv[14]);
    ::std::istringstream ssShowImage (argv[15]);
    bool printStdOut {true};
    bool async {true};
    bool showImage {true};

    ssPrintStdOut >> ::std::boolalpha;
    ssPrintStdOut >> printStdOut;
    ssAsync >> ::std::boolalpha >> async;
    ssShowImage >> ::std::boolalpha >> showImage;
    
    if (!showImage) {
        return 0;
    }

    QApplication application {argc, const_cast<char**> (argv)};
    MainWindow mainWindow {};
    ::MobileRT::Config config {};
    config.width = width;
    config.height = height;
    config.threads = threads;
    config.shader = shader;
    config.sceneIndex = scene;
    config.samplesPixel = samplesPixel;
    config.samplesLight = samplesLight;
    config.repeats = repeats;
    config.accelerator = accelerator;
    config.printStdOut = printStdOut;
    config.objFilePath = ::std::string {pathObj};
    config.mtlFilePath = ::std::string {pathMtl};
    config.camFilePath = ::std::string {pathCam};

    mainWindow.setImage(config, async);
    mainWindow.show();

    if (!async) {
        return 0;
    }

    return application.exec();
}
