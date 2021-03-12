#include <jni.h>
#include <cstring>
#include <cstdio>
#include "base16384.h"

FILE *fp, *fpo;

#define execute(function){\
    const char *inputFileDir = env->GetStringUTFChars(sf, JNI_FALSE);\
    const char *outputFileDir = env->GetStringUTFChars(df, JNI_FALSE);\
    fp = fpo = nullptr;\
    fp = fopen(inputFileDir, "rb");\
    fpo = fopen(outputFileDir, "wb");\
    if(fp != nullptr && fpo != nullptr){\
        function(fp, fpo);\
        env->ReleaseStringUTFChars(sf, inputFileDir);\
        env->ReleaseStringUTFChars(df, outputFileDir);\
        fclose(fp);\
        fclose(fpo);\
        return 0;\
    }else return 1;\
}

extern "C" JNIEXPORT int JNICALL
Java_top_fumiama_base16384_MainActivity_encode(JNIEnv* env, jobject, jstring sf, jstring df) execute(encode)

extern "C" JNIEXPORT int JNICALL
Java_top_fumiama_base16384_MainActivity_decode(JNIEnv* env, jobject, jstring sf, jstring df) execute(decode)