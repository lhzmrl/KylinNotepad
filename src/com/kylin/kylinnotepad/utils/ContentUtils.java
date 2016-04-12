package com.kylin.kylinnotepad.utils;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

public class ContentUtils {

	public static final int CONTENT_TYPE_UNKNOW = 0xFFFFFFFF;
	public static final int CONTENT_TYPE_IMAGE = 0x0;
	public static final int CONTENT_TYPE_AUDIO = 0x1;
	public static final int CONTENT_TYPE_VEDIO = 0x2;

	public static int getTagType(String tag){
		String regex = "(img|video|audio){1}";
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(tag);
		if (matcher.find()){
			String s = matcher.group();
			if (s.equals("img")){
				return CONTENT_TYPE_IMAGE;
			}else if(s.equals("audio")){
				return CONTENT_TYPE_AUDIO;
			}else if(s.equals("vedio")){
				return CONTENT_TYPE_VEDIO;
			}else{
				return CONTENT_TYPE_UNKNOW;
			}
				
		}
		return CONTENT_TYPE_UNKNOW;
	}
	
	public static String getTagPath(String tag){
		StringTokenizer st = new StringTokenizer(tag, "\"", false);
		st.nextToken();
		String path = st.nextToken();
		return path;
	}
	
	public static SpannableString convertContent(Resources resources,
			String content) {
		SpannableString spannableString = new SpannableString(content);
		String regex = "<(img|video|audio){1}(\\s)src=\"[-\\w\\/_\\s\\.]+\\.[-\\w\\/_\\s]+(\">)";
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(content);
		while (matcher.find()) {
			String key = matcher.group();
			StringTokenizer st = new StringTokenizer(key, "\"", false);
			st.nextToken();
			String path = st.nextToken();
			Bitmap bmp = BitmapUtils.decodeSampledBitmapFromFile(resources,
					path, resources.getDisplayMetrics().widthPixels);
			Drawable drawable = new BitmapDrawable(resources, bmp);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
			ImageSpan is = new ImageSpan(drawable, key);
			spannableString.setSpan(is, matcher.start(), matcher.end(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannableString;
	}

	public static String convertAbbreviatedContent(String content) {
		String regexImg = "<(img){1}(\\s)src=\"[-\\w\\/_\\s\\.]+\\.[-\\w\\/_\\s]+(\">)";
		String regexAudio = "<(audio){1}(\\s)src=\"[-\\w\\/_\\s\\.]+\\.[-\\w\\/_\\s]+(\">)";
		String regexVedio = "<(video){1}(\\s)src=\"[-\\w\\/_\\s\\.]+\\.[-\\w\\/_\\s]+(\">)";
		Matcher matcher = Pattern.compile(regexImg).matcher(content);
		content = matcher.replaceAll("[img]");

		matcher = Pattern.compile(regexAudio).matcher(content);
		content = matcher.replaceAll("[audio]");

		matcher = Pattern.compile(regexVedio).matcher(content);
		content = matcher.replaceAll("[vedio]");
		return content;
	}

}
