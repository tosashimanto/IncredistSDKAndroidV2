LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libcrypto
LOCAL_SRC_FILES := $(LOCAL_PATH)/libs/$(TARGET_ARCH_ABI)/lib/libcrypto.a
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/libs/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_LDLIBS:=-llog
LOCAL_STATIC_LIBRARIES += libcrypto
LOCAL_MODULE:= Dukpt
LOCAL_SRC_FILES:= FL_DES.cpp Dukpt.cpp Dukptjni.cpp

include $(BUILD_SHARED_LIBRARY)
