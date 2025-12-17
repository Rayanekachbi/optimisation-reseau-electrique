package projet.reseau;

/**
 * Représente une maison (consommateur) dans le réseau électrique.
 * Une maison est identifiée par un nom unique et possède un profil de consommation (BASSE, NORMAL, FORTE).
 */
public class Maison {
    private String nom;
    private TypeConsommation consommation;

    /**
     * Construit une nouvelle maison avec un nom et un type de consommation.
     *
     * @param nom Le nom unique de la maison (ne peut pas être vide)
     * @param consommation Le type de consommation énergétique (ne peut pas être null)
     * @throws IllegalArgumentException Si le nom est vide ou la consommation nulle
     */
    public Maison(String nom, TypeConsommation consommation) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la maison ne peut pas être vide.");
        }
        if (consommation == null) {
            throw new IllegalArgumentException("Le type de consommation ne peut pas être nul.");
        }
        this.nom = nom;
        this.consommation = consommation;
    }

    // Getters, Setters 
    /**
     * Récupère le nom de la maison.
     *
     * @return Le nom de la maison
     */
    public String getNom() {
        return nom;
    }

    /**
     * Récupère le type de consommation actuel de la maison.
     *
     * @return L'objet TypeConsommation (contient la demande en kW)
     */
    public TypeConsommation getConsommation() {
        return consommation;
    }

    /**
     * Modifie le type de consommation de la maison.
     *
     * @param consommation Le nouveau type de consommation (ne peut pas être null)
     * @throws IllegalArgumentException Si le type fourni est null
     */
    public void setConsommation(TypeConsommation consommation) {
        if (consommation == null) {
            throw new IllegalArgumentException("Le type de consommation ne peut pas être nul.");
        }
        this.consommation = consommation;
    }
}
