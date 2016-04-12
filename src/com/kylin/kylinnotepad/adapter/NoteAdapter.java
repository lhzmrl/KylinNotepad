package com.kylin.kylinnotepad.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.entity.Note;
import com.kylin.kylinnotepad.utils.ContentUtils;

public class NoteAdapter extends CommonAdapter<Note> {

	private static final int LAYOUT_ID = R.layout.lvi_note;

	private boolean showingCheckBox;
	
	private OnCheckBoxCheckedChangeListener mOnCheckBoxCheckedChangeListener;

	public NoteAdapter(Context context, List<Note> mDatas) {
		super(context, mDatas, LAYOUT_ID);
	}

	@Override
	public void convert(ViewHolder helper, Note item, final int position) {
		// 设置标题
		if (item.getTitle().equals(""))
			helper.setViewVisibility(R.id.lvi_note_tv_title, View.GONE);
		else{
			helper.setViewVisibility(R.id.lvi_note_tv_title, View.VISIBLE);
			helper.setText(R.id.lvi_note_tv_title, item.getTitle());
		}
		// 设置缩略内容
		if (item.getContent().equals(""))
			helper.setViewVisibility(R.id.lvi_note_tv_content, View.GONE);
		else{
			helper.setViewVisibility(R.id.lvi_note_tv_content, View.VISIBLE);
			helper.setText(R.id.lvi_note_tv_content, ContentUtils.convertAbbreviatedContent(item.getContent()));
		}
		// 设置最近编辑时间
		helper.setText(R.id.lvi_note_tv_time, item.getFormatLastEditTime());
		// 是否显示CheckBox
		if (showingCheckBox) {
			helper.setViewVisibility(R.id.lvi_note_cb_del, View.VISIBLE);
		} else {
			helper.setViewVisibility(R.id.lvi_note_cb_del, View.GONE);
		}
		helper.setOnCheckedChangeListener(R.id.lvi_note_cb_del,
				new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (mOnCheckBoxCheckedChangeListener != null)
							mOnCheckBoxCheckedChangeListener.onCheckBoxClick(
									position, isChecked);
					}

				});
		helper.setCheckBoxState(R.id.lvi_note_cb_del, item.isShouleDelete());
	}

	public void setOnCheckBoxCheckedChangeListener(
			OnCheckBoxCheckedChangeListener onCheckBoxCheckedChangeListener) {
		mOnCheckBoxCheckedChangeListener = onCheckBoxCheckedChangeListener;
	}

	public interface OnCheckBoxCheckedChangeListener {
		public void onCheckBoxClick(int position, boolean isChecked);
	}
	
	public void shouldShowCheckBox(boolean showing){
		showingCheckBox = showing;
		notifyDataSetChanged();
	}

}
