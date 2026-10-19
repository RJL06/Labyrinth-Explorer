import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class CharacterAnimationPanel extends JPanel implements KeyListener, Runnable {
    private static final int PANEL_WIDTH = 600;
    private static final int PANEL_HEIGHT = 650;
    private static final int CHARACTER_WIDTH = 35;
    private static final int CHARACTER_HEIGHT = 35;
    private static final int NORMAL_SPEED = 4;
    private static final int GAME_TIME = 20; // secondi

    private double cumulativeSlowFactor = 1.0;

    // Stato del gioco
    private int x = 40, y = 0;
    private int currentSpeed = NORMAL_SPEED;
    private int timeRemaining = GAME_TIME;
    private int weaponsCollected = 0;
    private boolean gameOver = false;
    private boolean rightPressed = false, leftPressed = false, upPressed = false, downPressed = false;
    private String direction = "DOWN";

    // Oggetti di gioco
    private LabirintoPanel labirinto;
    private List<Traps> trapsList;
    private List<Weapon> weapons;
    private Flag flag;
    private Timer gameTimer;
    private Timer countdownTimer;

    // Immagini
    private BufferedImage backgroundImage;
    private BufferedImage rightImage, leftImage, upImage, downImage;

    private Buffer buf;
    private boolean showInstructions = true;

    public CharacterAnimationPanel(Buffer buf) {
        this.buf = buf;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        labirinto = new LabirintoPanel();
        trapsList = new ArrayList<>();
        weapons = new ArrayList<>();
        
        // ✅ RIMOSSO IL "../"
        flag = new Flag(530, 560, "Image/Flag.png");
        
        loadImages();
        setupKeyListener();
        initializeGame();
    }

    public void run() {
        System.out.println(this.getName() + " " + buf.leggi());
    }

    private void loadImages() {
        try {
            // ✅ RIMOSSO IL "../"
            backgroundImage = ImageIO.read(new File("Image/LawnBackground.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // ✅ RIMOSSO IL "../"
        rightImage = loadAndResizeImage("Image/lateral_right.png", CHARACTER_WIDTH, CHARACTER_HEIGHT);
        leftImage  = loadAndResizeImage("Image/lateral_left.png",  CHARACTER_WIDTH, CHARACTER_HEIGHT);
        upImage    = loadAndResizeImage("Image/back_view.png",     CHARACTER_WIDTH, CHARACTER_HEIGHT);
        downImage  = loadAndResizeImage("Image/frontal_view.png",  CHARACTER_WIDTH, CHARACTER_HEIGHT);
    }

    private BufferedImage loadAndResizeImage(String imagePath, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage bufferedResizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedResizedImage.createGraphics();
            g2d.drawImage(resizedImage, 0, 0, null);
            g2d.dispose();
            return bufferedResizedImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupKeyListener() {
        setFocusable(true);
        addKeyListener(this);
    }

    private void initializeGame() {
        // ✅ RIMOSSO IL "../"
        Traps nT1 = new Traps(520, 475, "Image/Trap1.png");
        Traps nT2 = new Traps(200, 360, "Image/Trap2.png");
        trapsList.add(nT1);
        trapsList.add(nT2);

        // ✅ RIMOSSO IL "../"
        Weapon n1 = new Weapon(450, 160, "Image/Arma1.png", this.buf, "Arma1");
        Weapon n2 = new Weapon(200, 160, "Image/Arma2.png", this.buf, "Arma2");
        Weapon n3 = new Weapon(500, 250, "Image/Arma3.png", this.buf, "Arma3");
        weapons.add(n1);
        weapons.add(n2);
        weapons.add(n3);

        gameTimer = new Timer(16, e -> {
            if (!gameOver) {
                updateCharacterPosition();
                repaint();
            }
        });
        gameTimer.start();

        new Timer(3000, e -> {
            showInstructions = false;
            repaint();
        }).start();

        countdownTimer = new Timer(1000, e -> {
            if (!gameOver) {
                timeRemaining--;
                boolean won = weaponsCollected == weapons.size()
                        && flag.checkCollision(new Rectangle(x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT),
                                               weaponsCollected, weapons.size());
                if (timeRemaining <= 0 || won) {
                    gameOver = true;
                    ((Timer) e.getSource()).stop();
                    gameTimer.stop();
                    if (won) {
                        // ✅ RIMOSSO IL "../"
                        playSoundEffect("Sounds/flag.wav");
                    }
                }
                repaint();
            }
        });
        countdownTimer.start();
    }

    private void updateCharacterPosition() {
        int newX = x;
        int newY = y;

        if (rightPressed) { newX += currentSpeed; direction = "RIGHT"; }
        if (leftPressed)  { newX -= currentSpeed; direction = "LEFT";  }
        if (upPressed)    { newY -= currentSpeed; direction = "UP";    }
        if (downPressed)  { newY += currentSpeed; direction = "DOWN";  }

        Rectangle newBounds = new Rectangle(newX, newY, CHARACTER_WIDTH, CHARACTER_HEIGHT);
        if (!labirinto.checkCollision(newBounds)
                && newX >= 0 && newX <= PANEL_WIDTH - CHARACTER_WIDTH
                && newY >= 0 && newY <= PANEL_HEIGHT - CHARACTER_HEIGHT) {
            x = newX;
            y = newY;
        }

        currentSpeed = NORMAL_SPEED;
        for (Traps trap : trapsList) {
            if (trap.checkCollision(new Rectangle(x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT))) {
                cumulativeSlowFactor += 0.5;
                currentSpeed = (int) (NORMAL_SPEED / cumulativeSlowFactor);
                if (currentSpeed <= 1) currentSpeed = 2;

                if (!trap.isSoundPlayed()) {
                    // ✅ RIMOSSO IL "../"
                    playSoundEffectTraps("Sounds/trap.wav");
                    trap.setSoundPlayed(true);
                }
                break;
            }
        }

        for (Weapon weapon : weapons) {
            if (!weapon.isCollected()
                    && weapon.checkCollision(new Rectangle(x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT))) {
                weaponsCollected++;
                // ✅ RIMOSSO IL "../"
                playSoundEffect("Sounds/weapon.wav");
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        labirinto.drawMaze(g);

        for (Weapon weapon : weapons) {
            weapon.draw(g);
        }

        Image currentImage = switch (direction) {
            case "RIGHT" -> rightImage;
            case "LEFT"  -> leftImage;
            case "UP"    -> upImage;
            default      -> downImage;
        };
        g.drawImage(currentImage, x, y, this);

        for (Traps trap : trapsList) {
            trap.drawTrap(g);
        }

        flag.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Time: " + timeRemaining + "s", 10, PANEL_HEIGHT - 20);
        g.drawString("Weapons: " + weaponsCollected + "/" + weapons.size(),
                PANEL_WIDTH - 150, PANEL_HEIGHT - 20);

        if (showInstructions) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));

            String instructions  = "Raccogli le 3 armi e prendi la";
            String instructions2 = "bandiera per uscire dal labirinto";

            FontMetrics fm = g.getFontMetrics();
            int w1 = fm.stringWidth(instructions);
            int w2 = fm.stringWidth(instructions2);
            g.drawString(instructions,  (PANEL_WIDTH - w1) / 2, PANEL_HEIGHT / 2 - 30);
            g.drawString(instructions2, (PANEL_WIDTH - w2) / 2, PANEL_HEIGHT / 2 + 10);
        }

        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));

            String message = flag.isCollected() ? "You win! Flag Collected!" : "Game Over!";
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(message);
            g.drawString(message, (PANEL_WIDTH - w) / 2, PANEL_HEIGHT / 2);
        }
    }

    private void playSoundEffect(String soundFilePath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(soundFilePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            Timer timer = new Timer(500, e -> clip.stop());
            timer.setRepeats(false);
            timer.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playSoundEffectTraps(String soundFilePath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(soundFilePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_LEFT)  leftPressed  = true;
        if (key == KeyEvent.VK_UP)    upPressed    = true;
        if (key == KeyEvent.VK_DOWN)  downPressed  = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_LEFT)  leftPressed  = false;
        if (key == KeyEvent.VK_UP)    upPressed    = false;
        if (key == KeyEvent.VK_DOWN)  downPressed  = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}