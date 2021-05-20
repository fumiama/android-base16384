//
// Created by fumiama on 2021/5/20.
//

#ifndef BASE16384_BASE16384_HPP
#define BASE16384_BASE16384_HPP
#include <stdint.h>

#ifndef CPUBIT32
    #ifndef CPUBIT64
        #define CPUBIT32
    #endif
#endif
#ifdef CPUBIT32
#define B14BUFSIZ 8192
struct LENDAT {
    uint8_t* data;
    uint32_t len;
};
typedef struct LENDAT LENDAT;

extern "C" LENDAT* encode(const uint8_t* data, const u_int32_t len);
extern "C" LENDAT* decode(const uint8_t* data, const u_int32_t len);
#endif
#ifdef CPUBIT64
#define B14BUFSIZ 16384
struct LENDAT {
    uint8_t* data;
    uint64_t len;
};
typedef struct LENDAT LENDAT;

extern "C" LENDAT* encode(const uint8_t* data, const u_int64_t len);
extern "C" LENDAT* decode(const uint8_t* data, const u_int64_t len);
#endif

extern "C" int encode_file(const char* input, const char* output);
extern "C" int decode_file(const char* input, const char* output);

#endif //BASE16384_BASE16384_HPP
