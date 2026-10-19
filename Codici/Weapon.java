import java.awt.*;
import javax.swing.*;

public class Weapon implements Runnable {
    private static final int WEAPON_WIDTH = 30;
    private static final int WEAPON_HEIGHT = 30;
    private static final int FALL_SPEED_MS = 90; // intervallo di caduta
    private static final int MAX_Y = 600;        // limite inferiore

    private final Image weaponImage;
    private final String nome;     // nome dell'arma: è il dato che passa nel buffer
    private final Buffer buf;

    private int x;
    private int y;
    private boolean collected;
    private volatile boolean running;   // letto da più thread: volatile
    private Thread thread;

    public Weapon(int x, int y, String imagePath, Buffer buf, String nome) {
        this.x = x;
        this.y = y;
        this.collected = false;
        this.running = true;
        this.nome = nome;
        this.buf = buf;
        this.weaponImage = new ImageIcon(imagePath).getImage()
                .getScaledInstance(WEAPON_WIDTH, WEAPON_HEIGHT, Image.SCALE_SMOOTH);
        this.thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (running) {
            try {
                moveWeapon();
                Thread.sleep(FALL_SPEED_MS);   // movimento regolare, SEMPRE
                if (running && !collected) {
                    buf.scrivi(nome);           // produce DOPO lo sleep
                }
            } catch (InterruptedException e) {
                running = false;                // se ci interrompono, esci
                Thread.currentThread().interrupt();
            }
        }
    }

    private void moveWeapon() {
        if (!collected) {
            y += 1;
            if (y > MAX_Y) {
                y = 0;   // ricomincia dall'alto
            }
        }
    }

    public void draw(Graphics g) {
        if (!collected) {
            g.drawImage(weaponImage, x, y, null);
        }
    }

    public boolean checkCollision(Rectangle playerBounds) {
        if (!collected && playerBounds.intersects(new Rectangle(x, y, WEAPON_WIDTH, WEAPON_HEIGHT))) {
            collected = true;
            stopThread();
            return true;
        }
        return false;
    }

    public boolean isCollected() {
        return collected;
    }

    public void stopThread() {
        running = false;
        if (thread != null) {
            thread.interrupt();   // sveglia il thread se è in sleep/wait
        }
    }
}