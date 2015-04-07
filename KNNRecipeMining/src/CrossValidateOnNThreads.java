import java.util.ArrayList;


interface GetDataFromThread {
	void receiveData(int threadNum, int count);
}

class CrossValidateOnNThreads implements GetDataFromThread {
	boolean[] completedThreads;
	
	ArrayList<Recipe> trainingData;
	int k, numberOfThreads;
	
	double correctPredictions = 0;
	
	public CrossValidateOnNThreads(ArrayList<Recipe> trainingData, int k, int numberOfThreads) {
		this.trainingData = trainingData;
		this.k = k;
		this.numberOfThreads = numberOfThreads;
		completedThreads = new boolean[numberOfThreads];
	}
	
	public void runAllThreads() {
		int numberPerThread = trainingData.size() / numberOfThreads;
		for (int i = 0; i < numberOfThreads; i++) {
			RunSomeOfTheTests tmp = new RunSomeOfTheTests(i * numberPerThread, ((i+1) * numberPerThread - 1), trainingData, i, k, this);
			new Thread(tmp).start();
		}
	}
	
	public synchronized void receiveData(int threadNum, int count) {
		completedThreads[threadNum] = true;
		correctPredictions += count;
		
		boolean done = true;
		for (int i = 0; i < numberOfThreads; i++) {
			done = done && completedThreads[i];
		}
		
		if (done) {
			System.out.println("Accuracy: " + correctPredictions / trainingData.size());
		}
	}
}

class RunSomeOfTheTests implements Runnable {
	int start, end, threadNum, k, correct = 0;
	GetDataFromThread callback;
	ArrayList<Recipe> trainingData;
	public RunSomeOfTheTests(int start, int end, ArrayList<Recipe> trainingData, int threadNum, int k, GetDataFromThread callback) {
		this.start = start;
		this.end = end;
		this.k = k;
		this.threadNum = threadNum;
		this.trainingData = trainingData;
		this.callback = callback;
	}
	@Override
	public void run() {
		for (int i = start; i < end; i++) {
			long startTime, endTime, minutes, seconds;
			startTime = System.currentTimeMillis();
			Recipe test = trainingData.get(i);
			int predictedCuisine = Predicter.predictCuisine(k, trainingData, test);
			if (predictedCuisine == test.cuisine) {
				correct++;
				//System.out.println("i = " + i + " on thread: " + threadNum + " Correct");
			}
			else {
				//System.out.println("i = " + i + " on thread: " + threadNum + " incorrect");
			}
			if ((i - start) % 1000 == 0) {
				endTime = System.currentTimeMillis();
				seconds = (endTime - startTime) / 1000;
				//minutes = (endTime - startTime) / 1000 / 60;
				System.out.println("Thread: " + threadNum + " found " + correct + " out of " + (i - start) + " so far" + " in " + seconds);
				startTime = System.currentTimeMillis();
			}
		}
		callback.receiveData(threadNum, correct);
	}
	
}
