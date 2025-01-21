import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class QuizFrame extends JFrame {
    private JLabel imageLabel;
    private JTextArea questionLabel;
    private JRadioButton[] optionButtons;
    private JButton prevButton;
    private JButton nextButton;
    private JButton submitButton;
    private JLabel timerLabel;
    private Timer timer;
    private int timeRemaining = 100;
    private int currentQuestionIndex = 0;
    private int totalQuestions;
    private JPanel navPanel;
    private JLayeredPane layeredPane;
    private String courseId;
    private String testId;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> questionsCollection;
    private String rollnumber;

    private String[] questionPhrases;
    private String[] questionIds;
    private String[] correctAnswers;
    private int score = 0;
    private ArrayList<String> selectedOptions;
    private String[][] optionsArray;


    public QuizFrame(String rollnumber, String courseId, String testId) {
        this.rollnumber = rollnumber;
        this.courseId = courseId;
        this.testId = testId;

        // Set up connection
        try {
            mongoClient = new MongoClient("localhost", 27017);
            database = mongoClient.getDatabase("MCQ_EXAMINATION");
            questionsCollection = database.getCollection("questions");
            totalQuestions = (int) questionsCollection.countDocuments(new Document("courseId", courseId).append("testId", testId));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to MongoDB: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        questionPhrases = new String[totalQuestions];
        questionIds = new String[totalQuestions];
        correctAnswers = new String[totalQuestions];
        selectedOptions = new ArrayList<>();
        //fetchQuestions();
        fetchQuestionsAndOptions();

        setTitle("Quiz Frame");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);

        // Layered Pane
        layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane);

        // Load and resize the image
        try {
            BufferedImage originalImage = ImageIO.read(new File("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\images.jpg"));
            Image scaledImage = originalImage.getScaledInstance(screenSize.width, screenSize.height, Image.SCALE_SMOOTH);
            ImageIcon imageIcon = new ImageIcon(scaledImage);
            imageLabel = new JLabel(imageIcon);
            imageLabel.setBounds(0, 0, screenSize.width, screenSize.height);
            layeredPane.add(imageLabel, JLayeredPane.DEFAULT_LAYER);
        } catch (Exception e) {
            e.printStackTrace();
        }


        JPanel questionPanel = new JPanel(new GridBagLayout());
        questionPanel.setOpaque(false);
        questionLabel = new JTextArea(); // Changed JLabel to JTextArea
        questionLabel.setFont(new Font("Times New Roman", Font.PLAIN, 25));
        questionLabel.setForeground(Color.white);
        questionLabel.setOpaque(false);
        questionLabel.setEditable(false);
        questionLabel.setLineWrap(true); // Enable line wrapping
        questionLabel.setWrapStyleWord(true); // Wrap whole words
        questionLabel.setSize(new Dimension(800,500));
        questionLabel.setAlignmentX(100);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Allow horizontal resizing
        questionPanel.add(questionLabel, gbc);

        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setOpaque(false);
        ButtonGroup optionGroup = new ButtonGroup();
        optionButtons = new JRadioButton[4];
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setForeground(Color.WHITE);
            optionButtons[i].setBackground(Color.BLACK);
            optionGroup.add(optionButtons[i]);
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.anchor = GridBagConstraints.WEST;
            optionsPanel.add(optionButtons[i], gbc);
        }
        questionPanel.add(optionsPanel, gbc);

        questionPanel.setBounds(0, 0, screenSize.width, screenSize.height);
        layeredPane.add(questionPanel, JLayeredPane.PALETTE_LAYER);





        navPanel = new JPanel();
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        submitButton = new JButton("Submit");

        navPanel.add(prevButton);
        navPanel.add(nextButton);
        navPanel.setBackground(new Color(255, 255, 255, 255));
        navPanel.setBounds(0, screenSize.height - 120, screenSize.width, 80);
        layeredPane.add(navPanel, JLayeredPane.MODAL_LAYER);

        timerLabel = new JLabel("Time Remaining: " + timeRemaining);
        timerLabel.setForeground(Color.RED);
        timerLabel.setBackground(Color.white);
        navPanel.add(timerLabel);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                if (timeRemaining >= 0) {
                    timerLabel.setText("Time Remaining: " + timeRemaining);
                } else {
                    timer.stop();
                    automaticSubmit();
                }
            }
        });
        setVisible(true);
        timer.start();

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveToPreviousQuestion();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveToNextQuestion();
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                calculateScore();
                JOptionPane.showMessageDialog(null, "Quiz submitted! Your score is: " + score);
                storeScoreInDatabase(rollnumber, courseId, testId, score);
                dispose();
                new ExistingUserFrame(rollnumber);
            }
        });

        loadQuestion(currentQuestionIndex);
    }

    private void loadQuestion(int index) {
        resetOptionButtons();

        String questionPhrase = questionPhrases[index];
        questionLabel.setText("Question " + (index + 1) + ": " + questionPhrase);

        String[] optionsArray = getOptionsArray(questionIds[index]);

        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(optionsArray[i]);

            if (selectedOptions.size() > index && optionsArray[i].startsWith(selectedOptions.get(index))) {
                optionButtons[i].setSelected(true);
            } else {
                optionButtons[i].setSelected(false);
            }
        }
    }

    private String[] getOptionsArray(String questionId) {
        for (int i = 0; i < questionIds.length; i++) {
            if (questionIds[i].equals(questionId)) {
                return optionsArray[i];
            }
        }
        return new String[0];
    }





    private void showSubmitButton() {
        submitButton.setVisible(true);
        navPanel.add(submitButton);
        layeredPane.repaint();
    }

    private void automaticSubmit() {
        calculateScore();
        JOptionPane.showMessageDialog(null, "Time's up! Quiz submitted. Your score is: " + score);
        storeScoreInDatabase(rollnumber, courseId, testId, score);
        dispose();
        new ExistingUserFrame(rollnumber);
    }

    private void calculateScore() {
        int minSize = Math.min(selectedOptions.size(), correctAnswers.length);
        for (int i = 0; i < minSize; i++) {
            String selectedOption = selectedOptions.get(i);
            String correctAnswer = correctAnswers[i];

            if (selectedOption != null && selectedOption.equals(correctAnswer)) {
                score += 4;
            } else {
                score -= 1;
            }
        }
    }

    private void moveToNextQuestion() {
        String selectedOption = getSelectedOption();
        if (selectedOptions.size() > currentQuestionIndex) {
            selectedOptions.set(currentQuestionIndex, selectedOption);
        } else {
            selectedOptions.add(selectedOption);
        }
        if (currentQuestionIndex < Math.min(questionIds.length, 10) - 1)  {
            currentQuestionIndex++;
            loadQuestion(currentQuestionIndex);
        } else {
            showSubmitButton();
        }
    }




    private void moveToPreviousQuestion() {
        if (selectedOptions.size() > currentQuestionIndex && selectedOptions.get(currentQuestionIndex) == null) {
            selectedOptions.remove(currentQuestionIndex);
        }
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            loadQuestion(currentQuestionIndex);
        }
    }

    private String getSelectedOption() {
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].isSelected()) {
                return optionButtons[i].getText().substring(0, 1);
            }
        }
        return null;
    }

    private void resetOptionButtons() {
        for (JRadioButton optionButton : optionButtons) {
            optionButton.setSelected(false);
        }
    }

    private void fetchQuestionsAndOptions() {
        ArrayList<String> allQuestionIds = new ArrayList<>();
        ArrayList<String[]> allOptionsArrays = new ArrayList<>();
        MongoCursor<Document> cursorAllIds = questionsCollection.find(new Document("courseId", courseId).append("testId", testId)).projection(new Document("questionId", 1)).iterator();
        while (cursorAllIds.hasNext()) {
            Document doc = cursorAllIds.next();
            allQuestionIds.add(doc.getString("questionId"));
        }
        cursorAllIds.close();

        Collections.shuffle(allQuestionIds);

        for (String questionId : allQuestionIds) {
            Document questionDoc = questionsCollection.find(new Document("courseId", courseId).append("testId", testId).append("questionId", questionId)).first();
            if (questionDoc != null) {
                ArrayList<String> optionsList = new ArrayList<>();
                ArrayList<Document> optionsArray = (ArrayList<Document>) questionDoc.get("options");
                for (Document option : optionsArray) {
                    String optionId = option.getString("optionId");
                    String optionText = option.getString("optionText");
                    optionsList.add(optionId + ": " + optionText);
                }
                String[] optionsArrayString = optionsList.toArray(new String[0]);
                allOptionsArrays.add(optionsArrayString);
            }
        }

        int numQuestions = Math.min(10, allQuestionIds.size());
        questionIds = new String[numQuestions];
        questionPhrases = new String[numQuestions];
        correctAnswers = new String[numQuestions];
        optionsArray = new String[numQuestions][];
        for (int i = 0; i < numQuestions; i++) {
            String questionId = allQuestionIds.get(i);
            Document questionDoc = questionsCollection.find(new Document("courseId", courseId).append("testId", testId).append("questionId", questionId)).first();
            if (questionDoc != null) {
                questionIds[i] = questionId;
                questionPhrases[i] = questionDoc.getString("questionPhrase");
                correctAnswers[i] = questionDoc.getString("correctAnswer");
                optionsArray[i] = allOptionsArrays.get(i);
            }
        }
    }

    private void storeScoreInDatabase(String rollnumber, String courseId, String testId, int score) {
        try {
            MongoCollection<Document> scoreCollection = database.getCollection("Score");
            Document scoreDocument = new Document()
                    .append("rollnumber", rollnumber)
                    .append("courseId", courseId)
                    .append("testId", testId)
                    .append("score", score);
            scoreCollection.insertOne(scoreDocument);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to store score in database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}

