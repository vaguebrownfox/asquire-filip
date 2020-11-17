//
// Created by darwin on 9/11/20.
//

#include "AsqPlyEngine.h"

extern int32_t g_sample_rate;
extern oboe::AudioFormat g_format;
extern oboe::ChannelCount g_n_ch;
extern bool g_is_rec_on;
extern size_t g_total_data_size;

const char* g_ply_buffer_path;
bool g_is_ply_on;