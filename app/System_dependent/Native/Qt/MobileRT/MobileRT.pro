#-------------------------------------------------
#
# Project created by QtCreator 2020-01-19T12:29:56
#
#-------------------------------------------------

QT += core gui widgets

TARGET = MobileRT
TEMPLATE = app

# The following define makes your compiler emit warnings if you use
# any feature of Qt which has been marked as deprecated (the exact warnings
# depend on your compiler). Please consult the documentation of the
# deprecated API in order to know how to port your code away from it.
DEFINES += QT_DEPRECATED_WARNINGS

# You can also make your code fail to compile if you use deprecated APIs.
# In order to do so, uncomment the following line.
# You can also select to disable deprecated APIs only up to a certain version of Qt.
#DEFINES += QT_DISABLE_DEPRECATED_BEFORE=0x060000    # disables all the APIs deprecated before Qt 6.0.0

INCLUDEPATH += /mnt/D/Projects/MobileRT/app
INCLUDEPATH += /mnt/D/Projects/MobileRT/app/System_dependent/Native
INCLUDEPATH += /mnt/D/Projects/MobileRT/app/third_party
INCLUDEPATH += /mnt/D/Projects/MobileRT/app/third_party/boost/libs/assert/include
INCLUDEPATH += /mnt/D/Projects/MobileRT/app/third_party/pcg-cpp/include
INCLUDEPATH += /mnt/D/Projects/MobileRT/app/Scenes

#LIBS += -L/mnt/D/Projects/MobileRT/build_debug/lib -lMobileRTd -lComponentsd
LIBS += -L/mnt/D/Projects/MobileRT/build_release/lib
LIBS += -lMobileRT -lComponents

SOURCES += \
        /mnt/D/Projects/MobileRT/app/Scenes/Scenes.cpp \
        /mnt/D/Projects/MobileRT/app/System_dependent/Native/C_wrapper.cpp \
        /mnt/D/Projects/MobileRT/app/System_dependent/Native/Utils_dependent.cpp \
        /mnt/D/Projects/MobileRT/app/System_dependent/Native/Qt/MobileRT/main.cpp \
        /mnt/D/Projects/MobileRT/app/System_dependent/Native/Qt/MobileRT/about.cpp \
        /mnt/D/Projects/MobileRT/app/System_dependent/Native/Qt/MobileRT/config.cpp \
        /mnt/D/Projects/MobileRT/app/System_dependent/Native/Qt/MobileRT/mainwindow.cpp

HEADERS += \
        /mnt/D/Projects/MobileRT/app/System_dependent/Native/Qt/MobileRT/mainwindow.h \
        /mnt/D/Projects/MobileRT/app/System_dependent/Native/Qt/MobileRT/about.h \
        /mnt/D/Projects/MobileRT/app/System_dependent/Native/Qt/MobileRT/config.h

FORMS += \
        mainwindow.ui
