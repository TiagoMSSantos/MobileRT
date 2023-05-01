#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include "config.h"
#include "MobileRT/Config.hpp"

#include <QGraphicsScene>
#include <QKeyEvent>
#include <QMainWindow>
#include <vector>

namespace Ui {
    class MainWindow;
}

class MainWindow : public QMainWindow {
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow() override;
    void draw(const ::std::vector<::std::int32_t> &bitmap, ::std::int32_t width, ::std::int32_t height);
    void setImage(const ::MobileRT::Config &config, const bool async);

private:
    void restart();

private:
    void keyPressEvent(QKeyEvent *keyEvent) override;

private:
    Ui::MainWindow *m_ui;
    QGraphicsScene *const m_graphicsScene {new QGraphicsScene {}};
    QPixmap m_pixmap {};
    QGraphicsPixmapItem *m_graphicsPixmapItem {};
    QTimer *m_timer {};
    bool m_async {};
    ::MobileRT::Config m_config {};

public slots:
    void update_image();
    void on_actionRender_triggered();

private slots:
    void select_obj();
    void select_config();
    void exit_app();
    void about();
    void stop_render();
};

#endif // MAINWINDOW_H
