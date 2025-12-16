package packet.javafx;

import javafx.application.Application; 
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import packet.java.GestionFichier;
import packet.java.Reseau;

import java.io.File;

/**
 * Classe principale de l'application graphique (JavaFX).
 * Elle sert de point d'entrée et gère la fenêtre principale ainsi que le menu d'accueil.
 * Elle permet à l'utilisateur de choisir entre l'importation d'un fichier, la création manuelle
 * ou la configuration de lambda.
 */
public class MainApp extends Application {

    private Stage stagePrincipale;

    /**
     * Méthode principale de démarrage de l'application JavaFX.
     * Configure la fenêtre principale (Stage), charge l'icône, et construit la scène d'accueil
     * contenant le menu principal et la légende.
     *
     * @param stage La fenêtre principale fournie par le système JavaFX
     */
    @Override
    public void start(Stage stage) {
        this.stagePrincipale = stage;
        stagePrincipale.setTitle("Gestion Réseau Electrique - Projet");

        // Chargement icône fenêtre
        try {
            Image icon = new Image("file:icones/generateur.png");
            stagePrincipale.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Erreur icône : " + e.getMessage());
        }

        // LE MENU CENTRAL (Titre + Boutons)
        Label titreLabel = new Label("Bienvenue dans le gestionnaire de réseau");
        titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 20;");

        Button btnImporter = creerBoutonMenu("Importer un fichier");
        Button btnManuel = creerBoutonMenu("Créer un nouveau réseau");
        Button btnParametres = creerBoutonMenu("Paramètres (Lambda)");

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getChildren().addAll(titreLabel, btnImporter, btnManuel, btnParametres);
        

        // LA LÉGENDE (En bas)
        HBox legendeBox = creerBarreLegende();

        // LA RACINE (BorderPane)
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        root.setCenter(menuBox);
        root.setBottom(legendeBox);

        Scene sceneMenu = new Scene(root, 800, 600);

        // Actions des boutons
        btnImporter.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionnez un fichier de réseau");
            
            File dossierCourant = new File("./Instances-20251127");
            if(dossierCourant.exists()) fileChooser.setInitialDirectory(dossierCourant);
            
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Texte", "*.txt"));
            File fichierSelectionne = fileChooser.showOpenDialog(stagePrincipale);

            if (fichierSelectionne != null) {
                try {
                    Reseau reseauCharge = GestionFichier.lireFichier(fichierSelectionne.getAbsolutePath());
                    lancerInterfacePrincipale(reseauCharge, true);
                } catch (Exception e) {
                    System.err.println("Erreur lecture : " + e.getMessage());
                }
            }
        });

        btnManuel.setOnAction(event -> lancerInterfacePrincipale(new Reseau(), false));
        btnParametres.setOnAction(event -> ouvrirFenetreParametres());

        stagePrincipale.setScene(sceneMenu);
        stagePrincipale.show();
    }

    /**
     * Crée la barre de légende située en bas de l'écran d'accueil.
     * Affiche les icônes des différents types de maisons avec leur consommation.
     *
     * @return Un conteneur HBox contenant les éléments de la légende
     */
    private HBox creerBarreLegende() {
        HBox hbox = new HBox(60);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20, 0, 70, 0));
        
        try {
            // Création des 3 éléments de la légende
            VBox legendeFaible = creerItemLegende("file:icones/faible.png", "BASSE (10kW)");
            VBox legendeNormal = creerItemLegende("file:icones/normal.png", "NORMAL (20kW)");
            VBox legendeForte = creerItemLegende("file:icones/forte.png", "FORTE (40kW)");
            
            hbox.getChildren().addAll(legendeFaible, legendeNormal, legendeForte);
        } catch (Exception e) {
            System.out.println("Erreur chargement images légende : " + e.getMessage());
        }
        
        return hbox;
    }

    /**
     * Méthode utilitaire pour créer un élément individuel de la légende.
     *
     * @param cheminImage Le chemin vers l'image de l'icône
     * @param texte Le texte descriptif à afficher sous l'image
     * @return Un VBox contenant l'image et le texte centrés
     */
    private VBox creerItemLegende(String cheminImage, String texte) {
        ImageView iv = new ImageView(new Image(cheminImage));
        iv.setFitWidth(50); 
        iv.setPreserveRatio(true);
        
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-weight: bold;"); // Gris clair
        
        VBox vbox = new VBox(5); 
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(iv, lbl);
        
        return vbox;
    }

    /**
     * Ouvre une fenêtre pour modifier le paramètre Lambda.
     */
    private void ouvrirFenetreParametres() {
        Stage fenetre = new Stage();
        fenetre.initModality(Modality.APPLICATION_MODAL);
        fenetre.setTitle("Paramètres Globaux");

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #333333;");

        double lambdaActuel = new Reseau().getLambda();

        Label labelInfo = new Label("Valeur de Lambda (Pénalité Surcharge) :");
        labelInfo.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        TextField champLambda = new TextField(String.valueOf(lambdaActuel));
        champLambda.setMaxWidth(200);

        Button btnValider = new Button("Valider");
        btnValider.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");

        btnValider.setOnAction(e -> {
            try {
                double val = Double.parseDouble(champLambda.getText());
                if (val < 0) throw new NumberFormatException();
                new Reseau().setLambda(val);
                fenetre.close();
            } catch (NumberFormatException ex) {
                champLambda.setStyle("-fx-border-color: red; -fx-text-fill: red;");
                champLambda.setText("Erreur : Nombre positif requis");
            }
        });

        layout.getChildren().addAll(labelInfo, champLambda, btnValider);
        Scene scene = new Scene(layout, 300, 200);
        fenetre.setScene(scene);
        fenetre.showAndWait();
    }

    /**
     * Crée un bouton stylisé pour le menu principal.
     * Applique un style sombre avec des effets de survol.
     *
     * @param texte Le texte à afficher sur le bouton
     * @return Le bouton configuré et stylisé
     */
    private Button creerBoutonMenu(String texte) {
        Button b = new Button(texte);
        String styleDefaut = "-fx-background-color: #3c3f41; -fx-text-fill: white; -fx-font-size: 14px; " +
                             "-fx-border-color: #666; -fx-border-radius: 5; -fx-background-radius: 5;";
        String styleHover = "-fx-background-color: #505354; -fx-text-fill: white; -fx-font-size: 14px; " +
                            "-fx-border-color: #888; -fx-border-radius: 5; -fx-background-radius: 5;";
        b.setStyle(styleDefaut);
        b.setMinWidth(250);
        b.setOnMouseEntered(e -> b.setStyle(styleHover));
        b.setOnMouseExited(e -> b.setStyle(styleDefaut));
        return b;
    }

    /**
     * Change la scène pour afficher l'interface principale de gestion du réseau.
     * Passe en mode plein écran (Maximized).
     *
     * @param reseau L'objet Reseau à afficher (vide ou chargé depuis un fichier)
     * @param estFichier Indique si le réseau provient d'un fichier (true) ou est créé manuellement (false)
     */
    public void lancerInterfacePrincipale(Reseau reseau, boolean estFichier) {
        InterfaceGraphique rootPrincipal = new InterfaceGraphique(reseau, estFichier);
        Scene scenePrincipale = new Scene(rootPrincipal, 900, 600);
        stagePrincipale.setScene(scenePrincipale);
        stagePrincipale.setMaximized(true);
    }

    /**
     * Main qui Lance le thread JavaFX.
     *
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}