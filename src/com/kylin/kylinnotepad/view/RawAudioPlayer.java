package com.kylin.kylinnotepad.view;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

public class RawAudioPlayer implements AudioPlayer {

	// /////////////////录音相关配置信息/////////////////////////////
	// 设置音频的采样率，44100是目前的标准，但是某些设备仍然支持22050,16000,11025
	private int mSampleRateInHz = 41100;
	// 设置音频的录制声道，CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
	private int mChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
	// 设置音频数据格式:PCM 16位每个样本，保证设备支持。PCM 8位每个样本，不一定能得到设备的支持。
	private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
	private int mBufferSizeInBytes;

	private File mAudioFile;

	private PlayTask mPlayTask;
	
	private OnFinishPlayListener mOnFinishPlayListener;

	public RawAudioPlayer(File audioFile) {
		mAudioFile = audioFile;
		mBufferSizeInBytes = AudioTrack.getMinBufferSize(mSampleRateInHz,
				mChannelConfig, mAudioFormat);
	}

	@Override
	public void start() {
		mPlayTask = new PlayTask(mAudioFile);
		mPlayTask.execute();
	}

	@Override
	public void pause() {
		if (mPlayTask != null) {
			mPlayTask.pausePlay();
		}
	}

	@Override
	public void resume() {
		if (mPlayTask != null) {
			mPlayTask.resumePlay();
		}
	}

	@Override
	public void stop() {
		if (mPlayTask != null && mPlayTask.isPlaying()) {
			mPlayTask.stopPalying();
		}
	}

	@Override
	public void seekTo(float percentage) {

	}

	@Override
	public int getDuration() {
		return 0;
	}
	
	@Override
	public void setOnFinishPlayListener(
			OnFinishPlayListener onFinishPlayListener) {
		this.mOnFinishPlayListener = onFinishPlayListener;
	}

	private class PlayTask extends AsyncTask<Void, Void, Boolean> {

		boolean isPausing;
		boolean isContinuePlay;
		File audioFile;

		public PlayTask(File file) {
			isPausing = false;
			audioFile = file;
			isContinuePlay = true;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!audioFile.exists())
				return false;
			short[] buffer = new short[mBufferSizeInBytes / 2];
			try {
				DataInputStream dis = new DataInputStream(
						new BufferedInputStream(new FileInputStream(audioFile)));
				AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
						mSampleRateInHz, mChannelConfig, mAudioFormat,
						mBufferSizeInBytes, AudioTrack.MODE_STREAM);
				track.play();
				while (isContinuePlay && dis.available() > 0) {
					if (isPausing)
						continue;
					int i = 0;
					while (dis.available() > 0 && i < buffer.length) {
						buffer[i] = dis.readShort();
						i++;
					}
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
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			mOnFinishPlayListener.onFinishPlayListener(result);
		}

		public void pausePlay() {
			isPausing = true;
		}

		public void resumePlay() {
			isPausing = false;
		}

		public boolean isPlaying() {
			return isContinuePlay;
		}

		public void stopPalying() {
			isContinuePlay = false;
		}

	}
	
}
