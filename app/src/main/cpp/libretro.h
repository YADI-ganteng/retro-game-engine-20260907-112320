
#ifndef LIBRETRO_H__
#define LIBRETRO_H__

#include <stdint.h>
#include <stddef.h>

#define RETRO_API_VERSION 1

struct retro_game_info {
    const char *path;
    const void *data;
    size_t size;
};

struct retro_system_info {
    const char *library_name;
    const char *library_version;
    const char *valid_extensions;
    bool need_fullpath;
    bool block_extract;
};

struct retro_system_av_info {
    unsigned geometry_base_width;
    unsigned geometry_base_height;
    unsigned geometry_max_width;
    unsigned geometry_max_height;
    float geometry_aspect_ratio;
    unsigned timing_fps;
    double timing_sample_rate;
};

typedef void (*retro_video_refresh_t)(const void*, unsigned, unsigned, size_t);
typedef void (*retro_audio_sample_t)(int16_t, int16_t);
typedef size_t (*retro_audio_sample_batch_t)(const int16_t*, size_t);
typedef void (*retro_input_poll_t)(void);
typedef int16_t (*retro_input_state_t)(unsigned, unsigned, unsigned, unsigned);
typedef bool (*retro_environment_t)(unsigned, void*);

#endif
