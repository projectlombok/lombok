
@jakarta.persistence.Entity(name = "Post")
class Post {
    @jakarta.persistence.OneToMany(mappedBy = "post")
    private java.util.List<PostComment> comments = new java.util.ArrayList<>();

    @jakarta.persistence.OneToOne(mappedBy = "post")
    private PostDetails details;

    @jakarta.persistence.ManyToMany(mappedBy = "posts")
    private java.util.Set<Tag> tags = new java.util.HashSet<>();

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void addPostComment(PostComment postComment) {
        this.comments.add(postComment);
        postComment.setPost(this);
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void removePostComment(PostComment postComment) {
        this.comments.remove(postComment);
        postComment.setPost(null);
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void updatePostDetails(PostDetails postDetails) {
        if (postDetails == null) {
            if (this.details != null) {
                this.details.setPost(null);
            }
        } else {
            postDetails.setPost(this);
        }

        this.details = postDetails;
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getPosts().add(this);
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getPosts().remove(this);
    }
}

@jakarta.persistence.Entity(name = "PostComment")
class PostComment {
    @jakarta.persistence.ManyToOne
    private Post post;

    public void setPost(final Post post) {
        this.post = post;
    }
}

@jakarta.persistence.Entity(name = "PostDetails")
class PostDetails {
    @jakarta.persistence.OneToOne
    private Post post;

    public void setPost(final Post post) {
        this.post = post;
    }
}

@jakarta.persistence.Entity(name = "Tag")
class Tag {
    @jakarta.persistence.ManyToMany
    private java.util.Set<Post> posts = new java.util.HashSet<>();

    public java.util.Set<Post> getPosts() {
        return this.posts;
    }
}
