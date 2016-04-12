package com.kylin.kylinnotepad.entity;

import java.io.Serializable;
import java.util.List;

public class KylinNotepad implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<Category> mListCategory;

	public KylinNotepad(List<Category> listCategory){
		this.mListCategory = listCategory;
	}
	
	public List<Category> getmListCategory() {
		return mListCategory;
	}

	public void setmListCategory(List<Category> mListCategory) {
		this.mListCategory = mListCategory;
	}

}
