package be.pasque.UI;

import be.pasque.Model.Category;
import be.pasque.Model.Manager;
import be.pasque.Model.Ride;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ManagerFrame extends JFrame {

    private final Manager manager;
    private JComboBox<Category> categoryCombo;
    private JTable rideTable;
    private DefaultTableModel tableModel;

    public ManagerFrame(Manager manager) {
        this.manager = manager;

        setTitle("Responsable - Gestion des sorties");
        setSize(1150, 780);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 0));
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Gestionnaire : " + manager.getFirstName() + " " + manager.getLastName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Déconnexion");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(new Color(0, 102, 0));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());
        header.add(logoutBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(20, 20));
        center.setBorder(new EmptyBorder(20, 30, 30, 30));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.add(new JLabel("Catégorie gérée :"));
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        categoryCombo.setPreferredSize(new Dimension(380, 40));
        topPanel.add(categoryCombo);

        JButton newRideBtn = new JButton("Nouvelle sortie");
        newRideBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        newRideBtn.setBackground(new Color(0, 150, 0));
        newRideBtn.setForeground(Color.WHITE);
        newRideBtn.setFocusPainted(false);
        newRideBtn.setBorderPainted(false);
        newRideBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        newRideBtn.setPreferredSize(new Dimension(200, 45));
        newRideBtn.addActionListener(e -> openNewRideDialog());
        topPanel.add(newRideBtn);

        center.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Date", "Lieu de départ", "Tarif", "Inscrits"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override 
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
        };

        rideTable = new JTable(tableModel);
        rideTable.setRowHeight(52);
        rideTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        rideTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        rideTable.getTableHeader().setBackground(new Color(240, 255, 240));

        JScrollPane scroll = new JScrollPane(rideTable);
        center.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        loadManagedCategoriesAndRides();
        setVisible(true);
    }

    private void loadManagedCategoriesAndRides() {
        categoryCombo.removeAllItems();
        tableModel.setRowCount(0);

        List<Category> managedCategories = manager.getManagedCategories();

        if (managedCategories.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vous n'êtes responsable d'aucune catégorie.\nContactez l'administrateur.",
                "Aucune catégorie gérée", JOptionPane.WARNING_MESSAGE);
            categoryCombo.addItem(new Category() { 
                @Override 
                public String toString() { 
                    return "Aucune catégorie assignée"; 
                } 
            });
            categoryCombo.setEnabled(false);
            return;
        }

        for (Category cat : managedCategories) {
            categoryCombo.addItem(cat);
        }

        categoryCombo.setSelectedIndex(0);
        categoryCombo.addActionListener(e -> refreshRideTable());
        refreshRideTable();
    }

    private void refreshRideTable() {
        tableModel.setRowCount(0);
        Category selected = (Category) categoryCombo.getSelectedItem();
        if (selected == null || selected.getCalendar() == null) return;

        Category fresh = Category.findById(selected.getId());

        if (fresh == null || fresh.getCalendar() == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy 'à' HH:mm");

        for (Ride ride : fresh.getCalendar().getRides()) {
            int inscrits = ride.getInscriptions() != null ? ride.getInscriptions().size() : 0;
            tableModel.addRow(new Object[]{
                sdf.format(ride.getStartDate()),
                ride.getStartPlace(),
                ride.getFee() + " €",
                inscrits + " inscrit" + (inscrits > 1 ? "s" : "")
            });
        }
    }

    private void openNewRideDialog() {
        Category selectedCategory = (Category) categoryCombo.getSelectedItem();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une catégorie d'abord");
            return;
        }

        JDialog dialog = new JDialog(this, "Créer une nouvelle sortie", true);
        dialog.setSize(540, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField placeField = new JTextField(25);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy HH:mm");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());

        JTextField feeField = new JTextField("0", 10);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Lieu de départ :"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dialog.add(placeField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; dialog.add(new JLabel("Date et heure :"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dialog.add(dateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; dialog.add(new JLabel("Tarif (€) :"), gbc);
        gbc.gridx = 1; dialog.add(feeField, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton saveBtn = new JButton("Créer la sortie");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        saveBtn.setBackground(new Color(0, 150, 0));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setPreferredSize(new Dimension(240, 50));
        saveBtn.addActionListener(e -> {
            String place = placeField.getText().trim();
            if (place.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Le lieu est obligatoire");
                return;
            }

            double fee;
            try {
                fee = Double.parseDouble(feeField.getText().replace(",", "."));
                if (fee < 0) throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Tarif invalide");
                return;
            }

            Ride ride = new Ride();
            ride.setStartPlace(place);
            ride.setStartDate((Date) dateSpinner.getValue());
            ride.setFee(fee);

            try {

                manager.createRide(ride);
                
                JOptionPane.showMessageDialog(dialog, "Sortie créée avec succès !");
                dialog.dispose();
                loadManagedCategoriesAndRides(); 
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        dialog.add(saveBtn, gbc);
        dialog.setVisible(true);
    }

    private void logout() {
        dispose();
        new LoginFrame();
    }
}