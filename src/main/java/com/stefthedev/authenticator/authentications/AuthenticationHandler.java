package com.stefthedev.authenticator.authentications;

import com.stefthedev.authenticator.Authenticator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AuthenticationHandler {

    private final Authenticator authenticator;
    private final File userDirectory;

    private final Set<Authentication> authentications;
    private final Set<AuthenticationRequest> authenticationRequests;

    private final Set<UUID> uuids;

    public AuthenticationHandler(Authenticator authenticator) {
        this.authenticator = authenticator;

        this.userDirectory = new File(authenticator.getDataFolder() + File.separator + "users" + File.separator);

        this.authentications = new HashSet<>();
        this.authenticationRequests = new HashSet<>();

        this.uuids = new HashSet<>();

        if (!userDirectory.isDirectory()) {
            userDirectory.mkdir();
        }
    }

    public Authentication load(UUID uuid) {
        File userFile = new File(userDirectory + File.separator + uuid.toString() + ".yml");
        if (!userFile.exists()) return null;
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(userFile); /*cba to remove the UUID configuration section*/
        String secretKey = new String(Base64.getDecoder().decode(Objects.requireNonNull(fileConfiguration.getString(uuid.toString() + ".key"))));
        boolean enabled = fileConfiguration.getBoolean(uuid.toString() + ".enabled");
        return new Authentication(uuid, secretKey, enabled);
    }

    void unload(UUID uuid) {
        Authentication authentication = getAuthentication(uuid);
        if (authentication == null) return;
        File userFile = new File(userDirectory + File.separator + uuid.toString() + ".yml");
        if (!userFile.exists()) return; /*shouldn't ever happen*/
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(userFile);
        if(authentication.getKey() != null) {
            String secretKey = new String((Base64.getEncoder().encode(authentication.getKey().getBytes())));
            fileConfiguration.set(authentication.getUuid().toString() + ".key", secretKey);
        } else {
            fileConfiguration.set(authentication.getUuid().toString() + ".key", "");
        }

        fileConfiguration.set(authentication.getUuid().toString() + ".enabled", authentication.isEnabled());
        uuids.remove(uuid);
        authenticator.saveConfig();
    }

    public void unload() {

        authentications.forEach(authentication -> {
            File userFile = new File(userDirectory + File.separator + authentication.getUuid().toString() + ".yml");
            if (!userFile.exists()) {
                try {
                    userFile.createNewFile();
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(userFile);
            if(authentication.getKey() != null) {
                String secretKey = new String((Base64.getEncoder().encode(authentication.getKey().getBytes())));
                fileConfiguration.set(authentication.getUuid().toString() + ".key", secretKey);
            } else {
                fileConfiguration.set(authentication.getUuid().toString() + ".key", "");
            }

            fileConfiguration.set(authentication.getUuid().toString() + ".enabled", authentication.isEnabled());
            try {
                fileConfiguration.save(userFile);
            } catch (IOException exc) {
                exc.printStackTrace();
            }
         });
    }

    public void add(Authentication authentication) {
        authentications.add(authentication);
    }

    void add(UUID uuid) {
        uuids.add(uuid);
    }

    public void add(AuthenticationRequest authenticationRequest) {
        authenticationRequests.add(authenticationRequest);
    }

    public void remove(AuthenticationRequest authenticationRequest) {
        authenticationRequests.remove(authenticationRequest);
    }

    public void remove(Authentication authentication) {
        authentications.remove(authentication);
    }

    void remove(UUID uuid) {
        uuids.remove(uuid);
    }

    public AuthenticationRequest getRequest(UUID uuid) {
        return authenticationRequests.stream().filter(req -> req.getUuid() == uuid).findFirst().orElse(null);
    }

    public Authentication getAuthentication(UUID uuid) {
        return authentications.stream().filter(authentication -> authentication.getUuid() == uuid).findFirst().orElse(null);
    }

    Set<UUID> getUuids() {
        return Collections.unmodifiableSet(uuids);
    }
}
