#include "config.h"
#include "ui_config.h"
#include "MobileRT/Utils.hpp"
#include <iostream>

Config::Config(::std::int32_t shader, ::std::int32_t accelerator, ::std::int32_t scene) :
    QDialog(nullptr),
    ui(new Ui::Config)
{
    ui->setupUi(this);
    LOG("Config");

    m_shader = shader;
    m_accelerator = accelerator;
    m_scene = scene;

    ui->shaderButton->addAction(new QAction("No Shadows", this));
    ui->shaderButton->addAction(new QAction("Whitted", this));
    ui->shaderButton->addAction(new QAction("Path Tracing", this));
    ui->shaderButton->addAction(new QAction("DepthMap", this));
    ui->shaderButton->addAction(new QAction("Diffuse", this));
    ui->shaderButton->setDefaultAction(ui->shaderButton->actions().at(shader));

    ui->acceleratorButton->addAction(new QAction("None", this));
    ui->acceleratorButton->addAction(new QAction("Naive", this));
    ui->acceleratorButton->addAction(new QAction("Regular Grid", this));
    ui->acceleratorButton->addAction(new QAction("BVH", this));
    ui->acceleratorButton->setDefaultAction(ui->acceleratorButton->actions().at(accelerator));

    ui->sceneButton->addAction(new QAction("Cornell", this));
    ui->sceneButton->addAction(new QAction("Spheres", this));
    ui->sceneButton->addAction(new QAction("Cornell2", this));
    ui->sceneButton->addAction(new QAction("Spheres2", this));
    ui->sceneButton->addAction(new QAction("OBJ", this));
    ui->sceneButton->setDefaultAction(ui->sceneButton->actions().at(scene));
}

Config::~Config()
{
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
