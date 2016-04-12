package com.kylin.kylinnotepad.utils;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.photoview.PhotoViewAttacher;
import com.kylin.kylinnotepad.photoview.PhotoViewAttacher.OnPhotoTapListener;
import com.kylin.kylinnotepad.view.VolumeView;

public class CustomPopupWindows {

	/**
	 * 预览图片PopupWindow
	 * 
	 * @param context
	 * @param path
	 * @param onDeletePhotoListner
	 * @param onWindowDismissListener
	 * @return
	 */
	public static PopupWindow getPhotoPopupWindow(Context context, String path,
			final OnDeletePhotoListner onDeletePhotoListner,
			final OnWindowDismissListener onWindowDismissListener) {
		
		View layoutPop = View
				.inflate(context, R.layout.popupwindow_photo, null);
		final PopupWindow popwindow = new PopupWindow(layoutPop,
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		final View title = layoutPop.findViewById(R.id.popupwindow_photo_title);
		final ImageView img = (ImageView) layoutPop
				.findViewById(R.id.popupwindow_photo_photo);
		final Drawable drawable = new BitmapDrawable(context.getResources(),
				path);
		img.setImageDrawable(drawable);
		final PhotoViewAttacher mAttacher = new PhotoViewAttacher(img);
		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View view, float x, float y) {
				if (title.getVisibility() == View.VISIBLE)
					title.setVisibility(View.GONE);
				else
					title.setVisibility(View.VISIBLE);
			}

		});
		Button btnBack = (Button) layoutPop
				.findViewById(R.id.popupwindow_photo_btn_back);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popwindow.dismiss();
			}
		});
		Button btnDel = (Button) layoutPop
				.findViewById(R.id.popupwindow_photo_btn_del);
		btnDel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onDeletePhotoListner.onDeletePhoto();
			}
		});
		popwindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				img.setImageDrawable(null);
				mAttacher.cleanup();
				onWindowDismissListener.onWindowDismissListener();
			}
		});
		return popwindow;
	}

	/**
	 * 声音录制PopupWindows
	 * 
	 * @param context
	 * @param path
	 * @param onFinishRecordListner
	 * @param onWindowDismissListener
	 * @return
	 */
	public static PopupWindow getAudioRecordPopupWindow(Context context,
			final File path, final OnFinishRecordListner onFinishRecordListner,
			final OnWindowDismissListener onWindowDismissListener) {
		View layoutPop = View.inflate(context,
				R.layout.popupwindow_audio_record, null);

		final PopupWindow popwindow = new PopupWindow(layoutPop,
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		popwindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				onWindowDismissListener.onWindowDismissListener();
			}
		});
		popwindow.setAnimationStyle(R.style.anim_menu_bottombar);

		final VolumeView volumeView = (VolumeView) layoutPop
				.findViewById(R.id.popwindow_audio_volumeview);
		volumeView.setIndicationNum(25);
		volumeView.setInterval(150);
		final ImageButton ibCancle = (ImageButton) layoutPop
				.findViewById(R.id.popwindow_audio_ib_cancle);
		final CheckBox cbStartOrFinish = (CheckBox) layoutPop
				.findViewById(R.id.popwindow_audio_cb_start_finish);
		final CheckBox cbPauseOrResume = (CheckBox) layoutPop
				.findViewById(R.id.popwindow_audio_cb_pause_resume);
		layoutPop.findViewById(R.id.popwindow_audio_btn_close).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popwindow.dismiss();
			}
		});

		// 取消录制，恢复为初始状态
		ibCancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ibCancle.setVisibility(View.GONE);
				cbPauseOrResume.setVisibility(View.GONE);
				cbStartOrFinish.setChecked(false);
				volumeView.finishRecord();
			}
		});
		// 录制开始和完成事件，
		cbStartOrFinish
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							ibCancle.setVisibility(View.VISIBLE);
							cbPauseOrResume.setVisibility(View.VISIBLE);
							volumeView.setIndicationNum(25);
							volumeView.setInterval(150);
							volumeView.startRecord(path, true);
						} else {
							volumeView.finishRecord();
							if (ibCancle.getVisibility() != View.GONE) {
								onFinishRecordListner.onFinishRecord();
								popwindow.dismiss();
							}
						}
					}
				});

		cbPauseOrResume
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							volumeView.pauseRecord();
						} else {
							volumeView.resumeRecord();
						}
					}
				});

		return popwindow;
	}

	/**
	 * 声音播放PopupWindows
	 * 
	 * @param context
	 * @param path
	 * @param onFinishRecordListner
	 * @param onWindowDismissListener
	 * @return
	 */
	public static PopupWindow getAudioPlayPopupWindow(Context context,
			final File path, final OnFinishRecordListner onFinishRecordListner,
			final OnWindowDismissListener onWindowDismissListener) {
		View layoutPop = View.inflate(context,
				R.layout.popupwindow_audio_record, null);

		final PopupWindow popwindow = new PopupWindow(layoutPop,
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		popwindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				onWindowDismissListener.onWindowDismissListener();
			}
		});
		popwindow.setAnimationStyle(R.style.anim_menu_bottombar);

		final VolumeView volumeView = (VolumeView) layoutPop
				.findViewById(R.id.popwindow_audio_volumeview);
		volumeView.setIndicationNum(25);
		volumeView.setInterval(150);
		CheckBox cbStartOrFinish = (CheckBox) layoutPop
				.findViewById(R.id.popwindow_audio_cb_start_finish);

		cbStartOrFinish
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							volumeView.pauseAudioPlay();
						} else {
							volumeView.resumeRecord();
						}
					}
				});
		return popwindow;
	}

	public interface OnDeletePhotoListner {
		public void onDeletePhoto();
	}

	public interface OnFinishRecordListner {
		public void onFinishRecord();
	}

	public interface OnWindowDismissListener {
		public void onWindowDismissListener();
	}

}
