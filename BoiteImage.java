import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texture;


public class BoiteImage extends Boite{

    private static final String IMAGE_PAR_DEFAUT = "img/fond3.png";
    Texture image;

    BoiteImage(Rectangle rectangle, String image) {
	super(rectangle);
    String cheminImage = image + "/photo_small.png";
    if (!(new java.io.File(cheminImage).exists())) {
        cheminImage = IMAGE_PAR_DEFAUT;
    }
    this.image = new Texture(cheminImage, new Point(760, 648));
    }

    public Texture getImage() {
	return this.image;
    }

    public void setImage(String chemin) {
    String cheminImage = chemin + "/photo_small.png";
    if (!(new java.io.File(cheminImage).exists())) {
        this.image.setImg(IMAGE_PAR_DEFAUT);
    } else {
        this.image.setImg(cheminImage);
    }
	//this.image.setTaille(400, 320);
    }

}
