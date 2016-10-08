package com.slg;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

/**
 * @author Jeff Risberg
 * @since 10/8/16
 */
public class GettingStartedTwo {

    static String keyspace = "ca01";

    public static void main(String[] args) {

        Cluster cluster;
        Session session;
        ResultSet results;

        // Connect to the cluster and keyspace "demo"
        cluster = Cluster
                .builder()
                .addContactPoint("localhost")
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withLoadBalancingPolicy(
                        new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().build()))
                .build();
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
        PreparedStatement statement = session.prepare(
                "INSERT INTO users (lastname, age, city, email, firstname) VALUES (?,?,?,?,?);");

        BoundStatement boundStatement = new BoundStatement(statement);

        session.execute(boundStatement.bind("Jones", 35, "Austin", "bob@example.com", "Bob"));

        // Use select to get the user we just entered
        Statement select = QueryBuilder.select().all().from(keyspace, "users")
                .where(eq("lastname", "Jones"));
        results = session.execute(select);
        for (Row row : results) {
            System.out.format("%s %d \n", row.getString("firstname"), row.getInt("age"));
        }

        // Update the same user with a new age
        Statement update = QueryBuilder.update(keyspace, "users")
                .with(QueryBuilder.set("age", 36))
                .where((eq("lastname", "Jones")));
        session.execute(update);

        // Select and show the change
        select = QueryBuilder.select().all().from(keyspace, "users")
                .where(eq("lastname", "Jones"));
        results = session.execute(select);
        for (Row row : results) {
            System.out.format("%s %d \n", row.getString("firstname"), row.getInt("age"));
        }

        // Delete the user from the users table
        Statement delete = QueryBuilder.delete().from("users")
                .where(eq("lastname", "Jones"));
        results = session.execute(delete);

        // Show that the user is gone
        select = QueryBuilder.select().all().from(keyspace, "users");
        results = session.execute(select);
        for (Row row : results) {
            System.out.format("%s %d %s %s %s\n", row.getString("lastname"),
                    row.getInt("age"), row.getString("city"),
                    row.getString("email"), row.getString("firstname"));
        }

        // Clean up the connection by closing it
        cluster.close();
    }
}
