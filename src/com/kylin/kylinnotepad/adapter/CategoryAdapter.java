package com.kylin.kylinnotepad.adapter;

import java.util.List;

import android.content.Context;

import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.entity.Category;

public class CategoryAdapter extends CommonAdapter<Category>{

	private static final int LAYOUT_ID = R.layout.lvi_category;
	
	public CategoryAdapter(Context context, List<Category> mDatas) {
		super(context, mDatas, LAYOUT_ID);
	}

	@Override
	public void convert(ViewHolder helper, Category item, int position) {
		helper.setText(R.id.lvi_category_tv_catename, item.getCategoryName());
		helper.setText(R.id.lvi_category_tv_notenum, "（"+item.getNotes().size()+"）");
	}

}
