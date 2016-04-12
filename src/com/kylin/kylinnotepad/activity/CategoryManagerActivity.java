package com.kylin.kylinnotepad.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.adapter.CategoryManagerAdapter;
import com.kylin.kylinnotepad.adapter.CategoryManagerAdapter.OnButtonClickListener;
import com.kylin.kylinnotepad.db.DatabaseOperator;
import com.kylin.kylinnotepad.entity.Category;
import com.kylin.kylinnotepad.entity.KylinNotepad;
import com.loc.db;

public class CategoryManagerActivity extends BaseActivity implements
		OnButtonClickListener, OnClickListener {

	private List<Category> mListCategory;

	private Button mBtnBack;
	private Button mBtnAdd;
	private ListView mLvCategory;

	private CategoryManagerAdapter mAdapterCateManager;

	@Override
	protected void initActivity(Bundle savedInstanceState) {
		setContentView(R.layout.activity_category_manager);
		initData();
		initView();
	}

	private void initData() {
		Intent intent = getIntent();

		mListCategory = ((KylinNotepad) (intent
				.getSerializableExtra("categorys"))).getmListCategory();
		for (Category category : mListCategory) {
			if (category.getId() == 1) {
				mListCategory.remove(category);
				break;
			}
		}
		mAdapterCateManager = new CategoryManagerAdapter(this, mListCategory,
				this);
	}

	private void initView() {
		mBtnBack = (Button) findViewById(R.id.act_cate_manager_btn_back);
		mBtnAdd = (Button) findViewById(R.id.act_cate_manager_btn_add);
		mLvCategory = (ListView) findViewById(R.id.act_cate_manager_lv_category);

		mBtnBack.setOnClickListener(this);
		mBtnAdd.setOnClickListener(this);
		mLvCategory.setAdapter(mAdapterCateManager);
	}

	@Override
	public void onButtonClickListener(final int position) {
		AlertDialog.Builder buidler = new AlertDialog.Builder(this);
		buidler.setTitle("提示").setMessage("是否删除？")
				.setNegativeButton("放弃", null)
				.setPositiveButton("删除", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						DatabaseOperator dbOperator = new DatabaseOperator(
								CategoryManagerActivity.this);
						dbOperator.delCategory(mListCategory.remove(position)
								.getId());
						dbOperator.close();
						mAdapterCateManager.notifyDataSetChanged();
					}

				}).show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.act_cate_manager_btn_back:
			setResult(MainActivity.RESULT_CODE_FINISH_MANAGER_CATEGORY,
					new Intent());
			finish();
			break;
		case R.id.act_cate_manager_btn_add:
			tryAddCategory();
			break;
		default:
			break;
		}
	}

	private void tryAddCategory() {
		final EditText et = new EditText(this);
		new AlertDialog.Builder(this).setTitle("请输入")
				.setIcon(android.R.drawable.ic_dialog_info).setView(et)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!"".equals(et.getText().toString())) {
							addCategory(et.getText().toString());
						} else {
							Toast.makeText(getApplicationContext(), "无效名称",
									Toast.LENGTH_SHORT).show();
						}

					}

				}).setNegativeButton("取消", null).show();
	}

	private void addCategory(String name) {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
		Category category = new Category();
		category.setCategoryName(name);
		DatabaseOperator dbOperator = new DatabaseOperator(this);
		dbOperator.addCategory(category);
		dbOperator.close();
		mListCategory.add(0, category);
		mAdapterCateManager.notifyDataSetChanged();
	}
}
