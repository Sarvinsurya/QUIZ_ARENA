import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class QuestionFrame extends JFrame {
    private String courseId;
    private JTable table;

    public QuestionFrame(String courseId) {
        this.courseId = courseId;
        setTitle("Questions for Course: " + courseId);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create table panel
        JPanel tablePanel = new JPanel(new BorderLayout());

        // Create table with custom DefaultTableModel
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Create "Add Question" button
        JButton addQuestionButton = new JButton("Add Question");
        addQuestionButton.addActionListener(e -> {
            AddQuestionFrame addQuestionFrame = new AddQuestionFrame(courseId,this);
            addQuestionFrame.setVisible(true);
        });

        // Add components to main panel
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(addQuestionButton, BorderLayout.SOUTH);

        // Add main panel to frame
        getContentPane().add(mainPanel);

        // Load questions into the table
        loadQuestions();
    }

    private void loadQuestions() {
        try {
            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
            MongoCollection<Document> questions = database.getCollection("questions");

            ArrayList<String> columns = new ArrayList<>(Arrays.asList("Question ID", "Test ID", "Question Phrase", "Option A", "Option B", "Option C", "Option D", "Correct Answer"));

            DefaultTableModel model = new DefaultTableModel(columns.toArray(), 0);
            table.setModel(model);

            // Load existing questions into the table
            MongoCursor<Document> cursor = questions.find(new Document("courseId", courseId)).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String questionId = doc.getString("questionId");
                String testId = doc.getString("testId");
                String questionPhrase = doc.getString("questionPhrase");
                java.util.List<Document> options = (java.util.List<Document>) doc.get("options");
                String optionA = options.get(0).getString("optionText");
                String optionB = options.get(1).getString("optionText");
                String optionC = options.get(2).getString("optionText");
                String optionD = options.get(3).getString("optionText");
                String correctAnswer = doc.getString("correctAnswer");

                model.addRow(new Object[]{questionId, testId, questionPhrase, optionA, optionB, optionC, optionD, correctAnswer});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load questions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
