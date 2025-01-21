import javax.swing.*;
import java.awt.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class AddQuestionFrame extends JFrame {
    private String courseId;
    private QuestionFrame questionFrame;
    private JTextField questionIdField;
    private JTextField testIdField;
    private JTextField questionPhraseField;
    private JTextField optionAField;
    private JTextField optionBField;
    private JTextField optionCField;
    private JTextField optionDField;
    private JTextField correctAnswerField;

    public AddQuestionFrame(String courseId,QuestionFrame questionFrame) {
        this.questionFrame=questionFrame;
        this.courseId = courseId;
        setTitle("Add Question");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create input panel
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add input fields for question details
        questionIdField = new JTextField();
        inputPanel.add(new JLabel("Question ID:"));
        inputPanel.add(questionIdField);

        testIdField = new JTextField();
        inputPanel.add(new JLabel("Test ID:"));
        inputPanel.add(testIdField);

        questionPhraseField = new JTextField();
        inputPanel.add(new JLabel("Question Phrase:"));
        inputPanel.add(questionPhraseField);

        optionAField = new JTextField();
        inputPanel.add(new JLabel("Option A:"));
        inputPanel.add(optionAField);

        optionBField = new JTextField();
        inputPanel.add(new JLabel("Option B:"));
        inputPanel.add(optionBField);

        optionCField = new JTextField();
        inputPanel.add(new JLabel("Option C:"));
        inputPanel.add(optionCField);

        optionDField = new JTextField();
        inputPanel.add(new JLabel("Option D:"));
        inputPanel.add(optionDField);

        correctAnswerField = new JTextField();
        inputPanel.add(new JLabel("Correct Answer:"));
        inputPanel.add(correctAnswerField);

        // Create "Submit" button
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> addQuestion());
        inputPanel.add(submitButton);

        // Add input panel to main panel
        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // Add main panel to frame
        getContentPane().add(mainPanel);
    }

    private void addQuestion() {
        try {
            // Retrieve values from input fields
            String questionId = questionIdField.getText().trim();
            String testId = testIdField.getText().trim();
            String questionPhrase = questionPhraseField.getText().trim();
            String optionA = optionAField.getText().trim();
            String optionB = optionBField.getText().trim();
            String optionC = optionCField.getText().trim();
            String optionD = optionDField.getText().trim();
            String correctAnswer = correctAnswerField.getText().trim();

            // Check if any input field is empty
            if (questionId.isEmpty() || testId.isEmpty() || questionPhrase.isEmpty() ||
                    optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty() || correctAnswer.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all the fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Exit the method if any field is empty
            }

            // Connect to MongoDB
            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
            MongoCollection<Document> questions = database.getCollection("questions");

            // Construct a Document representing the question
            Document questionDoc = new Document();
            questionDoc.append("courseId", courseId)
                    .append("testId", testId)
                    .append("questionId", questionId)
                    .append("questionPhrase", questionPhrase)
                    .append("options", java.util.Arrays.asList(
                            new Document("optionId", "A").append("optionText", optionA),
                            new Document("optionId", "B").append("optionText", optionB),
                            new Document("optionId", "C").append("optionText", optionC),
                            new Document("optionId", "D").append("optionText", optionD)
                    ))
                    .append("correctAnswer", correctAnswer);

            // Insert the document into the collection
            questions.insertOne(questionDoc);
            // Display success message
            JOptionPane.showMessageDialog(this, "Question added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear input fields
            clearInputFields();

            // Close the AddQuestionFrame and reopen the QuestionFrame
            dispose();
            questionFrame.dispose();
            questionFrame = new QuestionFrame(courseId);
            questionFrame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to add question: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void clearInputFields() {
        questionIdField.setText("");
        testIdField.setText("");
        questionPhraseField.setText("");
        optionAField.setText("");
        optionBField.setText("");
        optionCField.setText("");
        optionDField.setText("");
        correctAnswerField.setText("");
    }
}
