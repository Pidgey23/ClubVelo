package be.pasque.Model;



public class Bike {
    private Long id;
    private double weight;
    private String type;
    private double length;
    private Member owner;
    private String customLabel = null;

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

    @Override
    public String toString() {
        if (customLabel != null) {
            return customLabel;
        }
        String typeStr = (type != null && !type.isEmpty()) ? type : "Vélo";
        return typeStr + " (" + weight + " kg)";
    }
}