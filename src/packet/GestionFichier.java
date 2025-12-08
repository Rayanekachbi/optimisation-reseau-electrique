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
 */
public class GestionFichier {

    // Constantes pour suivre l'ordre imposé dans le fichier
    private static final int ETAPE_GENERATEUR = 0;
    private static final int ETAPE_MAISON = 1;
    private static final int ETAPE_CONNEXION = 2;

    /**
     * Lit un fichier texte et construit le réseau correspondant.
     * @param cheminFichier Le chemin vers le fichier .txt
     * @return Un objet Reseau complet et validé.
     * @throws IOException Si le fichier n'est pas trouvé ou s'il y a une erreur de format.
     */
    public static Reseau lireFichier(String cheminFichier) throws IOException {
        Reseau reseau = new Reseau();
        
        // Try-with-resources pour fermer automatiquement le fichier à la fin
        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            
            String ligne;
            int numLigne = 0;
            int etapeActuelle = ETAPE_GENERATEUR; // On doit commencer par les générateurs 

            while ((ligne = reader.readLine()) != null) {
                numLigne++;
                ligne = ligne.trim(); // Enlever les espaces avant et après

                // Ignorer les lignes vides
                if (ligne.isEmpty()) continue;

                // 1. Vérification syntaxique de base : doit finir par un point 
                if (!ligne.endsWith(".")) {
                    throw new IOException("Erreur ligne " + numLigne + " : La ligne doit se terminer par un point '.'.");
                }
                // On retire le point pour faciliter l'analyse
                ligne = ligne.substring(0, ligne.length() - 1).trim();

                // 2. Analyse du contenu selon le mot-clé
                if (ligne.startsWith("generateur")) {
                    // Vérification de l'ordre 
                    if (etapeActuelle > ETAPE_GENERATEUR) {
                        throw new IOException("Erreur ligne " + numLigne + " : Les générateurs doivent être définis au début du fichier.");
                    }
                    etapeActuelle = ETAPE_GENERATEUR;
                    traiterGenerateur(ligne, numLigne, reseau);

                } else if (ligne.startsWith("maison")) {
                    if (etapeActuelle > ETAPE_MAISON) {
                        throw new IOException("Erreur ligne " + numLigne + " : Les maisons doivent être définies avant les connexions.");
                    }
                    etapeActuelle = ETAPE_MAISON;
                    traiterMaison(ligne, numLigne, reseau);

                } else if (ligne.startsWith("connexion")) {
                    etapeActuelle = ETAPE_CONNEXION;
                    traiterConnexion(ligne, numLigne, reseau);

                } else {
                    throw new IOException("Erreur ligne " + numLigne + " : Mot-clé inconnu ou format invalide. Attendu : generateur, maison ou connexion.");
                }
            }
        } catch (IOException e) {
            // On relance l'exception pour que le Main puisse l'afficher à l'utilisateur
            throw e; 
        } catch (Exception e) {
             // Catch global pour les erreurs imprévues (ex: index out of bounds)
             throw new IOException("Erreur critique lors de la lecture : " + e.getMessage());
        }

        return reseau;
    }

    // --- MÉTHODES PRIVÉES D'ANALYSE (PARSING) ---

    /**
     * Analyse une ligne de type : generateur(nom,capacite)
     */
    private static void traiterGenerateur(String ligne, int numLigne, Reseau reseau) throws IOException {
        String[] args = extraireArguments(ligne, numLigne, "generateur");
        
        // Vérification du nombre d'arguments 
        if (args.length != 2) {
            throw new IOException("Erreur ligne " + numLigne + " : Format generateur incorrect. Attendu : generateur(nom,capacite).");
        }

        String nom = args[0].trim();
        String capStr = args[1].trim();

        try {
            int capacite = Integer.parseInt(capStr);
            reseau.ajouterOuMajGenerateur(nom, capacite);
        } catch (NumberFormatException e) {
            throw new IOException("Erreur ligne " + numLigne + " : La capacité du générateur doit être un nombre entier.");
        }
    }

    /**
     * Analyse une ligne de type : maison(nom,TYPE)
     */
    private static void traiterMaison(String ligne, int numLigne, Reseau reseau) throws IOException {
        String[] args = extraireArguments(ligne, numLigne, "maison");

        if (args.length != 2) {
            throw new IOException("Erreur ligne " + numLigne + " : Format maison incorrect. Attendu : maison(nom,TYPE).");
        }

        String nom = args[0].trim();
        String typeStr = args[1].trim().toUpperCase(); // On met en majuscule pour être sympa

        try {
            // Conversion String -> Enum
            TypeConsommation type = TypeConsommation.valueOf(typeStr);
            reseau.ajouterOuMajMaison(nom, type);
        } catch (IllegalArgumentException e) {
            throw new IOException("Erreur ligne " + numLigne + " : Type de consommation '" + typeStr + "' inconnu (Attendu : BASSE, NORMAL, FORTE).");
        }
    }

    /**
     * Analyse une ligne de type : connexion(nom1,nom2)
     */
