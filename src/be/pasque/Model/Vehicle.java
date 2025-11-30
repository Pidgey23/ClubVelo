package be.pasque.Model;

public class Vehicle {
    private Long id;
    private int seatNumber = 5;
    private int bikeSpotNumber = 0;
    private Member driver;
    private String customLabel = null;

    public Vehicle() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public int getBikeSpotNumber() { return bikeSpotNumber; }
    public void setBikeSpotNumber(int bikeSpotNumber) { this.bikeSpotNumber = bikeSpotNumber; }

    public Member getDriver() { return driver; }
    public void setDriver(Member driver) { this.driver = driver; }

    public void setCustomLabel(String label) {
        this.customLabel = label;
    }

    @Override
    public String toString() {
        return customLabel != null ? customLabel : "Véhicule sans nom";
    }
}