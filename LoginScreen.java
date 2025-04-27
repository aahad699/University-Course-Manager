import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginScreen extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    
    public LoginScreen() {
        setTitle("Student Management System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        
        JLabel lblHeader = new JLabel("University Course Manager - Log In");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(lblHeader, gbc);
        
        JLabel lblUsername = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(lblUsername, gbc);
        
        txtUsername = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);
        
        JLabel lblPassword = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblPassword, gbc);
        
        txtPassword = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);
        
        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(0, 123, 255));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(120, 40));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 10, 20);
        panel.add(btnLogin, gbc);
        
        JButton btnSignup = new JButton("Sign Up");
        btnSignup.setBackground(new Color(40, 167, 69));
        btnSignup.setForeground(Color.BLACK);
        btnSignup.setFocusPainted(false);
        btnSignup.setPreferredSize(new Dimension(120, 40));
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 20, 10, 20);
        panel.add(btnSignup, gbc);
        
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        
        btnSignup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new SignupScreen().setVisible(true);
            }
        });
        
        add(panel);
        
        Font mainFont = new Font("Segoe UI", Font.PLAIN, 15);
        setGlobalFont(this, mainFont);
    }
    
    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT ID, PasswordHash, Role FROM Users WHERE ID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("PasswordHash");
                if (BCrypt.checkpw(password, storedHash)) {
                    char role = rs.getString("Role").charAt(0);
                    
                    if (role == 'S') {
                        new StudentDashboard(username).setVisible(true);
                    } else if (role == 'T') {
                        new TeacherDashboard(username).setVisible(true);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong password", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "User not found", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setGlobalFont(Component component, Font font) {
        if (component instanceof JLabel || component instanceof JButton || component instanceof JTextField || 
            component instanceof JPasswordField || component instanceof JTable) {
            component.setFont(font);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setGlobalFont(child, font);
            }
        }
    }
}