// be.pasque.UI/RideDetailFrame.java
package be.pasque.UI;

import be.pasque.Model.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class RideDetailFrame extends JFrame {
    private Ride ride;
    private final Member currentMember;
    private JTextArea recap;

    public RideDetailFrame(Long rideId, Member member) {
        this.currentMember = member;
        reloadRide(rideId);

        setTitle("Sortie : " + ride.getStartPlace());
        setSize(950, 700);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    private void reloadRide(Long rideId) {
        this.ride = Ride.findByIdWithAllDetails(rideId);
    }

    public void refresh() {
        reloadRide(ride.getId());
        updateRecap();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy 'à' HH:mm");
        JLabel title = new JLabel("<html><h1>" + ride.getStartPlace() + "</h1><h3>" +
                sdf.format(ride.getStartDate()) + " • " + ride.getFee() + " €</h3></html>");
        add(title, BorderLayout.NORTH);

        recap = new JTextArea(15, 60);
        recap.setEditable(false);
        recap.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        recap.setBackground(new Color(245, 245, 245));
        updateRecap();
        add(new JScrollPane(recap), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        JButton driverBtn = new JButton("Je propose mon véhicule");
        JButton passengerBtn = new JButton("Je veux une place (avec/sans vélo)");
        JButton closeBtn = new JButton("Fermer");

        driverBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        passengerBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));

        driverBtn.setPreferredSize(new Dimension(280, 50));
        passengerBtn.setPreferredSize(new Dimension(280, 50));

        buttons.add(driverBtn);
        buttons.add(passengerBtn);
        buttons.add(closeBtn);
        add(buttons, BorderLayout.SOUTH);

        driverBtn.addActionListener(e -> proposeAsDriver());
        passengerBtn.addActionListener(e -> reserveAsPassenger());
        closeBtn.addActionListener(e -> dispose());
    }

    private void updateRecap() {
        recap.setText("");
        recap.append("RÉCAPITULATIF COVOITURAGE\n\n");

        recap.append("Places disponibles : " + ride.getAvailableSeats() + "\n");
        recap.append("Places vélo disponibles : " + ride.getAvailableBikeSpots() + "\n\n");
        recap.append("Conducteurs inscrits : " + ride.getDriversCount() + "\n");
        recap.append("Passagers inscrits : " + ride.getPassengersCount() + "\n");
        recap.append("Vélos à transporter : " + ride.getBikeTransportRequests() + "\n");
    }

    private void proposeAsDriver() {

        List<Vehicle> vehicles = currentMember.getVehicles();

        if (vehicles.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Vous n'avez aucun véhicule enregistré.\nVoulez-vous en ajouter un maintenant ?",
                    "Véhicule manquant", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                new VehicleFormFrame(currentMember, ride, this);
            }
            return;
        }

        Vehicle selected = (Vehicle) JOptionPane.showInputDialog(this,
                "Choisissez le véhicule à proposer :", "Conducteur",
                JOptionPane.QUESTION_MESSAGE, null,
                vehicles.toArray(), vehicles.get(0));

        if (selected == null) return;

        try {
            Inscription insc = new Inscription(ride, currentMember, true, false, false);
            insc.setVehicleUsed(selected);

            currentMember.registerForRide(insc);
            
            JOptionPane.showMessageDialog(this, "Vous êtes inscrit comme conducteur !");
            refresh();
            
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Déjà inscrit", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void reserveAsPassenger() {
        Object[] options = {"Avec mon vélo", "Sans vélo"};
        int choice = JOptionPane.showOptionDialog(this,
                "Avez-vous besoin de transporter votre vélo ?", "Passager",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == -1) return;

        boolean withBike = (choice == 0);
        Bike selectedBike = null;

        if (withBike) {
            List<Bike> bikes = currentMember.getBikes();

            if (bikes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Vous n'avez aucun vélo enregistré.\nInscription sans vélo.",
                    "Aucun vélo", JOptionPane.INFORMATION_MESSAGE);
                withBike = false;
            } else {
                selectedBike = (Bike) JOptionPane.showInputDialog(this,
                    "Choisissez le vélo à transporter :", "Transport vélo",
                    JOptionPane.QUESTION_MESSAGE, null,
                    bikes.toArray(), bikes.get(0));

                if (selectedBike == null) return; 
            }
        }

        try {
            Inscription insc = new Inscription(ride, currentMember, false, true, withBike);

            currentMember.registerForRide(insc);
            
            JOptionPane.showMessageDialog(this,
                withBike ? "Place réservée + transport vélo demandé !" : "Place réservée !");
            refresh();
            
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Déjà inscrit", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }
}