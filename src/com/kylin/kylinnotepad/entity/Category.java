package com.kylin.kylinnotepad.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Category implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static transient final String TABLE_NAME = "category";
	public static transient final String ID = "_id";
	public static transient final String CATEGORY_NAME = "categoryname";

	private int id;
	private String categoryName;
	private List<Note> notes;

	public Category(){
		notes = new ArrayList<Note>();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public List<Note> getNotes() {
		return notes;
	}

	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}
	
}
