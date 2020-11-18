# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("asquirefilip0x08")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


# kaldi-lite source
#set(KALDI_DIR /home/darwin/Libs/kaldi_lite)
#add_subdirectory(${KALDI_DIR} ./kaldi-bin)
#include_directories(
#        ${KALDI_DIR}/include
#        ${KALDI_DIR}/include/kaldi
#        ${KALDI_DIR}/include/libsvm
#        ${KALDI_DIR}/include/openfst
#)

# Oboe source - download from github: gooogle/oboe
set(OBOE_DIR /home/darwin/Libs/oboe)
add_subdirectory(${OBOE_DIR} ./oboe-bin)
include_directories(${OBOE_DIR}/include ${OBOE_DIR}/samples/shared)

# Debug utilities
set(DEBUG_UTILS_PATH "${OBOE_DIR}/samples/debug-utils")
set(DEBUG_UTILS_SOURCES ${DEBUG_UTILS_PATH}/trace.cpp)
include_directories(${DEBUG_UTILS_PATH})


add_library( # Sets the name of the library.
        asq-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        support/kaldi/base/io-funcs.cc
        support/kaldi/base/kaldi-error.cc
        support/kaldi/base/kaldi-math.cc
        support/kaldi/base/kaldi-utils.cc
        support/kaldi/base/timer.cc
        support/kaldi/feat/feature-functions.cc
        support/kaldi/feat/feature-mfcc.cc
        support/kaldi/feat/feature-window.cc
        support/kaldi/feat/mel-computations.cc
        support/kaldi/feat/resample.cc
        support/kaldi/feat/wave-reader.cc
        support/kaldi/matrix/compressed-matrix.cc
        support/kaldi/matrix/kaldi-matrix.cc
        support/kaldi/matrix/kaldi-vector.cc
        support/kaldi/matrix/matrix-functions.cc
        support/kaldi/matrix/optimization.cc
        support/kaldi/matrix/packed-matrix.cc
        support/kaldi/matrix/qr.cc
        support/kaldi/matrix/sp-matrix.cc
        support/kaldi/matrix/sparse-matrix.cc
        support/kaldi/matrix/srfft.cc
        support/kaldi/matrix/tp-matrix.cc
        support/kaldi/transform/cmvn.cc
        support/kaldi/util/kaldi-holder.cc
        support/kaldi/util/kaldi-io.cc
        support/kaldi/util/kaldi-semaphore.cc
        support/kaldi/util/kaldi-table.cc
        support/kaldi/util/parse-options.cc
        support/kaldi/util/simple-io-funcs.cc
        support/kaldi/util/text-utils.cc
        support/kaldi/ivector/voice-activity-detection.cc
        support/kaldi/ivectorbin/compute-vad.cc
        support/libsvm/svm-predict.c
        support/libsvm/svm.cpp


        asqengine/AsqRecEngine.cpp
        asqengine/AsqPlyEngine.cpp
        prediction/Prediction.cpp
        jni-bridge.cpp)

target_include_directories(
        asq-lib
        PRIVATE
        ${OBOE_DIR}/samples/debug-utils
        ${OBOE_DIR}/include

        support/include
        support/include/kaldi
        support/include/libsvm
        support/include/openfst
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

add_library(lapack
        STATIC
        IMPORTED)

add_library(blas
        STATIC
        IMPORTED)

add_library(clapack
        STATIC
        IMPORTED)

add_library(f2c
        STATIC
        IMPORTED)

add_library(openblas
        STATIC
        IMPORTED)

set_target_properties(lapack
        PROPERTIES
        IMPORTED_LOCATION ${KALDI_DIR}/libs/${ANDROID_ABI}/liblapack.a)

set_target_properties(blas
        PROPERTIES
        IMPORTED_LOCATION ${KALDI_DIR}/libs/${ANDROID_ABI}/libblas.a)

set_target_properties(clapack
        PROPERTIES
        IMPORTED_LOCATION ${KALDI_DIR}/libs/${ANDROID_ABI}/libclapack.a)

set_target_properties(f2c
        PROPERTIES
        IMPORTED_LOCATION ${KALDI_DIR}/libs/${ANDROID_ABI}/libf2c.a)

set_target_properties(openblas
        PROPERTIES
        IMPORTED_LOCATION ${KALDI_DIR}/libs/${ANDROID_ABI}/libopenblas.a)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        asq-lib
        oboe
        android
        atomic
        clapack
        lapack
        blas
        f2c
        openblas

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

#target_compile_options(asq-lib PRIVATE -Wall -Werror "$<$<CONFIG:RELEASE>:-Ofast>")