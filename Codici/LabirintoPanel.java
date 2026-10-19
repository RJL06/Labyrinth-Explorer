import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class LabirintoPanel {
    private static final int WALL_SIZE = 40; // dimensione di un blocco muro

    private List<Rectangle> walls;
    private BufferedImage wallTexture;
    private TexturePaint texture;

    public LabirintoPanel() {
        walls = new ArrayList<>();
        loadWallTexture();
        createMaze();
    }

    private void loadWallTexture() {
        try {
            // ✅ RIMOSSO IL "../"
            wallTexture = ImageIO.read(new File("Image/wall_texture.png"));
            if (wallTexture == null) {
                System.err.println("Texture del muro non trovata, uso colore solido");
                return;
            }
            Rectangle2D rect = new Rectangle2D.Double(
                    0, 0, wallTexture.getWidth(), wallTexture.getHeight());
            texture = new TexturePaint(wallTexture, rect);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Errore nel caricamento della texture: " + e.getMessage());
        }
    }

    private void createMaze() {
        // Bordi esterni (15x15 blocchi)
        for (int i = 0; i < 15; i++) {
            addWall(i * WALL_SIZE, 0, WALL_SIZE, WALL_SIZE);              // alto
            addWall(i * WALL_SIZE, 14 * WALL_SIZE, WALL_SIZE, WALL_SIZE); // basso
            addWall(0, i * WALL_SIZE, WALL_SIZE, WALL_SIZE);              // sinistra
            addWall(14 * WALL_SIZE, i * WALL_SIZE, WALL_SIZE, WALL_SIZE); // destra
        }

        // Entrata in alto a sinistra
        walls.removeIf(w -> w.x == WALL_SIZE && w.y == 0);
        // Uscita in basso a destra
        walls.removeIf(w -> w.x == 13 * WALL_SIZE && w.y == 14 * WALL_SIZE);

        // Muri interni (blocchi)
        addWall(2 * WALL_SIZE, WALL_SIZE, WALL_SIZE, 5 * WALL_SIZE);
        addWall(3 * WALL_SIZE, 5 * WALL_SIZE, 4 * WALL_SIZE, WALL_SIZE);
        addWall(6 * WALL_SIZE, 2 * WALL_SIZE, WALL_SIZE, 3 * WALL_SIZE);

        addWall(8 * WALL_SIZE, 3 * WALL_SIZE, WALL_SIZE, 7 * WALL_SIZE);
        addWall(9 * WALL_SIZE, 3 * WALL_SIZE, 4 * WALL_SIZE, WALL_SIZE);
        addWall(12 * WALL_SIZE, 4 * WALL_SIZE, WALL_SIZE, 4 * WALL_SIZE);

        addWall(4 * WALL_SIZE, 8 * WALL_SIZE, 5 * WALL_SIZE, WALL_SIZE);
        addWall(2 * WALL_SIZE, 10 * WALL_SIZE, WALL_SIZE, 3 * WALL_SIZE);
        addWall(5 * WALL_SIZE, 10 * WALL_SIZE, WALL_SIZE, 2 * WALL_SIZE);

        addWall(10 * WALL_SIZE, 10 * WALL_SIZE, 3 * WALL_SIZE, WALL_SIZE);
        addWall(7 * WALL_SIZE, 12 * WALL_SIZE, WALL_SIZE, 2 * WALL_SIZE);
    }

    private void addWall(int x, int y, int width, int height) {
        walls.add(new Rectangle(x, y, width, height));
    }

    public void drawMaze(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (texture != null) {
            g2d.setPaint(texture);
            for (Rectangle wall : walls) {
                g2d.fill(wall);
            }
        } else {
            g2d.setColor(new Color(64, 64, 64));
            for (Rectangle wall : walls) {
                g2d.fill(wall);
            }
        }
    }

    public boolean checkCollision(Rectangle player) {
        for (Rectangle wall : walls) {
            if (player.intersects(wall)) {
                return true;
            }
        }
        return false;
    }

    public boolean canMove(int newX, int newY, int width, int height) {
        return !checkCollision(new Rectangle(newX, newY, width, height));
    }
}