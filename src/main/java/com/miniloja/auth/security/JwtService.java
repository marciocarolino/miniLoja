package com.miniloja.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    // ATENÇÃO: apenas para desenvolvimento local.
    // Em produção, mover para variável de ambiente / secret store.
    // Não registrar este valor em logs.
    private static final String DEFAULT_DEV_SECRET = "miniloja-dev-secret-change-me";

    private static final String HMAC_ALG = "HmacSHA256";
    private static final Duration DEFAULT_TTL = Duration.ofHours(2);

    public String generateToken(String subject) {
        return generateToken(subject, Instant.now(), DEFAULT_TTL);
    }

    public String generateToken(String subject, Instant now, Duration ttl) {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        long iat = now.getEpochSecond();
        long exp = now.plus(ttl).getEpochSecond();
        String payloadJson = "{\"sub\":\"" + escapeJson(subject) + "\",\"iat\":" + iat + ",\"exp\":" + exp + "}";

        String header = b64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = b64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = header + "." + payload;
        String signature = sign(signingInput, DEFAULT_DEV_SECRET);

        return signingInput + "." + signature;
    }

    public Optional<String> validateAndGetSubject(String token) {
        if (token == null || token.isBlank()) return Optional.empty();

        String[] parts = token.split("\\.");
        if (parts.length != 3) return Optional.empty();

        String signingInput = parts[0] + "." + parts[1];
        String expectedSig = sign(signingInput, DEFAULT_DEV_SECRET);

        if (!constantTimeEquals(expectedSig, parts[2])) return Optional.empty();

        String payloadJson = new String(b64UrlDecode(parts[1]), StandardCharsets.UTF_8);

        Long exp = extractLong(payloadJson, "exp");
        if (exp == null) return Optional.empty();

        if (Instant.now().getEpochSecond() >= exp) return Optional.empty();

        String sub = extractString(payloadJson, "sub");
        if (sub == null || sub.isBlank()) return Optional.empty();

        return Optional.of(sub);
    }

    private static String sign(String signingInput, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] sig = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            return b64Url(sig);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao assinar JWT", e);
        }
    }

    private static String b64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] b64UrlDecode(String b64Url) {
        return Base64.getUrlDecoder().decode(b64Url);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    // JSON extraction (minimista para payload controlado)
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractString(String json, String key) {
        String needle = "\"" + key + "\":\"";
        int idx = json.indexOf(needle);
        if (idx < 0) return null;
        int start = idx + needle.length();
        int end = json.indexOf('"', start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static Long extractLong(String json, String key) {
        String needle = "\"" + key + "\":";
        int idx = json.indexOf(needle);
        if (idx < 0) return null;
        int start = idx + needle.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        if (end == start) return null;
        return Long.parseLong(json.substring(start, end));
    }
}
