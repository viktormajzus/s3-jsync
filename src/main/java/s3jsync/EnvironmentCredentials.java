package s3jsync;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EnvironmentCredentials {
    private Map<String, String> env;
    private Boolean areValid;

    /** Constructor for environment variables class
     *
     * @throws Throwable Throws an exception if credentials aren't in a valid format
     */
    public EnvironmentCredentials() throws Throwable {
        areValid = false;

        Boolean isSuccess = load();
        if (!isSuccess) {
            throw(new Exception("Invalid credentials!"));
        }

        areValid = true;
    }

    /** Loads the environment variables into a private map
     *
     * @return Returns true on success, false otherwise
     */
    public Boolean load() {
        String[] credentials = new String[3];

        credentials[0] = System.getenv("AWS_ACCESS_KEY_ID").trim();
        credentials[1] = System.getenv("AWS_SECRET_KEY").trim();
        credentials[2] = System.getenv("AWS_REGION").trim();

        if(!areValidCredentials(credentials))
            return false;

        env = new HashMap<>();
        env.put("AWS_ACCESS_KEY_ID", credentials[0]);
        env.put("AWS_SECRET_KEY", credentials[1]);
        env.put("AWS_REGION", credentials[2]);

        return true;
    }

    /** Getter for validity of credentials
     *
     * @return Returns true if credentials are in valid format, false otherwise
     */
    public Boolean getValidity() {
        return areValid;
    }

    /** Getter for the access key id
     *
     * @return Access Key ID string
     */
    public String getAccessKey() {
        return env.get("AWS_ACCESS_KEY_ID");
    }

    /** Getter for the secret key
     *
     * @return Returns Secret Key string
     */
    public String getSecretKey() {
        return env.get("AWS_SECRET_KEY");
    }

    /** Getter for region
     *
     * @return Returns Region string
     */
    public String getRegion() {
        return env.get("AWS_REGION");
    }

    // for support with old version, needs rewriting
    /** Placeholder method for support with the old CredentialsManager class
     *
     * @deprecated Since it's not used. Added the tag to get rid of warnings
     * @param a Nothing
     * @param b Nothing
     * @param c Nothing
     */
    public void save(String a, String b, String c) {

    }

    private static final Pattern UPPER_ALNUM = Pattern.compile("^[A-Z0-9]+$");
    private Boolean isValidAccessKey(String accessKey) {
        if(accessKey == null || accessKey.isEmpty())
            return false;
        if(accessKey.length() < 16 || accessKey.length() > 128)
            return false;
        return UPPER_ALNUM.matcher(accessKey).matches();
    }

    private static final Pattern SECRET_LENIENT = Pattern.compile("^[A-Za-z0-9/+=]{40,}$");
    private Boolean isValidSecretKey(String secretKey) {
        if(secretKey == null || secretKey.isEmpty())
            return false;
        return SECRET_LENIENT.matcher(secretKey).matches();
    }

    private static final Pattern REGION = Pattern.compile("^[a-z]{2}(?:-[a-z]+)+-\\d+$");
    private Boolean isValidRegion(String region) {
        if(region == null || region.isEmpty())
            return false;
        return region.equals("aws-global") || REGION.matcher(region).matches();
    }

    private Boolean areValidCredentials(String[] array) {
        if(array == null || array.length == 0 || array.length > 3)
            return false;

        return isValidAccessKey(array[0]) &&
                isValidSecretKey(array[1]) &&
                isValidRegion(array[2]);
    }
}
