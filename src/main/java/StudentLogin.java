import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class StudentLogin extends JFrame {
    public static MongoClient mongoClient;
    public static MongoDatabase database;
    public static MongoCollection<Document> users;
    private JFrame mainFrame;
    public JTextField rollNumberField;
    public JPasswordField passwordField;
    private Border border;
    private String[] questionPhrases;
    private String[] questionIds;
    private String[] correctAnswers;

    public StudentLogin() {
        mainFrame=this;
        setTitle("Student - User Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ImageIcon backgroundImageIcon = new ImageIcon("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\3.png");
        Image backgroundImage = backgroundImageIcon.getImage();
        JPanel panelWithBackground = new BackgroundImagePanel(backgroundImage);
        panelWithBackground.setLayout(new BorderLayout());

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setOpaque(false); // Make the panel transparent

        JLabel loginTitleLabel = new JLabel("STUDENT LOGIN");
        loginTitleLabel.setFont(new Font("Palatino", Font.BOLD, 45));
        loginTitleLabel.setForeground(Color.WHITE);
        GridBagConstraints titleGbc = new GridBagConstraints();
        titleGbc.gridx = 0;
        titleGbc.gridy = 0;
        titleGbc.gridwidth = 2; // Span two columns
        titleGbc.anchor = GridBagConstraints.NORTH;
        titleGbc.insets = new Insets(-100, 0, 10, 0); // Adjust top padding
        loginPanel.add(loginTitleLabel, titleGbc);



        JLabel rollNumberLabel = new JLabel("Roll Number    :");
        JLabel passwordLabel = new JLabel("Password        :");
        rollNumberField = new JTextField(20);
        passwordField = new JPasswordField(20);

        rollNumberLabel.setForeground(Color.WHITE);
        passwordLabel.setForeground(Color.WHITE);


        Font labelFont = rollNumberLabel.getFont().deriveFont(Font.BOLD, 20);
        Font fieldFont = rollNumberField.getFont().deriveFont(Font.PLAIN, 20);
        rollNumberLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        rollNumberField.setFont(fieldFont);
        passwordField.setFont(fieldFont);


        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.PLAIN, 20));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(Color.BLACK);
        loginButton.setFocusPainted(false);


        JLabel newUserLabel = new JLabel("New User? Register here.");
        newUserLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        newUserLabel.setForeground(Color.WHITE);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Perform login authentication
                String rollNumber = rollNumberField.getText();
                String password = new String(passwordField.getPassword());

                boolean checkrool=checkPassword(rollNumber,password);

                if (checkrool) {
                    ExistingUserFrame existingUserFrame=new ExistingUserFrame(rollNumber);

                    dispose();
                }

                
                passwordField.setText("");
            }
        });


        newUserLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                UserFrame newUserFrame = new UserFrame(mainFrame, border);
                setVisible(false); // Hide the current frame

            }
        });


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(25, 70, 25, 25); // Padding
        gbc.anchor = GridBagConstraints.WEST;

        loginPanel.add(rollNumberLabel, gbc);
        gbc.gridy++;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx++;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(rollNumberField, gbc);
        gbc.gridy++;
        loginPanel.add(passwordField, gbc);


        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(25, 150, 10, 100);
        loginPanel.add(loginButton, gbc);


        gbc.gridy++;
        loginPanel.add(newUserLabel, gbc);

        panelWithBackground.add(loginPanel, BorderLayout.WEST);

        add(panelWithBackground);
    }


    class BackgroundImagePanel extends JPanel {
        private Image backgroundImage;

        public BackgroundImagePanel(Image backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    private static UserObj mapDocumentToUserObj(Document document) {
        UserObj userObj = new UserObj();
        userObj.setUsername(document.getString("username"));
        userObj.setPassword(document.getString("password"));
        userObj.setEmail(document.getString("email"));
        userObj.setRollno(document.getString("rollno"));
        return userObj;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StudentLogin studentLogin = new StudentLogin();
                studentLogin.setVisible(true);
            }
        });
    }

    public static boolean checkPassword(String rollNumber, String password) {
        mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        database = mongoClient.getDatabase("MCQ_EXAMINATION");
        users = database.getCollection("users");

        System.out.println("database connected");

        Document query = new Document("rollno", rollNumber);
        FindIterable<Document> results = users.find(query).limit(1);
        System.out.println(query);
        String pass;

        if (!results.iterator().hasNext()) {
            JOptionPane.showMessageDialog(null, "Roll number not found in the database", "Login Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            for (Document document : results) {
                UserObj userObj = mapDocumentToUserObj(document);
                pass = userObj.getPassword();

                if (password.equals(pass)) {
                    return true;
                }
            }
            JOptionPane.showMessageDialog(null, "Invalid user name or password", "Invalid username", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void actionPerformed(ActionEvent e) {

        String rollNumber = rollNumberField.getText();
        String password = new String(passwordField.getPassword());

        boolean checkRoll = checkPassword(rollNumber, password);

        if (checkRoll) {
            ExistingUserFrame existingUserFrame=new ExistingUserFrame(rollNumber);
            setVisible(false);
            dispose();
        } else {
            JOptionPane.showMessageDialog(null, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
