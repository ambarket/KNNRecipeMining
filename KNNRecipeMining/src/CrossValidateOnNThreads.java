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
	if (endIndex <= 0 || endIndex > Main.trainingData.size()) {
	    endIndex = Main.trainingData.size();
	}
	int numberPerThread = endIndex / Main.numberOfThreads;

	Thread[] threads = new Thread[Main.numberOfThreads];

	for (int threadNum = 0; threadNum < Main.numberOfThreads; threadNum++) {
	    int threadStartIndex = threadNum * numberPerThread;
	    int threadEndIndex = (threadNum + 1) * numberPerThread - 1;
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
	return ((double) correctPredictions) / Main.trainingData.size();
    }

    public synchronized void receiveData(int threadNum, int correctCount) {
	System.out.println("Received data from thread #" + threadNum);
	correctPredictions += correctCount;
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
	for (int i = start; i < end; i++) {

	    Recipe test = Main.trainingData.get(i);
	    int predictedCuisine = Predicter.predictCuisine(test, distanceSpaceForThisThread);
	    if (predictedCuisine == test.cuisine) {
		correct++;
	    }
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