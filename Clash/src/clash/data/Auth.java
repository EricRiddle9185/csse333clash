package clash.data;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JOptionPane;

public class Auth {
    private static final Random RANDOM = new SecureRandom();
    private static final Base64.Encoder enc = Base64.getEncoder();
    private static final Base64.Decoder dec = Base64.getDecoder();

    private DatabaseConn dbConn;
    private String user;
    private int userId;

    public Auth(DatabaseConn dbConn) {
        this.dbConn = dbConn;
    }

    public String user() {
        return this.user;
    }

    public int userId() {
        return this.userId;
    }

    private boolean setUserId() {
        try {
            CallableStatement stmt = this.dbConn.getConn().prepareCall("{? = call GetPlayerId(?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, this.user);

            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                this.userId = results.getInt("ID");

                return true;
            } else {
                this.user = null;
                return false;
            }
        } catch (SQLException e) {
            this.user = null;
            return false;
        }
    }

    public byte[] hashPassword(byte[] salt, String password) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory f;
        byte[] hash = null;
        try {
            f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            hash = f.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
            e.printStackTrace();
        }

        return hash;
    }

    public boolean register(String username, String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hashedPass = hashPassword(salt, password);

        try {
            CallableStatement stmt = this.dbConn.getConn().prepareCall("{? = call Register(?, ?, ?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, username);
            stmt.setString(3, enc.encodeToString(hashedPass));
            stmt.setString(4, enc.encodeToString(salt));

            stmt.execute();

            this.user = username;
            return this.setUserId();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean login(String username, String password) {
        try {
            CallableStatement stmt = this.dbConn.getConn().prepareCall("{? = call GetCredentials(?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, username);

            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                byte[] storedHash = dec.decode(results.getString("PasswordHash"));
                byte[] salt = dec.decode(results.getString("PasswordSalt"));

                byte[] calcedHash = hashPassword(salt, password);

                if (Arrays.equals(storedHash, calcedHash)) {
                    this.user = username;
                    return this.setUserId();
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
