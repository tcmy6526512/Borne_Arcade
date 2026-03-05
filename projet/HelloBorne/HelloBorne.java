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

    private static final int W = 1275;
    private static final int H = 1020;

    private static final int ARENA_LEFT = 120;
    private static final int ARENA_RIGHT = 1155;
    private static final int ARENA_BOTTOM = 90;
    private static final int ARENA_TOP = 930;

    private static final int PLAYER_W = 44;
    private static final int PLAYER_H = 58;
    private static final int ENEMY_SIZE = 40;

    private static final String SCORE_FILE = "projet/HelloBorne/highscore";

    private static class Enemy {
        double x;
        double y;
        int hp;
        Rectangle body;
        int lastDrawX;
        int lastDrawY;

        Enemy(double x, double y, Rectangle body) {
            this.x = x;
            this.y = y;
            this.hp = 2;
            this.body = body;
            this.lastDrawX = (int) x;
            this.lastDrawY = (int) y;
        }
    }

    private static class Effect {
        Rectangle shape;
        long endAt;

        Effect(Rectangle shape, long endAt) {
            this.shape = shape;
            this.endAt = endAt;
        }
    }

    private static Rectangle solidRect(Couleur c, int x1, int y1, int x2, int y2) {
        Rectangle r = new Rectangle(c, new Point(x1, y1), new Point(x2, y2));
        r.setPlein(true);
        return r;
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double clampDouble(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static void moveRectTo(Rectangle r, int left, int bottom) {
        int currentLeft = Math.min(r.getA().getX(), r.getB().getX());
        int currentBottom = Math.min(r.getA().getY(), r.getB().getY());
        r.translater(left - currentLeft, bottom - currentBottom);
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

    private static Enemy spawnEnemy(Random rng, int level) {
        int side = rng.nextInt(4);
        int x;
        int y;

        if (side == 0) {
            x = ARENA_LEFT + rng.nextInt(ARENA_RIGHT - ARENA_LEFT - ENEMY_SIZE);
            y = ARENA_TOP + 30;
        } else if (side == 1) {
            x = ARENA_LEFT + rng.nextInt(ARENA_RIGHT - ARENA_LEFT - ENEMY_SIZE);
            y = ARENA_BOTTOM - 30;
        } else if (side == 2) {
            x = ARENA_LEFT - 30;
            y = ARENA_BOTTOM + rng.nextInt(ARENA_TOP - ARENA_BOTTOM - ENEMY_SIZE);
        } else {
            x = ARENA_RIGHT + 30;
            y = ARENA_BOTTOM + rng.nextInt(ARENA_TOP - ARENA_BOTTOM - ENEMY_SIZE);
        }

        Couleur c = (level % 3 == 0) ? Couleur.ROUGE : ((level % 2 == 0) ? Couleur.ORANGE : Couleur.VERT);
        Rectangle body = solidRect(c, x, y, x + ENEMY_SIZE, y + ENEMY_SIZE);
        return new Enemy(x, y, body);
    }

    public static void main(String[] args) {
        FenetrePleinEcran f = new FenetrePleinEcran("Dungeon Blitz");
        f.setVisible(true);

        ClavierBorneArcade clavier = new ClavierBorneArcade();
        f.addKeyListener(clavier);
        if (f.getP() != null) {
            f.getP().addKeyListener(clavier);
            f.getP().requestFocusInWindow();
        }

        Rectangle bg = solidRect(Couleur.NOIR, 0, 0, W, H);
        Rectangle arena = solidRect(Couleur.GRIS_FONCE, ARENA_LEFT, ARENA_BOTTOM, ARENA_RIGHT, ARENA_TOP);
        Rectangle wallTop = solidRect(Couleur.GRIS, ARENA_LEFT - 18, ARENA_TOP, ARENA_RIGHT + 18, ARENA_TOP + 24);
        Rectangle wallBottom = solidRect(Couleur.GRIS, ARENA_LEFT - 18, ARENA_BOTTOM - 24, ARENA_RIGHT + 18, ARENA_BOTTOM);
        Rectangle wallLeft = solidRect(Couleur.GRIS, ARENA_LEFT - 24, ARENA_BOTTOM, ARENA_LEFT, ARENA_TOP);
        Rectangle wallRight = solidRect(Couleur.GRIS, ARENA_RIGHT, ARENA_BOTTOM, ARENA_RIGHT + 24, ARENA_TOP);

        ArrayList<Rectangle> floorTiles = new ArrayList<>();
        int tile = 70;
        for (int x = ARENA_LEFT; x < ARENA_RIGHT; x += tile) {
            for (int y = ARENA_BOTTOM; y < ARENA_TOP; y += tile) {
                Couleur c = (((x / tile) + (y / tile)) % 2 == 0) ? Couleur.GRIS_FONCE : Couleur.GRIS;
                Rectangle t = solidRect(c, x, y, Math.min(x + tile, ARENA_RIGHT), Math.min(y + tile, ARENA_TOP));
                floorTiles.add(t);
            }
        }

        Rectangle torch1 = solidRect(Couleur.ORANGE, ARENA_LEFT - 8, ARENA_TOP - 8, ARENA_LEFT + 8, ARENA_TOP + 8);
        Rectangle torch2 = solidRect(Couleur.ORANGE, ARENA_RIGHT - 8, ARENA_TOP - 8, ARENA_RIGHT + 8, ARENA_TOP + 8);
        Rectangle torch3 = solidRect(Couleur.ORANGE, ARENA_LEFT - 8, ARENA_BOTTOM - 8, ARENA_LEFT + 8, ARENA_BOTTOM + 8);
        Rectangle torch4 = solidRect(Couleur.ORANGE, ARENA_RIGHT - 8, ARENA_BOTTOM - 8, ARENA_RIGHT + 8, ARENA_BOTTOM + 8);

        int px = (ARENA_LEFT + ARENA_RIGHT) / 2 - PLAYER_W / 2;
        int py = (ARENA_BOTTOM + ARENA_TOP) / 2 - PLAYER_H / 2;
        Rectangle player = solidRect(Couleur.BLEU, px, py, px + PLAYER_W, py + PLAYER_H);
        Rectangle playerAura = solidRect(Couleur.CYAN, px - 4, py - 4, px + PLAYER_W + 4, py + PLAYER_H + 4);

        Font titleFont = new Font("Calibri", Font.BOLD, 40);
        Font hudFont = new Font("Calibri", Font.BOLD, 28);
        Font smallFont = new Font("Calibri", Font.PLAIN, 24);

        Texte title = new Texte(Couleur.BLANC, "DUNGEON BLITZ", titleFont, new Point(W / 2, 982));
        Texte scoreTxt = new Texte(Couleur.BLANC, "Score: 0", hudFont, new Point(130, 982));
        int bestScore = loadBestScore();
        Texte bestTxt = new Texte(Couleur.BLANC, "Best: " + bestScore, hudFont, new Point(1130, 982));
        Texte hpTxt = new Texte(Couleur.ROUGE, "HP: 5", hudFont, new Point(130, 944));
        Texte waveTxt = new Texte(Couleur.JAUNE, "Wave: 1", hudFont, new Point(W / 2, 944));
        Texte helpTxt = new Texte(Couleur.GRIS_CLAIR, "J1 stick: bouger | A: slash | B: dash | C: nova | Z: quitter", smallFont, new Point(W / 2, 42));
        Texte infoTxt = new Texte(Couleur.JAUNE, "", hudFont, new Point(W / 2, 520));

        f.ajouter(bg);
        for (Rectangle t : floorTiles) {
            f.ajouter(t);
        }
        f.ajouter(arena);
        f.ajouter(wallTop);
        f.ajouter(wallBottom);
        f.ajouter(wallLeft);
        f.ajouter(wallRight);
        f.ajouter(torch1);
        f.ajouter(torch2);
        f.ajouter(torch3);
        f.ajouter(torch4);
        f.ajouter(playerAura);
        f.ajouter(player);

        f.ajouter(title);
        f.ajouter(scoreTxt);
        f.ajouter(bestTxt);
        f.ajouter(hpTxt);
        f.ajouter(waveTxt);
        f.ajouter(helpTxt);
        f.ajouter(infoTxt);

        ArrayList<Enemy> enemies = new ArrayList<>();
        ArrayList<Effect> effects = new ArrayList<>();
        Random rng = new Random();

        long start = System.currentTimeMillis();
        long lastSpawn = start;
        long lastScoreTick = start;
        long lastAttack = 0;
        long lastDash = 0;
        long lastNova = 0;
        long lastHit = 0;

        double facingX = 0.0;
        double facingY = 1.0;

        int hp = 5;
        int score = 0;
        boolean gameOver = false;

        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            long now = System.currentTimeMillis();

            if (clavier.getBoutonJ1ZTape()) {
                break;
            }

            int wave = 1 + (int) ((now - start) / 20000);
            waveTxt.setTexte("Wave: " + wave);

            torch1.setCouleur((now / 180) % 2 == 0 ? Couleur.ORANGE : Couleur.JAUNE);
            torch2.setCouleur((now / 210) % 2 == 0 ? Couleur.ORANGE : Couleur.JAUNE);
            torch3.setCouleur((now / 160) % 2 == 0 ? Couleur.ORANGE : Couleur.JAUNE);
            torch4.setCouleur((now / 230) % 2 == 0 ? Couleur.ORANGE : Couleur.JAUNE);

            Iterator<Effect> itFx = effects.iterator();
            while (itFx.hasNext()) {
                Effect fx = itFx.next();
                if (now >= fx.endAt) {
                    f.supprimer(fx.shape);
                    itFx.remove();
                }
            }

            if (gameOver) {
                if (clavier.getBoutonJ1ATape()) {
                    for (Enemy e : enemies) {
                        f.supprimer(e.body);
                    }
                    enemies.clear();

                    for (Effect fx : effects) {
                        f.supprimer(fx.shape);
                    }
                    effects.clear();

                    px = (ARENA_LEFT + ARENA_RIGHT) / 2 - PLAYER_W / 2;
                    py = (ARENA_BOTTOM + ARENA_TOP) / 2 - PLAYER_H / 2;
                    moveRectTo(player, px, py);
                    moveRectTo(playerAura, px - 4, py - 4);

                    hp = 5;
                    score = 0;
                    start = now;
                    lastSpawn = now;
                    lastScoreTick = now;
                    lastAttack = 0;
                    lastDash = 0;
                    lastNova = 0;
                    lastHit = 0;
                    facingX = 0;
                    facingY = 1;
                    infoTxt.setTexte("");
                    hpTxt.setTexte("HP: 5");
                    scoreTxt.setTexte("Score: 0");
                    gameOver = false;
                }

                f.rafraichir();
                continue;
            }

            double mx = 0.0;
            double my = 0.0;
            if (clavier.getJoyJ1GaucheEnfoncee()) {
                mx -= 1.0;
            }
            if (clavier.getJoyJ1DroiteEnfoncee()) {
                mx += 1.0;
            }
            if (clavier.getJoyJ1HautEnfoncee()) {
                my += 1.0;
            }
            if (clavier.getJoyJ1BasEnfoncee()) {
                my -= 1.0;
            }

            double len = Math.sqrt(mx * mx + my * my);
            if (len > 0.0001) {
                mx /= len;
                my /= len;
                facingX = mx;
                facingY = my;
            }

            double speed = 5.0;
            if (now - lastDash < 120) {
                speed = 13.0;
            }

            if (clavier.getBoutonJ1BTape() && now - lastDash > 900) {
                lastDash = now;
            }

            px = (int) clampDouble(px + mx * speed, ARENA_LEFT, ARENA_RIGHT - PLAYER_W);
            py = (int) clampDouble(py + my * speed, ARENA_BOTTOM, ARENA_TOP - PLAYER_H);
            moveRectTo(player, px, py);
            moveRectTo(playerAura, px - 4, py - 4);

            if (clavier.getBoutonJ1ATape() && now - lastAttack > 240) {
                lastAttack = now;

                int cx = px + PLAYER_W / 2;
                int cy = py + PLAYER_H / 2;
                int slashX = cx + (int) (facingX * 48) - 30;
                int slashY = cy + (int) (facingY * 48) - 30;
                Rectangle slash = solidRect(Couleur.JAUNE, slashX, slashY, slashX + 60, slashY + 60);
                f.ajouter(slash);
                effects.add(new Effect(slash, now + 110));

                Iterator<Enemy> itE = enemies.iterator();
                while (itE.hasNext()) {
                    Enemy e = itE.next();
                    int ex = (int) e.x + ENEMY_SIZE / 2;
                    int ey = (int) e.y + ENEMY_SIZE / 2;
                    double dx = ex - cx;
                    double dy = ey - cy;
                    double d = Math.sqrt(dx * dx + dy * dy);
                    if (d <= 95) {
                        double dot = (dx * facingX + dy * facingY) / (d + 0.0001);
                        if (dot > 0.05) {
                            e.hp -= 1;
                            e.body.setCouleur(Couleur.ROUGE);
                            if (e.hp <= 0) {
                                f.supprimer(e.body);
                                itE.remove();
                                score += 20;
                            }
                        }
                    }
                }
            }

            if (clavier.getBoutonJ1CTape() && now - lastNova > 3000) {
                lastNova = now;
                int cx = px + PLAYER_W / 2;
                int cy = py + PLAYER_H / 2;
                Rectangle nova = solidRect(Couleur.CYAN, cx - 120, cy - 120, cx + 120, cy + 120);
                f.ajouter(nova);
                effects.add(new Effect(nova, now + 100));

                Iterator<Enemy> itE = enemies.iterator();
                while (itE.hasNext()) {
                    Enemy e = itE.next();
                    int ex = (int) e.x + ENEMY_SIZE / 2;
                    int ey = (int) e.y + ENEMY_SIZE / 2;
                    double dx = ex - cx;
                    double dy = ey - cy;
                    if (Math.sqrt(dx * dx + dy * dy) <= 140) {
                        f.supprimer(e.body);
                        itE.remove();
                        score += 25;
                    }
                }
            }

            int maxEnemies = 4 + wave * 2;
            int spawnDelay = Math.max(220, 1100 - wave * 55);
            if (enemies.size() < maxEnemies && now - lastSpawn >= spawnDelay) {
                Enemy e = spawnEnemy(rng, wave);
                enemies.add(e);
                f.ajouter(e.body);
                lastSpawn = now;
            }

            double enemySpeed = Math.min(4.8, 1.6 + 0.18 * wave);
            for (Enemy e : enemies) {
                double pcx = px + PLAYER_W / 2.0;
                double pcy = py + PLAYER_H / 2.0;
                double ecx = e.x + ENEMY_SIZE / 2.0;
                double ecy = e.y + ENEMY_SIZE / 2.0;

                double dx = pcx - ecx;
                double dy = pcy - ecy;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d > 0.001) {
                    dx /= d;
                    dy /= d;
                }

                e.x += dx * enemySpeed;
                e.y += dy * enemySpeed;

                int nx = (int) e.x;
                int ny = (int) e.y;
                e.body.translater(nx - e.lastDrawX, ny - e.lastDrawY);
                e.lastDrawX = nx;
                e.lastDrawY = ny;
            }

            if (now - lastHit >= 650) {
                for (Enemy e : enemies) {
                    if (intersects(player, e.body)) {
                        hp -= 1;
                        hpTxt.setTexte("HP: " + hp);
                        lastHit = now;
                        Rectangle hitFx = solidRect(Couleur.ROUGE, px - 10, py - 10, px + PLAYER_W + 10, py + PLAYER_H + 10);
                        f.ajouter(hitFx);
                        effects.add(new Effect(hitFx, now + 100));
                        break;
                    }
                }
            }

            while (now - lastScoreTick >= 250) {
                score += 1;
                lastScoreTick += 250;
            }

            scoreTxt.setTexte("Score: " + score);

            if (hp <= 0) {
                gameOver = true;
                if (score > bestScore) {
                    bestScore = score;
                    saveBestScore(bestScore);
                    bestTxt.setTexte("Best: " + bestScore);
                }
                infoTxt.setTexte("GAME OVER - A pour rejouer");
            }

            f.rafraichir();
        }

        System.exit(0);
    }
}
