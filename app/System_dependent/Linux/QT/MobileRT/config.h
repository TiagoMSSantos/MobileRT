#ifndef CONFIG_H
#define CONFIG_H

#include <QDialog>

namespace Ui {
class Config;
}

class Config : public QDialog
{
    Q_OBJECT

public:
    explicit Config(QWidget *parent = 0);
    ~Config();
    ::std::int32_t getShader();
    ::std::int32_t getAccelerator();

private slots:
    void selected_shader(QAction *action);
    void selected_accelerator(QAction *action);

private:
    Ui::Config *ui;
    ::std::int32_t m_shader {};
    ::std::int32_t m_accelerator {};
};

#endif // CONFIG_H
