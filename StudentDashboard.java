import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class StudentDashboard extends JFrame {
    private String studentID;
    private JLabel lblWelcome;
    private JTextField txtName, txtEmail, txtMajor;
    private JTable tableCourses;
    private JButton btnUpdateProfile, btnRegisterCourse, btnLogout;
    private JButton btnChangePassword;

    public StudentDashboard(String studentID) {
        this.studentID = studentID;

        setTitle("Student Dashboard");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        lblWelcome = new JLabel("Welcome, " + studentID + "!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel profilePanel = createProfilePanel();
        JPanel coursesPanel = createCoursesPanel();

        tabbedPane.addTab("Profile", profilePanel);
        tabbedPane.addTab("Courses", coursesPanel);

        btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(153, 0, 0));
        btnLogout.setForeground(Color.BLACK);
        btnLogout.setFocusPainted(false);
        btnLogout.setPreferredSize(new Dimension(120, 40));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnLogout);

        add(lblWelcome, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadStudentData();
        loadEnrolledCourses();

        btnUpdateProfile.addActionListener(e -> updateProfile());
        btnRegisterCourse.addActionListener(e -> openRegisterCourseDialog());
        btnChangePassword.addActionListener(e -> openChangePasswordDialog());
        btnLogout.addActionListener(e -> logout());
        
        Font mainFont = new Font("Segoe UI", Font.PLAIN, 15);
        setGlobalFont(this, mainFont);
        getContentPane().setBackground(new Color(245, 245, 245));

    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel lblName = new JLabel("Name:");
        txtName = new JTextField(20);
        JLabel lblEmail = new JLabel("Email:");
        txtEmail = new JTextField(20);
        JLabel lblMajor = new JLabel("Major:");
        txtMajor = new JTextField(20);

        btnUpdateProfile = new JButton("Update Profile");
        btnUpdateProfile.setBackground(new Color(40, 167, 69));
        btnUpdateProfile.setForeground(Color.BLACK);
        btnUpdateProfile.setFocusPainted(false);
        
        btnChangePassword = new JButton("Change Password");
        btnChangePassword.setBackground(new Color(255, 193, 7)); // A nice yellow
        btnChangePassword.setForeground(Color.BLACK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblName, gbc);
        gbc.gridx = 1;
        panel.add(txtName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblEmail, gbc);
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblMajor, gbc);
        gbc.gridx = 1;
        panel.add(txtMajor, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnUpdateProfile, gbc);

        gbc.gridy = 4;
        panel.add(btnChangePassword, gbc);

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        tableCourses = new JTable();
        tableCourses.setRowHeight(30);
        tableCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCourses.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));

        JScrollPane scrollPane = new JScrollPane(tableCourses);

        btnRegisterCourse = new JButton("Register New Course");
        btnRegisterCourse.setBackground(new Color(0, 123, 255));
        btnRegisterCourse.setForeground(Color.BLACK);
        btnRegisterCourse.setFocusPainted(false);

        JButton btnRemoveCourse = new JButton("Remove Selected Course");
        btnRemoveCourse.setBackground(new Color(220, 53, 69));
        btnRemoveCourse.setForeground(Color.BLACK);
        btnRemoveCourse.setFocusPainted(false);

        btnRegisterCourse.addActionListener(e -> openRegisterCourseDialog());
        btnRemoveCourse.addActionListener(e -> removeSelectedCourse());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(btnRegisterCourse);
        topPanel.add(btnRemoveCourse);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginScreen().setVisible(true);
            dispose();
        }
    }

    private void loadStudentData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT s.Name, u.Email, s.Major FROM Students s JOIN Users u ON s.UserID = u.ID WHERE s.StudentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                txtName.setText(rs.getString("Name"));
                txtEmail.setText(rs.getString("Email"));
                txtMajor.setText(rs.getString("Major"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEnrolledCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT c.CourseID, c.Name FROM Enrollments e JOIN Courses c ON e.CourseID = c.CourseID WHERE e.StudentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentID);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new String[]{"Course ID", "Course Name"}, 0);

            while (rs.next()) {
                String courseID = rs.getString("CourseID");
                String courseName = rs.getString("Name");
                model.addRow(new Object[]{courseID, courseName});
            }

            tableCourses.setModel(model);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProfile() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String major = txtMajor.getText().trim();

        if (name.isEmpty() || email.isEmpty() || major.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String updateStudentSql = "UPDATE Students SET Name = ?, Major = ? WHERE StudentID = ?";
                PreparedStatement studentStmt = conn.prepareStatement(updateStudentSql);
                studentStmt.setString(1, name);
                studentStmt.setString(2, major);
                studentStmt.setString(3, studentID);
                studentStmt.executeUpdate();

                String updateUserSql = "UPDATE Users SET Email = ? WHERE ID = ?";
                PreparedStatement userStmt = conn.prepareStatement(updateUserSql);
                userStmt.setString(1, email);
                userStmt.setString(2, studentID);
                userStmt.executeUpdate();

                conn.commit();

                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterCourseDialog() {
        new RegisterCourseDialog(this, studentID).setVisible(true);
        loadEnrolledCourses();
    }
    
    private void openChangePasswordDialog() {
        JPasswordField oldPasswordField = new JPasswordField(15);
        JPasswordField newPasswordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Old Password:"));
        panel.add(oldPasswordField);
        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String checkSql = "SELECT PasswordHash FROM Users WHERE ID = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, studentID);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("PasswordHash");
                    if (!BCrypt.checkpw(oldPassword, storedHash)) {
                        JOptionPane.showMessageDialog(this, "Old password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                String hashedPassword = BCrypt.hashpw(newPassword);
                String updateSql = "UPDATE Users SET PasswordHash = ? WHERE ID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, studentID);

                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error changing password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void removeSelectedCourse() {
        int selectedRow = tableCourses.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseID = (String) tableCourses.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove this course?", "Confirm Removal", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM Enrollments WHERE StudentID = ? AND CourseID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, studentID);
                stmt.setString(2, courseID);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Course removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadEnrolledCourses(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to remove the course.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error removing course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void setGlobalFont(Component component, Font font) {
        if (component instanceof JLabel || component instanceof JButton || component instanceof JTextField || component instanceof JTable) {
            component.setFont(font);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setGlobalFont(child, font);
            }
        }
    }

}
