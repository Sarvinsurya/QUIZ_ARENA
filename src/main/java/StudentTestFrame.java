import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.Image; // Import for Image
import java.awt.Font; // Import for Font
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;

// Custom JButton class with transparent background and curved ends
class Rbutton extends JButton {
    private Color backgroundColor;

    public Rbutton(String text, Color backgroundColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false); // Disable default button border

        setForeground(Color.black); // Set text color to white
        setFocusPainted(false); // Remove focus border
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30)); // Adjust the radius as needed
        super.paintComponent(g2);
        g2.dispose();
    }
}


class StudentTestFrame extends JFrame {
    private ArrayList<String> tests = new ArrayList<>(); // ArrayList to store distinct test names
    private Image backgroundImage;
    private String courseId;
    private String rollnumber;

    public StudentTestFrame(String rollnumber,String courseId) {
        this.rollnumber=rollnumber;
        this.courseId = courseId;
        // Load background image
        backgroundImage = new ImageIcon("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\3.png").getImage();

        setTitle("Admin Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose the frame on close
        setVisible(true);

        // Connect to MongoDB and fetch test names
        try {
            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
            MongoCollection<Document> questions = database.getCollection("questions");

            // Find distinct test names for the given courseId
            try (MongoCursor<String> cursor = questions.distinct("testId", String.class)
                    .filter(new Document("courseId", courseId)).iterator()) {
                while (cursor.hasNext()) {
                    tests.add(cursor.next());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to MongoDB: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // Create a main panel for the dashboard
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw background image
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Create label for "Choose Appropriate Course"
        JLabel chooseCourseLabel = new JLabel("Choose Appropriate Course");
        chooseCourseLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center align the text
        chooseCourseLabel.setFont(new Font("Arial", Font.BOLD, 30)); // Set font and size
        chooseCourseLabel.setForeground(Color.white);

        // Create panel for test buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center align buttons
        buttonPanel.setOpaque(false); // Make panel transparent

        // Add the label and test buttons
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);
        labelPanel.add(chooseCourseLabel, BorderLayout.NORTH);
        mainPanel.add(labelPanel, BorderLayout.NORTH);

        // Add vertical spacing between label and buttons
        labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 70, 0));

        // Add test buttons
        for (String test : tests) {
            JButton testButton = new Rbutton(test, new Color(255, 255, 255, 150)); // Use custom transparent button with curved ends
            testButton.setPreferredSize(new Dimension(200, 200)); // Set preferred size for buttons
            testButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {


                    boolean hasAttended = checkIfUserAttendedTest(rollnumber, courseId, test);

                    if (!hasAttended) {
                        // If the user has not attended the test, open the quiz frame
                        dispose();
                        QuizFrame quizFrame = new QuizFrame(rollnumber, courseId, test);
                    } else {
                        // If the user has already attended the test, display an error message
                        JOptionPane.showMessageDialog(StudentTestFrame.this, "You have already attended this test.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                }
            });
            buttonPanel.add(testButton);
            buttonPanel.add(Box.createHorizontalStrut(30)); // Add horizontal gap between buttons
        }


        JButton returnButton = createStyledButton("Return to Courses");
        returnButton.setPreferredSize(new Dimension(250, 50)); // Set preferred size for the button
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the current frame
                new StudentCourseFrame(rollnumber).setVisible(true); // Open StudentCourseFrame
            }
        });

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel returnButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        returnButtonPanel.setOpaque(false); // Make the panel transparent
        returnButtonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0)); // Add padding around the button
        returnButtonPanel.add(returnButton);

// Add returnButtonPanel to mainPanel
        mainPanel.add(returnButtonPanel, BorderLayout.SOUTH);


        // Add buttonPanel and returnButton to mainPanel

        // Add main panel to the frame
        add(mainPanel);
    }
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFont(new Font("Arial", Font.BOLD, 20)); // Adjust font size as needed
        button.setForeground(Color.WHITE); // Set text color to white
        button.setPreferredSize(new Dimension(250, 100)); // Adjust button size as needed
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1), // Set border color to white
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }
    private boolean checkIfUserAttendedTest(String rollnumber, String courseId, String testId) {
        try {
            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
            MongoCollection<Document> scoreCollection = database.getCollection("Score");

            // Check if there exists a document with the given rollnumber, courseId, and testId
            Document query = new Document("rollnumber", rollnumber)
                    .append("courseId", courseId)
                    .append("testId", testId);
            long count = scoreCollection.countDocuments(query);

            mongoClient.close();

            // If count is greater than 0, user has attended the test
            return count > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(StudentTestFrame.this, "Failed to check attendance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return true; // Return true to prevent opening the quiz frame in case of an error
        }
    }

}

