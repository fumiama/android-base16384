cmake_minimum_required(VERSION 3.10.2)
project("base16384")

include(TestBigEndian)
test_big_endian(isBigEndian)
if (${isBigEndian})
    add_definitions(-DWORDS_BIGENDIAN)
endif()

IF(CMAKE_SIZEOF_VOID_P EQUAL 8)
    add_library(base14 SHARED base16384.cpp file.c base1464.c)
ELSE()
    add_library(base14 SHARED base16384.cpp file.c base1432.c)
ENDIF()

