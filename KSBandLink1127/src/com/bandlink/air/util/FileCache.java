package com.bandlink.air.util;

import java.io.File;

import android.content.Context;

public class FileCache {
	private File CacheDir;
	public FileCache(Context context){
		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			CacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"Avatar");
		else
			CacheDir=context.getCacheDir();
		if(!CacheDir.exists())
			CacheDir.mkdir();
	}
	
	public File getFile(String url){
		String filename=String.valueOf(url.hashCode());
		File file=new File(CacheDir,filename);
		return file;
		
	}
	public void clearSingleFile(String url) {
		String filename=String.valueOf(url.hashCode());
		File file=new File(CacheDir,filename);
		if(file.exists())
		file.delete();
	}
	
	public void clear(){
		File[] files=CacheDir.listFiles();
		if(files==null){
			 return;
		}else{
			for(File f:files){
				f.delete();
			}
		}
	}

}
