cmake_minimum_required(VERSION 3.10.2)
project("base16384")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -D_7ZIP_ST")

add_library(
        lzma
        SHARED
        jni/lzma.c Alloc.c LzFind.c LzmaDec.c LzmaEnc.c 7zFile.c 7zStream.c
)
