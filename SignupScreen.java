import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SignupScreen extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtName;
    private JTextField txtEmail;
    private JComboBox<String> cmbRole;
    private JTextField txtMajorDept;
    private JLabel lblMajorDept;
    
    public SignupScreen() {
        setTitle("AAHAD.69");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        
        JLabel lblHeader = new JLabel("University Course Manager - Sign Up");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(lblHeader, gbc);
        
        JLabel lblRole = new JLabel("Role:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(lblRole, gbc);
        
        cmbRole = new JComboBox<>(new String[]{"Student", "Teacher"});
        cmbRole.addActionListener(e -> updateMajorDeptLabel());
        gbc.gridx = 1;
        panel.add(cmbRole, gbc);
        
        JLabel lblUsername = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblUsername, gbc);
        
        txtUsername = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);
        
        JLabel lblPassword = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblPassword, gbc);
        
        txtPassword = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);
        
        // Name
        JLabel lblName = new JLabel("Full Name:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(lblName, gbc);
        
        txtName = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtName, gbc);
        
        JLabel lblEmail = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(lblEmail, gbc);
        
        txtEmail = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);
        
        lblMajorDept = new JLabel("Major:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(lblMajorDept, gbc);
        
        txtMajorDept = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtMajorDept, gbc);
        
        JButton btnSignup = new JButton("Sign Up");
        btnSignup.setBackground(new Color(40, 167, 69));
        btnSignup.setForeground(Color.BLACK);
        btnSignup.setFocusPainted(false);
        btnSignup.setPreferredSize(new Dimension(120, 40));
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 10, 20);
        panel.add(btnSignup, gbc);
        
        JButton btnBack = new JButton("Back to Login");
        btnBack.setBackground(new Color(0, 123, 255));
        btnBack.setForeground(Color.BLACK);
        btnBack.setFocusPainted(false);
        btnBack.setPreferredSize(new Dimension(120, 40));
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 20, 10, 20);
        panel.add(btnBack, gbc);
        
        btnSignup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signup();
            }
        });
        
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new LoginScreen().setVisible(true);
                dispose();
            }
        });
        
        add(panel);
        updateMajorDeptLabel();
        
        Font mainFont = new Font("Segoe UI", Font.PLAIN, 15);
        setGlobalFont(this, mainFont);
    }
    
    private void updateMajorDeptLabel() {
        String role = (String) cmbRole.getSelectedItem();
        lblMajorDept.setText(role.equals("Student") ? "Major:" : "Department:");
    }
    
    private void signup() {
        String role = (String) cmbRole.getSelectedItem();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String majorDept = txtMajorDept.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty() || majorDept.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        char rolePrefix = role.equals("Student") ? 'S' : 'T';
        if (!username.startsWith(String.valueOf(rolePrefix))) {
            username = rolePrefix + username;
            txtUsername.setText(username);
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = "SELECT ID FROM Users WHERE ID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already exists", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
            conn.setAutoCommit(false);
            
            try {
                String userSql = "INSERT INTO Users (ID, PasswordHash, Role, Email) VALUES (?, ?, ?, ?)";
                PreparedStatement userStmt = conn.prepareStatement(userSql);
                userStmt.setString(1, username);
                userStmt.setString(2, hashedPassword);
                userStmt.setString(3, String.valueOf(rolePrefix));
                userStmt.setString(4, email);
                userStmt.executeUpdate();
                
                if (role.equals("Student")) {
                    String studentSql = "INSERT INTO Students (StudentID, UserID, Name, Major) VALUES (?, ?, ?, ?)";
                    PreparedStatement studentStmt = conn.prepareStatement(studentSql);
                    studentStmt.setString(1, username);
                    studentStmt.setString(2, username);
                    studentStmt.setString(3, name);
                    studentStmt.setString(4, majorDept);
                    studentStmt.executeUpdate();
                } else {
                    String teacherSql = "INSERT INTO Teachers (TeacherID, UserID, Name, Department) VALUES (?, ?, ?, ?)";
                    PreparedStatement teacherStmt = conn.prepareStatement(teacherSql);
                    teacherStmt.setString(1, username);
                    teacherStmt.setString(2, username);
                    teacherStmt.setString(3, name);
                    teacherStmt.setString(4, majorDept);
                    teacherStmt.executeUpdate();
                }
                
                conn.commit();
                JOptionPane.showMessageDialog(this, "Account created successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                new LoginScreen().setVisible(true);
                dispose();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setGlobalFont(Component component, Font font) {
        if (component instanceof JLabel || component instanceof JButton || component instanceof JTextField || 
            component instanceof JPasswordField || component instanceof JTable || component instanceof JComboBox) {
            component.setFont(font);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setGlobalFont(child, font);
            }
        }
    }
}