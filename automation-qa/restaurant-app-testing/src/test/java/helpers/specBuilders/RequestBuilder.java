package helpers.specBuilders;

import context.ShareContext;
import io.cucumber.cienvironment.internal.com.eclipsesource.json.JsonHandler;
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
                .setBody(shareContext.getUser())
                .build();
    }
}
