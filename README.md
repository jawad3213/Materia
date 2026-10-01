<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/TypeScript-6.0-3178C6?style=for-the-badge&logo=typescript&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Kafka-3.7-231F20?style=for-the-badge&logo=apachekafka&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/AWS-ECS_Fargate-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white" />
  <img src="https://img.shields.io/badge/Terraform-IaC-844FBA?style=for-the-badge&logo=terraform&logoColor=white" />
</p>

<h1 align="center">🏭 MATERIA</h1>
<h3 align="center">Système Intelligent de Gestion des Achats & Approvisionnements</h3>
<h4 align="center">Plateforme Intégrée pour la Gestion de la Chaîne Logistique (Supply Chain)</h4>

<p align="center">
  <em>🚧 Projet en cours de développement — Version Pré-production 🚧</em>
</p>

---

## 🌟 Présentation Générale & Problématiques Résolues

### 📌 Problèmes Rencontrés & Solutions Apportées

Dans de nombreuses entreprises, la gestion des approvisionnements souffre de processus fragmentés, d'un manque de transparence et de pertes d'efficacité considérables :

| Problématique Métier | Impact Négatif | Solution Apportée par Materia |
|---|---|---|
| **Processus d'achat manuels et sur papier** | Délais de traitement excessifs, erreurs de saisie, perte de documents | Numérisation intégrale du cycle d'achat avec traçabilité et historique complet |
| **Absence de contrôle et d'audit des validations** | Commandes non autorisées, dépassements budgétaires incontrôlés | Circuit d'approbation strict à plusieurs niveaux avec matrice de droits RBAC |
| **Manque de visibilité sur les niveaux de stock** | Ruptures imprévues de stocks, surstockage coûteux, gâchis | Gestion en temps réel des stocks avec seuils d'alerte et de réapprovisionnement |
| **Données fournisseurs éparpillées** | Perte de temps, mauvaise négociation tarifaire, doublons | Répertoire centralisé et qualifié des fournisseurs avec catalogue associé |
| **Écarts entre commande, livraison et facture** | Litiges récurrents avec les fournisseurs, erreurs de paiement | Rapprochement automatique à trois voies (*3-Way Matching*) : Bon de commande ↔ Réception ↔ Facture |
| **Manque d'indicateurs de pilotage (KPI)** | Prise de décision à l'aveugle, reporting manuel laborieux | Tableaux de bord décisionnels et métriques consolidées en temps réel |

---

### ✨ Fonctionnalités Principales

#### 🔐 1. Authentification & Sécurité Avancée
- **Authentification double jeton (JWT)** : Jeton d'accès (*Access Token*) à courte durée de vie et jeton de rafraîchissement (*Refresh Token*) transmis sous forme de cookie sécurisé `HttpOnly` avec rotation automatique.
- **Cycle complet de gestion des identifiants** : Récupération de mot de passe oublié par e-mail avec jeton d'autorisation temporaire à usage unique, et modification sécurisée du mot de passe connecté.
- **Contrôle d'accès basé sur les rôles (RBAC)** : Gestion différenciée des habilitations pour les rôles **Administrateur** (`ADMIN`), **Acheteur** (`PURCHASER`) et **Réceptionnaire** (`RECEIVER`).
- **Protection par limitation de débit (*Rate Limiting*)** : Quota global (120 req/min) et protection renforcée anti-bruteforce sur les endpoints sensibles d'authentification (10 req/min par IP).

#### 📋 2. Demandes d'Achat (*Purchase Requisitions*)
- Création et transmission numérique des demandes d'achats internes avec sélection de matériaux et quantités requises.
- **Workflow d'approbation d'entreprise à états finis** :
  `BROUILLON (Draft) ➔ SOUMIS (Submitted) ➔ APPROUVÉ (Approved) / REJETÉ (Rejected) ➔ CONVERTI (Converted) ou ANNULÉ (Cancelled)`.
- Matrice de transition d'état stricte empêchant toute manipulation non autorisée ou rétrogradation arbitraire.
- Numérotation annuelle séquentielle automatique (ex. `REQ-2026-00001`).
- Conversion directe en bon de commande officiel dès l'approbation financière obtenue.

