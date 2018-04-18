#include <jni.h>
#include <stdio.h>
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

#define EPT_SRC     0x401
#define EPT_DST     0x0

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

JNIEXPORT jobject JNICALL
Java_com_kynetics_ampsensors_device_DeviceManager_openDeviceNative(JNIEnv *env, jobject instance) {

    const char *returnValue = "/dev/rpmsg0";
    jstring retstr = (*env)->NewStringUTF(env, returnValue);

    int fd_ctrldev, ret;
    fd_ctrldev = open("/dev/rpmsg_ctrl0", O_RDONLY);

    jclass jcls = (*env)->FindClass(env,
                                    "com/kynetics/ampsensors/device/DeviceManager$DeviceDescriptor");
    jmethodID cstr = (*env)->GetMethodID(env, jcls, "<init>", "(ILjava/lang/String;)V");


    if (fd_ctrldev < 0) {
        printf("Error opening /dev/rpmsg_ctrl0 %s \n", strerror(errno));
        return NULL;
    }

    ret = ioctl(fd_ctrldev, RPMSG_CREATE_EPT_IOCTL, &ep);
    if (ret < 0) {
        printf("Error creating endpoint device: %s \n", strerror(errno));
        close(fd_ctrldev);
        return NULL;
    }


    jobject obj = (*env)->NewObject(env, jcls, cstr, fd_ctrldev, retstr);
    return obj;

}

JNIEXPORT void JNICALL
Java_com_kynetics_ampsensors_device_DeviceManager_closeDeviceNative(JNIEnv *env, jobject instance,
                                                                    jint fd) {
    close(fd);
}