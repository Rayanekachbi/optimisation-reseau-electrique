package packet;

/**
 * Représente un générateur électrique (source d'énergie) dans le réseau.
 * Un générateur est défini par son nom et sa capacité maximale de production en kW.
 */
public class Generateur {
    private String nom;
    private double capaciteMaximale;

    /**
     * Construit un nouveau générateur avec un nom et une capacité.
     *
     * @param nom Le nom unique du générateur
     * @param capaciteMaximale La puissance maximale que le générateur peut fournir (en kW)
     * @throws IllegalArgumentException Si la capacité fournie est négative
     */
    public Generateur(String nom, double capaciteMaximale) {
        if (capaciteMaximale < 0) {
            throw new IllegalArgumentException("La capacité ne peut pas être négative.");
        }
        this.nom = nom;
        this.capaciteMaximale = capaciteMaximale;
    }

    // Getters, Setters
    /**
     * Récupère le nom du générateur.
     *
     * @return Le nom du générateur
     */
    public String getNom() {
        return nom;
    }

    /**
     * Récupère la capacité maximale de production actuelle.
     *
     * @return La capacité en kW
     */
    public double getCapaciteMaximale() {
        return capaciteMaximale;
    }

    /**
     * Modifie la capacité maximale du générateur.
     *
     * @param capaciteMaximale La nouvelle capacité en kW (doit être positive ou nulle)
     * @throws IllegalArgumentException Si la capacité fournie est négative
     */
    public void setCapaciteMaximale(double capaciteMaximale) {
        if (capaciteMaximale < 0) {
            throw new IllegalArgumentException("La capacité ne peut pas être négative.");
        }
        this.capaciteMaximale = capaciteMaximale;
    }
}
