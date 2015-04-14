import java.util.HashMap;
import java.util.HashSet;

class Recipe {
	public int cuisine;
	HashSet<String> ingredients;
	public double cuisineEntropy;
	public int recipeNum;
	public Recipe(boolean training, String line, int recipeNum) {
		this.recipeNum = recipeNum;
		ingredients = new HashSet<String>();
		String[] lineArray = line.split(" ");
		if (training) {
			cuisine = Integer.parseInt(lineArray[0]);
		}
		else {
			cuisine = -1; 
		}
		
		for (int i = 1; i < lineArray.length; i++) {
			if (!(Main.tabooList.contains(lineArray[i]))) {
				ingredients.add(lineArray[i]);
			}
		}		
	}
	
	public void setEntropy() {
		cuisineEntropy = 0;
		for (String ingr :ingredients) {
			double totalOccurances = 0;
			for (int i = 1; i < 8; i++) {
				totalOccurances += Main.cuisineCounts.get(ingr)[i];
			}
			double sumOfProb = 0;
			for (int i = 1; i < 8; i++) {
				double prob = Main.cuisineCounts.get(ingr)[i] / totalOccurances;
				if (prob != 0) {
					sumOfProb += prob * ((prob != 0) ? Math.log(prob) : 0);
				}
				cuisineEntropy += prob * ((prob != 0) ? Math.log(prob) : 0);
			}
			//System.out.println("Sum of prob: " + sumOfProb);

		}
		cuisineEntropy *= -1; 
		cuisineEntropy /= ingredients.size();
	}
	
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		Recipe other = (Recipe)obj;
		boolean equal = true;
		equal &= cuisine != -1 && other.cuisine != -1; // They are both from the training set
		if (equal) equal &= cuisine == other.cuisine;	
		if (equal) equal &= ingredients.size() == other.ingredients.size();
		if (equal) ingredients.equals(other.ingredients);
		
		return equal;
	}
	

	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + cuisine;
		hash = 31 * hash + ingredients.hashCode();
		hash = 31 * hash + recipeNum;
		return hash;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(cuisine + " ");
		for (String s : ingredients) {
			sb.append(s + " ");
		}
		return sb.toString();
	}
	
	// One public distance function used in the rest of the code, add / comment out the return statements as needed.
	public float getDistance(Recipe other) {
	  switch (Main.distanceFunction) {
	    case JACCARD: 
	      return jaccardDistance(other);
	    case CUSTOM01:
	      return customDistance01(other);
	    case CUSTOM02:
	      return customDistance02(other);
	    case GA_JACCARD:
	    	return jaccardWithGAWeights(other);
	    case CUISINE_PROB_JACCARD:
	    	return jaccardWithCusineIngrWeights(other);
	    default:
	      System.out.println("ERROR: Invalid distance function selection.");
	      return Float.MAX_VALUE;
	  }
	}
	
	private float jaccardDistance(Recipe other) {
		HashSet<String> union = new HashSet<String>();
		union.addAll(this.ingredients);
		union.addAll(other.ingredients);
		float unionSize = union.size();
		
		float intersectSize = 0;
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				intersectSize++;
			}
		}
		
		
		return 1 - intersectSize / unionSize;
	}
	
	private float customDistance01(Recipe other) {
		HashSet<String> union = new HashSet<String>();
		union.addAll(this.ingredients);
		union.addAll(other.ingredients);
		
		float unionCuisineSum = 0;
		float intersectCuisineSum  = 0;
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				int numOfCuisines = 0;
				for (int i = 1; i < 8; i++) {
					if (Main.cuisineCounts.get(ingr)[i] > 0) {
						numOfCuisines++;
					}
				}
				double tmp = numOfCuisines / 8.0/*16.000000001*/;
				intersectCuisineSum += (1 - (tmp));
			}
		}
		
		for (String ingr : union) {
			int numOfCuisines = 0;
			for (int i = 1; i < 8; i++) {
				if (Main.cuisineCounts.get(ingr)[i] > 0) {
					numOfCuisines++;
				}
			}
			double tmp = numOfCuisines / 8.0/*16.000000001*/;
			unionCuisineSum += (1 - (tmp));
			//System.out.println(cuisineCounts.get(ingr).size());
		}

		return 1 - intersectCuisineSum / unionCuisineSum;
	}
	// This is terrible ~.60%
	private float customDistance02(Recipe other) {
	    	float intersectCuisineSum  = 0;
		double intersectSize  = 0;
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				intersectSize++;
				double totalOccurances = 0;
				for (int i = 1; i < 8; i++) {
					totalOccurances += Main.cuisineCounts.get(ingr)[i];
				}
				double sumOfProb = 0;
				for (int i = 1; i < 8; i++) {
					double prob = Main.cuisineCounts.get(ingr)[i] / totalOccurances;
					if (prob != 0) {
						sumOfProb += prob * ((prob != 0) ? Math.log(prob) : 0);
					}
					intersectCuisineSum += prob * ((prob != 0) ? Math.log(prob) : 0);
				}
				//System.out.println("Sum of prob: " + sumOfProb);
			}
		}
		intersectCuisineSum *= -1; 
		if (intersectSize != 0) {
			intersectCuisineSum /=  intersectSize;
		}
		else {
			intersectCuisineSum = 10000000;
		}
		//System.out.println("Entropy Distance: " + intersectCuisineSum);
		return intersectCuisineSum;
	}
	
	private float jaccardWithCusineIngrWeights(Recipe other) {
		
		HashSet<String> union = new HashSet<String>();
		union.addAll(this.ingredients);
		union.addAll(other.ingredients);
		float unionSize = 0;
		float intersectSize = 0;
		
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				intersectSize+= Main.cusisineIngrWeights[Main.uniqueIngredients.get(ingr)];
			}
		}
		
		for (String ingr : union) {
			unionSize += Main.cusisineIngrWeights[Main.uniqueIngredients.get(ingr)];
		}
		
		return 1 - intersectSize / unionSize;
	}
	
	private float jaccardWithGAWeights(Recipe other) {
		HashSet<String> union = new HashSet<String>();
		union.addAll(this.ingredients);
		union.addAll(other.ingredients);
		float unionSize = 0;
		float intersectSize = 0;
		
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				intersectSize+= Simulation.currWeights[Main.uniqueIngredients.get(ingr)];
			}
		}
		
		for (String ingr : union) {
			unionSize += Simulation.currWeights[Main.uniqueIngredients.get(ingr)];
		}
		
		return 1 - intersectSize / unionSize;
	}
}
