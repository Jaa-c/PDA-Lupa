LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := rgb
LOCAL_SRC_FILES := rgb.c
LOCAL_CFLAGS := -g

include $(BUILD_SHARED_LIBRARY)
