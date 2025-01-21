import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

class RoundedButton extends JButton {
    private Color backgroundColor;
    private String courseName;
    private boolean isCourseButton;

    public RoundedButton(String text, Color backgroundColor, String courseName, boolean isCourseButton) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.courseName = courseName;
        this.isCourseButton = isCourseButton;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setForeground(Color.black);
        setFocusPainted(false);

        addActionListener(e -> {
            if (isCourseButton) {
                QuestionFrame questionFrame = new QuestionFrame(courseName);
                questionFrame.setVisible(true);
            } else {
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                currentFrame.dispose();
                new AdminAnalysis();
                }
        });

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
        super.paintComponent(g2);
        g2.dispose();
    }
}

class AdminFrame extends JFrame {
    private ArrayList<String> subjects;
    private JPanel buttonPanel;
    private ImageIcon icon;

    public AdminFrame() {
        setTitle("Admin Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try {
            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
            MongoCollection<Document> questions = database.getCollection("questions");

            subjects = new ArrayList<>();
            try (MongoCursor<String> cursor = questions.distinct("courseId", String.class).iterator()) {
                while (cursor.hasNext()) {
                    subjects.add(cursor.next());
                }
            }
            mongoClient.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to MongoDB: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                icon = new ImageIcon("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\3.png");
                g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        JLabel chooseCourseLabel = new JLabel("CHOOSE APPROPRIATE COURSE ");
        chooseCourseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chooseCourseLabel.setFont(new Font("Arial", Font.BOLD, 30));
        chooseCourseLabel.setForeground(Color.white);
        chooseCourseLabel.setBorder(new EmptyBorder(50, 0, 25, 0));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(chooseCourseLabel, BorderLayout.NORTH);
        mainPanel.add(northPanel, BorderLayout.NORTH);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(25, 50, 50, 50));

        for (String subject : subjects) {
            JButton subjectButton = new RoundedButton(subject, new Color(255, 255, 255, 150), subject, true);
            subjectButton.setPreferredSize(new Dimension(200, 200));
            buttonPanel.add(subjectButton);
            buttonPanel.add(Box.createHorizontalStrut(30));
        }

        // Add View Analysis button
        JButton viewAnalysisButton = new RoundedButton("View Analysis", new Color(255, 255, 255, 150), null, false);
        viewAnalysisButton.setPreferredSize(new Dimension(200, 200));
        buttonPanel.add(viewAnalysisButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        JButton editCourseButton = new JButton("Edit Course");
        editCourseButton.setFont(new Font("Comic sans", Font.BOLD, 20));

        editCourseButton.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem addCourseItem = new JMenuItem("Add Course");
            JMenuItem deleteCourseItem = new JMenuItem("Delete Course");
            JMenuItem alterCourseItem = new JMenuItem("Alter Course");

            addCourseItem.setFont(new Font("Comic sans", Font.BOLD, 15));
            deleteCourseItem.setFont(new Font("Comic sans", Font.BOLD, 15));
            alterCourseItem.setFont(new Font("Comic sans", Font.BOLD, 15));

            addCourseItem.addActionListener(event -> {
                String courseName = JOptionPane.showInputDialog(AdminFrame.this, "Enter Course Name:");
                if (courseName != null && !courseName.isEmpty()) {
                    subjects.add(courseName);
                    updateDatabaseSubjects(null, courseName); // Pass null for oldCourseName
                    updateButtonPanel();
                }
            });

            deleteCourseItem.addActionListener(event -> {
                String[] courseNames = subjects.toArray(new String[0]);
                String selectedCourse = (String) JOptionPane.showInputDialog(AdminFrame.this,
                        "Select a course to delete:",
                        "Delete Course",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        courseNames,
                        courseNames[0]);

                if (selectedCourse != null) {
                    subjects.remove(selectedCourse);
                    deleteCourseFromDatabase(selectedCourse);
                    updateButtonPanel();
                }
            });

            alterCourseItem.addActionListener(event -> {
                String[] courseNames = subjects.toArray(new String[0]);
                String selectedCourse = (String) JOptionPane.showInputDialog(AdminFrame.this,
                        "Select a course to edit:",
                        "Edit Course",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        courseNames,
                        courseNames[0]);

                if (selectedCourse != null) {
                    String newCourseName = JOptionPane.showInputDialog(AdminFrame.this, "Enter the new course name:");
                    if (newCourseName != null && !newCourseName.isEmpty()) {
                        subjects.set(subjects.indexOf(selectedCourse), newCourseName);
                        updateDatabaseSubjects(selectedCourse, newCourseName);
                        updateButtonPanel();
                    }
                }
            });

            menu.add(addCourseItem);
            menu.add(deleteCourseItem);
            menu.add(alterCourseItem);

            menu.show(editCourseButton, 0, editCourseButton.getHeight());
        });

        JButton logoutButton = new JButton("Logout"); // Create logout button
        logoutButton.setFont(new Font("Comic sans", Font.BOLD, 20)); // Set font

        logoutButton.addActionListener(e -> {
            // Dispose of the current AdminFrame
            dispose();
            // Create a new login frame
            login login = new login();
        });

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topRightPanel.setOpaque(false);
        topRightPanel.setBorder(new EmptyBorder(50, 0, 35, 0));

        topRightPanel.add(editCourseButton);
        topRightPanel.add(logoutButton); // Add logout button

        mainPanel.add(topRightPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void updateDatabaseSubjects(String oldCourseName, String newCourseName) {
        try {
            if (oldCourseName != null) {
                // Update an existing course
                MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
                MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
                MongoCollection<Document> courses = database.getCollection("questions");

                Document filter = new Document("courseId", oldCourseName);
                Document update = new Document("$set", new Document("courseId", newCourseName));
                courses.updateMany(filter, update);

                mongoClient.close();
            } else {
                // Add a new course and transfer to add question frame
                SwingUtilities.invokeLater(() -> {
                    QuestionFrame questionFrame = new QuestionFrame(newCourseName);
                    questionFrame.setVisible(true);
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to update courses in the database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminFrame frame = new AdminFrame();
            frame.setVisible(true);
        });
    }

    private void deleteCourseFromDatabase(String courseName) {
        try {
            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
            MongoCollection<Document> courses = database.getCollection("questions");

            courses.deleteMany(new Document("courseId", courseName));

            mongoClient.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to delete course from the database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateButtonPanel() {
        buttonPanel.removeAll();
        for (String subject : subjects) {
            JButton subjectButton = new RoundedButton(subject, new Color(255, 255, 255, 150), subject, true);
            subjectButton.setPreferredSize(new Dimension(200, 200));
            buttonPanel.add(subjectButton);
            buttonPanel.add(Box.createHorizontalStrut(30));
        }
        JButton viewAnalysisButton = new RoundedButton("View Analysis", new Color(255, 255, 255, 150), null, false);
        viewAnalysisButton.setPreferredSize(new Dimension(200, 200));
        buttonPanel.add(viewAnalysisButton);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }
}
