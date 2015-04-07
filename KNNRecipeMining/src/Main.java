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
		
		long startTime, endTime, duration;
		startTime = System.nanoTime();
		
		CrossValidateOnNThreads c = new CrossValidateOnNThreads(trainingData, 50, 4);
		c.runAllThreads();
		
		//SingleThreaded.crossValidate(k, trainingData);
		
		endTime = System.nanoTime();
		duration = (endTime - startTime) / 1000000 / 1000;
		System.out.println(duration);
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
	
	public static void test()  {
		Scanner sc = new Scanner(System.in);
		Recipe test;
		while (sc.hasNextLine()) {
			test = new Recipe(false, sc.nextLine());
			System.out.println(Predicter.predictCuisine(k, trainingData, test));
		}
	}
}
