LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_CAMERA_MODULES:=off
#OPENCV_INSTALL_MODULES:=on
#OPENCV_LIB_TYPE:=SHARED
include C:\Users\Dani\Universidad\OpenCV\OpenCV-2.4.9-android-sdk\sdk\native\jni\OpenCV.mk

LOCAL_SRC_FILES  := ProcessFrame.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE     := ProcessFrame

include $(BUILD_SHARED_LIBRARY)
