package com.cs6365.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

public class Testing {
	
	
	/**
	 * Reads the file path under the folder KeystrokeDynamics
	 * 
	 * @param path
	 */
	public static String readFromFile(File f) {
		InputStream is = null;
		try {
			is = new FileInputStream(f);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr, 8192); // 2nd arg is buffer
															// size

		StringBuilder sb = new StringBuilder();
		try {
			String line = br.readLine() ;
			while (line!=null) {
				sb.append(line);
				line = br.readLine();
			}
			isr.close();
			is.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	
	/**
	 * Returns the list of files in directory KeystrokeDynamics
	 * 
	 * @return
	 */
	public static List<File> getListFiles() {
		File root = android.os.Environment.getExternalStorageDirectory();
		File parentDir = new File(root.getAbsolutePath() + "/KeystrokeDynamics");
		ArrayList<File> inFiles = new ArrayList<File>();
		File[] files = parentDir.listFiles();
		for (File file : files) {
			if (!file.isDirectory()) {
				inFiles.add(file);
			}
		}
		return inFiles;
	}
	
	/**
	 * Gets the mean of both the time between each button is pressed and the time
	 * a button is pressed.
	 */
	public static void getMean() {
		ArrayList<File> files = (ArrayList<File>) getListFiles();
		double meanPress=0;
		double meanBetween=0;
		double s=0.0;
		double meanPressS=0;
		double meanBetweenS=0;
		double meanPressNS=0;
		double meanBetweenNS=0;
		String content;
		for (File file : files) {
			if (!file.isDirectory()) {
				content = readFromFile(file);
				meanPress+=getMeanPress(content);
				meanBetween+=getMeanBetween(content);
				if(file.getName().contains("side")||file.getName().contains("SIDE")){
					s+=1;
					meanPressS+=getMeanPress(content);
					meanBetweenS+=getMeanBetween(content);
				} else {
					meanPressNS+=getMeanPress(content);
					meanBetweenNS+=getMeanBetween(content);
				}
			}
		}
		double d1=meanPress/files.size();
		double d2=meanBetween/files.size();
		double d1S=meanPressS/s;
		double d2S=meanBetweenS/s;
		double d1NS=meanPressNS/(files.size()-s);
		double d2NS=meanBetweenNS/(files.size()-s);
		Log.d("Testing","press: "+d1+"\nbetween: "+d2);
		Log.d("Testing","Side press: "+d1S+"\nbetween: "+d2S);
		Log.d("Testing","non Side press: "+d1NS+"\nbetween: "+d2NS);
	}


	/**
	 * Return the mean of the time between each button is pressed
	 * in a string of the testing format
	 * 
	 * @param s
	 * @return
	 */
	private static double getMeanBetween(String s) {
		String[] values = s.split(",")[1].split(";");
		double res=0;
		for(int i=0;i<(values.length-1)/2;i++){
			res+=Double.parseDouble(values[i])-Double.parseDouble(values[i+(values.length-1)/2]);
		}
		return res/((values.length-1)/2);
	}
	
	/**
	 * Return the mean of the time each button is pressed
	 * in a string of the testing format
	 * 
	 * @param s
	 * @return
	 */
	private static double getMeanPress(String s) {
		String[] values = s.split(",")[1].split(";");
		double res=0;
		for(int i=(values.length-1)/2;i<values.length;i++){
			res+=Double.parseDouble(values[i]);
		}
		return res/((values.length-1)/2);
	}
	
	
	public static void Test(Context ctx) {
		ArrayList<File> files = (ArrayList<File>) getListFiles();
		String content;
		int frr=0;
		int n=0;
		for (File file : files) {
			if (!file.isDirectory()) {
				n++;
				content = readFromFile(file);
				String username = getUserName(file.getName());
				String pwd=getPwd(content);
				//String username = getUserNameSIDE(file.getName());
				Vector<Double> features = getFeatures(content);
				//System.out.println(username);
				//System.out.println(pwd);
				boolean portrait = file.getName().contains("side") || file.getName().contains("SIDE");
				if(!Authentication.userExists(username, ctx)){
					Log.d("Testing","Register : "+username+";"+pwd);
					Authentication.initialization(username, features.size(), pwd, ctx);
				}
				if(!Authentication.authenticate(features, username, pwd, ctx, portrait)){
					Log.d("Testing","FAIL : "+username+";"+pwd);
					frr++;
				}
			}
		}	
		Log.d("Testing","FRR : "+frr);
		Log.d("Testing","logs : "+n);
	}


	private static Vector<Double> getFeatures(String content) {
		Vector<Double> res = new Vector<Double>();
		String features = content.split(",")[1];
		String[] f = features.split(";");
		for(int i=0 ; i < f.length ;i++){
			if(i<(f.length-1)/2){
				res.add(Double.parseDouble(f[i])-Double.parseDouble(f[i+(f.length-1)/2]));
			} else {
				res.add(Double.parseDouble(f[i]));
			}
		}
		return res;
	}


	private static String getPwd(String content) {
		return content.split(",")[0];
	}



	private static String getUserName(String name) {
		String res =name;
		/*if(name.contains("PIN")){
			res=name.substring(3, name.length());
		} */
		if(res.contains("SIDE")){
			res=res.split("SIDE")[0];
		/*} else if (res.contains("side")){
			res=res.split("side")[0];
		*/} else {
			res=res.split("0")[0];
		}
		return res;
	}	
	
	public static String getUserNameSide(String name) {
		String res =name;
		if(name.contains("PIN")){
			res=name.substring(3, name.length());
		} 
		res=res.split("0")[0];
		
		return res;
	}
}
