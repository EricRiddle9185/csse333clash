package clash.domain;

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
        try {
            CallableStatement stmt = this.conn.prepareCall("{? = call GetBuildingTypes}");
            stmt.registerOutParameter(1, Types.INTEGER);
            ResultSet results = stmt.executeQuery();

            ArrayList<BuildingType> buildingTypes = new ArrayList<BuildingType>();
            while (results.next()) {
                String name = results.getString("name");
                int level = results.getInt("level");
                int buildTime = results.getInt("buildTime");
                int maxHealth = results.getInt("maxHealth");
                int size = results.getInt("size");
                int goldCost = results.getInt("goldCost");
                int elixirCost = results.getInt("elixirCost");

                buildingTypes.add(new BuildingType(name, level, buildTime, maxHealth, size, goldCost, elixirCost));
            }

            return buildingTypes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Building> getBuildings(int userId, List<BuildingType> buildingTypes) {
        try {
            CallableStatement stmt = this.conn.prepareCall("{? = call GetBuildings(?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, userId);
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
                int x = results.getInt("posx");
                int y = results.getInt("posy");
                Date creationTime = results.getDate("CreationTime");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(creationTime);
                buildings.add(new Building(buildingType, calendar, x, y));
            }

            return buildings;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}