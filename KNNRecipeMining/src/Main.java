import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;


public class Main {
	static ArrayList<Recipe> trainingData;
	static HashMap<String, HashSet<Integer>> cuisineCounts;
	static int k, numberOfThreads;
	
	public static void main(String[] args) {
		k = 10;
		numberOfThreads = 4;
		
		trainingData = readTrainingFile();
		//cuisineCounts = getCuisineCounts(trainingData);
		
		CrossValidateOnNThreads c = new CrossValidateOnNThreads(trainingData, k , numberOfThreads);
		c.runAllThreads();
		
		//SingleThreaded.crossValidate(k, trainingData);
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
	
	public static void getCuisineCounts(ArrayList<Recipe> trainingData) {
		//HashMap<String, HashSet<Integer>> 
	}
	public static void test()  {
		Scanner sc = new Scanner(System.in);
		Recipe test;
		while (sc.hasNextLine()) {
			test = new Recipe(false, sc.nextLine());
			System.out.println(Predicter.predictCuisine(k, trainingData, test));
		}
	}
}
