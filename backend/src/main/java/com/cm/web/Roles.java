package com.cm.web;

import com.cm.domain.User;
import com.cm.domain.UserRole;
import com.cm.security.CurrentUser;

/** 角色校验小工具 */
public final class Roles {
    private Roles() {}

    public static User user() {
        User u = CurrentUser.get();
        if (u == null) {
            throw new ApiException(401, "未登录");
        }
        return u;
    }

    public static void requireAdmin() {
        User u = user();
        if (u.getRole() != UserRole.ADMIN) {
            throw new ApiException(403, "仅社区管理员可操作");
        }
    }

    public static void requireStaff() {
        User u = user();
        if (u.getRole() != UserRole.ADMIN && u.getRole() != UserRole.STAFF) {
            throw new ApiException(403, "仅工作人员可操作");
        }
    }

    public static void requireVendor() {
        User u = user();
        if (u.getRole() != UserRole.VENDOR) {
            throw new ApiException(403, "仅摊主账号可操作");
        }
    }
}
