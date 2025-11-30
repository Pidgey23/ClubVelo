package be.pasque.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Calendar {
    private Category category;
    private final List<Ride> rides = new ArrayList<>();

    public Calendar() {}

    public Calendar(Category category) {
        this.category = category;
    }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<Ride> getRides() { return Collections.unmodifiableList(rides); }

    public void addRide(Ride ride) {
        if (ride != null && !rides.contains(ride)) {
            rides.add(ride);
            if (ride.getCategory() == null || ride.getCategory() != category) {
                ride.setCategory(category);
            }
        }
    }
}