package clash.domain;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class BuildingType {
	public String name;
	public int level;
	public int buildTime;
	public int maxHealth;
	public int size;
	public int goldCost;
	public int elixirCost;
	
	public BuildingType(String name, int level, int buildTime, int maxHealth, int size, int goldCost, int elixirCost) {
		this.name = name;
		this.level = level;
		this.buildTime = buildTime;
		this.maxHealth = maxHealth;
		this.size = size;
		this.goldCost = goldCost;
		this.elixirCost = elixirCost;
	}
	
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
			 + "Cost: " + cost;
	}
	
	public final JLabel getPicture() {
		Image buildingImage;
		JLabel buildingPicture = null;
		try {
			buildingImage = ImageIO.read(new File("src\\clash\\resources\\" + this.name + this.level + ".png")).getScaledInstance(200, 200, 100);
			buildingPicture = new JLabel(new ImageIcon(buildingImage));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return buildingPicture;
	}
}