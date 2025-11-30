package be.pasque.DAO;
import be.pasque.utils.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DAO<T> {
    protected Connection connect = null;

    public DAO() {
        try {
            this.connect = DBConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    
    public abstract boolean create(T obj);

    
    public abstract boolean delete(T obj);

   
    public abstract boolean update(T obj);

   
    public abstract T find(int id);

   
    public void closeConnection() {
        if (connect != null) {
            try {
                connect.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}