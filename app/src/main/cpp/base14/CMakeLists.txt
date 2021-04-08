cmake_minimum_required(VERSION 3.10.2)

project("base16384")

add_library(base14 SHARED base16384.cpp)

add_library(base14c STATIC base16384.c)

IF(CMAKE_SIZEOF_VOID_P EQUAL 8)
    add_definitions("-DCPUBIT64")
    add_subdirectory("./64")
    target_link_libraries(base14c base1464)
ELSE()
    add_definitions("-DCPUBIT32")
    add_subdirectory("./32")
    target_link_libraries(base14c base1432)
ENDIF()

target_link_libraries(base14 base14c)
