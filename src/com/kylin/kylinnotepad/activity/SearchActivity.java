package com.kylin.kylinnotepad.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kylin.kylinnotepad.R;
import com.kylin.kylinnotepad.R.id;
import com.kylin.kylinnotepad.R.layout;
import com.kylin.kylinnotepad.adapter.NoteAdapter;
import com.kylin.kylinnotepad.db.DatabaseOperator;
import com.kylin.kylinnotepad.entity.Category;
import com.kylin.kylinnotepad.entity.KylinNotepad;
import com.kylin.kylinnotepad.entity.Note;

public class SearchActivity extends Activity implements OnItemClickListener{

	private List<Note> mListNote;
	private KylinNotepad mKylinNotepad;
	
	private ProgressBar mPbLoading;
	private EditText mEtContent;
	private ListView mLvNotes;

	private NoteAdapter mAdapterNote;
	
	private LoadDataTask mTaskLoadData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		initData();
		initView();
	}
	
	

	private void initData() {
		mKylinNotepad = ((KylinNotepad) getIntent().getSerializableExtra("set"));
		mListNote = new ArrayList<Note>();
		mAdapterNote = new NoteAdapter(this, mListNote);
	}



	private void initView() {
		mPbLoading = (ProgressBar) findViewById(R.id.act_search_pb);
		mEtContent = (EditText) findViewById(R.id.act_search_et_content);
		mLvNotes = (ListView) findViewById(R.id.act_search_lv);
		
		mLvNotes.setAdapter(mAdapterNote);
		mLvNotes.setOnItemClickListener(this);
	}



	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.act_search_btn_back:
			finish();
			break;
		case R.id.act_search_btn_search:
			loadDataFromDB();
			break;
		default:
			break;
		}
	}

	// 从数据库加载数据
	private void loadDataFromDB() {
		if ("".equals(mEtContent.getText().toString().trim())){
			Toast.makeText(this, "无效搜索!", Toast.LENGTH_SHORT).show();
			return;
		}
		mTaskLoadData = new LoadDataTask();
		mTaskLoadData.execute();
	}

	// 加载数据的线程
	private class LoadDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mPbLoading.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			DatabaseOperator dbOperator = new DatabaseOperator(
					SearchActivity.this);
			mListNote = dbOperator.getNoteListByContent(mEtContent.getText().toString().trim());
			dbOperator.close();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			resetData();
			mPbLoading.setVisibility(View.GONE);
		}

	}
	
	private void resetData() {
		mAdapterNote = new NoteAdapter(this, mListNote);
		mLvNotes.setAdapter(mAdapterNote);
	}



	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this, NoteActivity.class);
		intent.putExtra("noteinfo", mListNote.get(position));
		intent.putExtra("position", position);
		intent.putExtra("set", mKylinNotepad);
		intent.putExtra("cateid", mListNote.get(position).getCategoryId());
		startActivityForResult(intent, MainActivity.REQUEST_CODE_OPEN_NOTE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadDataFromDB();
	}
}
