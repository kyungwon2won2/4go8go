package com.example.demo.domain.post.model;

import lombok.Data;
import java.util.Date;

@Data
public class Favorite {
    private int favoriteId;
    private int userId;
    private int postId;
    private Date createdAt;
}
