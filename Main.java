import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime; // Imported for time checking
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static ArrayList<User> users = new ArrayList<>();
    private static User currentUser;

    public static void main(String[] args) {
        loadUserData();
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== SMART JOURNALING APP ===");
        
        // Login Loop
        while (currentUser == null) {
            System.out.print("Enter Email: ");
            String email = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            if (login(email, password)) {
                System.out.println("Login Successful!\n");
                // TRIGGER THE WELCOME PAGE HERE
                showWelcomeAndMenu(scanner); 
            } else {
                System.out.println("Invalid email or password. Please try again.\n");
            }
        }
        scanner.close();
    }

    private static void showWelcomeAndMenu(Scanner scanner) {
        // 1. Determine Greeting based on Time (Requirements page 4)
        LocalTime now = LocalTime.now();
        String greeting;

        if (now.isBefore(LocalTime.of(12, 0))) {
            greeting = "Good Morning";
        } else if (now.isBefore(LocalTime.of(17, 0))) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        // 2. Print Greeting + Display Name
        System.out.println(greeting + ", " + currentUser.getDisplayName());
        System.out.println("--------------------------------");

        // 3. Show Main Menu
        while (true) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Create, Edit & View Journals");
            System.out.println("2. View Weekly Mood Summary");
            System.out.println("3. Exit");
            System.out.print("> ");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                JournalManager jm = new JournalManager(currentUser);
                jm.manageJournals(scanner);
            } else if (choice.equals("2")) {
                // 1. create the manager
                JournalManager jm = new JournalManager(currentUser);
                // 2. calling method
                jm.viewWeeklySummary(scanner);
            } else if (choice.equals("3")) {
                System.out.println("Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // --- EXISTING METHODS BELOW ---

    private static void loadUserData() {
        try {
            File file = new File("UserData.txt");
            Scanner fileScanner = new Scanner(file);
            while (fileScanner.hasNextLine()) {
                String email = fileScanner.nextLine();
                if (!fileScanner.hasNextLine()) break;
                String name = fileScanner.nextLine();
                if (!fileScanner.hasNextLine()) break;
                String pass = fileScanner.nextLine();
                users.add(new User(email, name, pass));
            }
            fileScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: UserData.txt not found.");
        }
    }

    private static boolean login(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equals(email) && u.validatePassword(password)) {
                currentUser = u;
                return true;
            }
        }
        return false;
    }
}