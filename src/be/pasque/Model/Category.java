package be.pasque.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Category {
    private Long id;
    private CategoryType type;
    private Manager manager;
    private List<Member> members = new ArrayList<>();
    private Calendar calendar;

    public Category() {
        this.members = new ArrayList<>();
        this.calendar = new Calendar(this);
    }

    public Category(CategoryType type) {
        this.type = type;
        this.members = new ArrayList<>();
        this.calendar = new Calendar(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CategoryType getType() {
        return type;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
        if (manager != null) {
            manager.setManagedCategory(this);
        }
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public List<Member> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(Member member) {
        if (member != null && !members.contains(member)) {
            members.add(member);
        }
    }

    public List<Ride> getRides() {
        if (calendar == null) {
            return List.of();
        }
        return calendar.getRides();
    }

    public static List<Category> findAllWithFullDetails() {
        return new be.pasque.DAO.CategoryDAO().findAllWithFullDetails();
    }

    public static Category findById(Long categoryId) {
        return findAllWithFullDetails().stream()
            .filter(c -> c.getId().equals(categoryId))
            .findFirst()
            .orElse(null);
    }

    public void setTypeFromId() {
        if (this.id == null) {
            this.type = CategoryType.ROAD_BIKE;
            return;
        }
        this.type = switch (this.id.intValue()) {
            case 1 -> CategoryType.ROAD_BIKE;
            case 2 -> CategoryType.TRAIL;
            case 3 -> CategoryType.DOWNHILL;
            case 4 -> CategoryType.CROSS;
            default -> CategoryType.ROAD_BIKE;
        };
    }
    @Override
    public String toString() {
        return switch (type) {
            case ROAD_BIKE -> "Route";
            case TRAIL -> "Trail / VTT";
            case DOWNHILL -> "Descente";
            case CROSS -> "Cyclo-cross";
            default -> "Catégorie inconnue";
        };
    }
}