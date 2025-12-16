package packet.javafx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import packet.java.Reseau;
import packet.java.Generateur;
import packet.java.Maison;
import packet.java.TypeConsommation;


/**
 * Composant graphique personnalisé responsable de l'affichage visuel du réseau électrique.
 * Cette classe étend ScrollPane pour permettre le défilement lorsque le réseau contient
 * de nombreux éléments. 
 * Elle dessine les générateurs, les maisons et les connexions
 * sous forme de graphe interactif.
 */
public class VueReseau extends ScrollPane {

    private Reseau reseau;
    private Pane toileDessin; 
    
    private Image imgGenerateur;
    private Image imgMaisonFaible;
    private Image imgMaisonNormal;
    private Image imgMaisonForte;

    private Map<Object, List<Line>> lignesAssociees = new HashMap<>();
    private List<Line> toutesLesLignes = new ArrayList<>();
    private Map<Object, ImageView> elementsGraphiques = new HashMap<>();

    /**
     * Construit une nouvelle vue pour le réseau spécifié.
     * Initialise le ScrollPane, charge les images des icônes et configure
     * l'écouteur de redimensionnement pour adapter l'affichage à la taille de la fenêtre.
     *
     * @param reseau Le réseau électrique à visualiser
     */
    public VueReseau(Reseau reseau) {
        this.reseau = reseau;
        
        this.setFitToWidth(true); 
        this.setStyle("-fx-background-color: #333333; -fx-background: #333333;"); 
        
        this.toileDessin = new Pane();
        this.toileDessin.setStyle("-fx-background-color: #333333;");
        this.setContent(toileDessin);

        
        this.widthProperty().addListener((obs, oldVal, newVal) -> {
            rafraichir();
        });

        try {
            imgGenerateur = new Image("file:icones/generateur.png");
            imgMaisonFaible = new Image("file:icones/faible.png");
            imgMaisonNormal = new Image("file:icones/normal.png");
            imgMaisonForte = new Image("file:icones/forte.png");
        } catch (Exception e) {
            System.err.println("Erreur chargement images : " + e.getMessage());
        }
    }

    /**
     * Redessine entièrement le contenu de la vue.
     * Cette méthode efface tous les éléments graphiques actuels et les recrée
     * en fonction de l'état actuel de l'objet Reseau. Elle recalcule également
     * les positions des éléments et la hauteur totale nécessaire pour le défilement.
     */
    public void rafraichir() {
        toileDessin.getChildren().clear();
        elementsGraphiques.clear();
        lignesAssociees.clear();
        toutesLesLignes.clear();
        
        if (reseau == null) return;

        List<Generateur> generateurs = new ArrayList<>(reseau.getGenerateursMap().values());
        List<Maison> maisons = new ArrayList<>(reseau.getMaisonsMap().values());

        // Calcul Hauteur
        double hauteurParElement = 120; 
        int maxElements = Math.max(generateurs.size(), maisons.size());
        double hauteurTotale = Math.max(600, maxElements * hauteurParElement + 100);
        toileDessin.setPrefHeight(hauteurTotale);

        // CENTRAGE HORIZONTAL, calcul en pourcentage
        double largeurVisible = this.getWidth();
        if (largeurVisible == 0) largeurVisible = 1000;

        // On place les générateurs à 20% de la largeur et les maisons à 80%
        double xGen = largeurVisible * 0.20; 
        double xMaison = largeurVisible * 0.80 - 60; 
        // -60 pour compenser la largeur de l'icône

        // DESSIN DES GéNéRATEURS
        for (int i = 0; i < generateurs.size(); i++) {
            Generateur g = generateurs.get(i);
            double y = (i * hauteurParElement) + 80; 

            ImageView iv = creerIcone(imgGenerateur, 60);
            iv.setX(xGen);
            iv.setY(y);

            Text nom = new Text(g.getNom() + "\n" + (int)g.getCapaciteMaximale() + "kW");
            nom.setFill(Color.WHITE);
            nom.setX(xGen + 10);
            nom.setY(y + 80); 
            nom.setTextAlignment(TextAlignment.CENTER);

            iv.setOnMouseEntered(e -> mettreEnValeurConnexions(g));
            iv.setOnMouseExited(e -> reinitialiserVue());

            toileDessin.getChildren().addAll(iv, nom);
            elementsGraphiques.put(g, iv);
        }

        // DESSIN DES MAISONS
        for (int i = 0; i < maisons.size(); i++) {
            Maison m = maisons.get(i);
            double y = (i * hauteurParElement) + 80;

            Image imgChoisie = imgMaisonNormal;
            if (m.getConsommation() == TypeConsommation.BASSE) imgChoisie = imgMaisonFaible;
            else if (m.getConsommation() == TypeConsommation.FORTE) imgChoisie = imgMaisonForte;

            ImageView iv = creerIcone(imgChoisie, 50);
            iv.setX(xMaison);
            iv.setY(y);

            Text nom = new Text(m.getNom());
            nom.setFill(Color.WHITE);
            nom.setX(xMaison + 10);
            nom.setY(y + 70);

            iv.setOnMouseEntered(e -> mettreEnValeurConnexions(m));
            iv.setOnMouseExited(e -> reinitialiserVue());

            toileDessin.getChildren().addAll(iv, nom);
            elementsGraphiques.put(m, iv);
        }

        // TRACAGE DES LIGNES
        for (Map.Entry<Maison, Generateur> entry : reseau.getConnexionsMap().entrySet()) {
            Maison m = entry.getKey();
            Generateur g = entry.getValue();

            ImageView viewGen = elementsGraphiques.get(g);
            ImageView viewMaison = elementsGraphiques.get(m);

            if (viewGen != null && viewMaison != null) {
                double decalageY = 10;

                double startX = viewGen.getX() + viewGen.getFitWidth(); 
                double startY = viewGen.getY() + (viewGen.getFitHeight() / 2) + decalageY; 
                
                double endX = viewMaison.getX(); 
                double endY = viewMaison.getY() + (viewMaison.getFitHeight() / 2) + decalageY; 

                Line ligne = new Line(startX, startY, endX, endY);
                ligne.setStroke(Color.YELLOW);
                ligne.setStrokeWidth(2);
                ligne.setOpacity(0.6); 

                toileDessin.getChildren().add(0, ligne); 
                
                stockerLignePourObjet(g, ligne);
                stockerLignePourObjet(m, ligne);
                toutesLesLignes.add(ligne);
            }
        }
    }

