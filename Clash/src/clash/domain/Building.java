package clash.domain;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import clash.data.DatabaseConn;
import clash.gui.GUI;

public class Building {
	public int id;
	public BuildingType buildingType;
	public Calendar creationTime;
	public int posX;
	public int posY;
	// dont need to track player here, assumed correct

	public Building(int id, BuildingType buildingType, Calendar creationTime, int posX, int posY) {
		this.id = id;
		this.buildingType = buildingType;
		this.creationTime = creationTime;
		this.posX = posX;
		this.posY = posY;
	}
	
	public final JButton makeButton(DatabaseConn dbConn, JPanel basePanel) {
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
			// bottom panel
			JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
			infoFrame.add(bottomPanel, BorderLayout.PAGE_END);
			// upgrade button
			JButton upgradeButton = new JButton("Upgrade");
			upgradeButton.setFocusPainted(false);
			upgradeButton.setFont(GUI.LARGE_FONT);
			upgradeButton.addActionListener((ActionEvent e1) -> {
				GUI.makeBasePanel(dbConn, basePanel);
			});
			bottomPanel.add(upgradeButton);
			// delete button
			JButton deleteButton = new JButton("Delete");
			deleteButton.setFocusPainted(false);
			deleteButton.setFont(GUI.LARGE_FONT);
			deleteButton.addActionListener((ActionEvent e2) -> {
				dbConn.deleteBuilding(this.id);
				infoFrame.dispose();
				GUI.makeBasePanel(dbConn, basePanel);
			});
			bottomPanel.add(deleteButton);
			
			// FINISH
			infoFrame.setVisible(true);
		});
		return button;
	}
}