package com.aibackup.system.config;

import com.aibackup.system.entity.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;

public class DynamicDBConnection {

    public static Connection getConnection(DatabaseConfig db) {

        try {
            String url;

            if ("postgres".equalsIgnoreCase(db.getDbType())) {
                url = "jdbc:postgresql://" + db.getHost() + ":" + db.getPort() + "/" + db.getDbName();
            } else if ("mysql".equalsIgnoreCase(db.getDbType())) {
                url = "jdbc:mysql://" + db.getHost() + ":" + db.getPort() + "/" + db.getDbName();
            } else {
                throw new RuntimeException("Unsupported DB type");
            }

            return DriverManager.getConnection(
                    url,
                    db.getUsername(),
                    db.getPassword()
            );

        } catch (Exception e) {
            throw new RuntimeException("Connection failed: " + e.getMessage());
        }
    }
}