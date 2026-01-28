package clash.domain;

public class Camp extends BuildingType {
	public int troopsStored;

	public Camp(String name, int level, int buildTime, int maxHealth, int size, int goldCost, int elixirCost, int troopsStored) {
		super(name, level, buildTime, maxHealth, size, goldCost, elixirCost);
		this.troopsStored = troopsStored;
	}
	
	@Override
	public String getBuildingInfo() {
		String cost;
		if (this.goldCost == 0) {
			cost = this.elixirCost + " Elixir";
		}
		else {
			cost = this.goldCost + " Gold";
		}
		return "Build Time: " + this.buildTime + "\n"
			 + "Max Health: " + this.maxHealth + "\n"
			 + "Size: " + this.size + "\n"
			 + "Cost: " + cost + "\n"
			 + "Troops Stored: " + this.troopsStored;
	}
}