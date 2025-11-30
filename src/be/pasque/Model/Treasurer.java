package be.pasque.Model;

import be.pasque.DAO.TreasurerDAO;
import be.pasque.DAO.MemberDAO;
import be.pasque.DAO.InscriptionDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Treasurer extends Person {
    
    private final TreasurerDAO treasurerDAO = new TreasurerDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    public Treasurer() { 
        super(); 
    }

    public Treasurer(String lastName, String firstName, String phone, String email, String password) {
        super(lastName, firstName, phone, email, password);
    }

    public List<Member> getAllMembers() {
        return memberDAO.findAllMembers();
    }

    public List<Member> getUnpaidMembers() {
        return getAllMembers().stream()
            .filter(m -> !m.isMembershipPaid())
            .sorted((a, b) -> a.getLastName().compareToIgnoreCase(b.getLastName()))
            .collect(Collectors.toList());
    }

    public List<Member> getPaidMembers() {
        return getAllMembers().stream()
            .filter(Member::isMembershipPaid)
            .sorted((a, b) -> a.getLastName().compareToIgnoreCase(b.getLastName()))
            .collect(Collectors.toList());
    }

    public void creditMemberBalance(Member member, double amount) throws SQLException {
        if (member == null) {
            throw new IllegalArgumentException("Le membre ne peut pas être null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        
        member.addToBalance(amount);
    }

    public void markMembershipAsPaid(Member member) throws SQLException {
        if (member == null) {
            throw new IllegalArgumentException("Le membre ne peut pas être null");
        }
        
        member.setMembershipPaid(true);
        memberDAO.updateMembershipStatus(member.getId(), true);
    }

    public List<Member> getAllDrivers() throws SQLException {
        List<Long> driverMemberIds = inscriptionDAO.findDistinctDriverMemberIds();
        
        return getAllMembers().stream()
            .filter(m -> driverMemberIds.contains(m.getId()))
            .sorted((a, b) -> a.getLastName().compareToIgnoreCase(b.getLastName()))
            .collect(Collectors.toList());
    }

    public void payAllDrivers() throws SQLException {
        List<Member> drivers = getAllDrivers();
        
        for (Member driver : drivers) {
            creditMemberBalance(driver, 10.0);
        }
    }

    public void payDriver(Member driver, double amount) throws SQLException {
        if (driver == null) {
            throw new IllegalArgumentException("Le conducteur ne peut pas être null");
        }
        
        creditMemberBalance(driver, amount);
    }

    public double getTotalBalance() {
        return getAllMembers().stream()
            .mapToDouble(Member::getBalance)
            .sum();
    }

    public int getPaidMembersCount() {
        return (int) getAllMembers().stream()
            .filter(Member::isMembershipPaid)
            .count();
    }

    public int getUnpaidMembersCount() {
        return (int) getAllMembers().stream()
            .filter(m -> !m.isMembershipPaid())
            .count();
    }

    public static Treasurer findByPersonId(Long personId) {
        return new TreasurerDAO().getTreasurerByPersonId(personId);
    }
}