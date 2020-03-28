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
    explicit Config(::std::int32_t shader, ::std::int32_t accelerator, ::std::int32_t scene, ::std::int32_t spp, ::std::int32_t spl);
    ~Config();
    ::std::int32_t getShader();
    ::std::int32_t getAccelerator();
    ::std::int32_t getScene();
    ::std::int32_t getSPP();
    ::std::int32_t getSPL();

private slots:
    void selected_shader(QAction *action);
    void selected_accelerator(QAction *action);
    void selected_scene(QAction *action);
    void selected_spp(int value);
    void selected_spl(int value);

private:
    Ui::Config *ui;
    ::std::int32_t m_shader {};
    ::std::int32_t m_accelerator {};
    ::std::int32_t m_scene {};
    ::std::int32_t m_spp {};
    ::std::int32_t m_spl {};
};

#endif // CONFIG_H
