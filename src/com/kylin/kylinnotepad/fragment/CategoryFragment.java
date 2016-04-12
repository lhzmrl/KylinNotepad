package com.kylin.kylinnotepad.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.activity.CategoryManagerActivity;
import com.kylin.kylinnotepad.activity.MainActivity;
import com.kylin.kylinnotepad.adapter.CategoryAdapter;
import com.kylin.kylinnotepad.entity.Category;
import com.kylin.kylinnotepad.entity.KylinNotepad;

public class CategoryFragment extends Fragment implements OnItemClickListener,OnClickListener{

	private List<Category> mListCategory;
	
	private Button mBtnManageCategory;
	private ListView mLvCategory;
	
	private View mContentView;
	
	private CategoryAdapter mAdapterCategory;
	
	private OnChangeDrawerLoyoutListener mOnChangeDrawerLoyoutListener;
	private OnCategoryChangedListener mOnCategoryChangedListener;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnCategoryFragmentAttachListener){
			((OnCategoryFragmentAttachListener) context).onCategoryFragmentAttach(this);
		}
		if (context instanceof OnChangeDrawerLoyoutListener){
			this.mOnChangeDrawerLoyoutListener = (OnChangeDrawerLoyoutListener) context;
		}
		if (context instanceof OnCategoryChangedListener){
			this.mOnCategoryChangedListener = (OnCategoryChangedListener) context;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_category, container);
		initData();
		initView();
		bindDataAndListerner();
		return mContentView;
	}
	
	private void initData() {
		mListCategory = new ArrayList<Category>();
		mAdapterCategory = new CategoryAdapter(getActivity(), mListCategory);
	}

	private void initView() {
		mBtnManageCategory = (Button) mContentView.findViewById(R.id.fra_category_btn_manage_cat);
		mLvCategory = (ListView) mContentView.findViewById(R.id.fra_category_lv_category);
		
	}
	
	private void bindDataAndListerner() {
		mBtnManageCategory.setOnClickListener(this);
		mLvCategory.setAdapter(mAdapterCategory);
		mLvCategory.setOnItemClickListener(this);
	}

	public interface OnCategoryFragmentAttachListener{
		public void onCategoryFragmentAttach(CategoryFragment categoryFragment);
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(getActivity(), CategoryManagerActivity.class);
		intent.putExtra("categorys", new KylinNotepad(mListCategory));
		startActivityForResult(intent, MainActivity.REQUEST_CODE_MANAGER_CATEGORY);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mOnChangeDrawerLoyoutListener.closeDrawerLayout();
		mOnCategoryChangedListener.onCategoryChanged(mListCategory.get(position).getId());
	}
	
	public void setData(List<Category> listCategory){
		this.mListCategory = listCategory;
		mAdapterCategory = new CategoryAdapter(getActivity(), mListCategory);
		mLvCategory.setAdapter(mAdapterCategory);
	}
	
	public interface OnChangeDrawerLoyoutListener{
		public void openDrawerLayout();
		public void closeDrawerLayout();
	}
	
	public interface OnCategoryChangedListener{
		public void onCategoryChanged(int cateid);
	}
	
}
