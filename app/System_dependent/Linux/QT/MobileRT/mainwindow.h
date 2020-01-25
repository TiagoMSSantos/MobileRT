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
    void setImage(const ::std::vector<::std::int32_t> &m_bitmap, ::std::int32_t width, ::std::int32_t height);

private:
    void keyPressEvent(QKeyEvent *keyEvent);

private:
    Ui::MainWindow *m_ui;
    QGraphicsScene *const m_scene {new QGraphicsScene {}};
    QPixmap m_pixmap {};
    QGraphicsPixmapItem *m_graphicsPixmapItem {};
    const ::std::vector<::std::int32_t> *m_bitmap {};
    ::std::int32_t m_width {};
    ::std::int32_t m_height {};

private slots:
    void exit_app();
    void update_image();
};

#endif // MAINWINDOW_H
