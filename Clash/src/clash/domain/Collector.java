package clash.domain;

public class Collector extends BuildingType {
	public int goldPerHour;
	public int elixirPerHour;

	public Collector(int id, String name, int level, int buildTime, int maxHealth, int size, int goldCost, int elixirCost, int goldPerHour, int elixirPerHour) {
		super(id, name, level, buildTime, maxHealth, size, goldCost, elixirCost);
		this.goldPerHour = goldPerHour;
		this.elixirPerHour = elixirPerHour;
	}

	public Collector(BuildingType bt, int goldPerHour, int elixirPerHour) {
		super(bt.id, bt.name, bt.level, bt.buildTime, bt.maxHealth, bt.size, bt.goldCost, bt.elixirCost);
		this.goldPerHour = goldPerHour;
		this.elixirPerHour = elixirPerHour;
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
		String perHour;
		if (goldPerHour == 0) {
			perHour = "Elixir Per Hour: " + this.elixirPerHour;
		}
		else {
			perHour = "Gold Per Hour: " + this.goldPerHour;
		}
		return "Build Time: " + this.buildTime + "\n"
			 + "Max Health: " + this.maxHealth + "\n"
			 + "Size: " + this.size + "\n"
			 + "Cost: " + cost + "\n"
			 + perHour;
	}
}