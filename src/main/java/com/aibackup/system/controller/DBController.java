package com.aibackup.system.controller;

import org.springframework.web.bind.annotation.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class DBController {

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
            return "FAILED";
        }
    }
}