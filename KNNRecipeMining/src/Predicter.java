import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Predicter {
    
    public static int predictCuisine(Recipe test) {
	// Store the index of the neighbors in the trainingData, can't use recipe itself 
	//	and store distance inside recipe because of multithreading.
	int[] nearestNeighbors = new int[Main.k]; 
	double[] distances = new double[Main.trainingData.size()];
	int i = 0;
	// Fill up the nearest neighbors first
	for (int nearestNeighborsSize = 0; nearestNeighborsSize < Main.k;) {
	    if (!test.equalsRecipe(Main.trainingData.get(i))) {
		distances[i] = Main.trainingData.get(i).getDistance(test);
		nearestNeighbors[nearestNeighborsSize] = i;
		moveRecipeToCorrectLocation(nearestNeighborsSize,
			nearestNeighbors, distances);
		nearestNeighborsSize++;
	    }
	    i++;
	}

	// Now go through the rest of the training recipes, keep track of the k
	// nearest neighbors
	for (; i < Main.trainingData.size(); i++) {
	    if (!test.equalsRecipe(Main.trainingData.get(i))) {
		distances[i] = Main.trainingData.get(i).getDistance(test);
		if (distances[i] < distances[nearestNeighbors[Main.k - 1]]) {
		    nearestNeighbors[Main.k - 1] = i;
		    moveRecipeToCorrectLocation(Main.k - 1, nearestNeighbors, distances);
		}
	    }
	}

	// We now have the k nearest neighbors, tally the votes.
	double[] votes = null;

	switch (Main.voteWeightFunction) {
	case DISTANCE:
	    votes = getVotesWeightedByDistance(nearestNeighbors, distances);
	    break;
	case ENTROPY:
	    votes = getVotesWeightedByEntropy(nearestNeighbors, distances);
	    break;
	default:
	    System.out.println("ERROR: Invalid vote weight function selection");
	    break;
	}

	double maxVotes = -1;
	int predictedCuisine = -1;
	for (int j = 1; j < 8; j++) {
	    if (votes[j] > maxVotes) {
		maxVotes = votes[j];
		predictedCuisine = j;
	    }
	}
	return predictedCuisine;
    }
    
	
    // Keep in ascending order by distance
    private static void moveRecipeToCorrectLocation(int positionToSort, int[] nearestNeighbors, double[] distances) {
	for (int j = positionToSort; j > 0; j--) {
	    if (distances[nearestNeighbors[j]] < distances[nearestNeighbors[j - 1]]) {
		int tmp = nearestNeighbors[j - 1];
		nearestNeighbors[j - 1] = nearestNeighbors[j];
		nearestNeighbors[j] = tmp;
	    }
	}
    }
    
    

    private static double[] getVotesWeightedByDistance(int[] nearestNeighbors, double[] distances) {
	double[] votes = new double[8]; // 0 is unused, cuisines are numbered 1
					// through 7.

	// Weight by distance squared
	for (int j = 0; j < Main.k; j++) {
	    votes[Main.trainingData.get(nearestNeighbors[j]).cuisine] += 1 / (distances[nearestNeighbors[j]] * distances[nearestNeighbors[j]]);
	}
	return votes;
    }

    private static double[] getVotesWeightedByEntropy(int[] nearestNeighbors, double[] distances) {
	double[] votes = new double[8]; // 0 is unused, cuisines are numbered 1
					// through 7.

	for (int j = 0; j < Main.k; j++) {
	    votes[Main.trainingData.get(nearestNeighbors[j]).cuisine] += 1 / Main.trainingData.get(nearestNeighbors[j]).cuisineEntropy;
	    // System.out.println(nearestNeighbors[j].cuisineEntropy);
	}
	return votes;
    }
}
