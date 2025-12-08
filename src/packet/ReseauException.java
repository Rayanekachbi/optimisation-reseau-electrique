package packet;

public class ReseauException extends Exception {

    // Constructeur de base
    public ReseauException(String message) {
        super(message);
    }

    // Constructeur avec numéro de ligne (Très important pour le sujet)
    public ReseauException(String message, int numLigne) {
        super("Erreur à la ligne " + numLigne + " : " + message);
    }

    // --- Sous-classes spécifiques (Static pour être accessibles partout) ---

    // 1. Erreur de Syntaxe (ex: manque un point virgule, mot clé inconnu)
    public static class Syntaxe extends ReseauException {
        public Syntaxe(String details, int ligne) {
            super("Syntaxe incorrecte. " + details, ligne);
        }
    }

    // 2. Erreur d'Ordre (ex: Connexion définie avant les Maisons)
    public static class Ordre extends ReseauException {
        public Ordre(String element, String raison, int ligne) {
            super("Ordre de définition invalide pour '" + element + "'. " + raison, ligne);
        }
    }

    // 3. Erreur de Données (ex: Capacité négative, type inconnu)
    public static class DonneeInvalide extends ReseauException {
        public DonneeInvalide(String details, int ligne) {
            super("Donnée invalide. " + details, ligne);
        }
    }

    // 4. Erreur de Logique (ex: Maison connectée à 2 générateurs, élément introuvable)
    public static class Logique extends ReseauException {
        public Logique(String details) { // Souvent pas de ligne associée si c'est une verif globale
            super("Incohérence logique : " + details);
        }
        public Logique(String details, int ligne) {
            super("Incohérence logique : " + details, ligne);
        }
    }
}