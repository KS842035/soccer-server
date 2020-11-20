package com.ksu.soccerserver.comment;

import com.ksu.soccerserver.account.Account;
import com.ksu.soccerserver.account.AccountRepository;
import com.ksu.soccerserver.board.Board;
import com.ksu.soccerserver.board.BoardRepository;
import com.ksu.soccerserver.comment.dto.CommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/api/boards")
@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentRepository commentRepository;
    private final AccountRepository accountRepository;
    private final BoardRepository boardRepository;

    @PostMapping("/{boardId}/comment/{accountId}")
    ResponseEntity<?> postComment(@RequestBody CommentRequest commentRequest, @PathVariable Long boardId, @PathVariable Long accountId) {

        Account account = accountRepository.findById(accountId).get();
        Board board = boardRepository.findById(boardId).get();
        Comment saveComment = commentRepository.save(Comment.builder()
                .content(commentRequest.getContent())
                .createdAt(LocalDateTime.now())
                .build());

        saveComment.commentAccount(account);
        saveComment.commentBoard(board);

        commentRepository.save(saveComment);
        return new ResponseEntity<>("Create new Comments" + board.getId() + "번째 게시판", HttpStatus.CREATED);
    }

    @GetMapping("/{boardId}/comment")
    ResponseEntity<?> getComment(@PathVariable Long boardId){
        List<Comment> comments = commentRepository.findByBoard(boardRepository.findById(boardId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 게시판입니다.")));
        if(comments.isEmpty()){
            return new ResponseEntity<>("댓글이 없습니다.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @PutMapping("/{boardId}/comment/{commentId}")
    ResponseEntity<?> putComment(@RequestBody CommentRequest commentRequest, @PathVariable Long boardId, @PathVariable Long commentId){
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 게시판입니다."));
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."));

        findComment.setContent(commentRequest.getContent());
        findComment.setTime(LocalDateTime.now());

        commentRepository.save(findComment);
        return new ResponseEntity<>(findComment, HttpStatus.OK);

    }

    @DeleteMapping("/{boardId}/comment/{commentId}")
    ResponseEntity<?> deleteComment(@PathVariable Long boardId, @PathVariable Long commentId){
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 게시판입니다."));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."));

        commentRepository.delete(comment);
        return new ResponseEntity<>(board.getTitle()+"의 댓글 Delete : " + comment.getContent(), HttpStatus.OK );
    }


}