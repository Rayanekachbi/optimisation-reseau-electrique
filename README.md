# Optimisation d'un Réseau de Distribution Électrique

**Projet de Licence 3 Informatique (Programmation Avancée) - Université Paris Cité**

## 1. À Propos du Projet

Ce projet vise à développer un logiciel capable de modéliser, simuler et évaluer l'efficacité de différentes configurations pour un réseau de distribution d'électricité. L'objectif est de trouver une allocation des maisons (consommateurs) aux générateurs (sources) qui minimise un coût global.

Ce coût est calculé en fonction de deux critères principaux :
1.  **L'équilibre de la charge** (minimiser la dispersion Disp(S))
2.  **Le respect des capacités maximales** (minimiser la pénalisation Surcharge(S))

La formule de coût utilisée est : $Cout(S) = Disp(S) + \lambda \cdot Surcharge(S)$.

## 2. Fonctionnalités (Partie 1)

Ce programme permet à un utilisateur de configurer manuellement un réseau via un menu interactif.

### Menu 1 : Configuration
* **1) Ajouter/Mettre à jour un générateur** (nom et capacité max, ex: "G1 60")
* **2) Ajouter/Mettre à jour une maison** (nom et type : BASSE, NORMAL, FORTE)
* **3) Ajouter/Modifier une connexion** (ex: "M1 G1")
* **4) Supprimer une connexion** (ex: "M1 G1")
* **5) Finaliser la configuration** (Vérifie que chaque maison a une connexion avant de passer au menu de gestion)

### Menu 2 : Analyse (Menu Gestion)
* **1) Calculer le coût** (Affiche $Cout(S)$, $Disp(S)$ et $Surcharge(S)$ avec $\lambda = 10$)
* **2) Modifier une connexion**
* **3) Afficher le réseau**
* **4) Fin** (Quitte le programme)
