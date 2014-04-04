package com.cs6365.model;
import java.math.BigInteger;
import java.util.Vector;

public class InstructionTable {

	private Vector<BigInteger> alphas;
	private Vector<BigInteger> betas;
	private BigInteger q;

	public InstructionTable(Vector<BigInteger> alphas,
			Vector<BigInteger> betas, BigInteger q) {
		super();
		this.alphas = alphas;
		this.betas = betas;
		this.q = q;
	}

	public Vector<BigInteger> getAlphas() {
		return alphas;
	}

	public Vector<BigInteger> getBetas() {
		return betas;
	}

	public BigInteger getQ() {
		return q;
	}
}
