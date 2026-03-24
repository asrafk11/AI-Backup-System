package com.aibackup.system.controller;

import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class DBController {

    private final String backupDir = "C:/backup";

    // 🔹 TEST CONNECTION
    @PostMapping("/connect")
    public String connectDB(@RequestBody Map<String, String> data) {

        String url = data.get("url");
        String username = data.get("username");
        String password = data.get("password");

        try {
            Connection con = DriverManager.getConnection(url, username, password);
            con.close();
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED";
        }
    }

    // 🔹 BACKUP DATABASE
    @PostMapping("/backup")
    public String backupDB(@RequestBody Map<String, String> data) {

        String url = data.get("url");
        String user = data.get("username");
        String pass = data.get("password");

        try {

            // ✅ Create folder if not exists
            File dir = new File(backupDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String dbName = url.substring(url.lastIndexOf("/") + 1);
            String fileName = "backup_" + System.currentTimeMillis() + ".backup";

            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_dump.exe",
                    "-U", user,
                    "-F", "c",
                    "-f", backupDir + "/" + fileName,
                    dbName
            );

            pb.environment().put("PGPASSWORD", pass);

            Process process = pb.start();
            int exitCode = process.waitFor();

            return exitCode == 0 ? "SUCCESS" : "FAILED";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    // 🔹 RESTORE LATEST BACKUP
    @PostMapping("/restore")
    public String restoreLatest(@RequestBody Map<String, String> data) {

        String url = data.get("url");
        String user = data.get("username");
        String pass = data.get("password");

        try {

            String dbName = url.substring(url.lastIndexOf("/") + 1);

            File folder = new File(backupDir);
            File[] files = folder.listFiles();

            if (files == null || files.length == 0) {
                return "NO_BACKUP";
            }

            File latestFile = files[0];
            for (File f : files) {
                if (f.lastModified() > latestFile.lastModified()) {
                    latestFile = f;
                }
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_restore.exe",
                    "-U", user,
                    "-d", dbName,
                    "-c",
                    latestFile.getAbsolutePath()
            );

            pb.environment().put("PGPASSWORD", pass);

            Process process = pb.start();
            int exitCode = process.waitFor();

            return exitCode == 0 ? "SUCCESS" : "FAILED";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    // 🔹 RESTORE SELECTED FILE
    @PostMapping("/restore-selected")
    public String restoreSelected(@RequestBody Map<String, String> data) {

        String url = data.get("url");
        String user = data.get("username");
        String pass = data.get("password");
        String fileName = data.get("fileName");

        try {

            String dbName = url.substring(url.lastIndexOf("/") + 1);

            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_restore.exe",
                    "-U", user,
                    "-d", dbName,
                    "-c",
                    backupDir + "/" + fileName
            );

            pb.environment().put("PGPASSWORD", pass);

            Process process = pb.start();
            int exitCode = process.waitFor();

            return exitCode == 0 ? "SUCCESS" : "FAILED";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    // 🔹 GET BACKUP FILES
    @GetMapping("/backups")
    public List<String> getBackupFiles() {

        File folder = new File(backupDir);
        File[] files = folder.listFiles();

        List<String> fileNames = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                fileNames.add(file.getName());
            }
        }

        return fileNames;
    }
}