#### 🏭 3. Données de Référence (*Master Data*)
- Catalogue centralisé des articles et matériaux avec caractéristiques techniques.
- Gestion multi-niveaux des catégories de produits pour une classification rigoureuse.
- Répertoire des fournisseurs agréés avec moteurs de recherche, filtres et contacts clés.
- Gestion des devises et référentiels organisationnels.

#### 👥 4. Gestion des Collaborateurs & Employés
- Annuaire complet des employés rattaché à la structure organisationnelle de l'entreprise.
- Gestion du cycle de vie des collaborateurs : processus d'intégration (*onboarding*) et de sortie (*offboarding*).
- Synchronisation sécurisée des identifiants d'accès aux services internes.

#### 📦 5. Bons de Commande (*Purchase Orders*)
- Émission et gestion du cycle de vie complet des commandes passées aux fournisseurs.
- Génération automatique à partir des demandes d'achat validées.

#### 📥 6. Réception de Marchandises (*Goods Receipt*)
- Contrôle quantitatif et qualitatif lors de l'arrivée des articles en entrepôt.
- Rapprochement immédiat avec le bon de commande d'origine pour constater les reliquats ou non-conformités.

#### 🧾 7. Facturation & Comptabilité Fournisseurs
- Enregistrement des factures fournisseurs et validation comptable.
- Mécanisme de rapprochement triangulaire pour prévenir les surfacturations et paiements indus.

#### 💳 8. Gestion des Règlements & Paiements
- Suivi du calendrier d'échéance et exécution des règlements.
- Réconciliation des paiements émis avec les écritures de factures.

#### ↩️ 9. Retours aux Fournisseurs (*Return To Vendor*)
- Procédure de renvoi des marchandises défectueuses ou non conformes.
- Suivi des demandes d'avoirs et réclamations litiges.

#### 📊 10. Tableaux de Bord & Notifications *(En cours / Planifié)*
- Visualisation synthétique des volumes d'achat, délais de livraison et dépenses par catégorie.
- Système d'alertes en temps réel pour notifier les approbateurs et demandeurs.

---

## 💎 Ce qui Rend Materia Exceptionnel

### 🏗️ 1. Architecture d'Entreprise Modulaire & Robuste

Materia n'est pas une simple application CRUD basique. Elle est conçue selon les principes du **Domain-Driven Design (DDD)** et de l'**Architecture Hexagonale (Ports & Adapteurs)** :

