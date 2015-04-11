import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

enum DistanceFunction { JACCARD, CUSTOM01, CUSTOM02, GA_JACCARD }
enum VoteWeightFunction { DISTANCE, ENTROPY }

public class Main {
    // All other classes just use these static variable to avoid passing a bunch of arguments around.
	static ArrayList<Recipe> trainingData;	// size - 21667 examples
	// value is array from cuisineID (1 to 7) to number of occurances.
	// index 0 is the total number of occurances
	static HashMap<String, int[]> cuisineCounts;
	static HashMap<String, Integer> uniqueIngredients;
	
	// Parameter to tune
	static int k = 10;
	static int numberOfThreads = 4;
	static DistanceFunction distanceFunction = DistanceFunction.JACCARD;
	static VoteWeightFunction voteWeightFunction = VoteWeightFunction.DISTANCE;
	
	// For k-fold cross val, setting this numberOfExamples implies leaveOneOut
	// Make this a factor of the number of threads for good results
	static int numberOfFolds = 4;	
	static int numberPerFold;	// this is set to numberOfExamples / numberOfFolds
	
	static float[][] trainingDataDistanceMatrix;
	
	
	public static void main(String[] args) {
		long startTime, endTime, seconds;
		startTime = System.currentTimeMillis();
	    setTrainingData();
		setCuisineCounts();
		int ingrNum = 0;
		uniqueIngredients = new HashMap<String, Integer>();
		for (String ingr : Main.cuisineCounts.keySet()) {
			uniqueIngredients.put(ingr, ingrNum);
			ingrNum++;
		}
		numberPerFold = Main.trainingData.size() / numberOfFolds;
		System.out.println("Number of folds: " + Main.numberOfFolds + " " + "Number in each fold " + numberPerFold);

		//uniqueIngredients = new ArrayList<String>(Main.cuisineCounts.keySet());
		endTime = System.currentTimeMillis();
		seconds = (endTime - startTime) / 1000;
		System.out.println("Read file and cuisine counts in " + seconds + " seconds");

		/*	This is way more massive than I thought...
		startTime = System.currentTimeMillis();
		setTrainingDataDistanceMatrix();
		endTime = System.currentTimeMillis();
		seconds = (endTime - startTime) / 1000;
		System.out.println("Calculated distance matrix in " + seconds + " seconds");
		
		startTime = System.currentTimeMillis();
		saveDistanceMatrixToFile();
		endTime = System.currentTimeMillis();
		seconds = (endTime - startTime) / 1000;
		*/
		
		for (Recipe r : trainingData) {
			r.setEntropy();
		}
		CrossValidateOnNThreads crossValidator = new CrossValidateOnNThreads();

		
		startTime = System.currentTimeMillis();
		double accuracy = crossValidator.runAndReturnResult();
		System.out.println("Accuracy: " + accuracy);
		
		endTime = System.currentTimeMillis();
		seconds = (endTime - startTime) / 1000;
		System.out.println("Ran CrossValidation in " + seconds + " seconds");
		//SingleThreaded.crossValidate();
		
		/*
		Simulation s = new Simulation(30);
		float[] weights = s.runAndReturnBest(10000000);
		System.out.print("float[] weights = { ");
		for (int i = 0; i < weights.length; i++) {
			System.out.print(weights[i] + ", ");
			if (i % 51 == 0) {
				System.out.println();
			}
		}
		System.out.print(" };");
		*/
	}
	
	static int weightFileNum = 0;
	public static void writeWeightsToFile(float[] weights, float accurracy) {
		weightFileNum++;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("weights" + weightFileNum + "acc-" + accurracy + ".txt")));
			for (String ingr : uniqueIngredients.keySet()) {
				int ingrNum = uniqueIngredients.get(ingr);
				bw.write(ingr + '\t' + weights[ingrNum] + '\n');
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void setTrainingData() {
		String trainingFile = "training-data.txt";
		
		trainingData = new ArrayList<Recipe>();

		try {
			// use FileWriter constructor that specifies open for appending
			BufferedReader br = new BufferedReader(new FileReader(new File(trainingFile)));
			
			String line;
			int recipeNum = 0;
			while ((line = br.readLine()) != null) {
				trainingData.add(new Recipe(true, line, recipeNum));
				recipeNum++;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setCuisineCounts() {
		cuisineCounts = new HashMap<String, int[]>();
		for (Recipe r : trainingData) {
			for (String ingr : r.ingredients) {
				if (!cuisineCounts.containsKey(ingr)) {
					cuisineCounts.put(ingr, new int[8]);	
				}
				cuisineCounts.get(ingr)[r.cuisine]++;
				cuisineCounts.get(ingr)[0]++;	// Use index 0 to store total
			}
		}
	}
	
	public static void setTrainingDataDistanceMatrix() {
	    trainingDataDistanceMatrix = new float[trainingData.size()][];
	    for (int i = 0; i < trainingData.size(); i++) {
	    	trainingDataDistanceMatrix[i] = new float[i+1];
		    for (int j = 0; j < i; j++) {
		    	trainingDataDistanceMatrix[i][j] = trainingData.get(i).getDistance(trainingData.get(j));
		    }
		   /* if (i % 500 == 0) {
		    	System.out.println(i);
		    }*/
	    }
	}
	
	public static void saveDistanceMatrixToFile() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(distanceFunction.toString() + "DistanceMatrix.txt")));
		    for (int i = 1; i < trainingData.size(); i++) {
		    	trainingDataDistanceMatrix[i] = new float[i];
			    for (int j = 0; j < i; j++) {
			    	bw.write(String.valueOf(trainingDataDistanceMatrix[i][j]));
			    	bw.write('\t');
			    }
			    if (i % 500 == 0) {
			    	System.out.println(i);
			    }
		    }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void runAgainstTestSet()  {
	    	double[] distanceSpaceForThisThread = new double[trainingData.size()];
          	Scanner sc = new Scanner(System.in);
          	Recipe test;
          	while (sc.hasNextLine()) {
          		//test = new Recipe(false, sc.nextLine());
          		//System.out.println(Predicter.predictCuisine(test, distanceSpaceForThisThread));
          	}
	}
}
