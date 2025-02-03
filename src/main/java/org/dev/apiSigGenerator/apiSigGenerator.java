package org.dev.apiSigGenerator;

import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@ApplicationScoped
public class apiSigGenerator {
    @ConfigProperty(name="codeforces.apiKey")
    String apiKey;

    @ConfigProperty(name="codeforces.apiSecret")
    String apiSecret;

    @ConfigProperty(name="codeforces.randomString")
    String randomString;

    public String createApiSig(String methodName, Map<String, String> params) {
        long time = Instant.now().getEpochSecond();

        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        System.out.println(sortedParams);
        sortedParams.put("apiKey", apiKey);
        sortedParams.put("time", String.valueOf(time));

        String queryString = sortedParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        String signString = String.format("%s/%s?%s#%s", randomString, methodName, queryString, apiSecret);
        return randomString + sha512(signString);
    }

    public static String sha512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }
    }
}
