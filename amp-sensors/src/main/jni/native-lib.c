#include <jni.h>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <poll.h>
#include <signal.h>
#include <stdlib.h>
#include <android/log.h>

#define RPMSG_CREATE_EPT_IOCTL  _IOW(0xb5, 0x1, struct rpmsg_endpoint_info)
#define RPMSG_DESTROY_EPT_IOCTL _IO(0xb5, 0x2)
#define EPT_SRC     0x401
#define EPT_DST     0x0

int sleepTime = 500000;

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
    time_t t_end, t_start;
    jstring retstr = (*env)->NewStringUTF(env, returnValue);
    int fd_ctrldev, ret;
    jclass jcls = (*env)->FindClass(env,
                                    "com/kynetics/ampsensors/device/DeviceManager$DeviceDescriptor");
    jmethodID cstr = (*env)->GetMethodID(env, jcls, "<init>", "(ILjava/lang/String;)V");
    fd_ctrldev = open("/dev/rpmsg_ctrl0", O_RDONLY);

    if (fd_ctrldev < 0) {
        return NULL;
    }

    ret = ioctl(fd_ctrldev, RPMSG_CREATE_EPT_IOCTL, &ep);
    if (ret < 0) {
        close(fd_ctrldev);
        return NULL;
    }

    t_start = time(0);
    while (access("/dev/rpmsg0", 0)) {
        t_end = time(0);

        if ((t_end - t_start) > 5) {
            close(fd_ctrldev);
            break;
        }
        usleep(sleepTime);
    }

    usleep(sleepTime);
    jobject obj = (*env)->NewObject(env, jcls, cstr, fd_ctrldev, retstr);
    return obj;
}

JNIEXPORT void JNICALL
Java_com_kynetics_ampsensors_device_DeviceManager_closeDeviceNative(JNIEnv *env, jobject instance,
                                                                    jint fd) {
    int fd_ept, ret;

    fd_ept = open("/dev/rpmsg0", O_RDONLY);
    if(fd_ept <0) {
        __android_log_print(ANDROID_LOG_INFO, "close device open", "Error destroying endpoint: %s \n",strerror(errno));
    }

    ret = ioctl(fd_ept, RPMSG_DESTROY_EPT_IOCTL);
    if (ret < 0)
        __android_log_print(ANDROID_LOG_INFO, "close device ioctl", "Error destroying endpoint: %s \n",strerror(errno));
    usleep(sleepTime);
    close(fd_ept);
    close(fd);
}