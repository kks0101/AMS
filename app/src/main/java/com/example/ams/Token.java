package com.example.ams;

public class Token{
    private String tokenId, userId;
    public Token(String token, String userId){
        this.tokenId = token;
        this.userId = userId;
    }

    public String getToken(){
        return this.tokenId;
    }
    public String getUserId(){
        return this.userId;
    }
    public void setToken(String token){
        this.tokenId = token;
    }
    public void setUserId(String userId){
        this.userId =userId;
    }
}
