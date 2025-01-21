import org.bson.Document;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UserAnalysis extends JFrame {
    private JLabel rollNumberLabel;
    private JPanel scoresPanel;

    public UserAnalysis(String rollNumber) {
        String userName = fetchUserNameFromDatabase(rollNumber);

        setTitle("User Analysis");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create a panel for displaying the roll number and name at the top right corner
        JPanel rollNumberPanel = new JPanel(new BorderLayout());
        rollNumberPanel.setPreferredSize(new Dimension(getWidth(), 70)); // Set panel height
        rollNumberPanel.setBackground(Color.LIGHT_GRAY); // Set background color

        // Create label for roll number and name with HTML for styling
        rollNumberLabel = new JLabel("<html><div style='text-align: right; font-size: 16px;'>Name: " + userName + " <br> Roll Number: " + rollNumber + "</div></html>", SwingConstants.RIGHT);
        rollNumberPanel.add(rollNumberLabel, BorderLayout.EAST);

        // Add the roll number panel to the frame's NORTH position
        add(rollNumberPanel, BorderLayout.NORTH);

        // Create a panel to hold the scores
        scoresPanel = new JPanel(new BorderLayout());
        scoresPanel.setPreferredSize(new Dimension(760, 500));

        // Add the scores panel to the center of the frame
        add(scoresPanel, BorderLayout.CENTER);

        // Fetch data from database and create the bar graph panel
        BarGraphPanel barGraphPanel = new BarGraphPanel(fetchScoresFromDatabase(rollNumber));
        scoresPanel.add(barGraphPanel, BorderLayout.CENTER);

        scoresPanel.revalidate();
        scoresPanel.repaint();
    }

    private String fetchUserNameFromDatabase(String rollNumber) {
        String userName = "Unknown";
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
            MongoCollection<Document> userCollection = database.getCollection("users");

            Document query = new Document("rollno", rollNumber); // Assuming rollno field corresponds to roll number
            FindIterable<Document> results = userCollection.find(query);

            for (Document doc : results) {
                userName = doc.getString("name");
                break; // Assume roll number is unique, so we only need the first result
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userName;
    }

    private Map<String, Map<String, Integer>> fetchScoresFromDatabase(String rollNumber) {
        Map<String, Map<String, Integer>> scoresMap = new HashMap<>();
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("MCQ_EXAMINATION");
            MongoCollection<Document> collection = database.getCollection("Score");

            Document query = new Document("rollnumber", rollNumber);
            FindIterable<Document> results = collection.find(query);

            for (Document doc : results) {
                String courseId = doc.getString("courseId");
                String testId = doc.getString("testId");
                int score = doc.getInteger("score");

                scoresMap.computeIfAbsent(courseId, k -> new HashMap<>()).put(testId, score);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scoresMap;
    }

    private class BarGraphPanel extends JPanel {
        private Map<String, Map<String, Integer>> scoresMap;
        private Map<String, Color> courseColors;

        public BarGraphPanel(Map<String, Map<String, Integer>> scoresMap) {
            this.scoresMap = scoresMap;
            this.courseColors = generateCourseColors();
            setPreferredSize(new Dimension(760, 500));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBarGraph(g);
        }

        private void drawBarGraph(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            int padding = 25;
            int labelPadding = 25;
            int numberYDivisions = 8;

            int maxScore = getMaxScore()+5;
            int minScore = getMinScore()-5;

            int totalBars = getTotalBarsCount();
            int barWidth = (width - 2 * padding - labelPadding) / (totalBars * 2); // Reduce bar width

            // Draw background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.BLACK);

            // Draw Y-axis
            for (int i = 0; i <= numberYDivisions; i++) {
                int x0 = padding + labelPadding;
                int x1 = barWidth * totalBars * 2 + padding + labelPadding;
                int y0 = height - ((i * (height - padding * 2)) / numberYDivisions + padding);
                int y1 = y0;
                g.drawLine(x0, y0, x1, y1);
                String yLabel = String.valueOf((int) (minScore + ((maxScore - minScore) * ((i * 1.0) / numberYDivisions))));
                FontMetrics metrics = g.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            }

            // Draw X-axis and bars
            int x = padding + labelPadding;
            for (String courseId : scoresMap.keySet()) {
                Map<String, Integer> tests = scoresMap.get(courseId);
                int courseBarStart = x; // Starting position of bars for this course
                Color courseColor = courseColors.get(courseId); // Get color for the course
                for (String testId : tests.keySet()) {
                    int value = tests.get(testId);
                    double barHeightRatio = (value - minScore) * 1.0 / (maxScore - minScore);
                    int barHeight = (int) (barHeightRatio * (height - padding * 2));
                    int barY = height - barHeight - padding;

                    g.setColor(courseColor);
                    if (value >= 0) {
                        g.fillRect(x, barY, barWidth - 10, barHeight);
                    } else {
                        g.fillRect(x, height - padding, barWidth - 10, -barHeight);
                    }
                    g.setColor(Color.BLACK);
                    g.drawRect(x, barY, barWidth - 10, barHeight);

                    // Draw the testId inside the bar
                    FontMetrics metrics = g.getFontMetrics();
                    int labelWidth = metrics.stringWidth(testId);
                    g.setColor(Color.black);
                    g.drawString(testId, x + (barWidth - 10) / 2 - labelWidth / 2, barY + barHeight / 2 + metrics.getHeight() / 2);
                    g.drawString(testId, x + (barWidth - 10) / 2 - labelWidth / 2, barY + barHeight / 2 + metrics.getHeight() / 2);
                    x += barWidth;
                }
                // Draw the courseId below the bars
                FontMetrics metrics = g.getFontMetrics();
                int labelWidth = metrics.stringWidth(courseId);
                int courseBarEnd = x - barWidth; // Ending position of bars for this course
                int courseLabelX = courseBarStart + (courseBarEnd - courseBarStart) / 2 - labelWidth / 2;
                g.setColor(Color.BLACK);
                g.drawString(courseId, courseLabelX, height - padding + metrics.getHeight() + 5);
            }
        }

        private int getTotalBarsCount() {
            int totalBars = 0;
            for (Map<String, Integer> tests : scoresMap.values()) {
                totalBars += tests.size();
            }
            return totalBars;
        }

        private int getMaxScore() {
            int maxScore = Integer.MIN_VALUE;
            for (Map<String, Integer> tests : scoresMap.values()) {
                for (int score : tests.values()) {
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
            }
            return maxScore;
        }

        private int getMinScore() {
            int minScore = Integer.MAX_VALUE;
            for (Map<String, Integer> tests : scoresMap.values()) {
                for (int score : tests.values()) {
                    if (score < minScore) {
                        minScore = score;
                    }
                }
            }
            return minScore;
        }

        private Map<String, Color> generateCourseColors() {
            Map<String, Color> colors = new HashMap<>();
            Random random = new Random();
            for (String courseId : scoresMap.keySet()) {
                Color color;
                do {
                    // Generate a random color
                    color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                } while (color.equals(Color.WHITE)); // Check if the color is white

                colors.put(courseId, color);
            }
            return colors;
        }
    }

    // Main method for independent running
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Sample roll number for testing
            String sampleRollNumber = "71762231047";
            UserAnalysis userAnalysis = new UserAnalysis(sampleRollNumber);
            userAnalysis.setVisible(true);
        });
    }
}