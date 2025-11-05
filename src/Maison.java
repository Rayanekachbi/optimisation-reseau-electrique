public class Maison {
    private String nom;
    private TypeConsommation consommation;

    public Maison(String nom, TypeConsommation consommation) {
        this.nom = nom;
        this.consommation = consommation;
    }

    // Getters, Setters et autres méthodes
    public String getNom() {
        return nom;
    }

    public TypeConsommation getConsommation() {
        return consommation;
    }

    public void setConsommation(TypeConsommation consommation) {
        this.consommation = consommation;
    }
}
