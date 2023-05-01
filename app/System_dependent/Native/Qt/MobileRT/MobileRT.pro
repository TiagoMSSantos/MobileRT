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

INCLUDEPATH += ../../../../
INCLUDEPATH += ../../../../System_dependent/Native
INCLUDEPATH += ../../../../third_party
INCLUDEPATH += ../../../../third_party/glm
INCLUDEPATH += ../../../../third_party/boost/libs/assert/include
INCLUDEPATH += ../../../../third_party/pcg-cpp/include
INCLUDEPATH += ../../../../Scenes

# Use release version by default! Only use debug version to debug some bug.
INCLUDEPATH += ../../../../System_dependent/Native/Qt/build-Release
LIBS += -L../../../../../build_release/lib -lMobileRT -lComponents
#INCLUDEPATH += ../../../../System_dependent/Native/Qt/build-Debug
#LIBS += -L../../../../../build_debug/lib -lMobileRTd -lComponentsd

SOURCES += \
        ../../../../Scenes/Scenes.cpp \
        ../../../../System_dependent/Native/C_wrapper.cpp \
        ../../../../System_dependent/Native/Utils_dependent.cpp \
        ../../../../System_dependent/Native/Qt/MobileRT/main.cpp \
        ../../../../System_dependent/Native/Qt/MobileRT/about.cpp \
        ../../../../System_dependent/Native/Qt/MobileRT/config.cpp \
        ../../../../System_dependent/Native/Qt/MobileRT/mainwindow.cpp

HEADERS += \
        ../../../../System_dependent/Native/Qt/MobileRT/mainwindow.h \
        ../../../../System_dependent/Native/Qt/MobileRT/about.h \
        ../../../../System_dependent/Native/Qt/MobileRT/config.h

FORMS += \
        mainwindow.ui
