package org.ndx.retweet.by.consensus.bot;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class TwitterWebHookReceiverTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/twitter")
          .then()
             .statusCode(200)
             .body(is("hello"));
    }

}