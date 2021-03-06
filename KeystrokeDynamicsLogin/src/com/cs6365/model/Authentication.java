package com.cs6365.model;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

public class Authentication {

	private final static int thresholdTimeBetweenPressPinLandscape = 90;
	private final static int thresholdTimeBetweenPressPin = 230;
	private final static int thresholdTimeBetweenPressLandscape = 180;
	private final static int thresholdTimeBetweenPress = 418;
	private final static int thresholdPress = 200;
	private final static int hSize = 10;
	private final static int hByteSize = 3200;
	private final static double k = 1.;

	
	/**
	 * Initialization of the data structures : Create empty history file, and
	 * initial instruction table
	 * 
	 * @param userId
	 * @param m
	 * @param pwd
	 * @param ctx
	 */
	public static void initialization(String userId, int m, String pwd,
			Context ctx) {
		// Generation of initial data
		BigInteger q = Utils.generatePrimeNumber(160);
		BigInteger hpwd = Utils.random(q);
		Vector<BigInteger> polynomial = Utils
				.generateRandomPolynomial(m - 2, q);
		polynomial.add(0, hpwd);
		Vector<BigInteger> alphas = new Vector<BigInteger>();
		Vector<BigInteger> betas = new Vector<BigInteger>();
		for (int i = 0; i < m; i++) {
			// Computation of initial values for alpha and beta
			int x1 = i + 1;
			int x2 = i + 1;
			BigInteger y1 = Utils.valueOfPolynomial(x1, polynomial);
			BigInteger y2 = Utils.valueOfPolynomial(x2, polynomial);
			BigInteger hash1 = Utils.computeSha256(x1, pwd);
			BigInteger hash2 = Utils.computeSha256(x2, pwd);
			BigInteger alpha = y1.multiply(hash1).mod(q);
			BigInteger beta = y2.multiply(hash2).mod(q);
			alphas.add(alpha);
			betas.add(beta);
		}
		Log.d("Init",q.toString());
		InstructionTable table = new InstructionTable(alphas, betas, q);
		storeInstructionTable(table, userId, ctx);
		// Generation of an empty history file
		String emptyHistory = " / ";
		byte[] emptyBytes = null;
		try {
			emptyBytes = emptyHistory.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// Padding and encryption
		byte[] history = Utils.pad(emptyBytes, hByteSize);
		byte[] cipher = Utils.encrypt(history, hpwd.toString().toCharArray());
		//Utils.writeToFile(cipher, "history" + userId, ctx);
		Utils.writeToExtFile(cipher, "history" + userId, ctx);//change with value above to write in the internal storage
	}

	/**
	 * Proceeds to the authentication of userId. Returns true if authentication
	 * succeeded and false otherwise.
	 * 
	 * @param featureVector
	 * @param userId
	 * @param pwd
	 * @param ctx
	 * @param portrait 
	 * @return
	 */
	public static boolean authenticate(Vector<Double> featureVector,
			String userId, String pwd, Context ctx, boolean portrait, boolean pin) {
		// Loading of the user's instruction table
		Log.d("Authenticate","------------------");
		Log.d("Authenticate", userId+" "+pwd);
		int x;
		BigInteger y;
		InstructionTable table = loadInstructionTable(userId, ctx);
		Vector<BigInteger> alphas = table.getAlphas();
		Vector<BigInteger> betas = table.getBetas();
		BigInteger q = table.getQ();
		Vector<Integer> xs = new Vector<Integer>();
		Vector<BigInteger> ys = new Vector<BigInteger>();
		int threshold;
		if(pin && portrait){
			threshold = thresholdTimeBetweenPressPin;
		} else if (pin) {
			threshold = thresholdTimeBetweenPressPinLandscape;
		} else if (portrait){
			threshold = thresholdTimeBetweenPress;
		} else {
			threshold = thresholdTimeBetweenPressLandscape;
		}
		
		StringBuilder testing = new StringBuilder();
		if(featureVector.size()>alphas.size()){//Password too long
			return false;
		}
		for (int ind = 0; ind < featureVector.size(); ind++) {
			if (ind == (featureVector.size() - 1) / 2) {
				threshold = thresholdPress;
			}
			Double feature = featureVector.get(ind);
			if (feature < threshold) {
				testing.append(ind+": Inf, ");
				x=ind + 1;
				BigInteger hashInv = Utils.computeSha256(x, pwd).modInverse(q);
				y = alphas.get(ind).multiply(hashInv).mod(q);
			} else {
				testing.append(ind+": sup, ");
				x=ind + 1;
				BigInteger hashInv = Utils.computeSha256(x, pwd).modInverse(q);
				y = betas.get(ind).multiply(hashInv).mod(q);
			}
			xs.add(x);
			ys.add(y);
		}
		Log.d("features",testing.toString());
		// Interpolation to get the hardened password
		BigInteger hpwd = Utils.interpolate(xs, ys, q);
		Log.d("hpwd","hpwd  : "+hpwd.toString());
		// Attempt to decrypt the file
		//byte[] history = Utils.readFrom("history" + userId, ctx);
		byte[] history = Utils.readFromExt("history" + userId, ctx);//change with value above to write in the internal storage
		history = Utils.decrypt(history, hpwd.toString().toCharArray());
		String historyString = "";
		try {
			historyString = new String(history, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String[] parts = historyString.split("/");
		// If it is decrypted, the content should be a set of double values
		// followed by a slash, followed by the same set of values

		// Checking that the file is split into two parts
		if (parts.length != 2) {
			Log.i("Authenticate", "history not decrypted : wrong password");
			return false;
		}
		String part0 = parts[0];
		String part1 = parts[1].substring(0, part0.length());
		// Checking that the two parts are equal
		if (!part0.equals(part1)) {
			Log.i("Authenticate", "history not decrypted : wrong password");
			return false;
		}
		// Reading history file
		HistoryFile hFile = extractHistory(part0);
		// Adding the new vector to the history
		hFile.addEntry(featureVector);
		// Truncate the history file if more than h attempts
		if (hFile.getSize() > hSize) {
			hFile.removeFirstEntry();
		}
		// Adding redundancy
		String content = hFile.toString() + "/" + hFile.toString();
		// Padding and encryption
		try {
			byte[] newHistory = content.getBytes("UTF-8");
			newHistory = Utils.pad(newHistory, hByteSize);
			byte[] cipher = Utils.encrypt(newHistory, hpwd.toString()
					.toCharArray());
			Utils.writeToFile(cipher, "history" + userId, ctx);
			Utils.writeToExtFile(cipher, "history" + userId, ctx);//change with value above to write in the internal storage
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// Computation of mean values and standard deviations
		Vector<Double> meanValues = hFile.computeMeanValues();
		Vector<Double> deviations = hFile.computeDeviations();
		int m = featureVector.size();
		Vector<BigInteger> polynomial = Utils
				.generateRandomPolynomial(m - 2, q);
		polynomial.add(0, hpwd);
		Vector<BigInteger> newAlphas = new Vector<BigInteger>();
		Vector<BigInteger> newBetas = new Vector<BigInteger>();
		testing = new StringBuilder();
		if(pin && portrait){
			threshold = thresholdTimeBetweenPressPin;
		} else if (pin) {
			threshold = thresholdTimeBetweenPressPinLandscape;
		} else if (portrait){
			threshold = thresholdTimeBetweenPress;
		} else {
			threshold = thresholdTimeBetweenPressLandscape;
		}
		Log.i("features","attempt "+hFile.getSize());
		for (int i = 0; i < m; i++) {
			// Computation of the new values for alpha and beta
			int x1 = i+1;
			int x2 = i+1;
			BigInteger y1;
			BigInteger y2;
			if (hFile.getSize() == hSize) {
				double mu = meanValues.get(i);
				double sigma = deviations.get(i);
				if (i == (featureVector.size() - 1) / 2) {
					threshold = thresholdPress;
				}
				if (Math.abs(mu - threshold) > k * sigma) {
					if (mu < threshold) {
						testing.append(i+": inf, ");
						y1 = Utils.valueOfPolynomial(x1, polynomial);
						y2 = Utils.random(q);
					} else {
						testing.append(i+": sup, ");
						y1 = Utils.random(q);
						y2 = Utils.valueOfPolynomial(x2, polynomial);
					}
				} else {
					y1 = Utils.valueOfPolynomial(x1, polynomial);
					y2 = Utils.valueOfPolynomial(x2, polynomial);
				}
			} else {
				y1 = Utils.valueOfPolynomial(x1, polynomial);
				y2 = Utils.valueOfPolynomial(x2, polynomial);
			}
			BigInteger hash1 = Utils.computeSha256(x1, pwd);
			BigInteger hash2 = Utils.computeSha256(x2, pwd);
			BigInteger alpha = y1.multiply(hash1).mod(q);
			BigInteger beta = y2.multiply(hash2).mod(q);
			newAlphas.add(alpha);
			newBetas.add(beta);
		}
		Log.d("Distinguishing",testing.toString());
		InstructionTable newTable = new InstructionTable(newAlphas, newBetas, q);
		storeInstructionTable(newTable, userId, ctx);
		return true;
	}

	/**
	 * Stores the instruction table of the given user
	 * 
	 * @param table
	 * @param userId
	 * @param ctx
	 */
	public static void storeInstructionTable(InstructionTable table,
			String userId, Context ctx) {
		String content = table.getQ().toString();
		for (int i = 0; i < table.getAlphas().size(); i++) {
			content += " " + table.getAlphas().get(i) + " "
					+ table.getBetas().get(i);
		}
		//Utils.writeToFile(content.getBytes(), "instruction" + userId, ctx);
		Utils.writeToExtFileString(content, "instruction" + userId, ctx);//change with value above to write in the internal storage
	}

	/**
	 * Loads the instruction table of the given user
	 * 
	 * @param userId
	 * @param ctx
	 * @return
	 */
	public static InstructionTable loadInstructionTable(String userId,
			Context ctx) {
		InstructionTable result;
		Vector<BigInteger> alphas = new Vector<BigInteger>();
		Vector<BigInteger> betas = new Vector<BigInteger>();
		String path = "instruction" + userId;
		String content = "";
		//content = Utils.readFileString(path, ctx);
		content = Utils.readExtFileString(path, ctx);//change with value above to write in the internal storage
		String[] parts = content.split("\\s+");
		BigInteger q = new BigInteger(parts[0]);
		for (int i = 1; i < parts.length; i = i + 2) {
			BigInteger alpha = new BigInteger(parts[i]);
			BigInteger beta = new BigInteger(parts[i + 1]);
			alphas.add(alpha);
			betas.add(beta);
		}
		result = new InstructionTable(alphas, betas, q);
		return result;
	}

	/**
	 * Extracts the entries of a history file
	 * 
	 * @param historyContent
	 * @return
	 */
	public static HistoryFile extractHistory(String historyContent) {
		HistoryFile hFile = new HistoryFile();
		if (historyContent.length() == 1)
			return hFile;
		String[] lines = historyContent.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String[] parts = lines[i].split("\\s+");
			if (parts.length > 1) {
				Vector<Double> vector = new Vector<Double>();
				for (int j = 0; j < parts.length; j++) {
					vector.add(Double.valueOf(parts[j]));
				}
				hFile.addEntry(vector);
			}
		}
		return hFile;
	}

	/**
	 * Checks if a userId already exists
	 * 
	 * @param userId
	 * @param ctx
	 * @return
	 */
	public static boolean userExists(String userId, Context ctx) {
		/*File file = ctx.getFileStreamPath("history" + userId);
		Log.d("userExists", userId + ";" + file.exists());
		return file.exists();*/
		String hashPath = Utils.sha256("history"+userId);//change with value above to write in the internal storage
		File root = android.os.Environment.getExternalStorageDirectory();
		File f = new File(root.getAbsolutePath()
				+ "/KeystrokeDynamicsLogin/"+hashPath);

		Log.d("userExists", f.getAbsolutePath());
		return f.exists();
	}
}
