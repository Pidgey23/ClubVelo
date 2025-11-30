package be.pasque.UI;

import be.pasque.Model.Category;
import be.pasque.Model.Member;
import be.pasque.Model.Ride;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 2218966554176075726L;
    private final Member currentMember;
    private JComboBox<Category> categoryCombo;
    private JTable rideTable;
    private DefaultTableModel tableModel;

    public MainFrame(Member member) {
        this.currentMember = member;

        setTitle("Club Cycliste - Tableau de bord");
        setSize(1080, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel welcomeLabel = new JLabel("Connecté : ");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        header.add(welcomeLabel, BorderLayout.WEST);

        JLabel nameLabel = new JLabel(member.getFirstName() + " " + member.getLastName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        nameLabel.setForeground(Color.WHITE);
        header.add(nameLabel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        JButton joinCategoryBtn = new JButton("S'inscrire à une catégorie");
        joinCategoryBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        joinCategoryBtn.setBackground(new Color(34, 139, 34));
        joinCategoryBtn.setForeground(Color.BLUE);
        joinCategoryBtn.setFocusPainted(false);
        joinCategoryBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        joinCategoryBtn.addActionListener(e -> showJoinCategoryDialog());
        rightPanel.add(joinCategoryBtn);

        JButton logoutBtn = new JButton("Déconnexion");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(new Color(0, 102, 204));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        rightPanel.add(logoutBtn);

        header.add(rightPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topPanel.add(new JLabel("Choisir une catégorie :"));
        categoryCombo = new JComboBox<>();
        categoryCombo.setPreferredSize(new Dimension(350, 40));
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        topPanel.add(categoryCombo);

        center.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Date", "Lieu", "Tarif", "Action", "ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override 
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        rideTable = new JTable(tableModel);
        rideTable.setRowHeight(60);
        rideTable.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        rideTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 17));
        rideTable.getTableHeader().setBackground(new Color(240, 248, 255));
        rideTable.removeColumn(rideTable.getColumn("ID"));

        rideTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        rideTable.getColumn("Action").setCellEditor(new ButtonEditor(currentMember));

        JScrollPane scrollPane = new JScrollPane(rideTable);
        center.add(scrollPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        loadCategoriesAndRides();
        setVisible(true);
    }

    private void loadCategoriesAndRides() {
        categoryCombo.removeAllItems();
        tableModel.setRowCount(0);

        List<Category> memberCategories = currentMember.getCategories();

        if (memberCategories.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vous n'êtes inscrit dans aucune catégorie.\nContactez un responsable pour vous ajouter.",
                "Accès restreint", JOptionPane.WARNING_MESSAGE);
            categoryCombo.addItem(new Category() {
                @Override 
                public String toString() { 
                    return "Aucune catégorie disponible"; 
                }
            });
            categoryCombo.setEnabled(false);
            return;
        }

        for (Category cat : memberCategories) {
            categoryCombo.addItem(cat);
        }

        categoryCombo.setSelectedIndex(0);
        categoryCombo.addActionListener(e -> refreshRideTableWithReload());
        refreshRideTableWithReload();
    }

    private void refreshRideTableWithReload() {
        tableModel.setRowCount(0);
        Category selected = (Category) categoryCombo.getSelectedItem();
        if (selected == null) return;

        Category fresh = Category.findById(selected.getId());

        if (fresh == null || fresh.getCalendar() == null || fresh.getCalendar().getRides().isEmpty()) {
            tableModel.addRow(new Object[]{"Aucune sortie prévue pour le moment", "", "", null, null});
            rideTable.getColumn("Action").setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                JLabel lbl = new JLabel("—");
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setForeground(Color.GRAY);
                lbl.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                return lbl;
            });
            return;
        }

        rideTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        rideTable.getColumn("Action").setCellEditor(new ButtonEditor(currentMember));

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy 'à' HH:mm");

        for (Ride ride : fresh.getCalendar().getRides()) {
            tableModel.addRow(new Object[]{
                sdf.format(ride.getStartDate()),
                ride.getStartPlace(),
                ride.getFee() + " €",
                "Participer",
                ride.getId()
            });
        }
    }

    private void showJoinCategoryDialog() {

        List<Category> available = currentMember.getAvailableCategories();

        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vous êtes déjà inscrit dans toutes les catégories disponibles !",
                "Félicitations !", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<Category> combo = new JComboBox<>(available.toArray(new Category[0]));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                if (value instanceof Category c) {
                    setText(c + " — 5 €");
                }
                return this;
            }
        });

        int result = JOptionPane.showOptionDialog(
            this,
            new Object[]{"Choisissez la catégorie à rejoindre :", combo},
            "S'inscrire à une nouvelle catégorie",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, null, null);

        if (result != JOptionPane.OK_OPTION) return;

        Category selectedCategory = (Category) combo.getSelectedItem();

        try {

            currentMember.joinCategory(selectedCategory);

            JOptionPane.showMessageDialog(this,
                "Inscription réussie dans la catégorie « " + selectedCategory + " » !\n" +
                "5 € ont été débités de votre solde.\n" +
                "Nouveau solde : " + String.format("%.2f €", currentMember.getBalance()),
                "Succès", JOptionPane.INFORMATION_MESSAGE);

            loadCategoriesAndRides();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Déjà inscrit", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage() + "\n\nContactez le trésorier pour recharger votre solde.",
                "Solde insuffisant", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'inscription : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setBackground(new Color(0, 122, 255));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private Long rideId;
        private final Member member;

        public ButtonEditor(Member member) {
            super(new JCheckBox());
            this.member = member;
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 15));
            button.setBackground(new Color(0, 122, 255));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            this.rideId = (Long) table.getModel().getValueAt(modelRow, 4);
            button.setText(value == null ? "" : value.toString());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (rideId != null) {
                SwingUtilities.invokeLater(() -> new RideDetailFrame(rideId, member));
            }
            return "Participer";
        }
    }
}