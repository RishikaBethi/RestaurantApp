package helpers.specBuilders;

import context.ShareContext;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.bouncycastle.cert.ocsp.Req;

import static io.restassured.RestAssured.oauth2;

public class RequestBuilder {

    private final ShareContext shareContext;

    public RequestBuilder(ShareContext shareContext)
    {
        this.shareContext = shareContext;
    }

    public static RequestSpecification sendPostRequestSpec(ShareContext shareContext){
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .setAccept("*/*")
                .setBody(shareContext.getUser())
                .build();
    }

    public static RequestSpecification sendAuthorizedCustomerGetRequest(ShareContext shareContext)
    {
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .setAuth(oauth2(shareContext.getCustomerToken()))
                .build();
    }

    public static RequestSpecification sendGETRequest(ShareContext shareContext)
    {
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .build();
    }

    public static RequestSpecification sendAuthorizedDeleteRequest(ShareContext shareContext){
        return new RequestSpecBuilder()
                .setAuth(oauth2(shareContext.getCustomerToken()))
                .setBaseUri(shareContext.getBaseUri())
                .build();
    }

    public static RequestSpecification sendDeleteRequest(ShareContext shareContext){
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .build();
    }

    public static RequestSpecification sendAuthorizedPostRequest(ShareContext shareContext){
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .setAuth(oauth2(shareContext.getCustomerToken()))
                .setBody(shareContext.getUser())
                .build();
    }

    public static RequestSpecification sendAuthorizedWaiterPostRequest(ShareContext shareContext)
    {
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .setAuth(oauth2(shareContext.getWaiterToken()))
                .setBody(shareContext.getUser())
                .build();
    }

    public static RequestSpecification sendAuthorizedPutRequest(ShareContext shareContext)
    {
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .setAuth(oauth2(shareContext.getCustomerToken()))
                .setBody(shareContext.getUser())
                .build();
    }
}
