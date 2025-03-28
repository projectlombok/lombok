
@jakarta.persistence.Entity(name = "Post")
class Post {
    @lombok.experimental.JpaAssociationSync
    @jakarta.persistence.OneToMany(mappedBy = "post")
    private java.util.List<PostComment> comments = new java.util.ArrayList<>();
}

@jakarta.persistence.Entity(name = "PostComment")
class PostComment {
    @jakarta.persistence.ManyToOne
    private Post post;

    public void setPost(final Post post) {
        this.post = post;
    }
}
