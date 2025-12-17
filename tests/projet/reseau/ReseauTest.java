package projet.reseau;

import static org.junit.jupiter.api.Assertions.*; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import projet.exception.ReseauException;

public class ReseauTest {

    private Reseau reseau;

    @BeforeEach
    public void setUp() {
        reseau = new Reseau();
        reseau.setLambda(10.0);
    }

    //                        TESTS GESTION 
    @Test
    public void testAjouterGenerateur() throws ReseauException {
        // Test création normale
        String res = reseau.ajouterOuMajGenerateur("G1", 100);
        assertTrue(res.startsWith("OK"));
        
        // Vérification que l'objet est bien dans la Map
        Generateur g = reseau.getGenerateursMap().get("G1");
        assertNotNull(g);
        assertEquals(100, g.getCapaciteMaximale());
    }

    @Test
    public void testMettreAJourGenerateur() throws ReseauException {
        reseau.ajouterOuMajGenerateur("G1", 100);
        // Mise à jour
        String res = reseau.ajouterOuMajGenerateur("G1", 150);
        
        assertTrue(res.startsWith("MAJ"));
        assertEquals(150, reseau.getGenerateursMap().get("G1").getCapaciteMaximale());
    }

    @Test
    public void testAjouterGenerateurInvalide() {
        // On vérifie que le code lève bien une exception pour un nom vide
        assertThrows(ReseauException.class, () -> {
            reseau.ajouterOuMajGenerateur("", 100);
        });
    }

    @Test
    public void testAjouterMaison() throws ReseauException {
        String res = reseau.ajouterOuMajMaison("M1", TypeConsommation.NORMAL);
        assertTrue(res.startsWith("OK"));
        
        Maison m = reseau.getMaisonsMap().get("M1");
        assertNotNull(m);
        assertEquals(20, m.getConsommation().getDemandeKw());
    }

    //                        TESTS CONNEXIONS
    @Test
    public void testAjouterConnexion() throws ReseauException {
        reseau.ajouterOuMajGenerateur("G1", 100);
        reseau.ajouterOuMajMaison("M1", TypeConsommation.NORMAL);

        String res = reseau.ajouterConnexion("M1", "G1");
        assertTrue(res.startsWith("OK"));
        
        // Vérification de l'existence
        assertTrue(reseau.isConnexionExiste("M1", "G1"));
        assertTrue(reseau.isConnexionExiste("G1", "M1"));
    }

    @Test
    public void testModifierConnexion() throws ReseauException {
        reseau.ajouterOuMajGenerateur("G1", 100);
        reseau.ajouterOuMajGenerateur("G2", 100);
        reseau.ajouterOuMajMaison("M1", TypeConsommation.NORMAL);

        reseau.ajouterConnexion("M1", "G1");
        
        // On change la connexion vers G2
        String res = reseau.ajouterConnexion("M1", "G2");
        
        assertTrue(res.startsWith("MAJ"));
        assertTrue(reseau.isConnexionExiste("M1", "G2"));
        assertFalse(reseau.isConnexionExiste("M1", "G1"));
    }

    @Test
    public void testConnexionElementIntrouvable() {
        // Test avec des éléments qui n'existent pas
        assertThrows(ReseauException.ElementIntrouvable.class, () -> {
            reseau.ajouterConnexion("M_Fantome", "G_Fantome");
        });
    }

    @Test
    public void testSupprimerConnexion() throws ReseauException {
        reseau.ajouterOuMajGenerateur("G1", 100);
        reseau.ajouterOuMajMaison("M1", TypeConsommation.NORMAL);
        reseau.ajouterConnexion("M1", "G1");

        // Suppression
        reseau.suppConnexion("M1", "G1");
        assertFalse(reseau.isConnexionExiste("M1", "G1"));
    }


    /**
     * Ce test va s'exécuter 3 fois de suite avec les données définies dans le @CsvSource.
     * Chaque ligne du CSV correspond à un scénario de test.
     * * Format du CSV : "NomGen, Capacité, NomMaison, TypeMaison, DoitConnecter"
     */
    @ParameterizedTest
    @CsvSource({
        "G1, 100, M1, NORMAL, true",   // Cas 1 : Tout va bien, on connecte
        "G2,  50, M2, FORTE,  true",   // Cas 2 : Autre config, on connecte
        "G3, 200, M3, BASSE,  false"   // Cas 3 : On crée mais on ne connecte PAS (pour tester l'absence)
    })
    public void testCreationEtConnexionViaCsv(String nomGen, int cap, String nomMaison, String typeStr, boolean connecter) throws ReseauException {
        reseau.ajouterOuMajGenerateur(nomGen, cap);
        assertNotNull(reseau.getGenerateursMap().get(nomGen), "Le générateur " + nomGen + " devrait être créé");

        TypeConsommation type = TypeConsommation.valueOf(typeStr);
        reseau.ajouterOuMajMaison(nomMaison, type);
        assertNotNull(reseau.getMaisonsMap().get(nomMaison), "La maison " + nomMaison + " devrait être créée");

        // Connexion (si demandé par la colonne 'connecter' du CSV)
        if (connecter) {
            String res = reseau.ajouterConnexion(nomMaison, nomGen);
            assertTrue(res.startsWith("OK"));
            
            // Vérification finale
            assertTrue(reseau.isConnexionExiste(nomMaison, nomGen));
        } else {
            // Si on a dit "false", on vérifie que la connexion N'existe PAS
            assertFalse(reseau.isConnexionExiste(nomMaison, nomGen));
        }
    }

