import java.util.HashMap;
import java.util.HashSet;

class Recipe {
	public double distance; // Only relevant in the context of a particular run of predictCuisine.
	public int cuisine;
	HashSet<String> ingredients;
	static HashMap<String, int[]> cuisineCounts;
	public double cuisineEntropy;
	
	public Recipe(boolean training, String line) {
		ingredients = new HashSet<String>();
		String[] lineArray = line.split(" ");
		if (training) {
			cuisine = Integer.parseInt(lineArray[0]);
		}
		else {
			cuisine = -1; 
		}
		
		for (int i = 1; i < lineArray.length; i++) {
			ingredients.add(lineArray[i]);
		}		
	}
	
	public void setEntropy() {
		cuisineEntropy = 0;
		for (String ingr :ingredients) {
			double totalOccurances = 0;
			for (int i = 1; i < 8; i++) {
				totalOccurances += cuisineCounts.get(ingr)[i];
			}
			double sumOfProb = 0;
			for (int i = 1; i < 8; i++) {
				double prob = cuisineCounts.get(ingr)[i] / totalOccurances;
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
	
	public boolean equalsRecipe(Recipe other) {
		return cuisine != -1 && other.cuisine != -1 && ingredients.size() == other.ingredients.size() && ingredients.containsAll(other.ingredients);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(cuisine + " ");
		for (String s : ingredients) {
			sb.append(s + " ");
		}
		return sb.toString();
	}
	
	public double jaccardDistance(Recipe other) {
		HashSet<String> union = new HashSet<String>();
		union.addAll(this.ingredients);
		union.addAll(other.ingredients);
		double unionSize = union.size();
		
		double intersectSize = 0;
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				intersectSize++;
			}
		}
		
		return 1 - intersectSize / unionSize;
	}
	
	public double customDistance01(Recipe other) {
		HashSet<String> union = new HashSet<String>();
		union.addAll(this.ingredients);
		union.addAll(other.ingredients);
		
		double unionCuisineSum = 0;
		double intersectCuisineSum  = 0;
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				int numOfCuisines = 0;
				for (int i = 1; i < 8; i++) {
					if (cuisineCounts.get(ingr)[i] > 0) {
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
				if (cuisineCounts.get(ingr)[i] > 0) {
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
	public double customDistance02(Recipe other) {
		double intersectCuisineSum  = 0;
		double intersectSize  = 0;
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				intersectSize++;
				double totalOccurances = 0;
				for (int i = 1; i < 8; i++) {
					totalOccurances += cuisineCounts.get(ingr)[i];
				}
				double sumOfProb = 0;
				for (int i = 1; i < 8; i++) {
					double prob = cuisineCounts.get(ingr)[i] / totalOccurances;
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
}