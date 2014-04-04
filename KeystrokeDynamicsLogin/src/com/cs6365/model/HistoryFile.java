package com.cs6365.model;
import java.util.Vector;

public class HistoryFile {
	private Vector<Vector<Double>> featureVectors;
	
	public HistoryFile(){
		featureVectors = new Vector<Vector<Double>>();
	}
	
	public void addEntry(Vector<Double> entry){
		featureVectors.add(entry);
	}
	
	public Vector<Double> get(int index){
		return featureVectors.get(index);
	}
	
	public int getSize(){
		return featureVectors.size();
	}
	
	public void removeFirstEntry(){
		featureVectors.remove(0);
	}
	
	public String toString(){
		String result = "";
		for (Vector<Double> vector:featureVectors){
			for (Double d: vector){
				result += d + " ";
			}
			result+="\n";
		}
		return result;
	}
	
	public Vector<Double> computeMeanValues(){
		Vector<Double> means = new Vector<Double>();

		int numFeatures = 0;
		double mean = 0.;
		if(featureVectors.size()!=0){
			numFeatures=featureVectors.get(0).size();
		}
		for(int i=0;i<numFeatures;i++){
			for (Vector<Double> vector:featureVectors){
				mean += vector.get(i);
			}
			mean=mean/numFeatures;
			means.add(mean);
		}
		return means;
	}
	
	public Vector<Double> computeDeviations(){
		Vector<Double> means = computeMeanValues();
		Vector<Double> deviations = new Vector<Double>();
		
		for (Vector<Double> vector:featureVectors){
			int count = 0;
			double deviation = 0.;
			for (int i=0;i<vector.size();i++){
				Double d = vector.get(i);
				if (d!=-1.){
					count++;
					deviation += Math.pow(d-means.get(i),2);
				}
			}
			deviation = Math.sqrt(deviation/count);
			deviations.add(deviation);
		}
		return deviations;
	}


}
