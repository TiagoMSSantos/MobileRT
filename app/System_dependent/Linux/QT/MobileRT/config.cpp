#include "config.h"
#include "ui_config.h"
#include <iostream>

Config::Config(QWidget *parent) :
    QDialog(parent),
    ui(new Ui::Config)
{
    ui->setupUi(this);

    ui->shaderButton->addAction(new QAction("No Shadows", this));
    ui->shaderButton->addAction(new QAction("Whitted", this));
    ui->shaderButton->addAction(new QAction("Path Tracing", this));
    ui->shaderButton->addAction(new QAction("DepthMap", this));
    ui->shaderButton->addAction(new QAction("Diffuse", this));
    ui->shaderButton->setDefaultAction(ui->shaderButton->actions().at(0));

    ui->acceleratorButton->addAction(new QAction("None", this));
    ui->acceleratorButton->addAction(new QAction("Naive", this));
    ui->acceleratorButton->addAction(new QAction("Regular Grid", this));
    ui->acceleratorButton->addAction(new QAction("BVH", this));
    ui->acceleratorButton->setDefaultAction(ui->acceleratorButton->actions().at(0));
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

void Config::selected_shader(QAction *action) {
    m_shader = ui->shaderButton->actions().indexOf(action);
    ui->shaderButton->setDefaultAction(action);
}

void Config::selected_accelerator(QAction *action) {
    m_accelerator = ui->acceleratorButton->actions().indexOf(action);
    ui->acceleratorButton->setDefaultAction(action);
}
