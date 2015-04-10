
public class Organism {
	float fitness = 0;
	int length;
	float[] chromosome;
	int sampleSize = 1000;
	
	public Organism(int length) {
		this.length = length;
		chromosome = new float[length];
		createRandomChromosome();
	}
	
	public void setFitness() {
		Simulation.currWeights = chromosome;
		CrossValidateOnNThreads crossValidator = new CrossValidateOnNThreads();
		long startTime, endTime, seconds;
		startTime = System.currentTimeMillis();
		
		fitness = 0;
		for (int i = 0; i < 5; i++) {
			fitness += (float)crossValidator.runAndReturnResultFromRandomSample(sampleSize);
		}
		fitness /= 5.0;

		if (fitness >= .905) {
			System.out.println("ABOVE .905 Acc: " + fitness);
			fitness = (float)crossValidator.runAndReturnResult(); // Try the whole thing.
			Main.writeWeightsToFile(chromosome, fitness);
		}
		
		System.out.println("Accuracy: " + fitness);
		endTime = System.currentTimeMillis();
		seconds = (endTime - startTime) / 1000;
		System.out.println("Eval fitness in " + seconds + " seconds");
	}
	
	public void createRandomChromosome() {
		for (int i = 0; i < Main.uniqueIngredients.size(); i++) {
			chromosome[i] = Simulation.mersenneTwister.nextFloat(true, true);
		}
	}
	
	public void mutate() {
		float mutProb = 1.0f - fitness / 25;
		for (int i = 0; i < length; i++) {
			float toss = Simulation.mersenneTwister.nextFloat(true, true);
			if (toss < mutProb) {
				chromosome[i] = Simulation.mersenneTwister.nextFloat(true, true);
			}
		}
	}
}
