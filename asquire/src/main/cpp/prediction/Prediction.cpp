//
// Created by darwin on 17/11/20.
//

#include "Prediction.h"


Prediction::Prediction(const char* wavFilepath, const char* modelFilePath) {

	char featFilepath[200];
	char outFilepath[200];
	strcpy(featFilepath, wavFilepath);
	strcat(featFilepath, ".feat.txt");
	strcpy(outFilepath, wavFilepath);
	strcat(outFilepath, ".out.txt");

	mWavFilePath = wavFilepath;
	mFeatsFilePath = featFilepath;
	mOutFilePath = outFilepath;
	mModelFilePath = modelFilePath;

	mNumMfccCoeffs = 13;
	mMfccOptions.frame_opts.frame_length_ms = 20;
	mMfccOptions.frame_opts.frame_shift_ms = 10;
	mMfccOptions.num_ceps = mNumMfccCoeffs;

	mMinDuration = 0.0;
}

Prediction::~Prediction() = default;

void Prediction::computeMfccFeats() {

	try {

		kaldi::Mfcc mfcc(mMfccOptions);

		kaldi::Input wavFile;
		wavFile.Open(mWavFilePath);

		kaldi::WaveHolder holder;
		holder.Read(wavFile.Stream());

		kaldi::WaveData waveData = holder.Value();

		wavFile.Close();

		if (waveData.Duration() < mMinDuration) {
			KALDI_WARN << "File is too short ("
			           << waveData.Duration() << " sec): producing no output.";
			exit(-1);
		}

		int32 sampleFreq = waveData.SampFreq();
		int32 numChannel = waveData.Data().NumRows();
		int32 channel = 0; // (0=left, 1=right...)

		// This block works out the channel (0=left, 1=right...)
		KALDI_ASSERT(numChannel > 0);  // at least one channel required
		if (numChannel < 1) {
			KALDI_WARN << "At least one channel required ("
			           << numChannel << " channels): provided.";
			exit(-1);
		}

		kaldi::SubVector<kaldi::BaseFloat> waveform(waveData.Data(), channel);

		mfcc.ComputeFeatures(waveform, sampleFreq, 1.0, &mFeatures);

		KALDI_LOG << "Computed MFCCs for " << mWavFilePath;

	} catch (const std::exception &e) {
		std::cerr << e.what();
		exit(-1);
	}
}

void Prediction::writeFeats() {
	int nRows = mFeatures.NumRows();
	int nCols = mFeatures.NumCols();

	std::ofstream featsFile(mFeatsFilePath, std::ofstream::app);
	featsFile.precision(4);
	featsFile << std::fixed;

	int ref_label = 0; // dummy labels

	for (int i = 0; i < nRows; i++) {
		SubVector<BaseFloat> row = mFeatures.Row(i);
		featsFile << /**(labels + i)*/  ref_label << " ";
		int index = 1;
		for (int j = 0; j < mNumMfccCoeffs; j++) {
			if (j % mNumMfccCoeffs != 0) {
				double gg = row.Data()[j];
				featsFile << (index++) << ":" << gg << " ";
			}
		}
		featsFile << "\n";
	}

	featsFile.flush();
	featsFile.close();
}

void Prediction::asqPredict() {

	computeMfccFeats();
	writeFeats();

	struct svm_model* asqModel = svm_load_model(mModelFilePath);

	FILE* input = fopen(mFeatsFilePath, "r");
	FILE* output = fopen(mOutFilePath, "w");

	predict(input, asqModel, output);

	fclose(input);
	fclose(output);

}

