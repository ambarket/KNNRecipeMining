import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Main {
	static ArrayList<Recipe> trainingData;
	static int k;
	
	public static void main(String[] args) {
		k = 10;
		
		trainingData = readTrainingFile();
		/*
		for (Recipe r : trainingData) {
			for (Recipe r2 : trainingData) {
				System.out.println(r.jaccardDistance(r2));
			}
			//System.out.println(r);
		}
		*/
		long startTime, endTime, duration;
		startTime = System.nanoTime();
		
		//crossValidate();
		//test();
		
		CrossValidateOnNThreads c = new CrossValidateOnNThreads(trainingData, 10, 4);
		c.runAllThreads();
		
		endTime = System.nanoTime();
		duration = (endTime - startTime) / 1000000 / 1000;
		System.out.println(duration);
	}
	
	public static void crossValidate() {
		
		double correct = 0;
		int testNum = 0;
		for (Recipe test : trainingData) {
			//System.out.println("TestNum: " + testNum++);
			int predictedCuisine = predictCuisine(test);
			if (predictedCuisine == test.cuisine) {
				correct++;
			}
		}
		System.out.println(correct / (trainingData.size() - 1));
	}
	
	public static void test() {
		Scanner sc = new Scanner(System.in);
		Recipe test;
		while (sc.hasNextLine()) {
			test = new Recipe(false, sc.nextLine());
			System.out.println(predictCuisine(test));
		}
	}
	
	public static int predictCuisine(Recipe test) {
		Recipe[] nearestNeighbors = new Recipe[k];
		
		for (int i = 0; i < k; i++) {
			trainingData.get(i).distance = trainingData.get(i).jaccardDistance(test);
			nearestNeighbors[i] = trainingData.get(i);
			moveRecipeToCorrectLocation(i, nearestNeighbors);
		}
		
		for (int i = k; i < trainingData.size(); i++) {
			trainingData.get(i).distance = trainingData.get(i).jaccardDistance(test);
			if (trainingData.get(i).distance < nearestNeighbors[k-1].distance) {
				nearestNeighbors[k-1] = trainingData.get(i);
				moveRecipeToCorrectLocation(k-1, nearestNeighbors);
			}
		}
		
		// We now have the k nearest neighbors.
		int[] votes = new int[8];
		
		for (int i = 0; i < k; i++) {
			votes[nearestNeighbors[i].cuisine]++;
		}
		
		int maxVotes = -1;
		int predictedCuisine = -1;
		for (int i = 0; i < 8; i++) {
			if (votes[i] > maxVotes) {
				maxVotes = votes[i];
				predictedCuisine = i;
			}
		}
		return predictedCuisine;
	}
	
	public static void moveRecipeToCorrectLocation(int position, Recipe[] nearestNeighbors) {
		for (int j = position; j > 0; j--) {
			if (nearestNeighbors[j].distance < nearestNeighbors[j-1].distance) {
				Recipe tmp = nearestNeighbors[j-1];
				nearestNeighbors[j-1] = nearestNeighbors[j];
				nearestNeighbors[j] = tmp;
			}
		}
	}
	
	
	public static ArrayList<Recipe> readTrainingFile() {
		String trainingFile = "training-data.txt";
		
		ArrayList<Recipe> trainingRecipes = new ArrayList<Recipe>();

		try {
			// use FileWriter constructor that specifies open for appending
			BufferedReader br = new BufferedReader(new FileReader(new File(trainingFile)));
			
			String line;

			while ((line = br.readLine()) != null) {
				trainingRecipes.add(new Recipe(true, line));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return trainingRecipes;
	}
}

class Recipe {
	public double distance; // Only relevant in the context of a particular run of predictCuisine.
	public int cuisine;
	HashSet<String> ingredients;
	
	public Recipe(boolean training, String line) {
		ingredients = new HashSet<String>();
		String[] lineArray = line.split(" ");
		if (training) {
			cuisine = Integer.parseInt(lineArray[0]);
		}
		else {
			cuisine = -1; 
		}
		
		for (int i = 1; i < lineArray.length; i++) {
			ingredients.add(lineArray[i]);
		}
		

	}
	
	public boolean equalsRecipe(Recipe other) {
		return cuisine != -1 && other.cuisine != -1 && ingredients.size() == other.ingredients.size() && ingredients.containsAll(other.ingredients);
	}
	
	/*
	public void setDistance(double distance) {
		this.distance = distance;
	}
	*/
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(cuisine + " ");
		for (String s : ingredients) {
			sb.append(s + " ");
		}
		return sb.toString();
	}
	
	public double jaccardDistance(Recipe other) {
		HashSet<String> union = new HashSet();
		union.addAll(this.ingredients);
		union.addAll(other.ingredients);
		double unionSize = union.size();
		
		double intersectSize = 0;
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				intersectSize++;
			}
		}
		
		return 1 - intersectSize / unionSize;
	}
}

