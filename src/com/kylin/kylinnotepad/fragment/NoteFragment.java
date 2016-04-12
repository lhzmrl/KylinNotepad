package com.kylin.kylinnotepad.fragment;

import java.util.List;

import org.w3c.dom.ls.LSException;

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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.activity.MainActivity;
import com.kylin.kylinnotepad.activity.NoteActivity;
import com.kylin.kylinnotepad.adapter.NoteAdapter;
import com.kylin.kylinnotepad.adapter.NoteAdapter.OnCheckBoxCheckedChangeListener;
import com.kylin.kylinnotepad.entity.Category;
import com.kylin.kylinnotepad.entity.KylinNotepad;
import com.kylin.kylinnotepad.entity.Note;

public class NoteFragment extends Fragment implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener,OnCheckBoxCheckedChangeListener {

	private boolean mIsInterimState;

	private Category mCurrCategory;
	private List<Category> mListCategory;

	private View mContentView;
	private ListView mLvNotes;
	private Button mBtnAddNote;

	private NoteAdapter mAdapterNote;

	private OnEnterInterimStateListener mOnEnterInterimStateListener;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnNoteFragmentAttachListener) {
			((OnNoteFragmentAttachListener) context).onNoteFragmentAttach(this);
		}
		if (context instanceof OnEnterInterimStateListener) {
			mOnEnterInterimStateListener = (OnEnterInterimStateListener) context;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_note, container);
		initData();
		initView();
		setDataAndListener();
		return mContentView;
	}

	private void initData() {
		mIsInterimState = false;
		mCurrCategory = new Category();
		mAdapterNote = new NoteAdapter(getActivity(), mCurrCategory.getNotes());
		mAdapterNote.setOnCheckBoxCheckedChangeListener(this);
	}

	private void initView() {
		mBtnAddNote = (Button) mContentView
				.findViewById(R.id.fra_note_ib_add_note);
		mLvNotes = (ListView) mContentView.findViewById(R.id.fra_note_lv_note);
	}

	private void setDataAndListener() {
		mLvNotes.setAdapter(mAdapterNote);

		mBtnAddNote.setOnClickListener(this);
		mLvNotes.setOnItemClickListener(this);
		mLvNotes.setOnItemLongClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(getActivity(), NoteActivity.class);
		intent.putExtra("set", new KylinNotepad(mListCategory));
		intent.putExtra("cateid", mCurrCategory.getId());
		startActivityForResult(intent, MainActivity.REQUEST_CODE_OPEN_NOTE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mIsInterimState) {
			CheckBox cb = (CheckBox) view.findViewById(R.id.lvi_note_cb_del);
			boolean state = cb.isChecked();
			cb.setChecked(!state);
		} else {
			Intent intent = new Intent(getActivity(), NoteActivity.class);
			intent.putExtra("noteinfo", mCurrCategory.getNotes().get(position));
			intent.putExtra("position", position);
			intent.putExtra("set", new KylinNotepad(mListCategory));
			intent.putExtra("cateid", mCurrCategory.getId());
			startActivityForResult(intent, MainActivity.REQUEST_CODE_OPEN_NOTE);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (mIsInterimState)
			return true;
		if (mOnEnterInterimStateListener != null) {
			mOnEnterInterimStateListener.onEnterInterimStateListener();
			CheckBox cb = (CheckBox) view.findViewById(R.id.lvi_note_cb_del);
			cb.setChecked(true);
			return true;
		}
		return false;
	}
	
	// ListView中CheckBox选择状态改变的回调函数
	@Override
	public void onCheckBoxClick(int position, boolean isChecked) {
		mCurrCategory.getNotes().get(position).setShouleDelete(isChecked);
		int selectItemNum = getCheckItemNum();
		if(mOnEnterInterimStateListener!=null){
			mOnEnterInterimStateListener.onSelectedNumChangeListener(selectItemNum);
		}
	}
	
	private int getCheckItemNum() {
		int n = 0;
		for (Note note : mCurrCategory.getNotes()) {
			if (note.isShouleDelete())
				n++;
		}
		return n;
	}

	// 选中所有CheckBox
	public int selectAllCheckBox() {
		for (Note note : mCurrCategory.getNotes()) {
			note.setShouleDelete(true);
		}
		mAdapterNote.notifyDataSetChanged();
		return mCurrCategory.getNotes().size();
	}

	// 取消所有CheckBox选中
	public int notSelectAnyCheckBox() {
		for (Note note : mCurrCategory.getNotes()) {
			note.setShouleDelete(false);
		}
		mAdapterNote.notifyDataSetChanged();
		return mCurrCategory.getNotes().size();
	}

	public void setIsInterimState(boolean isInterimState) {
		this.mIsInterimState = isInterimState;
		mAdapterNote.shouldShowCheckBox(mIsInterimState);
	}

	public void setData(List<Category> listCategory,Category category) {
		this.mListCategory = listCategory;
		this.mCurrCategory = category;
		mAdapterNote = new NoteAdapter(getActivity(), mCurrCategory.getNotes());
		mAdapterNote.setOnCheckBoxCheckedChangeListener(this);
		mLvNotes.setAdapter(mAdapterNote);
	}

	public interface OnNoteFragmentAttachListener {
		public void onNoteFragmentAttach(NoteFragment noteFragment);
	}

	public interface OnEnterInterimStateListener {
		public void onEnterInterimStateListener();
		public void onSelectedNumChangeListener(int selectItemNum);
	}

}
