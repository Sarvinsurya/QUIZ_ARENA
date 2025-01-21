
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class login {
    private JFrame frame;

    public login() {   // Load the background image
        ImageIcon backgroundImageIcon = new ImageIcon("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\3.png");
        Image backgroundImage = backgroundImageIcon.getImage();

        // Create a JFrame instance
        JFrame frame = new JFrame("Main Frame");

        // Set the size of the frame
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Set the frame to close when the close button is clicked
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JPanel with the background image
        JPanel panelWithBackground = new BackgroundImagePanel(backgroundImage);

        // Set layout for the background panel
        panelWithBackground.setLayout(new BorderLayout());

        // Create an instance of OptionsPanel
        JPanel optionsPanel = new OptionsPanel(frame);

        // Make the options panel transparent
        optionsPanel.setOpaque(false);

        // Add the options panel to the background panel
        panelWithBackground.add(optionsPanel, BorderLayout.WEST); // Align to the left side

        // Add the background panel to the JFrame
        frame.add(panelWithBackground);

        // Make the frame visible
        frame.setVisible(true);
    }
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

// Separate class for the panel with the option buttons
class OptionsPanel extends JPanel {
    private JFrame parentFrame;
    public OptionsPanel(JFrame parentFrame) {
        // Set layout for the panel
        this.parentFrame=parentFrame;
        setLayout(new GridBagLayout());

        // Create option buttons
        JButton userButton = new JButton("User");
        JButton adminButton = new JButton("Admin");

        // Make the buttons transparent
        userButton.setOpaque(false);
        userButton.setContentAreaFilled(false);
        adminButton.setOpaque(false);
        adminButton.setContentAreaFilled(false);

        // Set font size for buttons
        Font buttonFont = userButton.getFont().deriveFont(Font.BOLD, 28); // Adjust font size as needed
        userButton.setFont(buttonFont);
        adminButton.setFont(buttonFont);

        // Set font color for buttons
        userButton.setForeground(Color.WHITE); // Set text color to white
        adminButton.setForeground(Color.WHITE); // Set text color to white

        // Set preferred size for buttons
        Dimension buttonSize = new Dimension(250, 100); // Adjust button size as needed
        userButton.setPreferredSize(buttonSize);
        adminButton.setPreferredSize(buttonSize);

        // Remove focus border when button is clicked
        userButton.setFocusPainted(false);
        adminButton.setFocusPainted(false);

        // Add ActionListener to adminButton
        adminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open login page frame
                parentFrame.setVisible(false);

                AdminLogin adminLogin = new AdminLogin();
                adminLogin.setVisible(true);
            }
        });

        userButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open login page frame
                parentFrame.setVisible(false);

                StudentLogin studentLogin = new StudentLogin( );
                studentLogin.setVisible(true);
            }
        });

        // Create GridBagConstraints for userButton
        GridBagConstraints userButtonConstraints = new GridBagConstraints();
        userButtonConstraints.gridx = 0;
        userButtonConstraints.gridy = 0;
        userButtonConstraints.anchor = GridBagConstraints.WEST;
        userButtonConstraints.insets = new Insets(10, 10, 10, 10); // Adjust insets

        // Create GridBagConstraints for adminButton
        GridBagConstraints adminButtonConstraints = new GridBagConstraints();
        adminButtonConstraints.gridx = 0;
        adminButtonConstraints.gridy = 1;
        adminButtonConstraints.anchor = GridBagConstraints.WEST;
        adminButtonConstraints.insets = new Insets(10, 10, 10, 10); // Adjust insets

        // Add buttons to the panel with GridBagConstraints
        add(userButton, userButtonConstraints);
        add(adminButton, adminButtonConstraints);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 0); // Adjust the width of the transparent panel
    }}






