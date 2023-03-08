package org.mongo.labs;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.Arrays;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.include;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class App {
    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create(getClientSettings())) {

            MongoDatabase database = mongoClient.getDatabase("lab2db");
            MongoCollection<Book> booksCollection = database.getCollection("books", Book.class);

            Book book1 = new Book("book 1", new Author("author name 1", "author surname 1"), 10,
                    asList("review 1_1", "review 1_2"));
            Book book2 = new Book("book 2", new Author("author name 2", "author surname 2"), 20,
                    asList("review 2_1", "review 2_2", "review x"));
            Book book3 = new Book("book 2", new Author("author name 3", "author surname 3"), 30,
                    asList("review 3_1", "review 3_2", "review x"));

            // delete all items
            System.out.println("Deleting all elements ...");
            booksCollection.deleteMany(new BsonDocument());
            // insert books
            System.out.println("inserting book1 ...");
            booksCollection.insertOne(book1);
            System.out.println("inserting book2, book3 ...");
            booksCollection.insertMany(asList(book2, book3));

            // query collection
            System.out.println("\nfind all results:");
            booksCollection.find().forEach(System.out::println);

            System.out.println("\nfind by 1 attribute:");
            booksCollection.find(eq("name", "book 1")).forEach(System.out::println);

            System.out.println("\nfind by 2 attributes:");
            booksCollection.find(and(eq("reviews", "review x"), gt("pages", 25))).forEach(System.out::println);

            System.out.println("\nfind by 1 attribute with projection:");
            booksCollection.find(eq("author.surname", "author surname 1")).projection(include("name", "pages")).forEach(System.out::println);

            database = mongoClient.getDatabase("sample_supplies");
            MongoCollection<Document> salesCollection = database.getCollection("sales");


            // { $and: [{ $expr: { $in: [{$month: "$saleDate"},[6,7,8]]} }, {couponUsed: true}]}
            Bson filter = and(asList(
                    in("$expr", asList(eq("$month", "$saleDate"), Arrays.asList(6, 7, 8))),
                    eq("couponUsed", true))
            );

            System.out.println("\nItems sold in summer with coupon:");
            salesCollection.find(filter).projection(include("saleDate", "couponUsed")).limit(4).forEach(System.out::println);
        }
    }

    private static MongoClientSettings getClientSettings() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/");
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
        return clientSettings;
    }
}
