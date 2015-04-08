import java.util.HashMap;
import java.util.HashSet;

class Recipe {
	public double distance; // Only relevant in the context of a particular run of predictCuisine.
	public int cuisine;
	HashSet<String> ingredients;
	
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
	
	public double customDistance01(HashMap<String, HashSet<Integer>> cuisineCounts, Recipe other) {
		HashSet<String> union = new HashSet<String>();
		union.addAll(this.ingredients);
		union.addAll(other.ingredients);
		
		double unionCuisineSum = 0;
		double intersectCuisineSum  = 0;
		for (String ingr : this.ingredients) {
			if (other.ingredients.contains(ingr)) {
				double tmp = cuisineCounts.get(ingr).size() / 8.0/*16.000000001*/;
				intersectCuisineSum += (1 - (tmp));
			}
		}
		
		for (String ingr : union) {
			double tmp = cuisineCounts.get(ingr).size() / 8.0/*16.000000001*/;
			unionCuisineSum += (1 - (tmp));
			//System.out.println(cuisineCounts.get(ingr).size());
		}

		return 1 - intersectCuisineSum / unionCuisineSum;
	}
}