package clash.domain;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

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
	
	private void makeInfoPanel(DatabaseConn dbConn, JFrame infoFrame, JPanel infoPanel, JPanel basePanel, JPanel userPanel) {
		infoPanel.removeAll();

		// make sure to get the correct building type in case this has been upgraded
		List<Building> buildings = dbConn.getBuildings(1, dbConn.getBuildingTypes()); // TODO: add other players
		BuildingType buildingType = this.buildingType;
		for (Building b : buildings) {
			if (b.id == this.id) {
				buildingType = b.buildingType;
			}
		}
		infoFrame.setTitle(buildingType.name + " Level " + buildingType.level);

		// picture
		JLabel buildingPicture = buildingType.getPicture();
		infoPanel.add(buildingPicture, BorderLayout.CENTER);
		// info
		JTextArea buildingInfo = new JTextArea(buildingType.getBuildingInfo());
		buildingInfo.setEnabled(false);
		buildingInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
		buildingInfo.setBackground(null);
		buildingInfo.setFont(GUI.SMALL_FONT);
		buildingInfo.setDisabledTextColor(Color.BLACK);
		infoPanel.add(buildingInfo, BorderLayout.LINE_END);
		// bottom panel
		JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
		infoPanel.add(bottomPanel, BorderLayout.PAGE_END);
		// upgrade button
		JButton upgradeButton = new JButton("Upgrade");
		upgradeButton.setFocusPainted(false);
		upgradeButton.setFont(GUI.LARGE_FONT);
		upgradeButton.addActionListener((ActionEvent e1) -> {
			try {
				dbConn.upgradeBuilding(this.id);
				// TODO: update this.bt do reflect update
			} catch (SQLException e2) {
				JOptionPane.showMessageDialog(new JFrame(), "Can't upgrade building: " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			makeInfoPanel(dbConn, infoFrame, infoPanel, basePanel, userPanel);
			GUI.makeUserPanel(dbConn, userPanel); // fix user's gold/elixir bars
			GUI.makeBasePanel(dbConn, basePanel, userPanel);
		});
		bottomPanel.add(upgradeButton);
		// delete button
		JButton deleteButton = new JButton("Delete");
		deleteButton.setFocusPainted(false);
		deleteButton.setFont(GUI.LARGE_FONT);
		deleteButton.addActionListener((ActionEvent e2) -> {
			dbConn.deleteBuilding(this.id);
			infoFrame.dispose();
			GUI.makeBasePanel(dbConn, basePanel, userPanel);
		});
		bottomPanel.add(deleteButton);
		
		infoPanel.revalidate();
		infoPanel.repaint();
	}
	
	public final JButton makeButton(DatabaseConn dbConn, JPanel basePanel, JPanel userPanel) {
		// init
		Image buildingImage;
		JButton button = new JButton();
//		button.setPreferredSize(new Dimension(50 * this.buildingType.size, 50 * this.buildingType.size));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setContentAreaFilled(false);
		// image
		try {
			buildingImage = ImageIO.read(new File("src\\clash\\resources\\" + buildingType.name + buildingType.level + ".png"))
					.getScaledInstance(32 * buildingType.size, 32 * buildingType.size, 100);
			button.setIcon(new ImageIcon(buildingImage));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// popup window when clicked
		button.addActionListener((ActionEvent e) -> {
			// INIT
			JFrame infoFrame = new JFrame();
			infoFrame.setLayout(new BorderLayout());
//			infoFrame.setResizable(false);
			infoFrame.setSize(500, 350);

			JPanel infoPanel = new JPanel(new BorderLayout());
			this.makeInfoPanel(dbConn, infoFrame, infoPanel, basePanel, userPanel);
			infoFrame.add(infoPanel);
			
			// FINISH
			infoFrame.setVisible(true);
		});
		return button;
	}
}