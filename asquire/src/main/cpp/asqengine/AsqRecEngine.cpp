//
// Created by darwin on 9/11/20.
//

#include <logging_macros.h>
#include "AsqRecEngine.h"

extern const char* g_ply_buffer_path;
extern bool g_is_ply_on;

int32_t g_sample_rate;
oboe::AudioFormat g_format;
oboe::ChannelCount g_n_ch;
bool g_is_rec_on;
size_t g_total_data_size;

AsqRecEngine::AsqRecEngine() {
	// Rec stream parameters
	mDirection = oboe::Direction::Input;
	mChannelCount = oboe::ChannelCount::Mono;
	mFormat = oboe::AudioFormat::I16;
	mSampleRate = oboe::kUnspecified;
}

AsqRecEngine::~AsqRecEngine() {
	stop();
	closeRecStream();
}

oboe::AudioStreamBuilder *
AsqRecEngine::setupRecordStreamParameters(oboe::AudioStreamBuilder *builder) {
	builder->setCallback(nullptr)
			->setDirection(mDirection)
			->setSharingMode(oboe::SharingMode::Exclusive)
			->setPerformanceMode(oboe::PerformanceMode::LowLatency)
			->setFormat(mFormat)
			->setChannelCount(mChannelCount)
			->setUsage(oboe::Usage::Media)
			->setContentType(oboe::ContentType::Speech)
			->setInputPreset(oboe::InputPreset::VoicePerformance);

	return builder;
}

oboe::Result AsqRecEngine::openRecStream() {
	oboe::AudioStreamBuilder inStreamBuilder;
	setupRecordStreamParameters(&inStreamBuilder);

	oboe::Result result = inStreamBuilder.openStream(mRecordStream);

	if (result != oboe::Result::OK) {
		LOGD("Asquire Rec Engine: openStream: Failed - %s", oboe::convertToText(result));
		return result;
	} else {
		LOGD("Asquire Rec Engine: openStream: Success - %s", oboe::convertToText(result));
	}

	mSampleRate = mRecordStream->getSampleRate();
	mRecordStream->setBufferSizeInFrames(mRecordStream->getFramesPerBurst() * 2);

	//TODO: Set all global stream parameters
	g_sample_rate = mSampleRate;
	g_format = mRecordStream->getFormat();
	g_n_ch  = mChannelCount;
	// g_

	return result;
}

void AsqRecEngine::closeRecStream() {
	oboe::Result result;

	if (mRecordStream) {
		oboe::StreamState inputState = oboe::StreamState::Stopping;
		oboe::StreamState nextState = oboe::StreamState::Uninitialized;
		int64_t timeoutNanos = 100 * oboe::kNanosPerMillisecond;
		result = mRecordStream->requestStop();
		if (result != oboe::Result::OK)
			result = mRecordStream->waitForStateChange(inputState, &nextState, timeoutNanos);

		if (result != oboe::Result::OK) {
			LOGW("Asquire Rec Engine closeStream: Error stopping stream: %s",
			     oboe::convertToText(result));
		}
		result = mRecordStream->close();
		if (result != oboe::Result::OK) {
			LOGW("Asquire Rec Engine closeStream: Error closing stream: %s",
			     oboe::convertToText(result));
		} else {
			LOGW("Asquire Rec Engine closeStream: Successfully closed streams: %s",
			     oboe::convertToText(result));
		}
		mRecordStream.reset();
	}
}


oboe::Result AsqRecEngine::start() {

	wavFileWriter();

	oboe::StreamState inputState = oboe::StreamState::Starting;
	oboe::StreamState nextState = oboe::StreamState::Uninitialized;
	int64_t timeoutNanos = 100 * oboe::kNanosPerMillisecond;
	oboe::Result result = mRecordStream->requestStart();
	if (result != oboe::Result::OK)
		result = mRecordStream->waitForStateChange(inputState, &nextState, timeoutNanos);

	if (result != oboe::Result::OK) {
		LOGD("Asquire Rec Engine: start: Failed - %s", oboe::convertToText(result));
		return result;
	} else {
		LOGD("Asquire Rec Engine: start: Success - %s", oboe::convertToText(result));

	}


	return result;
}


