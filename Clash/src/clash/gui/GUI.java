package clash.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

import clash.domain.*;

public class GUI {
	// constants
	private static final int BASE_HEIGHT = 16;
	private static final int BASE_WIDTH = 16;

	// fonts
	public static final Font GIANT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 48);
	public static final Font LARGE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 32);
	public static final Font MEDIUM_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
	public static final Font SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 20);

	// variables
	private static int[] building_category_sizes = { 3, 0, 0 };
	private static int selected_building_category = 0;
	private static int selected_building_num = 0;
	private static boolean build_mode = false;

	// test data
	private static final BuildingType CANNON = new Defense("Cannon", 1, 10, 100, 1, 100, 0, 10, 10, "Single", "Ground");
	private static final BuildingType ARCHER_TOWER = new Defense("Archer Tower", 1, 60, 200, 2, 250, 0, 5, 20, "Single",
			"Any");
	private static final BuildingType WIZARD_TOWER = new Defense("Wizard Tower", 1, 300, 300, 3, 500, 0, 5, 10, "Multi",
			"Any");
	private static final ArrayList<BuildingType> DEFENSES = new ArrayList<>(
			Arrays.asList(CANNON, ARCHER_TOWER, WIZARD_TOWER));

	// more test data
	private static final Building BUILDING_A = new Building(CANNON, null, 2, 3);
	private static final Building BUILDING_B = new Building(ARCHER_TOWER, null, 8, 2);
	private static final Building BUILDING_C = new Building(WIZARD_TOWER, null, 5, 7);
	private static final ArrayList<Building> PLAYER_BUILDINGS = new ArrayList<>();

	// DB connection info
	private static final String SERVER = "golem.csse.rose-hulman.edu";
	private static final String DB_NAME = "riddleetwagnernbdonovagd";
	private static final String USERNAME = "Clashgui";
	private static final String PASSWORD = "Password123";

	public static void main(String[] args) {
		DatabaseConn dbConn = new DatabaseConn(SERVER, DB_NAME);
		dbConn.connect(USERNAME, PASSWORD);

		List<BuildingType> buildingTypes = dbConn.getBuildingTypes();

		for (Building kind : dbConn.getBuildings(1, buildingTypes)) {
			System.out.println(kind.buildingType.name + "\n" + kind.buildingType.getBuildingInfo());
		}

		// ------//
		// Init //
		// ------//
		JFrame mainFrame = new JFrame("Clash of Clans");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setLayout(new GridBagLayout());
		mainFrame.setSize(1140, 680);
		// mainFrame.setResizable(false);
		GridBagConstraints gbc;

		//------//
		// BASE //
		//------//
		// JPanel basePanel = new JPanel(new GridLayout(BASE_HEIGHT, BASE_WIDTH));
		JPanel basePanel = new JPanel(new GridBagLayout());
		basePanel.setPreferredSize(new Dimension(640, 640));
		basePanel.setBackground(Color.GREEN.darker());
		basePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		// BASE GRID BUTTONS
		// ArrayList<Building> buildings = Arrays.asList(CANNON);
		makeBasePanel(dbConn, basePanel);
		mainFrame.add(basePanel, gbc);

		// ------------//
		// SIDE PANEL //
		// ------------//
		JTabbedPane sidePanel = new JTabbedPane();
		sidePanel.setPreferredSize(new Dimension(480, 640));
		sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		sidePanel.setFont(MEDIUM_FONT);
		sidePanel.addChangeListener((ChangeEvent e) -> {
			build_mode = sidePanel.getSelectedIndex() == 1;
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0;
		mainFrame.add(sidePanel, gbc);

		// ------------//
		// USER PANEL //
		// ------------//
		JPanel userPanel = makeUserPanel();
		sidePanel.addTab("User", userPanel);

		// -------------//
		// BUILD PANEL //
		// -------------//
		JPanel buildPanel = new JPanel(new BorderLayout());
		makeBuildPanel(buildPanel, CANNON);
		sidePanel.addTab("Build", buildPanel);

		// -------------//
		// TRAIN PANEL //
		// -------------//
		JPanel trainPanel = new JPanel();
		sidePanel.addTab("Train", trainPanel);

		// --------//
		// FINISH //
		// --------//
		mainFrame.setVisible(true);
	}

	private static void makeBasePanel(DatabaseConn dbConn, JPanel basePanel) {
		basePanel.removeAll();
		List<Building> buildings = dbConn.getBuildings(1, dbConn.getBuildingTypes()); // TODO: add other players
		for (int i = 0; i < BASE_HEIGHT; i++) {
			for (int j = 0; j < BASE_WIDTH; j++) {
				boolean valid = true;
				Building building = null;
				for (Building b : buildings) {
					// check for building to be placed
					if (b.posX == j && b.posY == i) {
						building = b;
						break;
					}
					// check for overlap
					if (isOverlapping(b, j, i, 1)) {
						valid = false;
						break;
					}
				}
				// place a building
				if (building != null) {
					JButton button = building.getButton();
					button.setFocusPainted(false);
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.fill = GridBagConstraints.BOTH;
					gbc.gridx = j;
					gbc.gridy = i;
					gbc.gridwidth = building.buildingType.size;
					gbc.gridheight = building.buildingType.size;
					basePanel.add(button, gbc);
				}
				// create an empty square
				else if (valid) {
					JButton button = new JButton();
					button.setFocusPainted(false);
					button.setMargin(new Insets(16, 16, 16, 16));
					button.setContentAreaFilled(false);
					int posX = j;
					int posY = i;
					// button action to create new building
					button.addActionListener((ActionEvent e) -> {
						if (build_mode) {
							BuildingType newBuildingType = null;
							switch (selected_building_category) {
								case 0:
									newBuildingType = DEFENSES.get(selected_building_num);
							}
//							Building newBuilding = new Building(newBuildingType, null, posX, posY);
							if (newBuildingType != null) {
								boolean valid1 = posX + newBuildingType.size <= BASE_WIDTH
										&& posY + newBuildingType.size <= BASE_HEIGHT;
								for (Building b : buildings) {
									if (isOverlapping(b, posX, posY, newBuildingType.size)) {
										valid1 = false;
										break;
									}
								}
								if (valid1) {
									System.out.println("place buliding");
//									buildings.add(newBuilding);
									dbConn.placeBuilding(1, posX, posY); // TODO: add other buildings
									// TODO: add popups for things like insufficient resources
									makeBasePanel(dbConn, basePanel);
								}
							}
						}
					});
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.fill = GridBagConstraints.BOTH;
					gbc.gridx = j;
					gbc.gridy = i;
					basePanel.add(button, gbc);
				}
			}
		}
		basePanel.revalidate();
		basePanel.repaint();
	}

	// can be used for both drawing the base and checking that a building can be
	// placed
	private static boolean isOverlapping(Building building, int posX, int posY, int size) {
		int left = posX;
		int right = posX + size - 1;
		int top = posY;
		int bottom = posY + size - 1;
		int bLeft = building.posX;
		int bRight = building.posX + building.buildingType.size - 1;
		int bTop = building.posY;
		int bBottom = building.posY + building.buildingType.size - 1;
		return (((left >= bLeft && left <= bRight) || (right >= bLeft && right <= bRight)) &&
				((top >= bTop && top <= bBottom) || (bottom >= bTop && bottom <= bBottom))) ||

				(((bLeft >= left && bLeft <= right) || (bRight >= left && bRight <= right)) &&
						((bTop >= top && bTop <= bottom) || (bBottom >= top && bBottom <= bottom)));
	}

	private static JPanel makeUserPanel() {
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
		// USERNAME
		JLabel username = new JLabel("Hello, riddleet");
		username.setFont(LARGE_FONT);
		username.setAlignmentX(Component.CENTER_ALIGNMENT);
		userPanel.add(username);
		// GOLD BAR
		int gold = 500;
		int maxGold = 1000;
		JPanel goldPanel = new JPanel();
		goldPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel goldLabel = new JLabel("Gold: ");
		goldLabel.setFont(MEDIUM_FONT);
		goldPanel.add(goldLabel);
		JProgressBar goldBar = new JProgressBar();
		goldBar.setForeground(Color.YELLOW);
		// goldBar.addChangeListener(null);
		goldBar.setValue(gold * 100 / maxGold);
		goldPanel.add(goldBar);
		userPanel.add(goldPanel);
		// ELIXIR BAR
		int elixir = 500;
		int maxElixir = 1000;
		JPanel elixirPanel = new JPanel();
		elixirPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel elixirLabel = new JLabel("Elixir: ");
		elixirLabel.setFont(MEDIUM_FONT);
		elixirPanel.add(elixirLabel);
		JProgressBar elixirBar = new JProgressBar();
		elixirBar.setForeground(Color.MAGENTA);
		// elixirBar.addChangeListener(null);
		elixirBar.setValue(elixir * 100 / maxElixir);
		elixirPanel.add(elixirBar);
		userPanel.add(elixirPanel);
		// ATTACK BUTTON
		JButton attackButton = new JButton("Attack");
		attackButton.setFocusPainted(false);
		attackButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		attackButton.setFont(GIANT_FONT);
		userPanel.add(attackButton);
		return userPanel;

	}

	private static void makeBuildPanel(JPanel buildPanel, BuildingType building) {
		buildPanel.removeAll();
		// BUILDING CATEGORIES
		JPanel buildingCategoryPanel = new JPanel(new GridLayout(1, 3));
		JButton defensesButton = new JButton("Defences");
		defensesButton.setFocusPainted(false);
		defensesButton.setFont(SMALL_FONT);
		buildingCategoryPanel.add(defensesButton);
		JButton resourcesButton = new JButton("Resources");
		resourcesButton.setFocusPainted(false);
		resourcesButton.setFont(SMALL_FONT);
		buildingCategoryPanel.add(resourcesButton);
		JButton armyButton = new JButton("Army");
		armyButton.setFocusPainted(false);
		armyButton.setFont(SMALL_FONT);
		buildingCategoryPanel.add(armyButton);
		buildPanel.add(buildingCategoryPanel, BorderLayout.PAGE_START);
		// INFO
		JPanel buildingInfoPanel = new JPanel(new BorderLayout());
		JLabel buildingPicture = building.getPicture();
		buildingInfoPanel.add(buildingPicture, BorderLayout.CENTER);
		JTextArea buildingInfo = new JTextArea(building.getBuildingInfo());
		buildingInfo.setEnabled(false);
		buildingInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
		buildingInfo.setBackground(null);
		buildingInfo.setFont(SMALL_FONT);
		buildingInfo.setDisabledTextColor(Color.BLACK);
		buildingInfoPanel.add(buildingInfo, BorderLayout.PAGE_END);
		buildPanel.add(buildingInfoPanel, BorderLayout.CENTER);
		// ARROWS AND BUILD
		JPanel arrowPanel = new JPanel(new BorderLayout());
		// left arrow
		JButton leftArrow = new JButton("<");
		leftArrow.setFocusPainted(false);
		leftArrow.setFont(MEDIUM_FONT);
		leftArrow.addActionListener((ActionEvent e) -> {
			selected_building_num = Math.max(0, selected_building_num - 1);
			switch (selected_building_category) {
				case 0:
					makeBuildPanel(buildPanel, DEFENSES.get(selected_building_num));
			}
		});
		arrowPanel.add(leftArrow, BorderLayout.LINE_START);
		// building name label
		JLabel buildingName = new JLabel(building.name);
		buildingName.setFont(LARGE_FONT);
		arrowPanel.add(buildingName, BorderLayout.CENTER);
		// right arrow
		JButton rightArrow = new JButton(">");
		rightArrow.setFocusPainted(false);
		rightArrow.setFont(MEDIUM_FONT);
		rightArrow.addActionListener((ActionEvent e) -> {
			selected_building_num = Math.min(building_category_sizes[selected_building_category] - 1,
					selected_building_num + 1);
			switch (selected_building_category) {
				case 0:
					makeBuildPanel(buildPanel, DEFENSES.get(selected_building_num));
			}
		});
		arrowPanel.add(rightArrow, BorderLayout.LINE_END);
		// build button
		JButton buildButton = new JButton("Build");
		buildButton.setFont(LARGE_FONT);
		buildButton.addActionListener((ActionEvent e) -> {

		});
		// arrowPanel.add(buildButton, BorderLayout.PAGE_END);
		buildPanel.add(arrowPanel, BorderLayout.PAGE_END);
		buildPanel.revalidate();
		buildPanel.repaint();
	}
}