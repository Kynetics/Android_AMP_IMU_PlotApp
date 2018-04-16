#include <jni.h>

JNIEXPORT jobjectArray JNICALL
Java_com_example_marta_hello_1cmake_MainActivity_arrayFromJNI(JNIEnv *env, jobject instance) {

    double balance[] = {1000.0, 2.0, 3.4, 7.0, 50.0};
    return (*env)->NewDoubleArray(env, balance);

}

