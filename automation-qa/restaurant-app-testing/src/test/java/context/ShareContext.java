package context;

import io.restassured.response.Response;
import models.SignUp;

public class ShareContext {

    private String baseUri;
    private Object user;
    private Response response;
    private String token;

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

    public String getToken(){
        return this.token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }




}
