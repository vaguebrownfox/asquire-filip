package aashi.fiaxco.asquirefilip0x08.audioengine;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AsqViewModel extends ViewModel {
	private static final String TAG = "AsqViewModel";

	private final MutableLiveData<Boolean> mIsRecording = new MutableLiveData<>();
	private final MutableLiveData<AudioService.AudioBinder> mAudBinder = new MutableLiveData<>();

	private final ServiceConnection audioServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			Log.d(TAG, "onServiceConnected: Connected to audio service");
			AudioService.AudioBinder binder = (AudioService.AudioBinder) iBinder;
			mAudBinder.postValue(binder);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mAudBinder.postValue(null);
		}
	};
	public LiveData<Boolean> getIsRecording() {
		return mIsRecording;
	}

	public void setIsRecording(boolean r) {
		mIsRecording.postValue(r);
	}
	public LiveData<AudioService.AudioBinder> getAudBinder() {
		return mAudBinder;
	}

	public ServiceConnection getAudioServiceConnection() {
		return audioServiceConnection;
	}




	private final MutableLiveData<UploadService.FireBinder> mUpBinder = new MutableLiveData<>();

	private final ServiceConnection uploadServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			Log.d(TAG, "onServiceConnected: Connected to upload service");
			UploadService.FireBinder binder = (UploadService.FireBinder) iBinder;
			mUpBinder.postValue(binder);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mUpBinder.postValue(null);
		}
	};

	public LiveData<UploadService.FireBinder> getUpBinder() {
		return mUpBinder;
	}

	public ServiceConnection getUploadServiceConnection() {
		return uploadServiceConnection;
	}

}