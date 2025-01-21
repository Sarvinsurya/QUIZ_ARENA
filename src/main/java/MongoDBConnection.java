import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConnection {

    private MongoDatabase database;

    public MongoDBConnection() {
        // Connect to MongoDB server
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        // Get the database
        this.database = mongoClient.getDatabase("PROJMCQ");
        MongoCollection<Document> adminCollection = database.getCollection("ADMIN");
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        // Get the collection by name
        return database.getCollection(collectionName);
    }
}
