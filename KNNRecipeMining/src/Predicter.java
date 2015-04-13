import java.util.HashSet;

public class Predicter {
	// Return number correct
	public static int predictCuisines(HashSet<Recipe> tests, double[] distanceSpaceForThisThread) {
		long startTime, endTime, seconds;
		startTime = System.currentTimeMillis();
		int testNumber = 1, correctPredictions = 0;
		
		int[] nearestNeighbors = new int[Main.k];
		double[] distances = distanceSpaceForThisThread; 

		for (Recipe test : tests) {
			int i = 0;
			// Fill up the nearest neighbors first
			
			for (int nearestNeighborsSize = 0; nearestNeighborsSize < Main.k;) {
				if (!tests.contains(Main.trainingData.get(i))) {
					distances[i] = Main.trainingData.get(i).getDistance(test);
					nearestNeighbors[nearestNeighborsSize] = i;
					moveRecipeToCorrectLocation(nearestNeighborsSize, nearestNeighbors, distances);
					nearestNeighborsSize++;
				} else {
					//System.out.println("Were not overfitting" );
				}
				i++;
			}
	
			// Now go through the rest of the training recipes, keep track of the k
			// nearest neighbors
			for (; i < Main.trainingData.size(); i++) {
				if (!tests.contains(Main.trainingData.get(i))) {
					distances[i] = Main.trainingData.get(i).getDistance(test);
					if (distances[i] < distances[nearestNeighbors[Main.k - 1]]) {
						nearestNeighbors[Main.k - 1] = i;
						moveRecipeToCorrectLocation(Main.k - 1, nearestNeighbors,
								distances);
					}
				} else {
					//System.out.println("Were not overfitting");
				}
			}
			int prediction = collectVotesAndMakePrediction(nearestNeighbors, distances);
			if (prediction == test.cuisine) {
				correctPredictions++;
			}
			//System.out.println("Prediction done " + test.recipeNum);
			
			if (testNumber % 500 == 0) {
				endTime = System.currentTimeMillis();
				seconds = (endTime - startTime) / 1000;
				// minutes = (endTime - startTime) / 1000 / 60;
				System.out.println("One of the threads found " + correctPredictions
						+ " out of " + testNumber + " so far" + " in "
						+ seconds + " seconds, Accuracy: " + (double) correctPredictions
						/ testNumber);
			}
			testNumber++;
		}
		//System.out.println("Prediction of fold is complete, fold size:" +  tests.size());
		return correctPredictions;
	}
	
	private static int collectVotesAndMakePrediction(int[] nearestNeighbors, double[] distances) {
		// We now have the k nearest neighbors, tally the votes.
		double[] votes = null;

		switch (Main.voteWeightFunction) {
		case DISTANCE:
			votes = getVotesWeightedByDistance(nearestNeighbors, distances);
			break;
		case ENTROPY:
			votes = getVotesWeightedByEntropy(nearestNeighbors, distances);
			break;
		case NONE:
			votes = getNonWeightedVotes(nearestNeighbors);
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
	private static void moveRecipeToCorrectLocation(int positionToSort,
			int[] nearestNeighbors, double[] distances) {
		for (int j = positionToSort; j > 0; j--) {
			if (distances[nearestNeighbors[j]] < distances[nearestNeighbors[j - 1]]) {
				int tmp = nearestNeighbors[j - 1];
				nearestNeighbors[j - 1] = nearestNeighbors[j];
				nearestNeighbors[j] = tmp;
			}
		}
	}
	
	private static double[] getNonWeightedVotes(int[] nearestNeighbors) {
		double[] votes = new double[8]; // 0 is unused, cuisines are numbered 1
		// through 7.

		// Weight by distance squared
		for (int j = 0; j < Main.k; j++) {
			votes[Main.trainingData.get(nearestNeighbors[j]).cuisine] += 1;
		}
		return votes;
	}

	private static double[] getVotesWeightedByDistance(int[] nearestNeighbors,
			double[] distances) {
		double[] votes = new double[8]; // 0 is unused, cuisines are numbered 1
		// through 7.

		// Weight by distance squared
		for (int j = 0; j < Main.k; j++) {
			votes[Main.trainingData.get(nearestNeighbors[j]).cuisine] += 1 / (distances[nearestNeighbors[j]] * distances[nearestNeighbors[j]]);
		}
		return votes;
	}

	private static double[] getVotesWeightedByEntropy(int[] nearestNeighbors,
			double[] distances) {
		double[] votes = new double[8]; // 0 is unused, cuisines are numbered 1
		// through 7.

		for (int j = 0; j < Main.k; j++) {
			votes[Main.trainingData.get(nearestNeighbors[j]).cuisine] += 1 / Main.trainingData
					.get(nearestNeighbors[j]).cuisineEntropy;
			// System.out.println(nearestNeighbors[j].cuisineEntropy);
		}
		return votes;
	}
}
