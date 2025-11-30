package be.pasque.Model;

import be.pasque.DAO.RideDAO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Ride {
    private Long id;
    private String startPlace;
    private Date startDate;
    private double fee;
    private Category category;

    private final List<Inscription> inscriptions = new ArrayList<>();
    private static final RideDAO rideDAO = new RideDAO();

    public Ride() {}

    public Ride(String startPlace, Date startDate, double fee, Category category) {
        this.startPlace = startPlace;
        this.startDate = startDate;
        this.fee = fee;
        this.category = category;
    }

    public void addInscription(Inscription inscription) {
        if (inscription != null && !inscriptions.contains(inscription)) {
            inscriptions.add(inscription);
            inscription.setRide(this);
        }
    }

    public List<Inscription> getInscriptions() {
        return inscriptions;
    }

    public int getDriversCount() {
        return (int) inscriptions.stream().filter(Inscription::isDriver).count();
    }

    public int getPassengersCount() {
        return (int) inscriptions.stream().filter(Inscription::isPassenger).count();
    }

    public int getBikeTransportRequests() {
        return (int) inscriptions.stream().filter(Inscription::isNeedsBikeTransport).count();
    }

    public int getAvailableSeats() {
        int offered = inscriptions.stream()
                .filter(Inscription::isDriver)
                .mapToInt(i -> i.getVehicleUsed() != null ? i.getVehicleUsed().getSeatNumber() - 1 : 0)
                .sum();
        int occupied = getPassengersCount();
        return offered - occupied;
    }

    public int getAvailableBikeSpots() {
        int offered = inscriptions.stream()
                .filter(Inscription::isDriver)
                .mapToInt(i -> i.getVehicleUsed() != null ? i.getVehicleUsed().getBikeSpotNumber() : 0)
                .sum();
        int bikesNeeded = getBikeTransportRequests();
        return offered - bikesNeeded;
    }

    public static Ride findByIdWithAllDetails(Long rideId) {
        return rideDAO.findByIdWithAllDetails(rideId);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStartPlace() { return startPlace; }
    public void setStartPlace(String startPlace) { this.startPlace = startPlace; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return startPlace + " - " + (startDate != null ? sdf.format(startDate) : "Date ?")
                + " | Places : " + getAvailableSeats()
                + " | Vélos : " + getAvailableBikeSpots()
                + " | Tarif : " + fee + "€";
    }
}