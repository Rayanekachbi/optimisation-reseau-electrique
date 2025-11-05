# Optimisation d'un Réseau de Distribution Électrique

[cite_start]**Projet de Licence 3 Informatique (Programmation Avancée) - Université Paris Cité **

## 1. À Propos du Projet

[cite_start]Ce projet vise à développer un logiciel capable de modéliser, simuler et évaluer l'efficacité de différentes configurations pour un réseau de distribution d'électricité[cite: 8, 9, 10, 11]. [cite_start]L'objectif est de trouver une allocation des maisons (consommateurs) [cite: 15] [cite_start]aux générateurs (sources) [cite: 17] qui minimise un coût global.

[cite_start]Ce coût est calculé en fonction de deux critères principaux[cite: 35, 36]:
1.  [cite_start]**L'équilibre de la charge** (minimiser la dispersion $Disp(S)$) [cite: 50]
2.  [cite_start]**Le respect des capacités maximales** (minimiser la pénalisation $Surcharge(S)$) 

[cite_start]La formule de coût utilisée est : $Cout(S) = Disp(S) + \lambda \cdot Surcharge(S)$.

## 2. Fonctionnalités (Partie 1)

[cite_start]Ce programme permet à un utilisateur de configurer manuellement un réseau via un menu interactif.

### Menu 1 : Configuration
* [cite_start]**1) Ajouter un générateur** (nom et capacité max, ex: "G1 60") [cite: 113, 117]
* [cite_start]**2) Ajouter une maison** (nom et type : BASSE, NORMAL, FORTE) [cite: 114, 120]
* [cite_start]**3) Ajouter une connexion** (ex: "M1 G1") [cite: 115, 126]
* [cite_start]**4) Fin** (Vérifie que chaque maison a une unique connexion avant de continuer) [cite: 130]

### Menu 2 : Analyse
* [cite_start]**1) Calculer le coût** (Affiche $Cout(S)$, $Disp(S)$ et $Surcharge(S)$ avec $\lambda = 10$) [cite: 134, 138, 139]
* [cite_start]**2) Modifier une connexion** [cite: 135]
* [cite_start]**3) Afficher le réseau** [cite: 136]
* [cite_start]**4) Fin** (Quitte le programme) [cite: 137]

## 3. Comment l'utiliser

*(Ici, vous ajouterez les instructions pour compiler et exécuter votre code. Par exemple :)*

```bash
# Cloner le dépôt
git clone [https://github.com/votre-nom/optimisation-reseau-electrique.git](https://github.com/votre-nom/optimisation-reseau-electrique.git)
cd optimisation-reseau-electrique

# Compiler (si c'est du Java par exemple)
javac *.java

# Exécuter
java Main

