package com.kylin.kylinnotepad.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.entity.Category;

public class CategoryManagerAdapter extends CommonAdapter<Category>{

	private static final int LAYOUT_ID = R.layout.lvi_cate_manage;
	
	private OnButtonClickListener mOnButtonClickListener;
	
	public CategoryManagerAdapter(Context context, List<Category> mDatas,OnButtonClickListener onButtonClickListener) {
		super(context, mDatas, LAYOUT_ID);
		this.mOnButtonClickListener = onButtonClickListener;
	}

	@Override
	public void convert(ViewHolder helper, Category item, final int position) {
		helper.setText(R.id.lvi_cate_manage_tv_name, item.getCategoryName());
		helper.setViewOnCLickListener(R.id.lvi_cate_manager_btn_del, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mOnButtonClickListener.onButtonClickListener(position);
			}
		});
	}
	
	public interface OnButtonClickListener{
		public void onButtonClickListener(int position);
	}

}
