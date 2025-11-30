package be.pasque.Model;

import be.pasque.DAO.ManagerDAO;
import be.pasque.DAO.CategoryDAO;
import be.pasque.DAO.RideDAO;

import java.sql.SQLException;
import java.util.List;

public class Manager extends Person {
    
    private Category managedCategory;
    private final ManagerDAO managerDAO = new ManagerDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final RideDAO rideDAO = new RideDAO();

    public Manager() { 
        super(); 
    }

    public Manager(String lastName, String firstName, String phone, String email, String password) {
        super(lastName, firstName, phone, email, password);
    }


    public Category getManagedCategory() {
        if (managedCategory == null) {

            List<Category> allCategories = categoryDAO.findAllWithFullDetails();
            managedCategory = allCategories.stream()
                .filter(cat -> cat.getManager() != null && 
                              cat.getManager().getId().equals(this.getId()))
                .findFirst()
                .orElse(null);
        }
        return managedCategory;
    }

    public void setManagedCategory(Category managedCategory) {
        this.managedCategory = managedCategory;
    }


    public void reloadManagedCategory() {
        managedCategory = null;
        getManagedCategory();
    }


    public boolean hasCategory() {
        return getManagedCategory() != null;
    }


    public Ride createRide(Ride ride) throws SQLException {
        Category category = getManagedCategory();
        
        if (category == null) {
            throw new IllegalStateException("Vous ne gérez aucune catégorie");
        }
        
        if (ride == null) {
            throw new IllegalArgumentException("La sortie ne peut pas être null");
        }
        

        ride.setCategory(category);
        

        rideDAO.insert(ride);
        

        reloadManagedCategory();
        
        return ride;
    }


    public List<Ride> getManagedRides() {
        Category category = getManagedCategory();
        
        if (category == null || category.getCalendar() == null) {
            return List.of();
        }
        
        return category.getCalendar().getRides();
    }


    public List<Category> getManagedCategories() {
        return categoryDAO.findAllWithFullDetails().stream()
            .filter(cat -> cat.getManager() != null && 
                          cat.getManager().getId().equals(this.getId()))
            .toList();
    }


    public static Manager findByPersonId(Long personId) {
        return new ManagerDAO().getManagerByPersonId(personId);
    }
}