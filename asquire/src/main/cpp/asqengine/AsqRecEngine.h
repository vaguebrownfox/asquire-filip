//
// Created by darwin on 9/11/20.
//

#ifndef ASQUIRE_REON_0X07_ASQRECENGINE_H
#define ASQUIRE_REON_0X07_ASQRECENGINE_H

#include <oboe/Oboe.h>
#include <iostream>
#include <fstream>

namespace little_endian_io {
	template<typename Word>
	std::ostream &writeWord(std::ostream &outs, Word value, unsigned size = sizeof(Word)) {
		for (; size; --size, value >>= 8)
			outs.put(static_cast<char>(value & 0xFF));
		return outs;
	}
}

namespace big_endian_io {
	template <typename Word>
	std::ostream& writeWord(std::ostream& outs, Word value) {
		outs.write(reinterpret_cast<const char *>(&value), sizeof(value));
		return outs;
	}
}

class AsqRecEngine {
private: // Variables
	bool mIsRecOn = false;
	std::shared_ptr<oboe::AudioStream> mRecordStream;

	const char* mRecFilePath{};
	std::ofstream mRecFile;
	std::ofstream mPlyBuffer;

	oboe::AudioFormat mFormat;
	int32_t mSampleRate;

	oboe::Direction mDirection;
	oboe::ChannelCount mChannelCount;

	size_t mDataChunkPos{};



public:
	AsqRecEngine();
	~AsqRecEngine();

	oboe::AudioStreamBuilder *setupRecordStreamParameters(oboe::AudioStreamBuilder *builder);

	oboe::Result openRecStream();
	void closeRecStream();

	oboe::Result start();
	oboe::Result stop();

	bool setRecOn(bool isOn);

	void wavFileWriter();
	void wavFileFinish();


private:
	void recordStream();



public: // is, set, get
	bool isRecOn() {
		return mIsRecOn;
	}

	void setRecFilePath(const char* recFilePath) {
		mRecFilePath = recFilePath;
	}

};


#endif //ASQUIRE_REON_0X07_ASQRECENGINE_H
