package be.pasque.UI;

import be.pasque.Model.Member;
import be.pasque.Model.Treasurer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class TreasurerFrame extends JFrame {

    private final Treasurer treasurer;
    private JTable memberTable;
    private DefaultTableModel tableModel;

    public TreasurerFrame(Treasurer treasurer) {
        this.treasurer = treasurer;

        setTitle("Trésorier - Suivi des cotisations");
        setSize(1250, 780);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Trésorier : " + treasurer.getFirstName() + " " + treasurer.getLastName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightButtons.setOpaque(false);

        JButton reminderBtn = new JButton("Envoyer lettres de rappel");
        reminderBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reminderBtn.setBackground(new Color(183, 28, 28));
        reminderBtn.setForeground(Color.WHITE);
        reminderBtn.setOpaque(true);
        reminderBtn.setBorderPainted(false);
        reminderBtn.setContentAreaFilled(true);
        reminderBtn.setFocusPainted(false);
        reminderBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reminderBtn.setPreferredSize(new Dimension(280, 50));
        reminderBtn.addActionListener(e -> sendMembershipReminders());
        rightButtons.add(reminderBtn);

        JButton payDriversBtn = new JButton("Payer les conducteurs (10 €)");
        payDriversBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        payDriversBtn.setBackground(new Color(46, 125, 50));
        payDriversBtn.setForeground(Color.WHITE);
        payDriversBtn.setOpaque(true);
        payDriversBtn.setBorderPainted(false);
        payDriversBtn.setContentAreaFilled(true);
        payDriversBtn.setFocusPainted(false);
        payDriversBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        payDriversBtn.setPreferredSize(new Dimension(300, 50));
        payDriversBtn.addActionListener(e -> payDrivers());
        rightButtons.add(payDriversBtn);

        JButton logoutBtn = new JButton("Déconnexion");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(new Color(0, 102, 204));
        logoutBtn.setOpaque(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        rightButtons.add(logoutBtn);

        header.add(rightButtons, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JLabel mainTitle = new JLabel("Suivi des cotisations annuelles", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        mainTitle.setForeground(new Color(0, 102, 204));
        mainTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        String[] columns = {"Nom", "Prénom", "Email", "Téléphone", "Statut de cotisation", "Solde (€)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override 
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
        };

        memberTable = new JTable(tableModel);
        memberTable.setRowHeight(55);
        memberTable.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        memberTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 17));
        memberTable.getTableHeader().setBackground(new Color(240, 248, 255));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 5; i++) {
            memberTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        memberTable.getColumn("Solde (€)").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );

                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 18));
                label.setForeground(new Color(0, 120, 0));

                if (value instanceof Number) {
                    label.setText(String.format("%.2f €", ((Number) value).doubleValue()));
                } else {
                    label.setText(value != null ? value.toString() : "0,00 €");
                }

                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(memberTable);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(mainTitle, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        loadAllMembers();
        setVisible(true);
    }

    private void loadAllMembers() {
        tableModel.setRowCount(0);

        List<Member> members = treasurer.getAllMembers();

        for (Member m : members) {
            String status = m.isMembershipPaid() ? "Payé" : "À payer";

            tableModel.addRow(new Object[]{
                    m.getLastName().toUpperCase(),
                    m.getFirstName(),
                    m.getEmail(),
                    m.getPhone() != null ? m.getPhone() : "—",
                    status,
                    m.getBalance()
            });
        }
    }

    private void sendMembershipReminders() {

        List<Member> unpaidMembers = treasurer.getUnpaidMembers();

        if (unpaidMembers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tous les membres sont à jour.", 
                "Aucune lettre", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String list = unpaidMembers.stream()
                .map(m -> "• " + m.getFirstName() + " " + m.getLastName().toUpperCase() + " <" + m.getEmail() + ">")
                .collect(Collectors.joining("\n"));

        int count = unpaidMembers.size();

        int choice = JOptionPane.showConfirmDialog(this,
                "<html><h2>Lettres de rappel</h2><p><b>" + count + " membre(s)</b> non à jour :</p><pre>"
                        + list + "</pre><p>Envoyer ?</p></html>",
                "Rappels cotisation", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Lettres envoyées à " + count + " membres !", 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void payDrivers() {
        try {

            List<Member> drivers = treasurer.getAllDrivers();

            if (drivers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun conducteur trouvé.", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String list = drivers.stream()
                    .map(m -> "• " + m.getFirstName() + " " + m.getLastName().toUpperCase()
                            + " → " + String.format("%.2f €", m.getBalance()) + " + 10,00 €")
                    .collect(Collectors.joining("\n"));

            int choice = JOptionPane.showConfirmDialog(this,
                    "<html><h2>Paiement des conducteurs</h2>"
                            + "<p>" + drivers.size() + " conducteur(s) recevront 10 € :</p>"
                            + "<pre>" + list + "</pre>"
                            + "<p>Confirmer ?</p></html>",
                    "Payer les conducteurs", JOptionPane.YES_NO_OPTION);

            if (choice != JOptionPane.YES_OPTION) return;

            treasurer.payAllDrivers();

            JOptionPane.showMessageDialog(this,
                    "Paiement effectué !\n" + drivers.size() + " conducteurs crédités.",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            loadAllMembers();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}