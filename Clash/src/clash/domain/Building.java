package clash.domain;

import java.awt.BorderLayout;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import clash.gui.GUI;

public class Building {
	public BuildingType buildingType;
	public Calendar creationTime;
	public int posX;
	public int posY;
	// dont need to track player here, assumed correct

	public Building(BuildingType buildingType, Calendar creationTime, int posX, int posY) {
		this.buildingType = buildingType;
		this.creationTime = creationTime;
		this.posX = posX;
		this.posY = posY;
	}
	
	public final JButton getButton() {
		// init
		Image buildingImage;
		JButton button = new JButton();
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setContentAreaFilled(false);
		// image
		try {
			buildingImage = ImageIO.read(new File("src\\clash\\resources\\" + this.buildingType.name + this.buildingType.level + ".png"))
					.getScaledInstance(32 * this.buildingType.size, 32 * this.buildingType.size, 100);
			button.setIcon(new ImageIcon(buildingImage));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// popup window when clicked
		button.addActionListener((ActionEvent e) -> {
			// INIT
			JFrame infoFrame = new JFrame(this.buildingType.name + " Level " + this.buildingType.level);
			infoFrame.setLayout(new BorderLayout());
//			infoFrame.setResizable(false);
			infoFrame.setSize(500, 350);
			
			// picture
			JLabel buildingPicture = this.buildingType.getPicture();
			infoFrame.add(buildingPicture, BorderLayout.CENTER);
			// info
			JTextArea buildingInfo = new JTextArea(this.buildingType.getBuildingInfo());
			buildingInfo.setEnabled(false);
			buildingInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
			buildingInfo.setBackground(null);
			buildingInfo.setFont(GUI.SMALL_FONT);
			buildingInfo.setDisabledTextColor(Color.BLACK);
			infoFrame.add(buildingInfo, BorderLayout.LINE_END);
			// upgrade button
			JButton upgradeButton = new JButton("Upgrade");
			upgradeButton.setFocusPainted(false);
			upgradeButton.setFont(GUI.LARGE_FONT);
			infoFrame.add(upgradeButton, BorderLayout.PAGE_END);
			
			// FINISH
			infoFrame.setVisible(true);
		});
		return button;
	}
}