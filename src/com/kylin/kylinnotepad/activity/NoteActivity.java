package com.kylin.kylinnotepad.activity;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.db.DatabaseOperator;
import com.kylin.kylinnotepad.entity.Category;
import com.kylin.kylinnotepad.entity.KylinNotepad;
import com.kylin.kylinnotepad.entity.Note;
import com.kylin.kylinnotepad.utils.BitmapUtils;
import com.kylin.kylinnotepad.utils.ContentUtils;
import com.kylin.kylinnotepad.utils.CustomPopupWindows;
import com.kylin.kylinnotepad.utils.CustomPopupWindows.OnDeletePhotoListner;
import com.kylin.kylinnotepad.utils.CustomPopupWindows.OnFinishRecordListner;
import com.kylin.kylinnotepad.utils.CustomPopupWindows.OnWindowDismissListener;
import com.kylin.kylinnotepad.view.AudioPlayView;
import com.kylin.kylinnotepad.view.ImageMovementMethod;
import com.kylin.kylinnotepad.view.ImageMovementMethod.MessageSpan;
import com.kylin.kylinnotepad.view.RawAudioPlayer;
import com.kylin.kylinnotepad.view.VariableActionBar;
import com.kylin.kylinnotepad.view.VariableActionBar.OnInstantiateListener;

