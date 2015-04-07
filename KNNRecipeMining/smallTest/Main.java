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
		test();
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

