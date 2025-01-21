import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class UserFrame extends JFrame {
    private JFrame mainFrame;
    private Border border;
    public static MongoClient mongoClient;
    public static MongoDatabase database;
    public static MongoCollection<Document> users;
    private BufferedImage backgroundImage;

    public UserFrame(JFrame mainFrame, Border border) {
        this.mainFrame = mainFrame;
        this.border = border;

        setTitle("New User Page");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try {
            backgroundImage = ImageIO.read(new File("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
        setContentPane(backgroundPanel);
        backgroundPanel.setLayout(new GridBagLayout());


        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        JTextField nameField = new JTextField();

        JLabel rollNoLabel = new JLabel("Roll Number:");
        rollNoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rollNoLabel.setForeground(Color.WHITE);
        JTextField rollNoField = new JTextField();

        JLabel emailLabel = new JLabel("Email ID:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 18));
        emailLabel.setForeground(Color.WHITE);
        JTextField emailField = new JTextField();

        JLabel dobLabel = new JLabel("Date of Birth (YYYY-MM-DD):");
        dobLabel.setFont(new Font("Arial", Font.BOLD, 18));
        dobLabel.setForeground(Color.WHITE);
        JTextField dobField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 18));
        passwordLabel.setForeground(Color.WHITE);
        JPasswordField passwordField = new JPasswordField();

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.BOLD, 18));
        confirmPasswordLabel.setForeground(Color.WHITE);
        JPasswordField confirmPasswordField = new JPasswordField();

        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");


        Dimension buttonSize = new Dimension(200, 50);
        registerButton.setPreferredSize(buttonSize);
        backButton.setPreferredSize(buttonSize);


        Font buttonFont = new Font("Arial", Font.BOLD, 16);
        registerButton.setFont(buttonFont);
        backButton.setFont(buttonFont);


        registerButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));


        registerButton.addActionListener(e -> {
            mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            database = mongoClient.getDatabase("MCQ_EXAMINATION");
            users = UserFrame.database.getCollection("users");
            System.out.println("Database Connected");

            String name = nameField.getText();
            String rollNumber = rollNoField.getText();
            String email = emailField.getText();
            String dob = dobField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());


            if (name.isEmpty() || rollNumber.isEmpty() || email.isEmpty() || dob.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields are required", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (!name.matches("[a-zA-Z]+")) {
                JOptionPane.showMessageDialog(null, "Name must contain only letters", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (!rollNumber.matches("7176\\d{7}")) {
                JOptionPane.showMessageDialog(null, "Roll Number must start with '7176' followed by 7 digits", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
                JOptionPane.showMessageDialog(null, "Invalid email address", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if DOB is in the correct format (YYYY-MM-DD)
            try {
                LocalDate.parse(dob);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(null, "Date of Birth must be in the format YYYY-MM-DD", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return; // Exit the ActionListener
            }

            // Check if password contains at least one uppercase letter, one lowercase letter, one numeric digit, and one special character
            if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
                JOptionPane.showMessageDialog(null, "Password must contain at least one uppercase letter, one lowercase letter, one numeric digit, and one special character (@#$%^&+=!)", "Registration Error", JOptionPane.ERROR_MESSAGE);
                confirmPasswordField.setText("");
                return; // Exit the ActionListener
            }

            // Check if password and confirm password match
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(null, "Password and Confirm Password do not match", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return; // Exit the ActionListener
            }

            Document query = new Document("rollno", rollNumber);
            FindIterable<Document> results = users.find(query);
            if (results.iterator().hasNext()) {
                JOptionPane.showMessageDialog(null, "Roll number already exists in the database", "Registration Error", JOptionPane.ERROR_MESSAGE);
                rollNoField.setText("");
            } else {
                Document emailQuery = new Document("email", email);
                FindIterable<Document> emailResults = users.find(emailQuery);
                if (emailResults.iterator().hasNext()) {
                    JOptionPane.showMessageDialog(null, "Email already exists in the database", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    emailField.setText("");
                    return; // Exit the ActionListener
                }
                Document newUser = new Document();
                newUser.append("name", name)
                        .append("rollno", rollNumber)
                        .append("email", email)
                        .append("dob", dob)
                        .append("password", password);

                // Insert the new document into the database
                users.insertOne(newUser);

                JOptionPane.showMessageDialog(null, "User registered successfully", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                mainFrame.setVisible(true);
                // Clear all fields after successful registration
                nameField.setText("");
                rollNoField.setText("");
                emailField.setText("");
                dobField.setText("");
                passwordField.setText("");
                confirmPasswordField.setText("");
            }
        });

        // Back button ActionListener
        backButton.addActionListener(event -> {
            dispose();
            mainFrame.setVisible(true);
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        backgroundPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        backgroundPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        backgroundPanel.add(rollNoLabel, gbc);

        gbc.gridx = 1;
        backgroundPanel.add(rollNoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        backgroundPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        backgroundPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        backgroundPanel.add(dobLabel, gbc);

        gbc.gridx = 1;
        backgroundPanel.add(dobField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        backgroundPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        backgroundPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        backgroundPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        backgroundPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        backgroundPanel.add(registerButton, gbc);

        gbc.gridx = 1;
        backgroundPanel.add(backButton, gbc);

        // Set border for the content pane
        ((JPanel) getContentPane()).setBorder(border);

        setVisible(true);
    }

    public UserFrame() {
    }

    private static UserObj mapDocumentToUserObj(Document document) {
        UserObj userObj = new UserObj();
        userObj.setUsername(document.getString("username"));
        userObj.setPassword(document.getString("password"));
        userObj.setEmail(document.getString("email"));
        userObj.setRollno(document.getString("rollno"));
        return userObj;
    }

    // Custom JPanel to handle background image
    class BackgroundPanel extends JPanel {
        private BufferedImage image;

        public BackgroundPanel(BufferedImage image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
