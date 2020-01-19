#include "mainwindow.h"
#include "ui_mainwindow.h"
#include "MobileRT/Utils.hpp"
#include <QImage>

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
//    connect(ui->exit_button, SIGNAL(clicked()), this, SLOT(exit_app()));
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::exit_app()
{
    QApplication::exit();
}

void MainWindow::keyPressEvent(QKeyEvent *keyEvent) {
    LOG("KEY PRESSED");
    if (keyEvent->key() == Qt::Key_Escape) {
        QApplication::exit();
    }
}

void MainWindow::draw(::std::vector<::std::int32_t> bitmap) {
    QImage image;
    image.loadFromData(bitmap.data(), bitmap.size());
    image.scaled(QSize(300,300), Qt::KeepAspectRatio);

    QGraphicsPixmapItem item( QPixmap::fromImage( image ) );
    QGraphicsScene* scene = new QGraphicsScene;
    ui->graphicsView->setScene( scene );

    scene->addItem( &item );
    item.setPos( 0, 0 );

    ui->graphicsView->show();
}
