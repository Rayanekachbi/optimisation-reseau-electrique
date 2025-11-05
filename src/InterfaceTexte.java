import java.util.InputMismatchException;
import java.util.Scanner;

public class InterfaceTexte {
    private static final double LAMBDA = 10.0;

    private Reseau reseau;
    private Scanner scanner;

    public InterfaceTexte() {
        this.reseau = new Reseau();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        InterfaceTexte app = new InterfaceTexte();
        //Lancement du prog
        app.demarrer();
    }

    public void demarrer() {
        System.out.println("==============================================");
        System.out.println("|| Programme de Gestion de Réseau Électrique ||");
        System.out.println("==============================================");
        menuPrincipal();
    }


    // Menu Configuration (Menu Principal)
    private void menuPrincipal() {
        int choix = 0;
        boolean configurationFinie = false;
        while (!configurationFinie) {
            System.out.println("\n--- Menu Configuration du Réseau ---");
            System.out.println("1) Ajouter/Mettre à jour un générateur");
            System.out.println("2) Ajouter/Mettre à jour une maison");
            System.out.println("3) Ajouter/Modifier une connexion (Maison <-> Générateur)");
            System.out.println("4) Finaliser la configuration et passer à la gestion");
            System.out.print("Votre choix: ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine();

                switch (choix) {
                    case 1:
                        ajouterGenerateur();
                        break;
                    case 2:
                        ajouterMaison();
                        break;
                    case 3:
                        ajouterConnexion();
                        break;
                    case 4:
                        configurationFinie = finaliserConfiguration();
                        break;
                    default:
                        System.out.println("Choix invalide. Veuillez saisir un nombre entre 1 et 4.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur: Veuillez entrer un nombre entier pour le choix du menu.");
                scanner.nextLine();
                choix = 0;
            }
        }
    }

    private void ajouterGenerateur() {
        System.out.print("Saisissez le nom du générateur et sa capacité maximale (e.g., G1 60): ");
        String ligne = scanner.nextLine().trim();
        String[] parties = ligne.split(" ");

        if (parties.length != 2) {
            System.out.println("Erreur: Format incorrect. Format attendu: [Nom] [Capacité en kW].");
            return;
        }

        String nom = parties[0];
        try {
            int capacite = Integer.parseInt(parties[1]);
            String resultat = reseau.ajouterOuMajGenerateur(nom, capacite);
            System.out.println(resultat);
        } catch (NumberFormatException e) {
            System.out.println("Erreur: La capacité maximale doit être un nombre entier.");
        }
    }

    private void ajouterMaison() {
        System.out.print(
                "Saisissez le nom de la maison et son type de consommation (e.g., M1 NORMAL). Types: BASSE, NORMAL, FORTE: ");
        String ligne = scanner.nextLine().trim();
        String[] parties = ligne.split(" ");

        if (parties.length != 2) {
            System.out.println("Erreur: Format incorrect. Format attendu: [Nom] [TypeConsommation].");
            return;
        }

        String nom = parties[0];
        String typeStr = parties[1].toUpperCase();

        TypeConsommation type = TypeConsommation.fromString(typeStr);

        if (type == null) {
            System.out.println(
                    "Erreur: Type de consommation '" + typeStr + "' inconnu. Utilisez BASSE, NORMAL, ou FORTE.");
        } else {
            String resultat = reseau.ajouterOuMajMaison(nom, type);
            System.out.println(resultat);
        }
    }

    private void ajouterConnexion() {
        System.out.print("Saisissez la connexion (e.g., M1 G1 ou G1 M1): ");
        String ligne = scanner.nextLine().trim();
        String[] parties = ligne.split(" ");

        if (parties.length != 2) {
            System.out.println("Erreur: Format incorrect. Format attendu: [Entité1] [Entité2].");
            return;
        }

        String nom1 = parties[0];
        String nom2 = parties[1];

        String resultat = reseau.ajouterConnexion(nom1, nom2);
        System.out.println(resultat);
    }

    private void modifierConnexion() {

        System.out.print("Veuillez saisir la connexion que vous souhaitez modifier : ");
        //trim sert a retirer les espaces au debut et a la fin comme sur " g1 m1 "
        String ligneAncienne = scanner.nextLine().trim();

        //
        // Rajouter une methode dans la classe reseau qui sert à vérifier si une connexion existe
        // cela servira a traiter le cas ou l'on veut modifier une connexion saisis ici mais qui n'existe pas 
        //

        // Vérification du format de l'ancienne connexion
        String[] partiesAnciennes = ligneAncienne.split(" ");
        if (partiesAnciennes.length != 2) {
            System.out.println("Erreur: Format de connexion incorrect. Format attendu: [Entité1] [Entité2].");
            return;
        }
        
        System.out.print("Veuillez saisir la nouvelle connexion : ");
        String ligneNouvelle = scanner.nextLine().trim();

        // Vérification du format de la nouvelle connexion
        String[] partiesNouvelles = ligneNouvelle.split(" ");
        if (partiesNouvelles.length != 2) {
            System.out.println(
                    "Erreur: Format incorrect pour la nouvelle connexion. Format attendu: [Entité1] [Entité2].");
            return;
        }

        String nom1Nouvel = partiesNouvelles[0];
        String nom2Nouvel = partiesNouvelles[1];

        // Le réseau.ajouterConnexion() gère la validation et la mise à jour si la
        // Maison existe déjà.
        String resultat = reseau.ajouterConnexion(nom1Nouvel, nom2Nouvel);

        // Afficher le résultat
        if (resultat.startsWith("Erreur")) {
            System.out.println(resultat);
        } else {
            System.out.println("Modification réussie: " + resultat.replace("MAJ:", "").replace("OK:", "").trim());
        }
    }

    // la fonction retourne un boolean pour indiquer a menuPrincipal() si elle doit
    // s'arrêter
    private boolean finaliserConfiguration() {
        java.util.List<String> problemes = reseau.validerReseau();

        if (problemes.isEmpty()) {
            System.out.println("\nConfiguration terminée, le réseau est valide.");
            menuGestion();
            return true;
        } else {
            System.out.println("\nERREUR de configuration, le réseau est INVALIDE :");
            for (String probleme : problemes) {
                System.out.println("\t- " + probleme);
            }
            System.out.println("Veuillez corriger les problèmes avant de continuer, retour au Menu Configuration.");
            return false;
        }
    }

    //Menu Gestion (Menu Opérations)
    private void menuGestion() {
        int choix = 0;
        while (choix != 4) {
            System.out.println("\n--- Menu Gestion du Réseau ---");
            System.out.println("1) Calculer le coût du réseau électrique actuel");
            System.out.println("2) Modifier une connexion (via Ajouter une Connexion)");
            System.out.println("3) Afficher le réseau");
            System.out.println("4) Quitter le programme");
            System.out.print("Votre choix: ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine();

                switch (choix) {
                    case 1:
                        calculerCout();
                        break;
                    case 2:
                        modifierConnexion();
                        break;
                    case 3:
                        reseau.afficherReseau();
                        break;
                    case 4:
                        System.out.println("\nProgramme terminé. Au revoir.");
                        break;
                    default:
                        System.out.println("Choix invalide. Veuillez saisir un nombre entre 1 et 4.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur: Veuillez entrer un nombre entier pour le choix du menu.");
                scanner.nextLine();
                choix = 0;
            }
        }
        scanner.close();
    }

    private void calculerCout() {
        double disp = reseau.calculerDispersion();
        double surcharge = reseau.calculerSurcharge();
        double cout = reseau.calculerCout();

        System.out.println("\n--- Résultat du Calcul de Coût (Lambda = " + LAMBDA + ") ---");
        System.out.printf("Dispersion des écarts : %.4f\n", disp);
        System.out.printf("Pénalisation de la surcharge : %.4f\n", surcharge);
        System.out.printf("Coût total : %.4f\n", cout);
        System.out.println("-------------------------------------------------");
    }
}
