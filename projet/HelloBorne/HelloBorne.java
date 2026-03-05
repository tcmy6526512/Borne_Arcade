import MG2D.Couleur;
import MG2D.FenetrePleinEcran;
import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texte;
import MG2D.geometrie.Texture;
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

    private static final int PLAYER_W = 66;
    private static final int PLAYER_H = 82;
    private static final int ENEMY_W = 62;
    private static final int ENEMY_H = 62;

    private static final double BASE_PLAYER_SPEED = 5.4;
    private static final double DASH_SPEED = 13.5;

    private static final int ATTACK_RANGE = 180;
    private static final int ATTACK_ARC_HALF_DEG = 70;

    private static final String SCORE_FILE = "projet/HelloBorne/highscore";

    private static final String TEX_OUTSIDE_BG = "projet/InitialDrift/decor/accueil.jpg";
    private static final String TEX_ARENA_BG = "projet/CursedWare/minigames/TEST_GAME/assets/Background.png";
    private static final String TEX_PLAYER = "projet/CursedWare/minigames/BroForce/assets/bro.png";
    private static final String TEX_MONSTER_A = "projet/CursedWare/minigames/TEST_GAME/assets/creature-sheet.png";
    private static final String TEX_MONSTER_B = "projet/DinoRail/assets/img/bird.png";
    private static final String TEX_SWORD = "projet/JavaSpace/img/laser/player1/0.png";
    private static final String TEX_CRATE_A = "projet/InitialDrift/decor/Tonneau.png";
    private static final String TEX_CRATE_B = "projet/InitialDrift/decor/obj_bags2.png";
    private static final String TEX_CRATE_C = "projet/InitialDrift/decor/mursac.png";
    private static final String TEX_FIRE = "projet/InitialDrift/img/explosion.png";

    private static class Entity {
        double x;
        double y;
        int w;
        int h;
        Rectangle hitbox;
        Texture sprite;
        int lastDrawX;
        int lastDrawY;

        Entity(double x, double y, int w, int h, Rectangle hitbox, Texture sprite) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.hitbox = hitbox;
            this.sprite = sprite;
            this.lastDrawX = (int) x;
            this.lastDrawY = (int) y;
        }
    }

    private static class Enemy extends Entity {
        int hp;
        double speed;

        Enemy(double x, double y, int w, int h, Rectangle hitbox, Texture sprite, int hp, double speed) {
            super(x, y, w, h, hitbox, sprite);
            this.hp = hp;
            this.speed = speed;
        }
    }

    private static class Blocker {
        Rectangle hitbox;
        Texture sprite;

        Blocker(Rectangle hitbox, Texture sprite) {
            this.hitbox = hitbox;
            this.sprite = sprite;
        }
    }

    private static class EffectRect {
        Rectangle rect;
        long endAt;

        EffectRect(Rectangle rect, long endAt) {
            this.rect = rect;
            this.endAt = endAt;
        }
    }

    private static class EffectTex {
        Texture tex;
        long endAt;

        EffectTex(Texture tex, long endAt) {
            this.tex = tex;
            this.endAt = endAt;
        }
    }

    private static Rectangle solidRect(Couleur c, int x1, int y1, int x2, int y2) {
        Rectangle r = new Rectangle(c, new Point(x1, y1), new Point(x2, y2));
        r.setPlein(true);
        return r;
    }

    private static Rectangle buildHitbox(int x, int y, int w, int h) {
        Rectangle r = new Rectangle(Couleur.NOIR, new Point(x, y), new Point(x + w, y + h));
        r.setPlein(true);
        return r;
    }

    private static Texture buildTextureOrNull(String path, int x, int y, int w, int h) {
        try {
            File f = new File(path);
            if (f.exists()) {
                return new Texture(path, new Point(x, y), w, h);
            }
        } catch (Exception e) {
            System.err.println("Texture invalide: " + path + " -> " + e.getMessage());
        }
        return null;
    }

    private static void moveRectTo(Rectangle r, int left, int bottom) {
        int cx = Math.min(r.getA().getX(), r.getB().getX());
        int cy = Math.min(r.getA().getY(), r.getB().getY());
        r.translater(left - cx, bottom - cy);
    }

    private static void moveTextureTo(Texture t, int left, int bottom) {
        if (t == null) {
            return;
        }
        Rectangle box = t.getBoiteEnglobante();
        int cx = Math.min(box.getA().getX(), box.getB().getX());
        int cy = Math.min(box.getA().getY(), box.getB().getY());
        t.translater(left - cx, bottom - cy);
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

    private static boolean insideArena(Rectangle r) {
        int left = Math.min(r.getA().getX(), r.getB().getX());
        int right = Math.max(r.getA().getX(), r.getB().getX());
        int bottom = Math.min(r.getA().getY(), r.getB().getY());
        int top = Math.max(r.getA().getY(), r.getB().getY());

        return left >= ARENA_LEFT && right <= ARENA_RIGHT && bottom >= ARENA_BOTTOM && top <= ARENA_TOP;
    }

    private static boolean collidesAny(Rectangle r, ArrayList<Blocker> blockers) {
        for (Blocker b : blockers) {
            if (intersects(r, b.hitbox)) {
                return true;
            }
        }
        return false;
    }

    private static void tryMoveEntity(Entity e, double dx, double dy, ArrayList<Blocker> blockers) {
        int moveX = (int) Math.round(dx);
        int moveY = (int) Math.round(dy);

        if (moveX != 0) {
            e.hitbox.translater(moveX, 0);
            if (!insideArena(e.hitbox) || collidesAny(e.hitbox, blockers)) {
                e.hitbox.translater(-moveX, 0);
            } else {
                e.x += moveX;
                if (e.sprite != null) {
                    e.sprite.translater(moveX, 0);
                }
            }
        }

        if (moveY != 0) {
            e.hitbox.translater(0, moveY);
            if (!insideArena(e.hitbox) || collidesAny(e.hitbox, blockers)) {
                e.hitbox.translater(0, -moveY);
            } else {
                e.y += moveY;
                if (e.sprite != null) {
                    e.sprite.translater(0, moveY);
                }
            }
        }
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

    private static Enemy spawnEnemy(Random rng, int wave) {
        int side = rng.nextInt(4);
        int x;
        int y;

        if (side == 0) {
            x = ARENA_LEFT + rng.nextInt(ARENA_RIGHT - ARENA_LEFT - ENEMY_W);
            y = ARENA_TOP + 28;
        } else if (side == 1) {
            x = ARENA_LEFT + rng.nextInt(ARENA_RIGHT - ARENA_LEFT - ENEMY_W);
            y = ARENA_BOTTOM - ENEMY_H - 28;
        } else if (side == 2) {
            x = ARENA_LEFT - ENEMY_W - 28;
            y = ARENA_BOTTOM + rng.nextInt(ARENA_TOP - ARENA_BOTTOM - ENEMY_H);
        } else {
            x = ARENA_RIGHT + 28;
            y = ARENA_BOTTOM + rng.nextInt(ARENA_TOP - ARENA_BOTTOM - ENEMY_H);
        }

        Rectangle hit = buildHitbox(x, y, ENEMY_W, ENEMY_H);
        String texPath = rng.nextBoolean() ? TEX_MONSTER_A : TEX_MONSTER_B;
        Texture spr = buildTextureOrNull(texPath, x, y, ENEMY_W, ENEMY_H);

        double speed = Math.min(4.9, 1.8 + wave * 0.17);
        int hp = 2 + (wave / 4);
        return new Enemy(x, y, ENEMY_W, ENEMY_H, hit, spr, hp, speed);
    }

    private static Blocker createBlocker(String texturePath, int x, int y, int w, int h) {
        Rectangle hit = buildHitbox(x, y, w, h);
        Texture spr = buildTextureOrNull(texturePath, x, y, w, h);
        return new Blocker(hit, spr);
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

        Texture outsideBg = buildTextureOrNull(TEX_OUTSIDE_BG, 0, 0, W, H);
        Texture arenaBg = buildTextureOrNull(TEX_ARENA_BG, ARENA_LEFT, ARENA_BOTTOM, ARENA_RIGHT - ARENA_LEFT, ARENA_TOP - ARENA_BOTTOM);

        Rectangle outsideFallback = solidRect(Couleur.NOIR, 0, 0, W, H);
        Rectangle arenaFallback = solidRect(Couleur.GRIS_FONCE, ARENA_LEFT, ARENA_BOTTOM, ARENA_RIGHT, ARENA_TOP);

        Rectangle wallTop = solidRect(Couleur.GRIS, ARENA_LEFT - 18, ARENA_TOP, ARENA_RIGHT + 18, ARENA_TOP + 24);
        Rectangle wallBottom = solidRect(Couleur.GRIS, ARENA_LEFT - 18, ARENA_BOTTOM - 24, ARENA_RIGHT + 18, ARENA_BOTTOM);
        Rectangle wallLeft = solidRect(Couleur.GRIS, ARENA_LEFT - 24, ARENA_BOTTOM, ARENA_LEFT, ARENA_TOP);
        Rectangle wallRight = solidRect(Couleur.GRIS, ARENA_RIGHT, ARENA_BOTTOM, ARENA_RIGHT + 24, ARENA_TOP);

        ArrayList<Rectangle> floorPattern = new ArrayList<>();
        int tSize = 84;
        for (int x = ARENA_LEFT; x < ARENA_RIGHT; x += tSize) {
            for (int y = ARENA_BOTTOM; y < ARENA_TOP; y += tSize) {
                Couleur c = (((x / tSize) + (y / tSize)) % 2 == 0) ? Couleur.GRIS_FONCE : Couleur.GRIS;
                Rectangle tile = solidRect(c, x, y, Math.min(x + tSize, ARENA_RIGHT), Math.min(y + tSize, ARENA_TOP));
                tile.setCouleur(c == Couleur.GRIS ? Couleur.GRIS : Couleur.GRIS_FONCE);
                floorPattern.add(tile);
            }
        }

        int px = (ARENA_LEFT + ARENA_RIGHT) / 2 - PLAYER_W / 2;
        int py = (ARENA_BOTTOM + ARENA_TOP) / 2 - PLAYER_H / 2;
        Entity player = new Entity(px, py, PLAYER_W, PLAYER_H, buildHitbox(px, py, PLAYER_W, PLAYER_H), buildTextureOrNull(TEX_PLAYER, px, py, PLAYER_W, PLAYER_H));
        Rectangle playerFallback = solidRect(Couleur.BLEU, px, py, px + PLAYER_W, py + PLAYER_H);

        ArrayList<Blocker> blockers = new ArrayList<>();
        blockers.add(createBlocker(TEX_CRATE_A, 290, 220, 72, 72));
        blockers.add(createBlocker(TEX_CRATE_B, 435, 265, 84, 60));
        blockers.add(createBlocker(TEX_CRATE_A, 560, 400, 72, 72));
        blockers.add(createBlocker(TEX_CRATE_C, 720, 300, 110, 70));
        blockers.add(createBlocker(TEX_CRATE_A, 910, 250, 72, 72));
        blockers.add(createBlocker(TEX_CRATE_B, 360, 650, 90, 64));
        blockers.add(createBlocker(TEX_CRATE_C, 545, 735, 110, 70));
        blockers.add(createBlocker(TEX_CRATE_A, 770, 620, 72, 72));
        blockers.add(createBlocker(TEX_CRATE_B, 940, 705, 88, 64));

        Rectangle slashFallback = solidRect(Couleur.JAUNE, -200, -200, -150, -150);
        moveRectTo(slashFallback, -300, -300);

        Font titleFont = new Font("Calibri", Font.BOLD, 40);
        Font hudFont = new Font("Calibri", Font.BOLD, 28);
        Font infoFont = new Font("Calibri", Font.PLAIN, 23);

        Texte title = new Texte(Couleur.BLANC, "DUNGEON BLITZ", titleFont, new Point(W / 2, 982));
        Texte scoreTxt = new Texte(Couleur.BLANC, "Score: 0", hudFont, new Point(145, 982));
        int bestScore = loadBestScore();
        Texte bestTxt = new Texte(Couleur.BLANC, "Best: " + bestScore, hudFont, new Point(1120, 982));
        Texte hpTxt = new Texte(Couleur.ROUGE, "HP: 6", hudFont, new Point(145, 944));
        Texte waveTxt = new Texte(Couleur.JAUNE, "Wave: 1", hudFont, new Point(W / 2, 944));
        Texte coolTxt = new Texte(Couleur.CYAN, "Dash(B): ready | Nova(C): ready", infoFont, new Point(W / 2, 908));
        Texte helpTxt = new Texte(Couleur.GRIS_CLAIR, "J1 stick: bouger | A: epee longue | B: dash | C: nova | Z: quitter", infoFont, new Point(W / 2, 42));
        Texte infoTxt = new Texte(Couleur.JAUNE, "", hudFont, new Point(W / 2, 520));

        f.ajouter(outsideFallback);
        if (outsideBg != null) {
            f.ajouter(outsideBg);
        }
        for (Rectangle tile : floorPattern) {
            f.ajouter(tile);
        }
        f.ajouter(arenaFallback);
        if (arenaBg != null) {
            f.ajouter(arenaBg);
        }
        f.ajouter(wallTop);
        f.ajouter(wallBottom);
        f.ajouter(wallLeft);
        f.ajouter(wallRight);

        for (Blocker b : blockers) {
            if (b.sprite != null) {
                f.ajouter(b.sprite);
            } else {
                Rectangle fallbackBox = solidRect(Couleur.ORANGE, Math.min(b.hitbox.getA().getX(), b.hitbox.getB().getX()), Math.min(b.hitbox.getA().getY(), b.hitbox.getB().getY()), Math.max(b.hitbox.getA().getX(), b.hitbox.getB().getX()), Math.max(b.hitbox.getA().getY(), b.hitbox.getB().getY()));
                f.ajouter(fallbackBox);
            }
        }

        if (player.sprite != null) {
            f.ajouter(player.sprite);
        } else {
            f.ajouter(playerFallback);
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
        ArrayList<EffectRect> fxRects = new ArrayList<>();
        ArrayList<EffectTex> fxTex = new ArrayList<>();
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

            long dashCd = Math.max(500, 900 - wave * 15);
            long novaCd = 3000;
            String dashStatus = (now - lastDash >= dashCd) ? "ready" : ("" + ((dashCd - (now - lastDash)) / 1000 + 1) + "s");
            String novaStatus = (now - lastNova >= novaCd) ? "ready" : ("" + ((novaCd - (now - lastNova)) / 1000 + 1) + "s");
            coolTxt.setTexte("Dash(B): " + dashStatus + " | Nova(C): " + novaStatus);

            Iterator<EffectRect> itFr = fxRects.iterator();
            while (itFr.hasNext()) {
                EffectRect fx = itFr.next();
                if (now >= fx.endAt) {
                    f.supprimer(fx.rect);
                    itFr.remove();
                }
            }

            Iterator<EffectTex> itFt = fxTex.iterator();
            while (itFt.hasNext()) {
                EffectTex fx = itFt.next();
                if (now >= fx.endAt) {
                    f.supprimer(fx.tex);
                    itFt.remove();
                }
            }

            if (gameOver) {
                if (clavier.getBoutonJ1ATape()) {
                    for (Enemy e : enemies) {
                        if (e.sprite != null) {
                            f.supprimer(e.sprite);
                        }
                        if (e.hitbox != null) {
                            // Hitbox non ajoutee a la fenetre
                        }
                    }
                    enemies.clear();

                    for (EffectRect fx : fxRects) {
                        f.supprimer(fx.rect);
                    }
                    fxRects.clear();

                    for (EffectTex fx : fxTex) {
                        f.supprimer(fx.tex);
                    }
                    fxTex.clear();

                    px = (ARENA_LEFT + ARENA_RIGHT) / 2 - PLAYER_W / 2;
                    py = (ARENA_BOTTOM + ARENA_TOP) / 2 - PLAYER_H / 2;
                    moveRectTo(player.hitbox, px, py);
                    moveTextureTo(player.sprite, px, py);
                    moveRectTo(playerFallback, px, py);

                    hp = 6;
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

                    hpTxt.setTexte("HP: 6");
                    scoreTxt.setTexte("Score: 0");
                    infoTxt.setTexte("");
                    gameOver = false;
                }
                f.rafraichir();
                continue;
            }

            double mx = 0;
            double my = 0;
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

            double norm = Math.sqrt(mx * mx + my * my);
            if (norm > 0.0001) {
                mx /= norm;
                my /= norm;
                facingX = mx;
                facingY = my;
            }

            if (clavier.getBoutonJ1BTape() && now - lastDash >= dashCd) {
                lastDash = now;
            }

            double currentSpeed = (now - lastDash < 140) ? DASH_SPEED : BASE_PLAYER_SPEED;
            tryMoveEntity(player, mx * currentSpeed, my * currentSpeed, blockers);

            if (player.sprite == null) {
                moveRectTo(playerFallback, (int) player.x, (int) player.y);
            }

            if (clavier.getBoutonJ1ATape() && now - lastAttack > 210) {
                lastAttack = now;

                int cx = (int) player.x + PLAYER_W / 2;
                int cy = (int) player.y + PLAYER_H / 2;

                int swordX = cx + (int) (facingX * 92) - 26;
                int swordY = cy + (int) (facingY * 92) - 26;
                Texture swordFx = buildTextureOrNull(TEX_SWORD, swordX, swordY, 52, 52);
                if (swordFx != null) {
                    f.ajouter(swordFx);
                    fxTex.add(new EffectTex(swordFx, now + 120));
                } else {
                    Rectangle slash = solidRect(Couleur.JAUNE, swordX, swordY, swordX + 52, swordY + 52);
                    f.ajouter(slash);
                    fxRects.add(new EffectRect(slash, now + 120));
                }

                Iterator<Enemy> itE = enemies.iterator();
                while (itE.hasNext()) {
                    Enemy e = itE.next();
                    int ex = (int) e.x + ENEMY_W / 2;
                    int ey = (int) e.y + ENEMY_H / 2;
                    double dx = ex - cx;
                    double dy = ey - cy;
                    double d = Math.sqrt(dx * dx + dy * dy);
                    if (d <= ATTACK_RANGE) {
                        double dot = (dx * facingX + dy * facingY) / (d + 0.0001);
                        double threshold = Math.cos(Math.toRadians(ATTACK_ARC_HALF_DEG));
                        if (dot >= threshold) {
                            e.hp -= 1;
                            if (e.hp <= 0) {
                                if (e.sprite != null) {
                                    f.supprimer(e.sprite);
                                }
                                Texture fireFx = buildTextureOrNull(TEX_FIRE, (int) e.x - 10, (int) e.y - 10, ENEMY_W + 20, ENEMY_H + 20);
                                if (fireFx != null) {
                                    f.ajouter(fireFx);
                                    fxTex.add(new EffectTex(fireFx, now + 140));
                                }
                                itE.remove();
                                score += 25;
                            }
                        }
                    }
                }
            }

            if (clavier.getBoutonJ1CTape() && now - lastNova >= novaCd) {
                lastNova = now;
                int cx = (int) player.x + PLAYER_W / 2;
                int cy = (int) player.y + PLAYER_H / 2;
                Rectangle nova = solidRect(Couleur.CYAN, cx - 165, cy - 165, cx + 165, cy + 165);
                f.ajouter(nova);
                fxRects.add(new EffectRect(nova, now + 120));

                Iterator<Enemy> itE = enemies.iterator();
                while (itE.hasNext()) {
                    Enemy e = itE.next();
                    int ex = (int) e.x + ENEMY_W / 2;
                    int ey = (int) e.y + ENEMY_H / 2;
                    double dx = ex - cx;
                    double dy = ey - cy;
                    if (Math.sqrt(dx * dx + dy * dy) <= 175) {
                        if (e.sprite != null) {
                            f.supprimer(e.sprite);
                        }
                        itE.remove();
                        score += 30;
                    }
                }
            }

            int maxEnemies = 5 + wave * 2;
            int spawnDelay = Math.max(190, 1020 - wave * 52);
            if (enemies.size() < maxEnemies && now - lastSpawn >= spawnDelay) {
                Enemy e = spawnEnemy(rng, wave);
                enemies.add(e);
                if (e.sprite != null) {
                    f.ajouter(e.sprite);
                } else {
                    Rectangle fallback = solidRect(Couleur.ROUGE, (int) e.x, (int) e.y, (int) e.x + ENEMY_W, (int) e.y + ENEMY_H);
                    e.sprite = null;
                    e.hitbox = buildHitbox((int) e.x, (int) e.y, ENEMY_W, ENEMY_H);
                    f.ajouter(fallback);
                }
                lastSpawn = now;
            }

            for (Enemy e : enemies) {
                double pcx = player.x + PLAYER_W / 2.0;
                double pcy = player.y + PLAYER_H / 2.0;
                double ecx = e.x + ENEMY_W / 2.0;
                double ecy = e.y + ENEMY_H / 2.0;

                double dx = pcx - ecx;
                double dy = pcy - ecy;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d > 0.001) {
                    dx /= d;
                    dy /= d;
                }

                int beforeX = (int) e.x;
                int beforeY = (int) e.y;
                e.x += dx * e.speed;
                e.y += dy * e.speed;

                moveRectTo(e.hitbox, (int) e.x, (int) e.y);
                if (!insideArena(e.hitbox) || collidesAny(e.hitbox, blockers)) {
                    e.x = beforeX;
                    e.y = beforeY;
                    moveRectTo(e.hitbox, beforeX, beforeY);
                    e.x += (rng.nextBoolean() ? 1 : -1) * 1.5;
                    e.y += (rng.nextBoolean() ? 1 : -1) * 1.5;
                    moveRectTo(e.hitbox, (int) e.x, (int) e.y);
                    if (!insideArena(e.hitbox) || collidesAny(e.hitbox, blockers)) {
                        e.x = beforeX;
                        e.y = beforeY;
                        moveRectTo(e.hitbox, beforeX, beforeY);
                    }
                }

                if (e.sprite != null) {
                    moveTextureTo(e.sprite, (int) e.x, (int) e.y);
                }
            }

            if (now - lastHit >= 620) {
                for (Enemy e : enemies) {
                    if (intersects(player.hitbox, e.hitbox)) {
                        hp -= 1;
                        hpTxt.setTexte("HP: " + hp);
                        lastHit = now;
                        Rectangle hitFx = solidRect(Couleur.ROUGE, (int) player.x - 12, (int) player.y - 12, (int) player.x + PLAYER_W + 12, (int) player.y + PLAYER_H + 12);
                        f.ajouter(hitFx);
                        fxRects.add(new EffectRect(hitFx, now + 110));
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
