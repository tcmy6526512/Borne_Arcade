import MG2D.Couleur;
import MG2D.FenetrePleinEcran;
import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texte;
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

    private static final int LARGEUR = 1275;
    private static final int HAUTEUR = 1020;
    private static final int ROAD_LEFT = 260;
    private static final int ROAD_RIGHT = 1015;
    private static final int ROAD_BOTTOM = 0;
    private static final int ROAD_TOP = HAUTEUR;
    private static final int LANES = 3;

    private static final int CAR_W = 90;
    private static final int CAR_H = 145;
    private static final int PLAYER_Y = 85;

    private static final String SCORE_FILE = "projet/HelloBorne/highscore";

    private static class Obstacle {
        Rectangle rect;
        int speed;

        Obstacle(Rectangle rect, int speed) {
            this.rect = rect;
            this.speed = speed;
        }
    }

    private static int laneCenterX(int lane) {
        int laneWidth = (ROAD_RIGHT - ROAD_LEFT) / LANES;
        return ROAD_LEFT + (lane * laneWidth) + laneWidth / 2;
    }

    private static void placeCar(Rectangle car, int lane, int y) {
        int cx = laneCenterX(lane);
        int left = cx - CAR_W / 2;
        car.setA(new Point(left, y));
        car.setB(new Point(left + CAR_W, y + CAR_H));
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
            if (line == null) {
                return 0;
            }
            String[] p = line.split("-");
            if (p.length != 2) {
                return 0;
            }
            return Integer.parseInt(p[1].trim());
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

    private static Rectangle laneMark(int x, int y) {
        Rectangle r = new Rectangle(Couleur.BLANC, new Point(x, y), new Point(x + 14, y + 90));
        r.setPlein(true);
        return r;
    }

    private static Rectangle solidRect(Couleur c, int x1, int y1, int x2, int y2) {
        Rectangle r = new Rectangle(c, new Point(x1, y1), new Point(x2, y2));
        r.setPlein(true);
        return r;
    }

    public static void main(String[] args) {
        FenetrePleinEcran f = new FenetrePleinEcran("HelloBorne Lane Rush");
        f.setVisible(true);

        ClavierBorneArcade clavier = new ClavierBorneArcade();
        f.addKeyListener(clavier);
        f.getP().addKeyListener(clavier);

        Rectangle bg = solidRect(Couleur.GRIS_FONCE, 0, 0, LARGEUR, HAUTEUR);
        Rectangle road = solidRect(Couleur.NOIR, ROAD_LEFT, ROAD_BOTTOM, ROAD_RIGHT, ROAD_TOP);
        Rectangle borderL = solidRect(Couleur.JAUNE, ROAD_LEFT - 8, ROAD_BOTTOM, ROAD_LEFT, ROAD_TOP);
        Rectangle borderR = solidRect(Couleur.JAUNE, ROAD_RIGHT, ROAD_BOTTOM, ROAD_RIGHT + 8, ROAD_TOP);

        Rectangle player = solidRect(Couleur.VERT, 0, 0, CAR_W, CAR_H);
        int playerLane = 1;
        placeCar(player, playerLane, PLAYER_Y);

        ArrayList<Rectangle> laneMarks = new ArrayList<>();
        int laneWidth = (ROAD_RIGHT - ROAD_LEFT) / LANES;
        for (int sep = 1; sep < LANES; sep++) {
            int x = ROAD_LEFT + sep * laneWidth - 7;
            for (int y = -90; y <= HAUTEUR + 90; y += 180) {
                laneMarks.add(laneMark(x, y));
            }
        }

        Font fontBig = new Font("Calibri", Font.BOLD, 40);
        Font font = new Font("Calibri", Font.BOLD, 28);

        Texte title = new Texte(Couleur.BLANC, "LANE RUSH", fontBig, new Point(640, 980));
        Texte scoreTxt = new Texte(Couleur.BLANC, "Score: 0", font, new Point(140, 980));
        int bestScore = loadBestScore();
        Texte bestTxt = new Texte(Couleur.BLANC, "Best: " + bestScore, font, new Point(1100, 980));
        Texte levelTxt = new Texte(Couleur.BLANC, "Level: 1", font, new Point(640, 940));
        Texte helpTxt = new Texte(Couleur.GRIS_CLAIR, "J1 Gauche/Droite: voie | A: rejouer | Z: quitter", font, new Point(640, 40));
        Texte infoTxt = new Texte(Couleur.JAUNE, "", font, new Point(640, 520));

        f.ajouter(bg);
        f.ajouter(road);
        f.ajouter(borderL);
        f.ajouter(borderR);
        for (Rectangle mark : laneMarks) {
            f.ajouter(mark);
        }
        f.ajouter(player);
        f.ajouter(title);
        f.ajouter(scoreTxt);
        f.ajouter(bestTxt);
        f.ajouter(levelTxt);
        f.ajouter(helpTxt);
        f.ajouter(infoTxt);
        f.rafraichir();

        ArrayList<Obstacle> obstacles = new ArrayList<>();
        Random rng = new Random();

        int[] laneCooldown = new int[] {0, 0, 0};
        long startTime = System.currentTimeMillis();
        long lastSpawn = startTime;

        int score = 0;
        boolean gameOver = false;

        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            if (clavier.getBoutonJ1ZTape()) {
                break;
            }

            if (gameOver) {
                if (clavier.getBoutonJ1ATape()) {
                    for (Obstacle obs : obstacles) {
                        f.supprimer(obs.rect);
                    }
                    obstacles.clear();

                    laneCooldown[0] = 0;
                    laneCooldown[1] = 0;
                    laneCooldown[2] = 0;

                    startTime = System.currentTimeMillis();
                    lastSpawn = startTime;
                    score = 0;
                    playerLane = 1;
                    placeCar(player, playerLane, PLAYER_Y);
                    player.setCouleur(Couleur.VERT);
                    scoreTxt.setTexte("Score: 0");
                    levelTxt.setTexte("Level: 1");
                    infoTxt.setTexte("");
                    gameOver = false;
                }
                f.rafraichir();
                continue;
            }

            if (clavier.getJoyJ1GaucheTape() && playerLane > 0) {
                playerLane--;
                placeCar(player, playerLane, PLAYER_Y);
            }
            if (clavier.getJoyJ1DroiteTape() && playerLane < LANES - 1) {
                playerLane++;
                placeCar(player, playerLane, PLAYER_Y);
            }

            for (Rectangle mark : laneMarks) {
                mark.translater(0, -10);
                if (mark.getB().getY() < 0) {
                    int h = mark.getB().getY() - mark.getA().getY();
                    mark.setA(new Point(mark.getA().getX(), HAUTEUR + 1));
                    mark.setB(new Point(mark.getB().getX(), HAUTEUR + 1 + h));
                }
            }

            long now = System.currentTimeMillis();
            long elapsedSec = (now - startTime) / 1000;
            int level = 1 + (int) (elapsedSec / 10);
            int speed = Math.min(24, 9 + level / 2);
            int spawnDelay = Math.max(240, 900 - 24 * level);

            levelTxt.setTexte("Level: " + level);

            for (int i = 0; i < LANES; i++) {
                laneCooldown[i] = Math.max(0, laneCooldown[i] - 20);
            }

            if (now - lastSpawn >= spawnDelay) {
                ArrayList<Integer> openLanes = new ArrayList<>();
                for (int lane = 0; lane < LANES; lane++) {
                    if (laneCooldown[lane] == 0) {
                        openLanes.add(lane);
                    }
                }

                if (!openLanes.isEmpty()) {
                    int lane = openLanes.get(rng.nextInt(openLanes.size()));
                    int cx = laneCenterX(lane);
                    int left = cx - CAR_W / 2;
                    int yBottom = HAUTEUR + 20;
                    Rectangle enemy = solidRect(rng.nextBoolean() ? Couleur.ROUGE : Couleur.BLEU, left, yBottom, left + CAR_W, yBottom + CAR_H);
                    obstacles.add(new Obstacle(enemy, speed));
                    f.ajouter(enemy);
                    laneCooldown[lane] = 340;
                }
                lastSpawn = now;
            }

            Iterator<Obstacle> it = obstacles.iterator();
            while (it.hasNext()) {
                Obstacle obs = it.next();
                obs.rect.translater(0, -obs.speed);

                if (intersects(player, obs.rect)) {
                    gameOver = true;
                    player.setCouleur(Couleur.ROUGE);
                    infoTxt.setTexte("CRASH! Appuie sur A pour rejouer");
                    if (score > bestScore) {
                        bestScore = score;
                        saveBestScore(bestScore);
                        bestTxt.setTexte("Best: " + bestScore);
                    }
                    break;
                }

                if (obs.rect.getB().getY() < 0) {
                    f.supprimer(obs.rect);
                    it.remove();
                    score++;
                }
            }

            scoreTxt.setTexte("Score: " + score);
            f.rafraichir();
        }

        System.exit(0);
    }
}
