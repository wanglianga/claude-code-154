package com.cm.web;

import com.cm.service.ComplaintService;
import com.cm.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 居民公开接口：无需登录，仅可读取已公示内容与提交投诉 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final ReviewService review;
    private final ComplaintService complaints;

    public PublicController(ReviewService review, ComplaintService complaints) {
        this.review = review;
        this.complaints = complaints;
    }

    /** 已公示活动列表 */
    @GetMapping("/events")
    public List<Map<String, Object>> events() {
        return review.publicEvents();
    }

    /** 公示详情：总捐赠、用途、摊主履约、异常处理 */
    @GetMapping("/events/{id}")
    public Map<String, Object> event(@PathVariable Long id) {
        return review.publicView(id);
    }

    /** 居民公开投诉入口（需提供活动 ID） */
    @PostMapping("/events/{id}/complaints")
    public Map<String, Object> complaint(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return com.cm.dto.Views.complaint(complaints.create(id, null,
                (String) body.get("complainant"), (String) body.get("contact"),
                (String) body.get("content")));
    }
}
