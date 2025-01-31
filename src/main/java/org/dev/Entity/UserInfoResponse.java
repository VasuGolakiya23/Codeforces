package org.dev.Entity;

import io.vertx.ext.auth.User;
import org.dev.Entity.UserInfo;

import java.util.List;

public class UserInfoResponse {
    private String status;
    private List<UserInfo> result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<UserInfo> getResult() {
        return result;
    }

    public void setResult(List<UserInfo> result) {
        this.result = result;
    }
}
