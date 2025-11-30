package be.pasque.UI;

import be.pasque.Model.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class VehicleFormFrame extends JFrame {
    private final Member member;
    private final Ride ride;
    private final RideDetailFrame parent;

    public VehicleFormFrame(Member member, Ride ride, RideDetailFrame parent) {
        this.member = member;
        this.ride = ride;
        this.parent = parent;

        setTitle("Ajouter un véhicule");
        setSize(420, 320);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Nouveau véhicule", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        panel.add(new JLabel("Places (conducteur inclus) :"), gbc);
        gbc.gridx = 1;
        JSpinner seats = new JSpinner(new SpinnerNumberModel(5, 2, 9, 1));
        panel.add(seats, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Emplacements vélos :"), gbc);
        gbc.gridx = 1;
        JSpinner bikes = new JSpinner(new SpinnerNumberModel(0, 0, 6, 1));
        panel.add(bikes, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JButton save = new JButton("Enregistrer et proposer");
        save.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(save, gbc);

        save.addActionListener(e -> {
            try {
                Vehicle v = new Vehicle();
                v.setDriver(member);
                v.setSeatNumber((Integer) seats.getValue());
                v.setBikeSpotNumber((Integer) bikes.getValue());

                member.addVehicle(v);

                Inscription insc = new Inscription(ride, member, true, false, false);
                insc.setVehicleUsed(v);
                member.registerForRide(insc);

                JOptionPane.showMessageDialog(this, "Véhicule ajouté et inscrit !");
                parent.refresh();
                dispose();
                
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur de validation : " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, 
                    ex.getMessage(), 
                    "Déjà inscrit", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur technique : " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel);
        setVisible(true);
    }
}