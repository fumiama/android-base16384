#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include "base16384.hpp"

#define execute(function){\
    const char *inputFileDir = env->GetStringUTFChars(sf, JNI_FALSE);\
    const char *outputFileDir = env->GetStringUTFChars(df, JNI_FALSE);\
    int re = function(inputFileDir, outputFileDir);\
    env->ReleaseStringUTFChars(sf, inputFileDir);\
    env->ReleaseStringUTFChars(df, outputFileDir);\
    return re;\
}

#define exe_byte(fun) {\
uint32_t len = env->GetArrayLength(buf);\
    const uint8_t* data = (uint8_t*)env->GetByteArrayElements(buf, JNI_FALSE);\
    LENDAT* ld = fun(data, len);\
    jbyteArray out = env->NewByteArray(ld->len);\
    auto out_data = env->GetByteArrayElements(out, JNI_FALSE);\
    memcpy(out_data, ld->data, ld->len);\
    free(ld);\
    env->ReleaseByteArrayElements(out, out_data, JNI_COMMIT);\
    return out;\
}

extern "C" JNIEXPORT int JNICALL
Java_top_fumiama_base16384_MainActivity_encode(JNIEnv* env, jobject, jstring sf, jstring df) execute(encode_file)

extern "C" JNIEXPORT int JNICALL
Java_top_fumiama_base16384_MainActivity_decode(JNIEnv* env, jobject, jstring sf, jstring df) execute(decode_file)

extern "C" JNIEXPORT jbyteArray JNICALL
Java_top_fumiama_base16384_MainActivity_encodeByteArray(JNIEnv* env, jobject, jbyteArray buf) exe_byte(encode)

extern "C" JNIEXPORT jbyteArray JNICALL
Java_top_fumiama_base16384_MainActivity_decodeByteArray(JNIEnv* env, jobject, jbyteArray buf) exe_byte(decode)