- **11 Bounded Contexts étanches** : Chaque capacité métier (Authentification, Employés, Demandes d'achat, Données maîtres, Facturation, etc.) est isolée dans son propre domaine avec une séparation stricte entre couches Métier, Application et Infrastructure.
- **Indépendance totale du Domaine Métier** : Les règles fondamentales et entités métier ne dépendent d'aucun framework, d'aucune base de données, ni d'aucun protocole de transport.
- **Communication Événementielle Hybride** : Les modules communiquent via des événements applicatifs Spring Events en environnement local, et basculent automatiquement sur un cluster distribué **Apache Kafka** en production par simple variable de configuration.
- **Passerelle d'API Centralisée (*API Gateway Pattern*)** : Filtre d'entrée unique gérant le routage intelligent, la validation des jetons JWT, la limitation du trafic et la conformité CORS.

---

### 🔒 2. Sécurité de Niveau Bancaire

| Mécanisme | Détail de l'Implémentation |
|---|---|
| **Authentification JWT Dual-Token** | Émission de jetons d'accès JWT signés (HMAC SHA-256) via la bibliothèque JJWT 0.12.5. |
| **Rotation des Refresh Tokens** | Stockage sécurisé des jetons de renouvellement dans des cookies `HttpOnly`, `SameSite=Lax` (ou `Strict`), révoqués après usage unique. |
| **Autorisation basée sur les rôles (RBAC)** | Sécurisation granulaire au niveau méthode (`@PreAuthorize`) avec les rôles `ADMIN`, `PURCHASER`, `RECEIVER`. |
| **Limitation de Débit (*Rate Limiting*)** | Prévention des attaques par déni de service et force brute (120 req/min global, 10 req/min sur l'authentification). |
| **Journalisation Complète des Requêtes** | Traçabilité de chaque appel HTTP (méthode, URI, code statut, temps de réponse) via un filtre dédié. |
| **Gestion Fine des Origines CORS** | Restriction stricte des origines autorisées, configurable par profil d'environnement. |

---

### ☁️ 3. Infrastructure Cloud-Native & Déploiement Industriel

L'intégralité du projet est conteneurisée et prête à être déployée à grande échelle sur Amazon Web Services (AWS) via l'Infrastructure as Code (IaC) :

- **4 Profils Docker Compose** : Environnements prédéfinis prêts à l'emploi (`base`, `dev`, `staging`, `prod`) avec contraintes de ressources mémoire et CPU adaptées.
- **Orchestration Serveur AWS ECS Fargate** : Déploiement sans serveur des conteneurs backend pour une haute disponibilité et une montée en charge automatique.
- **Distribution Frontend S3 + CloudFront CDN** : Hébergement du bundle React sur AWS S3 distribué mondialement à faible latence via le réseau CDN CloudFront.
- **10 Modules Terraform Réutilisables** : Provisionnement complet et auditable de l'infrastructure AWS (VPC, sous-réseaux, passerelles NAT, Security Groups, ALB, ECR, ECS Fargate, RDS PostgreSQL, S3, IAM OIDC et Secrets Manager).
- **Intégration et Déploiement Continus (CI/CD)** : Pipelines GitHub Actions automatisés pour le déploiement sur les environnements de staging et production, avec authentification sans clé permanente grâce à AWS IAM OIDC.

---

### 🧪 4. Stratégie de Test & Assurance Qualité

- **Tests Unitaires Isolés** : Vérification des entités du domaine et des cas d'utilisation applicatifs sans mocks lourds.
- **Tests d'Intégration Réalistes (Testcontainers)** : Exécution des tests de persistance et de requêtes complexes directement contre une véritable instance conteneurisée **PostgreSQL 16**.
- **Tests de Tranche WebMvc (*WebMvcTest*)** : Validation de la couche de contrôleurs REST, de la sérialisation JSON et du contexte de sécurité Spring Security.
- **Tests de Workflows Complets** : Validation intégrale du cycle de vie des demandes d'achat et des transitions de statut.
- **Mesure de Couverture de Code** : Intégration du plugin JaCoCo pour garantir la pérennité du socle logiciel.

---

## 📐 Architecture du Système

### Vue d'Ensemble Globale

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CLIENT UTILISATEUR (Navigateur)                  │
│                Application React 19 + Vite + TailwindCSS            │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ Requêtes HTTPS / REST
┌──────────────────────────────▼──────────────────────────────────────┐
│                  PASSERELLE D'API (API GATEWAY)                     │
│  ┌────────────────────┬─────────────────────┬────────────────────┐  │
│  │ Filtre Auth JWT    │ Filtre Rate Limiter │ Filtre Journal/Log │  │
│  │ (Validation Token) │ (Quota requêtes/IP) │ (Traçabilité HTTP) │  │
│  └────────────────────┴─────────────────────┴────────────────────┘  │
│        SecurityConfig · RouteConfig · GlobalExceptionHandler        │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ Routage Interne
┌──────────────────────────────▼──────────────────────────────────────┐
│           MODULES MÉTIERS (BOUNDED CONTEXTS - DDD)                  │
│                                                                     │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐  │
│  │ Auth & Accès │ │  Employés    │ │Données Maître│ │ Demandes   │  │
│  │  (Sécurité)  │ │(Collaborat.) │ │ (Articles)   │ │  d'Achat   │  │
│  └──────────────┘ └──────────────┘ └──────────────┘ └────────────┘  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐  │
│  │   Bons de    │ │ Réception    │ │  Factures    │ │ Paiements  │  │
│  │   Commande   │ │ Marchandises │ │ Fournisseurs │ │ & Trésorerie│ │
│  └──────────────┘ └──────────────┘ └──────────────┘ └────────────┘  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                 │
│  │ Retours      │ │ Analytique & │ │ Notifications│                 │
│  │ Fournisseurs │ │ Statistiques │ │  & Alertes   │                 │
│  └──────────────┘ └──────────────┘ └──────────────┘                 │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         ▼                     ▼                     ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  PostgreSQL 16   │  │   Apache Kafka   │  │   Serveur SMTP   │
│ (Base Données)   │  │ (3.7 mode KRaft) │  │ (Envoi d'e-mails)│
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

---

### Architecture Hexagonale (par Module Métier)

Chaque Bounded Context respecte rigoureusement l'organisation interne suivante :

```
┌─────────────────────────────────────────────────────────────┐
│                    BOUNDED CONTEXT                          │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                  COUCHE DOMAINE                       │  │
│  │  Entités · Objets-Valeurs · Énumérations              │  │
│  │  Événements Métier · Exceptions Métier                 │  │
│  │  Ports d'Entrée & de Sortie (Interfaces pures)        │  │
│  └───────────────────────────────────────────────────────┘  │
│                             ▲                               │
│  ┌──────────────────────────┴────────────────────────────┐  │
│  │                COUCHE APPLICATION                     │  │
│  │  Cas d'Utilisation (Services Métier) · DTOs · Mappers  │  │
│  └───────────────────────────────────────────────────────┘  │
│                             ▲                               │
│  ┌──────────────────────────┴────────────────────────────┐  │
│  │              COUCHE INFRASTRUCTURE                    │  │
│  │  ┌───────────────────────┐  ┌───────────────────────┐ │  │
│  │  │    ADAPTEURS D'ENTRÉE │  │   ADAPTEURS DE SORTIE │ │  │
│  │  │    (Web / REST API)   │  │ (Persistance & Envoi) │ │  │
│  │  │  Contrôleurs REST     │  │  Repositories JPA     │ │  │
│  │  │  DTOs de requête Web  │  │  Entités JPA & Tables │ │  │
│  │  │  Mappers Web          │  │  Kafka Producers/SMTP │ │  │
│  │  └───────────────────────┘  └───────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Arborescence Détaillée du Projet

```
materia/
│
├── 📂 backend/                          # Application Spring Boot (Java 21)
│   ├── src/main/java/com/materia/backend/
│   │   ├── Backend2Application.java     # Point d'entrée de l'application
│   │   │
│   │   ├── 📂 gateway/                  # Couche Passerelle d'API (Gateway)
│   │   │   ├── config/                  # Configuration des routes, sécurité et CORS
│   │   │   ├── filter/                  # Filtres JWT, Rate Limit et Journalisation
│   │   │   ├── security/               # Fournisseur de jetons JWT et utilitaires
│   │   │   └── exception/              # Gestionnaire global des erreurs (RFC 7807)
│   │   │
│   │   ├── 📂 contexts/                 # Modules Métiers (DDD Bounded Contexts)
│   │   │   ├── auth/                    # 🔐 Authentification & Autorisation
│   │   │   │   ├── application/         #    Services applicatifs, DTOs, Mappers
│   │   │   │   ├── domain/              #    Entité Utilisateur, Rôles, Ports, Exceptions
│   │   │   │   └── infrastructure/      #    Contrôleurs REST, entités JPA, envoi SMTP
│   │   │   │
│   │   │   ├── employee/                # 👥 Gestion des Collaborateurs
│   │   │   │   ├── application/
│   │   │   │   ├── domain/
│   │   │   │   └── infrastructure/
│   │   │   │
│   │   │   ├── masterData/              # 🏭 Catalogue Articles, Catégories, Fournisseurs
│   │   │   │   ├── application/
│   │   │   │   ├── domain/
│   │   │   │   └── infrastructure/
│   │   │   │
│   │   │   ├── purchaseRequisition/     # 📋 Demandes d'Achat & Workflow de Validation
│   │   │   │   ├── application/
│   │   │   │   ├── domain/              #    Entités, Objets-Valeurs, Matrice d'état
│   │   │   │   └── infrastructure/
│   │   │   │
│   │   │   ├── purchaseOrder/           # 📦 Bons de Commande Fournisseurs
│   │   │   ├── goodsReceipt/            # 📥 Réceptions & Contrôles Magasin
│   │   │   ├── invoice/                 # 🧾 Facturation Fournisseurs & Rapprochement
│   │   │   ├── payement/                # 💳 Règlements & Gestion des Flux
│   │   │   ├── returnToVendor/          # ↩️  Retours Marchandises & Litiges
│   │   │   ├── analytics-service/       # 📊 Statistiques & Tableaux de Bord (planifié)
│   │   │   └── notification-service/    # 🔔 Alertes & Notifications (planifié)
│   │   │
│   │   ├── 📂 common/                   # Utilitaires transversaux et classes de base
│   │   ├── 📂 environment/              # Détection et configuration des profils
│   │   └── 📂 infrastructure/           # Configuration d'infrastructure partagée
│   │
│   ├── src/main/resources/
│   │   ├── application.properties       # Base de configuration partagée
│   │   ├── application-prod.properties  # Paramètres spécifiques à la production
│   │   └── db/migration/               # Scripts de migration Flyway (9 scripts versionnés)
│   │
│   ├── src/test/java/                   # Suite de tests automatisés
│   │   ├── contexts/auth/              # Tests unitaires et d'intégration Auth
│   │   ├── contexts/purchaseRequisition/ # Tests d'intégration du workflow d'approbation
│   │   ├── contexts/masterData/        # Tests du catalogue et des stocks
│   │   ├── contexts/employee/          # Tests de la gestion des employés
│   │   └── support/                    # Fixtures de test, assertions et conteneurs
│   │
│   ├── Dockerfile                       # Build multi-étapes pour Java 21
│   └── pom.xml                          # Dépendances Maven et plugins
│
├── 📂 frontend/                         # Application Web React 19 (Vite + TypeScript)
│   ├── src/
│   │   ├── app/                         # Enveloppe applicative et routage
│   │   │   └── routes/AppRoutes.tsx     # Définition centralisée des routes
│   │   │
│   │   ├── 📂 modules/                  # Modules fonctionnels calqués sur le domaine
│   │   │   ├── auth/                    # Connexion, mot de passe oublié, réinitialisation
│   │   │   ├── dashboard/              # Tableau de bord principal
│   │   │   ├── requisitions/           # Gestion et création des demandes d'achat
│   │   │   ├── materials/              # Gestion des articles et suivi des stocks
│   │   │   ├── suppliers/              # Répertoire et contacts des fournisseurs
│   │   │   ├── categories/             # Gestion de l'arbre des catégories
│   │   │   ├── purchaseOrders/         # Suivi des bons de commande
│   │   │   ├── goodsReceipts/          # Suivi des réceptions entrepôt
│   │   │   ├── invoices/               # Consultation des factures
│   │   │   ├── payments/               # Suivi des paiements
│   │   │   ├── returnToVendor/         # Traitement des retours
│   │   │   ├── users/                  # Administration des comptes utilisateurs
│   │   │   ├── userProfile/            # Profil individuel utilisateur
│   │   │   └── admin/                  # Panneau d'administration global
│   │   │
│   │   └── 📂 shared/                   # Composants et utilitaires transversaux
│   │       ├── api/                     # Client Axios avec intercepteurs de jetons
│   │       ├── components/              # En-tête, Barre latérale, primitives d'interface
│   │       ├── context/                 # Contextes React (authentification, thèmes)
│   │       ├── hooks/                   # Hooks React réutilisables
│   │       ├── layout/                  # Gabarit structurel AppLayout
│   │       ├── icons/                   # Composants d'icônes SVG optimisés
│   │       ├── types/                   # Définitions TypeScript globales
│   │       └── utils/                   # Fonctions d'aide et formateurs
│   │
│   ├── Dockerfile                       # Build de production et serveur Nginx
│   ├── nginx.conf                       # Configuration du serveur web inverse (Reverse Proxy)
│   └── package.json                     # Dépendances frontend et scripts
│
├── 📂 aws/                              # Infrastructure as Code (Terraform)
│   ├── modules/
│   │   ├── vpc/                         # Réseau virtuel privé sécurisé
│   │   ├── networking/                  # Sous-réseaux, tables de routage, passerelles NAT
│   │   ├── security/                    # Groupes de sécurité et listes d'accès (NACL)
│   │   ├── load_balancer/               # Équilibreur de charge applicatif (ALB)
│   │   ├── ecr/                         # Registre d'images de conteneurs AWS ECR
│   │   ├── ecs_fargate/                 # Définition des tâches et services Fargate
│   │   ├── database/                    # Base de données managée AWS RDS PostgreSQL
│   │   ├── frontend_s3_cloudfront/      # Hébergement S3 et réseau CDN CloudFront
│   │   ├── iam_github_actions/          # Rôles IAM et authentification OIDC
│   │   └── secrets/                     # Gestionnaire de secrets AWS Secrets Manager
│   └── environments/                    # Variables spécifiques par environnement
│
├── 📂 .github/                          # Intégration et Déploiement Continus (CI/CD)
│   ├── WORKFLOWS.md                     # Documentation détaillée des pipelines CI/CD
│   └── workflows/
│       ├── deploy-staging.yml           # Déploiement automatisé sur Staging
│       ├── deploy-prod.yml              # Déploiement automatisé sur Production
│       └── terraform-ci.yml             # Validation et vérification des plans Terraform
│
├── 📂 scripts/                           # Scripts d'automatisation DevOps et outillage
│
├── docker-compose.yml                   # Services de base (Postgres, Kafka, Backend, Frontend)
├── docker-compose.dev.yml               # Surcharges pour le développement (ports exposés)
├── docker-compose.staging.yml           # Surcharges pour l'environnement de recette
├── docker-compose.prod.yml              # Surcharges pour la production (limites mémoires/CPU)
│
└── .env.example                         # Modèle de variables d'environnement
```

---

## 🛠️ Stack Technologique

### Backend
| Technologie | Version | Rôle / Utilisation |
|---|---|---|
| **Java** | 21 (LTS) | Environnement d'exécution moderne |
| **Spring Boot** | 3.2.5 | Framework applicatif d'entreprise |
| **Spring Security** | 6.x | Gestion de la sécurité et contrôle d'accès RBAC |
| **Spring Data JPA** | 3.x | Couche d'accès aux données relationnelles et ORM |
| **Flyway** | 9.x | Gestion et versionnage des migrations SQL |
| **JJWT** | 0.12.5 | Création, signature et validation des jetons JWT |
| **Spring Kafka** | 3.x | Messagerie asynchrone et flux d'événements |
| **SpringDoc OpenAPI** | 2.5.0 | Documentation interactive de l'API REST (Swagger UI) |
| **Lombok** | Dernière | Réduction du code verbeux (getters, constructeurs) |
| **PostgreSQL** | 16-alpine | Système de gestion de base de données relationnelle |
| **Testcontainers** | 1.19.8 | Tests d'intégration sur un conteneur PostgreSQL réel |
| **JaCoCo** | 0.8.12 | Rapport et suivi de couverture des tests |

### Frontend
| Technologie | Version | Rôle / Utilisation |
|---|---|---|
| **React** | 19 | Bibliothèque d'interface utilisateur réactive |
| **TypeScript** | 6.0 | Typage statique et robustesse du code |
| **Vite** | 8.x | Outil de build ultra-rapide et serveur de dev |
| **TailwindCSS** | 4.x | Système de styles utilitaire moderne |
| **React Router** | 7.x | Routage côté client dynamique et fluide |
| **Axios** | 1.x | Client HTTP avec gestion centralisée des erreurs |
| **React Helmet Async** | 3.x | Gestion des balises meta et du référencement |

### Infrastructure & Déploiement
| Technologie | Rôle / Utilisation |
|---|---|
| **Docker & Docker Compose** | Conteneurisation multi-environnements (`base`, `dev`, `staging`, `prod`) |
| **Apache Kafka (KRaft)** | Plateforme d'échange d'événements distribuée sans ZooKeeper |
| **Nginx** | Serveur web et reverse-proxy de production pour le frontend |
| **AWS ECS Fargate** | Exécution serverless des conteneurs backend |
| **AWS RDS PostgreSQL** | Base de données infogérée avec sauvegardes automatiques |
| **AWS S3 + CloudFront** | Hébergement statique et CDN mondial pour l'application React |
| **AWS ECR** | Registre privé de stockage des images Docker |
| **AWS Application Load Balancer** | Répartition de charge avec sondes de santé Actuator |
| **AWS Secrets Manager** | Stockage sécurisé et chiffré des secrets et identifiants |
| **Terraform** | Définition déclarative de l'infrastructure Cloud (IaC) |
| **GitHub Actions** | Automatisation des tests et des déploiements sécurisés via OIDC |

---

## 🚀 Démarrage Rapide

### Prérequis Système
- **Java 21** ou supérieur
- **Node.js 20+** et npm
- **Docker** et **Docker Compose**
- **Maven 3.9+** (ou utilisation du wrapper `./mvnw` inclus)

---

### Lancement avec Docker Compose (Méthode Recommandée)

```bash
# 1. Cloner le dépôt
git clone https://github.com/jawad3213/Materia.git
cd Materia

# 2. Configurer les variables d'environnement
cp .env.example .env
cp backend/.env.example backend/.env

# 3. Lancer l'environnement complet de développement
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

---

### Lancement Manuel en Local

#### 1. Démarrer le Backend :
```bash
cd backend
./mvnw clean spring-boot:run
```
*Le serveur backend démarre sur le port `8080`.*

#### 2. Démarrer le Frontend (dans un autre terminal) :
```bash
cd frontend
npm install
npm run dev
```
*L'application frontend sera accessible sur `http://localhost:5173`.*

---

### Déploiement en Mode Production :
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

### Documentation Interactive de l'API
Une fois le backend démarré, vous pouvez explorer et tester directement l'ensemble des endpoints documentés via Swagger UI :
```
http://localhost:8080/swagger-ui.html
```

---

## 📊 État d'Avancement des Modules

Le projet est conçu pour couvrir l'ensemble du cycle de la chaîne d'approvisionnement. Voici l'état actuel de réalisation :

| Module Métier | Backend (API & Domaine) | Frontend (Interface) | Tests Automatisés | Statut Global |
|---|:---:|:---:|:---:|---|
| 🔐 **Authentification & Accès** | ✅ Terminé | ✅ Terminé | ✅ Couvert | **Prêt pour la production** |
| 👥 **Gestion des Employés** | ✅ Terminé | ✅ Terminé | ✅ Couvert | **Prêt pour la production** |
| 🏭 **Données Maître (Articles, Fournisseurs, Catégories)** | ✅ Terminé | ✅ Terminé | ✅ Couvert | **Prêt pour la production** |
| 📋 **Demandes d'Achat (Requisitions)** | ✅ Terminé | ✅ Terminé | ✅ Couvert | **Prêt pour la production** |
| 📦 **Bons de Commande Fournisseurs** | ✅ Terminé | ✅ Terminé | ✅ Couvert | **Prêt pour la production** |
| 📥 **Réceptions de Marchandises** | ✅ Terminé | ✅ Terminé | ✅ Couvert | **Prêt pour la production** |
| 🧾 **Factures Fournisseurs** | ✅ Terminé | ✅ Terminé | ✅ Couvert | **Prêt pour la production** |
| 💳 **Paiements & Rapprochement** | ✅ Terminé | ✅ Terminé | ✅ Couvert | **Prêt pour la production** |
| ↩️ **Retours Fournisseurs (RTV)** | ✅ Terminé | ✅ Terminé | ✅ Couvert | **Prêt pour la production** |
| 📊 **Analytique & Décisionnel** | ⬜ Planifié | ⬜ Planifié | ⬜ Non débuté | *Feuille de route future* |
| 🔔 **Service de Notifications** | ⬜ Planifié | ⬜ Planifié | ⬜ Non débuté | *Feuille de route future* |

---

## 👥 L'Équipe Projet

Projet d'envergure développé avec rigueur et passion pour moderniser et automatiser les processus d'achats en entreprise.

---

## 📄 Licence

Ce projet est propriétaire. Tous droits réservés.