    /**
     * Associe une ligne de connexion à un objet métier (Maison ou Générateur).
     * Cette association permet de retrouver toutes les lignes connectées à un élément
     * pour l'effet de surbrillance au survol de la souris.
     *
     * @param obj L'objet métier (Maison ou Generateur)
     * @param ligne La ligne graphique représentant la connexion
     */
    private void stockerLignePourObjet(Object obj, Line ligne) {
        lignesAssociees.putIfAbsent(obj, new ArrayList<>());
        lignesAssociees.get(obj).add(ligne);
    }

    /**
     * Active l'effet de surbrillance pour les connexions d'un objet spécifique.
     * Lorsqu'un élément est survolé, ses connexions passent en rouge et deviennent plus épaisses,
     * tandis que les autres connexions sont grisées pour améliorer la lisibilité.
     *
     * @param obj L'objet survolé dont on veut mettre en valeur les connexions
     */
    private void mettreEnValeurConnexions(Object obj) {
        for (Line l : toutesLesLignes) {
            l.setStroke(Color.DARKGRAY);
            l.setOpacity(0.1);
            l.setStrokeWidth(1);
        }
        List<Line> lignesCibles = lignesAssociees.get(obj);
        if (lignesCibles != null) {
            for (Line l : lignesCibles) {
                l.setStroke(Color.RED); 
                l.setOpacity(1.0);
                l.setStrokeWidth(4); 
                l.toFront(); 
            }
        }
    }

    /**
     * Réinitialise l'apparence normale de toutes les connexions.
     * Appelé lorsque la souris quitte un élément, remettant toutes les lignes en jaune
     * avec leur épaisseur et opacité par défaut.
     */
    private void reinitialiserVue() {
        for (Line l : toutesLesLignes) {
            l.setStroke(Color.YELLOW);
            l.setOpacity(0.6);
            l.setStrokeWidth(2);
        }
    }

    /**
     * Crée et configure une ImageView pour représenter une icône du réseau.
     *
     * @param img L'image source à utiliser
     * @param taille La largeur souhaitée pour l'icône
     * @return Une instance d'ImageView configurée et prête à être ajoutée à la scène
     */
    private ImageView creerIcone(Image img, double taille) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(taille);
        iv.setPreserveRatio(true);
        iv.setStyle("-fx-cursor: hand;");
        return iv;
    }

    /**
     * Met à jour le réseau associé à cette vue et déclenche un rafraîchissement.
     *
     * @param reseau Le nouveau réseau à afficher
     */
    public void setReseau(Reseau reseau) {
        this.reseau = reseau;
        rafraichir();
    }
}