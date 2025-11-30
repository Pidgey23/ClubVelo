package be.pasque.Model;

import be.pasque.DAO.MemberDAO;
import be.pasque.DAO.MemberCategoryDAO;
import be.pasque.DAO.CategoryDAO;
import be.pasque.DAO.InscriptionDAO;
import be.pasque.DAO.VehicleDAO;
import be.pasque.DAO.BikeDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Member extends Person {
    private double balance = 0.0;
    private boolean membershipPaid = false;

    private List<Category> categories;
    private List<Vehicle> vehicles;
    private List<Bike> bikes;
    private List<Inscription> inscriptions;

    private final MemberDAO memberDAO = new MemberDAO();
    private final MemberCategoryDAO memberCategoryDAO = new MemberCategoryDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final BikeDAO bikeDAO = new BikeDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    public Member() { 
        super(); 
    }

    public Member(String lastName, String firstName, String phone, String email, String password) {
        super(lastName, firstName, phone, email, password);
    }

    public double getBalance() { 
        return balance; 
    }
    
    public void setBalance(double balance) { 
        this.balance = balance; 
    }
    
    public boolean isMembershipPaid() { 
        return membershipPaid; 
    }
    
    public void setMembershipPaid(boolean membershipPaid) { 
        this.membershipPaid = membershipPaid; 
    }

    public void addToBalance(double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        this.balance += amount;
        memberDAO.updateBalance(this.getId(), this.balance);
    }


    public void deductFromBalance(double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        if (this.balance < amount) {
            throw new IllegalArgumentException(
                "Solde insuffisant ! Solde actuel : " + String.format("%.2f €", this.balance)
            );
        }
        this.balance -= amount;
        memberDAO.updateBalance(this.getId(), this.balance);
    }

    public List<Category> getCategories() {
        if (categories == null) {
            categories = memberDAO.findCategoriesByMember(this);
        }
        return Collections.unmodifiableList(categories);
    }

    public void reloadCategories() {
        categories = memberDAO.findCategoriesByMember(this);
    }

    public boolean isInCategory(Category category) {
        return getCategories().stream()
            .anyMatch(c -> c.getId().equals(category.getId()));
    }

    public void joinCategory(Category category) throws SQLException {
        if (category == null) {
            throw new IllegalArgumentException("La catégorie ne peut pas être null");
        }

        reloadCategories();

        if (isInCategory(category)) {
            throw new IllegalStateException(
                "Vous êtes déjà inscrit dans la catégorie : " + category
            );
        }

        if (this.balance < 5.0) {
            throw new IllegalArgumentException(
                "Solde insuffisant ! Il faut 5 € pour s'inscrire à une nouvelle catégorie.\n" +
                "Solde actuel : " + String.format("%.2f €", this.balance)
            );
        }

        this.balance -= 5.0;
        memberDAO.updateBalance(this.getId(), this.balance);

        memberCategoryDAO.insert(this.getId(), category.getId());

        if (categories == null) {
            categories = new ArrayList<>();
        }
        categories.add(category);
    }

    public List<Category> getAvailableCategories() {
        List<Category> allCategories = categoryDAO.findAllWithFullDetails();
        List<Category> memberCategories = getCategories();

        return allCategories.stream()
            .filter(cat -> memberCategories.stream()
                .noneMatch(mc -> mc.getId().equals(cat.getId())))
            .toList();
    }

    public List<Vehicle> getVehicles() {
        if (vehicles == null) {
            vehicles = vehicleDAO.findByMember(this);
        }
        return Collections.unmodifiableList(vehicles);
    }

    public void reloadVehicles() {
        vehicles = vehicleDAO.findByMember(this);
    }
    
    public void addVehicle(Vehicle vehicle) throws SQLException {
        if (vehicle == null) {
            throw new IllegalArgumentException("Le véhicule ne peut pas être null");
        }
        
        vehicle.setDriver(this);
        vehicleDAO.insert(vehicle);
        
        if (vehicles == null) {
            vehicles = new ArrayList<>();
        }
        vehicles.add(vehicle);
    }

    public List<Bike> getBikes() {
        if (bikes == null) {
            bikes = bikeDAO.findByMember(this);
        }
        return Collections.unmodifiableList(bikes);
    }

    public void reloadBikes() {
        bikes = bikeDAO.findByMember(this);
    }

    public void addBike(Bike bike) throws SQLException {
        if (bike == null) {
            throw new IllegalArgumentException("Le vélo ne peut pas être null");
        }
        
        bike.setOwner(this);
        bikeDAO.insert(bike);
        

        if (bikes == null) {
            bikes = new ArrayList<>();
        }
        bikes.add(bike);
    }

    public List<Inscription> getInscriptions() {
        if (inscriptions == null) {
            inscriptions = inscriptionDAO.findByMember(this);
        }
        return Collections.unmodifiableList(inscriptions);
    }


    public void reloadInscriptions() {
        inscriptions = inscriptionDAO.findByMember(this);
    }

    public boolean isRegisteredForRide(Ride ride) {
        return getInscriptions().stream()
            .anyMatch(i -> i.getRide().getId().equals(ride.getId()));
    }


    public void registerForRide(Inscription inscription) throws SQLException {
        if (inscription == null) {
            throw new IllegalArgumentException("L'inscription ne peut pas être null");
        }
        
        if (isRegisteredForRide(inscription.getRide())) {
            throw new IllegalStateException("Vous êtes déjà inscrit à cette sortie");
        }
        
        inscriptionDAO.save(inscription);
        

        if (inscriptions == null) {
            inscriptions = new ArrayList<>();
        }
        inscriptions.add(inscription);
    }


    public static List<Member> findAll() {
        return new MemberDAO().findAllMembers();
    }


    public static Member findByPersonId(Long personId) {
        return new MemberDAO().getMemberByPersonId(personId);
    }


    public static Member findByEmailAndPassword(String email, String password) {
        return new MemberDAO().findByEmailAndPassword(email, password);
    }
}