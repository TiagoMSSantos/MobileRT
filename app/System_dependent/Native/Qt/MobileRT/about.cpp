#include "about.h"
#include "ui_about.h"

#include <iostream>
#include <QDesktopServices>
#include <QUrl>

About::About(QWidget *parent) :
    QDialog(parent),
    ui(new Ui::About) {
    ui->setupUi(this);
}

About::~About() {
    delete ui;
}

void About::open_link(const QString &link) {
    ::std::cout << "Opening link: " << link.toStdString() << ::std::endl;
    QDesktopServices::openUrl(QUrl(link));
}
