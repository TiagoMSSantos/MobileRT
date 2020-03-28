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
    explicit Config(::std::int32_t shader, ::std::int32_t accelerator, ::std::int32_t scene);
    ~Config();
    ::std::int32_t getShader();
    ::std::int32_t getAccelerator();
    ::std::int32_t getScene();

private slots:
    void selected_shader(QAction *action);
    void selected_accelerator(QAction *action);
    void selected_scene(QAction *action);

private:
    Ui::Config *ui;
    ::std::int32_t m_shader {};
    ::std::int32_t m_accelerator {};
    ::std::int32_t m_scene {};
};

#endif // CONFIG_H
