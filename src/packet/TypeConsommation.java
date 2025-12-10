package packet;

/**
 * Énumération représentant les différents niveaux de consommation électrique possibles pour une maison.
 * Chaque type est associé à une demande de puissance fixe exprimée en kW.
 */
public enum TypeConsommation {
    BASSE(10), // 10 kW
    NORMAL(20), // 20 kW
    FORTE(40); // 40 kW

    private final int demandeKw;

    /**
     * Constructeur privé associant une puissance au type de consommation.
     *
     * @param demandeKw La puissance demandée en kilowatts
     */
    private TypeConsommation(int demandeKw) {
        this.demandeKw = demandeKw;
    }

    /**
     * Récupère la valeur de la demande énergétique en kW associée à ce type.
     *
     * @return La puissance en kW (10, 20 ou 40)
     */
    public int getDemandeKw() {
        return demandeKw;
    }

    /**
     * Tente de convertir une chaîne de caractères en TypeConsommation.
     * La conversion n'est pas sensible à la casse (ex: "basse" fonctionne pour BASSE).
     *
     * @param type Le nom du type (ex: "NORMAL")
     * @return L'objet TypeConsommation correspondant, ou null si la chaîne est invalide ou nulle
     */
    public static TypeConsommation fromString(String type) {
        if (type == null)
            return null;
        try {
            // Le type doit correspondre aux noms d'Enum (pa exemple :"BASSE")
            return TypeConsommation.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
 
}