package be.pasque.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {


    private static final String DB_PATH = "DATABASE/ClubVeloPasqueJulien.accdb";

    private static final String URL = "jdbc:ucanaccess://" + DB_PATH 
            + ";memory=false"
            + ";immediatelyReleaseResources=true"
            + ";keepMirror=true";

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("Nouvelle connexion Access établie !");
            return conn;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver UCanAccess non trouvé. Vérifie ton classpath !", e);
        }
    }

}