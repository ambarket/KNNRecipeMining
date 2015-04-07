import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;


public class SingleThreaded {
	
	public static void crossValidate(int k, ArrayList<Recipe> trainingData) {
		
		double correct = 0;
		int testNum = 0;
		for (Recipe test : trainingData) {
			//System.out.println("TestNum: " + testNum++);
			int predictedCuisine = Predicter.predictCuisine(k, trainingData, test);
			if (predictedCuisine == test.cuisine) {
				correct++;
			}
		}
		System.out.println(correct / (trainingData.size() - 1));
	}
	
}