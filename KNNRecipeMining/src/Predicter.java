import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class Predicter {
	public static int predictCuisine(int k, ArrayList<Recipe> trainingData, HashMap<String, HashSet<Integer>> cuisineCounts, Recipe test) {
		Recipe[] nearestNeighbors = new Recipe[k];
		
		int i = 0;
		for (int lastNearestNeighbor = 0; lastNearestNeighbor < k;) {
			if (!test.equalsRecipe(trainingData.get(i))) {
				//trainingData.get(i).distance = trainingData.get(i).jaccardDistance(test);
				trainingData.get(i).distance = trainingData.get(i).customDistance01(cuisineCounts, test);
				nearestNeighbors[lastNearestNeighbor] = trainingData.get(i);
				moveRecipeToCorrectLocation(lastNearestNeighbor, nearestNeighbors);
				lastNearestNeighbor++;
			}
			i++;
		}
		
		for (; i < trainingData.size(); i++) {
			if (!test.equalsRecipe(trainingData.get(i))) {
				//trainingData.get(i).distance = trainingData.get(i).jaccardDistance(test);
				trainingData.get(i).distance = trainingData.get(i).customDistance01(cuisineCounts, test);
				if (trainingData.get(i).distance < nearestNeighbors[k-1].distance) {
					nearestNeighbors[k-1] = trainingData.get(i);
					moveRecipeToCorrectLocation(k-1, nearestNeighbors);
				}
				
			}
		}
		
		// We now have the k nearest neighbors.
		double[] votes = new double[8];
		
		for (int j = 0; j < k; j++) {
			votes[nearestNeighbors[j].cuisine] += 1 / (nearestNeighbors[j].distance * nearestNeighbors[j].distance);
		}
		
		double maxVotes = -1;
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
