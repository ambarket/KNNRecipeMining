// Theres something wrong here.
public class CrossValidationPredicter {
  
        public static int predictCuisine(int testIndex) {
    	// Store the index of the neighbors in the trainingData, can't use recipe itself 
    	//	and store distance inside recipe because of multithreading.
    	int[] nearestNeighbors = new int[Main.k]; 
    	int i = 0;
    	// Fill up the nearest neighbors first
    	for (int nearestNeighborsSize = 0; nearestNeighborsSize < Main.k;) {
    	    if (testIndex != i) {
	    		nearestNeighbors[nearestNeighborsSize] = i;
	    		moveRecipeToCorrectLocation(nearestNeighborsSize, nearestNeighbors, testIndex);
	    		nearestNeighborsSize++;
    	    }
    	    i++;
    	}

    	// Now go through the rest of the training recipes, keep track of the k
    	// nearest neighbors
    	for (; i < Main.trainingData.size(); i++) {
    	    if (testIndex != i) {
    	    	boolean condition;
    	    	if (testIndex < i) {
    	    		if (testIndex < nearestNeighbors[Main.k - 1]) {
        	    		condition = Main.trainingDataDistanceMatrix[i][testIndex] < Main.trainingDataDistanceMatrix[nearestNeighbors[Main.k - 1]][testIndex];
    	    		}
    	    		else {
        	    		condition = Main.trainingDataDistanceMatrix[i][testIndex] < Main.trainingDataDistanceMatrix[testIndex][nearestNeighbors[Main.k - 1]];
    	    		}

    	    	}
    	    	else {
    	    		if (testIndex < nearestNeighbors[Main.k - 1]) {
        	    		condition = Main.trainingDataDistanceMatrix[testIndex][i] < Main.trainingDataDistanceMatrix[nearestNeighbors[Main.k - 1]][testIndex];
    	    		}
    	    		else {
        	    		condition = Main.trainingDataDistanceMatrix[testIndex][i] < Main.trainingDataDistanceMatrix[testIndex][nearestNeighbors[Main.k - 1]];
    	    		}
    	    	}
	    		if (condition) {
	    		    nearestNeighbors[Main.k - 1] = i;
	    		    moveRecipeToCorrectLocation(Main.k - 1, nearestNeighbors, testIndex);
	    		}
    	    }
    	}

    	// We now have the k nearest neighbors, tally the votes.
    	double[] votes = null;

    	switch (Main.voteWeightFunction) {
    	case DISTANCE:
    	    votes = getVotesWeightedByDistance(nearestNeighbors, testIndex);
    	    break;
    	case ENTROPY:
    	    votes = getVotesWeightedByEntropy(nearestNeighbors);
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
        private static void moveRecipeToCorrectLocation(int positionToSort, int[] nearestNeighbors, int testIndex) {
        	for (int j = positionToSort; j > 0; j--) {
    	    	boolean condition;
    	    	if (testIndex < nearestNeighbors[j]) {
    	    		if (testIndex < nearestNeighbors[j - 1]) {
    	    			condition = Main.trainingDataDistanceMatrix[nearestNeighbors[j]][testIndex] < Main.trainingDataDistanceMatrix[nearestNeighbors[j - 1]][testIndex];
    	    		}
    	    		else {
    	    			condition = Main.trainingDataDistanceMatrix[nearestNeighbors[j]][testIndex] < Main.trainingDataDistanceMatrix[testIndex][nearestNeighbors[j - 1]];
    	    		}
    	    	}
    	    	else {
    	    		if (testIndex < nearestNeighbors[j - 1]) {
    	    			condition = Main.trainingDataDistanceMatrix[testIndex][nearestNeighbors[j]] < Main.trainingDataDistanceMatrix[nearestNeighbors[j - 1]][testIndex];
    	    		}
    	    		else {
    	    			condition = Main.trainingDataDistanceMatrix[testIndex][nearestNeighbors[j]] < Main.trainingDataDistanceMatrix[testIndex][nearestNeighbors[j - 1]];
    	    		}
    	    	}
        	    if (condition) {
        		int tmp = nearestNeighbors[j - 1];
        		nearestNeighbors[j - 1] = nearestNeighbors[j];
        		nearestNeighbors[j] = tmp;
        	    }
        	}
        }
        
        private static double[] getVotesWeightedByDistance(int[] nearestNeighbors, int testIndex) {
        	double[] votes = new double[8]; // 0 is unused, cuisines are numbered 1
        					// through 7.
    
        	float distance;
        	// Weight by distance squared
        	for (int j = 0; j < Main.k; j++) {
        		if (testIndex < nearestNeighbors[j]) {
        			distance = Main.trainingDataDistanceMatrix[nearestNeighbors[j]][testIndex];
        		}
        		else {
        			distance = Main.trainingDataDistanceMatrix[testIndex][nearestNeighbors[j]];
        		}
        	    votes[Main.trainingData.get(nearestNeighbors[j]).cuisine] += 1 / (distance * distance);
        	}
        	return votes;
        }

        private static double[] getVotesWeightedByEntropy(int[] nearestNeighbors) {
        	double[] votes = new double[8]; // 0 is unused, cuisines are numbered 1
        					// through 7.
    
        	for (int j = 0; j < Main.k; j++) {
        	    votes[Main.trainingData.get(nearestNeighbors[j]).cuisine] += 1 / Main.trainingData.get(nearestNeighbors[j]).cuisineEntropy;
        	    // System.out.println(nearestNeighbors[j].cuisineEntropy);
        	}
        	return votes;
        }

}
