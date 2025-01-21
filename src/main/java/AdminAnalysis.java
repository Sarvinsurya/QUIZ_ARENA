import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class PlaceholderTextField extends JTextField {
    private String placeholder;

    public PlaceholderTextField(String text, String placeholder) {
        super(text);
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.GRAY);
            g2.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
            g2.dispose();
        }
    }
}

public class AdminAnalysis extends JFrame {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> questionsCollection;
    private MongoCollection<Document> scoreCollection;
    private MongoCollection<Document> usersCollection;
    private JPanel resultPanel;
    private Font resultFont = new Font("AngsanaUPC", Font.PLAIN, 18); // Font for the results

    AdminAnalysis() {
        // Initialize MongoDB connection
        try {
            mongoClient = new MongoClient("localhost", 27017);
            database = mongoClient.getDatabase("MCQ_EXAMINATION");
            questionsCollection = database.getCollection("questions");
            scoreCollection = database.getCollection("Score");
            usersCollection = database.getCollection("users");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to MongoDB: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        JFrame f = new JFrame("AdminAnalysis");

        // Load background image
        ImageIcon backgroundImageIcon = new ImageIcon("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\stock-photo-printed-question-marks-on-white-cards-over-blue.jpg");
        Image backgroundImage = backgroundImageIcon.getImage();

        // Create background panel with the image
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
        backgroundPanel.setLayout(new BorderLayout());

        // Create panel for search components
        JPanel searchPanel = new JPanel();
        searchPanel.setOpaque(false); // Set background panel to transparent
        searchPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        // Add "Back to Home" button
        JButton backToHomeButton = new JButton("Back to Home");
        backToHomeButton.setPreferredSize(new Dimension(120, 35));
        backToHomeButton.addActionListener(e -> {
            f.dispose();
            SwingUtilities.invokeLater(() -> {
                AdminFrame adminFrame = new AdminFrame();
                adminFrame.setVisible(true);
            });
        });
        searchPanel.add(backToHomeButton);


        // Add dropdown box for course selection
        JComboBox<String> courseComboBox = new JComboBox<>(fetchCourses());
        courseComboBox.setPreferredSize(new Dimension(250, 35));
        searchPanel.add(courseComboBox);

        // Add dropdown box for test selection
        JComboBox<String> testComboBox = new JComboBox<>();
        testComboBox.setPreferredSize(new Dimension(250, 35));
        searchPanel.add(testComboBox);

        // Populate testComboBox based on selected course
        courseComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedCourse = (String) courseComboBox.getSelectedItem();
                testComboBox.removeAllItems();
                for (String test : fetchTests(selectedCourse)) {
                    testComboBox.addItem(test);
                }
            }
        });

        // Add search button
        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(100, 35));

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get selected values from the combo boxes
                String selectedCourse = (String) courseComboBox.getSelectedItem();
                String selectedTest = (String) testComboBox.getSelectedItem();

                Object[][] data = fetchData(selectedCourse, selectedTest);

                // Remove any previous result panels
                if (resultPanel != null) {
                    backgroundPanel.remove(resultPanel);
                }

                // Create panel for displaying results
                resultPanel = new JPanel(new BorderLayout());
                resultPanel.setOpaque(false); // Set result panel to transparent

                String[] columnNames = {"Name", "Roll Number", "Score"};

                if (data.length > 0) {
                    JTable table = new JTable(data, columnNames);
                    table.setFont(resultFont); // Set font for the table
                    table.setRowHeight(25); // Increase row height
                    JTableHeader tableHeader = table.getTableHeader();
                    tableHeader.setFont(resultFont); // Set font for the table header
                    JScrollPane scrollPane = new JScrollPane(table);
                    resultPanel.add(scrollPane, BorderLayout.EAST);
                    resultPanel.setPreferredSize(new Dimension(100, 20));
                } else {
                    JLabel noDataLabel = new JLabel("No data available for the selected course and test.", SwingConstants.LEFT);
                    noDataLabel.setFont(new Font("AngsanaUPC", Font.PLAIN, 20));
                    noDataLabel.setForeground(Color.RED);
                    resultPanel.add(noDataLabel, BorderLayout.EAST);
                    resultPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));
                }

                // Add result panel to the background panel
                backgroundPanel.add(resultPanel, BorderLayout.CENTER);
                backgroundPanel.revalidate();
                backgroundPanel.repaint();
            }
        });
        searchPanel.add(searchButton);


        backgroundPanel.add(searchPanel, BorderLayout.NORTH);

        f.setContentPane(backgroundPanel); // Set background panel as content pane
        f.setSize(400, 200);

        // Set the extended state of the frame
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);

        f.setVisible(true);
    }

    public static void main(String args[]) {
        new AdminAnalysis();
    }

    // Fetch distinct courses from MongoDB
    private String[] fetchCourses() {
        MongoCursor<String> cursor = questionsCollection.distinct("courseId", String.class).iterator();
        Set<String> courses = new HashSet<>();
        while (cursor.hasNext()) {
            courses.add(cursor.next());
        }
        cursor.close();
        return courses.toArray(new String[0]);
    }

    // Fetch distinct tests based on selected course from MongoDB
    private String[] fetchTests(String courseId) {
        MongoCursor<String> cursor = questionsCollection.distinct("testId", String.class)
                .filter(new Document("courseId", courseId)).iterator();
        Set<String> tests = new HashSet<>();
        while (cursor.hasNext()) {
            tests.add(cursor.next());
        }
        cursor.close();
        return tests.toArray(new String[0]);
    }

    // Fetch data based on selected course and test from MongoDB
    private Object[][] fetchData(String selectedCourse, String selectedTest) {
        // Create a map to store roll number to name mapping
        // Create a map to store roll number to name mapping
        Map<String, String> rollNumberToNameMap = new HashMap<>();
        MongoCursor<Document> usersCursor = usersCollection.find().iterator();
        while (usersCursor.hasNext()) {
            Document userDoc = usersCursor.next();
            rollNumberToNameMap.put(userDoc.getString("rollno"), userDoc.getString("name"));
        }
        usersCursor.close();

        MongoCursor<Document> scoreCursor = scoreCollection.find(new Document("courseId", selectedCourse)
                .append("testId", selectedTest)).iterator();
        List<Object[]> dataList = new ArrayList<>();
        while (scoreCursor.hasNext()) {
            Document scoreDoc = scoreCursor.next();
            String rollNumber = scoreDoc.getString("rollnumber");
            int score = scoreDoc.getInteger("score");
            String name = rollNumberToNameMap.getOrDefault(rollNumber, "Unknown");
            dataList.add(new Object[]{name, rollNumber, score});
        }
        scoreCursor.close();
        return dataList.toArray(new Object[0][]);
    }
}

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
