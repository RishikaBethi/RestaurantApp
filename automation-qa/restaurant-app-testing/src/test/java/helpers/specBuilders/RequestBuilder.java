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

    public static RequestSpecification sendGetRequest(ShareContext shareContext,String customerToken)
    {
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .setAuth(oauth2(customerToken))
                .build();
    }

    public static RequestSpecification sendGetRequest(ShareContext shareContext)
    {
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .build();
    }

    public static RequestSpecification sendDeleteRequest(ShareContext shareContext,String customerToken){
        return new RequestSpecBuilder()
                .setAuth(oauth2(customerToken))
                .setBaseUri(shareContext.getBaseUri())
                .build();
    }

    public static RequestSpecification sendDeleteRequest(ShareContext shareContext){
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .build();
    }

    public static RequestSpecification sendPostRequest(ShareContext shareContext,String customerToken){
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .setAuth(oauth2(customerToken))
                .setBody(shareContext.getUser())
                .build();
    }

    public static RequestSpecification sendAuthorizedPostRequest(ShareContext shareContext,String waiterToken)
    {
        return new RequestSpecBuilder()
                .setBaseUri(shareContext.getBaseUri())
                .setContentType("application/json")
                .setAuth(oauth2(waiterToken))
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