    //                        TESTS VALIDATION
    @Test
    public void testValidationReseauValide() throws ReseauException {
        reseau.ajouterOuMajGenerateur("G1", 100);
        reseau.ajouterOuMajMaison("M1", TypeConsommation.NORMAL);
        reseau.ajouterConnexion("M1", "G1");

        // Si tout va bien, aucune exception n'est levée
        assertDoesNotThrow(() -> reseau.validerReseau());
    }

    @Test
    public void testValidationReseauInvalide() throws ReseauException {
        reseau.ajouterOuMajGenerateur("G1", 100);
        reseau.ajouterOuMajMaison("M1", TypeConsommation.NORMAL);
        // Pas de connexion !

        // On s'attend à une erreur Logique
        ReseauException.Logique e = assertThrows(ReseauException.Logique.class, () -> {
            reseau.validerReseau();
        });

        assertTrue(e.getMessage().contains("aucune connexion"));
    }

    // TESTS CALCULS 
    @Test
    public void testCalculCharge() throws ReseauException {
        // G1 (100) connecté à M1 (20) et M2 (40)
        reseau.ajouterOuMajGenerateur("G1", 100);
        reseau.ajouterOuMajMaison("M1", TypeConsommation.NORMAL); // 20
        reseau.ajouterOuMajMaison("M2", TypeConsommation.FORTE);  // 40
        
        reseau.ajouterConnexion("M1", "G1");
        reseau.ajouterConnexion("M2", "G1");

        Generateur g1 = reseau.getGenerateursMap().get("G1");
        assertEquals(60.0, reseau.calculerChargeActuelle(g1));
        assertEquals(0.6, reseau.calculerTauxUtilisation(g1));
    }

    @Test
    public void testCalculSurchargeEtCout() throws ReseauException {
        // Scénario de Surcharge :
        // G1 (10kW) connecté à M1 (FORTE = 40kW)
        // Charge = 40. Capacité = 10. Taux = 4.0.
        // Surcharge = Taux - 1 = 3.0.
        // Dispersion (1 seul générateur) = 0.
        // Coût = Disp + Lambda * Surcharge = 0 + 10 * 3.0 = 30.0
        
        reseau.ajouterOuMajGenerateur("G1", 10);
        reseau.ajouterOuMajMaison("M1", TypeConsommation.FORTE);
        reseau.ajouterConnexion("M1", "G1");

        assertEquals(3.0, reseau.calculerSurcharge(), 0.001);
        assertEquals(30.0, reseau.calculerCout(), 0.001);
    }

    @Test
    public void testCalculDispersion() throws ReseauException {
        // Scénario Dispersion :
        // G1 (100kW) -> M1 (20kW) => Taux 0.2
        // G2 (100kW) -> M2 (80kW) => Taux 0.8
        // Moyenne taux = 0.5
        // Ecart G1 = |0.2 - 0.5| = 0.3
        // Ecart G2 = |0.8 - 0.5| = 0.3
        // Dispersion Totale = 0.3 + 0.3 = 0.6
        
        // Pour simuler 80kW, je crée 2 maisons FORTE (40+40)
        reseau.ajouterOuMajGenerateur("G1", 100);
        reseau.ajouterOuMajGenerateur("G2", 100);
        
        reseau.ajouterOuMajMaison("M1", TypeConsommation.NORMAL); // 20
        reseau.ajouterOuMajMaison("M2_A", TypeConsommation.FORTE); // 40
        reseau.ajouterOuMajMaison("M2_B", TypeConsommation.FORTE); // 40

        reseau.ajouterConnexion("M1", "G1");     // G1 charge 20
        reseau.ajouterConnexion("M2_A", "G2");   // G2 charge 80
        reseau.ajouterConnexion("M2_B", "G2");

        assertEquals(0.6, reseau.calculerDispersion(), 0.001);
    }


    // TESTS COMPLÉMENTAIRES
    @Test
    public void testCalculTauxCapaciteZero() throws ReseauException {
        // On crée un générateur avec 0 de capacité
        reseau.ajouterOuMajGenerateur("G_Zero", 0);
        Generateur g = reseau.getGenerateursMap().get("G_Zero");

        // On vérifie que le calcul lance bien une exception Logique au lieu de faire une division par zéro
         assertThrows(ReseauException.Logique.class, () -> {
            reseau.calculerTauxUtilisation(g);
        });
        
    }

    @Test
    public void testValidationReseauVide() {
        // Le réseau est vide (setUp crée un new Reseau())
        
        assertThrows(ReseauException.Logique.class, () -> {
            reseau.validerReseau();
        });
    }

    @Test
    public void testSuppressionConnexionInexistante() throws ReseauException {
       
        reseau.ajouterOuMajGenerateur("G1", 100);
        reseau.ajouterOuMajMaison("M1", TypeConsommation.NORMAL);
        // On ne les connecte PAS

        assertThrows(ReseauException.Logique.class, () -> {
            reseau.suppConnexion("M1", "G1");
        });
    }
    
    @Test
    public void testSetLambda() {
        reseau.setLambda(50.0);
        assertEquals(50.0, reseau.getLambda());
    }
}