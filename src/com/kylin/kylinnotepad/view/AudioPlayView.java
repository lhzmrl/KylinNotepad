package com.kylin.kylinnotepad.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.view.AudioPlayer.OnFinishPlayListener;

public class AudioPlayView extends RelativeLayout implements
		OnCheckedChangeListener, OnFinishPlayListener, OnClickListener {

	private int mDuration;

	private ImageButton mIbShow;
	private TextView mTvShowTotalTime;

	private CheckBox mCbPlay;
	private ImageButton mIbStop;
	private SeekBar mSbProgress;
	private TextView mTvCurrTime;
	private TextView mTvTotalTime;

	private View mPlayView;
	private View mShowView;

	private AudioPlayer mAudioPlayer;

	public AudioPlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		setGravity(Gravity.CENTER_VERTICAL);
		mShowView = inflate(getContext(), R.layout.audio_play_layout_show, null);
		mPlayView = inflate(getContext(), R.layout.audio_play_layout_play, null);

		// 为播放键设置监听事件
		mShowView.findViewById(R.id.audio_play_ib_show_play_view)
				.setOnClickListener(this);
		mTvShowTotalTime = (TextView) mShowView
				.findViewById(R.id.audio_play_tv_show_time);

		((CheckBox) mPlayView.findViewById(R.id.audio_play_cb_play))
				.setOnCheckedChangeListener(this);
		mPlayView.findViewById(R.id.audio_play_ib_stop)
				.setOnClickListener(this);
		mSbProgress = (SeekBar) mPlayView
				.findViewById(R.id.audio_play_progress);
		mSbProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (!fromUser)
					return;
				float percentage = progress * 0.1f / 100;
				seekTo(percentage);
			}
		});
		mTvCurrTime = (TextView) mPlayView
				.findViewById(R.id.audio_play_tv_curr_time);
		mTvTotalTime = (TextView) mPlayView
				.findViewById(R.id.audio_play_tv_total_time);

		addView(mShowView, 0);
		addView(mPlayView, 1);
		mPlayView.setVisibility(View.GONE);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.audio_play_cb_play:
			if (isChecked) {
				pause();
			} else {
				resume();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.audio_play_ib_show_play_view:
			mPlayView.setVisibility(View.VISIBLE);
			mShowView.setVisibility(View.GONE);
			play();
			break;
		case R.id.audio_play_ib_stop:
			mPlayView.setVisibility(View.GONE);
			mShowView.setVisibility(View.VISIBLE);
			stop();
			break;
		default:
			break;
		}
	}

	public void setAudioPlayer(AudioPlayer audioPlayer) {
		if (audioPlayer != null) {
			mAudioPlayer = audioPlayer;
			mDuration = mAudioPlayer.getDuration();
			mAudioPlayer.setOnFinishPlayListener(this);
		}
	}

	private void play() {
		if (mAudioPlayer != null)
			mAudioPlayer.start();
	}

	private void pause() {
		if (mAudioPlayer != null)
			mAudioPlayer.pause();
	}

	private void resume() {
		if (mAudioPlayer != null)
			mAudioPlayer.resume();
	}

	private void stop() {
		if (mAudioPlayer != null)
			mAudioPlayer.stop();
	}

	private void seekTo(float percentage) {
		if (mAudioPlayer != null)
			mAudioPlayer.seekTo(percentage);
	}

	@Override
	public void onFinishPlayListener(boolean result) {
		mPlayView.setVisibility(View.GONE);
		mShowView.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		if (visibility==View.GONE)
			stop();
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility==View.GONE)
			stop();
	}

}
