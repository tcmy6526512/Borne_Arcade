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

    private static final int LARGEUR = 1280;
    private static final int HAUTEUR = 1024;
    private static final int MARGE = 20;
    private static final int TAILLE_JOUEUR = 70;
    private static final int VITESSE_JOUEUR = 11;
    private static final String SCORE_FILE = "projet/HelloBorne/highscore";

    private static class Obstacle {
        Rectangle shape;
        int speed;

        Obstacle(Rectangle shape, int speed) {
            this.shape = shape;
            this.speed = speed;
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
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

    private static Obstacle buildObstacle(Random rng) {
        int size = 45 + rng.nextInt(65);
        int x = MARGE + rng.nextInt(LARGEUR - 2 * MARGE - size);
        int speed = 6 + rng.nextInt(7);
        Rectangle r = new Rectangle(Couleur.ROUGE, new Point(x, HAUTEUR + size), size, size, true);
        return new Obstacle(r, speed);
    }

    public static void main(String[] args) {
        FenetrePleinEcran fen = new FenetrePleinEcran("Comet Rush");
        ClavierBorneArcade clavier = new ClavierBorneArcade();
        fen.addKeyListener(clavier);
        fen.getP().addKeyListener(clavier);

        Rectangle fond = new Rectangle(Couleur.GRIS_FONCE, new Point(0, 0), LARGEUR, HAUTEUR, true);
        Rectangle zone = new Rectangle(Couleur.NOIR, new Point(MARGE, MARGE), LARGEUR - 2 * MARGE, HAUTEUR - 2 * MARGE, true);
        Rectangle joueur = new Rectangle(Couleur.VERT, new Point(LARGEUR / 2 - TAILLE_JOUEUR / 2, 90), TAILLE_JOUEUR, TAILLE_JOUEUR, true);

        Font fTitle = new Font("Calibri", Font.BOLD, 42);
        Font fMain = new Font("Calibri", Font.BOLD, 30);
        Font fHelp = new Font("Calibri", Font.PLAIN, 22);

        Texte titre = new Texte(Couleur.BLANC, "COMET RUSH", fTitle, new Point(640, 985));
        Texte scoreTxt = new Texte(Couleur.BLANC, "Score: 0", fMain, new Point(170, 980));
        int bestScore = loadBestScore();
        Texte bestTxt = new Texte(Couleur.BLANC, "Best: " + bestScore, fMain, new Point(1110, 980));
        Texte help1 = new Texte(Couleur.GRIS_CLAIR, "J1 joystick: bouger", fHelp, new Point(220, 45));
        Texte help2 = new Texte(Couleur.GRIS_CLAIR, "A: rejouer", fHelp, new Point(620, 45));
        Texte help3 = new Texte(Couleur.GRIS_CLAIR, "Z: quitter", fHelp, new Point(1000, 45));
        Texte info = new Texte(Couleur.JAUNE, "", fMain, new Point(640, 520));

        fen.ajouter(fond);
        fen.ajouter(zone);
        fen.ajouter(joueur);
        fen.ajouter(titre);
        fen.ajouter(scoreTxt);
        fen.ajouter(bestTxt);
        fen.ajouter(help1);
        fen.ajouter(help2);
        fen.ajouter(help3);
        fen.ajouter(info);

        ArrayList<Obstacle> obstacles = new ArrayList<>();
        Random rng = new Random();

        long lastSpawn = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        int spawnDelayMs = 900;
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
                        fen.supprimer(obs.shape);
                    }
                    obstacles.clear();
                    score = 0;
                    lastSpawn = System.currentTimeMillis();
                    startTime = System.currentTimeMillis();
                    spawnDelayMs = 900;
                    joueur.setA(new Point(LARGEUR / 2 - TAILLE_JOUEUR / 2, 90));
                    joueur.setB(new Point(LARGEUR / 2 + TAILLE_JOUEUR / 2, 90 + TAILLE_JOUEUR));
                    info.setTexte("");
                    scoreTxt.setTexte("Score: 0");
                    gameOver = false;
                }
                fen.rafraichir();
                continue;
            }

            int dx = 0;
            int dy = 0;
            if (clavier.getJoyJ1GaucheEnfoncee()) dx -= VITESSE_JOUEUR;
            if (clavier.getJoyJ1DroiteEnfoncee()) dx += VITESSE_JOUEUR;
            if (clavier.getJoyJ1HautEnfoncee()) dy += VITESSE_JOUEUR;
            if (clavier.getJoyJ1BasEnfoncee()) dy -= VITESSE_JOUEUR;

            int nx = clamp(joueur.getA().getX() + dx, MARGE + 2, LARGEUR - MARGE - 2 - TAILLE_JOUEUR);
            int ny = clamp(joueur.getA().getY() + dy, MARGE + 2, HAUTEUR - MARGE - 2 - TAILLE_JOUEUR);
            joueur.setA(new Point(nx, ny));
            joueur.setB(new Point(nx + TAILLE_JOUEUR, ny + TAILLE_JOUEUR));

            long now = System.currentTimeMillis();
            long elapsedSeconds = (now - startTime) / 1000;
            spawnDelayMs = Math.max(260, 900 - (int) (elapsedSeconds * 10));

            if (now - lastSpawn >= spawnDelayMs) {
                Obstacle obs = buildObstacle(rng);
                obstacles.add(obs);
                fen.ajouter(obs.shape);
                lastSpawn = now;
            }

            Iterator<Obstacle> it = obstacles.iterator();
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
                    info.setTexte("GAME OVER - A pour rejouer");
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
        }

        System.exit(0);
    }
}
