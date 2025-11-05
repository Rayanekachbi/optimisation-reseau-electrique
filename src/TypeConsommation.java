public enum TypeConsommation {
    BASSE(10), // 10 kW
    NORMAL(20), // 20 kW
    FORTE(40); // 40 kW

    private final int demandeKw;

    private TypeConsommation(int demandeKw) {
        this.demandeKw = demandeKw;
    }

    public int getDemandeKw() {
        return demandeKw;
    }

    public static TypeConsommation fromString(String type) {
        if (type == null)
            return null;
        try {
            // Le type doit correspondre aux noms d'Enum (pa exemple :"BASSE")
            return TypeConsommation.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Gérer le cas où la chaîne ne correspond à aucun nom d'Enum
            return null;
        }
    }
}