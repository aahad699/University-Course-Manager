import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterCourseDialog extends JDialog {
    private String studentID;
    private JTable tableAvailableCourses;
    private JButton btnRegister;

    public RegisterCourseDialog(JFrame parent, String studentID) {
        super(parent, "Register New Course", true);
        this.studentID = studentID;

        setSize(500, 400);
        setLocationRelativeTo(parent);

        tableAvailableCourses = new JTable();
        btnRegister = new JButton("Register Selected Course");
        btnRegister.setBackground(new Color(40, 167, 69));
        btnRegister.setForeground(Color.WHITE);

        setLayout(new BorderLayout());
        add(new JScrollPane(tableAvailableCourses), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnRegister);
        add(bottomPanel, BorderLayout.SOUTH);

        loadAvailableCourses();

        btnRegister.addActionListener(e -> registerSelectedCourse());
    }

    private void loadAvailableCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                    SELECT CourseID, Name FROM Courses
                    WHERE CourseID NOT IN (
                        SELECT CourseID FROM Enrollments WHERE StudentID = ?
                    )
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentID);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new String[]{"Course ID", "Course Name"}, 0);
            while (rs.next()) {
                String courseID = rs.getString("CourseID");
                String courseName = rs.getString("Name");
                model.addRow(new Object[]{courseID, courseName});
            }
            tableAvailableCourses.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerSelectedCourse() {
        int selectedRow = tableAvailableCourses.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to register.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseID = tableAvailableCourses.getValueAt(selectedRow, 0).toString();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Enrollments (StudentID, CourseID) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentID);
            stmt.setString(2, courseID);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Course registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
