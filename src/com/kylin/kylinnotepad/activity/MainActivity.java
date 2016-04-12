package com.kylin.kylinnotepad.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kylin.kylinnotepad.AppConfig;
import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.db.DatabaseOperator;
import com.kylin.kylinnotepad.entity.Category;
import com.kylin.kylinnotepad.entity.KylinNotepad;
import com.kylin.kylinnotepad.entity.Note;
import com.kylin.kylinnotepad.fragment.CategoryFragment;
import com.kylin.kylinnotepad.fragment.CategoryFragment.OnCategoryChangedListener;
import com.kylin.kylinnotepad.fragment.CategoryFragment.OnCategoryFragmentAttachListener;
import com.kylin.kylinnotepad.fragment.CategoryFragment.OnChangeDrawerLoyoutListener;
import com.kylin.kylinnotepad.fragment.NoteFragment;
import com.kylin.kylinnotepad.fragment.NoteFragment.OnEnterInterimStateListener;
import com.kylin.kylinnotepad.fragment.NoteFragment.OnNoteFragmentAttachListener;
import com.kylin.kylinnotepad.view.VariableActionBar;
import com.kylin.kylinnotepad.view.VariableActionBar.OnInstantiateListener;

public class MainActivity extends BaseActivity implements
		OnInstantiateListener, OnCheckedChangeListener, OnClickListener,
		OnNoteFragmentAttachListener, OnCategoryFragmentAttachListener,
		OnEnterInterimStateListener, OnChangeDrawerLoyoutListener,
		OnCategoryChangedListener {

	public static final int REQUEST_CODE_OPEN_NOTE = 0x0;
	public static final int REQUEST_CODE_MANAGER_CATEGORY = 0x4;
	public static final int REQUEST_CODE_SEARCH_NOTE = 0x5;

	public static final int RESULT_CODE_UPDATA_NOTE = 0x1;
	public static final int RESULT_CODE_NO_CHANGE = 0x2;
	public static final int RESULT_CODE_CREATE_NEW_NOTE = 0x3;
	public static final int RESULT_CODE_FINISH_MANAGER_CATEGORY = 0x4;

	private boolean mIsFirstLogin;
	private int mCurrCategoryId;
	private int mSelectItemNum;

	private Category mCurrCategory;
	private List<Category> mListCategory;

	// Normal ActionBar相关按钮
	private Button mBtnNavigation;
	private TextView mTvNormalTitle;
	private Button mBtnSearch;
	private Button mBtnNormalOverFlow;
	// Interim ActionBar相关按钮
	private CheckBox mCbSelect;
	private TextView mTvInterimTitle;
	private Button mBtnShare;
	private Button mBtnDelete;
	private Button mBtnInterimOverFlow;

	private ProgressBar mPgLoading;
	private VariableActionBar mVarActionBar;
	private DrawerLayout mDrawerLayout;

	private NoteFragment mFragmentNote;
	private CategoryFragment mFragmentCategory;

	private LoadDataTask mTaskLoadData;

	@Override
	protected void initActivity(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
		initData();
		initView();
		loadDataFromDB();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		SharedPreferences sp = getSharedPreferences(AppConfig.PREFS_NAME,
				MODE_PRIVATE);
		mIsFirstLogin = sp.getBoolean(AppConfig.SP_KEY_IS_FIRST_LOGIN, true);
		mCurrCategoryId = sp.getInt(AppConfig.SP_KEY_CURRENT_CATEGORY_ID, 1);
		mCurrCategory = new Category();
	}

	private void initView() {
		mPgLoading = (ProgressBar) findViewById(R.id.act_main_pb_loading);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.act_main_drawer);
		mVarActionBar = (VariableActionBar) findViewById(R.id.act_main_var_bar);
		mVarActionBar.setOnInstantiateListener(this);
		exitInterimState();
	}

	/**
	 * NoteFragment绑定监听
	 */
	@Override
	public void onNoteFragmentAttach(NoteFragment noteFragment) {
		mFragmentNote = noteFragment;
	}

	/**
	 * CategoryFragment绑定监听
	 */
	@Override
	public void onCategoryFragmentAttach(CategoryFragment categoryFragment) {
		mFragmentCategory = categoryFragment;
	}

	// 从数据库加载数据
	private void loadDataFromDB() {
		mTaskLoadData = new LoadDataTask();
		mTaskLoadData.execute();
	}

	// 加载数据的线程
	private class LoadDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mPgLoading.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			DatabaseOperator dbOperator = new DatabaseOperator(
					MainActivity.this);
			if (mIsFirstLogin) {
				Category category = new Category();
				category.setCategoryName(getResources().getString(
						R.string.default_category_name));
				dbOperator.addCategory(category);
				SharedPreferences sp = getSharedPreferences(
						AppConfig.PREFS_NAME, MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putBoolean(AppConfig.SP_KEY_IS_FIRST_LOGIN, false);
				editor.commit();
			}
			mListCategory = dbOperator.getCategoryList();
			dbOperator.close();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			resetData();
			mPgLoading.setVisibility(View.GONE);
		}
	}

	// 重新设置数据
	private void resetData() {
		mCurrCategory = findCategoryById(mCurrCategoryId);
		mCurrCategoryId = mCurrCategory.getId();
		notifyDataSetChanged();
	}

	// 刷新数据
	private void notifyDataSetChanged() {
		mTvNormalTitle.setText(mCurrCategory.getCategoryName() + "("
				+ mCurrCategory.getNotes().size() + ")");
		mFragmentCategory.setData(mListCategory);
		mFragmentNote.setData(mListCategory, mCurrCategory);
	}

	private Category findCategoryById(int id) {
		Category categoryAll = new Category();
		categoryAll.setId(0);
		categoryAll.setCategoryName(getResources().getString(R.string.all));
		for (Category categoty : mListCategory) {
			if (categoty.getId() == id) {
				return categoty;
			}
			categoryAll.getNotes().addAll(categoty.getNotes());
		}
		return categoryAll;
	}

	// 初始化ActionBar回调函数
	@Override
	public View instantiateNormalActionBar() {
		View view = View.inflate(this, R.layout.actionbar_main_normal, null);
		mBtnNavigation = (Button) view
				.findViewById(R.id.actbar_normal_btn_navigation);
		mBtnNavigation.setOnClickListener(this);
		mTvNormalTitle = (TextView) view
				.findViewById(R.id.actbar_normal_tv_title);
		mBtnSearch = (Button) view.findViewById(R.id.actbar_normal_btn_search);
		mBtnSearch.setOnClickListener(this);
		mBtnNormalOverFlow = (Button) view
				.findViewById(R.id.actbar_normal_btn_overflow);
		return view;
	}

	// 初始化状态栏
	@Override
	public View instantiateInterimActionBar() {
		View view = View.inflate(this, R.layout.actionbar_main_interim, null);
		mCbSelect = (CheckBox) view.findViewById(R.id.actbar_interim_cb);
		mCbSelect.setOnCheckedChangeListener(this);
		mTvInterimTitle = (TextView) view
				.findViewById(R.id.actbar_interim_tv_title);
		mBtnShare = (Button) view.findViewById(R.id.actbar_interim_btn_share);
		mBtnDelete = (Button) view.findViewById(R.id.actbar_interim_btn_delete);
		mBtnDelete.setOnClickListener(this);
		mBtnInterimOverFlow = (Button) view
				.findViewById(R.id.actbar_interim_btn_overflow);
		return view;
	}

	// 单击事件监听器
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.actbar_interim_btn_delete:
			tryRemoveNote();
		case R.id.actbar_normal_btn_navigation:
			if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
				closeDrawerLayout();
			else
				openDrawerLayout();
			break;
		case R.id.actbar_normal_btn_search:
			Intent intent  = new Intent(this,SearchActivity.class);
			intent.putExtra("set", new KylinNotepad(mListCategory));
			startActivityForResult(intent, REQUEST_CODE_SEARCH_NOTE);
			break;
		default:
			break;
		}
	}

	// 全选和全取消的事件处理
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked)
			mFragmentNote.selectAllCheckBox();
		else
			mFragmentNote.notSelectAnyCheckBox();
	}

	// 键盘事件回调函数

	@Override
	public void onBackPressed() {
		if (mVarActionBar.getActionBarState()) {
			exitInterimState();
			return;
		}
		if (mDrawerLayout.isDrawerVisible(Gravity.LEFT)) {
			mDrawerLayout.closeDrawer(Gravity.LEFT);
			return;
		}
		super.onBackPressed();
	}

	// Activity返回的回调
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode & 0xffff) {
		case REQUEST_CODE_OPEN_NOTE:
			handleNoteActivityResult(resultCode, data);
			break;
		case REQUEST_CODE_SEARCH_NOTE:
			loadDataFromDB();
			break;
		case REQUEST_CODE_MANAGER_CATEGORY:
			loadDataFromDB();
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	// 处理NoteActivity返回结果
	private void handleNoteActivityResult(int resultCode, Intent data) {
		if (resultCode != 0x0206) {
			loadDataFromDB();
			return;
		}
		switch (resultCode) {
		case RESULT_CODE_CREATE_NEW_NOTE:
			Note noteNew = (Note) data.getSerializableExtra("noteinfo");
			mCurrCategory.getNotes().add(noteNew);
			Collections.sort(mCurrCategory.getNotes());
			notifyDataSetChanged();
			break;
		case RESULT_CODE_UPDATA_NOTE:
			Note noteUpdata = (Note) data.getSerializableExtra("noteinfo");
			int position = data.getIntExtra("position", 0);
			mCurrCategory.getNotes().remove(position);
			mCurrCategory.getNotes().add(0, noteUpdata);
			notifyDataSetChanged();
			break;
		case RESULT_CODE_NO_CHANGE:
			break;
		default:
			break;
		}
	}

	// 进入临时模式
	@Override
	public void onEnterInterimStateListener() {
		mFragmentNote.setIsInterimState(true);
		mVarActionBar.setActionBarState(true);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		mBtnShare.setVisibility(View.GONE);
		mBtnDelete.setVisibility(View.GONE);
		mBtnInterimOverFlow.setVisibility(View.GONE);
	}

	@Override
	public void onSelectedNumChangeListener(int selectItemNum) {
		mSelectItemNum = selectItemNum;
		mTvInterimTitle.setText(selectItemNum + "已选");
		switch (selectItemNum) {
		case 0:
			mBtnShare.setVisibility(View.GONE);
			mBtnDelete.setVisibility(View.GONE);
			mBtnInterimOverFlow.setVisibility(View.GONE);
			break;
		case 1:
			mBtnShare.setVisibility(View.VISIBLE);
			mBtnDelete.setVisibility(View.VISIBLE);
			mBtnInterimOverFlow.setVisibility(View.VISIBLE);
			break;
		default:
			mBtnShare.setVisibility(View.GONE);
			mBtnDelete.setVisibility(View.VISIBLE);
			mBtnInterimOverFlow.setVisibility(View.VISIBLE);
			break;
		}
	}

	// 退出临时模式
	private void exitInterimState() {
		mFragmentNote.setIsInterimState(false);
		mVarActionBar.setActionBarState(false);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		mTvNormalTitle.setText(mCurrCategory.getCategoryName() + "("
				+ mCurrCategory.getNotes().size() + ")");

		for (Note note : mCurrCategory.getNotes()) {
			note.setShouleDelete(false);
		}
	}

	// 尝试删除Note，弹出提示对话框
	private void tryRemoveNote() {
		AlertDialog.Builder buidler = new AlertDialog.Builder(this);
		buidler.setTitle("提示").setMessage("是否删除这" + mSelectItemNum + "项？")
				.setNegativeButton("放弃", null)
				.setPositiveButton("删除", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						removeNote();
					}

				}).show();
	}

	// 删除所有标记的Note
	private void removeNote() {
		List<String> ids = new ArrayList<String>();
		Note note;
		for (int n = mCurrCategory.getNotes().size() - 1; n >= 0; n--) {
			note = mCurrCategory.getNotes().get(n);
			if (note.isShouleDelete()) {
				ids.add(note.getId() + "");
				mCurrCategory.getNotes().remove(n);
			}
		}
		DatabaseOperator dbOperator = new DatabaseOperator(this);
		dbOperator.delNotes(ids);
		dbOperator.close();
		exitInterimState();
	}

	@Override
	public void openDrawerLayout() {
		mDrawerLayout.openDrawer(Gravity.LEFT);
	}

	@Override
	public void closeDrawerLayout() {
		mDrawerLayout.closeDrawer(Gravity.LEFT);
	}

	@Override
	public void onCategoryChanged(int cateid) {
		mCurrCategoryId = cateid;
		resetData();
	}

}
