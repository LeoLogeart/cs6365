package com.cs6365.model;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

public class Authentication {


	private final static int thresholdPress=100;
	private final static int thresholdTimeBetweenPress=300;
	private final static int hSize = 10;
	private final static int hByteSize = 3200;
	private final static double k = 1.;

	
	/**
	 * Initialization of the data structures : Create empty history file, and initial
	 * instruction table
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
				.generateRandomPolynomial(m - 1, q);
		polynomial.add(0, hpwd);
		Vector<BigInteger> alphas = new Vector<BigInteger>();
		Vector<BigInteger> betas = new Vector<BigInteger>();
		for (int i = 0; i < m; i++) {
			// Computation of initial values for alpha and beta
			int x1 = 2 * (i + 1);
			int x2 = x1 + 1;
			BigInteger y1 = Utils.valueOfPolynomial(x1, polynomial).mod(q);
			BigInteger y2 = Utils.valueOfPolynomial(x2, polynomial).mod(q);
			BigInteger hash1 = Utils.computeSha256(x1, pwd).mod(q);
			BigInteger hash2 = Utils.computeSha256(x2, pwd).mod(q);
			BigInteger alpha = y1.multiply(hash1);
			BigInteger beta = y2.multiply(hash2);
			alphas.add(alpha);
			betas.add(beta);
		}
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
		Utils.writeToFile(cipher, "history" + userId, ctx);
	}

	
	/**
	 * Proceeds to the authentication of userId. Returns true if authentication
	 * succeeded and false otherwise.
	 * 
	 * @param featureVector
	 * @param userId
	 * @param pwd
	 * @param ctx
	 * @return
	 */
	public static boolean authenticate(Vector<Double> featureVector,
			String userId, String pwd, Context ctx) {
		// Loading of the user's instruction table
		int x;
		BigInteger y;
		InstructionTable table = loadInstructionTable(userId, ctx);
		Vector<BigInteger> alphas = table.getAlphas();
		Vector<BigInteger> betas = table.getBetas();
		BigInteger q = table.getQ();
		Vector<Integer> xs = new Vector<Integer>();
		Vector<BigInteger> ys = new Vector<BigInteger>();
		int threshold = thresholdTimeBetweenPress;
		for (int ind = 0; ind < featureVector.size(); ind++) {// TODO
			// Computation of the set of m points (xi,yi)
			if ( ind == (featureVector.size() - 1) / 2) {
				threshold = thresholdPress;
			}
				Double feature = featureVector.get(ind);
				if (feature < threshold) {
					x = 2 * (ind + 1);
					BigInteger hash = Utils.computeSha256(x, pwd).mod(q);
					y = alphas.get(ind).divide(hash);
				} else {
					x = 2 * (ind + 1) + 1;
					BigInteger hash = Utils.computeSha256(x, pwd).mod(q);
					y = betas.get(ind).divide(hash);
				}
			//TODO
			xs.add(x);
			ys.add(y);
		}
		// Interpolation to get the hardened password
		BigInteger hpwd = Utils.interpolate(xs, ys, q);
		// Attempt to decrypt the file
		byte[] history = Utils.readFile("history" + userId);
		history = Utils.decrypt(history, hpwd.toString().toCharArray());
		String historyString = "";
		try {
			historyString = new String(history, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String[] parts = historyString.split("/");
		// If it is decrypted, the content should be a set of double values
		// followed
		// by a slash, followed by the same set of values

		// Checking that the file is split into two parts
		if (parts.length != 2)
			return false;
		String part0 = parts[0];
		String part1 = parts[1].substring(0, part0.length());
		// Checking that the two parts are equal
		if (part0.equals(part1)) {
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
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			// Computation of mean values and standard deviations
			Vector<Double> meanValues = hFile.computeMeanValues();
			Vector<Double> deviations = hFile.computeDeviations();
			int m = featureVector.size();
			Vector<BigInteger> polynomial = Utils.generateRandomPolynomial(
					m - 1, q);
			polynomial.add(0, hpwd);
			Vector<BigInteger> newAlphas = new Vector<BigInteger>();
			Vector<BigInteger> newBetas = new Vector<BigInteger>();
			threshold=thresholdTimeBetweenPress;
			for (int i = 0; i < m; i++) {
				// Computation of the new values for alpha and beta TODO
				int x1 = 2 * (i + 1);
				int x2 = x1 + 1;
				BigInteger y1;
				BigInteger y2;
				if (hFile.getSize() > hSize) {
					double mu = meanValues.get(i);
					double sigma = deviations.get(i);
					if ( i == (featureVector.size() - 1) / 2) {
						threshold = thresholdPress;
					}
					if (Math.abs(mu - threshold) > k * sigma) {
						if (mu < threshold) {
							y1 = Utils.valueOfPolynomial(x1, polynomial).mod(q);
							y2 = Utils.random(q);
						} else {
							y1 = Utils.random(q);
							y2 = Utils.valueOfPolynomial(x2, polynomial).mod(q);
						}
					} else {
						y1 = Utils.valueOfPolynomial(x1, polynomial).mod(q);
						y2 = Utils.valueOfPolynomial(x2, polynomial).mod(q);
					}
				} else {
					y1 = Utils.valueOfPolynomial(x1, polynomial).mod(q);
					y2 = Utils.valueOfPolynomial(x2, polynomial).mod(q);
				}
				BigInteger hash1 = Utils.computeSha256(x1, pwd).mod(q);
				BigInteger hash2 = Utils.computeSha256(x2, pwd).mod(q);
				BigInteger alpha = y1.multiply(hash1);
				BigInteger beta = y2.multiply(hash2);
				newAlphas.add(alpha);
				newBetas.add(beta);
			}// TODO
			InstructionTable newTable = new InstructionTable(newAlphas,
					newBetas, q);
			storeInstructionTable(newTable, userId, ctx);
			return true;
		} else
			return false;
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
		Utils.writeToFile(content.getBytes(), "inst" + userId, ctx);
		// Utils.testFile(content, "inst" + userId, ctx);
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
		String path = "inst" + userId;
		String content = "";
		content = Utils.readFileString(path, ctx);
		/*
		 * BufferedReader br; try { String line; br = new BufferedReader(new
		 * FileReader(path)); while ((line = br.readLine()) != null) { content
		 * += line; } br.close(); } catch (IOException e) { e.printStackTrace();
		 * }
		 */
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
		File file = ctx.getFileStreamPath("history" + userId);
		Log.d("userExists", userId + ";" + file.exists());
		return file.exists();
	}
}
