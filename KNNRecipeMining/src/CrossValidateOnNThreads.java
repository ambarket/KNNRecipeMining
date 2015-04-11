import java.util.HashSet;

interface CrossValidationRunnableListener {
	void receiveData(int threadNum, int count);
}

class CrossValidateOnNThreads implements CrossValidationRunnableListener {
	int correctPredictions = 0;

	// Note: This method is a blocking call and will not return until all
	// threads have finished.
	public double runAndReturnResult() {
		return runAndReturnResult(Main.trainingData.size());
	}

	// endIndex allows running crossValidation on
	// only the 0 to endIndex, instead of the entire array of training
	// recipes. Each recipe is still predicted using the entire training
	// data set, it will just only try to leave out and predict the recipes
	// from 0 to endIndex. If endIndex is <= 0 OR endIndex > the size of the
	// trainingData set, the entire set will be used
	public double runAndReturnResult(int endIndex) {
		correctPredictions = 0;
		if (endIndex <= 0 || endIndex > Main.trainingData.size()) {
			endIndex = Main.trainingData.size();
		}
		//int numberPerThread = endIndex / Main.numberOfThreads;

		int foldsPerThread =  (int)Math.ceil(Main.numberOfFolds / (double)Main.numberOfThreads);
		int numberPerThread = foldsPerThread * Main.numberPerFold;
		
		Thread[] threads = new Thread[Main.numberOfThreads];

		for (int threadNum = 0; threadNum < Main.numberOfThreads; threadNum++) {
			int threadStartIndex = threadNum * numberPerThread;
			int threadEndIndex = (threadNum + 1) * numberPerThread - 1;
			// Handle any remaining tests in the last thread
			if (threadNum == Main.numberOfThreads-1) { threadEndIndex = endIndex; }
			
			CrossValidationRunnable tmp = new CrossValidationRunnable(
					threadNum, threadStartIndex, threadEndIndex, this);
			threads[threadNum] = new Thread(tmp);
			threads[threadNum].start();
		}

		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// At this point all threads have completed, thus
		// this.correctPredictions will be set to the total number
		// of correctPredictions.
		return ((double) correctPredictions) / endIndex;
	}

	public double runAndReturnResultFromRandomSample(int sampleSize) {
		correctPredictions = 0;
		if (sampleSize <= 0 || sampleSize > Main.trainingData.size()) {
			return 0;
		} else {
			if (sampleSize % Main.numberOfThreads != 0) {
				sampleSize += sampleSize % Main.numberOfThreads;
			}
			assert (sampleSize % Main.numberOfThreads == 0);
			int numberPerThread = sampleSize / Main.numberOfThreads;

			Thread[] threads = new Thread[Main.numberOfThreads];

			int threadStartIndex, threadEndIndex = 0;
			int[] prevStarts = new int[Main.numberOfThreads];
			for (int threadNum = 0; threadNum < Main.numberOfThreads; threadNum++) {
				threadStartIndex = Simulation.mersenneTwister
						.nextInt(Main.trainingData.size() - numberPerThread);
				boolean ok = true;
				int numberOfTrys = 0;
				do {
					if (numberOfTrys < 100) {
						numberOfTrys++;
						for (int i = 0; i < threadNum; i++) {
							ok &= Math.abs(prevStarts[threadNum]
									- threadStartIndex) > numberPerThread;
						}
					} else {
						System.out
								.println("Couldn't get nonoverlapping after 100 tries, just going with it");
						break;
					}
				} while (!ok );
				threadEndIndex = threadStartIndex + numberPerThread;
				// System.out.println("ThreadStart: " + threadStartIndex +
				// " ThreadEnd: " + threadEndIndex );
				CrossValidationRunnable tmp = new CrossValidationRunnable(
						threadNum, threadStartIndex, threadEndIndex, this);
				threads[threadNum] = new Thread(tmp);
				threads[threadNum].start();
			}

			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// At this point all threads have completed, thus
			// this.correctPredictions will be set to the total number
			// of correctPredictions.
			return ((double) correctPredictions) / sampleSize;
		}
	}

	public synchronized void receiveData(int threadNum, int correctCount) {
		//System.out.println("Received data from thread #" + threadNum + " " + correctCount + " correct");
		correctPredictions += correctCount;
		//System.out.println("Total so far" + correctPredictions);
	}
}

class CrossValidationRunnable implements Runnable {
	int start, end, threadNum, correct = 0;
	CrossValidationRunnableListener listener;

	public CrossValidationRunnable(int threadNum, int start, int end,
			CrossValidationRunnableListener listener) {
		this.threadNum = threadNum;
		this.start = start;
		this.end = end;
		this.listener = listener;
	}

	@Override
	public void run() {
		double[] distanceSpaceForThisThread = new double[Main.trainingData.size()];
		long startTime, endTime, seconds;
		startTime = System.currentTimeMillis();
		
		for (int i = start; i < end; i += Main.numberPerFold) {
			HashSet<Recipe> tests = new HashSet<Recipe>();
			for (int j = i; j < end && j < i + Main.numberPerFold; j++) {
				tests.add(Main.trainingData.get(j));
			}
			correct += Predicter.predictCuisines(tests, distanceSpaceForThisThread);

			if ((i - start + 1) % 500 == 0) {
				endTime = System.currentTimeMillis();
				seconds = (endTime - startTime) / 1000;
				// minutes = (endTime - startTime) / 1000 / 60;
				System.out.println("Thread: " + threadNum + " found " + correct
						+ " out of " + (i - start) + " so far" + " in "
						+ seconds + " seconds, Accuracy: " + (double) correct
						/ (i - start));
			}
		}
		listener.receiveData(threadNum, correct);
	}
}