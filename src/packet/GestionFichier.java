package packet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Classe utilitaire pour gérer la lecture et l'écriture de fichiers.
 * Elle est responsable de transformer un fichier texte en objet Reseau.
 * Elle utilise ReseauException pour signaler les erreurs précises à l'utilisateur.
 */
public class GestionFichier {

    // Constantes pour suivre l'ordre imposé dans le fichier
    private static final int ETAPE_GENERATEUR = 0;
    private static final int ETAPE_MAISON = 1;
    private static final int ETAPE_CONNEXION = 2;

    /**
     * Lit un fichier texte ligne par ligne pour construire un objet Reseau complet.
     * Vérifie la syntaxe, l'ordre des définitions (Générateurs -> Maisons -> Connexions) et la validité des données.
     *
     * @param cheminFichier Le chemin d'accès au fichier de configuration (.txt)
     * @return L'objet Reseau initialisé avec les données du fichier
     * @throws IOException En cas de problème d'accès au fichier (lecture impossible, fichier absent)
     * @throws ReseauException En cas d'erreur de format, de syntaxe ou de logique métier dans le fichier
     */
    public static Reseau lireFichier(String cheminFichier) throws IOException, ReseauException {
        Reseau reseau = new Reseau();
        
        int numLigne = 0;

        // Try-with-resources pour fermer automatiquement le fichier à la fin
        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            
            String ligne;
            //int numLigne = 0;
            int etapeActuelle = ETAPE_GENERATEUR; // On doit commencer par les générateurs 

            while ((ligne = reader.readLine()) != null) {
                numLigne++;
                ligne = ligne.trim(); // Enlever les espaces avant et après

                // Ignorer les lignes vides
                if (ligne.isEmpty()) continue;

                // ça doit finir par un point 
                if (!ligne.endsWith(".")) {
                    throw new ReseauException.Syntaxe("La ligne doit se terminer par un point '.'", numLigne);
                }
                // On retire le point pour faciliter l'analyse
                ligne = ligne.substring(0, ligne.length() - 1).trim();

                // Analyse du contenu selon le mot-clé
                if (ligne.startsWith("generateur")) {
                    // Vérification de l'ordre 
                    if (etapeActuelle > ETAPE_GENERATEUR) {
                        throw new ReseauException.Ordre("générateur", "Les générateurs doivent être définis au début du fichier.", numLigne);
                    }
                    etapeActuelle = ETAPE_GENERATEUR;
                    traiterGenerateur(ligne, numLigne, reseau);

                } else if (ligne.startsWith("maison")) {
                    if (etapeActuelle > ETAPE_MAISON) {
                        throw new ReseauException.Ordre("maison", "Les maisons doivent être définies avant les connexions.", numLigne);
                    }
                    etapeActuelle = ETAPE_MAISON;
                    traiterMaison(ligne, numLigne, reseau);

                } else if (ligne.startsWith("connexion")) {
                    etapeActuelle = ETAPE_CONNEXION;
                    traiterConnexion(ligne, numLigne, reseau);

                } else {
                    // Mot inconnu
                    throw new ReseauException.Syntaxe("Mot-clé inconnu '" + ligne.split("\\(")[0] + "'", numLigne);
                }
            }
        } catch (IOException e) {
            throw e; // On relance les erreurs disques telles quelles
        } catch (ReseauException e) {
            throw e; // On relance nos exceptions métier telles quelles
        } catch (Exception e) {
             // Catch global pour les erreurs imprévues (ex: index out of bounds)
        	throw new ReseauException("Erreur critique inattendue : " + e.getMessage(), numLigne);
        }

