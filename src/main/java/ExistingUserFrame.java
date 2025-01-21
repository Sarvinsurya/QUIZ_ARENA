import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.Border;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class ExistingUserFrame extends JFrame {
    private String rollNumber;
    private String userName; // Name of the existing user
    private JPanel buttonPanel;

    public ExistingUserFrame(String rollNumber) {
        this.rollNumber = rollNumber; // Set the roll number
        // Fetch username from the database based on the roll number
        this.userName = fetchUserNameFromDatabase(rollNumber);

        setTitle("Existing User Page");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Load the background image
        ImageIcon backgroundImageIcon = new ImageIcon("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\3.png");
        Image backgroundImage = backgroundImageIcon.getImage();

        // Create a JPanel with the background image
        JPanel mainPanel = new BackgroundImagePanel(backgroundImage);
        mainPanel.setLayout(new BorderLayout());

        // Create a panel for displaying name and roll number at the top right corner
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setPreferredSize(new Dimension(getWidth(), 100)); // Set panel height
        userInfoPanel.setBackground(new Color(0, 0, 0, 150)); // Set semi-transparent background color

        // Create labels for name and roll number with HTML line breaks and increased font size
        JLabel userInfoLabel = new JLabel("<html><div style='text-align: right; font-size: 20px; color: white;'>Name: " + userName + "<br>Roll Number: " + rollNumber + "</div></html>");
        userInfoPanel.add(userInfoLabel);

        // Add the user info panel to the frame's NORTH position
        mainPanel.add(userInfoPanel, BorderLayout.NORTH);

        // Create a panel for the buttons
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false); // Make the panel transparent

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 125, 15, 15); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add buttons and other components to the main panel
        JButton takeTestButton = createStyledButton("Take Test");
        JButton viewScoreButton = createStyledButton("View Score");
        JButton logoutButton = createStyledButton("LOGOUT");

        takeTestButton.addActionListener(e -> {
            blinkButton(takeTestButton);
            dispose();
            new StudentCourseFrame(rollNumber).setVisible(true);
        });

        viewScoreButton.addActionListener(e -> {
            blinkButton(viewScoreButton);

            UserAnalysis userAnalysis = new UserAnalysis(rollNumber);
            userAnalysis.setVisible(true);
        });

        logoutButton.addActionListener(event -> {
            blinkButton(logoutButton);
            dispose();
            // Create a new instance of the login frame
            new login();
        });

        gbc.gridy = 0;
        buttonPanel.add(takeTestButton, gbc);

        gbc.gridy = 1;
        buttonPanel.add(viewScoreButton, gbc);

        gbc.gridy = 2;
        buttonPanel.add(logoutButton, gbc);

        // Add the button panel to the frame's WEST position
        mainPanel.add(buttonPanel, BorderLayout.WEST);

        add(mainPanel);

        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFont(new Font("Arial", Font.BOLD, 28)); // Adjust font size as needed
        button.setForeground(Color.WHITE); // Set text color to white
        button.setPreferredSize(new Dimension(250, 100)); // Adjust button size as needed
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Set white border
        return button;
    }

    private void blinkButton(JButton button) {
        Timer timer = new Timer(100, new ActionListener() {
            private int count = 0;
            private boolean isVisible = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < 6) { // Blink 3 times
                    button.setBorder(isVisible ? BorderFactory.createLineBorder(Color.WHITE, 2) : BorderFactory.createEmptyBorder(2, 2, 2, 2));
                    isVisible = !isVisible;
                    count++;
                } else {
                    ((Timer) e.getSource()).stop();
                    button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Reset to original border
                }
            }
        });
        timer.start();
    }

    private String fetchUserNameFromDatabase(String rollNumber) {
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
        MongoCollection<Document> users = database.getCollection("users");

        Document query = new Document("rollno", rollNumber);
        FindIterable<Document> results = users.find(query).limit(1);

        // Extract the username from the query result
        String userName = null;
        for (Document document : results) {
            userName = document.getString("name");
        }

        mongoClient.close();

        return userName;
    }

    // Separate class for a JPanel with a background image
    class BackgroundImagePanel extends JPanel {
        private Image backgroundImage;

        public BackgroundImagePanel(Image backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw the background image
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExistingUserFrame("71762231047").setVisible(true));
    }
}
