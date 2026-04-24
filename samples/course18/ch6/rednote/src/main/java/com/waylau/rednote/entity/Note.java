package com.waylau.rednote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Note 笔记实体
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
@Entity
@Table(name = "t_note")
// @Data集合了@Getter @Setter @ToString @EqualsAndHashCode
@Data
// 无参构造器
@NoArgsConstructor
// 包含所有参数的构造器
@AllArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long noteId;

    private String title;

    private String content;

    @ElementCollection
    private List<String> topics = new ArrayList<>();

    @ElementCollection
    private List<String> images = new ArrayList<>();

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(updatable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    private LocalDateTime updateAt = LocalDateTime.now();

    @OneToMany(mappedBy = "note", cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

    // 统计点赞数，使用Transient注解
    @Transient
    public long getLikeCount() {
        return likes.size();
    }

    // 笔记是否被用户点赞
    @Transient
    public boolean isLikedByUser(Long userId) {
        if (userId == null) {
            return false;
        }

        for (Like like : likes) {
            if (like.getUser().getUserId().equals(userId)) {
                return true;
            }
        }

        return false;
    }

    @OneToMany(mappedBy = "note", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 计算评论数
    @Transient
    public long getCommentCount() {
        return comments.size();
    }
}
