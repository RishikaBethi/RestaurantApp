package helpers.specBuilders;

import context.ShareContext;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

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
}
