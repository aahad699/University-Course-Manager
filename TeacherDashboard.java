import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TeacherDashboard extends JFrame {
    private String teacherID;
    private JLabel lblWelcome;
    private JTextField txtName, txtEmail;
    private JTable tableCourses, tableStudents;
    private JButton btnAddCourse, btnEditCourse, btnDeleteCourse, btnLogout;
    private JButton btnDeleteStudentRegistration;
    private JButton btnChangePassword;
    private JButton btnUpdateProfile;

    public TeacherDashboard(String teacherID) {
        this.teacherID = teacherID;

        setTitle("AAHAD.69");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        lblWelcome = new JLabel("Welcome, " + teacherID + "!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel profilePanel = createProfilePanel();
        JPanel coursesPanel = createCoursesPanel();
        JPanel studentsPanel = createStudentsPanel();

        tabbedPane.addTab("Profile", profilePanel);
        tabbedPane.addTab("Manage Courses", coursesPanel);
        tabbedPane.addTab("View Enrolled Students", studentsPanel);

        btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(153, 0, 0));
        btnLogout.setForeground(Color.BLACK);
        btnLogout.setFocusPainted(false);
        btnLogout.setPreferredSize(new Dimension(120, 40));
        btnLogout.addActionListener(e -> logout());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnLogout);

        add(lblWelcome, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        Font mainFont = new Font("Segoe UI", Font.PLAIN, 15);
        setGlobalFont(this, mainFont);
        getContentPane().setBackground(new Color(245, 245, 245));

        loadCourses();
        loadEnrolledStudents();
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

        btnUpdateProfile = new JButton("Update Profile");
        btnUpdateProfile.setBackground(new Color(40, 167, 69));
        btnUpdateProfile.setForeground(Color.BLACK);
        btnUpdateProfile.setFocusPainted(false);
        
        btnChangePassword = new JButton("Change Password");
        btnChangePassword.setBackground(new Color(255, 193, 7));
        btnChangePassword.setForeground(Color.BLACK);
        btnChangePassword.setFocusPainted(false);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.Name, u.Email FROM Teachers t JOIN Users u ON t.UserID = u.ID WHERE t.TeacherID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                txtName.setText(rs.getString("Name"));
                txtEmail.setText(rs.getString("Email"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

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
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnUpdateProfile, gbc);

        gbc.gridy = 4;
        panel.add(btnChangePassword, gbc);

        btnUpdateProfile.addActionListener(e -> editProfile(txtName.getText(), txtEmail.getText()));
        btnChangePassword.addActionListener(e -> changePassword());

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

        btnAddCourse = new JButton("Add Course");
        btnAddCourse.setBackground(new Color(0, 123, 255));
        btnAddCourse.setForeground(Color.BLACK);
        btnAddCourse.setFocusPainted(false);
        
        btnEditCourse = new JButton("Edit Course");
        btnEditCourse.setBackground(new Color(40, 167, 69));
        btnEditCourse.setForeground(Color.BLACK);
        btnEditCourse.setFocusPainted(false);
        
        btnDeleteCourse = new JButton("Delete Course");
        btnDeleteCourse.setBackground(new Color(220, 53, 69));
        btnDeleteCourse.setForeground(Color.BLACK);
        btnDeleteCourse.setFocusPainted(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAddCourse);
        buttonPanel.add(btnEditCourse);
        buttonPanel.add(btnDeleteCourse);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnAddCourse.addActionListener(e -> addCourse());
        btnEditCourse.addActionListener(e -> editCourse());
        btnDeleteCourse.addActionListener(e -> deleteCourse());

        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        tableStudents = new JTable();
        tableStudents.setRowHeight(30);
        tableStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableStudents.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        JScrollPane scrollPane = new JScrollPane(tableStudents);

        btnDeleteStudentRegistration = new JButton("Remove Selected Student");
        btnDeleteStudentRegistration.setBackground(new Color(220, 53, 69));
        btnDeleteStudentRegistration.setForeground(Color.BLACK);
        btnDeleteStudentRegistration.setFocusPainted(false);
        btnDeleteStudentRegistration.addActionListener(e -> deleteStudentRegistration());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnDeleteStudentRegistration);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void changePassword() {
        JPasswordField currentPasswordField = new JPasswordField(20);
        JPasswordField newPasswordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Current Password:"));
        panel.add(currentPasswordField);
        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Confirm New Password:"));
        panel.add(confirmPasswordField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Change Password", 
                     JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (newPassword.equals(confirmPassword)) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "SELECT PasswordHash FROM Users WHERE ID = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, teacherID);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String storedHash = rs.getString("PasswordHash");
                        
                        if (BCrypt.checkpw(currentPassword, storedHash)) {
                            String hashedNewPassword = BCrypt.hashpw(newPassword);
                            
                            String updateSql = "UPDATE Users SET PasswordHash = ? WHERE ID = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                            updateStmt.setString(1, hashedNewPassword);
                            updateStmt.setString(2, teacherID);
                            updateStmt.executeUpdate();

                            JOptionPane.showMessageDialog(this, "Password changed successfully!", 
                                      "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Current password is incorrect.", 
                                      "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error changing password: " + ex.getMessage(), 
                              "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", 
                          "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editProfile(String name, String email) {
        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String updateTeacherSql = "UPDATE Teachers SET Name = ? WHERE TeacherID = ?";
                PreparedStatement teacherStmt = conn.prepareStatement(updateTeacherSql);
                teacherStmt.setString(1, name);
                teacherStmt.setString(2, teacherID);
                teacherStmt.executeUpdate();

                String updateUserSql = "UPDATE Users SET Email = ? WHERE ID = ?";
                PreparedStatement userStmt = conn.prepareStatement(updateUserSql);
                userStmt.setString(1, email);
                userStmt.setString(2, teacherID);
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

    private void deleteStudentRegistration() {
        int selectedRow = tableStudents.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to remove.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentID = tableStudents.getValueAt(selectedRow, 2).toString(); // Assuming the StudentID is in column 2
        String courseID = tableStudents.getValueAt(selectedRow, 0).toString(); // Assuming CourseID is in column 0

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this student's registration?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM Enrollments WHERE StudentID = ? AND CourseID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, studentID);
                stmt.setString(2, courseID);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Student registration removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadEnrolledStudents();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error removing student registration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT CourseID, Name FROM Courses WHERE TeacherID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherID);
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

    private void loadEnrolledStudents() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT c.CourseID, c.Name as CourseName, e.StudentID, s.Name as StudentName " +
                         "FROM Courses c JOIN Enrollments e ON c.CourseID = e.CourseID " +
                         "JOIN Students s ON e.StudentID = s.StudentID " +
                         "WHERE c.TeacherID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherID);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                new String[]{"Course ID", "Course Name", "Student ID", "Student Name"}, 0);

            while (rs.next()) {
                String courseID = rs.getString("CourseID");
                String courseName = rs.getString("CourseName");
                String studentID = rs.getString("StudentID");
                String studentName = rs.getString("StudentName");
                model.addRow(new Object[]{courseID, courseName, studentID, studentName});
            }

            tableStudents.setModel(model);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCourse() {
        String courseID = JOptionPane.showInputDialog(this, "Enter new course ID:");
        if (courseID == null || courseID.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseName = JOptionPane.showInputDialog(this, "Enter new course name:");
        if (courseName == null || courseName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String creditsStr = JOptionPane.showInputDialog(this, "Enter course credits:");
        if (creditsStr == null || creditsStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Credits cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input for credits. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String Description = JOptionPane.showInputDialog(this, "Enter program description:");
        if (Description == null || Description.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Program description cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Courses (CourseID, Name, Credits, Description, TeacherID) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseID);
            stmt.setString(2, courseName);
            stmt.setInt(3, credits);
            stmt.setString(4, Description);
            stmt.setString(5, teacherID);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Course added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourses();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editCourse() {
        int selectedRow = tableCourses.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseID = tableCourses.getValueAt(selectedRow, 0).toString();
        String currentName = tableCourses.getValueAt(selectedRow, 1).toString();

        String newName = JOptionPane.showInputDialog(this, "Enter new course name:", currentName);
        if (newName == null || newName.trim().isEmpty()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Courses SET Name = ? WHERE CourseID = ? AND TeacherID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setString(2, courseID);
            stmt.setString(3, teacherID);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Course updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourses();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCourse() {
        int selectedRow = tableCourses.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseID = tableCourses.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this course?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM Courses WHERE CourseID = ? AND TeacherID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseID);
            stmt.setString(2, teacherID);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Course deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourses();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginScreen().setVisible(true);
            dispose();
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