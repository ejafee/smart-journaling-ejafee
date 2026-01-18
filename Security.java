public class Security {
    // Simple Caesar Cipher (Shift by 3)
    // "abc" becomes "def"
    public static String hashPassword(String password) {
        StringBuilder result = new StringBuilder();
        int shift = 3; 

        for (char character : password.toCharArray()) {
            if (Character.isLetter(character)) {
                char base = Character.isLowerCase(character) ? 'a' : 'A';
                // Rotate the letter
                character = (char) ((character - base + shift) % 26 + base);
            }
            result.append(character);
        }
        return result.toString();
    }
}