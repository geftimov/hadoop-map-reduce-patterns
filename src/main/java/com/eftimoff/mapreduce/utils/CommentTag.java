package com.eftimoff.mapreduce.utils;

public enum CommentTag {

    ID("Id"),
    POSTID("PostId"),
    SCORE("Score"),
    TEXT("Text"),
    COMMENT("Comment"),
    CREATION_DATE("CreationDate"),
    USERID("UserId");

    private final String variable;


    CommentTag(String variable) {
        this.variable = variable;

    }

    @Override
    public String toString() {
       return variable;
    }

}