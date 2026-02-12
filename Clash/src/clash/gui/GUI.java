package clash.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

import clash.data.Auth;
import clash.data.DatabaseConn;
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
	private static int[] building_category_sizes = { 3, 4, 2 };
	private static int selected_building_category = 0;
	private static int selected_building_num = 0;
	private static boolean build_mode = false;
	private static JPanel basePanel; // need to be global to allow for auto-refresh

	// test data
	// private static final BuildingType CANNON = new Defense(1, "Cannon", 1, 10,
	// 100, 1, 100, 0, 10, 10, "Single", "Ground");
	// private static final BuildingType ARCHER_TOWER = new Defense(6, "Archer
	// Tower", 1, 60, 200, 2, 250, 0, 5, 20, "Single", "Any");
	// private static final BuildingType WIZARD_TOWER = new Defense(7, "Wizard
	// Tower", 1, 300, 300, 3, 500, 0, 5, 10, "Multi", "Any");
	// private static final ArrayList<BuildingType> DEFENSES = new ArrayList<>(
	// Arrays.asList(CANNON, ARCHER_TOWER, WIZARD_TOWER));

	// more test data
	// private static final Building BUILDING_A = new Building(CANNON, null, 2, 3);
	// private static final Building BUILDING_B = new Building(ARCHER_TOWER, null,
	// 8, 2);
	// private static final Building BUILDING_C = new Building(WIZARD_TOWER, null,
	// 5, 7);
	// private static final ArrayList<Building> PLAYER_BUILDINGS = new
	// ArrayList<>();

	// DB connection info
	private static final String SERVER = "golem.csse.rose-hulman.edu";
	private static final String DB_NAME = "riddleetwagnernbdonovagd";
	private static final String USERNAME = "Clashgui";
	private static final String PASSWORD = "Password123";

	public static void main(String[] args) {
		DatabaseConn dbConn = new DatabaseConn(SERVER, DB_NAME);
		dbConn.connect(USERNAME, PASSWORD);
		Auth auth = new Auth(dbConn);

		JFrame mainFrame = new JFrame("Clash of Clans");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(1140, 680);

		handleLogin(dbConn, auth, mainFrame);

		JPanel mainPanel = new JPanel();
		makeMainPanel(dbConn, auth, mainPanel);

		java.util.Timer timer = new java.util.Timer(); // loop that updates the base panel every second to check for buildings done
		timer.schedule( new TimerTask() {
		    public void run() {
		    	makeBasePanel(dbConn, auth, auth.userId(), basePanel, mainPanel);
		    }
		 }, 0, 1000);

		mainFrame.add(mainPanel);
		mainFrame.setVisible(true);
	}

	private static void handleLogin(DatabaseConn dbConn, Auth auth, JFrame frame) {
		JDialog dialog = new JDialog(frame, "Login", true);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		dialog.setSize(512, 256);

		// panel.setLayout(new GridBagLayout());
		// panel.setPreferredSize(new Dimension(640, 640));
		dialog.setLayout(new GridLayout(3, 2));
		// panel.setBorder(BorderFactory.createMatteBorder(15, 15, 15, 15,
		// Color.GREEN.darker().darker()));

		JLabel usernameLbl = new JLabel("Username:");
		dialog.add(usernameLbl);
		JTextField usernameTxt = new JTextField(20);
		dialog.add(usernameTxt);
		JLabel passwordLbl = new JLabel("Password:");
		dialog.add(passwordLbl);
		JPasswordField passwordTxt = new JPasswordField(20);
		dialog.add(passwordTxt);
		JButton loginButton = new JButton("Login");
		loginButton.addActionListener(e -> {
			if (auth.login(usernameTxt.getText(), passwordTxt.getText())) {
				dialog.setVisible(false);
			} else {
				JOptionPane.showMessageDialog(null, "Login failed");
			}
		});
		dialog.add(loginButton);
		JButton registerButton = new JButton("Register");
		registerButton.addActionListener(e -> {
			if (auth.register(usernameTxt.getText(), passwordTxt.getText())) {
				dialog.setVisible(false);
			} else {
				JOptionPane.showMessageDialog(null, "Register failed");
			}
		});
		dialog.add(registerButton);

		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}

	private static void makeMainPanel(DatabaseConn dbConn, Auth auth, JPanel mainPanel) {
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());
		// mainFrame.setResizable(false);
		GridBagConstraints gbc;

		// BASE PANEL
		basePanel = new JPanel();
		basePanel.setPreferredSize(new Dimension(640, 640));
		basePanel.setBackground(Color.GREEN.darker());
		basePanel.setBorder(BorderFactory.createMatteBorder(15, 15, 15, 15, Color.GREEN.darker().darker()));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;

		// USER PANEL
		JPanel userPanel = new JPanel();
		makeUserPanel(dbConn, auth, userPanel);

		// BASE PANEL cont'd
		makeBasePanel(dbConn, auth, auth.userId(), basePanel, userPanel);
		mainPanel.add(basePanel, gbc);

		// SIDE PANEL
		JTabbedPane sidePanel = new JTabbedPane();
		sidePanel.setPreferredSize(new Dimension(480, 640));
		sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		sidePanel.setFont(MEDIUM_FONT);
		sidePanel.addChangeListener((ChangeEvent e) -> {
			build_mode = sidePanel.getSelectedIndex() == 1;
			makeUserPanel(dbConn, auth, userPanel); // refresh user panel, updates gold/elixir bars

			if (sidePanel.getSelectedIndex() == 3) {
				basePanel.removeAll();
			} else {
				makeBasePanel(dbConn, auth, auth.userId(), basePanel, userPanel);
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0;
		mainPanel.add(sidePanel, gbc);

		// USER PANEL
		sidePanel.addTab("User", userPanel);

		// BUILD PANEL
		JPanel buildPanel = new JPanel();
		makeBuildPanel(dbConn, buildPanel);
		sidePanel.addTab("Build", buildPanel);

		// TRAIN PANEL
		JPanel trainPanel = new JPanel();
		sidePanel.addTab("Train", trainPanel);

		// PLAYERS PANEL
		JPanel playersPanel = new JPanel();
		makePlayersPanel(dbConn, auth, playersPanel, basePanel);
		sidePanel.addTab("Players", playersPanel);

		// REPAINT
		mainPanel.revalidate();
		mainPanel.repaint();
	}

	private static BuildingType getSelectedBuildingType(DatabaseConn dbConn) {
		switch (selected_building_category) {
			case 0:
				return dbConn.getBasicDefenses().get(selected_building_num);
			case 1:
				return dbConn.getBasicResources().get(selected_building_num);
			case 2:
				return dbConn.getBasicArmies().get(selected_building_num);
		}
		return null;
	}

	public static void makeBasePanel(DatabaseConn dbConn, Auth auth, int userId, JPanel basePanel, JPanel userPanel) {
		basePanel.removeAll();
		basePanel.setLayout(new GridBagLayout());
		List<Building> buildings = dbConn.getBuildings(userId, dbConn.getBuildingTypes());
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
					JButton button = building.makeButton(dbConn, auth, userId, basePanel, userPanel);
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
							BuildingType newBuildingType = getSelectedBuildingType(dbConn);
							// Building newBuilding = new Building(newBuildingType, null, posX, posY);
							if (newBuildingType != null) {
								boolean valid1 = posX + newBuildingType.size <= BASE_WIDTH
										&& posY + newBuildingType.size <= BASE_HEIGHT;
								if (!valid1) {
									JOptionPane.showMessageDialog(new JFrame(), "Can't place building: This placement extends byound the edge of the grid", "Error", JOptionPane.ERROR_MESSAGE);
								}
								else {
									for (Building b : buildings) {
										if (isOverlapping(b, posX, posY, newBuildingType.size)) {
											valid1 = false;
											JOptionPane.showMessageDialog(new JFrame(), "Can't place building: This placement overlaps with another building", "Error", JOptionPane.ERROR_MESSAGE);
											break;
										}
									}
								}
								if (valid1) {
									// buildings.add(newBuilding);
									try {
										dbConn.placeBuilding(auth.userId(), newBuildingType.id, posX, posY);
										// buildings
									} catch (SQLException e1) {
										JOptionPane.showMessageDialog(new JFrame(),
												"Can't place building: " + e1.getMessage(), "Error",
												JOptionPane.ERROR_MESSAGE);
									}
									makeBasePanel(dbConn, auth, userId, basePanel, userPanel);
									// makeMainPanel(dbConn, mainPanel); // refresh the screen
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

	public static void makeUserPanel(DatabaseConn dbConn, Auth auth, JPanel userPanel) {
		userPanel.removeAll();
		userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
		// USERNAME
		JLabel username = new JLabel("Hello, " + auth.user());
		username.setFont(LARGE_FONT);
		username.setAlignmentX(Component.CENTER_ALIGNMENT);
		userPanel.add(username);
		// GOLD BAR
		int gold = dbConn.getGold(auth.userId());
		int maxGold = 10000;
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
		int elixir = dbConn.getElixir(auth.userId());
		int maxElixir = 10000;
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
		userPanel.revalidate();
		userPanel.repaint();
	}

	private static void makeBuildPanel(DatabaseConn dbConn, JPanel buildPanel) {
		buildPanel.removeAll();
		buildPanel.setLayout(new BorderLayout());
		BuildingType selectedBuildingType = getSelectedBuildingType(dbConn);
		// BUILDING CATEGORIES
		JPanel buildingCategoryPanel = new JPanel(new GridLayout(1, 3));
		// defenses button
		JButton defensesButton = new JButton("Defences");
		defensesButton.setFocusPainted(false);
		defensesButton.setFont(SMALL_FONT);
		defensesButton.addActionListener((ActionEvent e) -> {
			selected_building_category = 0;
			selected_building_num = 0;
			makeBuildPanel(dbConn, buildPanel);
		});
		buildingCategoryPanel.add(defensesButton);
		// resources button
		JButton resourcesButton = new JButton("Resources");
		resourcesButton.setFocusPainted(false);
		resourcesButton.setFont(SMALL_FONT);
		resourcesButton.addActionListener((ActionEvent e) -> {
			selected_building_category = 1;
			selected_building_num = 0;
			makeBuildPanel(dbConn, buildPanel);
		});
		buildingCategoryPanel.add(resourcesButton);
		// army button
		JButton armyButton = new JButton("Army");
		armyButton.setFocusPainted(false);
		armyButton.setFont(SMALL_FONT);
		armyButton.addActionListener((ActionEvent e) -> {
			selected_building_category = 2;
			selected_building_num = 0;
			makeBuildPanel(dbConn, buildPanel);
		});
		buildingCategoryPanel.add(armyButton);
		buildPanel.add(buildingCategoryPanel, BorderLayout.PAGE_START);
		// INFO
		JPanel buildingInfoPanel = new JPanel(new BorderLayout());
		JLabel buildingPicture = selectedBuildingType.getPicture();
		buildingInfoPanel.add(buildingPicture, BorderLayout.CENTER);
		JTextArea buildingInfo = new JTextArea(selectedBuildingType.getBuildingInfo());
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
			makeBuildPanel(dbConn, buildPanel);
		});
		arrowPanel.add(leftArrow, BorderLayout.LINE_START);
		// building name label
		JLabel buildingName = new JLabel(selectedBuildingType.name);
		buildingName.setFont(LARGE_FONT);
		arrowPanel.add(buildingName, BorderLayout.CENTER);
		// right arrow
		JButton rightArrow = new JButton(">");
		rightArrow.setFocusPainted(false);
		rightArrow.setFont(MEDIUM_FONT);
		rightArrow.addActionListener((ActionEvent e) -> {
			selected_building_num = Math.min(building_category_sizes[selected_building_category] - 1,
					selected_building_num + 1);
			makeBuildPanel(dbConn, buildPanel);
		});
		arrowPanel.add(rightArrow, BorderLayout.LINE_END);
		buildPanel.add(arrowPanel, BorderLayout.PAGE_END);
		// repaint the build panel with the new building
		buildPanel.revalidate();
		buildPanel.repaint();
	}

	private static void makePlayersPanel(DatabaseConn dbConn, Auth auth, JPanel playersPanel, JPanel basePanel) {
		playersPanel.removeAll();

		JPanel listPanel = new JPanel(new GridLayout(0, 1));
		for (String player : dbConn.getPlayers()) {
			if (player.equals(auth.user()))
				continue;

			JButton entry = new JButton(player);
			entry.addActionListener(e -> makeBasePanel(dbConn, auth, dbConn.getUserId(player), basePanel, null));
			listPanel.add(entry);
		}
		JScrollPane scrollPane = new JScrollPane(listPanel);
		playersPanel.add(scrollPane);

		playersPanel.revalidate();
		playersPanel.repaint();
	}
}