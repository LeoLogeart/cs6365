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
		String content;
		for (File file : files) {
			if (!file.isDirectory()) {
				content = readFromFile(file);
				meanPress+=getMeanPress(content);
				meanBetween+=getMeanBetween(content);
			}
		}
		double d1=meanPress/files.size();
		double d2=meanBetween/files.size();
		System.out.println("press: "+d1+"\nbetween: "+d2);
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
			res+=Double.parseDouble(values[i]);
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
}
