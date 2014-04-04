package com.cs6365.model;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class Utils {

	/**
	 * Generates a random prime BigInteger of a given bit-length
	 * 
	 * @param bitLength
	 * @return
	 */
	public static BigInteger generatePrimeNumber(int bitLength) {
		return BigInteger.probablePrime(bitLength, new Random());
	}

	/**
	 * Generates a random BigInteger in Zq
	 * 
	 * @param q
	 * @return
	 */
	public static BigInteger random(BigInteger q) {
		Random rnd = new Random();
		while (true) {
			BigInteger i = new BigInteger(q.bitLength(), rnd);
			if (i.compareTo(q) < 0)
				return i;
		}
	}

	/**
	 * Generates a random polynomial of degree degree by generating the degree-1
	 * coefficients. The coefficients are in Zq
	 * 
	 * @param degree
	 * @param q
	 * @return
	 */
	public static Vector<BigInteger> generateRandomPolynomial(int degree,
			BigInteger q) {
		Vector<BigInteger> coefs = new Vector<BigInteger>();
		for (int i = 0; i < degree; i++) {
			BigInteger a = random(q);
			coefs.add(a);
		}
		return coefs;
	}

	/**
	 * Evaluates the value of a given polynomial at a certain x.
	 * 
	 * @param x
	 * @param polynomial
	 * @return
	 */
	public static BigInteger valueOfPolynomial(int x,
			Vector<BigInteger> polynomial) {
		BigInteger res = BigInteger.ZERO;
		for (int i = 0; i < polynomial.size(); i++) {
			BigInteger a = polynomial.get(i);
			BigInteger xBig = BigInteger.valueOf(x);
			xBig = xBig.pow(i);
			res = res.add(a.multiply(xBig));
		}
		return res;
	}

	/**
	 * Interpolation using the set of points (x,y)
	 * 
	 * @param x
	 * @param y
	 * @param q
	 * @return
	 */
	public static BigInteger interpolate(Vector<Integer> x,
			Vector<BigInteger> y, BigInteger q) {
		//TODO
		BigInteger result = BigInteger.ZERO;
		int xi, xj;
		for (int i = 0; i < x.size(); i++) {
			xi = x.get(i);
			BigInteger lambdai = BigInteger.ONE;
			for (int j = 0; j < x.size(); j++) {
				if (j != i) {
					xj = x.get(j);
					lambdai = lambdai.multiply(BigInteger.valueOf(xj));
				}
			}
			for (int j = 0; j < x.size(); j++) {
				if (j != i) {
					xj = x.get(j);
					lambdai = lambdai.divide(BigInteger.valueOf(xj - xi));
				}
			}
			BigInteger yi = y.get(i);
			result = result.add(yi.multiply(lambdai));
		}
		return result.mod(q);
	}

	/**
	 * Hashes the integer value using the key and the Sha256 algorithm
	 * 
	 * @param value
	 * @param key
	 * @return
	 */
	public static BigInteger computeSha256(int value, String key) {
		byte[] hash = null;
		try {
			String input = value + "";
			Key secKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secKey);
			hash = mac.doFinal(input.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		BigInteger result = new BigInteger(hash);
		return result;
	}

	/**
	 * Generates a padding of the given size
	 * 
	 * @param bytes
	 * @param size
	 * @return
	 */
	public static byte[] pad(byte[] bytes, int size) {
		byte[] padded = new byte[size];
		for (int i = 0; i < size; i++)
			padded[i] = 0;
		for (int i = 0; i < bytes.length; i++)
			padded[i] = bytes[i];
		return padded;
	}

	/**
	 * Encrypts a message using the given key and the AES algorithm
	 * 
	 * @param bytes
	 * @param key
	 * @return
	 */
	@SuppressLint("TrulyRandom")
	public static byte[] encrypt(byte[] bytes, char[] key) {
		byte[] ciphertext = null;
		byte[] salt;
		try {
			salt = ("thisIsASalt").getBytes("UTF-8");
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			SecretKeyFactory factory = SecretKeyFactory
					.getInstance("PBKDF2WithHmacSHA1");
			PBEKeySpec spec = new PBEKeySpec(key, salt, 1024, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, secret, ivspec);
			ciphertext = cipher.doFinal(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ciphertext;
	}

	/**
	 * Decrypts the bytes using the provided key and the AES algorithm
	 * 
	 * @param bytes
	 * @param key
	 * @return
	 */
	public static byte[] decrypt(byte[] bytes, char[] key) {
		byte[] deciphertext = null;
		byte[] salt;
		try {
			salt = ("thisIsASalt").getBytes("UTF-8");
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			SecretKeyFactory factory = SecretKeyFactory
					.getInstance("PBKDF2WithHmacSHA1");
			PBEKeySpec spec = new PBEKeySpec(key, salt, 1024, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, secret, ivspec);
			deciphertext = cipher.doFinal(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deciphertext;
	}

	/**
	 * Writes bytes to the file described by the path path
	 * 
	 * @param bytes
	 * @param path
	 * @param ctx
	 */
	public static void writeToFile(byte[] bytes, String path, Context ctx) {
		FileOutputStream fos;
		try {
			fos = ctx.openFileOutput(path, Context.MODE_PRIVATE);
			fos.write(bytes);
			fos.close();
			Log.d("writeToFile", path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("writeToFile","size "+bytes.length);
		
		/*FileInputStream fis = null;
		Log.e("readFrom","length "+bytes.length);
		byte[] buffer = new byte[bytes.length];
		try {
			fis = ctx.openFileInput(path);
			int val = fis.read(buffer);
			
			if (val != -1) {
				Log.e("readFrom","read size "+val);
			}
		} catch (IOException e) { e.printStackTrace(); }
		System.out.println(bytes.toString()+"\n\nbuffer:\n\n"+buffer.toString());
		Log.e("readFrom","equal "+(Arrays.equals(bytes,buffer)));*/
	  
		//return buffer;
		
	}
	public static byte[] readFrom(String path, Context ctx) {

		FileInputStream fis = null;
		byte[] buffer = new byte[4096];
		byte[] res = null;
		try {
			fis = ctx.openFileInput(path);
			int val = fis.read(buffer);
			
			if (val == 4096) {
				Log.e("readFrom","file too big "+val);
			} else {
				Log.d("readFrom","size "+val);
				res=Arrays.copyOf(buffer, val);
			}
		} catch (IOException e) { e.printStackTrace(); }
	  
		return res;
	}
	/*
	 * public static void testFile(String s, String path, Context ctx) {
	 * FileOutputStream fos; try { fos = ctx.openFileOutput(path,
	 * Context.MODE_PRIVATE); fos.write(s.getBytes()); fos.close();
	 * Log.d("writeToFile",path); } catch (FileNotFoundException e) {
	 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
	 * 
	 * 
	 * FileInputStream fis = null; StringBuffer fileContent = new
	 * StringBuffer(""); try { fis = ctx.openFileInput(path); byte[] buffer =
	 * new byte[1024]; int val = fis.read(buffer);; while ( val!= -1) {
	 * fileContent.append((new String(buffer)).substring(0, val));
	 * val=fis.read(buffer); } } catch (IOException e) { e.printStackTrace(); }
	 * Log.d("test1",s); Log.d("test2",fileContent.toString()); }
	 */

	/**
	 * Reads the file at the location path and returns the content in bytes
	 * 
	 * @param path
	 * @return
	 */
	public static byte[] readFile(String path) {
		byte[] b = null;
		try {
			RandomAccessFile f = new RandomAccessFile(path, "r");
			b = new byte[(int) f.length()];
			f.read(b);
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

	public static String readFileString(String path, Context ctx) {
		FileInputStream fis = null;
		StringBuffer fileContent = new StringBuffer("");
		try {
			fis = ctx.openFileInput(path);
			byte[] buffer = new byte[1024];
			int val = fis.read(buffer);
			
			while (val != -1) {
				fileContent.append((new String(buffer)).substring(0, val));
				val = fis.read(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileContent.toString();
	}
	
	
	public static String readFileString2(String path, Context ctx) {
		FileInputStream fis = null;
		StringBuffer fileContent = new StringBuffer("");
		try {
			fis = ctx.openFileInput(path);
			byte[] buffer = new byte[1024];
			int val = fis.read(buffer);
			
			while (val != -1) {
				fileContent.append((new String(buffer)));
				val = fis.read(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileContent.toString();
	}

	/**
	 * Checks if external storage is available for read and write
	 * 
	 * @return
	 */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if external storage is available to at least read
	 * 
	 * @return
	 */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the File filename located in "keystrokeLogin"
	 * 
	 * @param fileName
	 * @return
	 */
	public File getFile(String fileName) {
		File file = new File(
				Environment.getExternalStoragePublicDirectory("keystrokeLogin"),
				fileName);
		if (!file.mkdirs()) {
			Log.e("file", "Directory not created");
		}
		return file;
	}

	/**
	 * Writes the string value in the file named path inside the folder
	 * KeystrokeDynamics
	 * 
	 * @param path
	 * @param value
	 */
	public static void writeTo(String path, String value) {

		File root = android.os.Environment.getExternalStorageDirectory();
		Log.d("writeTo", "\nExternal file system root: " + root);

		File dir = new File(root.getAbsolutePath() + "/KeystrokeDynamics");
		dir.mkdirs();
		File file = new File(dir, path);

		try {
			FileOutputStream f = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(f);
			pw.println(value);
			pw.flush();
			pw.close();
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i("fileInfo",
					"******* File not found. Did you"
							+ " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("writeTo", "\n\nFile written to " + file);
	}

	/**
	 * Reads the file path under the folder KeystrokeDynamics
	 * 
	 * @param path
	 */
	public static String readFrom(String path) {
		Log.d("writeTo", "read");
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/KeystrokeDynamics");
		dir.mkdirs();
		File file = new File(dir, path);
		InputStream is = null;
		try {
			is = new FileInputStream(file);
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
		Log.d("writeTo", "\n\nThat is all");
		return sb.toString();
	}



}
