package clash.domain;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class TroopType {
    public int id;
    public String name;
    public int level;
    public int damage;
    public int attackRate;
    public String damageType;
    public int size;
    public String movementType;
    public int movementSpeed;

    public TroopType(int id, String name, int level, int damage, int attackRate, String damageType, int size,
            String movementType, int movementSpeed) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.damage = damage;
        this.attackRate = attackRate;
        this.damageType = damageType;
        this.size = size;
        this.movementType = movementType;
        this.movementSpeed = movementSpeed;
    }

    public String getTroopInfo() {
        return "Level: " + this.level + "\n"
                + "Damage: " + this.damage + "\n"
                + "Attack Rate: " + this.attackRate + "\n"
                + "Speed: " + this.movementSpeed + "\n"
                + "Movement: " + this.movementType;
    }

    public final JLabel getPicture() {
        Image image;
        JLabel label = null;
        try {
            image = ImageIO.read(new File("src\\clash\\resources\\" + this.name + ".png"))
                    .getScaledInstance(200, 200, 100);
            label = new JLabel(new ImageIcon(image));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return label;
    }
}
