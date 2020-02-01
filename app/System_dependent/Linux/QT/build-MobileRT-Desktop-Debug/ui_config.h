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
    QLabel *label;
    QLabel *label_2;
    QToolButton *acceleratorButton;

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
        label = new QLabel(Config);
        label->setObjectName(QString::fromUtf8("label"));
        label->setGeometry(QRect(40, 60, 80, 21));
        label_2 = new QLabel(Config);
        label_2->setObjectName(QString::fromUtf8("label_2"));
        label_2->setGeometry(QRect(0, 110, 191, 21));
        acceleratorButton = new QToolButton(Config);
        acceleratorButton->setObjectName(QString::fromUtf8("acceleratorButton"));
        acceleratorButton->setGeometry(QRect(239, 100, 151, 28));
        acceleratorButton->setPopupMode(QToolButton::InstantPopup);

        retranslateUi(Config);
        QObject::connect(buttonBox, SIGNAL(accepted()), Config, SLOT(accept()));
        QObject::connect(buttonBox, SIGNAL(rejected()), Config, SLOT(reject()));
        QObject::connect(shaderButton, SIGNAL(triggered(QAction*)), Config, SLOT(selected_shader(QAction*)));
        QObject::connect(acceleratorButton, SIGNAL(triggered(QAction*)), Config, SLOT(selected_accelerator(QAction*)));

        QMetaObject::connectSlotsByName(Config);
    } // setupUi

    void retranslateUi(QDialog *Config)
    {
        Config->setWindowTitle(QApplication::translate("Config", "Dialog", 0, QApplication::UnicodeUTF8));
        shaderButton->setText(QApplication::translate("Config", "Shader", 0, QApplication::UnicodeUTF8));
        label->setText(QApplication::translate("Config", "Shader", 0, QApplication::UnicodeUTF8));
        label_2->setText(QApplication::translate("Config", "Acceleration Structure", 0, QApplication::UnicodeUTF8));
        acceleratorButton->setText(QApplication::translate("Config", "Accelerator", 0, QApplication::UnicodeUTF8));
    } // retranslateUi

};

namespace Ui {
    class Config: public Ui_Config {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_CONFIG_H
