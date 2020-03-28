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
        shaderButton->setGeometry(QRect(240, 60, 150, 30));
        shaderButton->setAcceptDrops(false);
        shaderButton->setAutoRepeatDelay(300);
        shaderButton->setAutoRepeatInterval(100);
        shaderButton->setPopupMode(QToolButton::InstantPopup);
        shaderLabel = new QLabel(Config);
        shaderLabel->setObjectName(QString::fromUtf8("shaderLabel"));
        shaderLabel->setGeometry(QRect(40, 60, 61, 21));
        acceleratorLabel = new QLabel(Config);
        acceleratorLabel->setObjectName(QString::fromUtf8("acceleratorLabel"));
        acceleratorLabel->setGeometry(QRect(0, 110, 191, 21));
        acceleratorButton = new QToolButton(Config);
        acceleratorButton->setObjectName(QString::fromUtf8("acceleratorButton"));
        acceleratorButton->setGeometry(QRect(239, 100, 151, 28));
        acceleratorButton->setPopupMode(QToolButton::InstantPopup);
        sceneLabel = new QLabel(Config);
        sceneLabel->setObjectName(QString::fromUtf8("sceneLabel"));
        sceneLabel->setGeometry(QRect(50, 150, 61, 21));
        sceneButton = new QToolButton(Config);
        sceneButton->setObjectName(QString::fromUtf8("sceneButton"));
        sceneButton->setGeometry(QRect(240, 140, 151, 28));
        sceneButton->setPopupMode(QToolButton::InstantPopup);

        retranslateUi(Config);
        QObject::connect(buttonBox, SIGNAL(accepted()), Config, SLOT(accept()));
        QObject::connect(buttonBox, SIGNAL(rejected()), Config, SLOT(reject()));
        QObject::connect(shaderButton, SIGNAL(triggered(QAction*)), Config, SLOT(selected_shader(QAction*)));
        QObject::connect(acceleratorButton, SIGNAL(triggered(QAction*)), Config, SLOT(selected_accelerator(QAction*)));
        QObject::connect(sceneButton, SIGNAL(triggered(QAction*)), Config, SLOT(selected_scene(QAction*)));

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
    } // retranslateUi

};

namespace Ui {
    class Config: public Ui_Config {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_CONFIG_H
