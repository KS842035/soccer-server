package com.ksu.soccerserver.comment;

import com.ksu.soccerserver.account.Account;
import com.ksu.soccerserver.account.AccountRepository;
import com.ksu.soccerserver.account.CurrentAccount;
import com.ksu.soccerserver.board.Board;
import com.ksu.soccerserver.board.BoardRepository;
import com.ksu.soccerserver.comment.dto.CommentRequest;
import com.ksu.soccerserver.comment.dto.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/api/comments")
@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentRepository commentRepository;
    private final AccountRepository accountRepository;
    private final BoardRepository boardRepository;
    private final ModelMapper modelMaapper;

    @PostMapping
    ResponseEntity<?> postComment(@RequestBody CommentRequest commentRequest, @CurrentAccount Account currentAccount) {

        Account account = accountRepository.findById(currentAccount.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"NO_FOUND_ACCOUNT"));

        Board board = boardRepository.findById(commentRequest.getBoardId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"NO_FOUND_BOARD"));

        Comment postComment = commentRequest.toEntity(board, account);
        Comment saveComment = commentRepository.save(postComment);

        CommentResponse response = modelMaapper.map(saveComment, CommentResponse.class);

        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    /*
    @GetMapping("/{boardId}/comment")
    ResponseEntity<?> getComment(@PathVariable Long boardId){
        List<Comment> comments = commentRepository.findByBoard(boardRepository.findById(boardId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 게시판입니다.")));
        if(comments.isEmpty()){
            return new ResponseEntity<>("댓글이 없습니다.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }
    */

    @PutMapping("/{commentId}")
    ResponseEntity<?> putComment(@RequestBody CommentRequest commentRequest, @PathVariable Long commentId){
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."));

        findComment.setContent(commentRequest.getContent());
        findComment.setTime(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(findComment);

        CommentResponse response = modelMaapper.map(updatedComment, CommentResponse.class);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @DeleteMapping("/{commentId}")
    ResponseEntity<?> deleteComment(@PathVariable Long commentId){
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."));

        commentRepository.delete(comment);

        CommentResponse response = modelMaapper.map(comment, CommentResponse.class);

        return new ResponseEntity<>(response, HttpStatus.OK );
    }


}