private static void traiterConnexion(String ligne, int numLigne, Reseau reseau) throws IOException {
        String[] args = extraireArguments(ligne, numLigne, "connexion");

        if (args.length != 2) {
            throw new IOException("Erreur ligne " + numLigne + " : Format connexion incorrect. Attendu : connexion(nom1,nom2).");
        }

        String nom1 = args[0].trim();
        String nom2 = args[1].trim();

        String resultat = reseau.ajouterConnexion(nom1, nom2);
        
        // Les messages "OK:..." ou "MAJ:..." sont considérés comme des succès.
        if (resultat != null && resultat.startsWith("Erreur")) {
            throw new IOException("Erreur ligne " + numLigne + " : " + resultat);
        }
    }

    /**
     * Outil pour extraire ce qui est entre parenthèses et séparer par la virgule.
     * Transforme "generateur(g1, 60)" en tableau ["g1", "60"]
     */
    private static String[] extraireArguments(String ligne, int numLigne, String motCle) throws IOException {
        // Vérifie qu'on a bien une parenthèse ouvrante et fermante
        int debutPar = ligne.indexOf('(');
        int finPar = ligne.lastIndexOf(')');

        if (debutPar == -1 || finPar == -1 || finPar < debutPar) {
            throw new IOException("Erreur ligne " + numLigne + " : Parenthèses manquantes ou mal placées pour '" + motCle + "'.");
        }

        // On prend ce qu'il y a au milieu
        String contenu = ligne.substring(debutPar + 1, finPar);
        
        // On coupe à la virgule
        String[] parties = contenu.split(",");
        return parties;
    }

    /**
     * Sauvegarde l'état actuel du réseau dans un fichier texte.
     * Respecte le format imposé : Générateurs -> Maisons -> Connexions.
     * @param reseau L'objet réseau contenant les données.
     * @param cheminFichier Le nom/chemin du fichier de sortie.
     * @throws IOException En cas de problème d'écriture.
     */
    public static void ecrireFichier(Reseau reseau, String cheminFichier) throws IOException {
    	if (!cheminFichier.endsWith(".txt")) {
            cheminFichier += ".txt";
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier))) {
            
            // 1. Écrire les Générateurs
            for (Generateur g : reseau.getGenerateurs().values()) {
                String ligne = String.format("generateur(%s,%d).", g.getNom(), (int) g.getCapaciteMaximale());
                writer.write(ligne);
                writer.newLine();
            }

            // 2. Écrire les Maisons
            for (Maison m : reseau.getMaisons().values()) {
                // Format : maison(nom,TYPE).
                // Note: On suppose que l'enum TypeConsommation a le même nom que dans le fichier (NORMAL, BASSE, FORTE)
                String typeStr = "";
                int demande = m.getConsommation().getDemandeKw();
                // Petite astuce pour retrouver le nom du type (BASSE/NORMAL/FORTE) si vous ne l'avez pas stocké en String
                if (demande == 10) typeStr = "BASSE";
                else if (demande == 40) typeStr = "FORTE";
                else typeStr = "NORMAL";

                String ligne = String.format("maison(%s,%s).", m.getNom(), typeStr);
                writer.write(ligne);
                writer.newLine();
            }

            // 3. Écrire les Connexions
            for (Map.Entry<Maison, Generateur> entry : reseau.getConnexions().entrySet()) {
                Maison m = entry.getKey();
                Generateur g = entry.getValue();
                
                // Format : connexion(generateur,maison).
                String ligne = String.format("connexion(%s,%s).", g.getNom(), m.getNom());
                writer.write(ligne);
                writer.newLine();
            }
            
            System.out.println("Succès : Réseau sauvegardé dans " + cheminFichier);
        }
    }
}
