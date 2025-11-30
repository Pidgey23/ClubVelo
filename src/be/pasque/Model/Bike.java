package be.pasque.Model;

import be.pasque.DAO.BikeDAO;
import java.sql.SQLException;
import java.util.List;

public class Bike {
    private Long id;
    private double weight;
    private String type;
    private double length;
    private Member owner;
    private String customLabel = null;

    // DAO
    private final BikeDAO bikeDAO = new BikeDAO();

    public Bike() {}

    public Bike(double weight, String type, double length, Member owner) {
        this.weight = weight;
        this.type = type;
        this.length = length;
        this.owner = owner;
    }


    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public double getWeight() { 
        return weight; 
    }
    
    public void setWeight(double weight) { 
        if (weight <= 0) {
            throw new IllegalArgumentException("Le poids doit être positif");
        }
        this.weight = weight; 
    }

    public String getType() { 
        return type; 
    }
    
    public void setType(String type) { 
        this.type = type; 
    }

    public double getLength() { 
        return length; 
    }
    
    public void setLength(double length) { 
        if (length <= 0) {
            throw new IllegalArgumentException("La longueur doit être positive");
        }
        this.length = length; 
    }

    public Member getOwner() { 
        return owner; 
    }
    
    public void setOwner(Member owner) { 
        this.owner = owner; 
    }

    public String getCustomLabel() { 
        return customLabel; 
    }
    
    public void setCustomLabel(String label) { 
        this.customLabel = label; 
    }


    public void validate() {
        if (owner == null) {
            throw new IllegalStateException("Un vélo doit avoir un propriétaire");
        }
        
        if (weight <= 0) {
            throw new IllegalArgumentException("Le poids doit être positif");
        }
        
        if (length <= 0) {
            throw new IllegalArgumentException("La longueur doit être positive");
        }
        
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de vélo doit être spécifié");
        }
    }


    public boolean isValid() {
        try {
            validate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLightweight() {
        return weight < 10.0;
    }

    public boolean isLong() {
        return length > 2.0;
    }


    public String getFullDescription() {
        return String.format("%s - %.1f kg, %.2f m", 
            type != null ? type : "Vélo", 
            weight, 
            length);
    }


    public void save() throws SQLException {
        validate();
        
        if (this.id == null) {
            bikeDAO.insert(this);
        } else {
            bikeDAO.update(this);
        }
    }


    public void delete() throws SQLException {
        if (this.id == null) {
            throw new IllegalStateException("Impossible de supprimer un vélo non persisté");
        }
        bikeDAO.delete(this.id);
    }

    public static Bike findById(Long bikeId) {
        return new BikeDAO().findById(bikeId);
    }

    public static List<Bike> findByMember(Member member) {
        return new BikeDAO().findByMember(member);
    }

    public static List<Bike> findAll() {
        return new BikeDAO().findAll();
    }

    @Override
    public String toString() {
        if (customLabel != null) {
            return customLabel;
        }
        
        String typeStr = (type != null && !type.isEmpty()) ? type : "Vélo";
        return typeStr + " (" + weight + " kg)";
    }
}