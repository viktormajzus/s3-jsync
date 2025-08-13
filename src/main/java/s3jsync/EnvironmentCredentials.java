package s3jsync;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EnvironmentCredentials {
    private Map<String, String> env;
    private Boolean areValid;

    public EnvironmentCredentials() throws Throwable {
        areValid = false;

        Boolean isSuccess = load();
        if (!isSuccess) {
            throw(new Exception("Invalid credentials!"));
        }

        areValid = true;
    }

    public Boolean load() throws IOException {
        String credentials[] = new String[3];

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

    public Boolean getValidity() {
        return areValid;
    }

    public String getAccessKey() {
        return env.get("AWS_ACCESS_KEY_ID");
    }

    public String getSecretKey() {
        return env.get("AWS_SECRET_KEY");
    }

    public String getRegion() {
        return env.get("AWS_REGION");
    }

    // for support with old version, needs rewriting
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

        if(!isValidAccessKey(array[0]) || !isValidSecretKey(array[1]))
            return false;
        return isValidAccessKey(array[0]) &&
                isValidSecretKey(array[1]) &&
                isValidRegion(array[2]);
    }
}
