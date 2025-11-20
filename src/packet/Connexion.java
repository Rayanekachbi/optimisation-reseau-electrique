package packet;
public class Connexion {
    private Maison maison;
    private Generateur generateur;

    public Connexion(Maison maison, Generateur generateur) {
        this.maison = maison;
        this.generateur = generateur;
    }

    // Getters
    public Maison getMaison() {
        return maison;
    }

    public Generateur getGenerateur() {
        return generateur;
    }
}
