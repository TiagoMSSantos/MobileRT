#ifndef CONFIG_H
#define CONFIG_H

#include <QDialog>

namespace Ui {
    class Config;
}

class Config : public QDialog {
    Q_OBJECT

public:
    class Builder;

private:
    explicit Config(const Config::Builder& builder);

public:
    explicit Config() = delete;
    Config(const Config &config) = delete;
    Config(Config &&config) noexcept;
    Config &operator=(const Config &config) = delete;
    Config &operator=(Config &&config) noexcept = delete;

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

public:
    class Builder final {
    public:
        ::std::int32_t m_shader {};
        ::std::int32_t m_accelerator {};
        ::std::int32_t m_scene {};
        ::std::int32_t m_spp {};
        ::std::int32_t m_spl {};
        friend class Config;

    public:
        explicit Builder() noexcept;

        Builder withShader(::std::int32_t shader);

        Builder withAccelerator(::std::int32_t accelerator);

        Builder withScene(::std::int32_t scene);

        Builder withSpp(::std::int32_t spp);

        Builder withSpl(::std::int32_t spl);

        Config build();
    };
};

#endif // CONFIG_H
