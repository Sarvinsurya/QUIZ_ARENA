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

class RoundedButton1 extends JButton {
    private Color backgroundColor;
    private String courseId;
    private String filePath; // Path to the file to open
    private String rollnumber;

    public RoundedButton1(String text, Color backgroundColor, String courseId, String filePath,String rollnumber) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.courseId = courseId;
        this.filePath = filePath;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setForeground(Color.black);
        setFocusPainted(false);

        final String courseIdFinal = courseId; // Declare courseId as final

        addActionListener(e -> {
            JButton button = (JButton) e.getSource();
            Window window = SwingUtilities.windowForComponent(button);
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                frame.dispose(); // Dispose of the current frame
            }
            // Create a new StudentTestFrame
            StudentTestFrame studentTestFrame = new StudentTestFrame(rollnumber,courseId);
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

class StudentCourseFrame extends JFrame {
    private ArrayList<String> subjects;
    private JPanel buttonPanel;
    private ImageIcon icon;

    private String rollNumber;

    public StudentCourseFrame(String rollNumber) {
        this.rollNumber = rollNumber;

        setTitle("Admin Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try {
            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
            MongoCollection<Document> questions = database.getCollection("questions");

            // Find distinct values of courseId
            subjects = new ArrayList<>();
            try (MongoCursor<String> cursor = questions.distinct("courseId", String.class).iterator()) {
                while (cursor.hasNext()) {
                    subjects.add(cursor.next());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to MongoDB: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load background image
                icon = new ImageIcon("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\3.png");
                g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        JLabel chooseCourseLabel = new JLabel("CHOOSE APPROPRIATE COURSE ");
        chooseCourseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chooseCourseLabel.setFont(new Font("Arial", Font.BOLD, 30));
        chooseCourseLabel.setForeground(Color.white);

        // Add padding to chooseCourseLabel
        chooseCourseLabel.setBorder(new EmptyBorder(50, 0, 25, 0));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(chooseCourseLabel, BorderLayout.NORTH);
        mainPanel.add(northPanel, BorderLayout.NORTH);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        // Add padding to buttonPanel
        buttonPanel.setBorder(new EmptyBorder(25, 50, 50, 50));

        for (String subject : subjects) {
            JButton subjectButton = new RoundedButton1(subject, new Color(255, 255, 255, 150), subject, "C:\\Users\\barat\\IdeaProjects\\main_prgt\\Proj1\\src\\main\\java\\testno.java",rollNumber);
            subjectButton.setPreferredSize(new Dimension(200, 200));
            buttonPanel.add(subjectButton);
            buttonPanel.add(Box.createHorizontalStrut(30));
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Create a panel for the logout button at the bottom
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        JButton BackToMain = new JButton("Back To Main");
        BackToMain.setFont(new Font("Arial", Font.BOLD, 20));
        BackToMain.setForeground(Color.WHITE);
        BackToMain.setContentAreaFilled(false);
        BackToMain.setFocusPainted(false);
        BackToMain.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        BackToMain.setPreferredSize(new Dimension(200, 50));
        BackToMain.addActionListener(e -> {
            dispose();
            new ExistingUserFrame(rollNumber).setVisible(true);
        });
        southPanel.setBorder(new EmptyBorder(25, 0, 25, 0));
        southPanel.add(BackToMain);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }
}

