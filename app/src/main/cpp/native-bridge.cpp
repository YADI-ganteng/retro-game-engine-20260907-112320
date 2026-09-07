#include <jni.h>
#include <string>
#include <android/log.h>
#include "libretro.h"

#define LOG_TAG "RetroFrontend"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// Libretro callbacks
static retro_video_refresh_t video_cb;
static retro_audio_sample_t audio_cb;
static retro_audio_sample_batch_t audio_batch_cb;
static retro_input_poll_t input_poll_cb;
static retro_input_state_t input_state_cb;
static retro_environment_t environ_cb;

// Core function pointers
static void (*retro_init)(void);
static void (*retro_deinit)(void);
static unsigned (*retro_api_version)(void);
static void (*retro_get_system_info)(struct retro_system_info*);
static void (*retro_get_system_av_info)(struct retro_system_av_info*);
static void (*retro_set_environment)(retro_environment_t);
static void (*retro_set_video_refresh)(retro_video_refresh_t);
static void (*retro_set_audio_sample)(retro_audio_sample_t);
static void (*retro_set_audio_sample_batch)(retro_audio_sample_batch_t);
static void (*retro_set_input_poll)(retro_input_poll_t);
static void (*retro_set_input_state)(retro_input_state_t);
static void (*retro_run)(void);
static bool (*retro_load_game)(const struct retro_game_info*);
static void (*retro_unload_game)(void);

// Load core
void* core_handle = nullptr;

extern "C" JNIEXPORT jint JNICALL
Java_com_retroengine_app_GameActivity_retroLoadRom(JNIEnv* env, jobject thiz, 
                                                    jstring romPath, jstring coreName) {
    const char* rom = env->GetStringUTFChars(romPath, nullptr);
    const char* core = env->GetStringUTFChars(coreName, nullptr);
    
    LOGD("Loading ROM: %s with core: %s", rom, core);
    
    // Load core .so file
    // core_handle = dlopen(core_path, RTLD_LAZY);
    
    // Get function pointers
    // retro_init = dlsym(core_handle, "retro_init");
    
    // Initialize core
    // retro_init();
    
    // Load ROM
    // struct retro_game_info info = {rom, 0, 0};
    // retro_load_game(&info);
    
    env->ReleaseStringUTFChars(romPath, rom);
    env->ReleaseStringUTFChars(coreName, core);
    
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_retroengine_app_GameActivity_retroRun(JNIEnv* env, jobject thiz) {
    // retro_run();
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_retroengine_app_GameActivity_retroStop(JNIEnv* env, jobject thiz) {
    // retro_unload_game();
    // retro_deinit();
}