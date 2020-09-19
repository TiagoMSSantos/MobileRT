/********************************************************************************
** Form generated from reading UI file 'about.ui'
**
** Created by: Qt User Interface Compiler version 4.8.7
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_ABOUT_H
#define UI_ABOUT_H

#include <QtCore/QVariant>
#include <QAction>
#include <QApplication>
#include <QButtonGroup>
#include <QDialog>
#include <QDialogButtonBox>
#include <QHeaderView>
#include <QLabel>

QT_BEGIN_NAMESPACE

class Ui_About
{
public:
    QDialogButtonBox *buttonBox;
    QLabel *label;

    void setupUi(QDialog *About)
    {
        if (About->objectName().isEmpty())
            About->setObjectName(QString::fromUtf8("About"));
        About->resize(500, 300);
        buttonBox = new QDialogButtonBox(About);
        buttonBox->setObjectName(QString::fromUtf8("buttonBox"));
        buttonBox->setGeometry(QRect(30, 240, 341, 32));
        buttonBox->setOrientation(Qt::Horizontal);
        buttonBox->setStandardButtons(QDialogButtonBox::Ok);
        label = new QLabel(About);
        label->setObjectName(QString::fromUtf8("label"));
        label->setGeometry(QRect(10, 90, 481, 81));
        label->setStyleSheet(QString::fromUtf8(""));
        label->setTextFormat(Qt::RichText);
        label->setOpenExternalLinks(false);
        label->setTextInteractionFlags(Qt::TextBrowserInteraction);

        retranslateUi(About);
        QObject::connect(buttonBox, SIGNAL(accepted()), About, SLOT(accept()));
        QObject::connect(buttonBox, SIGNAL(rejected()), About, SLOT(reject()));
        QObject::connect(label, SIGNAL(linkActivated(QString)), About, SLOT(open_link(QString)));

        QMetaObject::connectSlotsByName(About);
    } // setupUi

    void retranslateUi(QDialog *About)
    {
        About->setWindowTitle(QApplication::translate("About", "About", 0));
        label->setText(QApplication::translate("About", "<html><head/><body><p>Made by: Tiago <br/><a href=\"https://tiagomssantos.github.io/\"><span style=\" font-weight:600; text-decoration: underline; color:#0000ff;\">https://tiagomssantos.github.io/</span></a></p></body></html>", 0));
    } // retranslateUi

};

namespace Ui {
    class About: public Ui_About {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_ABOUT_H
