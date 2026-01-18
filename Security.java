public class Security {
    // Simple Caesar Cipher (Shift by 3)
    // "abc" becomes "def"
    public static String hashPassword(String password) {
        StringBuilder result = new StringBuilder();
        int shift = 3; 

        for (char character : password.toCharArray()) {
            // lowercase letters
            if (Character.isLowerCase(character)) {
                character = (char) ((character - 'a' + shift) % 26 + 'a');
            }

            // uppercase letters
            else if (Character.isUpperCase(character)) {
                character = (char) ((character - 'A' + shift) % 26 + 'A');
            }

            // degits
            else if (Character.isDigit(character)) {
                character = (char) ((character - '0' + shift) % 10 + '0');
            }

            // Other characters (symbols) remain unchanged
            result.append(character);
        }
        return result.toString();
    }
}