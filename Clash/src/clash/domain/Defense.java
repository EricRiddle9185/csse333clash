package clash.domain;

public class Defense extends BuildingType {
	public int damage;
	public int attackRate;
	public String damageType;
	public String target;
	
	public Defense(int id, String name, int level, int buildTime, int maxHealth, int size, int goldCost, int elixirCost, int damage, int attackRate, String damageType, String target) {
		super(id, name, level, buildTime, maxHealth, size, goldCost, elixirCost);
		this.damage = damage;
		this.attackRate = attackRate;
		this.damageType = damageType;
		this.target = target;
	}
	
	public Defense(BuildingType bt, int damage, int attackRate, String damageType, String target) {
		super(bt.id, bt.name, bt.level, bt.buildTime, bt.maxHealth, bt.size, bt.goldCost, bt.elixirCost);
		this.damage = damage;
		this.attackRate = attackRate;
		this.damageType = damageType;
		this.target = target;
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
			 + "Damage: " + this.damage + "\n"
			 + "Attack Rate: " + this.attackRate + "\n"
			 + "Damage Type: " + this.damageType + "\n"
			 + "Target: " + this.target;
	}
}