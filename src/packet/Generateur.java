package packet;
public class Generateur {
    private String nom;
    private double capaciteMaximale;

    public Generateur(String nom, double capaciteMaximale) {
        this.nom = nom;
        this.capaciteMaximale = capaciteMaximale;
    }

    // Getters, Setters
    public String getNom() {
        return nom;
    }

    public double getCapaciteMaximale() {
        return capaciteMaximale;
    }

    public void setCapaciteMaximale(double capaciteMaximale) {
        this.capaciteMaximale = capaciteMaximale;
    }
}
