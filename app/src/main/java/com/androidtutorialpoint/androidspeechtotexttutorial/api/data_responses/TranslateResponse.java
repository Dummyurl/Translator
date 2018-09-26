package com.androidtutorialpoint.androidspeechtotexttutorial.api.data_responses;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rishabh on 5/3/16.
 */

public class TranslateResponse {

    /*{
        "code": 200,
            "lang": "en-zh",
            "text": [
        "你好怎么样"
        ]
    }*/
    @JsonProperty("code")
    private Integer code;
    @JsonProperty("lang")
    private String lang;
    @JsonProperty("text")
    private List<String> text = null;

    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(Integer code) {
        this.code = code;
    }

    @JsonProperty("lang")
    public String getLang() {
        return lang;
    }

    @JsonProperty("lang")
    public void setLang(String lang) {
        this.lang = lang;
    }

    @JsonProperty("text")
    public List<String> getText() {
        return text;
    }

    @JsonProperty("text")
    public void setText(List<String> text) {
        this.text = text;
    }

}
