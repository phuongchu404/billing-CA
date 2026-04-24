package com.rs.subscription.security;

import com.rs.subscription.entity.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

@Service
@Slf4j
public class JwtService {

    @Value("${sms.auth.jwt.access-token-ttl:3600}")
    private long accessTokenTtl;

    private final PrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtService(
        @Value("${sms.auth.jwt.private-key-path:classpath:keys/private.pem}") String privateKeyPath,
        @Value("${sms.auth.jwt.public-key-path:classpath:keys/public.pem}") String publicKeyPath
    ) {
        PrivateKey pk = null;
        RSAPublicKey pubKey = null;
        try {
            pk = loadPrivateKey(privateKeyPath);
            pubKey = (RSAPublicKey) loadPublicKey(publicKeyPath);
            log.info("Loaded RSA key pair from configured paths.");
        } catch (Exception e) {
            log.warn("Could not load RSA key files ({}), generating ephemeral key pair for dev. " +
                     "Configure SMS_JWT_PRIVATE_KEY_PATH for production.", e.getMessage());
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(2048);
                KeyPair kp = kpg.generateKeyPair();
                pk = kp.getPrivate();
                pubKey = (RSAPublicKey) kp.getPublic();
            } catch (Exception ex) {
                log.error("Failed to generate fallback key pair", ex);
            }
        }
        this.privateKey = pk;
        this.publicKey = pubKey;
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = readPemContent(path);
        pem = pem.replaceAll("-----[^-]+-----", "").replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        String pem = readPemContent(path);
        pem = pem.replaceAll("-----[^-]+-----", "").replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String readPemContent(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String resource = path.substring("classpath:".length());
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
                if (is == null) throw new IOException("Classpath resource not found: " + resource);
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } else {
            java.nio.file.Path filePath = java.nio.file.Paths.get(path);
            return java.nio.file.Files.readString(filePath, StandardCharsets.UTF_8);
        }
    }

    public String generateAccessToken(UserAccount user) {
        return generateAccessToken(user, Collections.emptyList());
    }

    public String generateAccessToken(UserAccount user, List<String> permissions) {
        List<String> roles = user.getRoles().stream()
            .map(r -> r.getRoleName())
            .collect(Collectors.toList());

        return Jwts.builder()
            .subject(user.getUserId())
            .claim("username", user.getUsername())
            .claim("roles", roles)
            .claim("permissions", permissions)
            .claim("auth_provider", "LOCAL")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenTtl * 1000))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String getUserIdFromToken(String token) {
        return validateAndGetClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = validateAndGetClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return Collections.emptyList();
    }

    public String getAuthProvider(String token) {
        return (String) validateAndGetClaims(token).get("auth_provider");
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public Map<String, Object> getJwks() {
        RSAPublicKey pub = publicKey;
        byte[] modulus = pub.getModulus().toByteArray();
        byte[] exponent = pub.getPublicExponent().toByteArray();
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("kty", "RSA");
        key.put("use", "sig");
        key.put("alg", "RS256");
        key.put("kid", "rs-sms-key-1");
        key.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(modulus));
        key.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(exponent));
        Map<String, Object> jwks = new LinkedHashMap<>();
        jwks.put("keys", List.of(key));
        return jwks;
    }
}
