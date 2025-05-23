package context;

import io.restassured.response.Response;
import models.SignUp;

public class ShareContext {

    private String baseUri;
    private Object user;
    private Response response;
    private String waiterToken;
    private String customerToken;

    public String getBaseUri(){
        return this.baseUri;
    }

    public void setBaseUri(String baseUri)
    {
        this.baseUri = baseUri;
    }

    public Object getUser(){
        return this.user;
    }

    public void setUser(Object user)
    {
        this.user = user;
    }

    public Response getResponse(){
        return this.response;
    }

    public void setResponse(Response response)
    {
        this.response = response;
    }

    public String getWaiterToken(){
        return this.waiterToken;
    }

    public void setWaiterToken(String waiterToken)
    {
        this.waiterToken = waiterToken;
    }

    public String getCustomerToken()
    {
        return this.customerToken;
    }

    public void setCustomerToken(String customerToken)
    {
        this.customerToken = customerToken;
    }



}
