package com.unindetec;

public class DatabaseConfig {
    private final String url;
    private final String username;
    private final String password;

    DatabaseConfig() {
        this.url = Config.get("db.url");
        this.username = Config.get("db.user");
        this.password = Config.get("db.password");
    }

    String getUrl() { return url; }
    String getUsername() { return username; }
    String getPassword() { return password; }
}
