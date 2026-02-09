import MG2D.*;
import MG2D.geometrie.*;

public class HelloBorne {
    public static void main(String[] args) {
        Fenetre fenetre = new Fenetre("HelloBorne", 1280, 1024);
        ClavierBorneArcade clavier = new ClavierBorneArcade();
        fenetre.addKeyListener(clavier);

        Rectangle joueur = new Rectangle(Couleur.ROUGE, new Point(640, 512), 60, 60, true);
        fenetre.ajouter(joueur);

        Texte titre = new Texte(Couleur.NOIR, "HELLO BORNE", new java.awt.Font("Calibri", java.awt.Font.BOLD, 32), new Point(640, 980));
        Texte aide1 = new Texte(Couleur.NOIR, "J1: deplacer avec le joystick", new java.awt.Font("Calibri", java.awt.Font.PLAIN, 22), new Point(640, 930));
        Texte aide2 = new Texte(Couleur.NOIR, "Quitter: bouton Z (J1)", new java.awt.Font("Calibri", java.awt.Font.PLAIN, 22), new Point(640, 900));
        fenetre.ajouter(titre);
        fenetre.ajouter(aide1);
        fenetre.ajouter(aide2);

        final int vitesse = 8;

        while (true) {
            try {
                Thread.sleep(16);
            } catch (Exception e) {
            }

            if (clavier.getBoutonJ1ZTape()) {
                break;
            }

            int dx = 0;
            int dy = 0;
            if (clavier.getJoyJ1GaucheEnfoncee()) {
                dx -= vitesse;
            }
            if (clavier.getJoyJ1DroiteEnfoncee()) {
                dx += vitesse;
            }
            if (clavier.getJoyJ1BasEnfoncee()) {
                dy -= vitesse;
            }
            if (clavier.getJoyJ1HautEnfoncee()) {
                dy += vitesse;
            }

            if (dx != 0 || dy != 0) {
                joueur.translater(dx, dy);
            }

            fenetre.rafraichir();
        }

        System.exit(0);
    }
}
