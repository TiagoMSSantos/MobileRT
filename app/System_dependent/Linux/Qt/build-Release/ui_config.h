/********************************************************************************
** Form generated from reading UI file 'config.ui'
**
** Created by: Qt User Interface Compiler version 4.8.7
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_CONFIG_H
#define UI_CONFIG_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtGui/QApplication>
#include <QtGui/QButtonGroup>
#include <QtGui/QDialog>
#include <QtGui/QDialogButtonBox>
#include <QtGui/QHeaderView>
#include <QtGui/QLabel>
#include <QtGui/QSpinBox>
#include <QtGui/QToolButton>

QT_BEGIN_NAMESPACE

class Ui_Config
{
public:
    QDialogButtonBox *buttonBox;
    QToolButton *shaderButton;
    QLabel *shaderLabel;
    QLabel *acceleratorLabel;
    QToolButton *acceleratorButton;
    QLabel *sceneLabel;
    QToolButton *sceneButton;
    QSpinBox *sppSpinBox;
    QLabel *sppLabel;
    QLabel *splLabel;
    QSpinBox *splSpinBox;

    void setupUi(QDialog *Config)
    {
        if (Config->objectName().isEmpty())
            Config->setObjectName(QString::fromUtf8("Config"));
        Config->resize(400, 300);
        buttonBox = new QDialogButtonBox(Config);
        buttonBox->setObjectName(QString::fromUtf8("buttonBox"));
        buttonBox->setGeometry(QRect(30, 240, 341, 32));
        buttonBox->setOrientation(Qt::Horizontal);
        buttonBox->setStandardButtons(QDialogButtonBox::Cancel|QDialogButtonBox::Ok);
        shaderButton = new QToolButton(Config);
        shaderButton->setObjectName(QString::fromUtf8("shaderButton"));
        shaderButton->setGeometry(QRect(240, 20, 150, 30));
        shaderButton->setAcceptDrops(false);
        shaderButton->setAutoRepeatDelay(300);
        shaderButton->setAutoRepeatInterval(100);
        shaderButton->setPopupMode(QToolButton::InstantPopup);
        shaderLabel = new QLabel(Config);
        shaderLabel->setObjectName(QString::fromUtf8("shaderLabel"));
        shaderLabel->setGeometry(QRect(40, 30, 61, 21));
        acceleratorLabel = new QLabel(Config);
        acceleratorLabel->setObjectName(QString::fromUtf8("acceleratorLabel"));
        acceleratorLabel->setGeometry(QRect(10, 70, 191, 21));
        acceleratorButton = new QToolButton(Config);
        acceleratorButton->setObjectName(QString::fromUtf8("acceleratorButton"));
        acceleratorButton->setGeometry(QRect(240, 70, 151, 28));
        acceleratorButton->setPopupMode(QToolButton::InstantPopup);
        sceneLabel = new QLabel(Config);
        sceneLabel->setObjectName(QString::fromUtf8("sceneLabel"));
        sceneLabel->setGeometry(QRect(50, 120, 61, 21));
        sceneButton = new QToolButton(Config);
        sceneButton->setObjectName(QString::fromUtf8("sceneButton"));
        sceneButton->setGeometry(QRect(240, 120, 151, 28));
        sceneButton->setPopupMode(QToolButton::InstantPopup);
        sppSpinBox = new QSpinBox(Config);
        sppSpinBox->setObjectName(QString::fromUtf8("sppSpinBox"));
        sppSpinBox->setGeometry(QRect(240, 160, 151, 30));
        sppLabel = new QLabel(Config);
        sppLabel->setObjectName(QString::fromUtf8("sppLabel"));
        sppLabel->setGeometry(QRect(30, 160, 161, 21));
        splLabel = new QLabel(Config);
        splLabel->setObjectName(QString::fromUtf8("splLabel"));
        splLabel->setGeometry(QRect(30, 200, 161, 21));
        splSpinBox = new QSpinBox(Config);
        splSpinBox->setObjectName(QString::fromUtf8("splSpinBox"));
        splSpinBox->setGeometry(QRect(240, 200, 151, 30));

        retranslateUi(Config);
        QObject::connect(buttonBox, SIGNAL(accepted()), Config, SLOT(accept()));
        QObject::connect(buttonBox, SIGNAL(rejected()), Config, SLOT(reject()));
        QObject::connect(shaderButton, SIGNAL(triggered(QAction*)), Config, SLOT(selected_shader(QAction*)));
        QObject::connect(acceleratorButton, SIGNAL(triggered(QAction*)), Config, SLOT(selected_accelerator(QAction*)));
        QObject::connect(sceneButton, SIGNAL(triggered(QAction*)), Config, SLOT(selected_scene(QAction*)));
        QObject::connect(sppSpinBox, SIGNAL(valueChanged(int)), Config, SLOT(selected_spp(int)));
        QObject::connect(splSpinBox, SIGNAL(valueChanged(int)), Config, SLOT(selected_spl(int)));

        QMetaObject::connectSlotsByName(Config);
    } // setupUi

    void retranslateUi(QDialog *Config)
    {
        Config->setWindowTitle(QApplication::translate("Config", "Dialog", 0, QApplication::UnicodeUTF8));
        shaderButton->setText(QApplication::translate("Config", "Shader", 0, QApplication::UnicodeUTF8));
        shaderLabel->setText(QApplication::translate("Config", "Shader", 0, QApplication::UnicodeUTF8));
        acceleratorLabel->setText(QApplication::translate("Config", "Acceleration Structure", 0, QApplication::UnicodeUTF8));
        acceleratorButton->setText(QApplication::translate("Config", "Accelerator", 0, QApplication::UnicodeUTF8));
        sceneLabel->setText(QApplication::translate("Config", "Scene", 0, QApplication::UnicodeUTF8));
        sceneButton->setText(QApplication::translate("Config", "Scene", 0, QApplication::UnicodeUTF8));
        sppLabel->setText(QApplication::translate("Config", "Samples per pixel", 0, QApplication::UnicodeUTF8));
        splLabel->setText(QApplication::translate("Config", "Samples per light", 0, QApplication::UnicodeUTF8));
    } // retranslateUi

};

namespace Ui {
    class Config: public Ui_Config {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_CONFIG_H
