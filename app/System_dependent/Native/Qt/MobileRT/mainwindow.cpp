#include "about.h"
#include "mainwindow.h"
#include "MobileRT/Utils/Constants.hpp"
#include "MobileRT/Utils/Utils.hpp"
#include "System_dependent/Native/C_wrapper.h"
#include "ui_mainwindow.h"

#include <chrono>
#include <QGraphicsPixmapItem>
#include <QImage>
#include <QFileDialog>
#include <QTimer>
#include <thread>

MainWindow::MainWindow(QWidget *parent) :
        QMainWindow(parent),
        m_ui(new Ui::MainWindow) {
    m_ui->setupUi(this);

    m_graphicsPixmapItem = m_graphicsScene->addPixmap(m_pixmap);
    m_ui->graphicsView->setScene(m_graphicsScene);
    m_ui->graphicsView->show();

    #if !defined(_WIN32) && !defined(__APPLE__)
        // Only catch SIGSEGV for Linux systems, since boost stacktrace doesn't work on Windows nor MacOS.
        ::std::cout << "Setting up SIGSEGV signal catch." << ::std::endl;
        ::std::signal(SIGSEGV, ::MobileRT::signalHandler);
    #endif
}

MainWindow::~MainWindow() {
    delete m_ui;
}

void MainWindow::exit_app() {
    stop_render();
    close();
}

void MainWindow::update_image() {
    draw(m_config.bitmap, m_config.width, m_config.height);
}

void MainWindow::on_actionRender_triggered() {
    restart();
}

void MainWindow::restart() {
    stopRender();
    m_timer->stop();
    disconnect(m_timer, SIGNAL(timeout()));

    m_config.width = ::MobileRT::roundDownToMultipleOf(this->width() - 2,
                                              static_cast<::std::int32_t> (::std::sqrt(
                                                      ::MobileRT::NumberOfTiles)));

    m_config.height = ::MobileRT::roundDownToMultipleOf(this->height() - 70,
                                              static_cast<::std::int32_t> (::std::sqrt(
                                                      ::MobileRT::NumberOfTiles)));

    const ::std::uint32_t size {static_cast<::std::uint32_t> (m_config.width) * static_cast<::std::uint32_t> (m_config.height)};
    LOG_DEBUG("width = ", m_config.width);
    LOG_DEBUG("height = ", m_config.height);
    m_config.bitmap = ::std::vector<::std::int32_t> (size);

    ::std::fill(m_config.bitmap.begin(), m_config.bitmap.end(), 0);

    RayTrace(m_config, m_async);

    m_timer = new QTimer(this);
    connect(m_timer, SIGNAL(timeout()), this, SLOT(update_image()));
    m_timer->start(1000);

    this->resize(m_config.width + 2, m_config.height + 70);
    m_ui->graphicsView->resize(m_config.width + 2, m_config.height + 2);
}

void MainWindow::setImage(const ::MobileRT::Config &config, const bool async) {
    m_async = async;
    m_config = config;

    LOG_DEBUG("width = ", m_config.width);
    LOG_DEBUG("height = ", m_config.height);
    LOG_DEBUG("async = ", m_async);

    const ::std::uint32_t size {static_cast<::std::uint32_t> (m_config.width) * static_cast<::std::uint32_t> (m_config.height)};
    LOG_DEBUG("width = ", m_config.width);
    LOG_DEBUG("height = ", m_config.height);
    m_config.bitmap = ::std::vector<::std::int32_t> (size);

    LOG_DEBUG("obj = ", m_config.objFilePath);
    LOG_DEBUG("mtl = ", m_config.mtlFilePath);
    LOG_DEBUG("cam = ", m_config.camFilePath);

    RayTrace(m_config, m_async);

    m_timer = new QTimer(this);
    connect(m_timer, SIGNAL(timeout()), this, SLOT(update_image()));
    m_timer->start(1000);

    this->resize(m_config.width + 2, m_config.height + 70);
    m_ui->graphicsView->resize(m_config.width + 2, m_config.height + 2);
}

void MainWindow::keyPressEvent(QKeyEvent *keyEvent) {
    LOG_DEBUG("KEY PRESSED");
    if (keyEvent->key() == ::Qt::Key_Escape) {
        ::QApplication::exit();
    }
}

void MainWindow::draw(const ::std::vector<::std::int32_t> &bitmap, const ::std::int32_t width, const ::std::int32_t height) {
    // ABGR
    const QImage image {
        QImage(
                    reinterpret_cast<const ::std::uint8_t *> (bitmap.data()),
                    width,
                    height,
                    ::QImage::Format::Format_ARGB32
        ).rgbSwapped()
    };
    m_pixmap = ::QPixmap::fromImage(image, ::Qt::NoFormatConversion);
    m_graphicsPixmapItem->setPixmap(m_pixmap);
}

void MainWindow::select_obj() {
    ::QFileDialog dialog {};
    dialog.setWindowTitle("Select OBJ file");
    dialog.setDirectory("../");
    dialog.setNameFilter("OBJ file (*.obj)");

    if (dialog.exec()) {
        const ::std::string fileName {dialog.selectedFiles().at(0).section(".", 0, 0).toStdString()};
        m_config.objFilePath = fileName + ".obj";
        m_config.mtlFilePath = fileName + ".mtl";
        m_config.camFilePath = fileName + ".cam";
    }
    ::std::cout << "pathObj: " << m_config.objFilePath << ::std::endl;
    ::std::cout << "pathMtl: " << m_config.mtlFilePath << ::std::endl;
    ::std::cout << "pathCam: " << m_config.camFilePath << ::std::endl;
}

void MainWindow::select_config() {
    Config::Builder builder {};
    builder.withShader(m_config.shader);
    builder.withAccelerator(m_config.accelerator);
    builder.withScene(m_config.sceneIndex);
    builder.withSpp(m_config.samplesPixel);
    builder.withSpl(m_config.samplesLight);

    Config config {builder.build()};

    if (config.exec()) {
        m_config.shader = config.getShader();
        m_config.accelerator = config.getAccelerator();
        m_config.sceneIndex = config.getScene();
        m_config.samplesPixel = config.getSPP();
        m_config.samplesLight = config.getSPL();
    }
}

void MainWindow::about() {
    About about {};
    about.exec();
}

void MainWindow::stop_render() {
    stopRender();
    m_timer->stop();
    disconnect(m_timer, SIGNAL(timeout()));
    ::std::this_thread::sleep_for(::std::chrono::seconds(1));
}
