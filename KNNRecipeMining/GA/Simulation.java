
public class Simulation {
	static MersenneTwisterFast mersenneTwister = new MersenneTwisterFast();
	public static float[] currWeights; // Set by the organism currently eval fitness, 
	Population population;
	public Simulation(int popSize) {
		population = new Population(popSize);	
	}
	
	public float[] runAndReturnBest(int maxIterations) {
		for (int i = 0; i < maxIterations; i++) {
			
			Organism parent1 = population.select();
			Organism parent2 = population.select();
			
			Organism offspring1 = new Organism(population.orgLength);
			Organism offspring2 = new Organism(population.orgLength);
			population.crossOver(4, parent1, parent2, offspring1, offspring2);
			offspring1.setFitness();
			offspring2.setFitness();
			
			Organism better;
			if (offspring1.fitness - offspring2.fitness > 0) {
				better = offspring1;
			}
			else {
				better = offspring2;
			}
			
			better.mutate();
			better.setFitness();

			population.replaceWorst(better);
			population.reportBestAndWorst();
		}
		return population.population[0].chromosome;
	}
}
