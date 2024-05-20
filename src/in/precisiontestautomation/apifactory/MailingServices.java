package in.precisiontestautomation.apifactory;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Getter;
import org.awaitility.Awaitility;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MailingServices provides functionalities to interact with a temporary email service using its API.
 * This class enables creating temporary email addresses, checking the mailbox, fetching emails,
 * and deleting email accounts. It is useful for testing or any application that requires interaction
 * with email services without the need for permanent email accounts.
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class MailingServices {

    private static final String ONE_SEC_MAIL_HOST = "https://www.1secmail.com/api/v1/";
    private static final String ONE_SEC_MAIL_HOST_DELETE = "https://www.1secmail.com/mailbox";

    private String mailName;
    private String mailDomain;
    private String mailMessageID;
    @Getter
    private String mailBody;
    @Getter
    private String emailAddress;

    /**
     * Returns an instance of MailingServices. This factory method encapsulates the instantiation
     * logic and can be extended to include singleton or prototype logic as needed.
     *
     * @return a new instance of MailingServices
     */
    public static MailingServices getInstance() {
        return new MailingServices();
    }

    /**
     * Generates a random email address using the 1secmail API. This method handles the API request
     * and updates the instance with the new email address details.
     *
     * @return this instance of MailingServices, updated with a new random email address
     */
    public MailingServices generateRandomEmailAddress() {
        HashMap<String, String> queryParamsMapper = new HashMap<>();
        queryParamsMapper.put("action", "genRandomMailbox");
        queryParamsMapper.put("count", "1");

        Response response = RestAssured.given().baseUri(ONE_SEC_MAIL_HOST)
                .queryParams(queryParamsMapper)
                .get();

        emailAddress = response.getBody().jsonPath().getList("$").get(0).toString();
        mailName = emailAddress.split("@")[0];
        mailDomain = emailAddress.split("@")[1];
        return this;
    }

    /**
     * Retrieves the list of messages from the mailbox. This method waits up to 30 seconds for an email
     * to appear in the mailbox and then retrieves the message id of the first email received.
     *
     * @return this instance of MailingServices, updated with the latest mail message ID
     */
    public MailingServices getMailBox(){
        Map<String,String> queryParamsMapper = new HashMap<>();
        queryParamsMapper.put("action","getMessages");
        queryParamsMapper.put("login",mailName);
        queryParamsMapper.put("domain",mailDomain);


        Awaitility.given().with().atMost(30, TimeUnit.SECONDS).until(()->
                !RestAssured.given().baseUri(ONE_SEC_MAIL_HOST)
                        .queryParams(queryParamsMapper)
                        .get().getBody().jsonPath().getList("$").isEmpty());

        Response response = RestAssured.given().baseUri(ONE_SEC_MAIL_HOST)
                .queryParams(queryParamsMapper)
                .get();
        response.getBody().prettyPrint();
        mailMessageID = response.getBody().jsonPath().getString("[0].id");
        return this;
    }

    /**
     * Fetches an email using its ID from the mailbox and updates the mailBody attribute with the
     * content of the email.
     *
     * @return this instance of MailingServices, updated with the body of the fetched email
     */
    public MailingServices fetchMailUsingId(){
        Map<String,String> queryParamsMapper = new HashMap<>();
        queryParamsMapper.put("action","readMessage");
        queryParamsMapper.put("login",mailName);
        queryParamsMapper.put("domain",mailDomain);
        queryParamsMapper.put("id",mailMessageID);

        Response response = RestAssured.given().baseUri(ONE_SEC_MAIL_HOST)
                .queryParams(queryParamsMapper)
                .get();
        response.prettyPrint();
        mailBody = response.getBody().jsonPath().getString("body");

        return this;
    }

    /**
     * Deletes the mailbox associated with the email address. This method sends a request to the 1secmail API
     * to delete the mailbox, effectively removing all messages and the mailbox itself.
     *
     * @return this instance of MailingServices
     */
    public MailingServices deleteMailBox(){
        Map<String,String> formData = new HashMap<>();
        formData.put("action","deleteMailbox");
        formData.put("login",mailName);
        formData.put("domain",mailDomain);

        RestAssured.given().baseUri(ONE_SEC_MAIL_HOST_DELETE)
                .formParams(formData)
                .post();
        return this;
    }
}
