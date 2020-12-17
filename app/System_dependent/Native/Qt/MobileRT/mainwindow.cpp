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
        const auto fileName {dialog.selectedFiles().at(0).section(".", 0, 0).toStdString()};
        m_config.objFilePath = (fileName + ".obj").c_str();
        m_config.mtlFilePath = (fileName + ".mtl").c_str();
        m_config.camFilePath = (fileName + ".cam").c_str();
    }
    ::std::cout << "m_pathObj: " << m_config.objFilePath << ::std::endl;
}

void MainWindow::select_config() {
    Config config {m_config.shader, m_config.accelerator, m_config.sceneIndex, m_config.samplesPixel, m_config.samplesLight};
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
