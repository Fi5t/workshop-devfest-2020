#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_redmadrobot_vulnerableapp_ui_login_LoginViewModel_getApiKey(JNIEnv *env, jobject thiz) {
    std::string apiKey = "b3b4e12487f9a41a613ad2d05237752acfba1047d8556551bd103bd96a98a057";
    return env->NewStringUTF(apiKey.c_str());
}
