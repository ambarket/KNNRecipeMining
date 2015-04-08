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
	static int k = 10;
	static int numberOfThreads = 4;
	
	public static void main(String[] args) {
		readTrainingFile();
		getCuisineCounts();
		
		CrossValidateOnNThreads c = new CrossValidateOnNThreads(trainingData, cuisineCounts, k , numberOfThreads);
		c.runAllThreads();
		
		//SingleThreaded.crossValidate(k, trainingData);
	}
	
	public static void readTrainingFile() {
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
	
	public static void getCuisineCounts() {
		cuisineCounts = new HashMap<String, HashSet<Integer>>();
		for (Recipe r : trainingData) {
			for (String ingr : r.ingredients) {
				if (!cuisineCounts.containsKey(ingr)) {
					cuisineCounts.put(ingr, new HashSet<Integer>());
				}
				cuisineCounts.get(ingr).add(r.cuisine);
			}
		}
	}
	
	public static void test()  {
		Scanner sc = new Scanner(System.in);
		Recipe test;
		while (sc.hasNextLine()) {
			test = new Recipe(false, sc.nextLine());
			System.out.println(Predicter.predictCuisine(k, trainingData, cuisineCounts, test));
		}
	}
}
