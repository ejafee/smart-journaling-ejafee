import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter; // NEW IMPORT
import java.io.IOException; // NEW IMPORT
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static ArrayList<User> users = new ArrayList<>();
    private static User currentUser;

    public static void main(String[] args) {
        loadUserData();
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== SMART JOURNALING APP ===");
        
        // --- NEW START MENU LOOP ---
        while (currentUser == null) {
            System.out.println("\n1. Login");
            System.out.println("2. Register New User");
            System.out.println("3. Exit");
            System.out.print("> ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                performLogin(scanner);
            } else if (choice.equals("2")) {
                registerUser(scanner);
            } else if (choice.equals("3")) {
                System.exit(0);
            } else {
                System.out.println("Invalid option.");
            }
        }

        // Once logged in, show the Welcome Page
        showWelcomeAndMenu(scanner);
        scanner.close();
    }

    // --- NEW REGISTRATION METHOD ---
    private static void registerUser(Scanner scanner) {
        System.out.println("\n=== Registration ===");
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        
        // Simple check if email already exists
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                System.out.println("Error: Email already exists!");
                return;
            }
        }

        System.out.print("Enter Display Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        // 1. Hash the password immediately (Security Feature)
        String hashedPassword = Security.hashPassword(password);

        // 2. Add to Memory (so you can login right now)
        users.add(new User(email, name, hashedPassword));

        // 3. Append to UserData.txt
        try {
            // "true" means append mode (don't overwrite the whole file)
            FileWriter fw = new FileWriter("UserData.txt", true);
            fw.write(email + "\n");
            fw.write(name + "\n");
            fw.write(hashedPassword + "\n");
            fw.close();
            System.out.println("Registration Successful! You can now login.");
        } catch (IOException e) {
            System.out.println("Error saving user to file.");
        }
    }

    // --- MOVED LOGIN LOGIC HERE ---
    private static void performLogin(Scanner scanner) {
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (login(email, password)) {
            System.out.println("Login Successful!\n");
        } else {
            System.out.println("Invalid email or password. Please try again.");
        }
    }

    private static void showWelcomeAndMenu(Scanner scanner) {
        // ... (This part stays exactly the same as your previous code) ...
        LocalTime now = LocalTime.now();
        String greeting;
        if (now.isBefore(LocalTime.of(12, 0))) greeting = "Good Morning";
        else if (now.isBefore(LocalTime.of(17, 0))) greeting = "Good Afternoon";
        else greeting = "Good Evening";

        System.out.println(greeting + ", " + currentUser.getDisplayName());
        System.out.println("--------------------------------");

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
                JournalManager jm = new JournalManager(currentUser);
                jm.viewWeeklySummary(scanner);
            } else if (choice.equals("3")) {
                System.out.println("Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // --- EXISTING HELPER METHODS ---

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