public class NoteActivity extends BaseActivity implements OnClickListener,
		OnInstantiateListener, AMapLocationListener {

	private static final String TAG = "NoteActivity";

	public final static int REQUEST_CODE_IMAGE = 0x2;
	public final static int REQUEST_CODE_FROM_ALBUM = 0x1000;
	public final static int REQUEST_CODE_FROM_CAMERA = 0x1001;
	public final static int REQUEST_CODE_RECORD_VIDEO = 0x1002;

	public final static int MESSAGE_TYPE_IMAGESPAN = 0x2001;
	private final static int SELECT_PICTURE = 0;
	private final static int SELECT_CAMERA = 1;

	private boolean mIsEdited;

	private int mResultCode;
	private int mPosition;
	private int mCategoryId;

	private Uri mOutputFileUri;
	private Note mNote;
	private List<Category> mListCategory;

	// 临时状态栏组件
	private TextView mTvActBarTitle;
	private Button mBtnActBarOverFlow;

	// 通常状态栏组件
	private Button mBtnActBarBtnCategory;
	private Button mBtnActBarBtnImage;
	private Button mBtnActBarBtnAudio;
	private Button mBtnActBarBtnVideo;
	private Button mBtnActBarBtnCancle;
	private Button mBtnActBarBtnSave;

	private EditText mEtTitle;
	private TextView mTvLoaction;
	private EditText mEtContent;
	private VariableActionBar mVarActionBar;
	private Button mBtnVideo;
	private AudioPlayView mAudioPlayView;
	private DisplayMetrics mDm;
	private PopupWindow mPopupWindowPhoto;
	private PopupWindow mPopupWindowAudio;

	private NoticeHandler mNoticeHandler;

	private AMapLocationClient mLocationClient = null;

	@Override
	protected void initActivity(Bundle savedInstanceState) {
		setContentView(R.layout.activity_note);
		initData();
		initView();
	}

	private void initData() {
		mIsEdited = false;
		Intent intent = getIntent();
		mPosition = intent.getIntExtra("position", -1);
		mNote = (Note) intent.getSerializableExtra("noteinfo");
		mListCategory = ((KylinNotepad) (intent.getSerializableExtra("set")))
				.getmListCategory();
		mCategoryId = intent.getIntExtra("cateid", 1);
		mResultCode = MainActivity.RESULT_CODE_UPDATA_NOTE;
		if (mNote == null) {
			mNote = new Note();
			mResultCode = MainActivity.RESULT_CODE_CREATE_NEW_NOTE;
		}
	}

	private void initView() {
		findViewById(R.id.act_note_view_mask).setOnClickListener(this);
		mVarActionBar = (VariableActionBar) findViewById(R.id.act_note_var_bar);
		mVarActionBar.setOnInstantiateListener(this);
		mEtTitle = (EditText) findViewById(R.id.act_note_et_title);
		mEtTitle.setText(mNote.getTitle());
		mEtTitle.setVisibility(View.GONE);
		mEtContent = (EditText) findViewById(R.id.act_note_et_content);
		mEtContent.setOnClickListener(this);
		mTvLoaction = (TextView) findViewById(R.id.act_note_tv_location);
		if (!"".equals(mNote.getLocaltionName())) {
			mTvLoaction.setVisibility(View.VISIBLE);
			mTvLoaction.setText(mNote.getLocaltionName());
		}
		findViewById(R.id.act_note_btn_del_audio).setOnClickListener(this);
		mBtnVideo = (Button) findViewById(R.id.act_note_btn_video);
		mBtnVideo.setOnClickListener(this);
		if (!"".equals(mNote.getVideoPath())) {
			mBtnVideo.setVisibility(View.VISIBLE);
		}
		mAudioPlayView = (AudioPlayView) findViewById(R.id.act_note_apv);

		mNoticeHandler = new NoticeHandler();
		mEtContent.setMovementMethod(ImageMovementMethod.getInstance(
				mNoticeHandler, ImageSpan.class));
		SpannableString ss = ContentUtils.convertContent(getResources(),
				mNote.getContent());
		mEtContent.setText(ss);
		mEtContent.setCursorVisible(false);
		if (mResultCode == MainActivity.RESULT_CODE_CREATE_NEW_NOTE) {
			exitInterimState();
		} else {
			enterInterimState();
		}
		mDm = getResources().getDisplayMetrics();
		changeAudioState();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mOutputFileUri != null)
			outState.putString("mOutputFileUri", mOutputFileUri.toString());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String uri = savedInstanceState.getString("mOutputFileUri");
		if (uri != null)
			mOutputFileUri = Uri.parse(uri);
		String lastContent = mEtContent.getText().toString();
		SpannableString ss = ContentUtils.convertContent(getResources(),
				lastContent);
		mEtContent.setText(ss);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.actbar_note_normal_btn_save:
			String title = mEtTitle.getText().toString();
			String content = mEtContent.getText().toString();
			if (title.equals("") && content.equals("")) {
				Toast.makeText(this, R.string.empty_note_msg,
						Toast.LENGTH_SHORT).show();
				justFinishActivity();
			} else
				tryFinishActivty();
			break;
		case R.id.actbar_card_interim_tv_title:
		case R.id.act_note_et_content:
			exitInterimState();
			break;
		case R.id.actbar_note_normal_btn_cancle:
			justFinishActivity();
			break;
		case R.id.actbar_note_normal_btn_image:
			selectImage();
			break;
		case R.id.actbar_note_normal_btn_audio:
			recordAudio();
			break;
		case R.id.act_note_btn_del_audio:
			deleteAudio();
			break;
		case R.id.actbar_card_interim_btn_overflow:
			showNoteOverFlow();
			break;
		case R.id.actbar_note_normal_btn_category:
			selectCategory();
			break;
		case R.id.act_note_btn_video:
			playVideo();
			break;
		default:
			break;
		}

	}

	/**
	 * 初始化状态栏
	 */
	@Override
	public View instantiateNormalActionBar() {
		View view = View.inflate(this, R.layout.actionbar_note_normal, null);
		mBtnActBarBtnCategory = (Button) view
				.findViewById(R.id.actbar_note_normal_btn_category);
		mBtnActBarBtnCategory.setOnClickListener(this);
		mBtnActBarBtnImage = (Button) view
				.findViewById(R.id.actbar_note_normal_btn_image);
		mBtnActBarBtnImage.setOnClickListener(this);
		mBtnActBarBtnAudio = (Button) view
				.findViewById(R.id.actbar_note_normal_btn_audio);
		mBtnActBarBtnAudio.setOnClickListener(this);
		mBtnActBarBtnVideo = (Button) view
				.findViewById(R.id.actbar_note_normal_btn_video);
		mBtnActBarBtnCancle = (Button) view
				.findViewById(R.id.actbar_note_normal_btn_cancle);
		mBtnActBarBtnCancle.setOnClickListener(this);
		mBtnActBarBtnSave = (Button) view
				.findViewById(R.id.actbar_note_normal_btn_save);
		mBtnActBarBtnSave.setOnClickListener(this);
		return view;
	}

	/**
	 * 初始化状态栏
	 */
	@Override
	public View instantiateInterimActionBar() {
		View view = View.inflate(this, R.layout.actionbar_note_interim, null);
		mTvActBarTitle = (TextView) view
				.findViewById(R.id.actbar_card_interim_tv_title);
		mTvActBarTitle.setText(mNote.getTitle());
		mTvActBarTitle.setOnClickListener(this);
		mBtnActBarOverFlow = (Button) view
				.findViewById(R.id.actbar_card_interim_btn_overflow);
		mBtnActBarOverFlow.setOnClickListener(this);
		return view;
	}

	// 进入临时模式
	private void enterInterimState() {
		mVarActionBar.setActionBarState(true);
	}

	// 退出临时模式
	private void exitInterimState() {
		mVarActionBar.setActionBarState(false);
		mEtTitle.setVisibility(View.VISIBLE);
		mEtContent.setCursorVisible(true);
		int i = mEtContent.getSelectionStart();
		if (i == -1)
			i = 0;
		mEtContent.setSelection(i);

	}

	@Override
	public void onBackPressed() {
		// 处于预览图片状态
		if (mPopupWindowPhoto != null && mPopupWindowPhoto.isShowing()) {
			mPopupWindowPhoto.dismiss();
			mPopupWindowPhoto = null;
			return;
		}
		// 处于音频录制状态
		if (mPopupWindowAudio != null && mPopupWindowAudio.isShowing()) {
			mPopupWindowAudio.dismiss();
			mPopupWindowAudio = null;
			return;
		}
		// 未更改直接返回
		if (mVarActionBar.getActionBarState() && !mIsEdited) {
			justFinishActivity();
			return;
		}
		// 更改了
		String title = mEtTitle.getText().toString();
		String content = mEtContent.getText().toString();
		if (title.equals("") && content.equals("")) {
			// XXX 为空有两种情况，新建的记事本或者将内容清除了，此处处理针对第一种情况，第二种情况是写了删除键，再议
			super.onBackPressed();
		} else {
			showDialog();
			return;
		}
	}

	private void showDialog() {
		AlertDialog.Builder buidler = new AlertDialog.Builder(this);
		buidler.setTitle("提示").setMessage("是否保存？")
				.setNegativeButton("放弃", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						justFinishActivity();
					}

				})
				.setPositiveButton("保存", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						tryFinishActivty();
					}

				}).show();
	}

	/**
	 * 结束Activity，通知主界面内容更新
	 */
	private void tryFinishActivty() {
		boolean result = saveNote();
		if (result) {
			Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.putExtra("position", mPosition);
			intent.putExtra("noteinfo", mNote);
			setResult(mResultCode, intent);
			finish();
		} else {
			Toast.makeText(this, "保存失败！", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 没有任何修改，直接返回
	 */
	private void justFinishActivity() {
		Intent intent = new Intent();
		setResult(MainActivity.RESULT_CODE_NO_CHANGE, intent);
		finish();
	}

	private void selectImage() {
		CharSequence[] items = { "相册", "摄像" };
		new AlertDialog.Builder(this).setTitle("添加图片")
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (which == SELECT_PICTURE) {
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							intent.setType("image/*");
							startActivityForResult(
									Intent.createChooser(intent, "选择"),
									REQUEST_CODE_FROM_ALBUM);
						} else {

							File file = new File(
									getExternalFilesDir(Environment.DIRECTORY_PICTURES),
									System.currentTimeMillis() + ".inote");
							mOutputFileUri = Uri.fromFile(file);

							Intent intent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT,
									mOutputFileUri);
							startActivityForResult(intent,
									REQUEST_CODE_FROM_CAMERA);
						}
					}
				}).create().show();
	}

	// 录音
	private void recordAudio() {
		final File file = new File(
				getExternalFilesDir(Environment.DIRECTORY_MUSIC),
				System.currentTimeMillis() + ".anote");
		mPopupWindowAudio = CustomPopupWindows.getAudioRecordPopupWindow(this,
				file, new OnFinishRecordListner() {

					@Override
					public void onFinishRecord() {
						mNote.setAudioPath(file.getPath());
						changeAudioState();
					}

				}, new OnWindowDismissListener() {

					@Override
					public void onWindowDismissListener() {
						findViewById(R.id.act_note_view_mask).setVisibility(
								View.GONE);
						mPopupWindowAudio = null;
					}
				});
		mPopupWindowAudio.showAtLocation(findViewById(R.id.act_note_audioview),
				Gravity.BOTTOM, 0, 0);
		findViewById(R.id.act_note_view_mask).setVisibility(View.VISIBLE);
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
	}

	// 删除录音
	private void deleteAudio() {
		AlertDialog.Builder buidler = new AlertDialog.Builder(this);
		buidler.setTitle("提示").setMessage("是否删除音频？")
				.setNegativeButton("放弃", null)
				.setPositiveButton("删除", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mNote.setAudioPath("");
						changeAudioState();
						saveNote();
					}

				}).show();
	}

	/**
	 * 根据实体类改变音频播放控件等的状态
	 */
	private void changeAudioState() {
		if ("".equals(mNote.getAudioPath())) {
			findViewById(R.id.act_note_audioview).setVisibility(View.GONE);
			mBtnActBarBtnAudio.setEnabled(true);
		} else {
			findViewById(R.id.act_note_audioview).setVisibility(View.VISIBLE);
			mBtnActBarBtnAudio.setEnabled(false);
			mAudioPlayView.setAudioPlayer(new RawAudioPlayer(new File(mNote
					.getAudioPath())));
		}
	}

	// 拍摄视频
	private void recordVideo() {
		// Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		// intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		// startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO);
		File file = new File(
				getExternalFilesDir(Environment.DIRECTORY_PICTURES),
				System.currentTimeMillis() + ".vnote");
		Intent intent = new Intent();
		intent.setAction("android.media.action.VIDEO_CAPTURE");
		intent.addCategory("android.intent.category.DEFAULT");
		if (file.exists()) {
			file.delete();
		}
		mOutputFileUri = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);
		startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO);
	}

	private void playVideo() {
		Intent it = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse(mNote.getVideoPath());
		it.setDataAndType(uri, "video/mp4");
		startActivity(it);
	}

	// 展示多功能菜单
	private void showNoteOverFlow() {
		PopupMenu popup = new PopupMenu(this, mBtnActBarOverFlow);
		popup.getMenuInflater().inflate(R.menu.popupmenu_note, popup.getMenu());
		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.menu_note_location:
					getLocation();
					break;
				case R.id.menu_note_share:
					showShare();
					break;
				case R.id.menu_note_video:
					recordVideo();
					break;
				default:
					break;
				}
				return false;
			}

		});
		popup.show();
	}

	// 更改类别
	private void selectCategory() {
		String[] titles = new String[mListCategory.size()];
		int selectItem = 1;
		for (int i = 0; i < mListCategory.size(); i++) {
			titles[i] = mListCategory.get(i).getCategoryName();
			if (mCategoryId == mListCategory.get(i).getId())
				selectItem = i;
		}
		new AlertDialog.Builder(this)
				.setTitle("选择类别")
				.setPositiveButton("确定", null)
				.setSingleChoiceItems(titles, selectItem,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mCategoryId = mListCategory.get(which).getId();
							}

						}).setNegativeButton("取消", null).show();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_CODE_FROM_ALBUM:
			handleImageFromPicture(resultCode, data);
			break;
		case REQUEST_CODE_FROM_CAMERA:
			handleImageFromCamera(resultCode, data);
			break;
		case REQUEST_CODE_RECORD_VIDEO:
			// Uri uri = data.getData();
			mNote.setVideoPath(mOutputFileUri.getPath());
			mBtnVideo.setVisibility(View.VISIBLE);
			saveNote();
			break;
		default:
			break;
		}
	}

	private void handleImageFromCamera(int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED)
			return;
		addImage(mOutputFileUri.getPath());
	}

	private void handleImageFromPicture(int resultCode, Intent data) {
		if (resultCode == 0)
			return;
		Uri uri = data.getData();
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		String path = cursor.getString(column_index);
		addImage(path);
	}

	private void addImage(String path) {
		Bitmap bmp = BitmapUtils.decodeSampledBitmapFromFile(getResources(),
				path, mDm.widthPixels);
		Drawable drawable = new BitmapDrawable(getResources(), bmp);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth() - 20,
				drawable.getIntrinsicHeight());
		StringBuilder sb = new StringBuilder("<img src=\"");
		sb.append(path);
		sb.append("\">\n\n");
		SpannableString ss = new SpannableString(sb.toString());
		ImageSpan is = new ImageSpan(drawable, sb.toString());
		ss.setSpan(is, 0, sb.length() - 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		int index = mEtContent.getSelectionStart();
		mEtContent.getText().insert(index, ss);
	}

	private boolean saveNote() {
		String title = mEtTitle.getText().toString();
		String content = mEtContent.getText().toString();
		mNote.setCategoryId(mCategoryId);
		mNote.setTitle(title);
		mNote.setContent(content);
		mNote.setLastEditTime(System.currentTimeMillis());
		DatabaseOperator dbOperator = new DatabaseOperator(NoteActivity.this);
		long rowId;
		switch (mResultCode) {
		case MainActivity.RESULT_CODE_CREATE_NEW_NOTE:
			rowId = dbOperator.addNote(mNote);
			break;
		case MainActivity.RESULT_CODE_UPDATA_NOTE:
			rowId = dbOperator.updataNote(mNote);
			break;
		default:
			rowId = -1;
			break;
		}

		dbOperator.close();
		if (rowId == -1)
			return false;
		else {
			return true;
		}
	}

	// 回调Handler
	private class NoticeHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_TYPE_IMAGESPAN:
				IBinder token = findViewById(R.id.act_note_audioview)
						.getWindowToken();
				handleImageSpanClick((MessageSpan) msg.obj);
				break;

			default:
				break;
			}
		}
	}

	// 处理图片点击
	private void handleImageSpanClick(MessageSpan ms) {
		Object[] spans = (Object[]) ms.getObj();
		for (Object span : spans) {
			if (!(span instanceof ImageSpan))
				return;
			String source = ((ImageSpan) span).getSource();
			String path = ContentUtils.getTagPath(source);
			int type = ContentUtils.getTagType(source);
			switch (type) {
			case ContentUtils.CONTENT_TYPE_IMAGE:
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(this.getCurrentFocus()
								.getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				if (mPopupWindowPhoto != null)
					return;
				mPopupWindowPhoto = CustomPopupWindows.getPhotoPopupWindow(
						this, path, new OnDeletePhotoListner() {

							@Override
							public void onDeletePhoto() {

							}

						}, new OnWindowDismissListener() {

							@Override
							public void onWindowDismissListener() {
								mPopupWindowPhoto = null;
							}
						});
				mPopupWindowPhoto.showAtLocation(
						findViewById(R.id.act_note_audioview), Gravity.CENTER,
						0, 0);
				break;
			default:
				break;
			}
		}
	}

	private void getLocation() {
		// 初始化定位参数
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// 设置是否返回地址信息（默认返回地址信息）
		mLocationOption.setNeedAddress(true);
		// 设置是否只定位一次,默认为false
		mLocationOption.setOnceLocation(false);
		// 设置是否强制刷新WIFI，默认为强制刷新
		mLocationOption.setWifiActiveScan(true);
		// 设置是否允许模拟位置,默认为false，不允许模拟位置
		mLocationOption.setMockEnable(false);
		// 设置定位间隔,单位毫秒,默认为2000ms
		mLocationOption.setInterval(2000);

		// 声明AMapLocationClient类对象
		// 初始化定位
		mLocationClient = new AMapLocationClient(getApplicationContext());
		// 设置定位回调监听
		mLocationClient.setLocationListener(this);
		// 给定位客户端对象设置定位参数
		mLocationClient.setLocationOption(mLocationOption);
		// 启动定位
		mLocationClient.startLocation();
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		String location = amapLocation.getProvince() + "-"
				+ amapLocation.getCity() + "-" + amapLocation.getDistrict()
				+ " " + amapLocation.getStreet() + " "
				+ amapLocation.getStreetNum();
		// Logger.d(TAG, "位置信息：" + location);
		mNote.setLocaltionName(location);
		mTvLoaction.setVisibility(View.VISIBLE);
		mTvLoaction.setText(mNote.getLocaltionName());
		mLocationClient.stopLocation();// 停止定位
		mLocationClient.onDestroy();// 销毁定位客户端。
		mIsEdited = true;
	}

	// 显示分享
	private void showShare() {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();

		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(getString(R.string.share));
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl("http://sharesdk.cn");
		// text是分享文本，所有平台都需要这个字段
		oks.setText("我是分享文本");
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		// oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl("http://sharesdk.cn");
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl("http://sharesdk.cn");

		// 启动分享GUI
		oks.show(this);
	}
}
