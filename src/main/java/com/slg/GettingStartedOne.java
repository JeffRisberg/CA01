package com.slg;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * @author Jeff Risberg
 * @since 10/8/16
 */
public class GettingStartedOne {

    static String keyspace = "ca01";

    public static void main(String[] args) {

        Cluster cluster;
        Session session;

        try {
            // Connect to the cluster and keyspace "demo"
            cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
            session = cluster.connect(keyspace);

            // Create the users table
            session.execute("DROP TABLE IF EXISTS users");
            session.execute("CREATE TABLE users ( " +
                    "firstname text, " +
                    "lastname text, " +
                    "age int, " +
                    "email text, " +
                    "city text, " +
                    "PRIMARY KEY (lastname));");

            // Insert one record into the users table
            session.execute("INSERT INTO users (lastname, age, city, email, firstname) VALUES ('Jones', 35, 'Austin', 'bob@example.com', 'Bob')");

            // Use select to get the user we just entered
            ResultSet results = session.execute("SELECT * FROM users WHERE lastname='Jones'");
            for (Row row : results) {
                System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
            }

            // Update the same user with a new age
            session.execute("update users set age = 36 where lastname = 'Jones'");

            // Select and show the change
            results = session.execute("select * from users where lastname='Jones'");
            for (Row row : results) {
                System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
            }

            // Delete the user from the users table
            session.execute("DELETE FROM users WHERE lastname = 'Jones'");

            // Show that the user is gone
            results = session.execute("SELECT * FROM users");
            for (Row row : results) {
                System.out.format("%s %d %s %s %s\n", row.getString("lastname"), row.getInt("age"), row.getString("city"), row.getString("email"), row.getString("firstname"));
            }

            // Clean up the connection by closing it
            cluster.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}