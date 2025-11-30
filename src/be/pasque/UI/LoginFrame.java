package be.pasque.UI;

import be.pasque.DAO.MemberDAO;
import be.pasque.Model.Manager;
import be.pasque.Model.Member;
import be.pasque.Model.Person;
import be.pasque.Model.Treasurer;
import be.pasque.utils.DBConnection;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        setTitle("Club Cycliste - Connexion");
        setSize(420, 340);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Connexion au Club Cycliste", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        panel.add(new JLabel("Email :"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton loginBtn = new JButton("Se connecter");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setPreferredSize(new Dimension(220, 48));
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> performLogin());

        add(panel);
        setVisible(true);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs");
            return;
        }

        Person person = Person.authenticate(email, password);

        if (person == null) {
            JOptionPane.showMessageDialog(this, "Identifiants incorrects", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();

        if (person instanceof Member m) {
            new MainFrame(m);
        } else if (person instanceof Manager m) {
            new ManagerFrame(m);
        } else if (person instanceof Treasurer t) {
            new TreasurerFrame(t);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception e) { e.printStackTrace(); }
            new LoginFrame();
        });
    }
}