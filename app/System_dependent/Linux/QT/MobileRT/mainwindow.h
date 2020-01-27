#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QKeyEvent>
#include <vector>
#include <QGraphicsScene>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();
    void draw(const ::std::vector<::std::int32_t> &m_bitmap, ::std::int32_t width, ::std::int32_t height);
    void setImage(::std::int32_t width, ::std::int32_t height, ::std::int32_t threads,
                  ::std::int32_t shader, ::std::int32_t scene, ::std::int32_t samplesPixel, ::std::int32_t samplesLight,
                  ::std::int32_t repeats, ::std::int32_t accelerator, bool printStdOut,
                  bool async, const char *pathObj, const char *pathMtl, const char *pathCam);

private:
    void restart();

private:
    void keyPressEvent(QKeyEvent *keyEvent);

private:
    Ui::MainWindow *m_ui;
    QGraphicsScene *const m_graphicsScene {new QGraphicsScene {}};
    QPixmap m_pixmap {};
    QGraphicsPixmapItem *m_graphicsPixmapItem {};
    QTimer *m_timer {};

    ::std::vector<::std::int32_t> m_bitmap {};
    ::std::int32_t m_width {};
    ::std::int32_t m_height {};
    ::std::int32_t m_threads {};
    ::std::int32_t m_shader {};
    ::std::int32_t m_scene {};
    ::std::int32_t m_samplesPixel {};
    ::std::int32_t m_samplesLight {};
    ::std::int32_t m_repeats {};
    ::std::int32_t m_accelerator {};
    bool m_printStdOut {};
    bool m_async {};
    ::std::string m_pathObj {};
    ::std::string m_pathMtl {};
    ::std::string m_pathCam {};

public slots:
    void exit_app();
    void update_image();
    void on_actionOpen_triggered();
    void on_actionRender_triggered();
};

#endif // MAINWINDOW_H
