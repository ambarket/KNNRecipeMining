import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

enum DistanceFunction { JACCARD, CUSTOM01, CUSTOM02, GA_JACCARD, CUISINE_PROB_JACCARD }
enum VoteWeightFunction { DISTANCE, ENTROPY, NONE }

public class Main {
    // All other classes just use these static variable to avoid passing a bunch of arguments around.
	static ArrayList<Recipe> trainingData;	// size - 21667 examples
	static HashSet<String> tabooList;
	// value is array from cuisineID (1 to 7) to number of occurances.
	// index 0 is the total number of occurances
	static HashMap<String, int[]> cuisineCounts;
	static HashMap<String, float[]> cuisineProbabilitiesByIngr;
	static HashMap<String, Integer> uniqueIngredients;
	static float[] cusisineIngrWeights;
	
	// Parameter to tune
	static int k = 10;
	static int numberOfThreads = 4;
	static int o = 0; // minimum threshold for words that appear in One cuisine (i.e. value of 0 will take all)
	static int s = 100; // minimum difference of proportions for words that appear in Seven cuisines
	static DistanceFunction distanceFunction = DistanceFunction.CUISINE_PROB_JACCARD;
	static VoteWeightFunction voteWeightFunction = VoteWeightFunction.DISTANCE;
	
	// For k-fold cross val, setting this numberOfExamples implies leaveOneOut
	// Make this a factor of the number of threads for good results
	static int numberOfFolds = 4;	
	static int numberPerFold;	// this is set to numberOfExamples / numberOfFolds
	
	static float[][] trainingDataDistanceMatrix;
	
	
	public static void main(String[] args) {
		long startTime, endTime, seconds;
		startTime = System.currentTimeMillis();
		buildTabooSet();
	    setTrainingData();
		setCuisineCounts();
		
		int ingrNum = 0;
		uniqueIngredients = new HashMap<String, Integer>();
		for (String ingr : Main.cuisineCounts.keySet()) {
			uniqueIngredients.put(ingr, ingrNum);
			ingrNum++;
		}
		
		setCuisineProbabilitiesByIngr();
		setCuisineIngrWeightsFromCuisineProbabilities();
		

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
		runTestsOnParameters();
		/*
		CrossValidateOnNThreads crossValidator = new CrossValidateOnNThreads();

		
		startTime = System.currentTimeMillis();
		double accuracy = crossValidator.runAndReturnResult();
		System.out.println("Accuracy: " + accuracy);
		
		endTime = System.currentTimeMillis();
		seconds = (endTime - startTime) / 1000;
		System.out.println("Ran CrossValidation in " + seconds + " seconds");
		//SingleThreaded.crossValidate();
		*/
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
	
	public static void runTestsOnParameters() {
		File output = new File("paramTest.txt");
		for (DistanceFunction df : DistanceFunction.values()) {
			for (VoteWeightFunction vf : VoteWeightFunction.values()) {
				for (int newK = 2; newK < 128; newK*=2) {
					long startTime, endTime, seconds;
					startTime = System.currentTimeMillis();
					Main.distanceFunction = df;
					Main.voteWeightFunction = vf;
					Main.k = newK;
					Main.numberOfFolds = 4;	
					Main.numberPerFold = Main.trainingData.size() / Main.numberOfFolds;
					CrossValidateOnNThreads crossValidator = new CrossValidateOnNThreads();
					startTime = System.currentTimeMillis();
					double accuracy = crossValidator.runAndReturnResult();
					
					System.out.println("Accuracy: " + accuracy);
					endTime = System.currentTimeMillis();
					seconds = (endTime - startTime) / 1000;
					System.out.println("Ran CrossValidation in " + seconds + " seconds");
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(output, true));
						bw.write("Time(Seconds):" + '\t' + seconds + '\n');
						bw.write("k:" + '\t' + Main.k + '\n');
						bw.write("Distance Function:" + '\t' + Main.distanceFunction.toString() + '\n');
						bw.write("Vote Weighting:" + '\t' + Main.voteWeightFunction.toString() + '\n');
						bw.write("numberOfFolds:" + '\t' + Main.numberOfFolds + '\n');
						bw.write("Accuracy:" + '\t' + accuracy + '\n');
						bw.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
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
	
	public static void buildTabooSet() {
		try {
			tabooList = new HashSet<String>();
			String line;
			String splitLine[];
			String splitProportions[];
			BufferedReader br = new BufferedReader(new FileReader(new File("all.txt")));
			while ((line = br.readLine()) != null) {
				splitLine = line.split(":");
				if (splitLine.length != 2) {
					System.out.println("ERROR? " + line);
				}
				splitProportions = splitLine[1].split(" ");
				double min, max;
				min = max = Double.parseDouble(splitProportions[0]);
				for (int i = 1; i < splitProportions.length; i++) {
					double current = Double.parseDouble(splitProportions[i]);
					min = min < current ? min : current;
					max = max > current ? max : current;
				}
				if (max - min > s) {
					tabooList.add(splitLine[0]);
				}
			}
			br.close();
			
			br = new BufferedReader(new FileReader(new File("one.txt")));
			while ((line = br.readLine()) != null) {
				splitLine = line.split(":");
				if (splitLine.length != 2) {
					System.out.println("ERROR? " + line);
				}
				if (Integer.parseInt(splitLine[1]) < o ) {
					tabooList.add(splitLine[0]);
				}
			}
			br.close();
		} catch (Exception e) {
			System.out.println(e);
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
	
	public static void setCuisineProbabilitiesByIngr() {
		
		cuisineProbabilitiesByIngr = new HashMap<String, float[]>();
		for (String ingr : Main.uniqueIngredients.keySet()) {
			float[] probabilities = new float[Main.uniqueIngredients.size()];
			int[] ingrCuisineCounts = Main.cuisineCounts.get(ingr);
			for (int i = 1; i < 8; i++) {
				probabilities[i] = ingrCuisineCounts[i] / ingrCuisineCounts[0];
			}
			cuisineProbabilitiesByIngr.put(ingr, probabilities);
		}
	}
	
	public static void setCuisineIngrWeightsFromCuisineProbabilities() {
		cusisineIngrWeights = new float[Main.uniqueIngredients.size()];
		
		for (String ingr : Main.uniqueIngredients.keySet()) {
			float[] probabilities = Main.cuisineProbabilitiesByIngr.get(ingr);
			float minProb = Float.MAX_VALUE, maxProb = Float.MIN_VALUE;
			for (int i = 1; i < 8; i++) {
				if (probabilities[i] < minProb) {
					minProb = probabilities[i];
				}
				if (probabilities[i] > maxProb) {
					maxProb = probabilities[i];
				}
			}
			cusisineIngrWeights[Main.uniqueIngredients.get(ingr)] = maxProb - minProb;
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
