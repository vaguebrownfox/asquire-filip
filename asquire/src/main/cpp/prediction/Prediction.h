//
// Created by darwin on 17/11/20.
//

#ifndef ASQUIRE_FILIP_0X08_PREDICTION_H
#define ASQUIRE_FILIP_0X08_PREDICTION_H

#include "base/kaldi-common.h"
#include "base/kaldi-common.h"
#include "util/common-utils.h"
#include "feat/feature-mfcc.h"
#include "feat/wave-reader.h"
#include "feat/resample.h"
#include "matrix/kaldi-matrix.h"
#include "transform/cmvn.h"

#include "ivectorbin/compute-vad.h"
#include <svm.h>
#include <svm-predict.h>

#include<algorithm>
#include<fstream>

class Prediction {
private:
	const char* mWavFilePath;
	const char* mFeatsFilePath;
	const char* mOutFilePath;
	const char* mModelFilePath;

	int32 mNChannel{};
	kaldi::BaseFloat mMinDuration;
	kaldi::MfccOptions mMfccOptions;
	int32 mNumMfccCoeffs;

	kaldi::Matrix<kaldi::BaseFloat> mFeatures;

public:
	Prediction(const char* filepath, const char* featsFilepath);
	~Prediction();

	void asqPredict();

private:
	void computeMfccFeats();
	void writeFeats();

};


#endif //ASQUIRE_FILIP_0X08_PREDICTION_H
