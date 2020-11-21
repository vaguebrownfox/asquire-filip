//
// Created by darwin on 17/11/20.
//

#include "Prediction.h"


Prediction::Prediction(const char* wavFilepath, const char* modelFilePath) {

	mWavFilePath = wavFilepath;
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



void Prediction::calculateMfccStats() {

	int nRows = mFeatures.NumRows();
	int nCols = mFeatures.NumCols();

	float32 stats[nCols];

	for (int i = 0; i < nCols; i++) {

		// mean
		stats[i] = 0;
		for (int j = 0; j < nRows; j++) {
//			stats[i] += mFeatures
		}
	}

}

kaldi::Matrix<kaldi::BaseFloat> mean(kaldi::Matrix<kaldi::BaseFloat> features) {
	int nRows = features.NumRows();
	int nCols = features.NumCols();

//	for (int i = 0; i <)
}

void Prediction::writeFeats() {
	int nRows = mFeatures.NumRows();
	int nCols = mFeatures.NumCols();

	char featFilepath[200];

	strcpy(featFilepath, mWavFilePath);
	strcat(featFilepath, ".feat.txt");





	std::ofstream featsFile(featFilepath, std::ofstream::app);
	featsFile.precision(4);
	featsFile << std::fixed;

	int ref_label = 0; // dummy labels

	for (int i = 0; i < nRows; i++) {
		SubVector<BaseFloat> row = mFeatures.Row(i);
		featsFile << /**(labels + i)*/  ref_label << " ";
		int index = 0;
		for (int j = 0; j < mNumMfccCoeffs; j++) {
			if (j % mNumMfccCoeffs != 0) {
				double gg = row.Data()[j];
				featsFile << (index++) << ":" << gg << " ";
			}
		}
		featsFile << "\n";
	}



	// dummy feat vector
	char dummyFeats[200];
	strcpy(dummyFeats, mWavFilePath);
	strcat(dummyFeats, ".dummyfeat.txt");
	std::ofstream dummyFeatsOf(dummyFeats, std::ofstream::app);
	dummyFeatsOf.precision(4);
	dummyFeatsOf << std::fixed;

	int ncols = (mNumMfccCoeffs - 1) * 6;
	for (int k = 0; k <= 13; k++) {
		dummyFeatsOf << ref_label << " ";
		for (int i = 0, j = 0; i <= ncols; i++) {
			double gg = pow(-1, j) * 0.1;
			dummyFeatsOf << (j++) << ":" << gg << " ";
		}
		dummyFeatsOf << "\n";
	}
	dummyFeatsOf.close();

	featsFile.flush();
	featsFile.close();
}

void Prediction::asqPredict(const char* featfilepath) {

	computeMfccFeats();
	writeFeats();

	char featFilepath[200];
	char outFilepath[200];
	strcpy(featFilepath, mWavFilePath);
	strcat(featFilepath, ".feat.txt");
	strcpy(outFilepath, mWavFilePath);
	strcat(outFilepath, ".out.txt");

	struct svm_model* asqModel = svm_load_model(mModelFilePath);

	FILE* input = fopen(featFilepath, "r");
	FILE* output = fopen(outFilepath, "w");


	// Temp stuff
	char dummyFeats[200];
	strcpy(dummyFeats, mWavFilePath);
	strcat(dummyFeats, ".dummyfeat.txt");
	FILE* dummyip = fopen(dummyFeats, "r");


//	for (int i = 0; i < 16; i++) {
		FILE* testfeatip = fopen(featfilepath, "r");
		predict(testfeatip, asqModel, output);
		fclose(testfeatip);
//	}

//	std::ofstream opOf(outFilepath, std::ofstream::app);
//
//	opOf << 1 ;
//	opOf.close();

	fclose(input);
	fclose(output);

}

