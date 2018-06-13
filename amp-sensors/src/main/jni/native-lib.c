/*
 * Copyright (c) Kynetics LLC. Author: Marta Todeschini
 *
 *               This program is free software: you can redistribute it and/or modify
 *               it under the terms of the GNU General Public License as published by
 *               the Free Software Foundation, either version 3 of the License, or
 *               (at your option) any later version.
 *
 *               This program is distributed in the hope that it will be useful,
 *               but WITHOUT ANY WARRANTY; without even the implied warranty of
 *               MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *               GNU General Public License for more details.
 *
 *               You should have received a copy of the GNU General Public License
 *               along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
#define EPT_SRC_IMU     0x401
#define EPT_SRC_STAT     0x402
#define EPT_DST     0x0
#define DEV_CREATE_TIMEOUT_SEC  5

int sleepTime = 500000;
const char *ctrlDev = "/dev/rpmsg_ctrl0";
const char *eptDev = "/dev/rpmsg0";
const char *eptStat = "/dev/rpmsg1";

struct rpmsg_endpoint_info {
    char name[32];
    uint32_t src;
    uint32_t dst;
};

struct rpmsg_endpoint_info ep_imu = {
        .name   = "rpmsg-openamp-demo-channel",
        .src    = EPT_SRC_IMU,
        .dst    = EPT_DST,
};

struct rpmsg_endpoint_info ep_stat = {
        .name = "rpmsg-openamp-demo-channel",
        .src = EPT_SRC_STAT,
        .dst = EPT_DST,
};

JNIEXPORT jobject JNICALL
Java_com_kynetics_ampsensors_device_DeviceManager_openDeviceNative(JNIEnv *env, jobject instance) {

    time_t t_end, t_start;

    /*Paths to return*/
    jstring retstrImu = (*env)->NewStringUTF(env, eptDev);
    jstring retstrStat = (*env)->NewStringUTF(env, eptStat);

    int fd_ctrldev, ret_imu, ret_stat;
    jclass jcls = (*env)->FindClass(env,
                                    "com/kynetics/ampsensors/device/DeviceManager$DeviceDescriptor");
    jmethodID cstr = (*env)->GetMethodID(env, jcls, "<init>", "(ILjava/lang/String;Ljava/lang/String;)V");


    /* Open controller device */
     fd_ctrldev = open(ctrlDev, O_RDONLY);
    if (fd_ctrldev < 0) {
        __android_log_print(ANDROID_LOG_INFO, "openDeviceNative", "Error opening controller device: %s \n",strerror(errno));
        return NULL;
    }

    /* Create endpoint device */
    ret_imu = ioctl(fd_ctrldev, RPMSG_CREATE_EPT_IOCTL, &ep_imu);
    if (ret_imu < 0) {
        __android_log_print(ANDROID_LOG_INFO, "openDeviceNative", "Error creating endpoint device: %s \n",strerror(errno));
        close(fd_ctrldev);
        return NULL;
    }

    /* Create endpoint device for statistics*/
    ret_stat = ioctl(fd_ctrldev, RPMSG_CREATE_EPT_IOCTL, &ep_stat);
    if (ret_stat < 0) {
        __android_log_print(ANDROID_LOG_INFO, "openDeviceNative", "Error creating endpoint device for statistics: %s \n",strerror(errno));
        close(fd_ctrldev);
        return NULL;
    }

    /* Check endpoint device creation */
    t_start = time(0);
    while (access(eptDev, 0)) {
        t_end = time(0);
        if ((t_end - t_start) > DEV_CREATE_TIMEOUT_SEC) {
            __android_log_print(ANDROID_LOG_INFO, "openDeviceNative", "Device is taking too long to be created, aborting...\n");
            close(fd_ctrldev);
            break;
        }
        usleep(sleepTime);
    }
    /* Check endpoint device creation */
    t_start = time(0);
    while (access(eptStat, 0)) {
        t_end = time(0);
        if ((t_end - t_start) > DEV_CREATE_TIMEOUT_SEC) {
            __android_log_print(ANDROID_LOG_INFO, "openDeviceNative", "Device is taking too long to be created, aborting...\n");
            close(fd_ctrldev);
            break;
        }
        usleep(sleepTime);
    }

    usleep(sleepTime);
    jobject obj = (*env)->NewObject(env, jcls, cstr, fd_ctrldev, retstrImu, retstrStat);
    return obj;
}

JNIEXPORT void JNICALL
Java_com_kynetics_ampsensors_device_DeviceManager_closeDeviceNative(JNIEnv *env, jobject instance,
                                                                    jint fd) {
    int fd_ept_imu, fd_ept_stat, ret_imu, ret_stat;

    fd_ept_imu = open(eptDev, O_RDONLY);
    if(fd_ept_imu <0) {
        __android_log_print(ANDROID_LOG_INFO, "closeDeviceNative", "Error opening endpoint device: %s \n",strerror(errno));
    }

    fd_ept_stat = open(eptStat, O_RDONLY);
    if(fd_ept_stat <0) {
        __android_log_print(ANDROID_LOG_INFO, "closeDeviceNative", "Error opening endpoint device statistics: %s \n",strerror(errno));
    }


    /* Destroy endpoint */
    ret_imu = ioctl(fd_ept_imu, RPMSG_DESTROY_EPT_IOCTL);
    if (ret_imu < 0) {
        __android_log_print(ANDROID_LOG_INFO, "closeDeviceNative", "Error destroying endpoint: %s \n",strerror(errno));
        }

    ret_stat = ioctl(fd_ept_stat, RPMSG_DESTROY_EPT_IOCTL);
    if (ret_stat < 0) {
        __android_log_print(ANDROID_LOG_INFO, "closeDeviceNative", "Error destroying endpoint: %s \n",strerror(errno));
    }

    usleep(sleepTime);
    close(fd_ept_imu);
    close(fd_ept_stat);
    close(fd);
}