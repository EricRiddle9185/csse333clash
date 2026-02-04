package clash.data;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import clash.domain.*;

public class DatabaseConn {
    private static final String URL = "jdbc:sqlserver://${dbServer};databaseName=${dbName};user=${user};password={${pass}};encrypt=false;";

    private Connection conn = null;

    private String databaseName;
    private String serverName;

    public DatabaseConn(String serverName, String databaseName) {
        this.serverName = serverName;
        this.databaseName = databaseName;
    }

    public boolean connect(String user, String pass) {
        String url = DatabaseConn.URL
                .replace("${dbServer}", this.serverName)
                .replace("${dbName}", this.databaseName)
                .replace("${user}", user)
                .replace("${pass}", pass);

        try {
            this.conn = DriverManager.getConnection(url);

            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConn() {
        return this.conn;
    }

    public void closeConnection() {
        try {
            if (this.conn != null && !this.conn.isClosed()) {
                this.conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BuildingType> getBuildingTypes() {
    	// get all buildings
    	CallableStatement stmt;
    	ResultSet results;
        try {
            stmt = this.conn.prepareCall("{? = call GetBuildingTypes}");
            stmt.registerOutParameter(1, Types.INTEGER);
            results = stmt.executeQuery();

            ArrayList<BuildingType> buildingTypes = new ArrayList<BuildingType>();
            while (results.next()) {
            	int id = results.getInt("id");
                String name = results.getString("name");
                int level = results.getInt("level");
                int buildTime = results.getInt("buildTime");
                int maxHealth = results.getInt("maxHealth");
                int size = results.getInt("size");
                int goldCost = results.getInt("goldCost");
                int elixirCost = results.getInt("elixirCost");

                buildingTypes.add(new BuildingType(id, name, level, buildTime, maxHealth, size, goldCost, elixirCost));
            }
            
            // replace defenses with defense objects
			stmt = this.conn.prepareCall("{? = call GetDefenses}");
			stmt.registerOutParameter(1, Types.INTEGER);
			results = stmt.executeQuery();
            while (results.next()) {
            	int id = results.getInt("id");
            	int damage = results.getInt("damage");
            	int attackRate = results.getInt("attackRate");
            	String damageType = results.getString("damageType");
            	String target = results.getString("attacksMovementType");
            	
            	// replace building type in buildingTypes with subclass
            	for (int i = 0; i < buildingTypes.size(); i++) {
					BuildingType old = buildingTypes.get(i);
            		if (id == old.id) {
            			buildingTypes.remove(i);
            			buildingTypes.add(i, new Defense(old, damage, attackRate, damageType, target));
            			break;
            		}
            	}
            }
            
            // replace collectors with collector objects
			stmt = this.conn.prepareCall("{? = call GetCollectors}");
			stmt.registerOutParameter(1, Types.INTEGER);
			results = stmt.executeQuery();
            while (results.next()) {
            	int id = results.getInt("id");
            	int goldPerHour = results.getInt("collectsGold");
            	int elixirPerHour = results.getInt("collectsElixir");
            	
            	// replace building type in buildingTypes with subclass
            	for (int i = 0; i < buildingTypes.size(); i++) {
					BuildingType old = buildingTypes.get(i);
            		if (id == old.id) {
            			buildingTypes.remove(i);
            			buildingTypes.add(i, new Collector(old, goldPerHour, elixirPerHour));
            			break;
            		}
            	}
            }
            
            // replace storages with storage objects
			stmt = this.conn.prepareCall("{? = call GetStorages}");
			stmt.registerOutParameter(1, Types.INTEGER);
			results = stmt.executeQuery();
            while (results.next()) {
            	int id = results.getInt("id");
            	int goldStored = results.getInt("storesGold");
            	int elixirStored = results.getInt("storesElixir");
            	
            	// replace building type in buildingTypes with subclass
            	for (int i = 0; i < buildingTypes.size(); i++) {
					BuildingType old = buildingTypes.get(i);
            		if (id == old.id) {
            			buildingTypes.remove(i);
            			buildingTypes.add(i, new Storage(old, goldStored, elixirStored));
            			break;
            		}
            	}
            }

            return buildingTypes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<BuildingType> getBasicDefenses() {
    	List<BuildingType> buildingTypes = this.getBuildingTypes();
    	buildingTypes.removeIf(x -> !(x.level == 1 && x instanceof Defense));
    	return buildingTypes;
    }
    
    public List<BuildingType> getBasicResources() {
    	List<BuildingType> buildingTypes = this.getBuildingTypes();
    	buildingTypes.removeIf(x -> !(x.level == 1 && (x instanceof Collector || x instanceof Storage)));
    	return buildingTypes;
    }

    public List<BuildingType> getBasicArmies() {
    	List<BuildingType> buildingTypes = this.getBuildingTypes();
    	buildingTypes.removeIf(x -> !(x.level == 1 && (x instanceof Camp || x.name.equals("Barracks"))));
    	return buildingTypes;
    }

    public List<Building> getBuildings(int playerId, List<BuildingType> buildingTypes) {
        try {
            CallableStatement stmt = this.conn.prepareCall("{? = call GetBuildings(?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, playerId);
            ResultSet results = stmt.executeQuery();

            ArrayList<Building> buildings = new ArrayList<Building>();
            while (results.next()) {
                String name = results.getString("name");
                int level = results.getInt("level");
                results.getInt("maxHealth");
                BuildingType buildingType = buildingTypes
                        .stream()
                        .filter(kind -> kind.name.equals(name) && kind.level == level)
                        .findAny()
                        .get();
                int id = results.getInt("id");
                int x = results.getInt("posx");
                int y = results.getInt("posy");
                Date creationTime = results.getDate("CreationTime");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(creationTime);
                buildings.add(new Building(id, buildingType, calendar, x, y));
            }

            return buildings;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteBuilding(int userId) {
        try {
            CallableStatement stmt = this.conn.prepareCall("{? = call DeleteBuilding(?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, userId);
            stmt.execute();

            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean placeBuilding(int buildingTypeID, int posX, int posY) throws SQLException {
		CallableStatement stmt = this.conn.prepareCall("{? = call PlaceBuilding(?, ?, ?, ?)}");
		stmt.registerOutParameter(1, Types.INTEGER);
		stmt.setInt(2, 1); // TODO: add other players lol
		stmt.setInt(3, posX);
		stmt.setInt(4, posY);
		stmt.setInt(5, buildingTypeID);
		stmt.execute();
//        ResultSet results = stmt.executeQuery();
		// TODO: do smth with the results?

		return true;
    }
    
    public int getElixir(int playerID) {
		try {
			CallableStatement stmt = this.conn.prepareCall("{? = call GetElixir(?)}");
			stmt.registerOutParameter(1, Types.INTEGER);
			stmt.setInt(2, playerID);
			ResultSet results = stmt.executeQuery();
			results.next();
			int elixir = results.getInt("amount");
			return elixir;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
    }
    public int getGold(int playerID) {
		try {
			CallableStatement stmt = this.conn.prepareCall("{? = call GetGold(?)}");
			stmt.registerOutParameter(1, Types.INTEGER);
			stmt.setInt(2, playerID);
			ResultSet results = stmt.executeQuery();
			results.next();
			int gold = results.getInt("amount");
			return gold;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
    }
}