#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QKeyEvent>
#include <vector>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();
    void draw(::std::vector<::std::int32_t> bitmap);

private:
    void keyPressEvent(QKeyEvent *keyEvent);

private:
    Ui::MainWindow *ui;

private slots:
    void exit_app();
};

#endif // MAINWINDOW_H
