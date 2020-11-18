#include <jni.h>
#include <string>
#include <logging_macros.h>
#include "asqengine/AsqRecEngine.h"
#include "prediction/Prediction.h"

AsqRecEngine *rengine = nullptr;

extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_create(JNIEnv *env, jclass clazz) {
    if (rengine != nullptr)
        return (rengine != nullptr /*&& pengine != nullptr*/) ? JNI_TRUE : JNI_FALSE;
    rengine = new AsqRecEngine();
    //	if (pengine == nullptr) {
//		pengine = new AsqPlyEngine();
//	}

    return (rengine != nullptr /*&& pengine != nullptr*/) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT void JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_delete(JNIEnv *env, jclass clazz) {
    if (rengine != nullptr) {
        delete rengine;
        rengine = nullptr;
//	delete pengine;
//	pengine = nullptr;
    } else {
        LOGE(
                "Engine is null, you must call createEngine before calling this "
                "method");
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_setRecOn(JNIEnv *env, jclass clazz,
                                                                  jboolean is_rec_on) {
    if (rengine != nullptr) return rengine->setRecOn(is_rec_on) ? JNI_TRUE : JNI_FALSE;
    LOGE(
            "Engine is null, you must call createEngine before calling this "
            "method");
    return JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_setPlyOn(JNIEnv *env, jclass clazz,
                                                                  jboolean is_ply_on) {
    // TODO: implement setPlyOn()
    return JNI_FALSE;
}

extern "C"
JNIEXPORT void JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_setRecFilePath(JNIEnv *env, jclass clazz,
                                                                        jstring rec_file_path) {
    if (rengine != nullptr) {
        const char *path = (*env).GetStringUTFChars(rec_file_path, nullptr);
        rengine->setRecFilePath(path);
    } else {
        LOGE(
                "Engine is null, you must call createEngine before calling this "
                "method");
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_setPlyBufferPath(JNIEnv *env, jclass clazz,
                                                                          jstring ply_buffer_path) {
    // TODO: implement setPlyBufferPath()
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_isRecOn(JNIEnv *env, jclass clazz) {
    if (rengine != nullptr) return rengine->isRecOn() ? JNI_TRUE : JNI_FALSE;
    LOGE(
            "Engine is null, you must call createEngine before calling this "
            "method");
    return JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_isPlyOn(JNIEnv *env, jclass clazz) {
    // TODO: implement isPlyOn()
    return JNI_FALSE;
}

extern "C"
JNIEXPORT void JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_native_1setDefaultStreamValues(JNIEnv *env,
                                                                                        jclass clazz,
                                                                                        jint default_sample_rate,
                                                                                        jint default_frames_per_burst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) default_sample_rate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) default_frames_per_burst;
}


extern "C"
JNIEXPORT void JNICALL
Java_aashi_fiaxco_asquirefilip0x08_audioengine_AsqEngine_asqPredict(JNIEnv *env, jclass clazz,
                                                                    jstring model_file_path) {

    const char *modelFilePath = env->GetStringUTFChars(model_file_path, nullptr);
    const char *wavFilePath = rengine->getWavFilePath();

    auto* asqPrediction = new Prediction(wavFilePath, modelFilePath);

    asqPrediction->asqPredict();

	delete asqPrediction;
}