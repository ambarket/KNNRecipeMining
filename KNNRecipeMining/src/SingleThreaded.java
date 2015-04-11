
public class SingleThreaded {
	/*
	public static void crossValidate() {
	    	double[] distanceSpaceForThisThread = new double[Main.trainingData.size()];
		double correct = 0;
		int i = 0;
		long startTime, endTime, seconds;
		startTime = System.currentTimeMillis();
		for (Recipe test : Main.trainingData) {
			int predictedCuisine = Predicter.predictCuisine(test, distanceSpaceForThisThread);
			if (predictedCuisine == test.cuisine) {
				correct++;
			}
			    if ((i + 1) % 500 == 0) {
				endTime = System.currentTimeMillis();
				seconds = (endTime - startTime) / 1000;
				// minutes = (endTime - startTime) / 1000 / 60;
				System.out.println("Thread: " + 1 + " found " + correct
					+ " out of " + (i) + " so far" + " in "
					+ seconds + " seconds, Accuracy: " + (double) correct
					/ (i));
			    }
			    i++;
		}
		System.out.println(correct / (Main.trainingData.size() - 1));
	}
	*/
	
}
