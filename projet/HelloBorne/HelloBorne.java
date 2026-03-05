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

    private static final int PLAYER_W = 54;
    private static final int PLAYER_H = 72;
    private static final int ENEMY_W = 48;
    private static final int ENEMY_H = 52;

    private static final double PLAYER_SPEED = 5.2;
    private static final double DASH_SPEED = 12.0;

    private static final int DIR_UP = 0;
    private static final int DIR_RIGHT = 1;
    private static final int DIR_DOWN = 2;
    private static final int DIR_LEFT = 3;

    private static final String SCORE_FILE = "projet/HelloBorne/highscore";

    private static class Player {
        double x;
        double y;
        Rectangle hitbox;
        Rectangle body;
        Rectangle cape;
        Rectangle head;
        Rectangle eyeL;
        Rectangle eyeR;
        Rectangle swordGuard;
        Rectangle swordBlade;
        int dir;

        Player(double x, double y) {
            this.x = x;
            this.y = y;
            this.dir = DIR_UP;

            this.hitbox = rect(0, 0, PLAYER_W, PLAYER_H, Couleur.NOIR);
            this.body = rect(0, 0, PLAYER_W, 42, Couleur.BLEU);
            this.cape = rect(0, 0, PLAYER_W + 8, 16, Couleur.ROUGE);
            this.head = rect(0, 0, 34, 26, Couleur.JAUNE);
            this.eyeL = rect(0, 0, 6, 6, Couleur.NOIR);
            this.eyeR = rect(0, 0, 6, 6, Couleur.NOIR);
            this.swordGuard = rect(0, 0, 18, 8, Couleur.GRIS_CLAIR);
            this.swordBlade = rect(0, 0, 14, 34, Couleur.BLANC);
            updateVisual();
        }

        void translate(int dx, int dy) {
            x += dx;
            y += dy;
            hitbox.translater(dx, dy);
            body.translater(dx, dy);
            cape.translater(dx, dy);
            head.translater(dx, dy);
            eyeL.translater(dx, dy);
            eyeR.translater(dx, dy);
            swordGuard.translater(dx, dy);
            swordBlade.translater(dx, dy);
        }

        void setPosition(int newX, int newY) {
            int oldX = left(hitbox);
            int oldY = bottom(hitbox);
            translate(newX - oldX, newY - oldY);
        }

        void updateVisual() {
            int x0 = (int) x;
            int y0 = (int) y;

            moveTo(hitbox, x0, y0);
            moveTo(body, x0, y0 + 12);
            moveTo(cape, x0 - 4, y0 + 4);
            moveTo(head, x0 + 10, y0 + 44);
            moveTo(eyeL, x0 + 16, y0 + 58);
            moveTo(eyeR, x0 + 28, y0 + 58);

            if (dir == DIR_UP) {
                moveTo(swordGuard, x0 + 18, y0 + 62);
                moveTo(swordBlade, x0 + 21, y0 + 70);
            } else if (dir == DIR_DOWN) {
                moveTo(swordGuard, x0 + 18, y0 + 10);
                moveTo(swordBlade, x0 + 21, y0 - 26);
            } else if (dir == DIR_RIGHT) {
                moveTo(swordGuard, x0 + 50, y0 + 32);
                moveTo(swordBlade, x0 + 66, y0 + 20);
            } else {
                moveTo(swordGuard, x0 - 14, y0 + 32);
                moveTo(swordBlade, x0 - 26, y0 + 20);
            }
        }

        Rectangle[] drawable() {
            return new Rectangle[] {cape, body, head, eyeL, eyeR, swordGuard, swordBlade};
        }
    }

    private static class Enemy {
        double x;
        double y;
        double speed;
        int hp;
        Rectangle hitbox;
        Rectangle body;
        Rectangle eyeL;
        Rectangle eyeR;
        Rectangle jaw;

        Enemy(double x, double y, double speed, int hp, Couleur bodyColor) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.hp = hp;

            this.hitbox = rect((int) x, (int) y, ENEMY_W, ENEMY_H, Couleur.NOIR);
            this.body = rect((int) x, (int) y, ENEMY_W, ENEMY_H, bodyColor);
            this.eyeL = rect((int) x + 10, (int) y + 34, 8, 8, Couleur.ROUGE);
            this.eyeR = rect((int) x + 30, (int) y + 34, 8, 8, Couleur.ROUGE);
            this.jaw = rect((int) x + 12, (int) y + 10, 24, 8, Couleur.BLANC);
        }

        void translate(int dx, int dy) {
            x += dx;
            y += dy;
            hitbox.translater(dx, dy);
            body.translater(dx, dy);
            eyeL.translater(dx, dy);
            eyeR.translater(dx, dy);
            jaw.translater(dx, dy);
        }

        Rectangle[] drawable() {
            return new Rectangle[] {body, eyeL, eyeR, jaw};
        }
    }

    private static class Obstacle {
        Rectangle collider;
        Rectangle box;
        Rectangle plankH;
        Rectangle plankV;

        Obstacle(int x, int y, int w, int h) {
            this.collider = rect(x, y, w, h, Couleur.NOIR);
            this.box = rect(x, y, w, h, Couleur.ORANGE);
            this.plankH = rect(x + 4, y + h / 2 - 3, w - 8, 6, Couleur.NOIR);
            this.plankV = rect(x + w / 2 - 3, y + 4, 6, h - 8, Couleur.NOIR);
        }

        Rectangle[] drawable() {
            return new Rectangle[] {box, plankH, plankV};
        }
    }

    private static class Fx {
        Rectangle shape;
        long endAt;

        Fx(Rectangle shape, long endAt) {
            this.shape = shape;
            this.endAt = endAt;
        }
    }

    private static Rectangle rect(int x, int y, int w, int h, Couleur c) {
        Rectangle r = new Rectangle(c, new Point(x, y), new Point(x + w, y + h));
        r.setPlein(true);
        return r;
    }

    private static void moveTo(Rectangle r, int x, int y) {
        r.translater(x - left(r), y - bottom(r));
    }

    private static int left(Rectangle r) {
        return Math.min(r.getA().getX(), r.getB().getX());
    }

    private static int right(Rectangle r) {
        return Math.max(r.getA().getX(), r.getB().getX());
    }

    private static int bottom(Rectangle r) {
        return Math.min(r.getA().getY(), r.getB().getY());
    }

    private static int top(Rectangle r) {
        return Math.max(r.getA().getY(), r.getB().getY());
    }

    private static boolean intersects(Rectangle a, Rectangle b) {
        return left(a) < right(b) && right(a) > left(b) && bottom(a) < top(b) && top(a) > bottom(b);
    }

    private static boolean insideArena(Rectangle r) {
        return left(r) >= ARENA_LEFT && right(r) <= ARENA_RIGHT && bottom(r) >= ARENA_BOTTOM && top(r) <= ARENA_TOP;
    }

    private static boolean collidesObstacle(Rectangle r, ArrayList<Obstacle> obstacles) {
        for (Obstacle o : obstacles) {
            if (intersects(r, o.collider)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryMovePlayer(Player p, int dx, int dy, ArrayList<Obstacle> obstacles) {
        boolean moved = false;

        if (dx != 0) {
            p.translate(dx, 0);
            if (!insideArena(p.hitbox) || collidesObstacle(p.hitbox, obstacles)) {
                p.translate(-dx, 0);
            } else {
                moved = true;
            }
        }

        if (dy != 0) {
            p.translate(0, dy);
            if (!insideArena(p.hitbox) || collidesObstacle(p.hitbox, obstacles)) {
                p.translate(0, -dy);
            } else {
                moved = true;
            }
        }

        return moved;
    }

    private static boolean tryMoveEnemy(Enemy e, int dx, int dy, ArrayList<Obstacle> obstacles) {
        boolean moved = false;

        if (dx != 0) {
            e.translate(dx, 0);
            if (!insideArena(e.hitbox) || collidesObstacle(e.hitbox, obstacles)) {
                e.translate(-dx, 0);
            } else {
                moved = true;
            }
        }

        if (dy != 0) {
            e.translate(0, dy);
            if (!insideArena(e.hitbox) || collidesObstacle(e.hitbox, obstacles)) {
                e.translate(0, -dy);
            } else {
                moved = true;
            }
        }

        return moved;
    }

    private static Enemy spawnEnemy(Random rng, int wave) {
        int side = rng.nextInt(4);
        int x;
        int y;

        if (side == 0) {
            x = ARENA_LEFT + rng.nextInt(ARENA_RIGHT - ARENA_LEFT - ENEMY_W);
            y = ARENA_TOP - ENEMY_H;
        } else if (side == 1) {
            x = ARENA_LEFT + rng.nextInt(ARENA_RIGHT - ARENA_LEFT - ENEMY_W);
            y = ARENA_BOTTOM;
        } else if (side == 2) {
            x = ARENA_LEFT;
            y = ARENA_BOTTOM + rng.nextInt(ARENA_TOP - ARENA_BOTTOM - ENEMY_H);
        } else {
            x = ARENA_RIGHT - ENEMY_W;
            y = ARENA_BOTTOM + rng.nextInt(ARENA_TOP - ARENA_BOTTOM - ENEMY_H);
        }

        double speed = Math.min(4.6, 1.4 + wave * 0.2);
        int hp = 2 + wave / 3;
        Couleur c = (wave % 3 == 0) ? Couleur.BLEU : (wave % 2 == 0 ? Couleur.ROUGE : Couleur.VERT);
        return new Enemy(x, y, speed, hp, c);
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

    public static void main(String[] args) {
        FenetrePleinEcran f = new FenetrePleinEcran("Dungeon Blitz");
        f.setVisible(true);

        ClavierBorneArcade clavier = new ClavierBorneArcade();
        f.addKeyListener(clavier);
        if (f.getP() != null) {
            f.getP().addKeyListener(clavier);
            f.getP().requestFocusInWindow();
        }

        Rectangle bg = rect(0, 0, W, H, Couleur.NOIR);
        Rectangle arena = rect(ARENA_LEFT, ARENA_BOTTOM, ARENA_RIGHT - ARENA_LEFT, ARENA_TOP - ARENA_BOTTOM, Couleur.GRIS_FONCE);
        Rectangle wallTop = rect(ARENA_LEFT - 20, ARENA_TOP, ARENA_RIGHT - ARENA_LEFT + 40, 24, Couleur.GRIS);
        Rectangle wallBottom = rect(ARENA_LEFT - 20, ARENA_BOTTOM - 24, ARENA_RIGHT - ARENA_LEFT + 40, 24, Couleur.GRIS);
        Rectangle wallLeft = rect(ARENA_LEFT - 24, ARENA_BOTTOM, 24, ARENA_TOP - ARENA_BOTTOM, Couleur.GRIS);
        Rectangle wallRight = rect(ARENA_RIGHT, ARENA_BOTTOM, 24, ARENA_TOP - ARENA_BOTTOM, Couleur.GRIS);

        ArrayList<Rectangle> floorTiles = new ArrayList<>();
        int tile = 70;
        for (int x = ARENA_LEFT; x < ARENA_RIGHT; x += tile) {
            for (int y = ARENA_BOTTOM; y < ARENA_TOP; y += tile) {
                Couleur c = (((x / tile) + (y / tile)) % 2 == 0) ? Couleur.GRIS_FONCE : Couleur.GRIS;
                floorTiles.add(rect(x, y, Math.min(tile, ARENA_RIGHT - x), Math.min(tile, ARENA_TOP - y), c));
            }
        }

        ArrayList<Obstacle> obstacles = new ArrayList<>();
        obstacles.add(new Obstacle(250, 210, 86, 76));
        obstacles.add(new Obstacle(430, 260, 92, 80));
        obstacles.add(new Obstacle(590, 395, 90, 84));
        obstacles.add(new Obstacle(760, 280, 106, 82));
        obstacles.add(new Obstacle(940, 240, 86, 76));
        obstacles.add(new Obstacle(340, 640, 92, 80));
        obstacles.add(new Obstacle(540, 742, 96, 82));
        obstacles.add(new Obstacle(760, 625, 92, 80));
        obstacles.add(new Obstacle(915, 706, 96, 82));

        int startX = (ARENA_LEFT + ARENA_RIGHT) / 2 - PLAYER_W / 2;
        int startY = (ARENA_BOTTOM + ARENA_TOP) / 2 - PLAYER_H / 2;
        Player player = new Player(startX, startY);

        Font titleFont = new Font("Calibri", Font.BOLD, 40);
        Font hudFont = new Font("Calibri", Font.BOLD, 28);
        Font smallFont = new Font("Calibri", Font.PLAIN, 24);

        Texte title = new Texte(Couleur.BLANC, "DUNGEON BLITZ", titleFont, new Point(W / 2, 982));
        Texte scoreTxt = new Texte(Couleur.BLANC, "Score: 0", hudFont, new Point(130, 982));
        int bestScore = loadBestScore();
        Texte bestTxt = new Texte(Couleur.BLANC, "Best: " + bestScore, hudFont, new Point(1130, 982));
        Texte hpTxt = new Texte(Couleur.ROUGE, "HP: 6", hudFont, new Point(130, 944));
        Texte waveTxt = new Texte(Couleur.JAUNE, "Wave: 1", hudFont, new Point(W / 2, 944));
        Texte coolTxt = new Texte(Couleur.CYAN, "Dash(B): ready | Nova(C): ready", smallFont, new Point(W / 2, 908));
        Texte helpTxt = new Texte(Couleur.GRIS_CLAIR, "J1 stick: bouger | A: epee longue | B: dash | C: nova | Z: quitter", smallFont, new Point(W / 2, 42));
        Texte infoTxt = new Texte(Couleur.JAUNE, "", hudFont, new Point(W / 2, 520));

        f.ajouter(bg);
        f.ajouter(arena);
        for (Rectangle t : floorTiles) {
            f.ajouter(t);
        }
        f.ajouter(wallTop);
        f.ajouter(wallBottom);
        f.ajouter(wallLeft);
        f.ajouter(wallRight);

        for (Obstacle o : obstacles) {
            for (Rectangle r : o.drawable()) {
                f.ajouter(r);
            }
        }

        for (Rectangle r : player.drawable()) {
            f.ajouter(r);
        }

        f.ajouter(title);
        f.ajouter(scoreTxt);
        f.ajouter(bestTxt);
        f.ajouter(hpTxt);
        f.ajouter(waveTxt);
        f.ajouter(coolTxt);
        f.ajouter(helpTxt);
        f.ajouter(infoTxt);

        ArrayList<Enemy> enemies = new ArrayList<>();
        ArrayList<Fx> effects = new ArrayList<>();
        Random rng = new Random();

        long start = System.currentTimeMillis();
        long lastSpawn = start;
        long lastScoreTick = start;
        long lastAttack = 0;
        long lastDash = 0;
        long lastNova = 0;
        long lastHit = 0;

        int hp = 6;
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

            int wave = 1 + (int) ((now - start) / 18000);
            waveTxt.setTexte("Wave: " + wave);

            long dashCd = Math.max(520, 900 - wave * 12);
            long novaCd = 3000;
            String dashStatus = (now - lastDash >= dashCd) ? "ready" : ("" + ((dashCd - (now - lastDash)) / 1000 + 1) + "s");
            String novaStatus = (now - lastNova >= novaCd) ? "ready" : ("" + ((novaCd - (now - lastNova)) / 1000 + 1) + "s");
            coolTxt.setTexte("Dash(B): " + dashStatus + " | Nova(C): " + novaStatus);

            Iterator<Fx> itFx = effects.iterator();
            while (itFx.hasNext()) {
                Fx fx = itFx.next();
                if (now >= fx.endAt) {
                    f.supprimer(fx.shape);
                    itFx.remove();
                }
            }

            if (gameOver) {
                if (clavier.getBoutonJ1ATape()) {
                    for (Enemy e : enemies) {
                        for (Rectangle r : e.drawable()) {
                            f.supprimer(r);
                        }
                    }
                    enemies.clear();

                    for (Fx fx : effects) {
                        f.supprimer(fx.shape);
                    }
                    effects.clear();

                    int resetX = (ARENA_LEFT + ARENA_RIGHT) / 2 - PLAYER_W / 2;
                    int resetY = (ARENA_BOTTOM + ARENA_TOP) / 2 - PLAYER_H / 2;
                    player.setPosition(resetX, resetY);
                    player.dir = DIR_UP;
                    player.updateVisual();

                    hp = 6;
                    score = 0;
                    start = now;
                    lastSpawn = now;
                    lastScoreTick = now;
                    lastAttack = 0;
                    lastDash = 0;
                    lastNova = 0;
                    lastHit = 0;

                    hpTxt.setTexte("HP: 6");
                    scoreTxt.setTexte("Score: 0");
                    infoTxt.setTexte("");
                    gameOver = false;
                }

                f.rafraichir();
                continue;
            }

            double mx = 0.0;
            double my = 0.0;
            if (clavier.getJoyJ1GaucheEnfoncee()) {
                mx -= 1.0;
                player.dir = DIR_LEFT;
            }
            if (clavier.getJoyJ1DroiteEnfoncee()) {
                mx += 1.0;
                player.dir = DIR_RIGHT;
            }
            if (clavier.getJoyJ1HautEnfoncee()) {
                my += 1.0;
                player.dir = DIR_UP;
            }
            if (clavier.getJoyJ1BasEnfoncee()) {
                my -= 1.0;
                player.dir = DIR_DOWN;
            }

            double norm = Math.sqrt(mx * mx + my * my);
            if (norm > 0.0001) {
                mx /= norm;
                my /= norm;
            }

            if (clavier.getBoutonJ1BTape() && now - lastDash >= dashCd) {
                lastDash = now;
            }

            double speed = (now - lastDash < 140) ? DASH_SPEED : PLAYER_SPEED;
            int dx = (int) Math.round(mx * speed);
            int dy = (int) Math.round(my * speed);
            tryMovePlayer(player, dx, dy, obstacles);
            player.updateVisual();

            if (clavier.getBoutonJ1ATape() && now - lastAttack >= 210) {
                lastAttack = now;

                int cx = left(player.hitbox) + PLAYER_W / 2;
                int cy = bottom(player.hitbox) + PLAYER_H / 2;
                Rectangle slash;

                if (player.dir == DIR_UP) {
                    slash = rect(cx - 58, cy + 18, 116, ATTACK_RANGE, Couleur.JAUNE);
                } else if (player.dir == DIR_DOWN) {
                    slash = rect(cx - 58, cy - ATTACK_RANGE - 18, 116, ATTACK_RANGE, Couleur.JAUNE);
                } else if (player.dir == DIR_RIGHT) {
                    slash = rect(cx + 18, cy - 58, ATTACK_RANGE, 116, Couleur.JAUNE);
                } else {
                    slash = rect(cx - ATTACK_RANGE - 18, cy - 58, ATTACK_RANGE, 116, Couleur.JAUNE);
                }

                f.ajouter(slash);
                effects.add(new Fx(slash, now + 100));

                Iterator<Enemy> it = enemies.iterator();
                while (it.hasNext()) {
                    Enemy e = it.next();
                    if (intersects(slash, e.hitbox)) {
                        e.hp -= 1;
                        e.body.setCouleur(Couleur.ORANGE);
                        if (e.hp <= 0) {
                            for (Rectangle r : e.drawable()) {
                                f.supprimer(r);
                            }
                            it.remove();
                            score += 24;
                        }
                    }
                }
            }

            if (clavier.getBoutonJ1CTape() && now - lastNova >= novaCd) {
                lastNova = now;
                int cx = left(player.hitbox) + PLAYER_W / 2;
                int cy = bottom(player.hitbox) + PLAYER_H / 2;
                Rectangle nova = rect(cx - 175, cy - 175, 350, 350, Couleur.CYAN);
                f.ajouter(nova);
                effects.add(new Fx(nova, now + 110));

                Iterator<Enemy> it = enemies.iterator();
                while (it.hasNext()) {
                    Enemy e = it.next();
                    if (intersects(nova, e.hitbox)) {
                        for (Rectangle r : e.drawable()) {
                            f.supprimer(r);
                        }
                        it.remove();
                        score += 30;
                    }
                }
            }

            int maxEnemies = 4 + wave * 2;
            int spawnDelay = Math.max(210, 980 - wave * 46);
            if (enemies.size() < maxEnemies && now - lastSpawn >= spawnDelay) {
                Enemy e = spawnEnemy(rng, wave);
                enemies.add(e);
                for (Rectangle r : e.drawable()) {
                    f.ajouter(r);
                }
                lastSpawn = now;
            }

            for (Enemy e : enemies) {
                double pxCenter = left(player.hitbox) + PLAYER_W / 2.0;
                double pyCenter = bottom(player.hitbox) + PLAYER_H / 2.0;
                double exCenter = left(e.hitbox) + ENEMY_W / 2.0;
                double eyCenter = bottom(e.hitbox) + ENEMY_H / 2.0;

                double tx = pxCenter - exCenter;
                double ty = pyCenter - eyCenter;
                double d = Math.sqrt(tx * tx + ty * ty);
                if (d > 0.001) {
                    tx /= d;
                    ty /= d;
                }

                int mdx = (int) Math.round(tx * e.speed);
                int mdy = (int) Math.round(ty * e.speed);
                boolean moved = tryMoveEnemy(e, mdx, mdy, obstacles);
                if (!moved) {
                    int sdx = rng.nextBoolean() ? 2 : -2;
                    int sdy = rng.nextBoolean() ? 2 : -2;
                    tryMoveEnemy(e, sdx, sdy, obstacles);
                }
            }

            if (now - lastHit >= 620) {
                for (Enemy e : enemies) {
                    if (intersects(player.hitbox, e.hitbox)) {
                        hp -= 1;
                        hpTxt.setTexte("HP: " + hp);
                        lastHit = now;
                        Rectangle hitFx = rect(left(player.hitbox) - 12, bottom(player.hitbox) - 12, PLAYER_W + 24, PLAYER_H + 24, Couleur.ROUGE);
                        f.ajouter(hitFx);
                        effects.add(new Fx(hitFx, now + 110));
                        break;
                    }
                }
            }

            while (now - lastScoreTick >= 220) {
                score += 1;
                lastScoreTick += 220;
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
