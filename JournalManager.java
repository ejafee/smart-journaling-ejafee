import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class JournalManager {
    private ArrayList<JournalEntry> journals = new ArrayList<>();
    private String userEmail;
    private String fileName = "journals.txt";

    private static final String WEATHER_API_URL = "https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1";
    private static final String MOOD_API_URL = "https://router.huggingface.co/hf-inference/models/distilbert/distilbert-base-uncased-finetuned-sst-2-english";
    private API api = new API();

    public JournalManager(User user) {
        this.userEmail = user.getEmail();
        loadJournals();
    }

    // 1. MAIN MENU FOR JOURNALS
    public void manageJournals(Scanner scanner) {
        while (true) {
            System.out.println("\n=== Journal Dates ===");
            
            // Sort by date
            journals.sort(Comparator.comparing(JournalEntry::getDate));

            // Display existing journals
            int count = 1;
            for (JournalEntry entry : journals) {
                System.out.println(count + ". " + entry.getDate());
                count++;
            }

            // Check if "Today" is already in the list
            String today = LocalDate.now().toString();
            JournalEntry todayEntry = null;
            for (JournalEntry entry : journals) {
                if (entry.getDate().equals(today)) {
                    todayEntry = entry;
                    break;
                }
            }

            // If today DOES NOT exist, show it as the last option to CREATE
            if (todayEntry == null) {
                System.out.println(count + ". " + today + " (Create New)");
            }

            System.out.println("0. Back to Main Menu");
            System.out.print("Select a date: ");
            
            String choiceStr = scanner.nextLine();
            try {
                int choice = Integer.parseInt(choiceStr);
                if (choice == 0) return; // Go back

                if (todayEntry == null && choice == count) {
                    // CASE 1: Create New Journal for Today
                    createJournal(scanner, today);
                } 
                else if (todayEntry != null && choice == (journals.indexOf(todayEntry) + 1)) {
                    // CASE 2: Today Exists -> Ask to View or Edit
                    handleExistingToday(scanner, todayEntry);
                } 
                else if (choice > 0 && choice <= journals.size()) {
                    // CASE 3: View Past Journal
                    viewJournal(journals.get(choice - 1));
                } 
                else {
                    System.out.println("Invalid selection.");
                }

            } catch (Exception e) {
                System.out.println("Invalid input.");
            }
        }
    }

    // --- SUB-MENUS ---

    private void handleExistingToday(Scanner scanner, JournalEntry entry) {
        System.out.println("\nEntry for today exists.");
        System.out.println("1. View Journal");
        System.out.println("2. Edit Journal");
        System.out.print("> ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            viewJournal(entry);
        } else if (choice.equals("2")) {
            editJournal(scanner, entry);
        } else {
            System.out.println("Invalid option.");
        }
    }

    // --- ACTIONS ---

    private void viewJournal(JournalEntry entry) {
        System.out.println("\n=== Journal Entry for " + entry.getDate() + " ===");
        System.out.println("Weather: " + entry.getWeather());
        System.out.println("Mood: " + entry.getMood());
        System.out.println("--------------------------------");
        System.out.println(entry.getContent());
        System.out.println("--------------------------------");
        System.out.println("Press Enter to go back.");
        new Scanner(System.in).nextLine(); // Wait for user
    }

    private void createJournal(Scanner scanner, String date) {
        System.out.println("\nWriting Journal for: " + date);
        System.out.print("Write your thoughts: ");
        String content = scanner.nextLine();

        System.out.println(">> Fetching weather data...");
        String weather = fetchWeather();
        
        System.out.println(">> Analyzing your mood...");
        String mood = determineMood(content);

        JournalEntry newEntry = new JournalEntry(date, content, weather, mood);
        journals.add(newEntry);
        saveJournals(); 
        System.out.println("Journal saved successfully!");
    }

    private void editJournal(Scanner scanner, JournalEntry entry) {
        System.out.println("\n=== Edit Journal (" + entry.getDate() + ") ===");
        System.out.println("Current: " + entry.getContent());
        System.out.print("New Content: ");
        String newContent = scanner.nextLine();

        // Update content
        entry.setContent(newContent);
        
        // Re-analyze mood (optional, but good for "Smart" features)
        System.out.println(">> Re-analyzing mood...");
        entry.setMood(determineMood(newContent));

        saveJournals();
        System.out.println("Journal updated!");
    }

    // --- FILE I/O (IMPROVED) ---

    private void loadJournals() {
        journals.clear();
        // Use a Map to automatically handle duplicates (edits overwriting old ones)
        Map<String, JournalEntry> tempMap = new HashMap<>();

        try {
            File file = new File(fileName);
            if (!file.exists()) return;

            Scanner fileScanner = new Scanner(file);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(";");
                // parts[0]=email, [1]=date, [2]=weather, [3]=mood, [4]=content
                if (parts.length >= 5 && parts[0].equals(userEmail)) {
                    // Put into map: Key = Date. 
                    // If date exists again later in file, it overwrites the old one.
                    tempMap.put(parts[1], new JournalEntry(parts[1], parts[4], parts[2], parts[3]));
                }
            }
            fileScanner.close();
            
            // Convert Map back to List
            journals.addAll(tempMap.values());

        } catch (Exception e) {
            System.out.println("Error loading journals.");
        }
    }

    private void saveJournals() {
        try {
            // Append mode: We add the new/edited entry to the end of the file.
            // The Load logic above handles the duplicates by only keeping the latest one.
            FileWriter fw = new FileWriter(fileName, true); 
             
            // Find today's entry in the list
            String today = LocalDate.now().toString();
            for(JournalEntry j : journals) {
                if(j.getDate().equals(today)) {
                     fw.write(userEmail + ";" + j.toFileFormat() + "\n");
                }
            }
            fw.close();
            
        } catch (IOException e) {
            System.out.println("Error saving journal.");
        }
    }
    // 5. WEEKLY SUMMARY
    public void viewWeeklySummary(Scanner scanner) {
        System.out.println("\n=== Weekly Summary (Past 7 Days) ===");
        
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minusDays(7);
        
        // Sort entries by date
        journals.sort(Comparator.comparing(JournalEntry::getDate));

        boolean found = false;
        int positiveCount = 0;
        int negativeCount = 0;

        // Print Table Header
        System.out.printf("%-15s %-30s %-15s\n", "Date", "Weather", "Mood");
        System.out.println("-------------------------------------------------------------");

        for (JournalEntry entry : journals) {
            try {
                LocalDate entryDate = LocalDate.parse(entry.getDate());
                
                // Check if date is within the last 7 days
                if ((entryDate.isAfter(oneWeekAgo) || entryDate.isEqual(oneWeekAgo)) && 
                    (entryDate.isBefore(today) || entryDate.isEqual(today))) {
                    
                    // Print row
                    System.out.printf("%-15s %-30s %-15s\n", 
                        entry.getDate(), 
                        shorten(entry.getWeather(), 28), // Truncate long weather text
                        entry.getMood()
                    );
                    
                    if(entry.getMood().equalsIgnoreCase("POSITIVE")) positiveCount++;
                    if(entry.getMood().equalsIgnoreCase("NEGATIVE")) negativeCount++;
                    
                    found = true;
                }
            } catch (Exception e) {
                // Ignore parsing errors for invalid dates
            }
        }

        if (!found) {
            System.out.println("No entries found for the past week.");
        } else {
            System.out.println("-------------------------------------------------------------");
            System.out.println("Summary: " + positiveCount + " Positive days, " + negativeCount + " Negative days.");
        }
        
        System.out.println("\nPress Enter to return.");
        scanner.nextLine();
    }
    
    // Helper to shorten long text (like weather descriptions) so the table looks neat
    private String shorten(String text, int limit) {
        if (text == null) return "-";
        if (text.length() <= limit) return text;
        return text.substring(0, limit - 3) + "...";
    }

    // --- API HELPER METHODS ---

    private String fetchWeather() {
        try {
            String response = api.get("https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1"); 
            String key = "\"summary_forecast\":\"";
            int startIndex = response.indexOf(key);
            if (startIndex != -1) {
                startIndex += key.length();
                int endIndex = response.indexOf("\"", startIndex);
                return response.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
             // System.out.println(e.getMessage()); 
        }
        return "Unknown"; 
    }

private String determineMood(String text) {
        try {
            // 1. Load the Environment Variables (Just like in API.java)
            Map<String, String> env = EnvLoader.loadEnv(".env");
            String token = env.get("BEARER_TOKEN");

            // Check if token exists to avoid crashing
            if (token == null || token.isEmpty()) {
                System.out.println(">> Error: Token not found in .env");
                return "Unknown";
            }

            // 2. Create the JSON Body
            String jsonBody = "{\"inputs\": \"" + text + "\"}";
            
            // 3. Call POST with ALL 3 REQUIRED ARGUMENTS: URL, Token, Body
            String response = api.post(MOOD_API_URL, token, jsonBody);

            // 4. Extract the result
            String key = "\"label\":\"";
            int startIndex = response.indexOf(key);
            if (startIndex != -1) {
                startIndex += key.length();
                int endIndex = response.indexOf("\"", startIndex);
                return response.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            System.out.println(">> Warning: Mood analysis failed. " + e.getMessage());
        }
        return "Neutral"; 
    }
}