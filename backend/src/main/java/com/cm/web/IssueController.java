package com.cm.web;

import com.cm.domain.IssueType;
import com.cm.dto.Views;
import com.cm.service.IssueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IssueController {

    private final IssueService issues;

    public IssueController(IssueService issues) {
        this.issues = issues;
    }

    @GetMapping("/events/{eventId}/issues")
    public List<Map<String, Object>> list(@PathVariable Long eventId) {
        Roles.requireStaff();
        return issues.listByEvent(eventId).stream().map(Views::issue).toList();
    }

    /** 工作人员/管理员手动登记异常（如食品资质不完整、同类冲突、用电超载） */
    @PostMapping("/events/{eventId}/issues")
    public Map<String, Object> report(@PathVariable Long eventId, @RequestBody Map<String, Object> body) {
        Roles.requireStaff();
        IssueType type = IssueType.valueOf(body.get("type").toString());
        Long assignmentId = body.get("assignmentId") == null || body.get("assignmentId").toString().isBlank()
                ? null : Long.valueOf(body.get("assignmentId").toString());
        int penalty = Integer.parseInt(String.valueOf(body.getOrDefault("creditPenalty", 10)));
        return Views.issue(issues.report(eventId, assignmentId, type,
                (String) body.get("description"), Roles.user().getId(), penalty));
    }

    /** 处理异常：resolution + 是否追加扣分 */
    @PostMapping("/issues/{id}/resolve")
    public Map<String, Object> resolve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Roles.requireStaff();
        boolean penalize = Boolean.parseBoolean(String.valueOf(body.getOrDefault("penalize", false)));
        return Views.issue(issues.resolve(id, (String) body.get("resolution"),
                penalize, Roles.user().getId()));
    }
}
