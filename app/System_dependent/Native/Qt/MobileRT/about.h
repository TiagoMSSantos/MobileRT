#ifndef ABOUT_H
#define ABOUT_H

#include <QDialog>

namespace Ui {
    class About;
}

class About : public QDialog {
    Q_OBJECT

public:
    explicit About(QWidget *parent = 0);
    ~About() override;

private:
    Ui::About *ui;

public slots:
    void open_link(const QString &link);
};

#endif // ABOUT_H
