package hotel.web.service.client.ui;


import java.io.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.example.agenthotelgrpc.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class CLI extends AbstractMain {

    // ANSI color codes remain the same
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE_BOLD = "\u001B[1;37m";

    // gRPC Channels and Stubs
    private ManagedChannel agenceChannel;
    private ManagedChannel comparateurChannel;
    // stubs
    private AgenceServiceGrpc.AgenceServiceBlockingStub agenceServiceStub;
    private ComparateurServiceGrpc.ComparateurServiceBlockingStub comparateurServiceStub;

    public static void main(String[] args) {
        CLI main = new CLI();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        String userInput = "";

        try {
            main.initializeGrpcChannels();
            main.displayWelcomeMessage();
            do {
                main.menu();
                userInput = inputReader.readLine();
                main.processUserInput(inputReader, userInput);
            } while (!userInput.equals(QUIT));
        } catch (Exception e) {
            System.err.println(ANSI_RED + "Une erreur inattendue s'est produite: " + e.getMessage() + ANSI_RESET);
        } finally {
            main.shutdownChannels();
            try {
                inputReader.close();
            } catch (IOException e) {
                System.err.println(ANSI_RED + "Erreur lors de la fermeture du lecteur: " + e.getMessage() + ANSI_RESET);
            }
        }
    }

    private void initializeGrpcChannels() {

        agenceChannel = ManagedChannelBuilder
                .forAddress("localhost", 9091)
                .usePlaintext()
                .build();

        agenceServiceStub = AgenceServiceGrpc.newBlockingStub(agenceChannel);

        comparateurChannel = ManagedChannelBuilder
                .forAddress("localhost", 9096)
                .usePlaintext()
                .build();

        comparateurServiceStub = ComparateurServiceGrpc.newBlockingStub(comparateurChannel);

    }

    private void shutdownChannels() {
        try {
            agenceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            comparateurChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println(ANSI_RED + "Erreur lors de la fermeture des canaux: " + e.getMessage() + ANSI_RESET);
        }
    }

    private void displayWelcomeMessage() {
        System.out.println(ANSI_BLUE + "    ============================================================");
        System.out.println("            Bienvenue dans votre service de Réservation d'Hôtel");
        System.out.println("    ============================================================" + ANSI_RESET);
    }


    @Override
    public void menu() {
        System.out.println(ANSI_YELLOW + "\n╔══════════════════════════════════════╗" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "║            Menu Principal            ║" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "╚══════════════════════════════════════╝" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "    1. " + ANSI_RESET + "🔍 Consulter les offres");
        System.out.println(ANSI_GREEN + "    2. " + ANSI_RESET + "📅 Effectuer une réservation");
        System.out.println(ANSI_GREEN + "    3. " + ANSI_RESET + "🆚 Comparateur  ");
        System.out.println(ANSI_GREEN + "    0. " + ANSI_RESET + "🚪 Quitter");
        System.out.println(ANSI_YELLOW + "────────────────────────────────────────" + ANSI_RESET);
        System.out.print(ANSI_CYAN + "➡️  Votre choix : " + ANSI_RESET);
    }


    private void processUserInput(BufferedReader reader, String userInput) {
        try {
            switch (userInput) {
                case "1":
                    consultation(reader);
                    break;
                case "2":
                    makeReservation(reader);
                    break;
                case "3":
                    compareReservations(reader);
                    break;
                case QUIT:
                    System.out.println(ANSI_BLUE + "\n🌟 Merci d'avoir utilisé notre service. Au revoir! 👋" + ANSI_RESET);
                    return;
                default:
                    System.out.println(ANSI_RED + "❌ Choix invalide. Veuillez réessayer." + ANSI_RESET);
            }
        } catch (Exception e) {
            System.err.println(ANSI_RED + "🚨 Erreur : " + e.getMessage() + ANSI_RESET);
        }
    }

    public void compareReservations(BufferedReader reader) throws IOException, NoSuchMethodException {
        String dateDebut = getDateInput(reader, "début");
        String dateFin = getDateInput(reader, "fin");
        String ville = getCityInput(reader);
        int nbEtoiles = getNbEtoilesInput(reader);
        int nbPersonnes = getNbPersonnes(reader);

        ComparateurRequest comparaisonRequest = ComparateurRequest.newBuilder()
                .setDateDebut(dateDebut)
                .setDateFin(dateFin)
                .setNbPersonnes(nbPersonnes)
                .setVille(ville)
                .setNbEtoiles(nbEtoiles)
                .build();

        ComparateurResponse compareResponse = comparateurServiceStub.compare(comparaisonRequest);

        if (compareResponse.getOffresCount() == 0) {
            printErrorBox("❌ Aucune offre disponible",
                    "Aucune offre n'a été trouvée pour les critères de votre recherche.\n");
            return;
        }

        displayComparatorResults(compareResponse);
    }
    private void displayComparatorResults(ComparateurResponse compareResponse) {
        System.out.println(ANSI_BLUE + "\n╔══════════════════════════════════════════╗");
        System.out.println("║      🔍 RÉSULTATS COMPARATEUR 🔍         ║");
        System.out.println("╚══════════════════════════════════════════╝" + ANSI_RESET);

        // Group offers by agency to differentiate
        HashMap<String, List<OffreComparateurDTO>> offresByAgence = new HashMap<>();
        for (OffreComparateurDTO offre : compareResponse.getOffresList()) {
            offresByAgence.computeIfAbsent(offre.getNomAgence(), k -> new ArrayList<>()).add(offre);
        }

        // Display offers grouped by agency
        for (Map.Entry<String, List<OffreComparateurDTO>> entry : offresByAgence.entrySet()) {
            System.out.println(ANSI_YELLOW + "\n╔══════════════════════════════════════════╗");
            System.out.printf("║  🏢 Agence: %-18s ║\n", entry.getKey());
            System.out.println("╚══════════════════════════════════════════╝" + ANSI_RESET);

            for (OffreComparateurDTO offre : entry.getValue()) {
                System.out.println(ANSI_YELLOW + "\n╔═════════════════════════════════════════════╗");
                System.out.printf("║  %s🏨 Hôtel:%s %-32s ║\n", ANSI_WHITE_BOLD, ANSI_CYAN, offre.getNomHotel());
                System.out.println("╠═════════════════════════════════════════════╣");

                System.out.printf("║  %s💰 Prix:%s %-29.2f€  \n", ANSI_WHITE_BOLD, ANSI_GREEN, offre.getPrix());
                System.out.printf("║  🌟 Étoiles: %-28d  \n", offre.getNbEtoiles());
                System.out.printf("║  🛏️  Lits disponibles: %-20d  \n", offre.getNbLitsDisponibles());
                System.out.printf("║  📍 Adresse:%s %-25s  \n", ANSI_CYAN, offre.getAdresseHotel());


                System.out.println("╠════════════════════════════════════════════════╣");
                System.out.printf("║  %s🏢 Agence:%s %-25s  \n", ANSI_WHITE_BOLD, ANSI_CYAN, offre.getNomAgence());
                System.out.printf("║  📍 Adresse:%s %-25s  \n", ANSI_CYAN, offre.getAdresseAgence());
                System.out.printf("║  %s💸 Pourcentage de réduction:%s %-10.2f%%\n",
                        ANSI_WHITE_BOLD, ANSI_GREEN, offre.getPourcentageReduction());

                System.out.println("╚════════════════════════════════════════════════╝" + ANSI_RESET);
            }
        }

        System.out.println(ANSI_BLUE + "\n═══════════ Fin des résultats ════════════" + ANSI_RESET + "\n");
    }

    private void consultation(BufferedReader reader) throws Exception {
        // Demander les informations pour la consultation
        int idAgence = getAuthenticationId(reader);
        String motDePasse = getAuthenticationPassword(reader);

        // Authentification via gRPC
        AuthentifierRequest authRequest = AuthentifierRequest.newBuilder()
                .setIdAgence(idAgence)
                .setPassword(motDePasse)
                .build();
        AuthentifierResponse authResponse = agenceServiceStub.authentifier(authRequest);

        if (!authResponse.getAuthentifie()) {
            printErrorBox("❌ Accès Refusé",
                    "Vérifiez vos identifiants d'agence.\n" +
                            "Assurez-vous d'avoir les droits d'accès nécessaires.");
            return;
        }

        System.out.println(ANSI_CYAN +
                "╔══════════════════════════════════════╗\n" +
                "║       ✅   ACCÈS ACCORDÉ     ✅     ║\n" +
                "╚══════════════════════════════════════╝" + ANSI_RESET);


        // Récupérer les critères de recherche
        String dateDebut = getDateInput(reader, "début");
        String dateFin = getDateInput(reader, "fin");
        String ville = getCityInput(reader);
        int nbEtoiles = getNbEtoilesInput(reader);
        int nbPersonnes = getNbPersonnes(reader);

        // Appel gRPC pour obtenir les offres
        GetOffresRequest request = GetOffresRequest.newBuilder()
                .setIdAgence(idAgence)
                //.setPassword(motDePasse)
                .setDateDebut(dateDebut)
                .setDateFin(dateFin)
                .setNbPersonnes(nbPersonnes)
                .setVille(ville)
                .setNbEtoiles(nbEtoiles)
                .build();
        GetOffresResponse response = agenceServiceStub.getOffres(request);

        // Afficher les offres reçues
        displayOffers(response);

    }

    // No Offers Found Message
    private void printNoOffersFound() {
        System.out.println(ANSI_YELLOW +
                "╔══════════════════════════════════════╗\n" +
                "║       🔍 AUCUNE OFFRE TROUVÉE       ║\n" +
                "╠══════════════════════════════════════╣\n" +
                "║ Aucun hébergement ne correspond à   ║\n" +
                "║ vos critères de recherche actuels.   ║\n" +
                "║ Essayez de modifier vos paramètres.  ║\n" +
                "╚══════════════════════════════════════╝" + ANSI_RESET);
    }


    private void displayOffers(GetOffresResponse response) {
        System.out.println(ANSI_BLUE + "\n╔══════════════════════════════════════════╗");
        System.out.println("║         ✨ OFFRES DISPONIBLES ✨        ║");
        System.out.println("╚══════════════════════════════════════════╝" + ANSI_RESET);

        if (response.getOffresCount() == 0) {
            printNoOffersFound();
            return;
        }

        for (com.example.agenthotelgrpc.proto.OffreDTO offre : response.getOffresList()) {
            System.out.println(ANSI_YELLOW + "\n╔══════════════════════════════════════════╗");
            System.out.printf("║  %s🏷️  Offre #%-28d%s ║\n", ANSI_WHITE_BOLD, offre.getIdOffre(), ANSI_YELLOW);
            System.out.println("╠══════════════════════════════════════════╣");

            System.out.printf("║  %s💰 Prix:%s %-29.2f€  \n", ANSI_WHITE_BOLD, ANSI_GREEN, offre.getPrix());
            System.out.printf("║  🛏️  Type: %-28s  \n", typeChambreToString(offre.getNbPersonnes()));
            System.out.printf("║  ⌛ Période:%s %s - %s\n", ANSI_YELLOW, offre.getDateDebut(), offre.getDateFin());

            System.out.println("╠══════════════════════════════════════════╣");
            System.out.printf("║  %s🏢 Hôtel:%s %-25s  \n", ANSI_WHITE_BOLD, ANSI_CYAN, offre.getNomHotel());
            System.out.printf("║  %s🌍 Ville:%s %-25s  \n", ANSI_WHITE_BOLD, ANSI_CYAN, offre.getVille());
            System.out.printf("║  %s💸 Pourcentage de réduction:%s %-25s   \n",
                    ANSI_WHITE_BOLD, ANSI_GREEN, offre.getPourcentageReduction());

            System.out.println("╚══════════════════════════════════════════╝" + ANSI_RESET);
        }

        System.out.println(ANSI_BLUE + "\n═══════════ Fin des offres ════════════" + ANSI_RESET + "\n");
        // Demander à l'utilisateur s'il souhaite télécharger des images
        promptForImageDownload(response.getOffresList());
    }

    private void promptForImageDownload(List<com.example.agenthotelgrpc.proto.OffreDTO> offres) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println(ANSI_CYAN + "\n╔══════════════════════════════════════════╗");
            System.out.println("║      📸 TÉLÉCHARGEMENT D'IMAGES 📸      ║");
            System.out.println("╚══════════════════════════════════════════╝" + ANSI_RESET);

            System.out.print("Souhaitez-vous télécharger des images d'offres ? (o/n): ");
            String response = reader.readLine().trim().toLowerCase();

            if (response.equals("o")) {
                handleImageDownload(reader, offres);
            }
        } catch (IOException e) {
            System.err.println(ANSI_RED + "Erreur lors de la lecture de l'entrée: " + e.getMessage() + ANSI_RESET);
        }
    }

    private void handleImageDownload(BufferedReader reader, List<com.example.agenthotelgrpc.proto.OffreDTO> offres) {
        try {
            // Créer le répertoire images s'il n'existe pas
            File imagesDir = new File("images");
            if (!imagesDir.exists()) {
                imagesDir.mkdir();
            }

            while (true) {
                System.out.println(ANSI_YELLOW + "\nEntrez l'ID de l'offre dont vous souhaitez télécharger l'image (ou 'q' pour quitter):" + ANSI_RESET);
                String input = reader.readLine().trim();

                if (input.equalsIgnoreCase("q")) {
                    break;
                }

                try {
                    int offreId = Integer.parseInt(input);
                    boolean found = false;

                    for (com.example.agenthotelgrpc.proto.OffreDTO offre : offres) {
                        if (offre.getIdOffre() == offreId) {
                            found = true;
                            downloadImage(offre);
                            break;
                        }
                    }

                    if (!found) {
                        System.out.println(ANSI_RED + "❌ ID d'offre non trouvé. Veuillez réessayer." + ANSI_RESET);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ANSI_RED + "❌ Veuillez entrer un nombre valide." + ANSI_RESET);
                }
            }
        } catch (IOException e) {
            System.err.println(ANSI_RED + "Erreur lors du téléchargement des images: " + e.getMessage() + ANSI_RESET);
        }
    }

    private void downloadImage(com.example.agenthotelgrpc.proto.OffreDTO offre) {
        try {
            byte[] imageData = offre.getImage().toByteArray();
            if (imageData == null || imageData.length == 0) {
                System.out.println(ANSI_YELLOW + "⚠️ Aucune image disponible pour cette offre." + ANSI_RESET);
                return;
            }

            String fileName = String.format("images/offre_%d_%s.jpg", offre.getIdOffre(), offre.getNomHotel().replaceAll("[^a-zA-Z0-9]", "_"));
            File outputFile = new File(fileName);

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(imageData);
                System.out.println(ANSI_GREEN + "✅ Image téléchargée avec succès: " + fileName + ANSI_RESET);
            }
        } catch (IOException e) {
            System.err.println(ANSI_RED + "Erreur lors de l'enregistrement de l'image: " + e.getMessage() + ANSI_RESET);
        }
    }


    private void makeReservation(BufferedReader reader) throws Exception {
        // ASCII Art Header
        printHeader();

        try {
            // Authentication
            int idAgence = getAuthenticationId(reader);
            String motDePasse = getAuthenticationPassword(reader);

            AuthentifierRequest authRequest = AuthentifierRequest.newBuilder()
                    .setIdAgence(idAgence)
                    .setPassword(motDePasse)
                    .build();
            AuthentifierResponse authResponse = agenceServiceStub.authentifier(authRequest);

            if (!authResponse.getAuthentifie()) {
                printErrorBox("❌ Accès Refusé",
                        "Vérifiez vos identifiants d'agence.\n" +
                                "Assurez-vous d'avoir les droits d'accès nécessaires.");
                return;
            }

            System.out.println(ANSI_CYAN +
                    "╔══════════════════════════════════════╗\n" +
                    "║       ✅   ACCÈS ACCORDÉ     ✅      ║\n" +
                    "╚══════════════════════════════════════╝" + ANSI_RESET);

            // Offer and Reservation Details
            int idOffre = getOffreId(reader);
            String dateDebut = getDateInput(reader, "début");
            String dateFin = getDateInput(reader, "fin");
            int nbPersonnes = getNbPersonnes(reader);

            // Availability Check
            PossibleRequest possibleRequest = PossibleRequest.newBuilder()
                    .setIdOffre(idOffre)
                    .setDateDebut(dateDebut)
                    .setDateFin(dateFin)
                    .setNbPersonnes(nbPersonnes)
                    .build();

            PossibleResponse possibleResponse = agenceServiceStub.possible(possibleRequest);

            if (!possibleResponse.getPossible()) {
                printErrorBox(
                        "❌ Réservation Indisponible",
                        "Les paramètres de votre réservation ne sont pas valides.\n" +
                                "Malheureusement, nous n'avons plus de disponibilité pour les dates ou le nombre de personnes sélectionnés.\n" +
                                "Veuillez modifier ces informations et réessayer."
                );
                return;

            }

            // Client Information Collection



            String nomClient = getClientName(reader, "Nom du client");
            String prenomClient = getClientName(reader, "Prénom du client");
            String emailClient = getClientEmail(reader);
            long numeroCarte = getCreditCardNumber(reader);
            String dateExpiration = getCreditCardExpiration(reader);
            int cryptogramme = getCreditCardCVV(reader);

            // Reservation Request
            ReservationRequest reservationRequest = ReservationRequest.newBuilder()
                    .setIdOffre(idOffre)
                    .setIdAgence(idAgence)
                    .setNomClient(nomClient)
                    .setPrenomClient(prenomClient)
                    .setDateDebut(dateDebut)
                    .setDateFin(dateFin)
                    .setEmailClient(emailClient)
                    .setNumeroCarte(numeroCarte)
                    .setDateExpiration(dateExpiration)
                    .setCryptogramme(cryptogramme)
                    .setNbPersonnes(nbPersonnes)
                    .build();

            // Perform Reservation
            ReservationResponse reservationResponse = agenceServiceStub.reserver(reservationRequest);

            if (reservationResponse.getReserve()) {
                printSuccessReservation(reservationResponse.getReservation());
            } else {
                printErrorBox("❌ Réservation Impossible",
                        "La réservation n'a pas pu être finalisée.\n" +
                                "Veuillez réessayer .");
                return;
            }
        } catch (StatusRuntimeException e) {
            printErrorBox("🚨 Chat  Service",
                    "\n" +
                            e.getStatus().getDescription());
        } catch (Exception e) {
            printErrorBox("🔧 Erreur Système",
                    "Une erreur inattendue est survenue :\n" +
                            e.getMessage());
        }
    }

    private void printHeader() {
        System.out.println(ANSI_CYAN +
                "╔══════════════════════════════════════╗\n" +
                "║      🏨 RÉSERVATION HÔTELIÈRE 🏨    ║\n" +
                "╚══════════════════════════════════════╝" + ANSI_RESET);
    }

    private void printErrorBox(String title, String message) {
        // Déterminer la largeur maximale du contenu
        String[] lines = message.split("\n"); // Diviser les lignes du message
        int maxWidth = Math.max(title.length(),
                Arrays.stream(lines).mapToInt(String::length).max().orElse(0)) + 4; // +4 pour marges

        // Générer les bordures dynamiques
        String topBottomBorder = "╔" + "═".repeat(maxWidth) + "╗";
        String middleBorder = "╠" + "═".repeat(maxWidth) + "╣";

        // Construire la boîte
        StringBuilder box = new StringBuilder();
        box.append(ANSI_RED).append(topBottomBorder).append("\n");
        box.append("║ ").append(centerText(title, maxWidth - 2)).append(" ║\n");
        box.append(middleBorder).append("\n");

        // Ajouter chaque ligne du message
        for (String line : lines) {
            box.append("║ ").append(centerText(line, maxWidth - 2)).append(" ║\n");
        }

        box.append(topBottomBorder).append(ANSI_RESET);

        // Afficher la boîte
        System.out.println(box);
    }





    // Success reservation details
    private void printSuccessReservation(ReservationDTO reservation) {
        System.out.println(ANSI_GREEN +
                "╔══════════════════════════════════════╗\n" +
                "║       ✅ RÉSERVATION CONFIRMÉE       ║\n" +
                "╠══════════════════════════════════════╣");
        System.out.printf("║ %-36s ║\n", "🏨 Hôtel: " + reservation.getNomHotel());
        System.out.printf("║ %-36s ║\n", "📅 Du " + reservation.getDateDebut() + " au " + reservation.getDateFin());
        System.out.printf("║ %-36s ║\n", "👤 Client: " + reservation.getNomClient() + " " + reservation.getPrenomClient());
        System.out.printf("║ %-36s ║\n", "💰 Prix: " + reservation.getPrix() + " €");
        System.out.println(
                "╚══════════════════════════════════════╝" + ANSI_RESET);
    }


    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        int extraPadding = (width - text.length()) % 2; // Gérer les largeurs impaires
        return " ".repeat(padding) + text + " ".repeat(padding + extraPadding);
    }


    private int getCreditCardCVV(BufferedReader reader) throws NoSuchMethodException, IOException {
        while (true) {
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("🔒 ** PAIEMENT : Cryptogramme de la carte (CVV) **");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("💡 Conseil : Le CVV est composé de **3 chiffres**");
            System.out.println("             visibles au dos de votre carte.");
            System.out.print("➡️ Entrez votre CVV : ");
            String input = new StringInputProcessor(reader).process();
            if (input.matches("\\d{3}")) {
                System.out.println("\n✅ Votre CVV est valide. Merci !");
                return Integer.parseInt(input);
            }
            System.out.println("❌ Erreur : Le CVV doit contenir exactement **3 chiffres**.");
            System.out.println("↻ Veuillez réessayer.\n");
        }
    }

    private String getCreditCardExpiration(BufferedReader reader) throws NoSuchMethodException, IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        while (true) {
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📅 ** PAIEMENT : Date d'expiration de la carte **");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("💡 Format attendu : **MM/AA** (par exemple, 08/25 pour août 2025).");
            System.out.println("⚠️  La date doit être **future**.");
            System.out.print("➡️ Entrez la date d'expiration : ");
            String input = new StringInputProcessor(reader).process();
            try {
                LocalDate expirationDate = LocalDate.parse("01/" + input, DateTimeFormatter.ofPattern("dd/MM/yy"));
                if (expirationDate.isAfter(LocalDate.now())) {
                    System.out.println("\n✅ Date d'expiration validée !");
                    return input;
                } else {
                    System.out.println("❌ Erreur : La date doit être supérieure à la date actuelle.");
                    System.out.println("↻ Veuillez réessayer.\n");
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Erreur : Format invalide. Utilisez **MM/AA**.");
                System.out.println("↻ Veuillez réessayer.\n");
            }
        }
    }

    private long getCreditCardNumber(BufferedReader reader) throws IOException, NoSuchMethodException {
        while (true) {
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("💳 ** PAIEMENT : Numéro de carte bancaire **");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("💡 Format attendu : **16 chiffres** (sans espace ni caractères spéciaux).");
            System.out.print("➡️ Entrez votre numéro de carte : ");
            String input = new StringInputProcessor(reader).process();
            if (input.matches("\\d{16}")) {
                System.out.println("\n✅ Numéro de carte validé. Merci !");
                return Long.parseLong(input);
            }
            System.out.println("❌ Erreur : Le numéro doit contenir exactement **16 chiffres**.");
            System.out.println("↻ Veuillez réessayer.\n");
        }
    }





    // Les méthodes utilitaires restent les mêmes (getAuthenticationId, getDateInput, etc.)
    private int getAuthenticationId(BufferedReader reader) throws IOException, NoSuchMethodException {
        System.out.print("Identifiant: ");
        return new IntegerInputProcessor(reader).process();
    }

    private String getAuthenticationPassword(BufferedReader reader) throws IOException, NoSuchMethodException {
        System.out.print("Mot de passe: ");
        return new StringInputProcessor(reader).process();
    }

    private String getDateInput(BufferedReader reader, String type) throws IOException, NoSuchMethodException {
        System.out.print("Date de " + type + " (YYYY-MM-DD): ");
        return new DateInputProcessor(reader).process();
    }

    private int getNbPersonnes(BufferedReader reader) throws IOException, NoSuchMethodException {
        System.out.print("Nombre de personnes: ");
        return new IntegerInputProcessor(reader).process();
    }

    private int getOffreId(BufferedReader reader) throws IOException, NoSuchMethodException {
        System.out.print("ID de l'offre: ");
        return new IntegerInputProcessor(reader).process();
    }

    private String getClientName(BufferedReader reader, String type) throws IOException, NoSuchMethodException {
        System.out.print(type + ": ");
        return new StringInputProcessor(reader).process();
    }

    private String getClientEmail(BufferedReader reader) throws IOException, NoSuchMethodException {
        while (true) {
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📧 ** CONTACT : Adresse e-mail **");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("💡 Conseil : Entrez une adresse valide contenant le caractère `@`.");
            System.out.println("             Exemple : utilisateur@exemple.com");
            System.out.print("➡️ Votre e-mail : ");
            String email = new StringInputProcessor(reader).process();

            if (email.contains("@")) {
                System.out.println("\n✅ Adresse e-mail validée. Merci !");
                return email;
            }

            System.out.println("\n❌ Erreur : L'adresse e-mail doit contenir le caractère `@`.");
            System.out.println("↻ Veuillez réessayer avec une adresse valide.\n");
        }
    }



    private String getCityInput(BufferedReader reader) throws IOException, NoSuchMethodException {
        System.out.print("Ville: ");
        return new StringInputProcessor(reader).process();
    }

    private int getNbEtoilesInput(BufferedReader reader) throws IOException, NoSuchMethodException {
        System.out.print("Nombre d'étoiles minimum souhaité: ");
        return new IntegerInputProcessor(reader).process();
    }

    private String typeChambreToString(int nbPersonnes) {
        switch (nbPersonnes) {
            case 1: return "Simple 🛏️";
            case 2: return "Double 🛏🛏";
            case 3: return "Triple 🛏🛏🛏";
            default: return "Inconnu";
        }
    }

}