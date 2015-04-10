
public class Population {
	Organism[] population;
	int orgLength = Main.cuisineCounts.size();
	double totalFitness = 0;
	int popSize;
	public Population(int popSize) {
		this.popSize = popSize;
		population = new Organism[popSize];
		for (int i = 0; i < popSize; i++) {
			population[i] = new Organism(orgLength);
			population[i].setFitness();
			totalFitness += population[i].fitness;
			System.out.println("Created org " + i);
		}
		sortPopulationByFitness();
	}
	
	public void sortPopulationByFitness() {
		int i, j;
		Organism tmp;
		for (i = popSize - 1; i > 1; i--) {
			for (j = 0; j < i; j++) {
				if (population[j].fitness < population[j + 1].fitness) {
					tmp = population[j];
					population[j] = population[j + 1];
					population[j + 1] = tmp;
				}
			}
		}
	}
	
	public void moveOrganismToSortedPosition(int indexToSort) {
		Organism tmp;
		int i = indexToSort;
		// Move the child left while its fitness is greater than it's left neighbor
		while ((i > 0) && (population[i].fitness > population[i - 1].fitness)) {
			tmp = population[i];
			population[i] = population[i - 1];
			population[i - 1] = tmp;
			i--;
		}
	}

	
	public int[] getCutPoints(int numCutPoints)
	{//select numCutPoints randomly in the range [0..orgLength-1]
	 //and store their locations in the cutPoints array
	
	  int m = 0;   // the number of points selected so far
	  int i=0;  //index for cutPoints array
	  int[] cutPoints = new int[numCutPoints];
	  
	  for (int t = 0; (t < orgLength) && (m < numCutPoints); t++){
	    if (((orgLength - t) * Simulation.mersenneTwister.nextFloat(true, true)) < (numCutPoints - m)){
	      cutPoints[i] = t;  // we use t instead of t+1, since this will eventually be referring to the index of chromosome
	      i++;
	      m++;
	    }
	  }//for
	  return cutPoints;
	 }//selectCutPoints
	
	public Organism select()
	{//selects a member of the population using proportional selection scheme.
		 //If totalFitness is zero then an organism is selected at random.
		  double toss;
		  int i = 0;
		  float sum;

		  if (totalFitness == 0){
		    i = Simulation.mersenneTwister.nextInt(popSize);
		  }
		  else{
		    sum  = population[0].fitness;
		    toss = Simulation.mersenneTwister.nextFloat(true, true) * totalFitness;
		    while (sum < toss){
		      i++;
		      sum += population[i].fitness;
		    }//while
		  }//else

		  return population[i];
	}//select
	
	public void replaceWorst(Organism replacement)
	{//selects a member of the population using proportional selection scheme.
		 //If totalFitness is zero then an organism is selected at random.
		if (population[popSize-1].fitness < replacement.fitness) {
			population[popSize-1] = replacement;
			moveOrganismToSortedPosition(popSize -1);
		}
	}//select


	public void crossOver(int numCutPoints, Organism parent1, Organism parent2, Organism offspring1, Organism offspring2)
	{// multipoint crossover operator
	 // take alternate segments from each parent, based on the cutpoints, to form an offspring
	 // Each segment consists of all characters from either the beginning or right after
	 // the last cut point up to and including the next cut point.

	  int[] cutPoints = getCutPoints(numCutPoints);
	  boolean child1 = true;
	  int current = 0;  //the overall finger through all chromosomes (parents & offspring)
	
	  for (int i = 0; i < numCutPoints; i++){
	    if (child1){
	      for (int j=current; j <= cutPoints[i]; j++){
		offspring1.chromosome[j] = parent1.chromosome[j];
		offspring2.chromosome[j] = parent2.chromosome[j];
	      }
	    }//if
	    else{
	      for (int j=current; j <= cutPoints[i]; j++){
		offspring1.chromosome[j] = parent2.chromosome[j];
		offspring2.chromosome[j] = parent1.chromosome[j];
	      }
	    }
	    current = cutPoints[i] + 1;
	    child1 = !child1;
	  }//endfor i
	
	  //now take care of the last segments, if any
	     if (child1){
	      for (int j=current; j < parent1.length; j++){
		offspring1.chromosome[j] = parent1.chromosome[j];
		offspring2.chromosome[j] = parent2.chromosome[j];
	      }//for
	    }//if
	    else{
	      for (int j=current; j < parent1.length; j++){
			offspring1.chromosome[j] = parent2.chromosome[j];
			offspring2.chromosome[j] = parent1.chromosome[j];
	      }//for
	    }//else
	
	}//crossOver
	
	public void reportBestAndWorst() {
		System.out.println("Best fitness: " + population[0].fitness);
		System.out.println("Worst fitness: " + population[popSize-1].fitness);
	}
}
