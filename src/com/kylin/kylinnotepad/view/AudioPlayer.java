package com.kylin.kylinnotepad.view;

public interface AudioPlayer {
	public void start();

	public void pause();

	public void resume();

	public void stop();

	public void seekTo(float percentage);

	public int getDuration();

	public void setOnFinishPlayListener(
			OnFinishPlayListener onFinishPlayListener);

	public interface OnFinishPlayListener {
		public void onFinishPlayListener(boolean result);
	}
}