#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <stdint.h>
#include <sys/ioctl.h>
#include <poll.h>
#include <signal.h>
#include <stdlib.h>
#define RPMSG_CREATE_EPT_IOCTL  _IOW(0xb5, 0x1, struct rpmsg_endpoint_info)
#define RPMSG_DESTROY_EPT_IOCTL _IO(0xb5, 0x2)

#define EPT_SRC     0x401
#define EPT_DST     0x0

jint fd_global;

struct rpmsg_endpoint_info {
    char name[32];
    uint32_t src;
    uint32_t dst;
};

struct rpmsg_endpoint_info ep = {
        .name   = "rpmsg-openamp-demo-channel",
        .src    = EPT_SRC,
        .dst    = EPT_DST,
};

JNIEXPORT jfloatArray JNICALL
Java_com_example_marta_hello_1cmake_ModuleFragment_arrayModuleFromJNI(JNIEnv *env, jobject instance) {


    jfloatArray result;
    result = (*env)->NewFloatArray(env, 30);
    jfloat arrayTot[30];

    jint fd, ret;
    fd = open("/dev/random", O_RDONLY);

    if(fd >= 0)
    {
        __android_log_print(ANDROID_LOG_INFO, "read dev", "open ok\n");
        ret = read(fd, &arrayTot, sizeof(arrayTot));
        __android_log_print(ANDROID_LOG_INFO, "read dev","read ok \n");
        (*env)->SetFloatArrayRegion(env, result, 0, 30, arrayTot);

    }
    else
    {
        __android_log_print(ANDROID_LOG_INFO, "read dev","Error opening /dev/zero %s \n", strerror(errno));
        __android_log_print(ANDROID_LOG_INFO, "read dev", "close fp\n");
        for(int i = 0; i < sizeof(arrayTot); i++){
            arrayTot[i]=0;
        }

        (*env)->SetFloatArrayRegion(env, result, 0, 10, arrayTot);
        return result;

    }

    return result;

}


JNIEXPORT jfloatArray JNICALL
Java_com_example_marta_hello_1cmake_RawFragment_arrayRawFromJNI(JNIEnv *env, jobject instance) {


    jfloatArray result;
    result = (*env)->NewFloatArray(env, 11);
    jfloat array1[11];

    array1[0] = 1;
    array1[1] = 2;
    array1[2] = 3.4;
    array1[3] = 4;
    array1[4] = 5;
    array1[5] = 6;
    array1[6] = 7;
    array1[7] = 8;
    array1[8] = 9;
    array1[9] = 10;
    array1[10] = 11;

    (*env)->SetFloatArrayRegion(env, result, 0, 11, array1);
    return result;



}

JNIEXPORT jstring JNICALL
Java_com_example_marta_hello_1cmake_MainActivity_createDevice(JNIEnv *env, jobject instance) {

    char *returnValue = "/dev/rpmsg0";

    int fd_ctrldev , ret;
    fd_ctrldev = open("/dev/rpmsg_ctrl0", O_RDONLY);
    fd_global = fd_ctrldev;

    if(fd_ctrldev <0){
        printf("Error opening /dev/rpmsg_ctrl0 %s \n", strerror(errno));
        return "-1";
    }

    ret = ioctl(fd_ctrldev, RPMSG_CREATE_EPT_IOCTL, &ep);
    if (ret < 0) {
        printf("Error creating endpoint device: %s \n", strerror(errno));
        close(fd_ctrldev);
        return "-1";
    }

    return (*env)->NewStringUTF(env, returnValue);
}

JNIEXPORT jstring JNICALL
Java_com_example_marta_hello_1cmake_MainActivity_destroyDevice(JNIEnv *env, jobject instance) {

    char *returnValue = "Ok";
    close(fd_global);

    return (*env)->NewStringUTF(env, returnValue);
}

JNIEXPORT jstring JNICALL
Java_com_example_marta_hello_1cmake_ModuleFragment_destroyDevice(JNIEnv *env, jobject instance) {
    __android_log_print(ANDROID_LOG_INFO, "destroyDevice", "START\n");

    char *returnValue = "Ok";
    int ret = close(fd_global);
    if (ret < 0) {
//        printf("Error destroying: %s \n", strerror(errno));
        __android_log_print(ANDROID_LOG_INFO, "destroyDevice error  ", "%s", strerror(errno));
        return "-1";
    }
    __android_log_print(ANDROID_LOG_INFO, "destroyDevice", "END\n");


    return (*env)->NewStringUTF(env, returnValue);

}