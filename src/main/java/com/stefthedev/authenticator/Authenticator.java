package com.stefthedev.authenticator;

import com.stefthedev.authenticator.authentications.AuthenticationHandler;
import com.stefthedev.authenticator.authentications.AuthenticationListener;
import com.stefthedev.authenticator.commands.AuthenticatorCommand;
import com.stefthedev.authenticator.commands.subcommands.*;
import com.stefthedev.authenticator.utilities.Config;
import com.stefthedev.authenticator.utilities.Message;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;

public class Authenticator extends JavaPlugin {

    private AuthenticationHandler authenticationHandler;

    public void onEnable() {
        saveDefaultConfig();
        registerMessages(new Config(this, "messages"));

        authenticationHandler = new AuthenticationHandler(this);

        AuthenticatorCommand authenticatorCommand = new AuthenticatorCommand(this);
        authenticatorCommand.initialise(
                new AcceptCommand(this),
                new DenyCommand(this),
                new DisableCommand(this),
                new EnableCommand(this),
                new ResetCommand(this)
        );

        getCommand("authenticator").setExecutor(authenticatorCommand);

        registerListeners(new AuthenticationListener(this));
    }

    public void onDisable() {
        if(authenticationHandler != null) authenticationHandler.unload();
    }

    private void registerListeners(Listener... listeners) {
        Arrays.asList(listeners).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
    }

    private void registerMessages(Config config) {
        config.setup();
        Message.setConfiguration(config.getFileConfiguration());
        for(Message message: Message.values()) {
            if (config.getFileConfiguration().getString(message.getPath()) == null) {
                if(message.getList() != null) {
                    config.getFileConfiguration().set(message.getPath(), message.getList());
                } else {
                    config.getFileConfiguration().set(message.getPath(), message.getDef());
                }
            }
        }
        config.save();
    }

    public AuthenticationHandler getAuthenticationHandler() {
        return authenticationHandler;
    }
}