interface GetDataFromThread {
	void receiveData(int threadNum, int count);
}

class CrossValidateOnNThreads implements GetDataFromThread {
	boolean[] completedThreads;
	
	ArrayList<Recipe> trainingData;
	int k, numberOfThreads;
	
	double correctPredictions = 0;
	
	public CrossValidateOnNThreads(ArrayList<Recipe> trainingData, int k, int numberOfThreads) {
		this.trainingData = trainingData;
		this.k = k;
		this.numberOfThreads = numberOfThreads;
		completedThreads = new boolean[numberOfThreads];
	}
	
	public void runAllThreads() {
		int numberPerThread = trainingData.size() / numberOfThreads;
		for (int i = 0; i < numberOfThreads; i++) {
			RunSomeOfTheTests tmp = new RunSomeOfTheTests(i * numberPerThread, ((i+1) * numberPerThread - 1), trainingData, i, k, this);
			new Thread(tmp).start();
		}
	}
	
	public synchronized void receiveData(int threadNum, int count) {
		completedThreads[threadNum] = true;
		correctPredictions += count;
		
		boolean done = true;
		for (int i = 0; i < numberOfThreads; i++) {
			done = done && completedThreads[i];
		}
		
		if (done) {
			System.out.println("Accuracy: " + correctPredictions / trainingData.size());
		}
	}
}

class RunSomeOfTheTests implements Runnable {
	int start, end, threadNum, k, correct = 0;
	GetDataFromThread callback;
	ArrayList<Recipe> trainingData;
	public RunSomeOfTheTests(int start, int end, ArrayList<Recipe> trainingData, int threadNum, int k, GetDataFromThread callback) {
		this.start = start;
		this.end = end;
		this.k = k;
		this.threadNum = threadNum;
		this.trainingData = trainingData;
		this.callback = callback;
	}
	@Override
	public void run() {
		System.out.println("here");
		for (int i = start; i < end; i++) {
			Recipe test = trainingData.get(i);
			int predictedCuisine = predictCuisine(test);
			if (predictedCuisine == test.cuisine) {
				correct++;
				System.out.println("i = " + i + " on thread: " + threadNum + " Correct");
			}
			else {
				System.out.println("i = " + i + " on thread: " + threadNum + " incorrect");
			}
		}
		callback.receiveData(threadNum, correct);
	}
	
	public int predictCuisine(Recipe test) {
		Recipe[] nearestNeighbors = new Recipe[k];
		
		int i = 0;
		for (int lastNearestNeighbor = 0; lastNearestNeighbor < k;) {
			if (!test.equalsRecipe(trainingData.get(i))) {
				trainingData.get(i).distance = trainingData.get(i).jaccardDistance(test);
				nearestNeighbors[lastNearestNeighbor] = trainingData.get(i);
				moveRecipeToCorrectLocation(lastNearestNeighbor, nearestNeighbors);
				lastNearestNeighbor++;
			}
			i++;
		}
		
		for (; i < trainingData.size(); i++) {
			if (!test.equalsRecipe(trainingData.get(i))) {
				trainingData.get(i).distance = trainingData.get(i).jaccardDistance(test);
				if (trainingData.get(i).distance < nearestNeighbors[k-1].distance) {
					nearestNeighbors[k-1] = trainingData.get(i);
					moveRecipeToCorrectLocation(k-1, nearestNeighbors);
				}
				
			}
		}
		
		// We now have the k nearest neighbors.
		int[] votes = new int[8];
		
		for (int j = 0; j < k; j++) {
			votes[nearestNeighbors[j].cuisine]++;
		}
		
		int maxVotes = -1;
		int predictedCuisine = -1;
		for (int j = 0; j < 8; j++) {
			if (votes[j] > maxVotes) {
				maxVotes = votes[j];
				predictedCuisine = j;
			}
		}
		return predictedCuisine;
	}
	
	public static void moveRecipeToCorrectLocation(int position, Recipe[] nearestNeighbors) {
		for (int j = position; j > 0; j--) {
			if (nearestNeighbors[j].distance < nearestNeighbors[j-1].distance) {
				Recipe tmp = nearestNeighbors[j-1];
				nearestNeighbors[j-1] = nearestNeighbors[j];
				nearestNeighbors[j] = tmp;
			}
		}
	}
}

