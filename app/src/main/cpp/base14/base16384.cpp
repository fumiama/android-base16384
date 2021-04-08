#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include "base16384.h"

#define execute(function){\
    const char *inputFileDir = env->GetStringUTFChars(sf, JNI_FALSE);\
    const char *outputFileDir = env->GetStringUTFChars(df, JNI_FALSE);\
    int re = function(inputFileDir, outputFileDir);\
    env->ReleaseStringUTFChars(sf, inputFileDir);\
    env->ReleaseStringUTFChars(df, outputFileDir);\
    return re;\
}

extern "C" int encode_file(const char* input, const char* output);
extern "C" int decode_file(const char* input, const char* output);

extern "C" JNIEXPORT int JNICALL
Java_top_fumiama_base16384_MainActivity_encode(JNIEnv* env, jobject, jstring sf, jstring df) execute(encode_file)

extern "C" JNIEXPORT int JNICALL
Java_top_fumiama_base16384_MainActivity_decode(JNIEnv* env, jobject, jstring sf, jstring df) execute(decode_file)