cmake_minimum_required(VERSION 3.10.2)
project("base16384")

IF(CMAKE_SIZEOF_VOID_P EQUAL 8)
    add_definitions("-DCPUBIT64")
    add_subdirectory("./64")
ELSE()
    add_definitions("-DCPUBIT32")
    add_subdirectory("./32")
ENDIF()

add_library(base14 SHARED base16384.cpp)
add_library(base14s STATIC base16384.c)
target_link_libraries(base14 base14s base14c)
