public class User {
    private String email;
    private String displayName;
    private String password;

    public User(String email, String displayName, String password) {
        this.email = email;
        this.displayName = displayName;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean validatePassword(String inputPassword) {
        // 1 hash the input password the user just typed
        String hashedInput = Security.hashPassword(inputPassword);
        
        // 2 compare it with the stored hash from the file
        return this.password.equals(hashedInput);
    }
}