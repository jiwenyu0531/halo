package run.halo.app.controller.content.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.JournalComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.JournalCommentParam;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.CommentWithHasChildrenVO;
import run.halo.app.service.JournalCommentService;
import run.halo.app.service.JournalService;
import run.halo.app.service.OptionService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author johnniang
 * @date 19-4-26
 */
@RestController("PortalJournalController")
@RequestMapping("/api/content/journals")
public class JournalController {

    private final JournalService journalService;

    private final JournalCommentService journalCommentService;

    private final OptionService optionService;

    public JournalController(JournalService journalService,
                             JournalCommentService journalCommentService,
                             OptionService optionService) {
        this.journalService = journalService;
        this.journalCommentService = journalCommentService;
        this.optionService = optionService;
    }

    @GetMapping("{journalId:\\d+}/comments/top_view")
    public Page<CommentWithHasChildrenVO> listTopComments(@PathVariable("journalId") Integer journalId,
                                                          @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                          @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return journalCommentService.pageTopCommentsBy(journalId, CommentStatus.PUBLISHED, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{journalId:\\d+}/comments/{commentParentId:\\d+}/children")
    public List<BaseCommentDTO> listChildrenBy(@PathVariable("journalId") Integer journalId,
                                               @PathVariable("commentParentId") Long commentParentId,
                                               @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        // Find all children comments
        List<JournalComment> postComments = journalCommentService.listChildrenBy(journalId, commentParentId, CommentStatus.PUBLISHED, sort);
        // Convert to base comment dto
        return journalCommentService.convertTo(postComments);
    }

    @GetMapping("{journalId:\\d+}/comments/tree_view")
    @ApiOperation("Lists comments with tree view")
    public Page<BaseCommentVO> listCommentsTree(@PathVariable("journalId") Integer journalId,
                                                @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return journalCommentService.pageVosBy(journalId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{journalId:\\d+}/comments/list_view")
    @ApiOperation("Lists comment with list view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("journalId") Integer journalId,
                                                      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                      @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return journalCommentService.pageWithParentVoBy(journalId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @PostMapping("comments")
    @ApiOperation("Comments a post")
    @CacheLock(autoDelete = false, traceRequest = true)
    public BaseCommentDTO comment(@RequestBody JournalCommentParam journalCommentParam) {
        return journalCommentService.convertTo(journalCommentService.createBy(journalCommentParam));
    }
}
