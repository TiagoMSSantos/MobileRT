#ifndef JNI_CONFIG_HPP
#define JNI_CONFIG_HPP

#include <jni.h>

namespace Dependent {
    /**
     * The configurator for the JNI layer to be able to call methods from Java.
     */
    struct Config {
    public:
        /**
         * The JNI environment.
         */
        JNIEnv *env;

        /**
         * The Java class to call the method from.
         */
        jclass clazz;

        /**
         * The Java object to call the method from.
         */
        jobject object;
    };
}//namespace Dependent

#endif //JNI_CONFIG_HPP