oboe::Result AsqRecEngine::stop() {
	oboe::Result result = oboe::Result::OK;

	if (mRecFile.is_open()) {
		wavFileFinish();
		LOGD("Asquire Rec Engine: stop: wave file finish");
	}

	if (mRecordStream) {
		oboe::StreamState inputState = oboe::StreamState::Stopping;
		oboe::StreamState nextState = oboe::StreamState::Uninitialized;
		int64_t timeoutNanos = 100 * oboe::kNanosPerMillisecond;
		result = mRecordStream->requestStop();
		if (result != oboe::Result::OK)
			result = mRecordStream->waitForStateChange(inputState, &nextState, timeoutNanos);

		if (result != oboe::Result::OK) {
			LOGD("Asquire Rec Engine: stop: Failed - %s", oboe::convertToText(result));
			return result;
		} else {
			LOGD("Asquire Rec Engine: stop: Success - %s", oboe::convertToText(result));
		}
	}

	LOGD("Asquire Rec Engine: stop: Rec stream undefined");
	return result;
}



bool AsqRecEngine::setRecOn(bool isOn) {
	bool success = true;

	if (isOn != mIsRecOn) {
		if (isOn) {
			success = openRecStream() == oboe::Result::OK;
			if (success) {
				success = start() == oboe::Result::OK;
				mIsRecOn = success;
				g_is_rec_on = mIsRecOn;
				recordStream();
			}
		} else {
			mIsRecOn = isOn;
			g_is_rec_on = mIsRecOn;
			success = stop() == oboe::Result::OK;
			closeRecStream();
		}
	}

	return success;
}

/**
 *
 */
void AsqRecEngine::recordStream() {

	constexpr int kMillisecondsToRecord = 2;
	const auto requestedFrames = (int32_t) (kMillisecondsToRecord *
	                                        (g_sample_rate / oboe::kMillisPerSecond));
	int16_t buffer[requestedFrames];

	constexpr int64_t kTimeoutValue = 3 * oboe::kNanosPerMillisecond;
	auto bufferSize = mRecordStream->getBufferSizeInFrames();

	int framesRead;
	do {
		auto result = mRecordStream->read(buffer, bufferSize, 0);
		if (result != oboe::Result::OK)
			break;
		framesRead = result.value();
	} while (framesRead != 0);

	while (g_is_rec_on) {
		auto result = mRecordStream->read(buffer, requestedFrames, kTimeoutValue);

		if (result == oboe::Result::OK) {
			int32_t numFrames = result.value();
			LOGD("Asquire Rec Engine: recordStream: Read %d frames", numFrames);
			for (int i = 0; i < numFrames; i++) {

				little_endian_io::writeWord(mRecFile, buffer[i], 2);
				//big_endian_io::writeWord(mPlyBuffer, buffer[i]);
//				mPlyBuffer << std::flush;
//				mRecFile << std::flush;
			}
		} else {
			LOGD("Asquire Rec Engine: recordStream: Failed reading stream");
		}
	}

}

/**
 *
 */
void AsqRecEngine::wavFileWriter() {

	mRecFile.open(mRecFilePath, std::ios::binary);
	//mPlyBuffer.open(g_ply_buffer_path, std::ios::binary);

	int Fs = g_sample_rate;
	int nCh = g_n_ch;
	int bPS = 16; // bits per sample
	using namespace little_endian_io;
	// Write the file headers
	mRecFile << "RIFF----WAVEfmt ";     // (chunk size to be filled in later)
	writeWord(mRecFile, 16, 4);  // no extension data
	writeWord(mRecFile, 1, 2);  // PCM - integer samples
	writeWord(mRecFile, nCh, 2);  // one channel (mono) or two channels (stereo file)
	writeWord(mRecFile, Fs, 4);  // samples per second (Hz)
	//writeWord( f, 176400, 4 );  // (Sample Rate * BitsPerSample * Channels) / 8
	writeWord(mRecFile, (Fs * bPS * nCh) / 8, 4);  // (Sample Rate * BitsPerSample * Channels) / 8
	writeWord(mRecFile, 4, 2);  // data block size (size of two integer samples, one for each channel, in bytes)
	writeWord(mRecFile, bPS, 2);  // number of bits per sample (use a multiple of 8)

	// Write the data chunk header
	mDataChunkPos = mRecFile.tellp();
	mRecFile << "data----";  // (chunk size to be filled in later)
	mRecFile << std::flush;
}

void AsqRecEngine::wavFileFinish() {
	// wav file finishing
	// (We'll need the final file size to fix the chunk sizes above)
	size_t file_length = mRecFile.tellp();

	// Fix the data chunk header to contain the data size
	g_total_data_size = file_length - mDataChunkPos + 8;
	mRecFile.seekp(mDataChunkPos + 4);
	little_endian_io::writeWord(mRecFile, static_cast<int16_t>(g_total_data_size));

	// Fix the file header to contain the proper RIFF chunk size, which is (file size - 8) bytes
	mRecFile.seekp(0 + 4);
	little_endian_io::writeWord(mRecFile, file_length - 8, 4);

	mRecFile.close();
	//mPlyBuffer.close();
}





