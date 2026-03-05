import MG2D.Couleur;
import MG2D.FenetrePleinEcran;
import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texte;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class HelloBorne {

    private static final int LARGEUR = 1280;
    private static final int HAUTEUR = 1024;
    private static final int ROAD_LEFT = 300;
    private static final int ROAD_RIGHT = 980;
    private static final int ROAD_BOTTOM = 0;
    private static final int ROAD_TOP = HAUTEUR;
    private static final int LANES = 3;
    private static final int CAR_W = 92;
    private static final int CAR_H = 150;
    private static final int PLAYER_Y = 90;
    private static final String SCORE_FILE = "projet/HelloBorne/highscore";

    private static class Obstacle {
        Rectangle shape;
        int speed;

        Obstacle(Rectangle shape, int speed) {
            this.shape = shape;
            this.speed = speed;
        }
    }

    private static boolean intersects(Rectangle a, Rectangle b) {
        int aLeft = Math.min(a.getA().getX(), a.getB().getX());
        int aRight = Math.max(a.getA().getX(), a.getB().getX());
        int aBottom = Math.min(a.getA().getY(), a.getB().getY());
        int aTop = Math.max(a.getA().getY(), a.getB().getY());

        int bLeft = Math.min(b.getA().getX(), b.getB().getX());
        int bRight = Math.max(b.getA().getX(), b.getB().getX());
        int bBottom = Math.min(b.getA().getY(), b.getB().getY());
        int bTop = Math.max(b.getA().getY(), b.getB().getY());

        return aLeft < bRight && aRight > bLeft && aBottom < bTop && aTop > bBottom;
    }

    private static int loadBestScore() {
        File f = new File(SCORE_FILE);
        if (!f.exists()) {
            return 0;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            if (line == null || line.trim().isEmpty()) {
                return 0;
            }
            String[] parts = line.split("-");
            if (parts.length != 2) {
                return 0;
            }
            return Integer.parseInt(parts[1].trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static void saveBestScore(int score) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SCORE_FILE))) {
            bw.write("BOT-" + score);
            bw.newLine();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static int laneCenterX(int lane) {
        int laneWidth = (ROAD_RIGHT - ROAD_LEFT) / LANES;
        return ROAD_LEFT + laneWidth / 2 + lane * laneWidth;
    }

    private static void setCarLane(Rectangle car, int lane, int y) {
        int x = laneCenterX(lane) - CAR_W / 2;
        car.setA(new Point(x, y));
        car.setB(new Point(x + CAR_W, y + CAR_H));
    }

    private static Obstacle buildTrafficCar(int lane, int speed, Random rng) {
        int x = laneCenterX(lane) - CAR_W / 2;
        int y = HAUTEUR + CAR_H + rng.nextInt(120);
        Couleur c = (rng.nextBoolean() ? Couleur.ROUGE : Couleur.BLEU);
        Rectangle r = new Rectangle(c, new Point(x, y), CAR_W, CAR_H, true);
        return new Obstacle(r, speed);
    }

    public static void main(String[] args) {
        FenetrePleinEcran fen = new FenetrePleinEcran("Lane Rush");
        fen.setVisible(true);
        fen.setBackground(Color.BLACK);
        ClavierBorneArcade clavier = new ClavierBorneArcade();
        fen.addKeyListener(clavier);
        if (fen.getP() != null) {
            fen.getP().addKeyListener(clavier);
            fen.getP().requestFocusInWindow();
        }
        fen.requestFocus();

        Rectangle fond = new Rectangle(Couleur.GRIS_FONCE, new Point(0, 0), LARGEUR, HAUTEUR, true);
        Rectangle route = new Rectangle(Couleur.NOIR, new Point(ROAD_LEFT, ROAD_BOTTOM), new Point(ROAD_RIGHT, ROAD_TOP), true);
        Rectangle bordG = new Rectangle(Couleur.JAUNE, new Point(ROAD_LEFT - 8, ROAD_BOTTOM), 8, ROAD_TOP, true);
        Rectangle bordD = new Rectangle(Couleur.JAUNE, new Point(ROAD_RIGHT, ROAD_BOTTOM), 8, ROAD_TOP, true);
        Rectangle joueur = new Rectangle(Couleur.VERT, new Point(0, 0), CAR_W, CAR_H, true);
        int playerLane = 1;
        setCarLane(joueur, playerLane, PLAYER_Y);

        ArrayList<Rectangle> tirets = new ArrayList<>();
        int laneWidth = (ROAD_RIGHT - ROAD_LEFT) / LANES;
        for (int laneSep = 1; laneSep < LANES; laneSep++) {
            int x = ROAD_LEFT + laneSep * laneWidth - 8;
            for (int y = 0; y < HAUTEUR; y += 180) {
                Rectangle mark = new Rectangle(Couleur.BLANC, new Point(x, y), 16, 90, true);
                tirets.add(mark);
            }
        }

        Font fTitle = new Font("Calibri", Font.BOLD, 42);
        Font fMain = new Font("Calibri", Font.BOLD, 30);
        Font fHelp = new Font("Calibri", Font.PLAIN, 22);

        Texte titre = new Texte(Couleur.BLANC, "LANE RUSH", fTitle, new Point(640, 985));
        Texte scoreTxt = new Texte(Couleur.BLANC, "Score: 0", fMain, new Point(170, 980));
        int bestScore = loadBestScore();
        Texte bestTxt = new Texte(Couleur.BLANC, "Best: " + bestScore, fMain, new Point(1030, 980));
        Texte levelTxt = new Texte(Couleur.BLANC, "Level: 1", fMain, new Point(640, 980));
        Texte help1 = new Texte(Couleur.GRIS_CLAIR, "J1 gauche/droite: changer de voie", fHelp, new Point(280, 45));
        Texte help2 = new Texte(Couleur.GRIS_CLAIR, "A: rejouer", fHelp, new Point(620, 45));
        Texte help3 = new Texte(Couleur.GRIS_CLAIR, "Z: quitter", fHelp, new Point(1000, 45));
        Texte info = new Texte(Couleur.JAUNE, "", fMain, new Point(640, 520));

        fen.ajouter(fond);
        fen.ajouter(route);
        fen.ajouter(bordG);
        fen.ajouter(bordD);
        for (Rectangle mark : tirets) {
            fen.ajouter(mark);
        }
        fen.ajouter(joueur);
        fen.ajouter(titre);
        fen.ajouter(scoreTxt);
        fen.ajouter(bestTxt);
        fen.ajouter(levelTxt);
        fen.ajouter(help1);
        fen.ajouter(help2);
        fen.ajouter(help3);
        fen.ajouter(info);

        // Force an initial frame to avoid a blank window if startup is slow.
        fen.rafraichir();

        ArrayList<Obstacle> traffic = new ArrayList<>();
        Random rng = new Random();

        long lastSpawn = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        int spawnDelayMs = 900;
        int[] laneCooldown = new int[] {0, 0, 0};
        int score = 0;
        boolean gameOver = false;

        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            try {
                if (clavier.getBoutonJ1ZTape()) {
                    break;
                }

                if (gameOver) {
                    if (clavier.getBoutonJ1ATape()) {
                        for (Obstacle obs : traffic) {
                            fen.supprimer(obs.shape);
                        }
                        traffic.clear();
                        score = 0;
                        lastSpawn = System.currentTimeMillis();
                        startTime = System.currentTimeMillis();
                        spawnDelayMs = 900;
                        laneCooldown[0] = laneCooldown[1] = laneCooldown[2] = 0;
                        playerLane = 1;
                        setCarLane(joueur, playerLane, PLAYER_Y);
                        info.setTexte("");
                        scoreTxt.setTexte("Score: 0");
                        levelTxt.setTexte("Level: 1");
                        gameOver = false;
                    }
                    fen.rafraichir();
                    continue;
                }

                if (clavier.getJoyJ1GaucheTape() && playerLane > 0) {
                    playerLane--;
                    setCarLane(joueur, playerLane, PLAYER_Y);
                }
                if (clavier.getJoyJ1DroiteTape() && playerLane < LANES - 1) {
                    playerLane++;
                    setCarLane(joueur, playerLane, PLAYER_Y);
                }

                // Animation route (impression de mouvement)
                int roadAnimSpeed = 9;
                for (Rectangle mark : tirets) {
                    mark.translater(0, -roadAnimSpeed);
                    if (mark.getB().getY() < 0) {
                        int h = Math.abs(mark.getB().getY() - mark.getA().getY());
                        mark.setA(new Point(mark.getA().getX(), HAUTEUR + h));
                        mark.setB(new Point(mark.getB().getX(), HAUTEUR + 2 * h));
                    }
                }

                long now = System.currentTimeMillis();
                long elapsedSeconds = (now - startTime) / 1000;
                int level = 1 + (int) (elapsedSeconds / 10);
                int trafficSpeed = Math.min(23, 9 + level / 2);
                spawnDelayMs = Math.max(230, 900 - level * 22);
                levelTxt.setTexte("Level: " + level);

                for (int i = 0; i < LANES; i++) {
                    laneCooldown[i] = Math.max(0, laneCooldown[i] - 20);
                }

                if (now - lastSpawn >= spawnDelayMs) {
                    ArrayList<Integer> open = new ArrayList<>();
                    for (int lane = 0; lane < LANES; lane++) {
                        if (laneCooldown[lane] == 0) {
                            open.add(lane);
                        }
                    }

                    if (!open.isEmpty()) {
                        int lane = open.get(rng.nextInt(open.size()));
                        Obstacle obs = buildTrafficCar(lane, trafficSpeed, rng);
                        traffic.add(obs);
                        fen.ajouter(obs.shape);
                        laneCooldown[lane] = 350;
                    }
                    lastSpawn = now;
                }

                Iterator<Obstacle> it = traffic.iterator();
                while (it.hasNext()) {
                    Obstacle obs = it.next();
                    obs.shape.translater(0, -obs.speed);

                    if (intersects(joueur, obs.shape)) {
                        gameOver = true;
                        if (score > bestScore) {
                            bestScore = score;
                            saveBestScore(bestScore);
                            bestTxt.setTexte("Best: " + bestScore);
                        }
                        info.setTexte("CRASH! A pour rejouer");
                        break;
                    }

                    if (obs.shape.getB().getY() < 0) {
                        fen.supprimer(obs.shape);
                        it.remove();
                        score += 1;
                    }
                }

                scoreTxt.setTexte("Score: " + score);
                fen.rafraichir();
            } catch (Exception e) {
                info.setTexte("Erreur runtime, voir terminal");
                fen.rafraichir();
                e.printStackTrace();
            }
        }

        System.exit(0);
    }
}
