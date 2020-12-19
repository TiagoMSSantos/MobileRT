#include "config.h"
#include "MobileRT/Utils/Utils.hpp"
#include "ui_config.h"

#include <functional>
#include <iostream>

Config::Config(const Config::Builder &builder) :
        QDialog(nullptr),
        ui(new Ui::Config) {

    ui->setupUi(this);
    LOG_DEBUG("Config");

    m_shader = builder.m_shader;
    m_accelerator = builder.m_accelerator;
    m_scene = builder.m_scene;
    m_spp = builder.m_spp;
    m_spl = builder.m_spl;

    ui->shaderButton->addAction(new QAction("No Shadows", this));
    ui->shaderButton->addAction(new QAction("Whitted", this));
    ui->shaderButton->addAction(new QAction("Path Tracing", this));
    ui->shaderButton->addAction(new QAction("DepthMap", this));
    ui->shaderButton->addAction(new QAction("Diffuse", this));
    ui->shaderButton->setDefaultAction(ui->shaderButton->actions().at(m_shader));

    ui->acceleratorButton->addAction(new QAction("None", this));
    ui->acceleratorButton->addAction(new QAction("Naive", this));
    ui->acceleratorButton->addAction(new QAction("Regular Grid", this));
    ui->acceleratorButton->addAction(new QAction("BVH", this));
    ui->acceleratorButton->setDefaultAction(ui->acceleratorButton->actions().at(m_accelerator));

    ui->sceneButton->addAction(new QAction("Cornell", this));
    ui->sceneButton->addAction(new QAction("Spheres", this));
    ui->sceneButton->addAction(new QAction("Cornell2", this));
    ui->sceneButton->addAction(new QAction("Spheres2", this));
    ui->sceneButton->addAction(new QAction("OBJ", this));
    ui->sceneButton->setDefaultAction(ui->sceneButton->actions().at(m_scene));

    ui->sppSpinBox->setMinimum(1);
    ui->sppSpinBox->setMaximum(100);
    ui->sppSpinBox->setValue(m_spp);

    ui->splSpinBox->setMinimum(1);
    ui->splSpinBox->setMaximum(100);
    ui->splSpinBox->setValue(m_spl);
}

Config::Config(Config &&config) noexcept {
    this->m_accelerator = config.m_accelerator;
    this->m_scene = config.m_scene;
    this->m_shader = config.m_shader;
    this->m_spp = config.m_spp;
    this->m_spl = config.m_spl;
    this->ui = new Ui::Config ();
}

Config::~Config() {
    delete ui;
}

::std::int32_t Config::getShader() {
    return m_shader;
}

::std::int32_t Config::getAccelerator() {
    return m_accelerator;
}

::std::int32_t Config::getScene() {
    return m_scene;
}

::std::int32_t Config::getSPP() {
    return m_spp;
}

::std::int32_t Config::getSPL() {
    return m_spl;
}

void Config::selected_shader(QAction *action) {
    m_shader = ui->shaderButton->actions().indexOf(action);
    ui->shaderButton->setDefaultAction(action);
}

void Config::selected_accelerator(QAction *action) {
    m_accelerator = ui->acceleratorButton->actions().indexOf(action);
    ui->acceleratorButton->setDefaultAction(action);
}

void Config::selected_scene(QAction *action) {
    m_scene = ui->sceneButton->actions().indexOf(action);
    ui->sceneButton->setDefaultAction(action);
}

void Config::selected_spp(int value) {
    ui->sppSpinBox->setValue(value);
    m_spp = value;
}

void Config::selected_spl(int value) {
    ui->splSpinBox->setValue(value);
    m_spl = value;
}

/**
 * The constructor.
 */
Config::Builder::Builder() noexcept {
}

/**
 * The constructor.
 *
 * @param shader The shader index.
 * @return A builder for the Config.
 */
Config::Builder Config::Builder::withShader(const ::std::int32_t shader) {
    this->m_shader = shader;
    return *this;
}

/**
 * The constructor.
 *
 * @param accelerator The accelerator index.
 * @return A builder for the Config.
 */
Config::Builder Config::Builder::withAccelerator(const ::std::int32_t accelerator) {
    this->m_accelerator = accelerator;
    return *this;
}

/**
 * The constructor.
 *
 * @param scene The scene index.
 * @return A builder for the Config.
 */
Config::Builder Config::Builder::withScene(const ::std::int32_t scene) {
    this->m_scene = scene;
    return *this;
}

/**
 * The constructor.
 *
 * @param spp The number of samples per pixel.
 * @return A builder for the Config.
 */
Config::Builder Config::Builder::withSpp(const ::std::int32_t spp) {
    this->m_spp = spp;
    return *this;
}

/**
 * The constructor.
 *
 * @param spl The number of samples per light.
 * @return A builder for the Config.
 */
Config::Builder Config::Builder::withSpl(const ::std::int32_t spl) {
    this->m_spl = spl;
    return *this;
}

/**
 * The build method.
 *
 * @return A new instance of a Config.
 */
Config Config::Builder::build() {
    return Config(*this);
}
