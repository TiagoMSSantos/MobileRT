#include "mainwindow.h"
#include "ui_mainwindow.h"
#include "MobileRT/Utils.hpp"
#include <QImage>
#include <QGraphicsPixmapItem>
#include <QTimer>

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    m_ui(new Ui::MainWindow)
{
    m_ui->setupUi(this);
    m_graphicsPixmapItem = m_scene->addPixmap(m_pixmap);
    m_ui->graphicsView->setScene(m_scene);
    m_ui->graphicsView->show();
}

MainWindow::~MainWindow()
{
    delete m_ui;
}

void MainWindow::exit_app()
{
    ::QApplication::exit();
}

void MainWindow::update_image()
{
    draw(*m_bitmap, m_width, m_height);
}

void MainWindow::setImage(const ::std::vector<::std::int32_t> &bitmap, const ::std::int32_t width, const ::std::int32_t height) {
    m_bitmap = &bitmap;
    m_width = width;
    m_height = height;

    QTimer *timer = new QTimer(this);
    connect(timer, SIGNAL(timeout()), this, SLOT(update_image()));
    timer->start(1000);

    this->resize(width + 2, height + 44);
    m_ui->graphicsView->resize(width + 2, height + 2);
}

void MainWindow::keyPressEvent(QKeyEvent *keyEvent) {
    LOG("KEY PRESSED");
    if (keyEvent->key() == ::Qt::Key_Escape) {
        ::QApplication::exit();
    }
}

void MainWindow::draw(const ::std::vector<::std::int32_t> &bitmap, const ::std::int32_t width, const ::std::int32_t height) {
    // ABGR
    const QImage image {
        QImage(reinterpret_cast<const ::std::uint8_t *> (bitmap.data()), width, height, ::QImage::Format::Format_ARGB32).rgbSwapped()
    };
    m_pixmap = ::QPixmap::fromImage(image, ::Qt::NoFormatConversion);
    m_graphicsPixmapItem->setPixmap(m_pixmap);
}
