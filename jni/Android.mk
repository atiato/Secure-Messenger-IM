LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libecc
LOCAL_SRC_FILES := protocols1.cpp \
					eliptic.cpp \
					onb.cpp \
					fastdes.cpp

include $(BUILD_SHARED_LIBRARY)

LOCAL_LDLIBS := -llog -landroid
LOCAL_JNI_SHARD_LIBRARIES := libecc
LOCAL_REQUIRED_MODULES := libtecc

APP_ABI := all



