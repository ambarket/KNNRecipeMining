import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

enum DistanceFunction { JACCARD, CUSTOM01, CUSTOM02 }
enum VoteWeightFunction { DISTANCE, ENTROPY }

public class Main {
    // All other classes just use these static variable to avoid passing a bunch of arguments around.
	static ArrayList<Recipe> trainingData;
	// value is array from cuisineID (1 to 7) to number of occurances.
	static HashMap<String, int[]> cuisineCounts;
	
	// Parameter to tune
	static int k = 10;
	static int numberOfThreads = 4;
	static DistanceFunction distanceFunction = DistanceFunction.JACCARD;
	static VoteWeightFunction voteWeightFunction = VoteWeightFunction.DISTANCE;
	
	static float[][] trainingDataDistanceMatrix;
	
	
	public static void main(String[] args) {
		long startTime, endTime, seconds;
		startTime = System.currentTimeMillis();

	    	setTrainingData();
		setCuisineCounts();
		setTrainingDataDistanceMatrix();

		for (Recipe r : trainingData) {
			r.setEntropy();
		}
		CrossValidateOnNThreads crossValidator = new CrossValidateOnNThreads();
		endTime = System.currentTimeMillis();
		seconds = (endTime - startTime) / 1000;
		System.out.println("Read file and preprocessed in " + seconds + " seconds");
		
		startTime = System.currentTimeMillis();
		double accuracy = crossValidator.runAndReturnResult();
		System.out.println("Accuracy: " + accuracy);
		
		endTime = System.currentTimeMillis();
		seconds = (endTime - startTime) / 1000;
		System.out.println("Ran CrossValidation in " + seconds + " seconds");
		//SingleThreaded.crossValidate();
		
	}
	
	public static void setTrainingData() {
		String trainingFile = "training-data.txt";
		
		trainingData = new ArrayList<Recipe>();

		try {
			// use FileWriter constructor that specifies open for appending
			BufferedReader br = new BufferedReader(new FileReader(new File(trainingFile)));
			
			String line;

			while ((line = br.readLine()) != null) {
				trainingData.add(new Recipe(true, line));
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
				cuisineCounts.get(ingr)[r.cuisine]++;;
			}
		}
	}
	
	public static void setTrainingDataDistanceMatrix() {
	    trainingDataDistanceMatrix = new float[trainingData.size()][trainingData.size()];
	    for (int i = 0; i < trainingData.size(); i++) {
		    for (int j = 0; i < trainingData.size(); i++) {
			trainingDataDistanceMatrix[i][j] = trainingData.get(i).getDistance(trainingData.get(j));
		    }
	    }
	}
	
	public static void runAgainstTestSet()  {
	    	double[] distanceSpaceForThisThread = new double[trainingData.size()];
          	Scanner sc = new Scanner(System.in);
          	Recipe test;
          	while (sc.hasNextLine()) {
          		test = new Recipe(false, sc.nextLine());
          		System.out.println(Predicter.predictCuisine(test, distanceSpaceForThisThread));
          	}
	}
}
