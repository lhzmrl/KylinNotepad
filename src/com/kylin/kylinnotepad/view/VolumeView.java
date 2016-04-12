package com.kylin.kylinnotepad.view;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * 音量显示控件，通过输入声音，可以显示声音波纹
 * 
 * @author Kylin_admin
 * 
 */
public class VolumeView extends LinearLayout {

	// /////////////////录音相关配置信息/////////////////////////////
	// 设置音频的采样率，44100是目前的标准，但是某些设备仍然支持22050,16000,11025
	private int mSampleRateInHz = 41100;
	// 设置音频的录制声道，CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
	private int mChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
	// 设置音频数据格式:PCM 16位每个样本，保证设备支持。PCM 8位每个样本，不一定能得到设备的支持。
	private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
	private int mBufferSizeInBytes;
	private int mRudioSource;

	public static final String TAG = "VolumeView";
	private int mIndicationNum;
	private float mCurrVolumnPercent;
	private float[] mVolumePercents;
	private int mInterval;

	private View[] mIndicationViews;

	private AnimationTask mAnimationTask;
	private RecordThread mThreadRecord;
	private PlayThread mPlayThread;

	public VolumeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDefaultValues();
	}

	private void setDefaultValues() {
		mBufferSizeInBytes = AudioTrack.getMinBufferSize(mSampleRateInHz,
				mChannelConfig, mAudioFormat);
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);
		mCurrVolumnPercent = 0f;
		mIndicationNum = 0;
		mInterval = 200;
		setIndicationNum(5);
	}

	/**
	 * 设置指示条数量
	 * 
	 * @param num
	 */
	public void setIndicationNum(int num) {
		this.mIndicationNum = num;
		initViews();
	}

	/**
	 * 初始化视图
	 */
	private void initViews() {
		mIndicationViews = new View[mIndicationNum];
		mVolumePercents = new float[mIndicationNum];
		removeAllViews();
		int parentHeight = getHeight();
		setWeightSum(mIndicationNum);
		for (int i = 0; i < mIndicationNum; i++) {
			mVolumePercents[i] = 0.1f;
			mIndicationViews[i] = new View(getContext());
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
					parentHeight);
			lp.weight = 1;
			lp.leftMargin = 10;
			lp.rightMargin = 10;
			mIndicationViews[i].setLayoutParams(lp);
			mIndicationViews[i].setScaleY(0.1f);
			mIndicationViews[i].setBackgroundColor(Color.rgb(239, 152, 1));
			addView(mIndicationViews[i], i);
		}
	}

	/**
	 * 设置实时音量
	 * 
	 * @param volumnPercent
	 *            当前音量占允许最大音量百分比
	 */
	private void setVolumn(float volumnPercent) {
		if (volumnPercent < 0.1)
			volumnPercent = 0.1f;
		if (volumnPercent > 1)
			volumnPercent = 1f;
		mCurrVolumnPercent = volumnPercent;
	}

	/**
	 * 设置动画刷新频率，默认200ms
	 * 
	 * @param interval
	 */
	public void setInterval(int interval) {
		this.mInterval = interval;
	}

	/**
	 * 开始动画(此处存在问题，在调用动画之前需在设置一次指示器数量)
	 */
	private void startAnimation() {
		// FIXME
		mAnimationTask = new AnimationTask();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			mAnimationTask.execute();
		} else {
			mAnimationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void stopAnimation() {
		if (mAnimationTask != null)
			mAnimationTask.finishAnimation();
	}

	private void pauseAnimation(boolean isPause) {
		if (mAnimationTask != null)
			mAnimationTask.pauseAnimation(isPause);
	}

	/**
	 * 结束动画
	 */
	public void finishAnimation() {
		mAnimationTask.finishAnimation();
	}

	/**
	 * 是否正在执行动画
	 * 
	 * @return
	 */
	public boolean isAnimating() {
		if (mAnimationTask == null)
			return false;
		return mAnimationTask.isAnimating;
	}

	public void startRecord(File file, boolean isAnimate) {
		mThreadRecord = new RecordThread(file, false);
		mThreadRecord.start();
		if (isAnimate) {
			startAnimation();
		}
	}

	public void pauseRecord() {
		if (mThreadRecord != null)
			mThreadRecord.setPause(true);
		pauseAnimation(true);
	}

	public void resumeRecord() {
		if (mThreadRecord != null)
			mThreadRecord.setPause(false);
		pauseAnimation(false);
	}

	public void finishRecord() {
		mThreadRecord.finishRecord();
		if (mAnimationTask != null) {
			finishAnimation();
			mAnimationTask = null;
		}
	}

	public void playAudio(File file) {
		mPlayThread = new PlayThread(file);
		mPlayThread.start();
		startAnimation();
	}

	public void pauseAudioPlay() {
		if (mPlayThread != null && mPlayThread.isPlaying()) {
			// TODO 暂停播放声音
		}
	}
	

	public void resumeAudioPlay() {
		if (mPlayThread != null && mPlayThread.isPlaying()) {
			// TODO 重新播放声音
		}
	}

	public void stopAudioPlay() {
		if (mPlayThread != null && mPlayThread.isPlaying()) {
			mPlayThread.stopPalying();
		}
		if (mAnimationTask != null) {
			finishAnimation();
			mAnimationTask = null;
		}
	}

	/**
	 * 执行动画的异步任务
	 * 
	 * @author Kylin_admin
	 * 
	 */
	private class AnimationTask extends AsyncTask<Void, Void, String> {

		boolean isAnimating;
		boolean isPause;

		@Override
		protected void onPreExecute() {
			isAnimating = true;
			isPause = false;
		}

		private void updateIndicationViews(int index) {
			if (mVolumePercents[index]<0.1)
				mVolumePercents[index] = 0.1f;
			Log.d(TAG, "第" + index + "个指示条声音为：" + mVolumePercents[index]);
			mIndicationViews[index].setScaleY(mVolumePercents[index]);
		}

		@Override
		protected String doInBackground(Void... params) {
			while (isAnimating) {
				if (!isPause)
					publishProgress();
				try {
					Thread.sleep(mInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.d(TAG, "刷新动画，当前声音：" + mCurrVolumnPercent);
			for (int i = mIndicationNum - 1; i > 0; i--) {
				mVolumePercents[i] = mVolumePercents[i - 1];
				updateIndicationViews(i);
			}
			mVolumePercents[0] = mCurrVolumnPercent;
			updateIndicationViews(0);
		}

		@Override
		protected void onPostExecute(String result) {
			for (int i = 0; i < mIndicationNum; i++) {
				mIndicationViews[1].setScaleY(0.1f);
			}
		}

		public void finishAnimation() {
			isAnimating = false;
		}

		public void pauseAnimation(boolean isPause) {
			this.isPause = isPause;
		}
	}

	private class RecordThread extends Thread {

		boolean addToFile;
		boolean isContinue;
		boolean isPause;
		File audioFile;
		File wavFile;

		public RecordThread(File file, boolean add) {
			audioFile = file;
			wavFile = new File(audioFile.getParent(),"temp.wav");
			addToFile = add;
			isContinue = true;
		}

		@Override
		public void run() {
			try {
				Log.v(TAG, "打开输出流");
				DataOutputStream dos = new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(audioFile)));
				Log.v(TAG, "初始化AudioRecord");
				AudioRecord record = new AudioRecord(mRudioSource,
						mSampleRateInHz, mChannelConfig, mAudioFormat,
						mBufferSizeInBytes);
				AudioRecord recordReal = new AudioRecord(mRudioSource,
						mSampleRateInHz, mChannelConfig, mAudioFormat,
						mBufferSizeInBytes);
				short[] buffer = new short[mBufferSizeInBytes];
				Log.v(TAG, "开始记录");
				record.startRecording();
				while (isContinue) {
					if (isPause)
						continue;
					int bufferReadResult = record.read(buffer, 0,
							mBufferSizeInBytes);
					Log.v(TAG, "记录数据：" + bufferReadResult);
					double v = 0;
					int i;
					for (i = 0; i < bufferReadResult; i++) {
						v += buffer[i] * buffer[i];
						dos.writeShort(buffer[i]);
					}
					double mean = v / (double) bufferReadResult;
					double volume = 10 * Math.log10(mean);
					volume -= 35;
					if (volume < 0)
						volume = 0;
					float volumnPercent = (float) (volume / 50);
					setVolumn(volumnPercent);
				}
				Log.v(TAG, "停止记录数据");
				record.stop();
				recordReal.stop();
				dos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void finishRecord() {
			isContinue = false;
		}

		public void setPause(boolean isPause) {
			this.isPause = isPause;
		}
	}

	private class PlayThread extends Thread {

		boolean isContinuePlay;
		File audioFile;

		public PlayThread(File file) {
			audioFile = file;
			isContinuePlay = true;
		}

		@Override
		public void run() {
			if (!audioFile.exists())
				return;
			short[] buffer = new short[mBufferSizeInBytes / 2];
			try {
				DataInputStream dis = new DataInputStream(
						new BufferedInputStream(new FileInputStream(audioFile)));
				AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
						mSampleRateInHz, mChannelConfig, mAudioFormat,
						mBufferSizeInBytes, AudioTrack.MODE_STREAM);
				track.play();
				while (isContinuePlay && dis.available() > 0) {
					int i = 0;
					double v = 0;
					while (dis.available() > 0 && i < buffer.length) {
						buffer[i] = dis.readShort();
						v += buffer[i] * buffer[i];
						i++;
					}
					double mean = v / (double) buffer.length;
					double volume = 10 * Math.log10(mean);
					volume -= 35;
					if (volume < 0)
						volume = 0;
					float volumnPercent = (float) (volume / 50);
					setVolumn(volumnPercent);
					track.write(buffer, 0, buffer.length);
				}
				track.stop();
				dis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				isContinuePlay = false;
			}
		}

		public boolean isPlaying() {
			return isContinuePlay;
		}

		public void stopPalying() {
			isContinuePlay = false;
		}
	}

}
