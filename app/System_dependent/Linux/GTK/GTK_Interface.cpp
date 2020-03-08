#include "System_dependent/Linux/c_wrapper.h"
#include "MobileRT/Utils.hpp"
#include <cmath>
#include <gdk/gdkkeysyms.h>
#include <gtk/gtk.h>

::std::int32_t main(::std::int32_t argc, char **argv) {
    if (argc != 16) {
        LOG("Wrong number of arguments: ", argc, ", must be 16");
        ::std::cin.ignore();
        ::std::exit(1);
    }

    const ::std::int32_t threads {static_cast<::std::int32_t> (strtol(argv[1], nullptr, 0))};
    const ::std::int32_t shader {static_cast<::std::int32_t> (strtol(argv[2], nullptr, 0))};
    const ::std::int32_t scene {static_cast<::std::int32_t> (strtol(argv[3], nullptr, 0))};
    const ::std::int32_t samplesPixel {static_cast<::std::int32_t> (strtol(argv[4], nullptr, 0))};
    const ::std::int32_t samplesLight {static_cast<::std::int32_t> (strtol(argv[5], nullptr, 0))};

    const ::std::int32_t width_ {
            ::MobileRT::roundDownToMultipleOf(static_cast<::std::int32_t> (strtol(argv[6], nullptr, 0)),
                                              static_cast<::std::int32_t> (::std::sqrt(
                                                      ::MobileRT::NumberOfTiles)))};

    const ::std::int32_t height_ {
            ::MobileRT::roundDownToMultipleOf(static_cast<::std::int32_t> (strtol(argv[7], nullptr, 0)),
                                              static_cast<::std::int32_t> (::std::sqrt(
                                                      ::MobileRT::NumberOfTiles)))};

    const ::std::int32_t accelerator {static_cast<::std::int32_t> (strtol(argv[8], nullptr, 0))};

    const ::std::int32_t repeats {static_cast<::std::int32_t> (strtol(argv[9], nullptr, 0))};
    const char *const pathObj {argv[10]};
    const char *const pathMtl {argv[11]};
    const char *const pathCam {argv[12]};

    ::std::istringstream ssPrintStdOut (argv[13]);
    ::std::istringstream ssAsync (argv[14]);
    ::std::istringstream ssShowImage (argv[15]);
    bool printStdOut {true};
    bool async {true};
    bool showImage {true};

    ssPrintStdOut >> ::std::boolalpha;
    ssPrintStdOut >> printStdOut;
    ssAsync >> ::std::boolalpha >> async;
    ssShowImage >> ::std::boolalpha >> showImage;

    const ::std::uint32_t size {static_cast<::std::uint32_t> (width_) * static_cast<::std::uint32_t> (height_)};
    ::std::vector<::std::int32_t> bitmap (size);

    RayTrace(bitmap.data(), width_, height_, threads, shader, scene, samplesPixel, samplesLight,
             repeats, accelerator, printStdOut, async, pathObj, pathMtl, pathCam);

    if (!showImage) {
        return 0;
    }

    gtk_init(&argc, &argv);
    GtkWidget *const window {gtk_window_new(GTK_WINDOW_TOPLEVEL)};
    gtk_signal_connect(GTK_OBJECT(window), "destroy", GTK_SIGNAL_FUNC(
            []() -> void {
                gtk_main_quit();
            }
    ), nullptr);
    auto key_handler (static_cast<bool (*)(
            GtkWidget *, GdkEventKey *, gpointer)> (
                               [](GtkWidget *const gtkWidget, GdkEventKey *const event,
                                  gpointer /*user_data*/) -> bool {
                                    switch (event->keyval) {
                                        case GDK_KEY_Escape:
                                            gtk_widget_destroy(gtkWidget);
                                            gtk_main_quit();
                                            return true;
                                    }
                                   return false;
                               })
    );
    gtk_signal_connect(
        GTK_OBJECT(window), "key_press_event", GTK_SIGNAL_FUNC(key_handler), nullptr);

   GdkPixbuf *const pixbuff {
        gdk_pixbuf_new_from_data(reinterpret_cast<unsigned char*> (bitmap.data()),
        GDK_COLORSPACE_RGB, TRUE, 8,
        static_cast<::std::int32_t> (width_), static_cast<::std::int32_t> (height_),
        static_cast<::std::int32_t> (width_ * 4), nullptr, nullptr)};
    GtkWidget *const image {gtk_image_new_from_pixbuf(pixbuff)};
    gtk_container_add(GTK_CONTAINER(window), image);
    gtk_widget_show_all(window);
    g_timeout_add(20, static_cast<::std::int32_t (*)(
        gpointer)> (
            [](gpointer userData) -> ::std::int32_t {
                gtk_widget_queue_draw (reinterpret_cast<GtkWidget*> (userData));
                return 1;
            }), window);
    gtk_main();
    g_object_unref(G_OBJECT(pixbuff));
    return argc;
}