        return reseau;
    }

    
    /**
     * Analyse une ligne décrivant un générateur et l'ajoute au réseau.
     * Format attendu : "generateur(nom,capacite)."
     *
     * @param ligne La ligne de texte brute à analyser
     * @param numLigne Le numéro de la ligne dans le fichier (pour les messages d'erreur)
     * @param reseau Le réseau en cours de construction
     * @throws ReseauException Si le format est incorrect ou la capacité invalide
     */
    private static void traiterGenerateur(String ligne, int numLigne, Reseau reseau) throws ReseauException {
        String[] args = extraireArguments(ligne, numLigne, "generateur");
        
        // Vérification du nombre d'arguments 
        if (args.length != 2) {
            throw new ReseauException.Syntaxe("generateur", "generateur(nom,capacite)", numLigne);
        }

        String nom = args[0].trim();
        String capStr = args[1].trim();

        try {
            int capacite = Integer.parseInt(capStr);
            reseau.ajouterOuMajGenerateur(nom, capacite);
        } catch (NumberFormatException e) {
            throw new ReseauException.DonneeInvalide("capacité du générateur", numLigne);
        }
    }

    /**
     * Analyse une ligne décrivant une maison et l'ajoute au réseau.
     * Format attendu : "maison(nom,TYPE_CONSOMMATION)."
     *
     * @param ligne La ligne de texte brute à analyser
     * @param numLigne Le numéro de la ligne dans le fichier
     * @param reseau Le réseau en cours de construction
     * @throws ReseauException Si le format est incorrect ou le type de consommation inconnu
     */
    private static void traiterMaison(String ligne, int numLigne, Reseau reseau) throws ReseauException {
        String[] args = extraireArguments(ligne, numLigne, "maison");

        if (args.length != 2) {
            throw new ReseauException.Syntaxe("maison", "maison(nom,TYPE)", numLigne);
        }

        String nom = args[0].trim();
        String typeStr = args[1].trim().toUpperCase();

        try {
            // Conversion String -> Enum
            TypeConsommation type = TypeConsommation.valueOf(typeStr);
            reseau.ajouterOuMajMaison(nom, type);
        } catch (IllegalArgumentException e) {
            throw new ReseauException.DonneeInvalide(typeStr, "BASSE, NORMAL, ou FORTE", numLigne);
        }
    }

    /**
     * Analyse une ligne décrivant une connexion et relie une maison à un générateur.
     * Format attendu : "connexion(nom1,nom2)."
     *
     * @param ligne La ligne de texte brute à analyser
     * @param numLigne Le numéro de la ligne dans le fichier
     * @param reseau Le réseau en cours de construction
     * @throws ReseauException Si le format est incorrect ou si un des éléments n'existe pas
     */
    private static void traiterConnexion(String ligne, int numLigne, Reseau reseau) throws ReseauException {
        String[] args = extraireArguments(ligne, numLigne, "connexion");

        if (args.length != 2) {
            throw new ReseauException.Syntaxe("connexion", "connexion(nom1,nom2)", numLigne);
        }

        String nom1 = args[0].trim();
        String nom2 = args[1].trim();

        // VALIDATION AVANCEE : On vérifie l'existence AVANT d'essayer d'ajouter la connexion
        // Cela permet de lancer ElementIntrouvable avec le numéro de ligne exact du fichier
        boolean nom1Existe = reseau.getMaisonsMap().containsKey(nom1) || reseau.getGenerateursMap().containsKey(nom1);
        boolean nom2Existe = reseau.getMaisonsMap().containsKey(nom2) || reseau.getGenerateursMap().containsKey(nom2);

        if (!nom1Existe) {
            throw new ReseauException.ElementIntrouvable("élément", nom1, numLigne);
        }
        if (!nom2Existe) {
            throw new ReseauException.ElementIntrouvable("élément", nom2, numLigne);
        }

        // Si tout existe, on tente la connexion
        // Note: Reseau.ajouterConnexion retourne une String, on doit l'analyser
        String resultat = reseau.ajouterConnexion(nom1, nom2);
        
        if (resultat != null && resultat.startsWith("Erreur")) {
            // Si c'est une erreur logique (ex: maison déjà connectée), on lève une exception générique avec le message
            throw new ReseauException(resultat.replace("Erreur: ", ""), numLigne);
        }
    }

    /**
     * Méthode utilitaire pour extraire les arguments situés entre parenthèses.
     * Découpe la chaîne contenue entre '(' et ')' en utilisant la virgule comme séparateur.
     *
     * @param ligne La ligne complète contenant la commande
     * @param numLigne Le numéro de ligne pour le rapport d'erreur
     * @param motCle Le mot-clé attendu (ex: "generateur") pour le message d'erreur
     * @return Un tableau de chaînes contenant les arguments extraits
     * @throws ReseauException.Syntaxe Si les parenthèses sont absentes ou mal placées
     */
    private static String[] extraireArguments(String ligne, int numLigne, String motCle) throws ReseauException {
        int debutPar = ligne.indexOf('(');
        int finPar = ligne.lastIndexOf(')');

        if (debutPar == -1 || finPar == -1 || finPar < debutPar) {
            throw new ReseauException.Syntaxe("Parenthèses manquantes ou mal placées pour '" + motCle + "'", numLigne);
        }

        String contenu = ligne.substring(debutPar + 1, finPar);
        
        // On vérifie s'il y a du contenu
        if (contenu.trim().isEmpty()) {
            return new String[0];
        }

        return contenu.split(",");
    }

    /**
     * Sauvegarde l'état actuel du réseau dans un fichier texte.
     * Le fichier généré respecte le format standard (Générateurs, puis Maisons, puis Connexions).
     *
     * @param reseau L'objet Reseau contenant les données à sauvegarder
     * @param cheminFichier Le chemin ou nom du fichier de destination (ajoute .txt si manquant)
     * @throws IOException En cas d'erreur lors de l'écriture sur le disque
     */
    public static void ecrireFichier(Reseau reseau, String cheminFichier) throws IOException {
        if (!cheminFichier.endsWith(".txt")) {
            cheminFichier += ".txt";
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier))) {
            
            // Ecrire les Générateurs
            for (Generateur g : reseau.getGenerateursMap().values()) {
                String ligne = String.format("generateur(%s,%d).", g.getNom(), (int) g.getCapaciteMaximale());
                writer.write(ligne);
                writer.newLine();
            }

            // Ecrire les Maisons
            for (Maison m : reseau.getMaisonsMap().values()) {
                String typeStr = "";
                int demande = m.getConsommation().getDemandeKw();
                if (demande == 10) typeStr = "BASSE";
                else if (demande == 40) typeStr = "FORTE";
                else typeStr = "NORMAL";

                String ligne = String.format("maison(%s,%s).", m.getNom(), typeStr);
                writer.write(ligne);
                writer.newLine();
            }

            // Ecrire les Connexions
            for (Map.Entry<Maison, Generateur> entry : reseau.getConnexionsMap().entrySet()) {
                String ligne = String.format("connexion(%s,%s).", entry.getValue().getNom(), entry.getKey().getNom());
                writer.write(ligne);
                writer.newLine();
            }
            
            System.out.println("Succès : Réseau sauvegardé dans " + cheminFichier);
        }
    }
}