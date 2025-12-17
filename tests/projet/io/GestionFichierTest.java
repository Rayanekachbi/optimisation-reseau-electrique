package projet.io;

import org.junit.jupiter.api.AfterEach;  
import org.junit.jupiter.api.Test;

import projet.exception.ReseauException;
import projet.reseau.Reseau;
import projet.reseau.TypeConsommation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import static org.junit.jupiter.api.Assertions.*;

public class GestionFichierTest {

    private static final String FICHIER_TEST = "test_temp.txt";

    // Nettoyage après chaque test pour ne pas laisser de fichiers traîner
    @AfterEach
    public void Nettoyage() {
        File file = new File(FICHIER_TEST);
        if (file.exists()) {
            file.delete();
        }
        File fileTxt = new File(FICHIER_TEST + ".txt"); // Pour le test de sauvegarde automatique
        if (fileTxt.exists()) {
            fileTxt.delete();
        }
    }

    /**
     * Helper pour créer rapidement un fichier de test
     */
    private void creerFichier(String... lignes) throws IOException {
        try (FileWriter writer = new FileWriter(FICHIER_TEST)) {
            for (String ligne : lignes) {
                writer.write(ligne + "\n");
            }
        }
    }

    // TESTS LECTURE 
    @Test
    public void testLireFichierValide() throws IOException, ReseauException {
        creerFichier(
            "generateur(G1, 100).",
            "maison(M1, NORMAL).",
            "connexion(G1, M1)."
        );

        Reseau reseau = GestionFichier.lireFichier(FICHIER_TEST);

        assertNotNull(reseau);
        // Vérif Générateur
        assertTrue(reseau.getGenerateursMap().containsKey("G1"));
        assertEquals(100, reseau.getGenerateursMap().get("G1").getCapaciteMaximale());
        
        // Vérif Maison
        assertTrue(reseau.getMaisonsMap().containsKey("M1"));
        assertEquals(TypeConsommation.NORMAL, reseau.getMaisonsMap().get("M1").getConsommation());

        // Vérif Connexion
        assertTrue(reseau.isConnexionExiste("M1", "G1"));
    }

    @Test
    public void testLireFichierVide() throws IOException, ReseauException {
        // Un fichier vide ne doit pas planter, il renvoie juste un réseau vide
        creerFichier("");
        Reseau reseau = GestionFichier.lireFichier(FICHIER_TEST);
        assertNotNull(reseau);
        assertTrue(reseau.getGenerateursMap().isEmpty());
    }

    // TESTS LECTURE
    @Test
    public void testErreurPointManquant() throws IOException {
        creerFichier("generateur(G1, 100)"); // Pas de point !

        assertThrows(ReseauException.class, () -> {
            GestionFichier.lireFichier(FICHIER_TEST);
        });
        
    }


    @Test
    public void testErreurParentheses() throws IOException {
        creerFichier("generateur G1, 100)."); // Manque parenthèse ouvrante

        assertThrows(ReseauException.class, () -> {
            GestionFichier.lireFichier(FICHIER_TEST);
        });
    }

    @Test
    public void testErreurMotCleInconnu() throws IOException {
        creerFichier("usine(G1, 100)."); // Mot clé inventé

        assertThrows(ReseauException.class, () -> {
            GestionFichier.lireFichier(FICHIER_TEST);
        });
    }

    // TESTS LECTURE (ERREURS D'ORDRE)
    @Test
    public void testErreurOrdreMaisonApresConnexion() throws IOException {
        creerFichier(
            "generateur(G1, 100).",
            "connexion(G1, M1).", 
            "maison(M1, NORMAL)."
        );
        
        assertThrows(ReseauException.class, () -> {
            GestionFichier.lireFichier(FICHIER_TEST);
        });
        
    }

    @Test
    public void testErreurOrdreGenerateurApresMaison() throws IOException {
        creerFichier(
            "maison(M1, NORMAL).",
            "generateur(G1, 100)."
        );

        assertThrows(ReseauException.class, () -> {
            GestionFichier.lireFichier(FICHIER_TEST);
        });
        
    }

    // TESTS LECTURE 
    @Test
    public void testCapaciteNonEntiere() throws IOException {
        creerFichier("generateur(G1, 100.5)."); // Double au lieu de int

        assertThrows(ReseauException.class, () -> {
            GestionFichier.lireFichier(FICHIER_TEST);
        });
    }

    @Test
    public void testTypeConsommationInconnu() throws IOException {
        creerFichier("maison(M1, GIGANTESQUE).");

        assertThrows(ReseauException.class, () -> {
            GestionFichier.lireFichier(FICHIER_TEST);
        });
    }

    @Test
    public void testConnexionElementInexistant() throws IOException {
        creerFichier(
            "generateur(G1, 100).",
            "connexion(G1, M_Fantome)." // M_Fantome n'est pas définie
        );

        assertThrows(ReseauException.class, () -> {
            GestionFichier.lireFichier(FICHIER_TEST);
        });
    }

    // TESTS ÉCRITURE (SAUVEGARDE)
    @Test
    public void testEcritureFichier() throws IOException, ReseauException {
        // 1) On prépare un réseau en mémoire
        Reseau reseauOriginal = new Reseau();
        reseauOriginal.ajouterOuMajGenerateur("GenTest", 50);
        reseauOriginal.ajouterOuMajMaison("MaisonTest", TypeConsommation.BASSE);
        reseauOriginal.ajouterConnexion("GenTest", "MaisonTest");

        String nomSansExtension = FICHIER_TEST.replace(".txt", "");
        // 2) On sauvegarde
        GestionFichier.ecrireFichier(reseauOriginal, nomSansExtension); // Ajoute .txt auto

        // 3) On vérifie que le fichier a été créé avec le bon nom 
        File fichierAttendu = new File(FICHIER_TEST);
        assertTrue(fichierAttendu.exists());

        // 4) On relit le fichier pour vérifier le contenu
        // test ultime
        Reseau reseauRelu = GestionFichier.lireFichier(FICHIER_TEST);

        assertEquals(1, reseauRelu.getGenerateursMap().size());
        assertEquals(50, reseauRelu.getGenerateursMap().get("GenTest").getCapaciteMaximale());
        assertTrue(reseauRelu.isConnexionExiste("MaisonTest", "GenTest"));
        
        fichierAttendu.delete();
    }
}