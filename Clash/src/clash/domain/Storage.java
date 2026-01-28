package clash.domain;

public class Storage extends BuildingType {
	public int goldStored;
	public int elixirStored;

	public Storage(String name, int level, int buildTime, int maxHealth, int size, int goldCost, int elixirCost, int goldStored, int elixirStored) {
		super(name, level, buildTime, maxHealth, size, goldCost, elixirCost);
		this.goldStored = goldStored;
		this.elixirStored = elixirStored;
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
		String stored;
		if (this.goldStored == 0) {
			stored = "Elixir Stored: " + this.elixirStored;
		}
		else {
			stored = "Gold Stored: " + this.goldStored;
		}
		return "Build Time: " + this.buildTime + "\n"
			 + "Max Health: " + this.maxHealth + "\n"
			 + "Size: " + this.size + "\n"
			 + "Cost: " + cost + "\n"
			 + stored;
	}
}