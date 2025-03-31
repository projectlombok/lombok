
@jakarta.persistence.Entity(name = "Post")
class Post {
    @jakarta.persistence.OneToMany(mappedBy = "post")
    private java.util.List<PostComment> comments = new java.util.ArrayList<>();

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
}

@jakarta.persistence.Entity(name = "PostComment")
class PostComment {
    @jakarta.persistence.ManyToOne
    private Post post;

    public void setPost(final Post post) {
        this.post = post;
    }
}
